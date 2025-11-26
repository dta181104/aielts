import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize, take } from 'rxjs';
import {
  AdminService,
  Course,
  Lesson,
  Question,
  QuestionRequestPayload,
  Quiz,
  QuizRequestPayload,
  QuizSkill,
  Section,
} from '../../services/admin.service';
import { AdminNavComponent } from './admin-nav.component';

@Component({
  selector: 'app-admin-course-quizzes',
  standalone: true,
  imports: [CommonModule, FormsModule, AdminNavComponent],
  templateUrl: './admin-course-quizzes.component.html',
  styleUrls: ['./admin-course-quizzes.component.css']
})
export class AdminCoursequizzesComponent implements OnInit {
  courseId: number | null = null;
  course: Course | null = null;
  sections: Section[] = [];
  lessons: Lesson[] = [];
  quizzes: Quiz[] = [];
  selectedSectionId?: number;
  selectedLessonId?: number;
  selectedQuiz: Quiz | null = null;
  editingQuizId?: number;
  editingQuestionId?: number;

  loadingCourse = false;
  loadingQuizzes = false;
  loadingQuizDetail = false;
  savingQuiz = false;
  savingQuestion = false;
  errorMessage?: string;

  quizForm: Partial<QuizRequestPayload> = {
    title: '',
    duration: null,
    passScore: null,
    shuffleQuestions: false,
  };

  readonly questionSkills: QuizSkill[] = ['LISTENING', 'READING', 'WRITING', 'SPEAKING'];
  questionForm: Partial<QuestionRequestPayload> = this.createDefaultQuestionForm();

  constructor(private route: ActivatedRoute, private admin: AdminService, private router: Router) {}

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    this.courseId = idParam ? Number(idParam) : null;
    if (!this.courseId) {
      this.errorMessage = 'Không tìm thấy mã khóa học hợp lệ.';
      return;
    }

    this.loadCourse(this.courseId);
  }

  back() {
    this.router.navigate(['/admin/courses']);
  }

  onSectionChange(sectionId: string) {
    this.selectedSectionId = Number(sectionId);
    const section = this.sections.find((s) => s.id === this.selectedSectionId);
    this.lessons = section?.lessons ?? [];
    this.selectedLessonId = this.lessons[0]?.id;
    this.handleLessonChange();
  }

  onLessonChange(lessonId: string) {
    this.selectedLessonId = Number(lessonId);
    this.handleLessonChange();
  }

  startCreateQuiz() {
    this.editingQuizId = undefined;
    this.quizForm = {
      title: '',
      duration: null,
      passScore: null,
      shuffleQuestions: false,
    };
  }

  selectQuiz(quiz: Quiz, prefillForm = true) {
    if (prefillForm) {
      this.editingQuizId = quiz.id;
      this.quizForm = {
        title: quiz.title,
        duration: quiz.duration ?? null,
        passScore: quiz.passScore ?? null,
        shuffleQuestions: quiz.shuffleQuestions ?? false,
      };
    } else {
      this.editingQuizId = undefined;
    }
    this.resetQuestionForm();
    this.loadQuizDetail(quiz.id);
  }

  saveQuiz() {
    if (!this.selectedLessonId) {
      alert('Vui lòng chọn bài học để tạo quiz.');
      return;
    }
    if (!this.quizForm.title?.trim()) {
      alert('Nhập tiêu đề quiz.');
      return;
    }

    const payload: QuizRequestPayload = {
      title: this.quizForm.title.trim(),
      lessonId: this.selectedLessonId,
      duration: this.quizForm.duration ?? null,
      passScore: this.quizForm.passScore ?? null,
      shuffleQuestions: this.quizForm.shuffleQuestions ?? false,
    };

    const isUpdate = Boolean(this.editingQuizId);
    const request$ = isUpdate
      ? this.admin.updateQuiz(this.editingQuizId!, payload)
      : this.admin.createQuiz(payload);

    this.savingQuiz = true;
    request$
      .pipe(
        take(1),
        finalize(() => (this.savingQuiz = false))
      )
      .subscribe({
        next: (quiz) => {
          this.loadQuizzes(this.selectedLessonId!, quiz.id, isUpdate);
          if (!isUpdate) {
            this.startCreateQuiz();
          }
        },
        error: (error) => {
          console.error('Failed to save quiz', error);
          alert('Không thể lưu quiz.');
        },
      });
  }

  deleteQuiz(quiz: Quiz) {
    if (!confirm(`Xoá quiz "${quiz.title}"?`)) {
      return;
    }
    this.admin
      .deleteQuiz(quiz.id)
      .pipe(take(1))
      .subscribe({
        next: () => {
          if (this.selectedQuiz?.id === quiz.id) {
            this.selectedQuiz = null;
            this.resetQuestionForm();
          }
          if (this.selectedLessonId) {
            this.loadQuizzes(this.selectedLessonId);
          }
        },
        error: (error) => {
          console.error('Failed to delete quiz', error);
          alert('Không thể xoá quiz.');
        },
      });
  }

  addQuestionOption() {
    const options = this.questionForm.options ?? [];
    this.questionForm.options = [...options, ''];
  }

  removeQuestionOption(index: number) {
    if (!this.questionForm.options) {
      return;
    }
    const updated = this.questionForm.options.filter((_, i) => i !== index);
    this.questionForm.options = updated.length ? updated : [''];
  }

  editQuestion(question: Question) {
    this.editingQuestionId = question.id;
    this.questionForm = {
      content: question.content,
      audioUrl: question.audioUrl,
      options: question.options && question.options.length
        ? question.options.map((option) => this.optionText(option))
        : [''],
      correctOption: question.correctOption,
      explanation: question.explanation,
      skill: question.skill ?? 'READING',
    };
  }

  resetQuestionForm() {
    this.editingQuestionId = undefined;
    this.questionForm = this.createDefaultQuestionForm();
  }

  saveQuestion() {
    if (!this.selectedQuiz) {
      alert('Vui lòng chọn quiz.');
      return;
    }
    if (!this.questionForm.content?.trim()) {
      alert('Nhập nội dung câu hỏi.');
      return;
    }

    const normalizedOptions = (this.questionForm.options ?? [])
      .map((option) => option.trim())
      .filter((option) => option.length);

    const formattedOptions = normalizedOptions.length ? this.formatOptionsWithLabels(normalizedOptions) : undefined;

    const payload: QuestionRequestPayload = {
      content: this.questionForm.content.trim(),
      audioUrl: this.questionForm.audioUrl?.trim() || undefined,
      options: formattedOptions,
      correctOption: this.questionForm.correctOption?.trim() || undefined,
      explanation: this.questionForm.explanation?.trim() || undefined,
      skill: this.questionForm.skill || 'READING',
    };

    const isUpdate = Boolean(this.editingQuestionId);
    const request$ = isUpdate
      ? this.admin.updateQuizQuestion(this.editingQuestionId!, payload)
      : this.admin.createQuizQuestion(this.selectedQuiz.id, payload);

    this.savingQuestion = true;
    request$
      .pipe(
        take(1),
        finalize(() => (this.savingQuestion = false))
      )
      .subscribe({
        next: () => {
          this.resetQuestionForm();
          this.loadQuizDetail(this.selectedQuiz!.id);
        },
        error: (error) => {
          console.error('Failed to save question', error);
          alert('Không thể lưu câu hỏi.');
        },
      });
  }

  deleteQuestion(question: Question) {
    if (!this.selectedQuiz) {
      return;
    }
    if (!confirm('Xoá câu hỏi này?')) {
      return;
    }
    this.admin
      .deleteQuizQuestion(question.id)
      .pipe(take(1))
      .subscribe({
        next: () => {
          this.resetQuestionForm();
          this.loadQuizDetail(this.selectedQuiz?.id || 0);
        },
        error: (error) => {
          console.error('Failed to delete question', error);
          alert('Không thể xoá câu hỏi.');
        },
      });
  }

  private loadCourse(id: number) {
    this.loadingCourse = true;
    this.admin
      .getCourseDetail(id)
      .pipe(
        take(1),
        finalize(() => (this.loadingCourse = false))
      )
      .subscribe({
        next: (course) => {
          this.course = course;
          this.sections = course.sections ?? [];
          this.initializeSelection();
          this.errorMessage = undefined;
        },
        error: (error: unknown) => {
          console.error('Failed to fetch course detail', error);
          this.course = this.admin.getCachedCourseById(id) || null;
          if (this.course) {
            this.sections = this.course.sections ?? [];
            this.initializeSelection();
          } else {
            this.errorMessage = 'Không thể tải thông tin khóa học.';
          }
        },
      });
  }

  private initializeSelection() {
    if (!this.sections.length) {
      this.lessons = [];
      this.selectedSectionId = undefined;
      this.selectedLessonId = undefined;
      this.quizzes = [];
      this.selectedQuiz = null;
      return;
    }

    const section = this.sections.find((s) => s.id === this.selectedSectionId) ?? this.sections[0];
    this.selectedSectionId = section.id;
    this.lessons = section.lessons ?? [];

    if (!this.lessons.length) {
      this.selectedLessonId = undefined;
      this.quizzes = [];
      this.selectedQuiz = null;
      return;
    }

    const lesson = this.lessons.find((l) => l.id === this.selectedLessonId) ?? this.lessons[0];
    this.selectedLessonId = lesson.id;
    this.loadQuizzes(this.selectedLessonId);
  }

  private handleLessonChange() {
    if (this.selectedLessonId) {
      this.loadQuizzes(this.selectedLessonId);
    } else {
      this.quizzes = [];
      this.selectedQuiz = null;
    }
    this.startCreateQuiz();
    this.resetQuestionForm();
  }

  private loadQuizzes(lessonId: number, retainQuizId?: number, autoEdit = false) {
    if (!lessonId) {
      return;
    }
    const preferredQuizId = retainQuizId ?? this.selectedQuiz?.id;
    this.loadingQuizzes = true;
    this.admin
      .getQuizzesByLesson(lessonId)
      .pipe(
        take(1),
        finalize(() => (this.loadingQuizzes = false))
      )
      .subscribe({
        next: (quizzes) => {
          this.quizzes = quizzes;
          if (!autoEdit) {
            this.startCreateQuiz();
          }
          this.editingQuizId = undefined;

          if (!quizzes.length) {
            this.resetQuestionForm();
            return;
          }

          const quizToSelect = quizzes.find((item) => item.id === preferredQuizId) ?? quizzes[0];
          this.selectQuiz(quizToSelect, autoEdit);
        },
        error: (error) => {
          console.error('Failed to load quizzes', error);
          this.quizzes = [];
        },
      });
  }

  private loadQuizDetail(quizId: number) {
    if (!quizId) {
      return;
    }
    this.loadingQuizDetail = true;
    this.admin
      .getQuizDetail(quizId)
      .pipe(
        take(1),
        finalize(() => (this.loadingQuizDetail = false))
      )
      .subscribe({
        next: (quiz) => {
          this.selectedQuiz = quiz;
        },
        error: (error) => {
          console.error('Failed to load quiz detail', error);
          alert('Không thể tải chi tiết quiz.');
        },
      });
  }

  private createDefaultQuestionForm(): Partial<QuestionRequestPayload> {
    return {
      content: '',
      audioUrl: '',
      options: ['', '', '', ''],
      correctOption: '',
      explanation: '',
      skill: 'READING',
    };
  }
  private readonly optionLetters = ['A', 'B', 'C', 'D', 'E', 'F'];

  optionLabel(option: string | undefined, index: number): string {
    if (!option) {
      return this.optionLetters[index] ?? `Lựa chọn ${index + 1}`;
    }
    const cleaned = option.trim().replace(/^"|"$/g, '');
    const match = cleaned.match(/^([A-Z])\s*["']?\s*(?::|=)/i);
    if (match) {
      return match[1].toUpperCase();
    }
    return this.optionLetters[index] ?? `Lựa chọn ${index + 1}`;
  }

  optionText(option: string | undefined, fallbackIndex?: number): string {
    if (!option) {
      return '';
    }
    const cleaned = option.trim().replace(/^"|"$/g, '');
    const match = cleaned.match(/^[A-Z]\s*["']?\s*(?::|=)\s*["']?(.*)$/i);
    if (match && match[1]) {
      return match[1].replace(/^"|"$/g, '').trim();
    }
    if (fallbackIndex !== undefined) {
      return cleaned.replace(/^([A-Z])\.?\s*/, '').trim();
    }
    return cleaned;
  }

  trackOption(index: number): number {
    return index;
  }

  private formatOptionsWithLabels(options: string[]): string[] {
    return options.map((option, index) => {
      const cleaned = option.replace(/^"|"$/g, '').trim();
      if (this.isOptionLabeled(cleaned)) {
        return cleaned;
      }
      const label = this.optionLetters[index] ?? `Lựa chọn ${index + 1}`;
      return `${label}": "${cleaned}`;
    });
  }

  private isOptionLabeled(value: string): boolean {
    return /^[A-Z]\s*["']?\s*(?::|=)/i.test(value);
  }
}
