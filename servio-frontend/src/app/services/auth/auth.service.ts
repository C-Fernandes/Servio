import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { Observable, tap } from 'rxjs'; import { environment } from '../../../environments/environment';
import { AuthResponse } from '../../models/User';
@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private http = inject(HttpClient);

  private readonly API_URL = `${environment.apiUrl}/auth`;
  private readonly TOKEN_KEY = 'token';

  isAuthenticated = signal<boolean>(!!localStorage.getItem(this.TOKEN_KEY));

  login(credentials: any): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/login`, credentials).pipe(
      tap((response) => {
        if (response.token) {
          this.saveToken(response.token);
          this.isAuthenticated.set(true);
        }
      })
    );
  }

  register(userData: any): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/register`, userData).pipe(
      tap((response) => {
        if (response.token) {
          this.saveToken(response.token);
          this.isAuthenticated.set(true);
        }
      })
    );
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    this.isAuthenticated.set(false);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  private saveToken(token: string): void {
    localStorage.setItem(this.TOKEN_KEY, token);
  }
  private decodeToken(): any {
    const token = localStorage.getItem('token');
    if (!token) return null;

    try {
      const payloadBase64 = token.split('.')[1];

      const base64 = payloadBase64.replace(/-/g, '+').replace(/_/g, '/');

      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split('')
          .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      );

      return JSON.parse(jsonPayload);
    } catch (e) {
      console.error('Erro crítico na decodificação do token:', e);
      return null;
    }
  }

  getUserName(): string | null {
    const payload = this.decodeToken();
    return payload ? payload.name : null;
  }

  getUserRole(): string | null {
    const payload = this.decodeToken();
    return payload ? payload.role : null;
  }
}