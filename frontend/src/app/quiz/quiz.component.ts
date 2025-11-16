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

  onCancel() { this.cancel.emit(); }
  onSubmit() { this.submit.emit(); }
}
