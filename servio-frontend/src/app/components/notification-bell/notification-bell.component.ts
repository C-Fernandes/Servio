import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router } from '@angular/router';
import { filter } from 'rxjs';

import { NotificationService } from '../../services/notification/notification.service';
import { AuthService } from '../../services/auth/auth.service';
import { NotificationResponseDTO } from '../../models/Notification';

@Component({
  selector: 'app-notification-bell',
  imports: [CommonModule],
  templateUrl: './notification-bell.component.html',
  styleUrl: './notification-bell.component.scss',
})
export class NotificationBellComponent {
  private notificationService = inject(NotificationService);
  private authService = inject(AuthService);
  private router = inject(Router);

  open = signal(false);
  loading = signal(false);
  notifications = signal<NotificationResponseDTO[]>([]);

  unreadCount = this.notificationService.unreadCount;

  constructor() {
    this.notificationService.refreshUnread();

    this.router.events
      .pipe(
        filter((e) => e instanceof NavigationEnd),
        takeUntilDestroyed(),
      )
      .subscribe(() => this.notificationService.refreshUnread());
  }

  toggle() {
    this.open.update((v) => !v);
    if (this.open()) {
      this.loadList();
    }
  }

  close() {
    this.open.set(false);
  }

  private loadList() {
    this.loading.set(true);
    this.notificationService.list().subscribe({
      next: (data) => {
        this.notifications.set(data);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  onItemClick(item: NotificationResponseDTO) {
    if (!item.read) {
      this.notificationService.markRead(item.id).subscribe({
        next: () => {
          this.markLocalAsRead(item.id);
          this.notificationService.refreshUnread();
        },
        error: () => {},
      });
    }

    this.close();

    if (item.orderId) {
      const target = this.authService.getUserRole() === 'CLIENT' ? '/client' : '/provider/orders';
      this.router.navigate([target]);
    }
  }

  markAllRead() {
    this.notificationService.markAllRead().subscribe({
      next: () => {
        this.notifications.update((list) => list.map((n) => ({ ...n, read: true })));
        this.notificationService.refreshUnread();
      },
      error: () => {},
    });
  }

  private markLocalAsRead(id: number) {
    this.notifications.update((list) =>
      list.map((n) => (n.id === id ? { ...n, read: true } : n)),
    );
  }
}
