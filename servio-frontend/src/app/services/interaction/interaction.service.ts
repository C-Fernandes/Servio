import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { InteractionEventDTO } from '../../models/Interaction';

@Injectable({
  providedIn: 'root',
})
export class InteractionService {
  private http = inject(HttpClient);

  private readonly API_URL = `${environment.apiUrl}/interactions`;

  getHistory(otherUserId: number): Observable<InteractionEventDTO[]> {
    return this.http.get<InteractionEventDTO[]>(`${this.API_URL}/${otherUserId}`);
  }
}
