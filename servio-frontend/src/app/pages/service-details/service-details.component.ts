import { Component, computed, inject, signal } from '@angular/core';
import { ServiceService } from '../../services/service/service.service';
import { ActivatedRoute, Router } from '@angular/router';
import { Service } from '../../models/Service';
import { CommonModule } from '@angular/common';
import { OrderCreateRequestDTO } from '../../models/Order';
import { OrderService } from '../../services/order/order.service';
import { ToastService } from '../../services/toast/toast.service';
import { ReviewService } from '../../services/review/review.service';
import { ReviewResponseDTO } from '../../models/Review';

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
  private reviewService = inject(ReviewService);
  private router = inject(Router);
  private toast = inject(ToastService);

  serviceData = signal<Service | undefined>(undefined);
  reviews = signal<ReviewResponseDTO[]>([]);

  isLoading = signal(true);
  isReserving = signal(false);

  selectedSlot = signal<{ date: string; time: string } | null>(null);

  activeTab = signal<'description' | 'professional' | 'reviews'>('description');

  reviewCount = computed(() => this.reviews().length);

  averageRating = computed(() => {
    const reviews = this.reviews();

    if (reviews.length === 0) {
      return 0;
    }

    const total = reviews.reduce((sum, review) => sum + review.rating, 0);
    return Math.round((total / reviews.length) * 10) / 10;
  });

  setActiveTab(tab: 'description' | 'professional' | 'reviews') {
    this.activeTab.set(tab);
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');

    if (id) {
      const serviceId = Number(id);
      this.loadService(serviceId);
      this.loadReviews(serviceId);
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
      },
    });
  }

  loadReviews(serviceId: number) {
    this.reviewService.findByService(serviceId).subscribe({
      next: (data) => {
        this.reviews.set(data);
      },
      error: (err) => {
        console.error('Erro ao buscar avaliações:', err);
      },
    });
  }

  selectSlot(slot: { date: string; time: string }) {
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
      startTime: slot.time,
    };

    this.orderService.create(payload).subscribe({
      next: () => {
        this.isReserving.set(false);
        this.toast.showToast('Reserva realizada com sucesso!', 'success');
        this.router.navigate(['/client']);
      },
      error: (err) => {
        console.error('Erro ao criar pedido:', err);
        this.isReserving.set(false);
        this.toast.showToast(err.error.message, 'error');
      },
    });
  }

  translateDay(day?: string): string {
    if (!day) return '';

    const days: { [key: string]: string } = {
      SUNDAY: 'Domingo',
      MONDAY: 'Segunda-feira',
      TUESDAY: 'Terça-feira',
      WEDNESDAY: 'Quarta-feira',
      THURSDAY: 'Quinta-feira',
      FRIDAY: 'Sexta-feira',
      SATURDAY: 'Sábado',
    };

    return days[day] || day;
  }

  getStars(): number[] {
    return [1, 2, 3, 4, 5];
  }

  getReviewCountText(): string {
    const count = this.reviewCount();

    if (count === 0) {
      return 'Sem avaliações';
    }

    if (count === 1) {
      return '1 avaliação';
    }

    return `${count} avaliações`;
  }

  countReviewsByRating(rating: number): number {
    return this.reviews().filter((review) => review.rating === rating).length;
  }

  getRatingPercentage(rating: number): number {
    const total = this.reviewCount();

    if (total === 0) {
      return 0;
    }

    return (this.countReviewsByRating(rating) / total) * 100;
  }

  formatReviewDate(date?: string): string {
    if (!date) return '';

    const parsedDate = new Date(date);

    return parsedDate.toLocaleDateString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
    });
  }

  getInitials(name?: string): string {
    if (!name) return 'CL';

    const parts = name.trim().split(' ');

    if (parts.length === 1) {
      return parts[0].substring(0, 2).toUpperCase();
    }

    return `${parts[0][0]}${parts[parts.length - 1][0]}`.toUpperCase();
  }
}