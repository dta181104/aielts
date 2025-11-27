import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { ProductItems } from '../app/types/productItem';
import { ApiResponse } from '../model/product.model';
import { environment } from '../environments/environment';

interface EnrolledCourseResponse {
  id: number;
  title: string;
  thumbnail?: string;
  description?: string;
  progressPercent?: number;
  enrolledDate?: string;
  status?: string;
  price?: number;
}

@Injectable({ providedIn: 'root' })
export class EnrollmentService {
  private key = 'enrolled_courses';
  private enrolledSubject = new BehaviorSubject<ProductItems[]>(this.getEnrolled());
  enrolled$ = this.enrolledSubject.asObservable();
  private apiBase = environment.apiUrl;

  constructor(private http: HttpClient) {}

  fetchUserEnrollments(userId: number | string): Observable<ProductItems[]> {
    const numericId = typeof userId === 'number' ? userId : Number(userId);
    if (!Number.isFinite(numericId) || numericId <= 0) {
      return throwError(() => new Error('INVALID_USER_ID'));
    }

    const url = `${this.apiBase}/users/${numericId}/courses`;
    return this.http.get<ApiResponse<EnrolledCourseResponse[]>>(url).pipe(
      map((response) => this.mapEnrollmentResponse(response?.result ?? [])),
      tap((list) => this.save(list)),
      catchError((error) => {
        console.error('fetchUserEnrollments failed', error);
        return throwError(() => error);
      })
    );
  }

  private getEnrolled(): ProductItems[] {
    try {
      return JSON.parse(localStorage.getItem(this.key) || '[]');
    } catch (e) {
      return [];
    }
  }

  private save(enrolled: ProductItems[]) {
    localStorage.setItem(this.key, JSON.stringify(enrolled));
    this.enrolledSubject.next(enrolled);
  }

  enroll(items: ProductItems[]) {
    const current = this.getEnrolled();
    // add unique by id
    const ids = new Set(current.map(i => i.id));
    const merged = [...current];
    items.forEach(it => {
      if (!ids.has(it.id)) {
        // ensure no quantity field stored for enrolled courses
        const copy = { ...it } as any;
        if (copy.quantity) delete copy.quantity;
        merged.push(copy);
        ids.add(it.id);
      }
    });
    this.save(merged);
  }

  getAll(): ProductItems[] {
    return this.getEnrolled();
  }

  isEnrolled(id: number): boolean {
    return this.getEnrolled().some(i => i.id === id);
  }

  private mapEnrollmentResponse(items: EnrolledCourseResponse[]): ProductItems[] {
    return items.map((item) => ({
      id: item.id,
      name: item.title,
      description: item.description || 'Khóa học đang cập nhật nội dung.',
      price: item.price ?? 0,
      images: item.thumbnail
        ? [
            {
              imageUrl: item.thumbnail,
              imageMain: true,
            },
          ]
        : undefined,
      progressPercent: item.progressPercent ?? 0,
      enrolledDate: item.enrolledDate,
      enrollmentStatus: item.status,
    }));
  }
}
