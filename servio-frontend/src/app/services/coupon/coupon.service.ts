import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { CouponCreateRequestDTO, CouponResponseDTO, CouponValidationResponseDTO } from '../../models/Coupon';

@Injectable({
  providedIn: 'root',
})
export class CouponService {
  private http = inject(HttpClient);

  private readonly API_URL = `${environment.apiUrl}/coupons`;

  create(dto: CouponCreateRequestDTO): Observable<CouponResponseDTO> {
    return this.http.post<CouponResponseDTO>(this.API_URL, dto);
  }

  findMine(): Observable<CouponResponseDTO[]> {
    return this.http.get<CouponResponseDTO[]>(`${this.API_URL}/my-coupons`);
  }

  deactivate(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  validate(code: string, serviceId: number): Observable<CouponValidationResponseDTO> {
    return this.http.get<CouponValidationResponseDTO>(`${this.API_URL}/validate`, {
      params: { code, serviceId },
    });
  }
}
