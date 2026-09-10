import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { RouterModule } from '@angular/router';

import { ServiceCardComponent } from '../../components/service-card/service-card.component';
import { FavoriteService } from '../../services/favorite/favorite.service';
import { ToastService } from '../../services/toast/toast.service';
import { FavoriteResponseDTO } from '../../models/Favorite';
import { Service } from '../../models/Service';

@Component({
  selector: 'app-favorites',
  imports: [CommonModule, RouterModule, ServiceCardComponent],
  templateUrl: './favorites.component.html',
  styleUrl: './favorites.component.scss',
})
export class FavoritesComponent {
  private favoriteService = inject(FavoriteService);
  private toast = inject(ToastService);

  loading = true;
  services: Service[] = [];

  ngOnInit(): void {
    this.loadFavorites();
  }

  loadFavorites() {
    this.loading = true;
    this.favoriteService.findMine().subscribe({
      next: (favorites) => {
        this.services = favorites.map((f) => this.toService(f));
        this.loading = false;
      },
      error: (err) => {
        console.error('Erro ao carregar favoritos:', err);
        this.toast.showToast('Não foi possível carregar seus favoritos.', 'error');
        this.loading = false;
      },
    });
  }

  onRemoveFavorite(service: Service) {
    this.favoriteService.remove(service.id).subscribe({
      next: () => {
        this.services = this.services.filter((s) => s.id !== service.id);
        this.toast.showToast('Serviço removido dos favoritos.', 'info');
      },
      error: (err) => {
        console.error('Erro ao remover favorito:', err);
        this.toast.showToast('Não foi possível remover o favorito.', 'error');
      },
    });
  }

  private toService(favorite: FavoriteResponseDTO): Service {
    return {
      id: favorite.serviceId,
      title: favorite.title,
      description: favorite.description,
      price: favorite.price,
      provider: favorite.provider ?? '',
      durationInMinutes: favorite.durationInMinutes,
      image: favorite.image ?? '',
      averageRating: favorite.averageRating,
      reviewCount: favorite.reviewCount,
      active: favorite.active,
      category: favorite.category ?? '',
      tags: [],
    };
  }
}
