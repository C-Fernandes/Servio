export interface ConversationResponseDTO {
  id: number;
  serviceId: number;
  serviceTitle: string;
  otherUserId: number;
  otherUserName: string;
  lastMessage: string | null;
  lastMessageAt: string | null;
  unreadCount: number;
}

export interface MessageResponseDTO {
  id: number;
  senderId: number;
  senderName: string;
  mine: boolean;
  content: string;
  sentAt: string;
}

export interface StartConversationRequestDTO {
  serviceId: number;
}

export interface SendMessageRequestDTO {
  content: string;
}
