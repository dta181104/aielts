import { Component, OnInit, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router';
import { ButtonComponent } from '../shared/button/button.component';
import { EnrollmentService } from '../../services/enrollment.service';
import { ProductItems } from '../types/productItem';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-my-courses',
  standalone: true,
  imports: [CommonModule, RouterLink, ButtonComponent],
  templateUrl: './my-courses.component.html',
  styleUrls: ['./my-courses.component.css'],
})
export class MyCoursesComponent implements OnInit {
  items: ProductItems[] = [];
  loading = false;
  errorMessage?: string;
  userId?: number;
  private destroyRef = inject(DestroyRef);

  constructor(private enrollmentService: EnrollmentService, private router: Router) {}

  ngOnInit(): void {
    this.items = this.enrollmentService.getAll();
    this.enrollmentService.enrolled$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((list) => (this.items = list || []));

    this.userId = this.resolveUserId();
    if (!this.userId) {
      this.errorMessage = 'Vui lòng đăng nhập để xem các khóa học đã đăng ký.';
      return;
    }

    this.loading = true;
    this.enrollmentService
      .fetchUserEnrollments(this.userId)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => (this.loading = false))
      )
      .subscribe({
        next: () => {
          this.errorMessage = undefined;
        },
        error: (error: unknown) => {
          console.error('Failed to load enrolled courses', error);
          this.errorMessage = 'Không thể tải danh sách khóa học. Vui lòng thử lại sau.';
        },
      });
  }

  goToCourse(item: ProductItems) {
    this.router.navigate(['/learn', item.id]);
  }

  openResources(item: ProductItems) {
    // placeholder: navigate to course resources or show modal
    this.router.navigate(['/course', item.id]);
  }

  private resolveUserId(): number | undefined {
    if (typeof window === 'undefined') {
      return undefined;
    }
    const sources = ['user_profile', 'account_profile'];
    for (const key of sources) {
      try {
        const raw = localStorage.getItem(key);
        if (!raw) continue;
        const profile = JSON.parse(raw);
        const candidate =
          profile?.id ??
          profile?.userId ??
          profile?.user?.id ??
          profile?.result?.id ??
          profile?.profileId ??
          profile?.code;
        const parsed = Number(candidate);
        if (Number.isFinite(parsed) && parsed > 0) {
          return parsed;
        }
      } catch (e) {
        console.warn('Failed to parse stored profile for user id', e);
      }
    }
    return undefined;
  }
}
