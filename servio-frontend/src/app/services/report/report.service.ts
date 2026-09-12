import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  CreateReportRequestDTO,
  ReportResponseDTO,
  ReportStatus,
  UpdateReportStatusRequestDTO,
} from '../../models/Report';

@Injectable({
  providedIn: 'root',
})
export class ReportService {
  private http = inject(HttpClient);

  private readonly API_URL = `${environment.apiUrl}/reports`;

  create(dto: CreateReportRequestDTO): Observable<ReportResponseDTO> {
    return this.http.post<ReportResponseDTO>(this.API_URL, dto);
  }

  listMine(): Observable<ReportResponseDTO[]> {
    return this.http.get<ReportResponseDTO[]>(`${this.API_URL}/my`);
  }

  listAll(status?: ReportStatus): Observable<ReportResponseDTO[]> {
    let params = new HttpParams();
    if (status) {
      params = params.set('status', status);
    }
    return this.http.get<ReportResponseDTO[]>(this.API_URL, { params });
  }

  updateStatus(id: number, status: ReportStatus): Observable<ReportResponseDTO> {
    const payload: UpdateReportStatusRequestDTO = { status };
    return this.http.patch<ReportResponseDTO>(`${this.API_URL}/${id}/status`, payload);
  }
}
