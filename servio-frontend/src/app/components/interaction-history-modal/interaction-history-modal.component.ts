import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, inject } from '@angular/core';

import { InteractionService } from '../../services/interaction/interaction.service';
import { INTERACTION_ICONS, InteractionEventDTO } from '../../models/Interaction';

export interface InteractionTarget {
  userId: number;
  userName: string;
}

@Component({
  selector: 'app-interaction-history-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './interaction-history-modal.component.html',
  styleUrl: './interaction-history-modal.component.scss',
})
export class InteractionHistoryModalComponent implements OnChanges {
  private interactionService = inject(InteractionService);

  @Input({ required: true }) target!: InteractionTarget | null;

  @Output() close = new EventEmitter<void>();

  events: InteractionEventDTO[] = [];
  loading = false;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['target'] && this.target) {
      this.loadHistory(this.target.userId);
    }
  }

  loadHistory(userId: number): void {
    this.loading = true;
    this.events = [];

    this.interactionService.getHistory(userId).subscribe({
      next: (data) => {
        this.events = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Erro ao carregar histórico de interações:', err);
        this.loading = false;
      },
    });
  }

  onClose(): void {
    this.close.emit();
  }

  iconFor(type: string): string {
    return INTERACTION_ICONS[type as keyof typeof INTERACTION_ICONS] ?? 'circle';
  }
}
