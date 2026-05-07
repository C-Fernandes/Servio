import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Tag, TagRequest } from '../../models/Tag';

@Injectable({
  providedIn: 'root',
})
export class TagService {
  private http = inject(HttpClient);

  private readonly API_URL = `${environment.apiUrl}/tags`;

  constructor() { }

  findAll(): Observable<Tag[]> {
    return this.http.get<Tag[]>(this.API_URL);
  }

  findById(id: number): Observable<Tag> {
    return this.http.get<Tag>(`${this.API_URL}/${id}`);
  }

  create(tagRequest: TagRequest): Observable<Tag> {
    return this.http.post<Tag>(this.API_URL, tagRequest);
  }

  update(id: number, tagRequest: TagRequest): Observable<Tag> {
    return this.http.put<Tag>(`${this.API_URL}/${id}`, tagRequest);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }
}