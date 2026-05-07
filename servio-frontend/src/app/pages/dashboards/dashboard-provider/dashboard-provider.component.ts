import { Component, computed, inject, signal } from '@angular/core';
import { OrderResponseDTO } from '../../../models/Order';
import { OrderService } from '../../../services/order/order.service';
import { FinancialDashboardService } from '../../../services/financial-dashboard/financial-dashboard.service';
import { ProviderFinancialDashboardResponseDTO } from '../../../models/Dashboard';
import { CommonModule } from '@angular/common';
import { ServiceService } from '../../../services/service/service.service';
import { Service } from '../../../models/Service';
export interface TransactionDTO {
  id: number;
  title: string;
  date: string;
  amount: number;
  type: 'INCOME' | 'EXPENSE';
}
@Component({
  selector: 'app-dashboard-provider',
  imports: [CommonModule],
  templateUrl: './dashboard-provider.component.html',
  styleUrl: './dashboard-provider.component.scss',
})
export class DashboardProviderComponent {
  private orderService = inject(OrderService);
  private financialService = inject(FinancialDashboardService);
  private serviceService = inject(ServiceService);
  activeTab = signal<'pedidos' | 'servicos' | 'financas'>('pedidos');
  orders = signal<OrderResponseDTO[]>([]);
  financialDashboard = signal<ProviderFinancialDashboardResponseDTO | null>(null);
  providerServices = signal<Service[]>([]);
  statusCounts = computed(() => {
    const list = this.orders();
    return {
      pending: list.filter(o => o.status === 'PENDING').length,
      confirmed: list.filter(o => o.status === 'CONFIRMED').length,
      inProgress: list.filter(o => o.status === 'IN_PROGRESS').length,
      completed: list.filter(o => o.status === 'COMPLETED').length,
      cancelled: list.filter(o => o.status === 'CANCELLED').length,
    };
  }); pendingOrders = computed(() => this.orders().filter(o => o.status === 'PENDING'));
  inProgressOrders = computed(() => this.orders().filter(o => o.status === 'IN_PROGRESS'));
  completedOrders = computed(() => this.orders().filter(o => o.status === 'COMPLETED'));
  cancelledOrders = computed(() => this.orders().filter(o => o.status === 'CANCELLED'));

  ngOnInit(): void {
    this.loadDashboardData();
  }

  financialTransactions = computed(() => {
    const completed = this.completedOrders(); // Pega a fila de concluídos do Kanban
    const transactions: any[] = [];

    completed.forEach(order => {
      const grossAmount = order.servicePrice || 0;
      const feeAmount = grossAmount * 0.10; // 10% da plataforma

      // 1. Linha do Recebimento (90% do valor)
      transactions.push({
        id: `${order.id}-income`,
        type: 'INCOME',
        title: `Recebimento de serviço (${order.serviceTitle})`,
        date: order.date,
        amount: grossAmount * 0.90
      });

      // 2. Linha da Taxa (10% do valor)
      transactions.push({
        id: `${order.id}-fee`,
        type: 'EXPENSE',
        title: 'Taxa da plataforma (10%)',
        date: order.date,
        amount: -feeAmount
      });
    });

    // Opcional: Você pode querer colocar um .sort() aqui para ordenar por data
    return transactions;
  });

  loadDashboardData() {
    this.financialService.getProviderDashboard().subscribe({
      next: (data) => this.financialDashboard.set(data),
      error: (err) => console.error('Erro ao carregar resumo', err)
    });

    this.orderService.findProviderOrders().subscribe({
      next: (data) => this.orders.set(data),
      error: (err) => console.error('Erro ao carregar pedidos', err)
    });

    this.serviceService.findMyServices().subscribe({
      next: (data) => this.providerServices.set(data),
      error: (err) => console.error('Erro ao carregar serviços', err)
    });
  }
  setTab(tab: 'pedidos' | 'servicos' | 'financas') {
    this.activeTab.set(tab);
  }

  deleteService(id: number) {
    if (confirm('Tem certeza que deseja excluir este serviço?')) {
      this.serviceService.delete(id).subscribe({
        next: () => {
          this.providerServices.update(list => list.filter(s => s.id !== id));
        },
        error: (err) => console.error('Erro ao excluir', err)
      });
    }
  }
}