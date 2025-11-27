import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { ButtonComponent } from '../shared/button/button.component';
import { AdminService, Lesson as CourseLesson, Quiz, Section } from '../../services/admin.service';
import { forkJoin, of } from 'rxjs';
import { catchError, finalize, map, switchMap, take } from 'rxjs/operators';

interface QuizListRow {
  quizId: number;
  title: string;
  lessonTitle: string;
  sectionTitle?: string;
  duration?: number;
  questionCount: number;
  sectionOrder: number;
  lessonOrder: number;
}

interface LessonSummary {
  id: number;
  title: string;
  sectionTitle?: string;
  sectionOrder: number;
  lessonOrder: number;
}

@Component({
  selector: 'app-quiz-list',
  standalone: true,
  imports: [CommonModule, ButtonComponent],
  templateUrl: './quiz-list.component.html',
  styleUrls: ['./quiz-list.component.css']
})
export class QuizListComponent implements OnInit {
  courseId: number | null = null;
  courseName = '';
  quizzes: QuizListRow[] = [];
  loading = false;
  error: string | null = null;

  constructor(
    private route: ActivatedRoute,
    public router: Router,
    private admin: AdminService
  ) {}

  ngOnInit(): void {
    const idStr = this.route.snapshot.paramMap.get('id');
    const id = idStr ? parseInt(idStr, 10) : null;
    this.courseId = id;
    if (!id) {
      this.router.navigate(['/my-courses']);
      return;
    }
    this.loadCourseInfo(id);
    this.loadQuizzes(id);
  }

  goBack() {
    if (this.courseId) {
      // navigate back to course player (learn) for the current course
      this.router.navigate(['/learn', String(this.courseId)]);
    } else {
      this.router.navigate(['/my-courses']);
    }
  }

  openQuiz(row: QuizListRow) {
    if (!this.courseId) return;
    this.router.navigate(['/course', String(this.courseId), 'quizzes', String(row.quizId)]);
  }

  trackByQuiz(_index: number, item: QuizListRow) {
    return item.quizId;
  }

  private loadQuizzes(courseId: number) {
    this.loading = true;
    this.error = null;
    this.admin
      .getSectionsByCourse(courseId)
      .pipe(
        map((sections) => this.flattenLessons(sections)),
        switchMap((lessons) => {
          if (!lessons.length) {
            return of<QuizListRow[]>([]);
          }
          const requests = lessons.map((lesson) =>
            this.admin.getQuizzesByLesson(lesson.id).pipe(
              map((quizzes) => quizzes.map((quiz) => this.mapQuizRow(quiz, lesson))),
              catchError((error) => {
                console.error('Failed to load quizzes for lesson', lesson.id, error);
                return of<QuizListRow[]>([]);
              })
            )
          );
          return forkJoin(requests).pipe(map((result) => result.flat()));
        }),
        finalize(() => (this.loading = false))
      )
      .subscribe({
        next: (rows) => {
          this.quizzes = rows.sort((a, b) => {
            if (a.sectionOrder !== b.sectionOrder) {
              return a.sectionOrder - b.sectionOrder;
            }
            return a.lessonOrder - b.lessonOrder;
          });
        },
        error: (error) => {
          console.error('Failed to load quizzes list', error);
          this.error = 'Không thể tải danh sách bài kiểm tra.';
          this.quizzes = [];
        },
      });
  }

  private flattenLessons(sections: Section[]): LessonSummary[] {
    const sortedSections = [...(sections || [])].sort((a, b) => this.compareOrder(a.orderIndex, b.orderIndex));
    const result: LessonSummary[] = [];
    sortedSections.forEach((section) => {
      const sectionOrder = this.normalizeOrder(section.orderIndex);
      const lessons = this.sortLessons(section.lessons ?? []);
      lessons.forEach((lesson) => {
        result.push({
          id: lesson.id,
          title: lesson.title,
          sectionTitle: section.title,
          sectionOrder,
          lessonOrder: this.normalizeOrder(lesson.orderIndex),
        });
      });
    });
    return result;
  }

  private sortLessons(lessons: CourseLesson[]): CourseLesson[] {
    return [...lessons].sort((a, b) => this.compareOrder(a.orderIndex, b.orderIndex));
  }

  private compareOrder(a?: number, b?: number): number {
    return this.normalizeOrder(a) - this.normalizeOrder(b);
  }

  private normalizeOrder(value?: number): number {
    return value ?? Number.MAX_SAFE_INTEGER;
  }

  private mapQuizRow(quiz: Quiz, lesson: LessonSummary): QuizListRow {
    return {
      quizId: quiz.id,
      title: quiz.title,
      duration: quiz.duration ?? undefined,
      questionCount: quiz.questions?.length ?? 0,
      lessonTitle: lesson.title,
      sectionTitle: lesson.sectionTitle,
      sectionOrder: lesson.sectionOrder,
      lessonOrder: lesson.lessonOrder,
    };
  }

  private loadCourseInfo(courseId: number) {
    this.admin
      .getCourseDetail(courseId)
      .pipe(take(1))
      .subscribe({
        next: (course) => {
          this.courseName = course?.title || this.courseName;
        },
        error: (error) => {
          console.warn('Unable to load course info for quiz list', error);
        },
      });
  }
}
