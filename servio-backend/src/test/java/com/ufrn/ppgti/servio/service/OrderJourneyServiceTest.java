package com.ufrn.ppgti.servio.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

import com.ufrn.ppgti.servio.dto.response.OrderJourneyResponseDTO;
import com.ufrn.ppgti.servio.model.Order;
import com.ufrn.ppgti.servio.model.OrderStatusHistory;
import com.ufrn.ppgti.servio.model.ProviderProfile;
import com.ufrn.ppgti.servio.model.User;
import com.ufrn.ppgti.servio.model.enums.OrderStatus;
import com.ufrn.ppgti.servio.model.enums.Role;
import com.ufrn.ppgti.servio.repository.OrderRepository;
import com.ufrn.ppgti.servio.repository.OrderStatusHistoryRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderJourneyService - SPEC-002 (RF-13 Jornada do Pedido)")
class OrderJourneyServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderStatusHistoryRepository historyRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private OrderJourneyService orderJourneyService;

    private static final Long CLIENT_ID = 10L;
    private static final Long PROVIDER_PROFILE_ID = 77L;
    private static final LocalDateTime T0 = LocalDateTime.of(2026, 9, 1, 10, 0);
    private static final LocalDateTime T1 = LocalDateTime.of(2026, 9, 1, 12, 30);
    private static final LocalDateTime T2 = LocalDateTime.of(2026, 9, 2, 9, 0);
    private static final LocalDateTime T3 = LocalDateTime.of(2026, 9, 2, 11, 0);

    @Test
    @DisplayName("Cenário 1: criação registra 'Solicitado' como DONE e 'Aceito' como CURRENT")
    void journey_pending_firstStepDone() {
        Order order = buildOrder(OrderStatus.PENDING, T0);
        asParticipantClient();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(historyRepository.findByOrderIdOrderByChangedAtAsc(1L)).thenReturn(List.of(
                history(order, OrderStatus.PENDING, T0)));

        OrderJourneyResponseDTO journey = orderJourneyService.getJourney(1L);

        assertEquals(OrderStatus.PENDING, journey.getCurrentStatus());
        assertEquals("DONE", journey.getSteps().get(0).getState());
        assertEquals(T0, journey.getSteps().get(0).getReachedAt());
        assertEquals("CURRENT", journey.getSteps().get(1).getState());
        assertEquals("PENDING", journey.getSteps().get(2).getState());
        assertNull(journey.getCancelledAt());
    }

    @Test
    @DisplayName("Cenário 2: após CONFIRMED, 'Aceito' fica DONE e 'Em andamento' CURRENT")
    void journey_confirmed_acceptedDone() {
        Order order = buildOrder(OrderStatus.CONFIRMED, T0);
        asParticipantClient();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(historyRepository.findByOrderIdOrderByChangedAtAsc(1L)).thenReturn(List.of(
                history(order, OrderStatus.PENDING, T0),
                history(order, OrderStatus.CONFIRMED, T1)));

        OrderJourneyResponseDTO journey = orderJourneyService.getJourney(1L);

        assertEquals("DONE", journey.getSteps().get(1).getState());
        assertEquals(T1, journey.getSteps().get(1).getReachedAt());
        assertEquals("CURRENT", journey.getSteps().get(2).getState());
    }

    @Test
    @DisplayName("Cenário 3: pedido concluído tem as quatro etapas DONE com data/hora")
    void journey_completed_allDone() {
        Order order = buildOrder(OrderStatus.COMPLETED, T0);
        asParticipantClient();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(historyRepository.findByOrderIdOrderByChangedAtAsc(1L)).thenReturn(List.of(
                history(order, OrderStatus.PENDING, T0),
                history(order, OrderStatus.CONFIRMED, T1),
                history(order, OrderStatus.IN_PROGRESS, T2),
                history(order, OrderStatus.COMPLETED, T3)));

        OrderJourneyResponseDTO journey = orderJourneyService.getJourney(1L);

        assertEquals(4, journey.getSteps().size());
        journey.getSteps().forEach(step -> {
            assertEquals("DONE", step.getState());
            assertNotNull(step.getReachedAt());
        });
        assertEquals(T3, journey.getSteps().get(3).getReachedAt());
    }

    @Test
    @DisplayName("Edge 1: pedido cancelado - etapas cumpridas DONE, seguintes SKIPPED, cancelledAt preenchido")
    void journey_cancelled_skipsRemaining() {
        Order order = buildOrder(OrderStatus.CANCELLED, T0);
        LocalDateTime cancelledAt = LocalDateTime.of(2026, 9, 1, 15, 0);
        asParticipantClient();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(historyRepository.findByOrderIdOrderByChangedAtAsc(1L)).thenReturn(List.of(
                history(order, OrderStatus.PENDING, T0),
                history(order, OrderStatus.CANCELLED, cancelledAt)));

        OrderJourneyResponseDTO journey = orderJourneyService.getJourney(1L);

        assertEquals("DONE", journey.getSteps().get(0).getState());
        assertEquals("SKIPPED", journey.getSteps().get(1).getState());
        assertEquals("SKIPPED", journey.getSteps().get(2).getState());
        assertEquals("SKIPPED", journey.getSteps().get(3).getState());
        assertEquals(cancelledAt, journey.getCancelledAt());
    }

    @Test
    @DisplayName("Edge 2: pedido legado sem histórico - jornada reconstruída a partir do status atual")
    void journey_legacyNoHistory_reconstructed() {
        Order order = buildOrder(OrderStatus.IN_PROGRESS, T0);
        asParticipantClient();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(historyRepository.findByOrderIdOrderByChangedAtAsc(1L)).thenReturn(List.of());

        OrderJourneyResponseDTO journey = orderJourneyService.getJourney(1L);

        assertEquals("DONE", journey.getSteps().get(0).getState());
        assertEquals("DONE", journey.getSteps().get(1).getState());
        assertEquals("DONE", journey.getSteps().get(2).getState());
        assertEquals("CURRENT", journey.getSteps().get(3).getState());
        assertEquals(T0, journey.getSteps().get(0).getReachedAt());
        assertEquals(T0, journey.getSteps().get(2).getReachedAt());
    }

    @Test
    @DisplayName("Edge 3: usuário que não participa do pedido recebe 403")
    void journey_nonParticipant_throws403() {
        Order order = buildOrder(OrderStatus.PENDING, T0);
        User stranger = user(999L, Role.CLIENT);
        when(authService.getAuthenticadUser()).thenReturn(stranger);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> orderJourneyService.getJourney(1L));
        assertEquals(403, ex.getStatusCode().value());
    }

    @Test
    @DisplayName("Pedido inexistente retorna 404")
    void journey_orderNotFound_throws404() {
        when(authService.getAuthenticadUser()).thenReturn(user(CLIENT_ID, Role.CLIENT));
        when(orderRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> orderJourneyService.getJourney(1L));
        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    @DisplayName("Prestador do pedido também acessa a jornada")
    void journey_providerParticipant_ok() {
        Order order = buildOrder(OrderStatus.CONFIRMED, T0);
        User providerUser = user(50L, Role.PROVIDER);
        ProviderProfile profile = new ProviderProfile();
        profile.setId(PROVIDER_PROFILE_ID);
        providerUser.setProviderProfile(profile);
        when(authService.getAuthenticadUser()).thenReturn(providerUser);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(historyRepository.findByOrderIdOrderByChangedAtAsc(1L)).thenReturn(List.of(
                history(order, OrderStatus.PENDING, T0),
                history(order, OrderStatus.CONFIRMED, T1)));

        OrderJourneyResponseDTO journey = orderJourneyService.getJourney(1L);

        assertEquals(OrderStatus.CONFIRMED, journey.getCurrentStatus());
    }

    // ----- helpers -----

    private void asParticipantClient() {
        when(authService.getAuthenticadUser()).thenReturn(user(CLIENT_ID, Role.CLIENT));
    }

    private User user(Long id, Role role) {
        User user = new User();
        user.setId(id);
        user.setName("User " + id);
        user.setRole(role);
        return user;
    }

    private Order buildOrder(OrderStatus status, LocalDateTime createdAt) {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(status);
        order.setCreatedAt(createdAt);
        order.setClient(user(CLIENT_ID, Role.CLIENT));

        ProviderProfile provider = new ProviderProfile();
        provider.setId(PROVIDER_PROFILE_ID);
        order.setProvider(provider);

        return order;
    }

    private OrderStatusHistory history(Order order, OrderStatus status, LocalDateTime at) {
        OrderStatusHistory event = new OrderStatusHistory(order, status, Role.PROVIDER);
        event.setChangedAt(at);
        return event;
    }
}
