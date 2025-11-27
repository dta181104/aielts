import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { DecimalPipe } from '@angular/common';
import { finalize, take } from 'rxjs';
import { ButtonComponent } from '../shared/button/button.component';
import { AdminService, Course } from '../../services/admin.service';

interface CourseItem {
  id: number;
  title: string;
  shortDesc: string;
  image?: string;
  price: number;
}

@Component({
  selector: 'app-courses',
  standalone: true,
  imports: [CommonModule, DecimalPipe, ButtonComponent],
  
  templateUrl: './courses.component.html',
  styleUrls: ['./courses.component.css'],
})
export class CoursesComponent implements OnInit {
  courses: CourseItem[] = [];
  loading = false;
  errorMessage?: string;

  constructor(private router: Router, private adminService: AdminService) {}

  ngOnInit(): void {
    this.loadCourses();
  }

  goDetail(id: number) {
    this.router.navigate(['/course', id]);
  }

  private loadCourses() {
    this.loading = true;
    this.errorMessage = undefined;
    this.adminService
      .fetchCourses({ status: 'PUBLISHED', page: 0, size: 12, sortBy: 'createdDate', direction: 'DESC' })
      .pipe(
        take(1),
        finalize(() => (this.loading = false))
      )
      .subscribe({
        next: (page) => {
          this.courses = page.content.map((course) => this.mapCourseItem(course));
        },
        error: (error: unknown) => {
          console.error('Failed to load public courses', error);
          this.courses = [];
          this.errorMessage = 'Không thể tải danh sách khóa học. Vui lòng thử lại sau.';
        },
      });
  }

  private mapCourseItem(course: Course): CourseItem {
    return {
      id: Number(course.id),
      title: course.title ?? 'Khóa học IELTS',
      shortDesc: course.description?.trim() || 'Nội dung đang được cập nhật.',
      image: course.thumbnail || course.imageUrl,
      price: course.price ?? 0,
    };
  }
}
