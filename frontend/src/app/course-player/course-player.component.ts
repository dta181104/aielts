import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { EnrollmentService } from '../../services/enrollment.service';
import { ProductItems } from '../types/productItem';
import { ButtonComponent } from '../shared/button/button.component';
import { AdminService, Course as AdminCourse, Lesson as CourseLesson, Quiz, Section } from '../../services/admin.service';
import { forkJoin } from 'rxjs';
import { finalize, take } from 'rxjs/operators';

type LessonView = CourseLesson & { sectionTitle?: string };
type SectionView = Section & { lessons: LessonView[] };

@Component({
  selector: 'app-course-player',
  standalone: true,
  imports: [CommonModule, ButtonComponent],
  templateUrl: './course-player.component.html',
  styleUrls: ['./course-player.component.css'],
})
export class CoursePlayerComponent implements OnInit {
  courseId: number | null = null;
  course: ProductItems | null = null;
  courseDetail: AdminCourse | null = null;
  sections: SectionView[] = [];
  lessons: LessonView[] = [];
  selectedLesson: LessonView | null = null;
  structureLoading = false;
  structureError: string | null = null;
  quizzes: Quiz[] = [];
  quizLoading = false;
  quizError: string | null = null;
  private lessonQuizCache: Record<number, Quiz[]> = {};

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private enrollment: EnrollmentService,
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

    const enrolled = this.enrollment.getAll();
    this.course = enrolled.find((c) => c.id === id) || null;
    if (!this.course) {
      this.router.navigate(['/course', id]);
      return;
    }

    this.loadCourseStructure(id);
  }

  get progressPercent(): number {
    const total = this.lessons.length || 1;
    const current = this.selectedLesson ? this.lessons.findIndex((l) => l.id === this.selectedLesson!.id) + 1 : 0;
    return Math.round((current / total) * 100);
  }

  get totalLessonCount(): number {
    return this.lessons.length;
  }

  formatLessonDuration(lesson?: CourseLesson | LessonView): string {
    if (!lesson || lesson.duration === undefined || lesson.duration === null) {
      return 'Đang cập nhật';
    }
    return `${lesson.duration} phút`;
  }

  selectLesson(lesson: LessonView) {
    if (this.selectedLesson?.id === lesson.id) {
      return;
    }
    this.selectedLesson = lesson;
    this.quizzes = [];
    this.quizError = null;
    this.loadQuizzesForLesson(lesson.id);
  }

  back() {
    this.router.navigate(['/my-courses']);
  }

  goToQuizList() {
    if (!this.courseId) {
      return;
    }
    this.router.navigate(['/course', String(this.courseId), 'quizzes']);
  }

  goToQuiz(quiz: Quiz) {
    if (!this.courseId || !quiz?.id) {
      return;
    }
    this.router.navigate(['/course', String(this.courseId), 'quizzes', String(quiz.id)]);
  }

  private loadCourseStructure(courseId: number) {
    this.structureLoading = true;
    this.structureError = null;
    forkJoin({
      detail: this.admin.getCourseDetail(courseId),
      sections: this.admin.getSectionsByCourse(courseId),
    })
      .pipe(finalize(() => (this.structureLoading = false)))
      .subscribe({
        next: ({ detail, sections }) => {
          this.courseDetail = detail;
          const resolvedSections = sections?.length ? sections : detail.sections ?? [];
          this.sections = this.buildSectionViews(resolvedSections);
          this.lessons = this.buildLessonList(this.sections);
          this.selectedLesson = this.lessons[0] || null;
          if (this.selectedLesson) {
            this.loadQuizzesForLesson(this.selectedLesson.id);
          } else {
            this.quizzes = [];
          }
        },
        error: (error) => {
          console.error('Failed to load course structure', error);
          this.structureError = 'Không thể tải nội dung khóa học.';
        },
      });
  }

  private buildSectionViews(sections: Section[]): SectionView[] {
    return [...(sections || [])]
      .map((section) => ({
        ...section,
        lessons: this.sortLessons(section.lessons ?? []).map((lesson) => ({
          ...lesson,
          sectionTitle: section.title,
        })),
      }))
      .sort((a, b) => this.compareOrder(a.orderIndex, b.orderIndex));
  }

  private buildLessonList(sections: SectionView[]): LessonView[] {
    const list: LessonView[] = [];
    sections.forEach((section) => {
      section.lessons.forEach((lesson) => list.push(lesson));
    });
    return list;
  }

  private sortLessons(lessons: CourseLesson[]): CourseLesson[] {
    return [...lessons].sort((a, b) => this.compareOrder(a.orderIndex, b.orderIndex));
  }

  private compareOrder(a?: number, b?: number): number {
    const safeA = a ?? Number.MAX_SAFE_INTEGER;
    const safeB = b ?? Number.MAX_SAFE_INTEGER;
    return safeA - safeB;
  }

  private loadQuizzesForLesson(lessonId: number) {
    if (!lessonId) {
      this.quizzes = [];
      return;
    }

    if (this.lessonQuizCache[lessonId]) {
      this.applyLessonQuizzes(lessonId, this.lessonQuizCache[lessonId]);
      return;
    }

    this.quizLoading = true;
    this.quizError = null;
    this.admin
      .getQuizzesByLesson(lessonId)
      .pipe(
        take(1),
        finalize(() => (this.quizLoading = false))
      )
      .subscribe({
        next: (quizzes) => this.applyLessonQuizzes(lessonId, quizzes),
        error: (error) => {
          console.error('Failed to load quizzes', error);
          this.quizError = 'Không thể tải bài kiểm tra.';
          this.quizzes = [];
        },
      });
  }

  private applyLessonQuizzes(lessonId: number, quizzes: Quiz[]) {
    this.lessonQuizCache[lessonId] = quizzes;
    this.quizzes = quizzes;
    this.quizError = null;
  }
}
