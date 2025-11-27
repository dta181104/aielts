import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-quiz',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './quiz.component.html',
  styleUrls: ['./quiz.component.css']
})
export class QuizComponent {
  // Generic inputs for any quiz consumer
  @Input() quizSections: any[] = [];
  @Input() answers: { [k: string]: any } = {};
  @Input() textAnswers: { [k: string]: string } = {};
  @Input() selectedQuiz: any | null = null;
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

  @Output() cancel = new EventEmitter<void>();
  @Output() submit = new EventEmitter<void>();

  private questionVisibility: Record<string, { prompt: boolean; answers: boolean }> = {};
  reviewExpanded = false;

  onCancel() { this.cancel.emit(); }
  onSubmit() { this.submit.emit(); }

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

  getUserAnswerDisplay(question: any): string {
    const key = String(question?.id ?? '');
    if (!key) {
      return 'Chưa trả lời';
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
}
