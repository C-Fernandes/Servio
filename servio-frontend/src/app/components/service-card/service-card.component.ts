import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Service } from '../../models/Service';

@Component({
  selector: 'app-service-card',
  imports: [CommonModule],
  templateUrl: './service-card.component.html',
  styleUrl: './service-card.component.scss',
})
export class ServiceCardComponent {
  @Input({ required: true }) service!: Service;
  @Input() variant: 'marketplace' | 'provider' = 'marketplace';

  @Output() editEvent = new EventEmitter<Service>();
  @Output() deleteEvent = new EventEmitter<Service>();
  @Output() toggleEvent = new EventEmitter<Service>();

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
