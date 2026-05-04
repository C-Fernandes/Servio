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

  serviceToEdit: any = null;
  private serviceService = inject(ServiceService);
  private toast = inject(ToastService);
  ngOnInit(): void {
    this.loadServices();
  }

  loadServices() {
    this.serviceService.findMyServices().subscribe({
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

    if (this.serviceToEdit) {

      this.serviceService.update(this.serviceToEdit.id, formData).subscribe({
        next: (updatedService) => {
          this.toast.showToast('Serviço atualizado com sucesso!', 'success');
          const index = this.services.findIndex(s => s.id === this.serviceToEdit.id);
          if (index !== -1) {
            this.services[index] = updatedService;
          }

          this.closeModal();
        },
        error: (err) => {
          this.toast.showToast('Erro ao atualizar serviço.', 'error');
          console.error('Erro ao atualizar serviço:', err);
        }
      });

    } else {
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
  } get activeServicesCount(): number {
    return this.services.filter(s => s.active).length;
  }
  openModal() {
    this.isModalOpen = true;
  }

  closeModal() {
    this.isModalOpen = false;
  } openEditModal(service: any) {
    console.log('Editing service:', service);
    this.serviceToEdit = service;

    // Future: pass 'serviceToEdit' to <app-service-modal> via @Input 
    // to pre-fill the form.
    this.isModalOpen = true;
  }

  deleteService(service: any) {
    if (confirm(`Tem certeza que deseja deletar o serviço "${service.title}"?`)) {

      // API integration example:
      /*
      this.serviceService.delete(service.id).subscribe({
        next: () => {
          this.services = this.services.filter(s => s.id !== service.id);
          this.toast.showToast('Serviço deletado com sucesso!', 'success');
        },
        error: (err) => {
          this.toast.showToast('Erro ao deletar serviço.', 'error');
          console.error('Error deleting service:', err);
        }
      });
      */

      // Temporary UI behavior:
      this.services = this.services.filter(s => s.id !== service.id);
      this.toast.showToast('Serviço removido (Apenas visualização)!', 'success');
    }
  }

  toggleStatus(service: any) {
    const newStatus = service.active;

    this.serviceService.toggleStatus(service.id).subscribe({
      next: (updatedService) => {
        service.active = updatedService.active;
        this.toast.showToast(`Serviço ${service.active ? 'ativado' : 'inativado'} com sucesso!`, 'success');
      },
      error: (err) => {
        service.active = !newStatus;

        this.toast.showToast('Erro ao alterar o status do serviço.', 'error');
        console.error('Erro ao atualizar status:', err);
      }
    });
  }


}