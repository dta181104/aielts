import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { DecimalPipe } from '@angular/common';
import { ButtonComponent } from '../shared/button/button.component';

interface CourseItem {
  id: number;
  title: string;
  shortDesc: string;
  image: string;
  price: number;
}

@Component({
  selector: 'app-courses',
  standalone: true,
  imports: [CommonModule, RouterLink, DecimalPipe, ButtonComponent],
  
  templateUrl: './courses.component.html',
  styleUrls: ['./courses.component.css'],
})
export class CoursesComponent implements OnInit {
  courses: CourseItem[] = [];

  constructor(private router: Router) {}

  ngOnInit(): void {
    // temporary mock data while API isn't available
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
        shortDesc: 'Rèn kỹ năng Writing & Speaking chuyên sâu.',
        image: 'assets/courses/intensive.jpg',
        price: 1490000,
      },
      {
        id: 3,
        title: 'IELTS Band 7.5+',
        shortDesc: 'Chiến thuật viết bài luận band 8+ với feedback chi tiết.',
        image: 'assets/courses/writing.jpg',
        price: 1990000,
      },
      // add a couple more mock items to show grid
    //   {
    //     id: 4,
    //     title: 'Speaking Booster',
    //     shortDesc: 'Luyện Speaking theo chủ đề thật, feedback cá nhân.',
    //     image: 'assets/courses/speaking.jpg',
    //     price: 690000,
    //   },
    //   {
    //     id: 5,
    //     title: 'Listening Mastery',
    //     shortDesc: 'Kỹ thuật nghe hiệu quả & tăng tốc phản xạ.',
    //     image: 'assets/courses/listening.jpg',
    //     price: 490000,
    //   },
    ];
  }

  goDetail(id: number) {
    this.router.navigate(['/course', id]);
  }
}
