import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { ServiceCardComponent } from '../../components/service-card/service-card.component';
import { ServiceModalComponent } from '../../components/service-modal/service-modal.component';
import { ServiceService } from '../../services/service/service.service';
import { Service } from '../../models/Service';
import { ToastService } from '../../services/toast/toast.service';

@Component({
  selector: 'app-provider-services',
  imports: [CommonModule, ServiceCardComponent, ServiceModalComponent],
  templateUrl: './provider-services.component.html',
  styleUrl: './provider-services.component.scss',
})
export class ProviderServicesComponent {
  services: any[] = [];
  isModalOpen = false;
  private serviceService = inject(ServiceService);
  private toast = inject(ToastService);
  ngOnInit(): void {
    this.loadServices();
  }

  loadServices() {
    this.serviceService.findAll().subscribe({
      next: (data) => {
        this.services = data;
        console.log('Serviços carregados:', this.services);
      },
      error: (err) => {
        console.error('Erro ao carregar serviços:', err);
      }
    });
  }
  onServiceSaved(serviceData: any) {
    const formData = new FormData();

    const serviceRequestDTO = {
      title: serviceData.title,
      description: serviceData.description,
      price: serviceData.price,
      durationInMinutes: serviceData.durationInMinutes,

      category: Number(serviceData.category),

      tags: serviceData.tags
    };

    formData.append('service', new Blob([JSON.stringify(serviceRequestDTO)], {
      type: 'application/json'
    }));


    if (serviceData.imageFile) {
      formData.append('image', serviceData.imageFile);
    }

    this.serviceService.create(formData).subscribe({
      next: (newService) => {
        this.toast.showToast('Serviço criado com sucesso!', 'success');
        this.services.push(newService);
        this.closeModal();
      },
      error: (err) => {
        this.toast.showToast('Erro ao criar serviço. Por favor, tente novamente.', 'error');
        console.error('Erro ao salvar serviço:', err);
      }
    });
  }
  openModal() {
    this.isModalOpen = true;
  }

  closeModal() {
    this.isModalOpen = false;
  }


}