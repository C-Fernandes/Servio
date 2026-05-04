import { CommonModule } from '@angular/common';
import { Component, EventEmitter, inject, Input, Output } from '@angular/core';
import { Service } from '../../models/Service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-service-card',
  imports: [CommonModule],
  templateUrl: './service-card.component.html',
  styleUrl: './service-card.component.scss',
})
export class ServiceCardComponent {
  private router = inject(Router);
  @Input({ required: true }) service!: Service;
  @Input() variant: 'marketplace' | 'provider' = 'marketplace';

  @Output() editEvent = new EventEmitter<Service>();
  @Output() deleteEvent = new EventEmitter<Service>();
  @Output() toggleEvent = new EventEmitter<Service>();
  goToDetails() {
    if (this.variant === 'marketplace') {
      this.router.navigate(['/service/details', this.service.id]);
    }
  }
  onEdit() {
    this.editEvent.emit(this.service);
  }

  onDelete() {
    this.deleteEvent.emit(this.service);
  }

  onToggleStatus(event: any) {
    this.service.active = event.target.checked;
    this.toggleEvent.emit(this.service);
  }
}
