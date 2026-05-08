import { Component, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

import { ClientFinancialDashboardResponseDTO } from '../../../models/Dashboard';
import { OrderResponseDTO, OrderStatusEnum } from '../../../models/Order';
import { ReviewCreateRequestDTO, ReviewResponseDTO } from '../../../models/Review';

import { FinancialDashboardService } from '../../../services/financial-dashboard/financial-dashboard.service';
import { OrderService } from '../../../services/order/order.service';
import { ReviewService } from '../../../services/review/review.service';

import { ReviewModalComponent } from '../../../components/review-modal/review-modal.component';

@Component({
  selector: 'app-dashboard-client',
  imports: [CommonModule, RouterLink, ReviewModalComponent],
  templateUrl: './dashboard-client.component.html',
  styleUrl: './dashboard-client.component.scss',
})
export class DashboardClientComponent {
  private orderService = inject(OrderService);
  private financialService = inject(FinancialDashboardService);
  private reviewService = inject(ReviewService);

  activeTab = signal<'reservas' | 'avaliacoes'>('reservas');

  stats = signal<ClientFinancialDashboardResponseDTO | null>(null);
  orders = signal<OrderResponseDTO[]>([]);
  reviews = signal<ReviewResponseDTO[]>([]);
  cancellingOrderId = signal<number | null>(null);
  selectedOrderToReview = signal<OrderResponseDTO | null>(null);

  mappedOrders = computed(() =>
    this.orders().map((order) => ({
      id: order.id,
      service: order.serviceTitle,
      provider: order.providerName,
      date: order.date,
      startTime: order.startTime,
      amount: order.servicePrice,
      status: order.status,
    }))
  );

  ngOnInit(): void {
    this.loadDashboard();
    this.loadOrders();
    this.loadReviews();
  }

  private loadDashboard() {
    this.financialService.getClientDashboard().subscribe({
      next: (data) => this.stats.set(data),
      error: (err) => console.error('Erro ao buscar dashboard:', err),
    });
  }

  private loadOrders() {
    this.orderService.findMyOrders().subscribe({
      next: (data) => this.orders.set(data),
      error: (err) => console.error('Erro ao buscar pedidos:', err),
    });
  }

  private loadReviews() {
    this.reviewService.findMyReviews().subscribe({
      next: (data) => this.reviews.set(data),
      error: (err) => console.error('Erro ao buscar avaliações:', err),
    });
  }

  setTab(tab: 'reservas' | 'avaliacoes') {
    this.activeTab.set(tab);
  }

  formatDateTime(date?: string, time?: string): string {
    if (!date) return '';

    const [year, month, day] = date.split('-');
    const formattedDate = `${day}/${month}/${year}`;

    if (!time) return formattedDate;

    return `${formattedDate}, ${time.slice(0, 5)}`;
  }

  formatReviewDate(date?: string): string {
    if (!date) return '';

    const parsedDate = new Date(date);

    return parsedDate.toLocaleString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  }
  getStatusLabel(status: OrderStatusEnum): string {
    const labels: Record<OrderStatusEnum, string> = {
      PENDING: 'Pendente',
      CONFIRMED: 'Confirmado',
      IN_PROGRESS: 'Em andamento',
      COMPLETED: 'Concluído',
      CANCELLED: 'Cancelado',
    };

    return labels[status];
  }

  getStatusClass(status: OrderStatusEnum): string {
    const classes: Record<OrderStatusEnum, string> = {
      PENDING: 'pending',
      CONFIRMED: 'confirmed',
      IN_PROGRESS: 'in-progress',
      COMPLETED: 'completed',
      CANCELLED: 'cancelled',
    };

    return classes[status];
  }

  canReview(status: OrderStatusEnum): boolean {
    return status === 'COMPLETED';
  }

  hasReviewForOrder(orderId: number): boolean {
    return this.reviews().some((review) => review.orderId === orderId);
  }

  openReviewModal(orderId: number) {
    const order = this.orders().find((item) => item.id === orderId);

    if (!order) return;

    this.selectedOrderToReview.set(order);
  }

  closeReviewModal() {
    this.selectedOrderToReview.set(null);
  }

  submitReview(dto: ReviewCreateRequestDTO) {
    this.reviewService.create(dto).subscribe({
      next: (createdReview) => {
        this.reviews.update((list) => [createdReview, ...list]);
        this.closeReviewModal();
      },
      error: (err) => {
        console.error('Erro ao enviar avaliação:', err);
        alert('Erro ao enviar avaliação.');
      },
    });
  }

  getStars(): number[] {
    return [1, 2, 3, 4, 5];
  } canCancel(status: OrderStatusEnum): boolean {
    return status === 'PENDING';
  }

  cancelOrder(orderId: number) {
    const confirmed = confirm('Tem certeza que deseja cancelar esta reserva?');

    if (!confirmed) return;

    this.cancellingOrderId.set(orderId);

    this.orderService.updateStatus(orderId, 'CANCELLED').subscribe({
      next: (updatedOrder) => {
        this.orders.update((list) =>
          list.map((order) => order.id === updatedOrder.id ? updatedOrder : order)
        );

        this.loadDashboard();
        this.cancellingOrderId.set(null);
      },
      error: (err) => {
        console.error('Erro ao cancelar pedido:', err);
        alert('Erro ao cancelar reserva.');
        this.cancellingOrderId.set(null);
      },
    });
  }
}