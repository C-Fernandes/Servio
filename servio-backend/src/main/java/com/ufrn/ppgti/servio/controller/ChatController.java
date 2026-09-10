package com.ufrn.ppgti.servio.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ufrn.ppgti.servio.annotations.Client;
import com.ufrn.ppgti.servio.dto.request.SendMessageRequestDTO;
import com.ufrn.ppgti.servio.dto.request.StartConversationRequestDTO;
import com.ufrn.ppgti.servio.dto.response.ConversationResponseDTO;
import com.ufrn.ppgti.servio.dto.response.MessageResponseDTO;
import com.ufrn.ppgti.servio.service.ChatService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @Client
    @PostMapping("/conversations")
    public ResponseEntity<ConversationResponseDTO> start(@RequestBody @Valid StartConversationRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(chatService.startConversation(dto.getServiceId()));
    }

    @Client
    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationResponseDTO>> listMine() {
        return ResponseEntity.ok(chatService.listMyConversations());
    }

    @Client
    @GetMapping("/conversations/{id}/messages")
    public ResponseEntity<List<MessageResponseDTO>> messages(@PathVariable Long id) {
        return ResponseEntity.ok(chatService.getMessages(id));
    }

    @Client
    @PostMapping("/conversations/{id}/messages")
    public ResponseEntity<MessageResponseDTO> send(@PathVariable Long id,
            @RequestBody @Valid SendMessageRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(chatService.sendMessage(id, dto.getContent()));
    }
}
