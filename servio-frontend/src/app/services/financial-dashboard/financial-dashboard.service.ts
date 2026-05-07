import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { Observable } from 'rxjs';
import { ProviderFinancialDashboardResponseDTO } from '../../models/Dashboard';

@Injectable({
  providedIn: 'root',
})
@Injectable({
  providedIn: 'root',
})
export class FinancialDashboardService {
  private http = inject(HttpClient);
  private readonly API_URL = `${environment.apiUrl}/financial-dashboard`;

  getProviderDashboard(): Observable<ProviderFinancialDashboardResponseDTO> {
    return this.http.get<ProviderFinancialDashboardResponseDTO>(`${this.API_URL}/provider`);
  }
}