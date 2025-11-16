import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AdminService, Course, CourseTest } from '../../services/admin.service';

@Component({
  selector: 'app-admin-course-tests',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-course-tests.component.html',
  styleUrls: ['./admin-course-tests.component.css']
})
export class AdminCourseTestsComponent {
  courseId: string | null = null;
  course: Course | null = null;
  tests: CourseTest[] = [];

  model: Partial<CourseTest> = {};

  constructor(private route: ActivatedRoute, private admin: AdminService, private router: Router) {
    this.courseId = this.route.snapshot.paramMap.get('id');
    if (this.courseId) {
      this.course = this.admin.getCourseById(this.courseId) || null;
      this.tests = this.admin.getTests(this.courseId) || [];
    }
  }

  addTest() {
    if (!this.model.title) return alert('Nhập tiêu đề bài test');
    const payload: CourseTest = {
      id: this.admin.generateId(),
      courseId: this.courseId || '',
      title: this.model.title || '',
      content: this.model.content || ''
    };
    this.admin.saveTest(this.courseId || '', payload);
    this.tests = this.admin.getTests(this.courseId || '') || [];
    this.model = {};
  }

  remove(t: CourseTest) {
    if (!confirm('Xoá bài test này?')) return;
    this.admin.deleteTest(this.courseId || '', t.id);
    this.tests = this.admin.getTests(this.courseId || '') || [];
  }

  back() {
    this.router.navigate(['/admin']);
  }
}
