import { CommonModule } from '@angular/common';
import { Component, effect, inject, input, signal } from '@angular/core';

import { OrderService } from '../../services/order/order.service';
import { OrderJourneyResponseDTO, OrderJourneyStepState } from '../../models/Order';

@Component({
  selector: 'app-order-journey',
  imports: [CommonModule],
  templateUrl: './order-journey.component.html',
  styleUrl: './order-journey.component.scss',
})
export class OrderJourneyComponent {
  private orderService = inject(OrderService);

  /** Pedido a exibir. */
  orderId = input.required<number>();
  /** Passar o status atual do pedido; ao mudar, a jornada é recarregada. */
  status = input<string | null>(null);

  journey = signal<OrderJourneyResponseDTO | null>(null);
  loading = signal<boolean>(true);
  error = signal<boolean>(false);

  constructor() {
    effect(() => {
      const id = this.orderId();
      this.status(); // dependência: recarrega quando o status muda
      this.fetch(id);
    });
  }

  private fetch(orderId: number) {
    this.loading.set(true);
    this.error.set(false);

    this.orderService.getJourney(orderId).subscribe({
      next: (data) => {
        this.journey.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Erro ao carregar a jornada do pedido:', err);
        this.error.set(true);
        this.loading.set(false);
      },
    });
  }

  iconFor(state: OrderJourneyStepState): string {
    switch (state) {
      case 'DONE':
        return 'check_circle';
      case 'CURRENT':
        return 'radio_button_checked';
      case 'SKIPPED':
        return 'remove_circle_outline';
      default:
        return 'radio_button_unchecked';
    }
  }
}
