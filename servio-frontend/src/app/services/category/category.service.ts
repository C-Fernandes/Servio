import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Category, CategoryRequest } from '../../models/Category';

@Injectable({
  providedIn: 'root',
})
export class CategoryService {
  private http = inject(HttpClient);

  private readonly API_URL = `${environment.apiUrl}/category`;

  constructor() { }

  findAll(): Observable<Category[]> {
    return this.http.get<Category[]>(this.API_URL);
  }

  findById(id: number): Observable<Category> {
    return this.http.get<Category>(`${this.API_URL}/${id}`);
  }

  create(categoryRequest: CategoryRequest): Observable<Category> {
    return this.http.post<Category>(this.API_URL, categoryRequest);
  }

  update(id: number, categoryRequest: CategoryRequest): Observable<Category> {
    return this.http.put<Category>(`${this.API_URL}/${id}`, categoryRequest);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }
}