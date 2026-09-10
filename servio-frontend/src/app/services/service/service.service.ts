import { inject, Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs/internal/Observable';
import { Service, ServiceSearchFilters } from '../../models/Service';
import { Locality } from '../../models/Locality';

@Injectable({
  providedIn: 'root',
})
export class ServiceService {
  private http = inject(HttpClient);

  private readonly API_URL = `${environment.apiUrl}/services`;

  constructor() { }

  findMyServices(): Observable<any[]> {
    return this.http.get<any[]>(`${this.API_URL}/my-services`);
  }
  findAllActive(): Observable<Service[]> {
    return this.http.get<Service[]>(this.API_URL);
  }

  search(filters: ServiceSearchFilters): Observable<Service[]> {
    let params = new HttpParams();

    const term = filters.term?.trim();
    if (term) {
      params = params.set('term', term);
    }

    if (filters.categoryId != null) {
      params = params.set('categoryId', filters.categoryId);
    }

    if (filters.minPrice != null) {
      params = params.set('minPrice', filters.minPrice);
    }

    if (filters.maxPrice != null) {
      params = params.set('maxPrice', filters.maxPrice);
    }

    if (filters.minRating != null && filters.minRating > 0) {
      params = params.set('minRating', filters.minRating);
    }

    if (filters.city) {
      params = params.set('city', filters.city);
    }

    if (filters.state) {
      params = params.set('state', filters.state);
    }

    if (filters.sortBy) {
      params = params.set('sortBy', filters.sortBy);
    }

    return this.http.get<Service[]>(`${this.API_URL}/search`, { params });
  }

  findLocations(): Observable<Locality[]> {
    return this.http.get<Locality[]>(`${this.API_URL}/locations`);
  }

  findById(id: number): Observable<Service> {
    return this.http.get<Service>(`${this.API_URL}/${id}`);
  }

  create(formData: FormData): Observable<any> {
    return this.http.post<any>(this.API_URL, formData);
  }


  update(id: number, serviceData: any): Observable<any> {
    return this.http.put<any>(`${this.API_URL}/${id}`, serviceData);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  } toggleStatus(id: number) {
    return this.http.patch<any>(`${this.API_URL}/${id}/status`, {});
  }
}
