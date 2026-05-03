import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-sidebar',
  imports: [RouterModule],
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
