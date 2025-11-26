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
  PageResult,
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
          this.admin.clearCourseTests(courseId);
          this.loadCourses();
        },
        error: (error: unknown) => {
          console.error('deleteCourse failed', error);
          this.setFeedback('error', 'Không thể xoá khóa học.');
        },
      });
  }

  manageTests(course: Course) {
    this.router.navigate(['/admin', 'course', `${course.id}`, 'tests']);
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
}
