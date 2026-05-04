import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { RouterModule } from '@angular/router';
import { ServiceCardComponent } from '../../components/service-card/service-card.component';
import { ServiceService } from '../../services/service/service.service';
import { CategoryService } from '../../services/category/category.service';
import { Category, Service } from '../../models/Service';

@Component({
  selector: 'app-marketplace',
  imports: [CommonModule, RouterModule, ServiceCardComponent],
  templateUrl: './marketplace.component.html',
  styleUrl: './marketplace.component.scss',
})
export class MarketplaceComponent {
  private serviceService = inject(ServiceService);

  private categoryService = inject(CategoryService);
  categories: Category[] = [];

  services: Service[] = [];

  ngOnInit(): void {
    this.loadCategories();
    this.loadActiveServices();
  } loadCategories() {
    this.categoryService.findAll().subscribe({
      next: (data) => {
        this.categories = data;
      },
      error: (err) => {
        console.error('Erro ao buscar categorias:', err);
      }
    });
  }

  loadActiveServices() {
    this.serviceService.findAllActive().subscribe({
      next: (data) => {
        this.services = data;
        console.log('Serviços ativos carregados:', this.services);
      },
      error: (err) => {
        console.error('Erro ao buscar serviços do marketplace:', err);
      }
    });
  }

  onCategoryChange(event: any) {
    const selectedId = event.target.value;

    if (!selectedId) {
      this.loadActiveServices();
      return;
    }

    this.serviceService.findAllActive().subscribe(allServices => {
      this.services = allServices.filter(s => s.categoryId === Number(selectedId));
    });
  }
}