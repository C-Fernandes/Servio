import { Component, inject } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth/auth.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ToastService } from '../../services/toast/toast.service';

@Component({
  selector: 'app-sidebar',
  imports: [RouterModule],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss',
})
export class SidebarComponent {
  private authService = inject(AuthService);
  private router = inject(Router);
  private toast = inject(ToastService);
  user = {
    name: 'Usuário',
    role: this.authService.getUserRole() || 'CLIENT',
    photo: 'US'
  };

  logout() {
    this.authService.logout();
    this.router.navigate(['/auth/login']);
    this.toast.showToast('Logout realizado com sucesso!', 'success');
  }

  get isProviderOrAdmin(): boolean {
    const role = this.authService.getUserRole();
    return role === 'PROVIDER' || role === 'ADMIN';
  }
}
