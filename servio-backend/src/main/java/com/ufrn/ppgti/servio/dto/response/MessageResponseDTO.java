package com.ufrn.ppgti.servio.dto.response;

import java.time.LocalDateTime;

public class MessageResponseDTO {

    private Long id;
    private Long senderId;
    private String senderName;
    private boolean mine;
    private String content;
    private LocalDateTime sentAt;

    public MessageResponseDTO(
            Long id,
            Long senderId,
            String senderName,
            boolean mine,
            String content,
            LocalDateTime sentAt) {
        this.id = id;
        this.senderId = senderId;
        this.senderName = senderName;
        this.mine = mine;
        this.content = content;
        this.sentAt = sentAt;
    }

    public Long getId() {
        return this.id;
    }

    public Long getSenderId() {
        return this.senderId;
    }

    public String getSenderName() {
        return this.senderName;
    }

    public boolean isMine() {
        return this.mine;
    }

    public String getContent() {
        return this.content;
    }

    public LocalDateTime getSentAt() {
        return this.sentAt;
    }
}
