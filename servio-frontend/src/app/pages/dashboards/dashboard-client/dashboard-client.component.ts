import { Component, computed, inject, signal } from '@angular/core';
import { ClientFinancialDashboardResponseDTO } from '../../../models/Dashboard';
import { OrderResponseDTO } from '../../../models/Order';
import { FinancialDashboardService } from '../../../services/financial-dashboard/financial-dashboard.service';
import { OrderService } from '../../../services/order/order.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-dashboard-client',
  imports: [CommonModule],
  templateUrl: './dashboard-client.component.html',
  styleUrl: './dashboard-client.component.scss',
})
export class DashboardClientComponent {
  private orderService = inject(OrderService);
  private financialService = inject(FinancialDashboardService);

  activeTab = signal<'reservas' | 'transacoes'>('reservas');

  stats = signal<ClientFinancialDashboardResponseDTO | null>(null);
  orders = signal<OrderResponseDTO[]>([]);

  mappedOrders = computed(() => this.orders().map(o => ({
    id: o.id,
    service: o.serviceTitle || 'Serviço',
    client: o.providerName || 'Prestador',
    date: `${o.date} ${o.startTime}`,
    amount: o.servicePrice || 0,
    statusUI: this.mapStatusFromAPI(o.status),
    statusEnum: o.status
  })));
  expenses = computed(() => {
    return this.orders()
      .filter(o => o.status === 'COMPLETED')
      .map(o => ({
        id: o.id,
        title: `Pagamento: ${o.serviceTitle}`,
        provider: o.providerName,
        date: `${o.date}`,
        amount: o.servicePrice || 0
      }));
  });

  ngOnInit(): void {
    this.financialService.getClientDashboard().subscribe({
      next: (data) => this.stats.set(data),
      error: (err) => console.error(err)
    });

    this.orderService.findMyOrders().subscribe({
      next: (data) => this.orders.set(data),
      error: (err) => console.error(err)
    });
  }

  setTab(tab: 'reservas' | 'transacoes') {
    this.activeTab.set(tab);
  }

  updateOrderStatus(event: { id: number, newStatus: string }) {
    const statusEnum = event.newStatus === 'cancelled' ? 'CANCELLED' : null;
    if (statusEnum) {
      this.orderService.updateStatus(event.id, statusEnum as any).subscribe({
        next: (updatedOrder) => this.orders.update(list => list.map(o => o.id === event.id ? updatedOrder : o)),
        error: (err) => alert('Erro ao atualizar')
      });
    }
  }

  private mapStatusFromAPI(status: string): string {
    const map: Record<string, string> = {
      'PENDING': 'pending', 'CONFIRMED': 'confirmed', 'IN_PROGRESS': 'inProgress',
      'COMPLETED': 'completed', 'CANCELLED': 'cancelled'
    };
    return map[status] || 'pending';
  }
}
