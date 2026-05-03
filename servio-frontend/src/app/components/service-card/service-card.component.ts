import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { Service } from '../../models/Service';

@Component({
  selector: 'app-service-card',
  imports: [CommonModule],
  templateUrl: './service-card.component.html',
  styleUrl: './service-card.component.scss',
})
export class ServiceCardComponent {
  @Input({ required: true }) service!: Service;
}
