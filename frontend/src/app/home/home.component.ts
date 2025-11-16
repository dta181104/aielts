import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { Router } from '@angular/router';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css'],
})
export class HomeComponent implements OnInit, OnDestroy {
  courses: any[] = [];
  getCoursesApi!: Subscription;
  router = inject(Router);
  currentYear: number = new Date().getFullYear();

  ngOnInit(): void {
    // Dữ liệu mẫu (có thể thay bằng gọi API sau)
    this.courses = [
      {
        id: 1,
        title: 'IELTS Foundation (0 - 5.0)',
        shortDesc: 'Xây nền tảng 4 kỹ năng, phát âm và từ vựng cốt lõi.',
        image: 'assets/courses/foundation.jpg',
        price: 990000,
      },
      {
        id: 2,
        title: 'IELTS Intensive (5.0 - 7.5)',
        shortDesc: 'Rèn luyện 4 kỹ năng chuyên sâu.',
        image: 'assets/courses/intensive.jpg',
        price: 1490000,
      },
      {
        id: 3,
        title: 'IELTS Band 7.5+',
        shortDesc: 'Nâng cao kỹ năng với chiến lược đạt band cao.',
        image: 'assets/courses/writing.jpg',
        price: 1990000,
      },
    ];
  }

  viewCourse(courseId: number) {
    this.router.navigate(['/course', courseId]);
  }

  ngOnDestroy(): void {
    if (this.getCoursesApi) this.getCoursesApi.unsubscribe();
  }
}
