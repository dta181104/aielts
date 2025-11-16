import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AdminService, Course } from '../../services/admin.service';

@Component({
  selector: 'app-admin-courses',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-courses.component.html',
  styleUrls: ['./admin-courses.component.css']
})
export class AdminCoursesComponent {
  courses: Course[] = [];
  model: Partial<Course> = {};
  editingId: string | null = null;

  constructor(private admin: AdminService, private router: Router) {
    this.load();
  }

  load() {
    this.courses = this.admin.getCourses();
  }

  startCreate() {
    this.model = { title: '', description: '', price: 0, imageUrl: '' };
    this.editingId = null;
  }

  edit(course: Course) {
    this.editingId = course.id;
    this.model = { ...course };
  }

  save() {
    if (!this.model.title) return alert('Vui lòng nhập tiêu đề khóa học');
    const payload: Course = {
      id: this.editingId || this.admin.generateId(),
      title: this.model.title || '',
      description: this.model.description || '',
      price: Number(this.model.price) || 0,
      imageUrl: this.model.imageUrl || ''
    };

    this.admin.saveCourse(payload);
    this.load();
    this.model = {};
    this.editingId = null;
  }

  remove(course: Course) {
    if (!confirm('Xoá khóa học này?')) return;
    this.admin.deleteCourse(course.id);
    this.load();
  }

  manageTests(course: Course) {
    this.router.navigate(['/admin', 'course', course.id, 'tests']);
  }
}
