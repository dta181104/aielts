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
  @Input() quizSubmitted = false;
  @Input() quizAutoScore: number | null = null;
  @Input() quizAutoTotal = 0;
  @Input() submitLabel = 'Nộp bài';

  @Output() cancel = new EventEmitter<void>();
  @Output() submit = new EventEmitter<void>();

  private questionVisibility: Record<string, { prompt: boolean; answers: boolean }> = {};

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

  private ensureQuestionVisibility(questionId: string) {
    const key = String(questionId);
    if (!this.questionVisibility[key]) {
      this.questionVisibility[key] = { prompt: false, answers: false };
    }
    return this.questionVisibility[key];
  }
}
