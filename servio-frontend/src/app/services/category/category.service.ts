import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs/internal/Observable';
import { environment } from '../../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Category } from '../../models/Service';
@Injectable({ providedIn: 'root' })
export class CategoryService {
  private http = inject(HttpClient);
  private readonly API_URL = `${environment.apiUrl}/category`;

  findAll(): Observable<Category[]> {
    return this.http.get<Category[]>(this.API_URL);
  }
}