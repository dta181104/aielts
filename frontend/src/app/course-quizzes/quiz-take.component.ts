import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { QuizComponent } from '../quiz/quiz.component';
import { ButtonComponent } from '../shared/button/button.component';
import { AdminService, Quiz, Question as AdminQuestion } from '../../services/admin.service';
import { QuizSubmissionService, SubmissionAnswerPayload } from '../../services/quiz-submission.service';
import { of } from 'rxjs';
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
  private quizStorageKey = 'course_quiz_results';
  submissionId: number | null = null;
  userId?: number;
  savingSection = false;
  private submissionStorageKey = 'quiz_submission_sessions';
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

  get quizAutoTotal(): number {
    if (this.latestAutoTotal !== null) {
      return this.latestAutoTotal;
    }
    return this.computeSectionTotal(this.quizSections);
  }

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private admin: AdminService,
    private quizSubmission: QuizSubmissionService
  ) {}

  ngOnInit(): void {
    const idStr = this.route.snapshot.paramMap.get('id');
    const qId = this.route.snapshot.paramMap.get('quizId');
    const id = idStr ? parseInt(idStr, 10) : null;
    this.courseId = id;
    const quizNumeric = qId ? parseInt(qId, 10) : null;
    this.quizId = Number.isFinite(quizNumeric) ? quizNumeric : null;
    this.userId = this.resolveUserId();
    if (this.quizId && this.userId) {
      const storedSubmissionId = this.readStoredSubmissionId();
      if (storedSubmissionId) {
        this.submissionId = storedSubmissionId;
        this.submissionEnsured = true;
      }
    }
    if (!id || !this.quizId) {
      this.router.navigate(['/my-courses']);
      return;
    }
    this.loadCourseTitle(id);
    this.loadQuizDetail(this.quizId);
  }

  ngOnDestroy(): void {
    this.clearActiveTimer();
  }

  getSavedQuizResult(quizId?: string | number) {
    try {
      const raw = localStorage.getItem(this.quizStorageKey) || '{}';
      const map = JSON.parse(raw);
      const cid = String(this.courseId);
      if (!map[cid]) return null;
      if (!quizId) return null;
      return map[cid][String(quizId)] || null;
    } catch(e) { return null; }
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
    const saved = this.getSavedQuizResult(this.selectedQuiz?.id)?.sections || {};
    const sr = saved?.[id] || null;
    this.answers = this.normalizeAnswers(sr?.answers || {});
    this.textAnswers = sr?.textAnswers || {};
    this.quizSubmitted = !!sr;
    this.quizAutoScore = sr?.autoScore ?? null;
    this.latestAutoTotal = sr?.autoTotal ?? this.computeSectionTotal(this.quizSections);
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
    const finalizeSection = () => {
      this.applySectionResult(index, section);
    };
    this.persistSectionAnswers(section)
      .pipe(
        take(1),
        finalize(() => (this.savingSection = false))
      )
      .subscribe({
        next: () => finalizeSection(),
        error: (error: unknown) => {
          console.warn('Failed to sync answers for section', section.id, error);
          finalizeSection();
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

  saveQuizResult(quizId: string | number | undefined, payload?: { autoScore?: number; autoTotal?: number }) {
    if (!quizId) return;
    try {
      const raw = localStorage.getItem(this.quizStorageKey) || '{}';
      const map = JSON.parse(raw);
      const cid = String(this.courseId);
      if (!map[cid]) map[cid] = {};
      const key = String(quizId);
      map[cid][key] = {
        autoScore: payload?.autoScore ?? this.quizAutoScore,
        autoTotal: payload?.autoTotal ?? this.computeSectionTotal(this.quizSections),
        answers: { ...this.answers },
        textAnswers: { ...this.textAnswers },
        sections: this.sectionResults,
        timestamp: new Date().toISOString()
      };
      localStorage.setItem(this.quizStorageKey, JSON.stringify(map));
    } catch (e) { console.warn('Failed to save quiz result', e); }
  }

  private ensureSubmissionSession() {
    if (!this.quizId || !this.userId) {
      return;
    }
    if (!this.submissionId) {
      const stored = this.readStoredSubmissionId();
      if (stored) {
        this.submissionId = stored;
        this.submissionEnsured = true;
        return;
      }
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
          if (this.submissionId) {
            this.storeSubmissionId(this.submissionId);
          }
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
    const payloads = this.buildSubmissionPayloads(section);
    if (!payloads.length) {
      return of([]);
    }
    return this.quizSubmission.saveAnswersBulk(this.submissionId, payloads);
  }

  private buildSubmissionPayloads(section: QuizSectionView): SubmissionAnswerPayload[] {
    const payloads: SubmissionAnswerPayload[] = [];
    section.questions.forEach((question) => {
      const numericId = Number(question.id);
      if (!Number.isFinite(numericId)) {
        return;
      }
      const key = String(question.id);
      if (question.type === 'mcq') {
        const selected = this.answers[key];
        if (selected) {
          payloads.push({ questionId: numericId, selectedOption: selected });
        }
      } else if (question.type === 'text') {
        const response = this.textAnswers[key];
        if (response && response.trim()) {
          payloads.push({ questionId: numericId, textAnswer: response.trim() });
        }
      }
    });
    return payloads;
  }

  private applySectionResult(index: number, section: QuizSectionView) {
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
      timestamp: new Date().toISOString(),
    };
    this.sectionCompleted[id] = true;
    this.clearActiveTimer();
    this.quizSubmitted = true;
    this.quizAutoScore = autoScore;
    this.latestAutoTotal = autoTotal;
    this.saveQuizResult(this.selectedQuiz?.id, { autoScore, autoTotal });
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

  private readStoredSubmissionId(): number | null {
    if (typeof window === 'undefined' || !this.quizId || !this.userId) {
      return null;
    }
    try {
      const raw = localStorage.getItem(this.submissionStorageKey);
      if (!raw) {
        return null;
      }
      const map = JSON.parse(raw);
      const quizEntry = map?.[String(this.quizId)];
      if (!quizEntry) {
        return null;
      }
      const stored = quizEntry[String(this.userId)];
      const parsed = Number(stored);
      return Number.isFinite(parsed) ? parsed : null;
    } catch (e) {
      console.warn('Failed to restore submission id', e);
      return null;
    }
  }

  private storeSubmissionId(submissionId: number) {
    if (typeof window === 'undefined' || !this.quizId || !this.userId) {
      return;
    }
    try {
      const raw = localStorage.getItem(this.submissionStorageKey) || '{}';
      const map = JSON.parse(raw);
      if (!map[String(this.quizId)]) {
        map[String(this.quizId)] = {};
      }
      map[String(this.quizId)][String(this.userId)] = submissionId;
      localStorage.setItem(this.submissionStorageKey, JSON.stringify(map));
    } catch (e) {
      console.warn('Failed to persist submission id', e);
    }
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
          this.restoreProgress();
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

  private restoreProgress() {
    const saved = this.getSavedQuizResult(this.selectedQuiz?.id);
    if (!saved) {
      this.quizSubmitted = false;
      this.quizAutoScore = null;
      this.answers = {};
      this.textAnswers = {};
      this.latestAutoTotal = null;
      return;
    }
    this.quizSubmitted = !!saved.autoScore;
    this.quizAutoScore = saved.autoScore ?? null;
    this.answers = this.normalizeAnswers(saved.answers || {});
    this.textAnswers = saved.textAnswers || {};
    this.sectionResults = this.normalizeStoredSections(saved.sections || {});
    this.latestAutoTotal = typeof saved.autoTotal === 'number' ? saved.autoTotal : Number(saved.autoTotal) || 0;
    Object.keys(this.sectionResults).forEach((sid) => {
      if (this.sectionRemaining[sid] === undefined) {
        this.ensureSectionMeta(sid);
        this.sectionRemaining[sid] = (this.sectionMeta[sid]?.minutes ?? 0) * 60;
      }
      this.sectionStarted[sid] = true;
      this.sectionCompleted[sid] = true;
    });
  }

  private normalizeStoredSections(sections: Record<string, any>): Record<string, SectionResult> {
    return Object.keys(sections || {}).reduce((acc, key) => {
      const entry = sections[key] || {};
      acc[key] = {
        autoScore: Number(entry.autoScore) || 0,
        autoTotal: Number(entry.autoTotal) || 0,
        answers: this.normalizeAnswers(entry.answers || {}),
        textAnswers: entry.textAnswers || {},
        timestamp: entry.timestamp || new Date().toISOString(),
      };
      return acc;
    }, {} as Record<string, SectionResult>);
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
