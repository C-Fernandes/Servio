import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { ServiceCardComponent } from '../../components/service-card/service-card.component';
import { ServiceService } from '../../services/service/service.service';
import { CategoryService } from '../../services/category/category.service';
import { FavoriteService } from '../../services/favorite/favorite.service';
import { ToastService } from '../../services/toast/toast.service';
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
  private favoriteService = inject(FavoriteService);
  private toast = inject(ToastService);

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

  toggleFavorite(service: Service) {
    if (service.favorite) {
      this.favoriteService.remove(service.id).subscribe({
        next: () => this.updateFavoriteState(service, false),
        error: (err: unknown) => {
          console.error('Erro ao remover favorito:', err);
          this.toast.showToast('Não foi possível remover o favorito.', 'error');
        },
      });
      return;
    }

    this.favoriteService.add(service.id).subscribe({
      next: () => this.updateFavoriteState(service, true),
      error: (err: unknown) => {
        console.error('Erro ao adicionar favorito:', err);
        this.toast.showToast('Não foi possível adicionar o favorito.', 'error');
      },
    });
  }

  private updateFavoriteState(service: Service, favorite: boolean) {
    service.favorite = favorite;
    this.allServices = this.allServices.map((item) =>
      item.id === service.id ? { ...item, favorite } : item
    );
    this.applyFilters();

    const message = favorite
      ? 'Serviço adicionado aos favoritos.'
      : 'Serviço removido dos favoritos.';
    this.toast.showToast(message, 'success');
  }
}
