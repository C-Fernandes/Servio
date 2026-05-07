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

  user: any = { name: 'Usuário', role: 'CLIENT', photo: 'US' };

  ngOnInit() {
    this.updateUserInfo();
  }
  updateUserInfo() {

    const name = this.authService.getUserName() || 'Usuário';
    const role = this.authService.getUserRole() || 'CLIENT';
    this.user = {
      name: name,
      role: role,
      photo: name.split(' ').map((n: string) => n[0]).join('').toUpperCase().slice(0, 2)
    };
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/auth/login']);
    this.toast.showToast('Logout realizado com sucesso!', 'success');
  }

  get isProviderOrAdmin(): boolean {
    const role = this.authService.getUserRole();
    return role === 'PROVIDER' || role === 'ADMIN';
  } get isAdmin(): boolean {
    const role = this.authService.getUserRole();
    return role === 'ADMIN';
  }
}
