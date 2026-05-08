import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  ReviewCreateRequestDTO,
  ReviewResponseDTO,
  ReviewUpdateRequestDTO,
} from '../../models/Review';

@Injectable({
  providedIn: 'root',
})
export class ReviewService {
  private http = inject(HttpClient);

  private readonly API_URL = `${environment.apiUrl}/reviews`;

  create(dto: ReviewCreateRequestDTO): Observable<ReviewResponseDTO> {
    return this.http.post<ReviewResponseDTO>(this.API_URL, dto);
  }

  findMyReviews(): Observable<ReviewResponseDTO[]> {
    return this.http.get<ReviewResponseDTO[]>(`${this.API_URL}/my-reviews`);
  }

  findProviderReviews(): Observable<ReviewResponseDTO[]> {
    return this.http.get<ReviewResponseDTO[]>(`${this.API_URL}/provider`);
  }

  findByService(serviceId: number): Observable<ReviewResponseDTO[]> {
    return this.http.get<ReviewResponseDTO[]>(`${this.API_URL}/service/${serviceId}`);
  }

  findById(id: number): Observable<ReviewResponseDTO> {
    return this.http.get<ReviewResponseDTO>(`${this.API_URL}/${id}`);
  }

  update(id: number, dto: ReviewUpdateRequestDTO): Observable<ReviewResponseDTO> {
    return this.http.put<ReviewResponseDTO>(`${this.API_URL}/${id}`, dto);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }
}