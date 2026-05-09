import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { ServiceCardComponent } from '../../components/service-card/service-card.component';
import { ServiceService } from '../../services/service/service.service';
import { CategoryService } from '../../services/category/category.service';
import { Service } from '../../models/Service';
import { Category } from '../../models/Category';

@Component({
  selector: 'app-marketplace',
  imports: [CommonModule, RouterModule, FormsModule, ServiceCardComponent],
  templateUrl: './marketplace.component.html',
  styleUrl: './marketplace.component.scss',
})
export class MarketplaceComponent {
  private serviceService = inject(ServiceService);
  private categoryService = inject(CategoryService);

  categories: Category[] = [];

  allServices: Service[] = [];
  services: Service[] = [];

  selectedCategory = '';
  maxPrice = 2000;
  minRating = 0;
  searchTerm = '';

  ngOnInit(): void {
    this.loadCategories();
    this.loadActiveServices();
  }

  loadCategories() {
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
        this.allServices = data;
        this.applyFilters();

        console.log('Serviços ativos carregados:', this.allServices);
      },
      error: (err) => {
        console.error('Erro ao buscar serviços do marketplace:', err);
      }
    });
  }

  applyFilters() {
    let filtered = [...this.allServices];

    if (this.selectedCategory) {
      filtered = filtered.filter(service =>
        service.category === this.selectedCategory
      );
    }

    filtered = filtered.filter(service =>
      Number(service.price || 0) <= Number(this.maxPrice)
    );

    if (Number(this.minRating) > 0) {
      filtered = filtered.filter(service =>
        Number(service.averageRating || 0) >= Number(this.minRating)
      );
    }

    const term = this.searchTerm.trim().toLowerCase();

    if (term) {
      filtered = filtered.filter(service =>
        service.title?.toLowerCase().includes(term) ||
        service.description?.toLowerCase().includes(term)
      );
    }

    this.services = filtered;
  }
}