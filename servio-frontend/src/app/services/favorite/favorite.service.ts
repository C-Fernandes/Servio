import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Service } from '../../models/Service';

@Injectable({
  providedIn: 'root',
})
export class FavoriteService {
  private http = inject(HttpClient);
  private readonly API_URL = `${environment.apiUrl}/favorites`;

  findAll(): Observable<Service[]> {
    return this.http.get<Service[]>(this.API_URL);
  }

  add(serviceId: number): Observable<Service> {
    return this.http.post<Service>(`${this.API_URL}/${serviceId}`, {});
  }

  remove(serviceId: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${serviceId}`);
  }
}

