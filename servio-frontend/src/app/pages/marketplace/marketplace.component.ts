import { CommonModule } from '@angular/common';
import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { of, Subject } from 'rxjs';
import { catchError, debounceTime, switchMap, takeUntil } from 'rxjs/operators';

import { ServiceCardComponent } from '../../components/service-card/service-card.component';
import { ServiceService } from '../../services/service/service.service';
import { CategoryService } from '../../services/category/category.service';
import { FavoriteService } from '../../services/favorite/favorite.service';
import { AuthService } from '../../services/auth/auth.service';
import { ToastService } from '../../services/toast/toast.service';
import { Service, ServiceSearchFilters, ServiceSortOption } from '../../models/Service';
import { Category } from '../../models/Category';
import { Locality } from '../../models/Locality';

@Component({
  selector: 'app-marketplace',
  imports: [CommonModule, RouterModule, FormsModule, ServiceCardComponent],
  templateUrl: './marketplace.component.html',
  styleUrl: './marketplace.component.scss',
})
export class MarketplaceComponent implements OnInit, OnDestroy {
  private serviceService = inject(ServiceService);
  private categoryService = inject(CategoryService);
  private favoriteService = inject(FavoriteService);
  private authService = inject(AuthService);
  private toast = inject(ToastService);

  private readonly searchTrigger = new Subject<void>();
  private readonly destroy = new Subject<void>();

  categories: Category[] = [];
  locations: Locality[] = [];
  services: Service[] = [];

  loading = false;
  favoriteIds = new Set<number>();

  searchTerm = '';
  selectedCategoryId: number | null = null;
  minPrice: number | null = null;
  maxPrice: number | null = null;
  minRating = 0;
  selectedLocation = '';
  sortBy: ServiceSortOption = 'recent';

  get isClient(): boolean {
    return this.authService.getUserRole() === 'CLIENT';
  }

  ngOnInit(): void {
    this.loadCategories();
    this.loadLocations();
    this.loadFavorites();
    this.listenToSearch();
    this.search();
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

  ngOnDestroy(): void {
    this.destroy.next();
    this.destroy.complete();
  }

  search(): void {
    this.loading = true;
    this.searchTrigger.next();
  }

  clearFilters(): void {
    this.searchTerm = '';
    this.selectedCategoryId = null;
    this.minPrice = null;
    this.maxPrice = null;
    this.minRating = 0;
    this.selectedLocation = '';
    this.sortBy = 'recent';

    this.search();
  }

  get hasActiveFilters(): boolean {
    return (
      this.searchTerm.trim() !== '' ||
      this.selectedCategoryId !== null ||
      this.minPrice !== null ||
      this.maxPrice !== null ||
      Number(this.minRating) > 0 ||
      this.selectedLocation !== '' ||
      this.sortBy !== 'recent'
    );
  }

  // Cada digitação e cada ajuste de filtro passa por aqui: o debounce evita uma
  // requisição por tecla e o switchMap descarta respostas de buscas já superadas.
  private listenToSearch(): void {
    this.searchTrigger
      .pipe(
        debounceTime(300),
        switchMap(() =>
          this.serviceService.search(this.buildFilters()).pipe(
            catchError((err: unknown) => {
              console.error('Erro ao buscar serviços do marketplace:', err);
              this.toast.showToast('Não foi possível carregar os serviços.', 'error');
              return of<Service[]>([]);
            })
          )
        ),
        takeUntil(this.destroy)
      )
      .subscribe((data) => {
        this.services = data;
        this.loading = false;
      });
  }

  private buildFilters(): ServiceSearchFilters {
    const [city, state] = this.selectedLocation
      ? this.selectedLocation.split('|')
      : [null, null];

    return {
      term: this.searchTerm,
      categoryId: this.selectedCategoryId,
      minPrice: this.minPrice,
      maxPrice: this.maxPrice,
      minRating: Number(this.minRating),
      city,
      state,
      sortBy: this.sortBy,
    };
  }

  private loadCategories(): void {
    this.categoryService.findAll().subscribe({
      next: (data) => {
        this.categories = data;
      },
      error: (err: unknown) => {
        console.error('Erro ao buscar categorias:', err);
      },
    });
  }

  private loadLocations(): void {
    this.serviceService.findLocations().subscribe({
      next: (data) => {
        this.locations = data;
      },
      error: (err: unknown) => {
        console.error('Erro ao buscar localizações:', err);
      },
    });
  }
}
