import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { QuizComponent } from '../quiz/quiz.component';
import { ButtonComponent } from '../shared/button/button.component';
import { marked } from 'marked';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { AdminService, Quiz, Question as AdminQuestion } from '../../services/admin.service';
import {
  QuizSubmissionService,
  SubmissionAnswerResponse,
  QuizSubmissionResponse,
} from '../../services/quiz-submission.service';
import { of, forkJoin } from 'rxjs';
import { finalize, take } from 'rxjs/operators';

interface QuizOptionView {
  id: string;
  text: string;
}

interface QuizQuestionView {
  id: string;
  prompt: string;
  type: 'mcq' | 'text';
  options?: QuizOptionView[];
  audioUrl?: string;
  correctOptionId?: string;
  skill?: string;
  explanation?: string;
}

interface QuizSectionView {
  id: string;
  title: string;
  questions: QuizQuestionView[];
}

interface SectionResult {
  autoScore: number;
  autoTotal: number;
  answers: { [id: string]: string };
  textAnswers: { [id: string]: string };
  aiScore?: number | null;
  teacherNote?: string | null;
  aiByQuestion?: Record<string, { score: number | null; teacherNote: string | null }>;
  timestamp: string;
}

@Component({
  selector: 'app-quiz-take',
  standalone: true,
  imports: [CommonModule, FormsModule, QuizComponent, ButtonComponent],
  templateUrl: './quiz-take.component.html',
  styleUrls: ['./quiz-take.component.css']
})
export class QuizTakeComponent implements OnInit, OnDestroy {
  courseId: number | null = null;
  quizId: number | null = null;
  courseName = '';
  selectedQuiz: Quiz | null = null;
  quizSections: QuizSectionView[] = [];
  answers: { [k:string]: string } = {};
  textAnswers: { [k:string]: string } = {};
  quizSubmitted = false;
  quizAutoScore: number | null = null;
  private latestAutoTotal: number | null = null;
  aiGradeScore: number | null = null;
  private _aiTeacherNote: string | null = null;
  get aiTeacherNote(): string | null {
    return this._aiTeacherNote;
  }
  set aiTeacherNote(value: string | null) {
    this._aiTeacherNote = value;
    this.updateFeedback(value);
  }
  submissionId: number | null = null;
  userId?: number;
  savingSection = false;
  private submissionEnsured = false;

  // Exam orchestration: order, metadata, timers and states
  sectionOrder: string[] = [];
  sectionMeta: { [id: string]: { title: string; minutes: number; desc?: string } } = {
    listening: { title: 'Kỹ năng Nghe (IELTS Listening)', minutes: 30, desc: '30 phút' },
    reading: { title: 'Kỹ năng Đọc (IELTS Reading)', minutes: 60, desc: '60 phút' },
    writing: { title: 'Kỹ năng Viết (IELTS Writing)', minutes: 60, desc: '60 phút' },
    speaking: { title: 'Kỹ năng Nói (IELTS Speaking)', minutes: 15, desc: '11-15 phút' },
    general: { title: 'Tổng hợp kỹ năng', minutes: 45, desc: 'Thời gian tham khảo' }
  };
  currentSectionIndex: number | null = null;
  sectionRemaining: { [id: string]: number } = {};
  sectionStarted: { [id: string]: boolean } = {};
  sectionCompleted: { [id: string]: boolean } = {};
  sectionResults: { [sectionId: string]: SectionResult } = {};
  private sectionDefinitions: Record<string, QuizSectionView> = {};
  private activeTimer: any = null;
  private activeTimerSectionId: string | null = null;
  loading = false;
  error: string | null = null;
  private readonly defaultSectionSequence = ['listening', 'reading', 'writing', 'speaking'];
  private readonly mandatorySectionKeys = ['listening', 'reading', 'writing', 'speaking'];
  private readonly optionLetters = Array.from('ABCDEFGHIJKLMNOPQRSTUVWXYZ');

  get currentSectionId(): string | null {
    if (this.currentSectionIndex === null) {
      return null;
    }
    return this.sectionOrder[this.currentSectionIndex] ?? null;
  }

  get isCurrentSectionAiGraded(): boolean {
    return this.isAiGradedSection(this.currentSectionId);
  }

  get isCurrentSectionWriting(): boolean {
    return (this.currentSectionId || '').toLowerCase() === 'writing';
  }

  get isCurrentSectionDetailedReviewAllowed(): boolean {
    const sid = (this.currentSectionId || '').toLowerCase();
    return sid === 'listening' || sid === 'reading';
  }

  get quizAutoTotal(): number {
    if (this.latestAutoTotal !== null) {
      return this.latestAutoTotal;
    }
    return this.computeSectionTotal(this.quizSections);
  }

  markdownHtml?: SafeHtml;
  currentAiByQuestion: Record<string, { score: number | null; teacherNote: string | null }> = {};
  aiMarkdownByQuestion: Record<string, SafeHtml> = {};

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private admin: AdminService,
    private quizSubmission: QuizSubmissionService,
    private sanitizer: DomSanitizer
  ) {}

  ngOnInit(): void {
    const idStr = this.route.snapshot.paramMap.get('id');
    const qId = this.route.snapshot.paramMap.get('quizId');
    const id = idStr ? parseInt(idStr, 10) : null;
    this.courseId = id;
    const quizNumeric = qId ? parseInt(qId, 10) : null;
    this.quizId = Number.isFinite(quizNumeric) ? quizNumeric : null;
    this.userId = this.resolveUserId();
    if (!id || !this.quizId) {
      this.router.navigate(['/my-courses']);
      return;
    }
    this.loadCourseTitle(id);
    this.loadQuizDetail(this.quizId);
  }

  private updateFeedback(note: string | null) {
    this.markdownHtml = this.renderMarkdown(note);
  }

  ngOnDestroy(): void {
    this.clearActiveTimer();
  }

  onCancel() {
    if (this.courseId) {
      this.router.navigate(['/course', this.courseId, 'quizzes'], { replaceUrl: true });
    } else {
      this.router.navigate(['/my-courses'], { replaceUrl: true });
    }
  }

  // --- Section / timer helpers ---
  openSection(index: number) {
    this.ensureSubmissionSession();
    this.currentSectionIndex = index;
    const id = this.sectionOrder[index];
    this.pauseActiveTimer();
    const sect = this.sectionDefinitions[id];
    this.quizSections = sect ? [sect] : [];
    const sr = this.sectionResults[id] || null;
    this.answers = this.normalizeAnswers(sr?.answers || {});
    this.textAnswers = sr?.textAnswers || {};
    this.quizSubmitted = !!sr;
    this.quizAutoScore = sr?.autoScore ?? null;
    this.latestAutoTotal = sr?.autoTotal ?? this.computeSectionTotal(this.quizSections);
    this.aiGradeScore = sr?.aiScore ?? null;
    this.aiTeacherNote = sr?.teacherNote ?? null;
    this.currentAiByQuestion = sr?.aiByQuestion ?? {};
    this.aiMarkdownByQuestion = this.renderMarkdownMap(this.currentAiByQuestion);
    this.resumeTimerIfNeeded(id);
  }

  startSection(index: number) {
    const id = this.sectionOrder[index];
    this.ensureSubmissionSession();
    this.pauseActiveTimer();
    if (!this.sectionStarted[id]) {
      if (!this.sectionRemaining[id]) this.sectionRemaining[id] = (this.sectionMeta[id]?.minutes ?? 0) * 60;
      this.sectionStarted[id] = true;
    }
    this.currentSectionIndex = index;
    const sect = this.sectionDefinitions[id];
    this.quizSections = sect ? [sect] : [];
    this.answers = {};
    this.textAnswers = {};
    this.quizSubmitted = false;
    this.quizAutoScore = null;
    this.latestAutoTotal = this.computeSectionTotal(this.quizSections);
    this.aiGradeScore = null;
    this.aiTeacherNote = null;
    this.currentAiByQuestion = {};
    this.aiMarkdownByQuestion = {};
    this.startTimerForSection(id);
  }

  clearActiveTimer() {
    if (this.activeTimer) { clearInterval(this.activeTimer); this.activeTimer = null; }
    this.activeTimerSectionId = null;
  }

  formatTime(sec: number) {
    const m = Math.floor(sec / 60).toString().padStart(2, '0');
    const s = Math.floor(sec % 60).toString().padStart(2, '0');
    return `${m}:${s}`;
  }

  submitSection(index: number) {
    const id = this.sectionOrder[index];
    const section = this.sectionDefinitions[id];
    if (!section) {
      return;
    }
    this.savingSection = true;
    const finalizeSection = (responses?: SubmissionAnswerResponse[] | null) => {
      const ai = this.isAiGradedSection(id) ? this.computeAiGradeFromResponses(responses) : null;
      const aiByQuestion = this.isAiGradedSection(id) ? this.computeAiByQuestionFromResponses(responses) : {};
      this.applySectionResult(index, section, ai?.score ?? null, ai?.teacherNote ?? null, aiByQuestion);
      if (this.isAiGradedSection(id) && (!ai?.score && !ai?.teacherNote)) {
        this.refreshAiGradeForSection(id);
      }
    };
    this.persistSectionAnswers(section)
      .pipe(
        take(1),
        finalize(() => (this.savingSection = false))
      )
      .subscribe({
        next: (responses: SubmissionAnswerResponse[] | any) => finalizeSection(responses as SubmissionAnswerResponse[]),
        error: (error: unknown) => {
          console.warn('Failed to sync answers for section', section.id, error);
          finalizeSection(null);
        },
      });
  }

  private pauseActiveTimer() {
    this.clearActiveTimer();
  }

  private startTimerForSection(sectionId: string) {
    if (!sectionId) { return; }
    this.clearActiveTimer();
    this.activeTimerSectionId = sectionId;
    this.activeTimer = setInterval(() => {
      const remaining = Math.max(0, (this.sectionRemaining[sectionId] ?? 0) - 1);
      this.sectionRemaining[sectionId] = remaining;
      if (remaining <= 0) {
        const sectionIndex = this.sectionOrder.indexOf(sectionId);
        if (sectionIndex >= 0) {
          this.submitSection(sectionIndex);
        }
      }
    }, 1000);
  }

  private resumeTimerIfNeeded(sectionId: string) {
    if (!sectionId) { return; }
    const alreadyRunning = this.activeTimerSectionId === sectionId;
    if (alreadyRunning) { return; }
    if (this.sectionStarted[sectionId] && !this.sectionCompleted[sectionId]) {
      this.startTimerForSection(sectionId);
    }
  }

  

  private ensureSubmissionSession() {
    if (!this.quizId || !this.userId) {
      return;
    }
    if (this.submissionId || this.submissionEnsured) {
      return;
    }
    this.submissionEnsured = true;
    this.quizSubmission
      .startSubmission(this.quizId, this.userId)
      .pipe(take(1))
      .subscribe({
        next: (submission) => {
          this.submissionId = submission?.id ?? null;
        },
        error: (error) => {
          console.warn('Unable to start quiz submission session', error);
          this.submissionEnsured = false;
        },
      });
  }

  private persistSectionAnswers(section: QuizSectionView) {
    if (!this.submissionId || !this.quizId || !this.userId) {
      return of([]);
    }
    const calls: import('rxjs').Observable<SubmissionAnswerResponse>[] = [];
    section.questions.forEach((question) => {
      const numericId = Number(question.id);
      if (!Number.isFinite(numericId)) {
        return;
      }
      const key = String(question.id);

      // If we have an audio file recorded for this question, upload multipart
      const audioFile = this.audioFiles[key];
      if (audioFile) {
        calls.push(this.quizSubmission.uploadAnswerAudio(this.submissionId!, numericId, audioFile));
        return;
      }

      // MCQ or text: send JSON payload as before
      if (question.type === 'mcq') {
        const selected = this.answers[key];
        if (selected) {
          calls.push(this.quizSubmission.saveAnswer(this.submissionId!, { questionId: numericId, selectedOption: selected }));
        }
      } else if (question.type === 'text') {
        const response = this.textAnswers[key];
        if (response && response.trim()) {
          calls.push(this.quizSubmission.saveAnswer(this.submissionId!, { questionId: numericId, textAnswer: response.trim() }));
        }
      }
    });

    if (!calls.length) {
      return of([]);
    }
    // Run all saves/uploads in parallel
    return forkJoin(calls);
  }

  // Recording state and storage for audio files per question (speaking)
  private mediaStream: MediaStream | null = null;
  private mediaRecorder: MediaRecorder | null = null;
  private audioChunks: { [id: string]: Blob[] } = {};
  audioBlobs: { [id: string]: Blob } = {};
  audioFiles: { [id: string]: File } = {};
  audioUrls: { [id: string]: string } = {};
  recordingQuestionId: string | null = null;
  recording = false;

  // Start recording for a specific question (requests microphone)
  async startRecording(questionId: string) {
    try {
      if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
        console.warn('getUserMedia not supported');
        return;
      }
      this.recordingQuestionId = questionId;
      this.recording = true;
      this.audioChunks[questionId] = [];
      this.mediaStream = await navigator.mediaDevices.getUserMedia({ audio: true });
      this.mediaRecorder = new MediaRecorder(this.mediaStream);
      this.mediaRecorder.ondataavailable = (ev: BlobEvent) => {
        if (ev.data && ev.data.size > 0) {
          this.audioChunks[questionId].push(ev.data);
        }
      };
      this.mediaRecorder.start();
    } catch (e) {
      console.warn('Failed to start recording', e);
      this.recording = false;
      this.recordingQuestionId = null;
    }
  }

  // Stop recording and finalize Blob/File for the question
  async stopRecording(): Promise<void> {
    if (!this.recordingQuestionId || !this.mediaRecorder) return;
    const qid = this.recordingQuestionId;
    return new Promise((resolve) => {
      this.mediaRecorder!.onstop = () => {
        const chunks = this.audioChunks[qid] || [];
        const blob = new Blob(chunks, { type: 'audio/webm' }); // webm is widely supported; backend can convert if needed
        this.audioBlobs[qid] = blob;
        const filename = `speaking_${qid}_${Date.now()}.webm`;
        const file = new File([blob], filename, { type: blob.type });
        this.audioFiles[qid] = file;
        try {
          this.audioUrls[qid] = URL.createObjectURL(blob);
        } catch {}
        // cleanup stream
        if (this.mediaStream) {
          this.mediaStream.getTracks().forEach((t) => t.stop());
        }
        this.mediaRecorder = null;
        this.mediaStream = null;
        this.recording = false;
        this.recordingQuestionId = null;
        resolve();
      };
      try {
        this.mediaRecorder!.stop();
      } catch {
        // If stop throws, still attempt cleanup
        this.mediaRecorder = null;
        this.mediaStream?.getTracks().forEach((t) => t.stop());
        this.mediaStream = null;
        this.recording = false;
        this.recordingQuestionId = null;
        resolve();
      }
    });
  }

  // Optional helper: remove recorded audio for a question
  removeRecording(questionId: string) {
    delete this.audioChunks[questionId];
    delete this.audioBlobs[questionId];
    if (this.audioUrls[questionId]) {
      try { URL.revokeObjectURL(this.audioUrls[questionId]); } catch {}
      delete this.audioUrls[questionId];
    }
    delete this.audioFiles[questionId];
  }

  // Receive recorded audio emitted from child `app-quiz` component
  onChildAudioCaptured(event: { questionId: string; file: File }) {
    const qid = String(event.questionId);
    this.audioFiles[qid] = event.file;
    this.audioBlobs[qid] = event.file;
    try { this.audioUrls[qid] = URL.createObjectURL(event.file); } catch {}
  }

  private applySectionResult(
    index: number,
    section: QuizSectionView,
    aiScore: number | null,
    teacherNote: string | null,
    aiByQuestion: Record<string, { score: number | null; teacherNote: string | null }>
  ) {
    const id = this.sectionOrder[index];
    let autoTotal = 0;
    let autoScore = 0;
    section.questions.forEach((q) => {
      if (q.type === 'mcq') {
        autoTotal++;
        const a = this.answers[q.id];
        if (a === q.correctOptionId) {
          autoScore++;
        }
      }
    });
    this.sectionResults[id] = {
      autoScore,
      autoTotal,
      answers: { ...this.answers },
      textAnswers: { ...this.textAnswers },
      aiScore,
      teacherNote,
      aiByQuestion,
      timestamp: new Date().toISOString(),
    };
    this.sectionCompleted[id] = true;
    this.clearActiveTimer();
    this.quizSubmitted = true;
    this.quizAutoScore = autoScore;
    this.latestAutoTotal = autoTotal;
    this.aiGradeScore = aiScore;
    this.aiTeacherNote = teacherNote;
    this.currentAiByQuestion = aiByQuestion;
    this.aiMarkdownByQuestion = this.renderMarkdownMap(this.currentAiByQuestion);
  }

  private resolveUserId(): number | undefined {
    if (typeof window === 'undefined') {
      return undefined;
    }
    const sources = ['user_profile', 'account_profile'];
    for (const key of sources) {
      try {
        const raw = localStorage.getItem(key);
        if (!raw) continue;
        const profile = JSON.parse(raw);
        const candidate =
          profile?.id ??
          profile?.userId ??
          profile?.user?.id ??
          profile?.result?.id ??
          profile?.profileId ??
          profile?.code;
        const parsed = Number(candidate);
        if (Number.isFinite(parsed) && parsed > 0) {
          return parsed;
        }
      } catch (e) {
        console.warn('Failed to parse stored profile for user id', e);
      }
    }
    return undefined;
  }

  

  private loadQuizDetail(quizId: number) {
    if (!quizId) {
      this.router.navigate(['/my-courses']);
      return;
    }
    this.loading = true;
    this.error = null;
    this.admin
      .getQuizDetail(quizId)
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (quiz) => {
          this.selectedQuiz = quiz;
          this.prepareQuiz(quiz);
          this.fetchUserSubmissionsAndRestore();
          this.ensureSubmissionSession();
        },
        error: (error) => {
          console.error('Failed to load quiz detail', error);
          this.error = 'Không thể tải chi tiết bài kiểm tra.';
        },
      });
  }

  private loadCourseTitle(courseId: number) {
    if (!courseId) {
      return;
    }
    this.admin
      .getCourseDetail(courseId)
      .pipe(take(1))
      .subscribe({
        next: (course) => {
          this.courseName = course?.title || this.courseName;
        },
        error: (error) => {
          console.warn('Unable to load course info for quiz take view', error);
        },
      });
  }

  private prepareQuiz(quiz: Quiz) {
    this.sectionDefinitions = this.buildSectionMap(quiz);
    const keys = Object.keys(this.sectionDefinitions);
    this.sectionOrder = [
      ...this.defaultSectionSequence.filter((key) => keys.includes(key)),
      ...keys.filter((key) => !this.defaultSectionSequence.includes(key)),
    ];
    this.sectionRemaining = {};
    this.sectionStarted = {};
    this.sectionCompleted = {};
    this.sectionResults = {};
    this.currentSectionIndex = null;
    this.quizSections = [];
    this.latestAutoTotal = null;
    this.sectionOrder.forEach((sid) => {
      this.ensureSectionMeta(sid);
      const minutes = this.sectionMeta[sid]?.minutes ?? 0;
      this.sectionRemaining[sid] = minutes * 60;
      this.sectionStarted[sid] = false;
      this.sectionCompleted[sid] = false;
    });
  }

  private fetchUserSubmissionsAndRestore() {
    if (!this.quizId || !this.userId) {
      this.quizSubmitted = false;
      this.quizAutoScore = null;
      this.answers = {};
      this.textAnswers = {};
      this.latestAutoTotal = null;
      return;
    }
    this.quizSubmission
      .getUserSubmissions(this.quizId, this.userId)
      .pipe(take(1))
      .subscribe({
        next: (subs) => {
          if (!subs || !subs.length) {
            this.quizSubmitted = false;
            this.answers = {};
            this.textAnswers = {};
            return;
          }
          // Prefer an in-progress submission, otherwise the most recent
          let preferred = subs.find((s) => s.status === 'DOING') || subs.slice().sort((a, b) => {
            const ta = (a.submitTime || a.startTime || '').toString();
            const tb = (b.submitTime || b.startTime || '').toString();
            return tb.localeCompare(ta);
          })[0];
          if (!preferred) return;
          this.submissionId = preferred.id ?? null;

          // build question -> section map
          const qToSection = this.buildQuestionToSectionMap();

          // reset
          this.answers = {};
          this.textAnswers = {};
          this.sectionResults = {};

          const aiScoreBuckets: Record<string, number[]> = {};
          const aiNoteBuckets: Record<string, string[]> = {};
          const aiByQuestionBuckets: Record<string, Record<string, { score: number | null; teacherNote: string | null }>> = {};

          (preferred.answers || []).forEach((ans) => {
            const qid = String(ans.questionId);
            const sid = qToSection[qid] || 'general';
            if (!this.sectionResults[sid]) {
              this.sectionResults[sid] = {
                autoScore: 0,
                autoTotal: 0,
                answers: {},
                textAnswers: {},
                aiScore: null,
                teacherNote: null,
                aiByQuestion: {},
                timestamp: preferred.submitTime || preferred.startTime || new Date().toISOString(),
              };
            }

            if (ans.selectedOption) this.answers[qid] = String(ans.selectedOption);
            if (ans.textAnswer) this.textAnswers[qid] = String(ans.textAnswer);
            if (ans.selectedOption) this.sectionResults[sid].answers[qid] = String(ans.selectedOption);
            if (ans.textAnswer) this.sectionResults[sid].textAnswers[qid] = String(ans.textAnswer);

            if (ans.gradeScore !== null && ans.gradeScore !== undefined) {
              aiScoreBuckets[sid] = aiScoreBuckets[sid] || [];
              aiScoreBuckets[sid].push(Number(ans.gradeScore));
            }
            if (ans.teacherNote && String(ans.teacherNote).trim()) {
              aiNoteBuckets[sid] = aiNoteBuckets[sid] || [];
              aiNoteBuckets[sid].push(String(ans.teacherNote).trim());
            }

            // per-question AI grade for Writing/Speaking
            if (ans.gradeScore !== null && ans.gradeScore !== undefined || (ans.teacherNote && String(ans.teacherNote).trim())) {
              aiByQuestionBuckets[sid] = aiByQuestionBuckets[sid] || {};
              const scoreRaw = ans.gradeScore;
              const score = scoreRaw === null || scoreRaw === undefined ? null : Number(scoreRaw);
              const teacherNote = ans.teacherNote && String(ans.teacherNote).trim() ? String(ans.teacherNote).trim() : null;
              aiByQuestionBuckets[sid][qid] = {
                score: Number.isFinite(score as any) ? (score as number) : null,
                teacherNote,
              };
            }
          });

          // compute auto scores per section
          Object.keys(this.sectionResults).forEach((sid) => {
            const sect = this.sectionDefinitions[sid];
            let autoTotal = 0;
            let autoScore = 0;
            (sect.questions || []).forEach((q) => {
              if (q.type === 'mcq') {
                autoTotal++;
                const sel = this.sectionResults[sid].answers[String(q.id)];
                if (sel && sel === q.correctOptionId) autoScore++;
              }
            });
            this.sectionResults[sid].autoTotal = autoTotal;
            this.sectionResults[sid].autoScore = autoScore;

            const scores = aiScoreBuckets[sid] || [];
            this.sectionResults[sid].aiScore = scores.length
              ? scores.reduce((sum, val) => sum + val, 0) / scores.length
              : null;
            const notes = Array.from(new Set(aiNoteBuckets[sid] || [])).filter(Boolean);
            this.sectionResults[sid].teacherNote = notes.length ? notes.join('\n\n') : null;

            this.sectionResults[sid].aiByQuestion = aiByQuestionBuckets[sid] || {};

            this.sectionStarted[sid] = true;
            this.sectionCompleted[sid] = true;
          });

          this.quizSubmitted = !!preferred.submitTime;
          this.quizAutoScore = Object.values(this.sectionResults).reduce((s, r) => s + (r.autoScore || 0), 0);
          this.latestAutoTotal = Object.values(this.sectionResults).reduce((s, r) => s + (r.autoTotal || 0), 0);

          // If user is currently viewing a section, refresh AI result bindings
          if (this.currentSectionId && this.sectionResults[this.currentSectionId]) {
            this.aiGradeScore = this.sectionResults[this.currentSectionId].aiScore ?? null;
            this.aiTeacherNote = this.sectionResults[this.currentSectionId].teacherNote ?? null;
            this.currentAiByQuestion = this.sectionResults[this.currentSectionId].aiByQuestion ?? {};
            this.aiMarkdownByQuestion = this.renderMarkdownMap(this.currentAiByQuestion);
          }
        },
        error: (e) => {
          console.warn('Failed to fetch user submissions', e);
        },
      });
  }

  private isAiGradedSection(sectionId: string | null | undefined): boolean {
    const sid = (sectionId || '').toLowerCase();
    return sid === 'writing' || sid === 'speaking';
  }

  private computeAiGradeFromResponses(
    responses?: SubmissionAnswerResponse[] | null
  ): { score: number | null; teacherNote: string | null } {
    const items = (responses || []).filter(Boolean);
    const scores = items
      .map((r) => r?.gradeScore)
      .filter((v): v is number => v !== null && v !== undefined)
      .map((v) => Number(v))
      .filter((v) => Number.isFinite(v));
    const notes = Array.from(
      new Set(
        items
          .map((r) => (r?.teacherNote ? String(r.teacherNote).trim() : ''))
          .filter((t) => !!t)
      )
    );
    return {
      score: scores.length ? scores.reduce((s, v) => s + v, 0) / scores.length : null,
      teacherNote: notes.length ? notes.join('\n\n') : null,
    };
  }

  private computeAiByQuestionFromResponses(
    responses?: SubmissionAnswerResponse[] | null
  ): Record<string, { score: number | null; teacherNote: string | null }> {
    const map: Record<string, { score: number | null; teacherNote: string | null }> = {};
    (responses || []).filter(Boolean).forEach((r) => {
      const qid = String((r as any)?.questionId ?? '');
      if (!qid) {
        return;
      }
      const scoreRaw = (r as any)?.gradeScore;
      const score = scoreRaw === null || scoreRaw === undefined ? null : Number(scoreRaw);
      const noteRaw = (r as any)?.teacherNote;
      const teacherNote = noteRaw && String(noteRaw).trim() ? String(noteRaw).trim() : null;
      map[qid] = {
        score: Number.isFinite(score as any) ? (score as number) : null,
        teacherNote,
      };
    });
    return map;
  }

  private renderMarkdown(markdown: string | null): SafeHtml {
    try {
      const html = marked.parse(markdown || '') as string;
      return this.sanitizer.bypassSecurityTrustHtml(html);
    } catch (e) {
      console.warn('Failed to render markdown', e);
      return this.sanitizer.bypassSecurityTrustHtml('');
    }
  }

  private renderMarkdownMap(source: Record<string, { score: number | null; teacherNote: string | null }>): Record<string, SafeHtml> {
    const out: Record<string, SafeHtml> = {};
    Object.keys(source || {}).forEach((qid) => {
      out[qid] = this.renderMarkdown(source[qid]?.teacherNote ?? null);
    });
    return out;
  }

  private buildQuestionToSectionMap(): Record<string, string> {
    const qToSection: Record<string, string> = {};
    Object.keys(this.sectionDefinitions || {}).forEach((sid) => {
      (this.sectionDefinitions[sid].questions || []).forEach((q) => {
        qToSection[String(q.id)] = sid;
      });
    });
    return qToSection;
  }

  private refreshAiGradeForSection(sectionId: string) {
    if (!this.quizId || !this.userId || !this.submissionId) {
      return;
    }
    const qToSection = this.buildQuestionToSectionMap();
    this.quizSubmission
      .getUserSubmissions(this.quizId, this.userId)
      .pipe(take(1))
      .subscribe({
        next: (subs: QuizSubmissionResponse[]) => {
          const submission = (subs || []).find((s) => s.id === this.submissionId);
          if (!submission) {
            return;
          }
          const answers = (submission.answers || []).filter(
            (a) => (qToSection[String(a.questionId)] || 'general') === sectionId
          );
          const aiByQuestion = this.computeAiByQuestionFromResponses(answers as any);
          const scores = answers
            .map((a) => a.gradeScore)
            .filter((v): v is number => v !== null && v !== undefined)
            .map((v) => Number(v))
            .filter((v) => Number.isFinite(v));
          const notes = Array.from(
            new Set(
              answers
                .map((a) => (a.teacherNote ? String(a.teacherNote).trim() : ''))
                .filter((t) => !!t)
            )
          );
          const aiScore = scores.length ? scores.reduce((s, v) => s + v, 0) / scores.length : null;
          const teacherNote = notes.length ? notes.join('\n\n') : null;

          if (!this.sectionResults[sectionId]) {
            this.sectionResults[sectionId] = {
              autoScore: 0,
              autoTotal: 0,
              answers: {},
              textAnswers: {},
              aiScore,
              teacherNote,
              aiByQuestion,
              timestamp: submission.submitTime || submission.startTime || new Date().toISOString(),
            };
          } else {
            this.sectionResults[sectionId].aiScore = aiScore;
            this.sectionResults[sectionId].teacherNote = teacherNote;
            this.sectionResults[sectionId].aiByQuestion = aiByQuestion;
          }

          if (this.currentSectionId === sectionId) {
            this.aiGradeScore = aiScore;
            this.aiTeacherNote = teacherNote;
            this.currentAiByQuestion = aiByQuestion;
            this.aiMarkdownByQuestion = this.renderMarkdownMap(this.currentAiByQuestion);
          }
        },
        error: (e) => console.warn('Failed to refresh AI grade for section', sectionId, e),
      });
  }

  private buildSectionMap(quiz: Quiz): Record<string, QuizSectionView> {
    const map: Record<string, QuizSectionView> = {};
    (quiz.questions || []).forEach((question, index) => {
      const skillKey = this.normalizeSkill(question.skill);
      if (!map[skillKey]) {
        map[skillKey] = {
          id: skillKey,
          title: this.sectionMeta[skillKey]?.title || this.toTitleCase(skillKey),
          questions: [],
        };
      }
      map[skillKey].questions.push(this.mapQuestion(question, index));
    });
    this.mandatorySectionKeys.forEach((key) => {
      if (!map[key]) {
        map[key] = {
          id: key,
          title: this.sectionMeta[key]?.title || this.toTitleCase(key),
          questions: [],
        };
      }
    });
    if (!Object.keys(map).length) {
      const generalMeta = this.sectionMeta['general'];
      map['general'] = {
        id: 'general',
        title: generalMeta?.title || this.toTitleCase('general'),
        questions: [],
      };
    }
    return map;
  }

  private mapQuestion(question: AdminQuestion, index: number): QuizQuestionView {
    const options = (question.options || []).map((option, idx) => this.parseOption(option, idx));
    const correctOptionId = this.extractOptionLabel(question.correctOption);
    return {
      id: String(question.id ?? `Q${index + 1}`),
      prompt: question.content,
      type: options.length ? 'mcq' : 'text',
      options: options.length ? options : undefined,
      audioUrl: question.audioUrl,
      correctOptionId,
      skill: question.skill,
      explanation: question.explanation,
    };
  }

  private parseOption(option?: string, fallbackIndex = 0): QuizOptionView {
    const fallbackLabel = this.optionLetters[fallbackIndex] ?? `Option${fallbackIndex + 1}`;
    if (!option) {
      return { id: fallbackLabel, text: '' };
    }
    const cleaned = option.trim().replace(/^["']|["']$/g, '');
    const labelMatch = cleaned.match(/^([A-Z])\s*["']?\s*(?::|=)/i);
    const label = labelMatch ? labelMatch[1].toUpperCase() : fallbackLabel;
    const textMatch = cleaned.match(/^[A-Z]\s*["']?\s*(?::|=)\s*["']?(.*)$/i);
    const text = textMatch && textMatch[1]
      ? textMatch[1].replace(/^["']|["']$/g, '').trim()
      : cleaned.replace(/^([A-Z])\.?\s*/, '').trim();
    return { id: label, text };
  }

  private extractOptionLabel(value?: string | null): string | undefined {
    if (!value) {
      return undefined;
    }
    const cleaned = value.trim().replace(/^["']|["']$/g, '');
    const match = cleaned.match(/^([A-Z])/i);
    return match ? match[1].toUpperCase() : undefined;
  }

  private normalizeAnswers(source: Record<string, any>): { [key: string]: string } {
    return Object.keys(source || {}).reduce((acc, key) => {
      const value = source[key];
      if (value === null || value === undefined) {
        return acc;
      }
      acc[key] = String(value);
      return acc;
    }, {} as { [key: string]: string });
  }

  private normalizeSkill(skill?: string | null): string {
    return (skill || 'general').toLowerCase();
  }

  private ensureSectionMeta(key: string) {
    if (!this.sectionMeta[key]) {
      this.sectionMeta[key] = {
        title: this.toTitleCase(key),
        minutes: 45,
        desc: 'Thời gian gợi ý',
      };
    }
  }

  private computeSectionTotal(sections: QuizSectionView[]): number {
    if (!sections?.length) {
      return 0;
    }
    return sections.reduce((sum, section) => {
      const count = section.questions?.filter((q) => q.type === 'mcq').length ?? 0;
      return sum + count;
    }, 0);
  }

  private toTitleCase(value: string): string {
    return value
      .split(/[-_\s]/)
      .filter(Boolean)
      .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
      .join(' ');
  }
}
