import { inject, Injectable } from '@angular/core';
import { OrderCreateRequestDTO, OrderResponseDTO, OrderStatusEnum, OrderStatusUpdateRequestDTO } from '../../models/Order';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class OrderService {
  private http = inject(HttpClient);
  private readonly API_URL = `${environment.apiUrl}/orders`;

  findProviderOrders(): Observable<OrderResponseDTO[]> {
    return this.http.get<OrderResponseDTO[]>(`${this.API_URL}/provider`);
  }
  findMyOrders(): Observable<OrderResponseDTO[]> {
    return this.http.get<OrderResponseDTO[]>(`${this.API_URL}/my-orders`);
  }
  updateStatus(id: number, status: OrderStatusEnum): Observable<OrderResponseDTO> {
    const payload: OrderStatusUpdateRequestDTO = { status };
    return this.http.patch<OrderResponseDTO>(`${this.API_URL}/${id}/status`, payload);
  }
  create(dto: OrderCreateRequestDTO): Observable<OrderResponseDTO> {
    return this.http.post<OrderResponseDTO>(this.API_URL, dto);
  }
}