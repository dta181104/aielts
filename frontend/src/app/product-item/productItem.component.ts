
import { Component, EventEmitter, Input, NgModule, OnChanges, OnDestroy, Output, SimpleChanges } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink, RouterOutlet } from '@angular/router';
import { UppercasePipe } from '../header-layout/pipes/UppercasePipe';
import { CurrencyPipe } from '../header-layout/pipes/CurrencyPipe';
import { ProductItems } from '../types/productItem';
import { NgClass, NgFor } from '@angular/common';


@Component({
  selector: 'app-product-item',
  standalone: true,
  imports: [
    RouterOutlet,
    FormsModule,
    CurrencyPipe,
    UppercasePipe,
    NgFor,
    NgClass,
    RouterLink,
  ],
  templateUrl: './productItem.component.html',
  styleUrls: ['./productItem.component.css', ],
})
export class ProductItemComponent implements OnChanges, OnDestroy {
    @Input() products: ProductItems[] = [];

    @Output() dataEvent = new EventEmitter<number>();

      currentYear: number = new Date().getFullYear();

    get totalPrice(): string{
      const sum = this.products.reduce((total, item) => {
        return total + item.price;
      }, 0);

      return `Total price ${sum}`;
    }
    ngOnChanges(changes: SimpleChanges): void {
        console.log(changes['products'].currentValue);
        console.log(changes['products'].previousValue);
    }
    ngOnDestroy(): void {
        console.log('Conponent removed')
    }

    handleDelete = (id: number) => {
        this.dataEvent.emit(id);
    }
}
