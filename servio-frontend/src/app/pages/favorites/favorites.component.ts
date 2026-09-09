import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { RouterModule } from '@angular/router';

import { ServiceCardComponent } from '../../components/service-card/service-card.component';
import { Service } from '../../models/Service';
import { FavoriteService } from '../../services/favorite/favorite.service';
import { ToastService } from '../../services/toast/toast.service';

@Component({
  selector: 'app-favorites',
  imports: [CommonModule, RouterModule, ServiceCardComponent],
  templateUrl: './favorites.component.html',
  styleUrl: './favorites.component.scss',
})
export class FavoritesComponent {
  private favoriteService = inject(FavoriteService);
  private toast = inject(ToastService);

  favorites: Service[] = [];
  isLoading = true;

  ngOnInit(): void {
    this.loadFavorites();
  }

  loadFavorites(): void {
    this.isLoading = true;

    this.favoriteService.findAll().subscribe({
      next: (services) => {
        this.favorites = services.map((service) => ({ ...service, favorite: true }));
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Erro ao buscar favoritos:', err);
        this.toast.showToast('Não foi possível carregar seus favoritos.', 'error');
        this.isLoading = false;
      },
    });
  }

  removeFavorite(service: Service): void {
    this.favoriteService.remove(service.id).subscribe({
      next: () => {
        this.favorites = this.favorites.filter((item) => item.id !== service.id);
        this.toast.showToast('Serviço removido dos favoritos.', 'success');
      },
      error: (err) => {
        console.error('Erro ao remover favorito:', err);
        this.toast.showToast('Não foi possível remover o favorito.', 'error');
      },
    });
  }
}

