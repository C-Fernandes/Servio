package com.ufrn.ppgti.servio.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.ufrn.ppgti.servio.dto.response.NotificationResponseDTO;
import com.ufrn.ppgti.servio.model.Notification;
import com.ufrn.ppgti.servio.model.Order;
import com.ufrn.ppgti.servio.model.ProviderProfile;
import com.ufrn.ppgti.servio.model.Service;
import com.ufrn.ppgti.servio.model.User;
import com.ufrn.ppgti.servio.model.enums.OrderStatus;
import com.ufrn.ppgti.servio.model.enums.Role;
import com.ufrn.ppgti.servio.repository.NotificationRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService - SPEC-003 (RF-14 Notificações de status)")
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private NotificationService notificationService;

    private static final Long CLIENT_ID = 10L;
    private static final Long PROVIDER_USER_ID = 20L;

    @Test
    @DisplayName("Cenário 1: prestador avança o status -> cliente notificado, prestador não")
    void notify_providerActor_notifiesClientOnly() {
        Order order = order("Limpeza Residencial");
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        notificationService.notifyOrderStatusChange(order, OrderStatus.CONFIRMED, Role.PROVIDER);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(1)).save(captor.capture());
        Notification saved = captor.getValue();
        assertEquals(CLIENT_ID, saved.getRecipient().getId());
        assertTrue(saved.getMessage().contains("Limpeza Residencial"));
        assertTrue(saved.getMessage().contains("Aceito"));
        assertFalse(saved.isRead());
    }

    @Test
    @DisplayName("Cenário 2: cliente cancela -> prestador notificado, cliente não")
    void notify_clientActor_notifiesProviderOnly() {
        Order order = order("Pintura");
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        notificationService.notifyOrderStatusChange(order, OrderStatus.CANCELLED, Role.CLIENT);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(1)).save(captor.capture());
        assertEquals(PROVIDER_USER_ID, captor.getValue().getRecipient().getId());
        assertTrue(captor.getValue().getMessage().contains("Cancelado"));
    }

    @Test
    @DisplayName("Edge 1: mudança feita por ADMIN -> cliente e prestador notificados")
    void notify_adminActor_notifiesBoth() {
        Order order = order("Elétrica");
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        notificationService.notifyOrderStatusChange(order, OrderStatus.IN_PROGRESS, Role.ADMIN);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(2)).save(captor.capture());
        List<Long> recipientIds = captor.getAllValues().stream()
                .map(n -> n.getRecipient().getId())
                .toList();
        assertTrue(recipientIds.contains(CLIENT_ID));
        assertTrue(recipientIds.contains(PROVIDER_USER_ID));
    }

    @Test
    @DisplayName("Cenário 3: lista as próprias notificações (ordem vem do repositório)")
    void listMine_returnsMapped() {
        when(authService.getAuthenticadUser()).thenReturn(user(CLIENT_ID, Role.CLIENT));
        when(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(CLIENT_ID)).thenReturn(List.of(
                notification(1L, "msg 3", false),
                notification(2L, "msg 2", true),
                notification(3L, "msg 1", true)));

        List<NotificationResponseDTO> result = notificationService.listMine();

        assertEquals(3, result.size());
        assertEquals("msg 3", result.get(0).getMessage());
        assertFalse(result.get(0).isRead());
    }

    @Test
    @DisplayName("Cenário 3: contador de não lidas")
    void unreadCount_returnsRepositoryValue() {
        when(authService.getAuthenticadUser()).thenReturn(user(CLIENT_ID, Role.CLIENT));
        when(notificationRepository.countByRecipientIdAndReadFalse(CLIENT_ID)).thenReturn(2L);

        assertEquals(2L, notificationService.unreadCount());
    }

    @Test
    @DisplayName("Cenário 4: marcar como lida")
    void markAsRead_marksAndReturns() {
        User user = user(CLIENT_ID, Role.CLIENT);
        Notification notification = notification(5L, "msg", false);
        when(authService.getAuthenticadUser()).thenReturn(user);
        when(notificationRepository.findById(5L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        NotificationResponseDTO dto = notificationService.markAsRead(5L);

        assertTrue(dto.isRead());
        assertTrue(notification.isRead());
    }

    @Test
    @DisplayName("Edge 2: marcar como lida notificação de outro usuário -> 403 e sem alteração")
    void markAsRead_otherUsersNotification_throws403() {
        User user = user(CLIENT_ID, Role.CLIENT);
        Notification other = notification(5L, "msg", false);
        other.getRecipient().setId(999L);
        when(authService.getAuthenticadUser()).thenReturn(user);
        when(notificationRepository.findById(5L)).thenReturn(Optional.of(other));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> notificationService.markAsRead(5L));
        assertEquals(403, ex.getStatusCode().value());
        verify(notificationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Edge 3: marcar como lida de novo é idempotente (readAt não muda)")
    void markAsRead_alreadyRead_isIdempotent() {
        User user = user(CLIENT_ID, Role.CLIENT);
        Notification notification = notification(5L, "msg", true);
        LocalDateTime originalReadAt = LocalDateTime.of(2026, 9, 9, 10, 0);
        notification.setReadAt(originalReadAt);
        when(authService.getAuthenticadUser()).thenReturn(user);
        when(notificationRepository.findById(5L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        notificationService.markAsRead(5L);

        assertEquals(originalReadAt, notification.getReadAt());
    }

    @Test
    @DisplayName("Edge 4: marcar todas como lidas sem ter nenhuma -> 0, sem erro")
    void markAllAsRead_none_returnsZero() {
        when(authService.getAuthenticadUser()).thenReturn(user(CLIENT_ID, Role.CLIENT));
        when(notificationRepository.markAllAsRead(org.mockito.ArgumentMatchers.eq(CLIENT_ID), any())).thenReturn(0);

        assertEquals(0, notificationService.markAllAsRead());
    }

    @Test
    @DisplayName("Marcar como lida notificação inexistente -> 404")
    void markAsRead_notFound_throws404() {
        when(authService.getAuthenticadUser()).thenReturn(user(CLIENT_ID, Role.CLIENT));
        when(notificationRepository.findById(any())).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> notificationService.markAsRead(1L));
        assertEquals(404, ex.getStatusCode().value());
    }

    // ----- helpers -----

    private User user(Long id, Role role) {
        User user = new User();
        user.setId(id);
        user.setName("User " + id);
        user.setRole(role);
        return user;
    }

    private Order order(String serviceTitle) {
        Order order = new Order();
        order.setId(1L);
        order.setClient(user(CLIENT_ID, Role.CLIENT));

        User providerUser = user(PROVIDER_USER_ID, Role.PROVIDER);
        ProviderProfile profile = new ProviderProfile();
        profile.setId(77L);
        profile.setUser(providerUser);
        order.setProvider(profile);

        Service service = new Service();
        service.setId(3L);
        service.setTitle(serviceTitle);
        order.setService(service);

        return order;
    }

    private Notification notification(Long id, String message, boolean read) {
        Notification notification = new Notification(user(CLIENT_ID, Role.CLIENT), message, null);
        notification.setId(id);
        notification.setRead(read);
        notification.setCreatedAt(LocalDateTime.now());
        return notification;
    }
}
