import { CommonModule } from '@angular/common';
import { Component, ElementRef, OnDestroy, OnInit, ViewChild, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Subject, interval } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { ChatService } from '../../services/chat/chat.service';
import { ToastService } from '../../services/toast/toast.service';
import { ConversationResponseDTO, MessageResponseDTO } from '../../models/Chat';

const POLL_INTERVAL_MS = 4000;

@Component({
  selector: 'app-messages',
  imports: [CommonModule, FormsModule],
  templateUrl: './messages.component.html',
  styleUrl: './messages.component.scss',
})
export class MessagesComponent implements OnInit, OnDestroy {
  private chatService = inject(ChatService);
  private toast = inject(ToastService);
  private route = inject(ActivatedRoute);

  private readonly destroy = new Subject<void>();
  private preselectId?: number;

  @ViewChild('messagesEnd') private messagesEnd?: ElementRef<HTMLDivElement>;

  conversations: ConversationResponseDTO[] = [];
  messages: MessageResponseDTO[] = [];
  selectedConversation: ConversationResponseDTO | null = null;

  loadingConversations = true;
  loadingMessages = false;
  sending = false;
  draft = '';

  ngOnInit(): void {
    const param = Number(this.route.snapshot.queryParamMap.get('conversationId'));
    this.preselectId = param || undefined;
    this.loadConversations();
    this.startPolling();
  }

  ngOnDestroy(): void {
    this.destroy.next();
    this.destroy.complete();
  }

  loadConversations(): void {
    this.chatService.listConversations().subscribe({
      next: (data) => {
        this.conversations = data;
        this.loadingConversations = false;

        if (!this.selectedConversation && data.length > 0) {
          const target = this.preselectId ? data.find((c) => c.id === this.preselectId) : undefined;
          this.selectConversation(target ?? data[0]);
        }
      },
      error: (err) => {
        console.error('Erro ao carregar conversas:', err);
        this.loadingConversations = false;
      },
    });
  }

  selectConversation(conversation: ConversationResponseDTO): void {
    this.selectedConversation = conversation;
    this.loadMessages(conversation.id);
  }

  loadMessages(conversationId: number): void {
    this.loadingMessages = true;
    this.chatService.getMessages(conversationId).subscribe({
      next: (data) => {
        this.messages = data;
        this.loadingMessages = false;
        this.clearUnread(conversationId);
        this.scrollToBottom();
      },
      error: (err) => {
        console.error('Erro ao carregar mensagens:', err);
        this.loadingMessages = false;
      },
    });
  }

  onEnterKey(event: Event): void {
    event.preventDefault();
    this.send();
  }

  send(): void {
    const content = this.draft.trim();
    const conversation = this.selectedConversation;

    if (!content || !conversation) {
      return;
    }

    this.sending = true;
    this.chatService.sendMessage(conversation.id, content).subscribe({
      next: (message) => {
        this.messages = [...this.messages, message];
        this.draft = '';
        this.sending = false;
        this.scrollToBottom();
        this.loadConversations();
      },
      error: (err) => {
        console.error('Erro ao enviar mensagem:', err);
        this.toast.showToast('Não foi possível enviar a mensagem.', 'error');
        this.sending = false;
      },
    });
  }

  private startPolling(): void {
    interval(POLL_INTERVAL_MS)
      .pipe(takeUntil(this.destroy))
      .subscribe(() => {
        this.loadConversations();
        if (this.selectedConversation) {
          this.pollMessages(this.selectedConversation.id);
        }
      });
  }

  private pollMessages(conversationId: number): void {
    this.chatService.getMessages(conversationId).subscribe({
      next: (data) => {
        const grew = data.length !== this.messages.length;
        this.messages = data;
        if (grew) {
          this.scrollToBottom();
        }
      },
      error: () => {
        // silencioso: não interrompe a conversa por uma falha pontual de polling
      },
    });
  }

  private clearUnread(conversationId: number): void {
    this.conversations = this.conversations.map((c) =>
      c.id === conversationId ? { ...c, unreadCount: 0 } : c
    );
  }

  private scrollToBottom(): void {
    setTimeout(() => {
      this.messagesEnd?.nativeElement.scrollIntoView({ behavior: 'smooth' });
    });
  }

  getInitials(name: string): string {
    const parts = name.trim().split(' ');

    if (parts.length === 1) {
      return parts[0].substring(0, 2).toUpperCase();
    }

    return `${parts[0][0]}${parts[parts.length - 1][0]}`.toUpperCase();
  }
}
