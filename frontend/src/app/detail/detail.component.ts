import { Component, OnInit, inject, DestroyRef } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Router } from '@angular/router';
import { CommonModule, NgIf, NgFor, DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ProductItems } from '../types/productItem';
import { ProductService } from '../../services/product.service';
import { CartService } from '../../services/cart.service';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NotificationService } from '../../services/notification.service';
import { ButtonComponent } from '../shared/button/button.component';

@Component({
  selector: 'app-detail',
  standalone: true,
  templateUrl: './detail.component.html',
  styleUrls: ['./detail.component.css'],
  imports: [CommonModule, NgIf, NgFor, DecimalPipe, RouterLink, ButtonComponent]
})
export class DetailComponent implements OnInit {
  product?: ProductItems;
  private destroyRef = inject(DestroyRef);
  selectedImageUrl: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private productService: ProductService,
    private cartService: CartService,
    private notificationService: NotificationService,
    private router: Router
  ) {}

  back() { this.router.navigate(['/courses']); }


  ngOnInit(): void {
    // Lắng nghe thay đổi param id
    this.route.paramMap
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(params => {
        const id = params.get('id');
        if (id) {
          this.loadProduct(id);
        }
      });
  }

  private loadProduct(id: string): void {
    this.productService.getProductById(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          this.product = res.result;
          // set default selected image
          this.selectedImageUrl = this.getMainImage();
        },
        error: (err) => {
          // If API not available, fall back to mock data so UI can be developed
          console.warn('getProductById failed, using mock product', err);
          this.product = this.getMockProduct(id);
          this.selectedImageUrl = this.getMainImage();
        }
      });
  }

  // Temporary mock product data when backend API is not ready
  private getMockProduct(id: string): ProductItems {
    const numId = Number(id) || 1;
    return {
      id: numId,
      code: `CRS-${1000 + numId}`,
      name: numId === 1 ? 'IELTS Foundation (0 - 5.0)' : numId === 2 ? 'IELTS Intensive (5.0 - 7.5)' : 'IELTS Band 7.5+',
      description:
        numId === 1 ? 'Khóa học thiết kế cho người mới bắt đầu, bao gồm 4 kỹ năng, phát âm và từ vựng cơ bản. Bao gồm bài tập và đánh giá AI.'
        : numId === 2 ? 'Khóa học nâng cao cho những người đã có nền tảng, tập trung vào kỹ năng làm bài thi.'
        : 'Khóa học chuyên sâu cho những người muốn đạt điểm cao.',
      status: 1,
      quantity: 999,
      price: numId === 1 ? 990000 : 1490000,
      createdBy: 'AIELTS Team',
      updatedBy: 'AIELTS Team',
      images: [
        { idImage: 1, imageUrl: 'assets/courses/foundation.jpg', imageMain: true },
      ],
    } as ProductItems;
  }

  getMainImage(): string {
    const mainImg = this.product?.images?.find(img => img.imageMain);
    return mainImg?.imageUrl ?? 'assets/images/default.png';
  }

onThumbnailClick(url: string | undefined) {
  if (url) {
    this.selectedImageUrl = url;
  }
}


  get hasMultipleImages(): boolean {
    return (this.product?.images?.length ?? 0) > 1;
  }

  addToCart(): void {
    if (!this.product) return;
    // For course purchases we do NOT add to cart. Instead we pass the course to checkout
    // and mark the checkout as a course-purchase flow so enrollment can be recorded after payment.
    const item = { ...this.product } as any;
    // persist the course items for the checkout flow so payment-result can enroll them
    try {
      localStorage.setItem('checkoutCourseItems', JSON.stringify([item]));
      localStorage.setItem('checkoutIsCoursePurchase', '1');
    } catch (e) {
      console.warn('Could not save checkoutCourseItems', e);
    }
    this.notificationService.show('success', 'Chuyển sang trang thanh toán...');
    this.router.navigate(['/checkout'], { state: { items: [item], skipInfo: true, isCoursePurchase: true } });
  }
}
