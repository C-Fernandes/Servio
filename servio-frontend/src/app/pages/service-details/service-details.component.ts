import { Component, inject, signal } from '@angular/core';
import { ServiceService } from '../../services/service/service.service';
import { ActivatedRoute } from '@angular/router';
import { Service } from '../../models/Service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-service-details',
  imports: [CommonModule],
  templateUrl: './service-details.component.html',
  styleUrl: './service-details.component.scss',
})
export class ServiceDetailsComponent {
  private route = inject(ActivatedRoute);
  private serviceService = inject(ServiceService);

  serviceData = signal<Service | undefined>(undefined);
  isLoading = signal(true);

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
  } translateDay(day?: string): string {
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