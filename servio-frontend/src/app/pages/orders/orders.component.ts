import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { OrderResponseDTO, OrderStatusEnum } from '../../models/Order';
import { OrderService } from '../../services/order/order.service';
import { CommonModule } from '@angular/common';
import { ToastService } from '../../services/toast/toast.service';
import { OrderDetailsModalComponent } from '../../components/order-details-modal/order-details-modal.component';
import { OrderKanbanCardComponent } from '../../components/order-kanban-card/order-kanban-card.component';
interface OrderUI {
  id: number;
  service: string;
  client: string;
  date: string;
  amount: number;
  statusUI: 'pending' | 'confirmed' | 'inProgress' | 'completed' | 'cancelled'; statusEnum: OrderStatusEnum;
}
@Component({
  selector: 'app-orders',
  imports: [CommonModule, OrderDetailsModalComponent, OrderKanbanCardComponent],
  templateUrl: './orders.component.html',
  styleUrl: './orders.component.scss',
})
export class OrdersComponent implements OnInit {
  private orderService = inject(OrderService);
  private toast = inject(ToastService);
  selectedOrderId = signal<number | null>(null);
  viewMode = signal<'kanban' | 'list'>('kanban');
  orders = signal<OrderUI[]>([]);

  totals = computed(() => {
    const list = this.orders();
    return {
      pending: list.filter(o => o.statusUI === 'pending').length, confirmed: list.filter(o => o.statusUI === 'confirmed').length,
      inProgress: list.filter(o => o.statusUI === 'inProgress').length,
      completed: list.filter(o => o.statusUI === 'completed').length,
      cancelled: list.filter(o => o.statusUI === 'cancelled').length,
    };
  });

  pendingOrders = computed(() => this.orders().filter(o => o.statusUI === 'pending')); confirmedOrders = computed(() => this.orders().filter(o => o.statusUI === 'confirmed'));
  inProgressOrders = computed(() => this.orders().filter(o => o.statusUI === 'inProgress'));
  completedOrders = computed(() => this.orders().filter(o => o.statusUI === 'completed'));
  cancelledOrders = computed(() => this.orders().filter(o => o.statusUI === 'cancelled'));

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders() {
    this.orderService.findProviderOrders().subscribe({
      next: (apiData) => {
        const mappedOrders = apiData.map(dto => this.mapDtoToUI(dto));
        this.orders.set(mappedOrders);
      },
      error: (err) => console.error('Error fetching orders', err)
    });
  }

  updateStatus(orderId: number, newStatusUI: string) {
    const newStatusEnum = this.mapStatusToAPI(newStatusUI);

    this.orderService.updateStatus(orderId, newStatusEnum).subscribe({
      next: (updatedOrder) => {
        this.orders.update(currentList =>
          currentList.map(o =>
            o.id === orderId ? this.mapDtoToUI(updatedOrder) : o
          )
        );
        this.toast.showToast('Status atualizado com sucesso!', 'success');
      },
      error: (err) => {
        const errorMessage = err.error?.message || 'Erro ao atualizar';
        this.toast.showToast(errorMessage, 'error');
        this.loadOrders();
      }
    });
  }

  selectedOrderDetails = computed(() => {
    const id = this.selectedOrderId();
    return id ? this.orders().find(o => o.id === id) || null : null;
  }); openDetails(orderId: number) {
    this.selectedOrderId.set(orderId);
  }

  closeDetails() {
    this.selectedOrderId.set(null);
  }
  setViewMode(mode: 'kanban' | 'list') {
    this.viewMode.set(mode);
  }

  getStatusLabel(statusUI: string): string {
    const labels: Record<string, string> = {
      pending: 'Pendente', confirmed: 'Confirmado',
      inProgress: 'Em andamento',
      completed: 'Concluído',
      cancelled: 'Cancelado'
    };
    return labels[statusUI] || statusUI;
  }

  handleModalStatusChange(event: { id: number, newStatus: string }) {
    this.updateStatus(event.id, event.newStatus);
  }

  private mapDtoToUI(dto: OrderResponseDTO): OrderUI {
    return {
      id: dto.id,
      service: dto.serviceTitle || 'Service not provided',
      client: dto.clientName || 'Anonymous client',
      date: `${dto.date} ${dto.startTime}`,
      amount: dto.servicePrice || 0,
      statusUI: this.mapStatusFromAPI(dto.status),
      statusEnum: dto.status
    };
  }

  private mapStatusFromAPI(status: OrderStatusEnum): OrderUI['statusUI'] {
    const map: Record<OrderStatusEnum, OrderUI['statusUI']> = {
      'PENDING': 'pending', 'CONFIRMED': 'confirmed',
      'IN_PROGRESS': 'inProgress',
      'COMPLETED': 'completed',
      'CANCELLED': 'cancelled'
    };
    return map[status] || 'pending';
  }

  private mapStatusToAPI(statusUI: string): OrderStatusEnum {
    const map: Record<string, OrderStatusEnum> = {
      'pending': 'PENDING',
      'confirmed': 'CONFIRMED',
      'inProgress': 'IN_PROGRESS',
      'completed': 'COMPLETED',
      'cancelled': 'CANCELLED'
    };
    return map[statusUI] || 'PENDING';
  }
}
