import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
export interface AuthResponseDto {
  token: string;
  type: string;
  userId: number;
  role: string;
}
@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private http = inject(HttpClient);

  private readonly API_URL = 'http://localhost:8080/api/auth';
  private readonly TOKEN_KEY = 'servio_auth_token';

  isAuthenticated = signal<boolean>(this.hasToken());

  login(email: string, password: string): Observable<AuthResponseDto> {
    return this.http.post<AuthResponseDto>(`${this.API_URL}/login`, { email, password }).pipe(
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

  private hasToken(): boolean {
    return !!this.getToken();
  }
}