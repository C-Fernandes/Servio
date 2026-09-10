package com.ufrn.ppgti.servio.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ufrn.ppgti.servio.annotations.Client;
import com.ufrn.ppgti.servio.dto.response.NotificationResponseDTO;
import com.ufrn.ppgti.servio.service.NotificationService;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Client
    @GetMapping
    public ResponseEntity<List<NotificationResponseDTO>> listMine() {
        return ResponseEntity.ok(notificationService.listMine());
    }

    @Client
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> unreadCount() {
        return ResponseEntity.ok(Map.of("count", notificationService.unreadCount()));
    }

    @Client
    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponseDTO> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    @Client
    @PatchMapping("/read-all")
    public ResponseEntity<Map<String, Integer>> markAllAsRead() {
        return ResponseEntity.ok(Map.of("updated", notificationService.markAllAsRead()));
    }
}
