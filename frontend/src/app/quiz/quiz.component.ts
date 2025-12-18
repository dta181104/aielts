import { Component, EventEmitter, Input, OnDestroy, Output, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { Subscription } from 'rxjs';
import { AudioRecorderService } from '../../services/audio-recorder.service';
import { QuizSubmissionService } from '../../services/quiz-submission.service';

@Component({
  selector: 'app-quiz',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './quiz.component.html',
  styleUrls: ['./quiz.component.css']
})
export class QuizComponent implements OnDestroy {
  @Input() quizSections: any[] = [];
  @Input() answers: { [k: string]: any } = {};
  @Input() textAnswers: { [k: string]: string } = {};
  @Input() selectedQuiz: any | null = null;
  @Input() submissionId: number | null = null;

  private _quizSubmitted = false;
  @Input() set quizSubmitted(value: boolean) {
    this._quizSubmitted = value;
    if (value) {
      this.reviewExpanded = false;
    }
  }
  get quizSubmitted(): boolean {
    return this._quizSubmitted;
  }
  @Input() quizAutoScore: number | null = null;
  @Input() quizAutoTotal = 0;
  @Input() submitLabel = 'Nộp bài';
  @Input() allowDetailedReview = true;

  @Output() cancel = new EventEmitter<void>();
  @Output() submitted = new EventEmitter<void>();
  @Output() audioCaptured = new EventEmitter<{ questionId: string; file: File }>();

  private questionVisibility: Record<string, { prompt: boolean; answers: boolean }> = {};
  reviewExpanded = false;

  // Audio recording properties
  isRecording: Record<string, boolean> = {};
  recordedAudio: Record<string, Blob | null> = {};
  audioUrls: Record<string, SafeUrl> = {};
  private recordingSub: Subscription | null = null;
  private recordedBlobSub: Subscription | null = null;
  // Recording timer state
  recordingStartTime: Record<string, number | null> = {};
  recordingTimerHandle: Record<string, any> = {};
  recordingTimeDisplay: Record<string, string> = {};

  constructor(
    private cdr: ChangeDetectorRef,
    private audioRecorderService: AudioRecorderService,
    private sanitizer: DomSanitizer,
    private quizSubmissionService: QuizSubmissionService
  ) {
        this.recordedBlobSub = this.audioRecorderService.getRecordedBlob().subscribe(blob => {
          const activeQuestionId = Object.keys(this.isRecording).find(key => this.isRecording[key]);
          if (!activeQuestionId) { return; }

          // always keep the recorded blob and expose playback URL to the user
          this.recordedAudio[activeQuestionId] = blob;
          this.audioUrls[activeQuestionId] = this.sanitizer.bypassSecurityTrustUrl(URL.createObjectURL(blob));
          this.isRecording[activeQuestionId] = false;

          // finalize timer display and clear interval
          if (this.recordingStartTime[activeQuestionId]) {
            const diff = Math.max(0, Date.now() - this.recordingStartTime[activeQuestionId]!);
            const seconds = Math.floor(diff / 1000);
            const mm = Math.floor(seconds / 60).toString().padStart(2, '0');
            const ss = (seconds % 60).toString().padStart(2, '0');
            this.recordingTimeDisplay[activeQuestionId] = `${mm}:${ss}`;
          }
          if (this.recordingTimerHandle[activeQuestionId]) {
            clearInterval(this.recordingTimerHandle[activeQuestionId]);
            delete this.recordingTimerHandle[activeQuestionId];
          }
          this.recordingStartTime[activeQuestionId] = null;

          // create a File for the blob and emit to parent so hosting pages can keep/store it
          try {
            const audioFile = new File([blob], `q${activeQuestionId}-recording.webm`, { type: blob.type || 'audio/webm' });
            this.audioCaptured.emit({ questionId: activeQuestionId, file: audioFile });
            // if a submissionId exists, also upload immediately
            if (this.submissionId) {
              const questionIdNum = parseInt(activeQuestionId, 10);
              this.quizSubmissionService.uploadAnswerAudio(this.submissionId, questionIdNum, audioFile)
                .subscribe({
                  next: (response) => console.log('Upload successful for question', activeQuestionId, response),
                  error: (error) => console.error('Upload failed for question', activeQuestionId, error)
                });
            }
          } catch (e) {
            console.warn('Failed to create audio File from blob', e);
          }
          // ensure Angular updates the view immediately
          try { this.cdr.detectChanges(); } catch (e) { /* ignore */ }
        });
  }

  onCancel() { this.cancel.emit(); }
  onSubmit(event?: Event) {
    try { event?.stopPropagation(); } catch (e) { /* ignore */ }
    this.submitted.emit();
  }

  startRecording(questionId: string) {
    Object.keys(this.isRecording).forEach(id => {
      if (this.isRecording[id]) {
        this.stopRecording(id);
      }
    });

    this.isRecording[questionId] = true;
    this.recordedAudio[questionId] = null;
    this.audioRecorderService.startRecording();

    // start timer
    this.recordingStartTime[questionId] = Date.now();
    this.recordingTimeDisplay[questionId] = '00:00';
    if (this.recordingTimerHandle[questionId]) {
      clearInterval(this.recordingTimerHandle[questionId]);
    }
    this.recordingTimerHandle[questionId] = setInterval(() => {
      const start = this.recordingStartTime[questionId] || 0;
      const diff = Math.max(0, Date.now() - start);
      const seconds = Math.floor(diff / 1000);
      const mm = Math.floor(seconds / 60).toString().padStart(2, '0');
      const ss = (seconds % 60).toString().padStart(2, '0');
      this.recordingTimeDisplay[questionId] = `${mm}:${ss}`;
    }, 250);
  }

  stopRecording(questionId: string) {
    // stop the audio service; recorded blob will be delivered via subscription
    this.audioRecorderService.stopRecording();
    // stop timer if present (finalization occurs when blob arrives)
    if (this.recordingTimerHandle[questionId]) {
      clearInterval(this.recordingTimerHandle[questionId]);
      delete this.recordingTimerHandle[questionId];
    }
    // set intermediary final time
    if (this.recordingStartTime[questionId]) {
      const diff = Math.max(0, Date.now() - this.recordingStartTime[questionId]!);
      const seconds = Math.floor(diff / 1000);
      const mm = Math.floor(seconds / 60).toString().padStart(2, '0');
      const ss = (seconds % 60).toString().padStart(2, '0');
      this.recordingTimeDisplay[questionId] = `${mm}:${ss}`;
    }
    this.recordingStartTime[questionId] = null;
  }

  getAudioUrl(questionId: string): SafeUrl | null {
    return this.audioUrls[questionId] || null;
  }

  togglePrompt(questionId: string) {
    const entry = this.ensureQuestionVisibility(questionId);
    entry.prompt = !entry.prompt;
  }

  toggleAnswers(questionId: string) {
    const entry = this.ensureQuestionVisibility(questionId);
    entry.answers = !entry.answers;
  }

  toggleReviewVisibility() {
    this.reviewExpanded = !this.reviewExpanded;
  }

  shouldShowPrompt(question: any): boolean {
    if (!this.isListeningQuestion(question)) {
      return true;
    }
    return this.ensureQuestionVisibility(question?.id).prompt;
  }

  shouldShowAnswers(question: any): boolean {
    if (!this.isListeningQuestion(question)) {
      return true;
    }
    return this.ensureQuestionVisibility(question?.id).answers;
  }

  isListeningQuestion(question: any): boolean {
    return (question?.skill || '').toLowerCase() === 'listening';
  }

  isSpeakingQuestion(question: any): boolean {
    return (question?.skill || '').toLowerCase() === 'speaking';
  }

  getUserAnswerDisplay(question: any): string {
    const key = String(question?.id ?? '');
    if (!key) {
      return 'Chưa trả lời';
    }
    if (this.isSpeakingQuestion(question)) {
      return this.recordedAudio[key] ? 'Đã ghi âm câu trả lời' : 'Chưa trả lời';
    }
    if (question?.type === 'mcq') {
      const selected = this.answers[key];
      if (!selected) {
        return 'Chưa trả lời';
      }
      return this.getOptionDisplay(question, selected) || selected;
    }
    const response = this.textAnswers[key];
    return response && response.trim() ? response.trim() : 'Chưa trả lời';
  }

  getOptionDisplay(question: any, optionId?: string | null): string {
    if (!question?.options || !optionId) {
      return optionId || '';
    }
    const option = question.options.find((o: any) => String(o.id) === String(optionId));
    if (!option) {
      return optionId;
    }
    return `${option.id}. ${option.text ?? ''}`.trim();
  }

  private ensureQuestionVisibility(questionId: string) {
    const key = String(questionId);
    if (!this.questionVisibility[key]) {
      this.questionVisibility[key] = { prompt: false, answers: false };
    }
    return this.questionVisibility[key];
  }

  ngOnDestroy(): void {
    this.recordingSub?.unsubscribe();
    this.recordedBlobSub?.unsubscribe();
    this.audioRecorderService.stopRecording();
  }
}
