import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { FavoriteResponseDTO } from '../../models/Favorite';

@Injectable({
  providedIn: 'root',
})
export class FavoriteService {
  private http = inject(HttpClient);

  private readonly API_URL = `${environment.apiUrl}/favorites`;

  add(serviceId: number): Observable<FavoriteResponseDTO> {
    return this.http.post<FavoriteResponseDTO>(`${this.API_URL}/${serviceId}`, {});
  }

  remove(serviceId: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${serviceId}`);
  }

  findMine(): Observable<FavoriteResponseDTO[]> {
    return this.http.get<FavoriteResponseDTO[]>(this.API_URL);
  }

  check(serviceId: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.API_URL}/check/${serviceId}`);
  }
}
