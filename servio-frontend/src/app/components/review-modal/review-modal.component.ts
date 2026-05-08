import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { OrderResponseDTO } from '../../models/Order';
import { ReviewCreateRequestDTO } from '../../models/Review';



@Component({
  selector: 'app-review-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './review-modal.component.html',
  styleUrl: './review-modal.component.scss',
})
export class ReviewModalComponent {
  @Input({ required: true }) order!: OrderResponseDTO | null;

  @Output() close = new EventEmitter<void>();
  @Output() submitReview = new EventEmitter<ReviewCreateRequestDTO>();

  rating = signal(0);
  hoverRating = signal(0);
  comment = '';

  setRating(value: number) {
    this.rating.set(value);
  }

  setHover(value: number) {
    this.hoverRating.set(value);
  }

  clearHover() {
    this.hoverRating.set(0);
  }

  isStarActive(star: number): boolean {
    const current = this.hoverRating() || this.rating();
    return star <= current;
  }

  onClose() {
    this.reset();
    this.close.emit();
  }

  onSubmit() {
    if (!this.order || this.rating() === 0) {
      return;
    }

    this.submitReview.emit({
      orderId: this.order.id,
      rating: this.rating(),
      comment: this.comment.trim(),
    });

    this.reset();
  }

  private reset() {
    this.rating.set(0);
    this.hoverRating.set(0);
    this.comment = '';
  }
}