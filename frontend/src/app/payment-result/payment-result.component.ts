import { Component, OnInit } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';
import { Router } from '@angular/router';
import { CartService } from '../../services/cart.service';
import { EnrollmentService } from '../../services/enrollment.service';

@Component({
  selector: 'app-payment-result',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './payment-result.component.html',
  styleUrls: ['./payment-result.component.css'],
})
export class PaymentResultComponent implements OnInit {
  paymentInfo: any = null;
  gateway: string = '';
  constructor(private router: Router, private cartService: CartService, private enrollmentService: EnrollmentService) {}

  ngOnInit(): void {
    const state = history.state;

    if (!state || !state.paymentStatus) {
      this.router.navigate(['/']);
      return;
    }

    this.paymentInfo = state;
    this.gateway = state.gateway === 'vnpay' ? 'VNPay' : (state.gateway === 'momo' ? 'MoMo' : 'COD');
    const itemIdsString = localStorage.getItem('checkoutItemIds');
    if (state.paymentStatus === 'success') {
      // If there were cart item ids saved, remove them from cart
      if (itemIdsString) {
        const itemIds: number[] = JSON.parse(itemIdsString);
        itemIds.forEach((id) => this.cartService.removeItem(id));
        localStorage.removeItem('checkoutItemIds');
      }

      // If this was a course purchase flow, enroll the course(s)
      const isCourse = !!localStorage.getItem('checkoutIsCoursePurchase');
      if (isCourse) {
        try {
          const courseItemsJson = localStorage.getItem('checkoutCourseItems');
          if (courseItemsJson) {
            const courseItems = JSON.parse(courseItemsJson) as any[];
            // ensure structures match ProductItems before enrolling
            this.enrollmentService.enroll(courseItems as any);
            localStorage.removeItem('checkoutCourseItems');
          }
        } catch (e) {
          console.error('Failed to enroll course items', e);
        }
        localStorage.removeItem('checkoutIsCoursePurchase');
      }
    }
  }

  goToMyCourses() {
    this.router.navigateByUrl('/my-courses').then(ok => {
      if (!ok) console.warn('Navigation to /my-courses returned false');
    }).catch(err => console.error('Navigation error to /my-courses', err));
  }

  goToCourses() {
    this.router.navigate(['/courses']);
  }
}
