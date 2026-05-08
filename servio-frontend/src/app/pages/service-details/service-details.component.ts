import { Component, inject, signal } from '@angular/core';
import { ServiceService } from '../../services/service/service.service';
import { ActivatedRoute, Router } from '@angular/router';
import { Service } from '../../models/Service';
import { CommonModule } from '@angular/common';
import { OrderCreateRequestDTO } from '../../models/Order';
import { OrderService } from '../../services/order/order.service';
import { ToastService } from '../../services/toast/toast.service';

@Component({
  selector: 'app-service-details',
  imports: [CommonModule],
  templateUrl: './service-details.component.html',
  styleUrl: './service-details.component.scss',
})
export class ServiceDetailsComponent {
  private route = inject(ActivatedRoute);
  private serviceService = inject(ServiceService);
  private orderService = inject(OrderService);
  private router = inject(Router);
  private toast = inject(ToastService);

  serviceData = signal<Service | undefined>(undefined);
  isLoading = signal(true); isReserving = signal(false); selectedSlot = signal<{ date: string, time: string } | null>(null);
  activeTab = signal<'description' | 'professional' | 'reviews'>('description');

  setActiveTab(tab: 'description' | 'professional' | 'reviews') {
    this.activeTab.set(tab);
  }
  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadService(Number(id));
    }
  }

  loadService(id: number) {
    this.serviceService.findById(id).subscribe({
      next: (data) => {
        this.serviceData.set(data);
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Erro ao buscar serviço:', err);
        this.isLoading.set(false);
      }
    });
  }


  selectSlot(slot: { date: string, time: string }) {
    this.selectedSlot.set(slot);
  }
  reserve() {
    const slot = this.selectedSlot();
    const service = this.serviceData();

    if (!slot || !service || !service.id) {
      alert('Por favor, selecione um horário antes de reservar.');
      return;
    }

    this.isReserving.set(true);

    const payload: OrderCreateRequestDTO = {
      serviceId: service.id,
      date: slot.date,
      startTime: slot.time
    };

    this.orderService.create(payload).subscribe({
      next: (response) => {
        this.isReserving.set(false);
        this.toast.showToast('Reserva realizada com sucesso!', 'success');

        this.router.navigate(['/client']);
      },
      error: (err) => {
        console.error('Erro ao criar pedido:', err);
        this.isReserving.set(false);
        this.toast.showToast('Ocorreu um erro ao tentar reservar o horário.', 'error');
      }
    });
  }

  translateDay(day?: string): string {
    if (!day) return '';

    const days: { [key: string]: string } = {
      'SUNDAY': 'Domingo',
      'MONDAY': 'Segunda-feira',
      'TUESDAY': 'Terça-feira',
      'WEDNESDAY': 'Quarta-feira',
      'THURSDAY': 'Quinta-feira',
      'FRIDAY': 'Sexta-feira',
      'SATURDAY': 'Sábado'
    };

    return days[day] || day;
  }
}