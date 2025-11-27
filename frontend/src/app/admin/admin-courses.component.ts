import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize, take } from 'rxjs';
import {
  AdminService,
  Course,
  CourseListParams,
  CourseRequestPayload,
  CourseStatus,
  CourseType,
  Lesson,
  LessonRequestPayload,
  LessonType,
  PageResult,
  Section,
  SectionRequestPayload,
} from '../../services/admin.service';
import { AdminNavComponent } from './admin-nav.component';

interface FeedbackMessage {
  type: 'success' | 'error';
  text: string;
}

type CourseFormValue = {
  title: string;
  levelName: string;
  targetBand: number | null;
  price: number | null;
  thumbnail: string;
  description: string;
  courseType: CourseType;
  status: CourseStatus;
};

type FilterFormValue = {
  keyword: string;
  status: CourseStatus | '';
  type: CourseType | '';
  size: number;
  sortBy: string;
  direction: 'ASC' | 'DESC';
};

@Component({
  selector: 'app-admin-courses',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, AdminNavComponent],
  templateUrl: './admin-courses.component.html',
  styleUrls: ['./admin-courses.component.css']
})
export class AdminCoursesComponent implements OnInit {
  courses: Course[] = [];
  courseForm: FormGroup;
  filterForm: FormGroup;
  loading = false;
  saving = false;
  deletingId?: number;
  feedback?: FeedbackMessage;
  errorMessage?: string;
  editingCourse: Course | null = null;
  pageIndex = 0;
  pageCount = 0;
  totalCourses = 0;

  readonly statusOptions: { label: string; value: CourseStatus | '' }[] = [
    { label: 'Tất cả trạng thái', value: '' },
    { label: 'Nháp', value: 'DRAFT' },
    { label: 'Đã xuất bản', value: 'PUBLISHED' },
    { label: 'Đã lưu trữ', value: 'ARCHIVED' },
  ];

  readonly typeOptions: { label: string; value: CourseType | '' }[] = [
    { label: 'Tất cả loại', value: '' },
    { label: 'Full Program', value: 'FULL' },
    { label: 'Khóa lẻ', value: 'SINGLE' },
    { label: 'Tips/Short', value: 'TIPS' },
  ];

  readonly pageSizeOptions = [5, 10, 20, 50];
  readonly lessonTypes: LessonType[] = ['VIDEO', 'DOCUMENT'];

  sectionForm: FormGroup;
  lessonForm: FormGroup;
  sections: Section[] = [];
  sectionLessons: Lesson[] = [];
  selectedSectionId?: number;
  editingSectionId?: number;
  deletingSectionId?: number;
  editingLessonId?: number;
  deletingLessonId?: number;
  loadingSections = false;
  loadingLessons = false;
  savingSection = false;
  savingLesson = false;

  constructor(private admin: AdminService, private fb: FormBuilder, private router: Router) {
    this.courseForm = this.fb.group({
      title: ['', [Validators.required, Validators.maxLength(255)]],
      levelName: [''],
      targetBand: [null, [Validators.min(0), Validators.max(9)]],
      price: [null, [Validators.min(0)]],
      thumbnail: [''],
      description: [''],
      courseType: ['FULL', Validators.required],
      status: ['DRAFT', Validators.required],
    });

    this.filterForm = this.fb.group({
      keyword: [''],
      status: [''],
      type: [''],
      size: [10],
      sortBy: ['createdDate'],
      direction: ['DESC'],
    });

    this.sectionForm = this.fb.group({
      title: ['', [Validators.required, Validators.maxLength(255)]],
      orderIndex: [null, Validators.min(0)],
    });

    this.lessonForm = this.fb.group({
      sectionId: [null, Validators.required],
      title: ['', [Validators.required, Validators.maxLength(255)]],
      type: ['VIDEO', Validators.required],
      videoUrl: [''],
      content: [''],
      duration: [null, Validators.min(0)],
    });

    this.lessonForm.get('sectionId')?.valueChanges.subscribe((sectionId) => {
      this.updateSelectedSection(sectionId);
    });
  }

  ngOnInit(): void {
    this.loadCourses();
  }

  get courseFormValue(): CourseFormValue {
    return this.courseForm.value as CourseFormValue;
  }

  get filterFormValue(): FilterFormValue {
    return this.filterForm.value as FilterFormValue;
  }

  applyFilters() {
    this.pageIndex = 0;
    this.loadCourses();
  }

  resetFilters() {
    this.filterForm.reset({
      keyword: '',
      status: '',
      type: '',
      size: 10,
      sortBy: 'createdDate',
      direction: 'DESC',
    });
    this.applyFilters();
  }

  changePage(delta: number) {
    const nextPage = this.pageIndex + delta;
    if (nextPage < 0 || (this.pageCount && nextPage >= this.pageCount)) {
      return;
    }
    this.pageIndex = nextPage;
    this.loadCourses();
  }

  changePageSize() {
    this.pageIndex = 0;
    this.loadCourses();
  }

  startCreate() {
    this.editingCourse = null;
    this.courseForm.reset({
      title: '',
      levelName: '',
      targetBand: null,
      price: null,
      thumbnail: '',
      description: '',
      courseType: 'FULL',
      status: 'DRAFT',
    });
    this.sections = [];
    this.sectionLessons = [];
    this.selectedSectionId = undefined;
    this.resetSectionForm();
    this.resetLessonForm(false);
  }

  editCourse(course: Course) {
    this.editingCourse = course;
    this.courseForm.patchValue({
      title: course.title,
      levelName: course.levelName ?? '',
      targetBand: course.targetBand ?? null,
      price: course.price ?? null,
      thumbnail: course.thumbnail ?? course.imageUrl ?? '',
      description: course.description ?? '',
      courseType: course.courseType ?? 'FULL',
      status: course.status ?? 'DRAFT',
    });
    const courseId = Number(course.id);
    if (Number.isFinite(courseId)) {
      this.loadSections(courseId);
    }
  }

  saveCourse() {
    if (this.courseForm.invalid) {
      this.courseForm.markAllAsTouched();
      return;
    }

    this.saving = true;
    const payload = this.buildCoursePayload();
    const request$ = this.editingCourse
      ? this.admin.updateCourse(Number(this.editingCourse.id), payload)
      : this.admin.createCourse(payload);

    request$
      .pipe(
        take(1),
        finalize(() => (this.saving = false))
      )
      .subscribe({
        next: () => {
          this.setFeedback('success', this.editingCourse ? 'Cập nhật khóa học thành công' : 'Đã tạo khóa học mới');
          this.startCreate();
          this.loadCourses();
        },
        error: (error: unknown) => {
          console.error('saveCourse failed', error);
          this.setFeedback('error', 'Không thể lưu khóa học. Vui lòng thử lại.');
        },
      });
  }

  deleteCourse(course: Course) {
    const courseId = Number(course.id);
    if (!courseId || !confirm(`Xoá khóa học "${course.title}"?`)) {
      return;
    }
    this.deletingId = courseId;
    this.admin
      .deleteCourse(courseId)
      .pipe(
        take(1),
        finalize(() => (this.deletingId = undefined))
      )
      .subscribe({
        next: () => {
          this.setFeedback('success', 'Đã xoá khóa học');
          this.admin.clearCourseQuizzes(courseId);
          this.loadCourses();
        },
        error: (error: unknown) => {
          console.error('deleteCourse failed', error);
          this.setFeedback('error', 'Không thể xoá khóa học.');
        },
      });
  }

  manageQuizzes(course: Course) {
    this.router.navigate(['/admin', 'course', `${course.id}`, 'quizzes']);
  }

  saveSection() {
    if (!this.editingCourse?.id) {
      this.setFeedback('error', 'Hãy chọn một khóa học trước khi thêm chương.');
      return;
    }
    if (this.sectionForm.invalid) {
      this.sectionForm.markAllAsTouched();
      return;
    }

    const value = this.sectionForm.value as { title: string; orderIndex: number | null };
    const payload: SectionRequestPayload = {
      courseId: Number(this.editingCourse.id),
      title: value.title.trim(),
      orderIndex:
        value.orderIndex === null || value.orderIndex === undefined ? undefined : Number(value.orderIndex),
    };

    const isUpdating = Boolean(this.editingSectionId);
    this.savingSection = true;
    const request$ = isUpdating
      ? this.admin.updateSection(Number(this.editingSectionId), payload)
      : this.admin.createSection(payload);

    request$
      .pipe(
        take(1),
        finalize(() => (this.savingSection = false))
      )
      .subscribe({
        next: () => {
          this.resetSectionForm();
          this.setFeedback('success', isUpdating ? 'Đã cập nhật chương' : 'Đã thêm chương mới');
          this.loadSections(Number(this.editingCourse?.id));
        },
        error: (error: unknown) => {
          console.error('saveSection failed', error);
          this.setFeedback('error', isUpdating ? 'Không thể cập nhật chương.' : 'Không thể tạo chương.');
        },
      });
  }

  saveLesson() {
    if (this.lessonForm.invalid) {
      this.lessonForm.markAllAsTouched();
      return;
    }
    const value = this.lessonForm.value as {
      sectionId: number;
      title: string;
      type: LessonType;
      videoUrl?: string | null;
      content?: string | null;
      duration?: number | null;
    };

    if (!value.sectionId) {
      alert('Vui lòng chọn chương để thêm bài học.');
      return;
    }

    const payload: LessonRequestPayload = {
      sectionId: Number(value.sectionId),
      title: value.title.trim(),
      type: value.type,
      videoUrl: value.videoUrl?.trim() || undefined,
      content: value.content?.trim() || undefined,
      duration: value.duration === null || value.duration === undefined ? undefined : Number(value.duration),
    };

    const isUpdating = Boolean(this.editingLessonId);
    this.savingLesson = true;
    const request$ = isUpdating
      ? this.admin.updateLesson(Number(this.editingLessonId), payload)
      : this.admin.createLesson(payload);

    request$
      .pipe(
        take(1),
        finalize(() => (this.savingLesson = false))
      )
      .subscribe({
        next: () => {
          const sectionId = Number(value.sectionId);
          this.resetLessonForm(true);
          if (sectionId) {
            this.loadLessons(sectionId);
          }
          this.setFeedback('success', isUpdating ? 'Đã cập nhật bài học' : 'Đã thêm bài học');
        },
        error: (error: unknown) => {
          console.error('saveLesson failed', error);
          this.setFeedback('error', isUpdating ? 'Không thể cập nhật bài học.' : 'Không thể tạo bài học.');
        },
      });
  }

  selectSection(section: Section) {
    if (!section?.id) {
      return;
    }
    this.selectedSectionId = section.id;
    this.lessonForm.patchValue({ sectionId: section.id }, { emitEvent: false });
    this.updateSelectedSection(section.id);
  }

  editLesson(lesson: Lesson) {
    if (!lesson?.id) {
      return;
    }
    this.editingLessonId = lesson.id;
    const sectionId = lesson.sectionId ?? this.selectedSectionId ?? null;
    this.lessonForm.patchValue(
      {
        sectionId,
        title: lesson.title || '',
        type: lesson.type || 'VIDEO',
        videoUrl: lesson.videoUrl || '',
        content: lesson.content || '',
        duration: lesson.duration ?? null,
      },
      { emitEvent: false }
    );
    this.updateSelectedSection(sectionId);
  }

  cancelLessonEdit() {
    this.resetLessonForm(true);
  }

  deleteLesson(lesson: Lesson) {
    if (!lesson?.id || !confirm(`Xoá bài học "${lesson.title}"?`)) {
      return;
    }
    this.deletingLessonId = lesson.id;
    this.admin
      .deleteLesson(lesson.id)
      .pipe(
        take(1),
        finalize(() => (this.deletingLessonId = undefined))
      )
      .subscribe({
        next: () => {
          if (this.editingLessonId === lesson.id) {
            this.resetLessonForm(true);
          }
          this.setFeedback('success', 'Đã xoá bài học');
          if (lesson.sectionId) {
            this.loadLessons(lesson.sectionId);
          }
        },
        error: (error: unknown) => {
          console.error('deleteLesson failed', error);
          this.setFeedback('error', 'Không thể xoá bài học.');
        },
      });
  }

  editSection(section: Section) {
    if (!section?.id) {
      return;
    }
    this.editingSectionId = section.id;
    this.sectionForm.patchValue({
      title: section.title || '',
      orderIndex: section.orderIndex ?? null,
    });
  }

  cancelSectionEdit() {
    this.resetSectionForm();
  }

  deleteSection(section: Section) {
    if (!section?.id || !confirm(`Xoá chương "${section.title}"?`)) {
      return;
    }
    this.deletingSectionId = section.id;
    this.admin
      .deleteSection(section.id)
      .pipe(
        take(1),
        finalize(() => (this.deletingSectionId = undefined))
      )
      .subscribe({
        next: () => {
          if (this.selectedSectionId === section.id) {
            this.updateSelectedSection(null);
          }
          if (this.editingSectionId === section.id) {
            this.resetSectionForm();
          }
          const editingLessonSectionId = this.lessonForm.get('sectionId')?.value;
          if (this.editingLessonId && editingLessonSectionId === section.id) {
            this.resetLessonForm(false);
          }
          this.setFeedback('success', 'Đã xoá chương');
          if (this.editingCourse?.id) {
            this.loadSections(Number(this.editingCourse.id));
          }
        },
        error: (error: unknown) => {
          console.error('deleteSection failed', error);
          this.setFeedback('error', 'Không thể xoá chương.');
        },
      });
  }

  refreshStructure() {
    if (this.editingCourse?.id) {
      this.loadSections(Number(this.editingCourse.id));
    }
  }

  trackByCourseId(_index: number, course: Course) {
    return course.id;
  }

  getStatusLabel(status?: CourseStatus) {
    if (!status) {
      return 'Không xác định';
    }
    switch (status) {
      case 'PUBLISHED':
        return 'Published';
      case 'ARCHIVED':
        return 'Archived';
      default:
        return 'Draft';
    }
  }

  getTypeLabel(type?: CourseType) {
    if (!type) {
      return 'N/A';
    }
    switch (type) {
      case 'FULL':
        return 'Full';
      case 'SINGLE':
        return 'Single';
      case 'TIPS':
        return 'Tips';
      default:
        return type;
    }
  }

  private loadCourses() {
    this.loading = true;
    const params: CourseListParams = {
      keyword: this.filterFormValue.keyword?.trim() || undefined,
      status: this.filterFormValue.status || undefined,
      type: this.filterFormValue.type || undefined,
      page: this.pageIndex,
      size: Number(this.filterFormValue.size) || 10,
      sortBy: this.filterFormValue.sortBy,
      direction: this.filterFormValue.direction,
    };

    this.admin
      .fetchCourses(params)
      .pipe(
        take(1),
        finalize(() => (this.loading = false))
      )
      .subscribe({
        next: (page: PageResult<Course>) => {
          this.courses = page.content;
          this.pageIndex = page.number;
          this.pageCount = page.totalPages;
          this.totalCourses = page.totalElements;
          this.errorMessage = undefined;
        },
        error: (error: unknown) => {
          console.error('loadCourses failed', error);
          this.errorMessage = 'Không thể tải dữ liệu. Vui lòng thử lại.';
          this.courses = [];
        },
      });
  }

  private loadSections(courseId: number) {
    if (!courseId) {
      return;
    }
    this.loadingSections = true;
    this.admin
      .getSectionsByCourse(courseId)
      .pipe(
        take(1),
        finalize(() => (this.loadingSections = false))
      )
      .subscribe({
        next: (sections) => {
          this.sections = sections;
          if (!sections.length) {
            this.selectedSectionId = undefined;
            this.sectionLessons = [];
            this.lessonForm.patchValue({ sectionId: null }, { emitEvent: false });
            this.resetLessonForm(false);
            return;
          }

          const existing = sections.find((section) => section.id === this.selectedSectionId);
          const fallbackId = existing?.id ?? sections[0].id;
          this.selectedSectionId = fallbackId;
          this.lessonForm.patchValue({ sectionId: fallbackId }, { emitEvent: false });
          this.updateSelectedSection(fallbackId);
        },
        error: (error: unknown) => {
          console.error('loadSections failed', error);
          this.sections = [];
          this.sectionLessons = [];
        },
      });
  }

  private loadLessons(sectionId: number) {
    if (!sectionId) {
      this.sectionLessons = [];
      return;
    }
    this.loadingLessons = true;
    this.admin
      .getLessonsBySection(sectionId)
      .pipe(
        take(1),
        finalize(() => (this.loadingLessons = false))
      )
      .subscribe({
        next: (lessons) => {
          this.sectionLessons = lessons;
        },
        error: (error: unknown) => {
          console.error('loadLessons failed', error);
          this.sectionLessons = [];
        },
      });
  }

  private buildCoursePayload(): CourseRequestPayload {
    const value = this.courseFormValue;
    const price = value.price === null || value.price === undefined ? undefined : Number(value.price);
    const targetBand = value.targetBand === null || value.targetBand === undefined ? undefined : Number(value.targetBand);

    return {
      title: value.title.trim(),
      levelName: value.levelName?.trim() || undefined,
      targetBand,
      price,
      thumbnail: value.thumbnail?.trim() || undefined,
      description: value.description?.trim() || undefined,
      courseType: value.courseType,
      status: value.status,
    };
  }

  private setFeedback(type: 'success' | 'error', text: string) {
    this.feedback = { type, text };
    setTimeout(() => {
      if (this.feedback?.text === text) {
        this.feedback = undefined;
      }
    }, 4000);
  }

  private updateSelectedSection(sectionId: unknown) {
    if (sectionId === null || sectionId === undefined || sectionId === '') {
      this.selectedSectionId = undefined;
      this.sectionLessons = [];
      return;
    }

    const numericId = typeof sectionId === 'number' ? sectionId : Number(sectionId);
    if (!Number.isFinite(numericId) || numericId <= 0) {
      this.selectedSectionId = undefined;
      this.sectionLessons = [];
      return;
    }

    this.selectedSectionId = numericId;
    this.loadLessons(numericId);
  }

  private resetSectionForm() {
    this.editingSectionId = undefined;
    this.sectionForm.reset({ title: '', orderIndex: null });
  }

  private resetLessonForm(keepSection = true) {
    const sectionId = keepSection ? this.lessonForm.get('sectionId')?.value ?? this.selectedSectionId ?? null : null;
    this.editingLessonId = undefined;
    this.lessonForm.reset(
      {
        sectionId,
        title: '',
        type: 'VIDEO',
        videoUrl: '',
        content: '',
        duration: null,
      },
      { emitEvent: false }
    );

    if (!sectionId) {
      this.updateSelectedSection(null);
    }
  }
}
