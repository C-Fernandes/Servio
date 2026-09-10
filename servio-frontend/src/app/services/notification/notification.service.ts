import { inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { NotificationResponseDTO } from '../../models/Notification';

@Injectable({
  providedIn: 'root',
})
export class NotificationService {
  private http = inject(HttpClient);

  private readonly API_URL = `${environment.apiUrl}/notifications`;

  /** Contador de não lidas, compartilhado com o sino. */
  unreadCount = signal<number>(0);

  list(): Observable<NotificationResponseDTO[]> {
    return this.http.get<NotificationResponseDTO[]>(this.API_URL);
  }

  fetchUnreadCount(): Observable<{ count: number }> {
    return this.http.get<{ count: number }>(`${this.API_URL}/unread-count`);
  }

  markRead(id: number): Observable<NotificationResponseDTO> {
    return this.http.patch<NotificationResponseDTO>(`${this.API_URL}/${id}/read`, {});
  }

  markAllRead(): Observable<{ updated: number }> {
    return this.http.patch<{ updated: number }>(`${this.API_URL}/read-all`, {});
  }

  /** Atualiza o contador de não lidas (chamado ao navegar / após ações). */
  refreshUnread(): void {
    this.fetchUnreadCount().subscribe({
      next: (res) => this.unreadCount.set(res.count),
      error: () => {},
    });
  }
}
