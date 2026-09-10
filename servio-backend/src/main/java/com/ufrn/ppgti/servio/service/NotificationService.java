package com.ufrn.ppgti.servio.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ufrn.ppgti.servio.dto.response.NotificationResponseDTO;
import com.ufrn.ppgti.servio.model.Notification;
import com.ufrn.ppgti.servio.model.Order;
import com.ufrn.ppgti.servio.model.User;
import com.ufrn.ppgti.servio.model.enums.OrderStatus;
import com.ufrn.ppgti.servio.model.enums.Role;
import com.ufrn.ppgti.servio.repository.NotificationRepository;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final AuthService authService;

    public NotificationService(NotificationRepository notificationRepository, AuthService authService) {
        this.notificationRepository = notificationRepository;
        this.authService = authService;
    }

    /**
     * Cria uma notificação para cada participante do pedido, exceto o autor da
     * mudança. Executa em transação própria (REQUIRES_NEW) para que uma falha aqui
     * não reverta a mudança de status já persistida (melhor esforço — SPEC-003).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifyOrderStatusChange(Order order, OrderStatus newStatus, Role actorRole) {
        String message = "Seu pedido \"" + serviceTitleOf(order) + "\" mudou para: " + labelOf(newStatus);

        for (User recipient : resolveRecipients(order, actorRole)) {
            notificationRepository.save(new Notification(recipient, message, order));
        }
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> listMine() {
        User user = authService.getAuthenticadUser();
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public long unreadCount() {
        User user = authService.getAuthenticadUser();
        return notificationRepository.countByRecipientIdAndReadFalse(user.getId());
    }

    @Transactional
    public NotificationResponseDTO markAsRead(Long id) {
        User user = authService.getAuthenticadUser();

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notificação não encontrada."));

        if (!notification.getRecipient().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Notificação de outro usuário.");
        }

        notification.markAsRead();
        return toResponseDTO(notificationRepository.save(notification));
    }

    @Transactional
    public int markAllAsRead() {
        User user = authService.getAuthenticadUser();
        return notificationRepository.markAllAsRead(user.getId(), LocalDateTime.now());
    }

    private List<User> resolveRecipients(Order order, Role actorRole) {
        User client = order.getClient();
        User providerUser = order.getProvider() != null ? order.getProvider().getUser() : null;

        List<User> recipients = new ArrayList<>();
        if (actorRole != Role.CLIENT && client != null) {
            recipients.add(client);
        }
        if (actorRole != Role.PROVIDER && providerUser != null) {
            recipients.add(providerUser);
        }
        return recipients;
    }

    private String serviceTitleOf(Order order) {
        return order.getService() != null && order.getService().getTitle() != null
                ? order.getService().getTitle()
                : "pedido";
    }

    private String labelOf(OrderStatus status) {
        return switch (status) {
            case PENDING -> "Solicitado";
            case CONFIRMED -> "Aceito";
            case IN_PROGRESS -> "Em andamento";
            case COMPLETED -> "Concluído";
            case CANCELLED -> "Cancelado";
        };
    }

    private NotificationResponseDTO toResponseDTO(Notification notification) {
        Long orderId = notification.getOrder() != null ? notification.getOrder().getId() : null;
        return new NotificationResponseDTO(
                notification.getId(),
                notification.getMessage(),
                orderId,
                notification.isRead(),
                notification.getCreatedAt());
    }
}
