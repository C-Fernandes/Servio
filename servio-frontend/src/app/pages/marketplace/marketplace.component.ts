import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { ServiceCardComponent } from '../../components/service-card/service-card.component';

@Component({
  selector: 'app-marketplace',
  imports: [CommonModule, RouterModule, ServiceCardComponent],
  templateUrl: './marketplace.component.html',
  styleUrl: './marketplace.component.scss',
})
export class MarketplaceComponent {
  categories = ['Design & Criação', 'Desenvolvimento', 'Marketing Digital', 'Redação', 'Negócios'];

  services = [
    {
      title: 'Identidade Visual Completa',
      description: 'Logo, paleta de cores, tipografia e manual da marca.',
      provider: 'Pedro Prestador',
      category: 'Design & Criação',
      price: 850,
      rating: 5.0,
      reviews: 1,
      duration: '300min'
    },
    {
      title: 'Landing Page em React',
      description: 'Desenvolvimento de landing page responsiva e moderna.',
      provider: 'Pedro Prestador',
      category: 'Desenvolvimento',
      price: 1200,
      rating: 5.0,
      reviews: 1,
      duration: '240min'
    }
  ];
}