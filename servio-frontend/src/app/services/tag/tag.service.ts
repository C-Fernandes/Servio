import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs/internal/Observable';
import { environment } from '../../../environments/environment';
import { Tag } from '../../models/Service';

@Injectable({ providedIn: 'root' })
export class TagService {
  private http = inject(HttpClient);
  private readonly API_URL = `${environment.apiUrl}/tags`;

  findAll(): Observable<Tag[]> {
    return this.http.get<Tag[]>(this.API_URL);
  }
}