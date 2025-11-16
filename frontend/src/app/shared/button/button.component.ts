import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-button',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './button.component.html',
  styleUrls: ['./button.component.css']
})
export class ButtonComponent {
  @Input() variant: 'primary' | 'secondary' | 'ghost' = 'primary';
  @Input() size: 'sm' | 'md' | 'lg' = 'md';
  @Input() disabled = false;
  @Input() type: 'button' | 'submit' | 'reset' = 'button';
  @Input() fullWidth = false;

  @Output() pressed = new EventEmitter<Event>();

  onClick(e: Event) {
    if (this.disabled) {
      e.preventDefault();
      return;
    }
    this.pressed.emit(e);
  }

  hostClasses() {
    return [
      `btn`,
      `btn--${this.variant}`,
      `btn--${this.size}`,
      this.fullWidth ? 'btn--full' : ''
    ].filter(Boolean).join(' ');
  }
}
