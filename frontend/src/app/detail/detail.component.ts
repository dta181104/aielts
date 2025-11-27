import { Component, OnInit, inject, DestroyRef } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule, NgIf, NgFor, DecimalPipe } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import { NotificationService } from '../../services/notification.service';
import { ButtonComponent } from '../shared/button/button.component';
import { AdminService, Course, Section } from '../../services/admin.service';

@Component({
  selector: 'app-detail',
  standalone: true,
  templateUrl: './detail.component.html',
  styleUrls: ['./detail.component.css'],
  imports: [CommonModule, NgIf, NgFor, DecimalPipe, RouterLink, ButtonComponent]
})
export class DetailComponent implements OnInit {
  course?: Course;
  sections: Section[] = [];
  private destroyRef = inject(DestroyRef);
  selectedImageUrl: string | null = null;
  loading = false;
  errorMessage?: string;

  constructor(
    private route: ActivatedRoute,
    private adminService: AdminService,
    private notificationService: NotificationService,
    private router: Router
  ) {}

  back() { this.router.navigate(['/courses']); }


  ngOnInit(): void {
    this.route.paramMap
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(params => {
        const id = Number(params.get('id'));
        if (Number.isFinite(id)) {
          this.loadCourse(id);
        } else {
          this.errorMessage = 'Mã khóa học không hợp lệ.';
          this.course = undefined;
          this.sections = [];
        }
      });
  }

  private loadCourse(id: number): void {
    this.loading = true;
    this.errorMessage = undefined;
    this.adminService
      .getCourseDetail(id)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => (this.loading = false))
      )
      .subscribe({
        next: (course) => {
          this.course = course;
          this.sections = course.sections ?? [];
          this.selectedImageUrl = course.thumbnail || course.imageUrl || null;
        },
        error: (error) => {
          console.error('loadCourse detail failed', error);
          this.course = undefined;
          this.sections = [];
          this.selectedImageUrl = null;
          this.errorMessage = 'Không tìm thấy thông tin khóa học.';
        },
      });
  }

  getDisplayImage(): string {
    return this.selectedImageUrl || this.course?.thumbnail || this.course?.imageUrl || 'assets/images/default.png';
  }

  getStatusLabel(status?: string): string {
    if (!status) {
      return 'Đang cập nhật';
    }
    switch (status) {
      case 'PUBLISHED':
        return 'Đang mở';
      case 'ARCHIVED':
        return 'Ngừng tuyển sinh';
      case 'DRAFT':
      default:
        return 'Sắp ra mắt';
    }
  }

  getTypeLabel(type?: string): string {
    if (!type) {
      return 'Khóa học';
    }
    switch (type) {
      case 'FULL':
        return 'Full Program';
      case 'SINGLE':
        return 'Khóa lẻ';
      case 'TIPS':
        return 'Tips & Tricks';
      default:
        return type;
    }
  }

  addToCart(): void {
    if (!this.course) return;
    const item = {
      id: this.course.id,
      name: this.course.title,
      description: this.course.description,
      price: this.course.price ?? 0,
      thumbnail: this.course.thumbnail || this.course.imageUrl,
      courseType: this.course.courseType,
    };
    // persist the course items for the checkout flow so payment-result can enroll them
    try {
      localStorage.setItem('checkoutCourseItems', JSON.stringify([item]));
      localStorage.setItem('checkoutIsCoursePurchase', '1');
    } catch (e) {
      console.warn('Could not save checkoutCourseItems', e);
    }
    this.notificationService.show('success', 'Chuyển sang trang thanh toán...');
    this.router.navigate(['/checkout'], { state: { items: [item], skipInfo: true, isCoursePurchase: true } });
  }
}
