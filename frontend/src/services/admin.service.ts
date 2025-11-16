import { Injectable } from '@angular/core';

export interface Course {
  id: string;
  title: string;
  description?: string;
  price?: number;
  imageUrl?: string;
}

export interface CourseTest {
  id: string;
  courseId: string;
  title: string;
  content?: string; // simple JSON or HTML representing test
}

@Injectable({ providedIn: 'root' })
export class AdminService {
  private coursesKey = 'admin_courses';
  private testsKey = 'admin_course_tests';

  generateId(prefix = ''): string {
    return prefix + Date.now().toString(36) + Math.random().toString(36).slice(2, 8);
  }

  getCourses(): Course[] {
    try {
      const raw = localStorage.getItem(this.coursesKey);
      return raw ? JSON.parse(raw) : [];
    } catch (e) {
      return [];
    }
  }

  saveCourse(course: Course) {
    const list = this.getCourses();
    const idx = list.findIndex(c => c.id === course.id);
    if (idx >= 0) list[idx] = course;
    else list.push(course);
    localStorage.setItem(this.coursesKey, JSON.stringify(list));
  }

  deleteCourse(id: string) {
    const list = this.getCourses().filter(c => c.id !== id);
    localStorage.setItem(this.coursesKey, JSON.stringify(list));
    // also remove tests for the course
    const testsMap = this._readTestsMap();
    delete testsMap[id];
    localStorage.setItem(this.testsKey, JSON.stringify(testsMap));
  }

  getCourseById(id: string): Course | undefined {
    return this.getCourses().find(c => c.id === id);
  }

  private _readTestsMap(): Record<string, CourseTest[]> {
    try {
      const raw = localStorage.getItem(this.testsKey);
      return raw ? JSON.parse(raw) : {};
    } catch (e) {
      return {};
    }
  }

  getTests(courseId: string): CourseTest[] {
    const m = this._readTestsMap();
    return m[courseId] || [];
  }

  saveTest(courseId: string, test: CourseTest) {
    const m = this._readTestsMap();
    if (!m[courseId]) m[courseId] = [];
    const idx = m[courseId].findIndex(t => t.id === test.id);
    if (idx >= 0) m[courseId][idx] = test;
    else m[courseId].push(test);
    localStorage.setItem(this.testsKey, JSON.stringify(m));
  }

  deleteTest(courseId: string, testId: string) {
    const m = this._readTestsMap();
    if (!m[courseId]) return;
    m[courseId] = m[courseId].filter(t => t.id !== testId);
    localStorage.setItem(this.testsKey, JSON.stringify(m));
  }
}
