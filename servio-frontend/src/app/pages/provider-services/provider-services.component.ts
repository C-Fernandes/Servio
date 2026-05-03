import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { ServiceCardComponent } from '../../components/service-card/service-card.component';
import { ServiceModalComponent } from '../../components/service-modal/service-modal.component';
import { ServiceService } from '../../services/service/service.service';

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
  ngOnInit(): void {
    this.loadServices();
  }

  loadServices() {
    this.serviceService.findAll().subscribe({
      next: (data) => {
        this.services = data;
      },
      error: (err) => {
        console.error('Erro ao carregar serviços:', err);
      }
    });
  }
  onServiceSaved(serviceData: any) {
    // 1. Cria o FormData para enviar texto e arquivo juntos
    const formData = new FormData();

    // 2. Monta o objeto no formato exato que o backend (ServiceRequestDTO) espera
    const serviceRequestDTO = {
      title: serviceData.title,
      description: serviceData.description,
      price: serviceData.price,
      provider: 1, // Usando 1 provisoriamente (como discutimos antes)
      categories: [
        { id: Number(serviceData.category) }
      ]
    };

    formData.append('service', new Blob([JSON.stringify(serviceRequestDTO)], {
      type: 'application/json'
    }));

    // 4. Anexa a imagem (se o usuário tiver selecionado uma)
    if (serviceData.imageFile) {
      formData.append('image', serviceData.imageFile);
    }

    // 5. Envia para a API usando o service
    this.serviceService.create(formData).subscribe({
      next: (newService) => {
        // Quando o backend confirmar que salvou, adicionamos o novo serviço na lista 
        // para ele aparecer na tela imediatamente, sem precisar recarregar a página
        this.services.push(newService);
        this.closeModal();
      },
      error: (err) => {
        console.error('Erro ao salvar serviço:', err);
        // Aqui você pode adicionar um aviso na tela se quiser (ex: MatSnackBar)
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