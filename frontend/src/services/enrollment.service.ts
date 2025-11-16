import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { ProductItems } from '../app/types/productItem';

@Injectable({ providedIn: 'root' })
export class EnrollmentService {
  private key = 'enrolled_courses';
  private enrolledSubject = new BehaviorSubject<ProductItems[]>(this.getEnrolled());
  enrolled$ = this.enrolledSubject.asObservable();

  constructor() {}

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
}
