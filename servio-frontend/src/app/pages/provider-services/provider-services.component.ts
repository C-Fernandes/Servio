import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { ServiceCardComponent } from '../../components/service-card/service-card.component';
import { ServiceModalComponent } from '../../components/service-modal/service-modal.component';

@Component({
  selector: 'app-provider-services',
  imports: [CommonModule, ServiceCardComponent, ServiceModalComponent],
  templateUrl: './provider-services.component.html',
  styleUrl: './provider-services.component.scss',
})
export class ProviderServicesComponent {
  services: any[] = [];
  isModalOpen = false;
  constructor() { }

  ngOnInit(): void {
    this.mockServices();
  }

  mockServices() {
    this.services = [
      {
        id: 1,
        title: 'Landing Page em React',
        description: 'Desenvolvimento de landing page responsiva e moderna.',
        price: 1200.00,
        active: true,
        imageUrl: 'assets/image_f1f1d4.jpg'
      },
      {
        id: 2,
        title: 'Identidade Visual Completa',
        description: 'Logo, paleta de cores e manual da marca.',
        price: 850.00,
        active: true
      }
    ];
  }

  openModal() {
    this.isModalOpen = true;
  }

  closeModal() {
    this.isModalOpen = false;
  }

  onServiceSaved(serviceData: any) {
    console.log('Dados recebidos do modal:', serviceData);
    this.closeModal();
  }
}