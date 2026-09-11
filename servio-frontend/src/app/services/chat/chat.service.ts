import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  ConversationResponseDTO,
  MessageResponseDTO,
  SendMessageRequestDTO,
  StartConversationRequestDTO,
} from '../../models/Chat';

@Injectable({
  providedIn: 'root',
})
export class ChatService {
  private http = inject(HttpClient);

  private readonly API_URL = `${environment.apiUrl}/chat`;

  startConversation(serviceId: number): Observable<ConversationResponseDTO> {
    const payload: StartConversationRequestDTO = { serviceId };
    return this.http.post<ConversationResponseDTO>(`${this.API_URL}/conversations`, payload);
  }

  listConversations(): Observable<ConversationResponseDTO[]> {
    return this.http.get<ConversationResponseDTO[]>(`${this.API_URL}/conversations`);
  }

  getMessages(conversationId: number): Observable<MessageResponseDTO[]> {
    return this.http.get<MessageResponseDTO[]>(`${this.API_URL}/conversations/${conversationId}/messages`);
  }

  sendMessage(conversationId: number, content: string): Observable<MessageResponseDTO> {
    const payload: SendMessageRequestDTO = { content };
    return this.http.post<MessageResponseDTO>(`${this.API_URL}/conversations/${conversationId}/messages`, payload);
  }
}
