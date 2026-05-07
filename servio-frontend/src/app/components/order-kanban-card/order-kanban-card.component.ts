import { CommonModule } from '@angular/common';
import { Component, input, output, signal } from '@angular/core';
import { OrderDetailsModalComponent } from '../order-details-modal/order-details-modal.component';
export interface OrderCardData {
  id: number;
  service: string;
  client: string;
  date: string;
  amount: number;
  statusUI: string;
}
@Component({
  selector: 'app-order-kanban-card',
  imports: [CommonModule, OrderDetailsModalComponent],
  templateUrl: './order-kanban-card.component.html',
  styleUrl: './order-kanban-card.component.scss',
})
export class OrderKanbanCardComponent {
  order = input.required<OrderCardData>();
  isModalOpen = signal(false);
  statusChange = output<{ id: number, newStatus: string }>();
  viewDetails = output<number>();

  onStatusChange(event: Event) {
    const newStatus = (event.target as HTMLSelectElement).value;
    this.statusChange.emit({ id: this.order().id, newStatus });
  }

  onViewDetails() {
    this.viewDetails.emit(this.order().id);
  } openModal() {
    this.isModalOpen.set(true);
  }

  closeModal() {
    this.isModalOpen.set(false);
  }

  handleModalStatusChange(event: { id: number, newStatus: string }) {
    this.statusChange.emit(event);
  }
}