import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface UserResponseDTO {
  id: number;
  name: string;
  email: string;
  role: string;
  phone?: string | null;
  street?: string | null;
  number?: string | null;
  complement?: string | null;
  neighborhood?: string | null;
  zipCode?: string | null;
  city?: string | null;
  state?: string | null;
}

export interface UserUpdateRequestDTO {
  name: string;
  phone: string | null;
  street: string | null;
  number: string | null;
  complement: string | null;
  neighborhood: string | null;
  zipCode: string | null;
  city: string | null;
  state: string | null;
}

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private http = inject(HttpClient);

  private readonly API_URL = `${environment.apiUrl}/users`;

  findAll(): Observable<UserResponseDTO[]> {
    return this.http.get<UserResponseDTO[]>(this.API_URL);
  }

  findById(id: number): Observable<UserResponseDTO> {
    return this.http.get<UserResponseDTO>(`${this.API_URL}/${id}`);
  }

  update(id: number, payload: UserUpdateRequestDTO): Observable<UserResponseDTO> {
    return this.http.put<UserResponseDTO>(`${this.API_URL}/${id}`, payload);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }
}
