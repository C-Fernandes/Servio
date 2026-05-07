import { CommonModule } from '@angular/common';
import { Component, input, output } from '@angular/core';
export interface OrderUI {
  id: number;
  service: string;
  client: string;
  date: string;
  amount: number;
  statusUI: string;
}
@Component({
  selector: 'app-order-details-modal',
  imports: [CommonModule],
  templateUrl: './order-details-modal.component.html',
  styleUrl: './order-details-modal.component.scss',
})
export class OrderDetailsModalComponent {
  order = input.required<OrderUI>();

  closeModal = output<void>();
  statusChange = output<{ id: number, newStatus: string }>();

  onClose() {
    this.closeModal.emit();
  }

  onStatusChange(event: Event) {
    const selectElement = event.target as HTMLSelectElement;
    this.statusChange.emit({
      id: this.order().id,
      newStatus: selectElement.value
    });
  }

}
