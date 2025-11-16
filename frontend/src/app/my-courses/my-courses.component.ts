import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router';
import { ButtonComponent } from '../shared/button/button.component';
import { EnrollmentService } from '../../services/enrollment.service';
import { ProductItems } from '../types/productItem';

@Component({
  selector: 'app-my-courses',
  standalone: true,
  imports: [CommonModule, RouterLink, ButtonComponent],
  templateUrl: './my-courses.component.html',
  styleUrls: ['./my-courses.component.css'],
})
export class MyCoursesComponent implements OnInit {
  items: ProductItems[] = [];

  constructor(private enrollmentService: EnrollmentService, private router: Router) {}

  ngOnInit(): void {
    this.loadItems();
    // subscribe to enrolled$ to react to new enrollments
    this.enrollmentService.enrolled$.subscribe(list => this.items = list || []);
  }

  private loadItems() {
    this.items = this.enrollmentService.getAll();
  }

  goToCourse(item: ProductItems) {
    this.router.navigate(['/learn', item.id]);
  }

  openResources(item: ProductItems) {
    // placeholder: navigate to course resources or show modal
    this.router.navigate(['/course', item.id]);
  }
}
