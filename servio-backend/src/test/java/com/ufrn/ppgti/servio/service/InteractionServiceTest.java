package com.ufrn.ppgti.servio.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.ufrn.ppgti.servio.dto.response.InteractionEventDTO;
import com.ufrn.ppgti.servio.model.Order;
import com.ufrn.ppgti.servio.model.OrderStatusHistory;
import com.ufrn.ppgti.servio.model.Review;
import com.ufrn.ppgti.servio.model.User;
import com.ufrn.ppgti.servio.model.enums.OrderStatus;
import com.ufrn.ppgti.servio.model.enums.Role;
import com.ufrn.ppgti.servio.repository.MessageRepository;
import com.ufrn.ppgti.servio.repository.OrderRepository;
import com.ufrn.ppgti.servio.repository.OrderStatusHistoryRepository;
import com.ufrn.ppgti.servio.repository.ReviewRepository;
import com.ufrn.ppgti.servio.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("InteractionService - SPEC-009 (RF-19 Histórico de Interações Cliente-Prestador)")
class InteractionServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderStatusHistoryRepository orderStatusHistoryRepository;
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private MessageRepository messageRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AuthService authService;

    @InjectMocks
    private InteractionService interactionService;

    private static final Long CLIENT_ID = 1L;
    private static final Long PROVIDER_ID = 2L;

    @Test
    @DisplayName("Cenário 1: histórico junta mensagens, pedido e avaliação em ordem cronológica")
    void getHistory_ordersReviewsAndMessages_returnsSortedMostRecentFirst() {
        User client = user(CLIENT_ID, Role.CLIENT);
        User provider = user(PROVIDER_ID, Role.PROVIDER);

        when(authService.getAuthenticadUser()).thenReturn(client);
        when(userRepository.findById(PROVIDER_ID)).thenReturn(Optional.of(provider));
        when(orderStatusHistoryRepository.findByOrder_Client_IdAndOrder_Provider_IdOrderByChangedAtAsc(CLIENT_ID,
                PROVIDER_ID)).thenReturn(List.of(history(1L, OrderStatus.COMPLETED, daysAgo(1))));
        when(orderRepository.findByClient_IdAndProvider_IdOrderByCreatedAtDesc(CLIENT_ID, PROVIDER_ID))
                .thenReturn(List.of());
        when(reviewRepository.findByClientIdAndProviderId(CLIENT_ID, PROVIDER_ID))
                .thenReturn(List.of(review(daysAgo(0))));
        when(messageRepository.findByConversation_Client_IdAndConversation_Provider_IdOrderBySentAtAsc(CLIENT_ID,
                PROVIDER_ID)).thenReturn(List.of());

        List<InteractionEventDTO> result = interactionService.getHistory(PROVIDER_ID);

        assertEquals(2, result.size());
        assertEquals("REVIEW", result.get(0).getType());
        assertEquals("ORDER_STATUS", result.get(1).getType());
        assertTrue(result.get(0).getTimestamp().isAfter(result.get(1).getTimestamp()));
    }

    @Test
    @DisplayName("Cenário 2: sem nenhuma interação prévia, o histórico retorna vazio sem erro")
    void getHistory_noInteractions_returnsEmptyList() {
        User client = user(CLIENT_ID, Role.CLIENT);
        User provider = user(PROVIDER_ID, Role.PROVIDER);

        when(authService.getAuthenticadUser()).thenReturn(client);
        when(userRepository.findById(PROVIDER_ID)).thenReturn(Optional.of(provider));
        when(orderStatusHistoryRepository.findByOrder_Client_IdAndOrder_Provider_IdOrderByChangedAtAsc(anyLong(),
                anyLong())).thenReturn(List.of());
        when(orderRepository.findByClient_IdAndProvider_IdOrderByCreatedAtDesc(anyLong(), anyLong()))
                .thenReturn(List.of());
        when(reviewRepository.findByClientIdAndProviderId(anyLong(), anyLong())).thenReturn(List.of());
        when(messageRepository.findByConversation_Client_IdAndConversation_Provider_IdOrderBySentAtAsc(anyLong(),
                anyLong())).thenReturn(List.of());

        List<InteractionEventDTO> result = interactionService.getHistory(PROVIDER_ID);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Cenário 3: prestador consulta o histórico e o sistema resolve o lado dele automaticamente")
    void getHistory_calledByProvider_resolvesClientProviderSides() {
        User provider = user(PROVIDER_ID, Role.PROVIDER);
        User client = user(CLIENT_ID, Role.CLIENT);

        when(authService.getAuthenticadUser()).thenReturn(provider);
        when(userRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));
        when(orderStatusHistoryRepository.findByOrder_Client_IdAndOrder_Provider_IdOrderByChangedAtAsc(CLIENT_ID,
                PROVIDER_ID)).thenReturn(List.of());
        when(orderRepository.findByClient_IdAndProvider_IdOrderByCreatedAtDesc(CLIENT_ID, PROVIDER_ID))
                .thenReturn(List.of());
        when(reviewRepository.findByClientIdAndProviderId(CLIENT_ID, PROVIDER_ID)).thenReturn(List.of());
        when(messageRepository.findByConversation_Client_IdAndConversation_Provider_IdOrderBySentAtAsc(CLIENT_ID,
                PROVIDER_ID)).thenReturn(List.of());

        interactionService.getHistory(CLIENT_ID);

        // A própria chamada aos repositórios com (CLIENT_ID, PROVIDER_ID) na ordem
        // certa, mesmo com o prestador autenticado, comprova a resolução de lado.
    }

    @Test
    @DisplayName("Edge 1: pedido legado sem histórico de status aparece sintetizado a partir do status atual")
    void getHistory_legacyOrderWithoutHistory_synthesizesEventFromCurrentStatus() {
        User client = user(CLIENT_ID, Role.CLIENT);
        User provider = user(PROVIDER_ID, Role.PROVIDER);
        Order legacyOrder = order(50L, OrderStatus.COMPLETED, daysAgo(10));

        when(authService.getAuthenticadUser()).thenReturn(client);
        when(userRepository.findById(PROVIDER_ID)).thenReturn(Optional.of(provider));
        when(orderStatusHistoryRepository.findByOrder_Client_IdAndOrder_Provider_IdOrderByChangedAtAsc(CLIENT_ID,
                PROVIDER_ID)).thenReturn(List.of());
        when(orderRepository.findByClient_IdAndProvider_IdOrderByCreatedAtDesc(CLIENT_ID, PROVIDER_ID))
                .thenReturn(List.of(legacyOrder));
        when(reviewRepository.findByClientIdAndProviderId(CLIENT_ID, PROVIDER_ID)).thenReturn(List.of());
        when(messageRepository.findByConversation_Client_IdAndConversation_Provider_IdOrderBySentAtAsc(CLIENT_ID,
                PROVIDER_ID)).thenReturn(List.of());

        List<InteractionEventDTO> result = interactionService.getHistory(PROVIDER_ID);

        assertEquals(1, result.size());
        assertEquals("ORDER_STATUS", result.get(0).getType());
        assertEquals(50L, result.get(0).getReferenceId());
    }

    @Test
    @DisplayName("Edge 2: usuário sem role compatível (ADMIN) é recusado")
    void getHistory_adminRole_throws403() {
        User admin = user(99L, Role.ADMIN);

        when(authService.getAuthenticadUser()).thenReturn(admin);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> interactionService.getHistory(PROVIDER_ID));
        assertEquals(403, ex.getStatusCode().value());
    }

    // ----- helpers -----

    private User user(Long id, Role role) {
        User user = new User();
        user.setId(id);
        user.setName("Usuário " + id);
        user.setRole(role);
        return user;
    }

    private LocalDateTime daysAgo(int days) {
        return LocalDateTime.now().minusDays(days);
    }

    private OrderStatusHistory history(Long orderId, OrderStatus status, LocalDateTime changedAt) {
        Order order = order(orderId, status, changedAt.minusHours(1));
        OrderStatusHistory history = new OrderStatusHistory(order, status, Role.CLIENT);
        history.setChangedAt(changedAt);
        return history;
    }

    private Order order(Long id, OrderStatus status, LocalDateTime createdAt) {
        Order order = new Order();
        order.setId(id);
        order.setStatus(status);
        order.setCreatedAt(createdAt);
        return order;
    }

    private Review review(LocalDateTime createdAt) {
        Review review = new Review();
        review.setId(1L);
        review.setRating(5);
        review.setComment("Excelente!");
        review.setCreatedAt(createdAt);
        return review;
    }
}
