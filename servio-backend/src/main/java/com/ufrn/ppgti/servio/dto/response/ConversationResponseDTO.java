package com.ufrn.ppgti.servio.dto.response;

import java.time.LocalDateTime;

public class ConversationResponseDTO {

    private Long id;

    private Long serviceId;
    private String serviceTitle;

    private Long otherUserId;
    private String otherUserName;

    private String lastMessage;
    private LocalDateTime lastMessageAt;

    private long unreadCount;

    public ConversationResponseDTO(
            Long id,
            Long serviceId,
            String serviceTitle,
            Long otherUserId,
            String otherUserName,
            String lastMessage,
            LocalDateTime lastMessageAt,
            long unreadCount) {
        this.id = id;
        this.serviceId = serviceId;
        this.serviceTitle = serviceTitle;
        this.otherUserId = otherUserId;
        this.otherUserName = otherUserName;
        this.lastMessage = lastMessage;
        this.lastMessageAt = lastMessageAt;
        this.unreadCount = unreadCount;
    }

    public Long getId() {
        return this.id;
    }

    public Long getServiceId() {
        return this.serviceId;
    }

    public String getServiceTitle() {
        return this.serviceTitle;
    }

    public Long getOtherUserId() {
        return this.otherUserId;
    }

    public String getOtherUserName() {
        return this.otherUserName;
    }

    public String getLastMessage() {
        return this.lastMessage;
    }

    public LocalDateTime getLastMessageAt() {
        return this.lastMessageAt;
    }

    public long getUnreadCount() {
        return this.unreadCount;
    }
}
