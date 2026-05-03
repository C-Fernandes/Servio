import { inject, Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/internal/Observable';

@Injectable({
  providedIn: 'root',
})
export class ServiceService {
  private http = inject(HttpClient);

  private readonly API_URL = `${environment.apiUrl}/services`;

  constructor() { }

  findAll(): Observable<any[]> {
    return this.http.get<any[]>(this.API_URL);
  }

  findById(id: number): Observable<any> {
    return this.http.get<any>(`${this.API_URL}/${id}`);
  }

  create(formData: FormData): Observable<any> {
    return this.http.post<any>(this.API_URL, formData);
  }


  update(id: number, serviceData: any): Observable<any> {
    return this.http.put<any>(`${this.API_URL}/${id}`, serviceData);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }
}