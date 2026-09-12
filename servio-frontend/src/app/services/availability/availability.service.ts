import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/internal/Observable';
import { Availability, Calendar } from '../../models/Availability';


@Injectable({
  providedIn: 'root'
})
export class AvailabilityService {
  private readonly API = `${environment.apiUrl}/api/calendar`;

  constructor(private http: HttpClient) { }

  getCalendar(): Observable<Calendar> {
    return this.http.get<Calendar>(`${this.API}/calendar`);
  }

  syncCalendar(request: Calendar): Observable<void> {
    return this.http.post<void>(`${this.API}/sync`, request);
  }

  listBlocks(): Observable<Availability[]> {
    return this.http.get<Availability[]>(`${this.API}/blocks`);
  }

  createBlock(payload: { specificDate: string; startTime: string; endTime: string }): Observable<Availability> {
    return this.http.post<Availability>(`${this.API}/blocks`, payload);
  }

  removeBlock(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API}/blocks/${id}`);
  }
}