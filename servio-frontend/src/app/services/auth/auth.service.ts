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
  private readonly TOKEN_KEY = 'servio_auth_token';

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
  getUserRole(): string | null {
    const token = localStorage.getItem('token');

    if (!token) {
      return null;
    }

    try {
      const payloadBase64Url = token.split('.')[1];
      const payloadBase64 = payloadBase64Url.replace(/-/g, '+').replace(/_/g, '/');
      const payloadDecoded = atob(payloadBase64);
      const payloadJson = JSON.parse(payloadDecoded);
      if (payloadJson.role) {
        return payloadJson.role.replace('ROLE_', '');
      }

      return null;
    } catch (error) {
      console.error('Erro ao tentar ler a role do token JWT:', error);
      return null;
    }
  }
}