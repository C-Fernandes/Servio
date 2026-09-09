import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { ServiceCardComponent } from '../../components/service-card/service-card.component';
import { ServiceService } from '../../services/service/service.service';
import { CategoryService } from '../../services/category/category.service';
import { FavoriteService } from '../../services/favorite/favorite.service';
import { AuthService } from '../../services/auth/auth.service';
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
  private authService = inject(AuthService);
  private toast = inject(ToastService);

  categories: Category[] = [];

  allServices: Service[] = [];
  services: Service[] = [];

  favoriteIds = new Set<number>();

  selectedCategory = '';
  maxPrice = 2000;
  minRating = 0;
  searchTerm = '';

  get isClient(): boolean {
    return this.authService.getUserRole() === 'CLIENT';
  }

  ngOnInit(): void {
    this.loadCategories();
    this.loadActiveServices();
    this.loadFavorites();
  }

  loadFavorites() {
    if (!this.isClient) {
      return;
    }

    this.favoriteService.findMine().subscribe({
      next: (favorites) => {
        this.favoriteIds = new Set(favorites.map((f) => f.serviceId));
      },
      error: (err) => {
        console.error('Erro ao carregar favoritos:', err);
      },
    });
  }

  onFavoriteToggle(service: Service) {
    if (this.favoriteIds.has(service.id)) {
      this.favoriteService.remove(service.id).subscribe({
        next: () => {
          this.favoriteIds.delete(service.id);
          this.favoriteIds = new Set(this.favoriteIds);
          this.toast.showToast('Serviço removido dos favoritos.', 'info');
        },
        error: (err: unknown) => this.handleFavoriteError(err),
      });
      return;
    }

    this.favoriteService.add(service.id).subscribe({
      next: () => {
        this.favoriteIds.add(service.id);
        this.favoriteIds = new Set(this.favoriteIds);
        this.toast.showToast('Serviço adicionado aos favoritos!', 'success');
      },
      error: (err: unknown) => this.handleFavoriteError(err),
    });
  }

  private handleFavoriteError(err: unknown) {
    console.error('Erro ao atualizar favorito:', err);
    this.toast.showToast('Não foi possível atualizar os favoritos.', 'error');
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