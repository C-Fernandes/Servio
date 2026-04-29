import { Component } from '@angular/core';

@Component({
  selector: 'app-sidebar',
  imports: [],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss',
})
export class SidebarComponent {
  user = {
    name: 'Pedro Prestador',
    role: 'Prestador',
    photo: 'PE'
  };

  logout() {
    console.log('Saindo...');
  }
}
