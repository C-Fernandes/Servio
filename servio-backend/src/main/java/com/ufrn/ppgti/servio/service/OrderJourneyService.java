package com.ufrn.ppgti.servio.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ufrn.ppgti.servio.dto.response.OrderJourneyResponseDTO;
import com.ufrn.ppgti.servio.dto.response.OrderJourneyStepDTO;
import com.ufrn.ppgti.servio.model.Order;
import com.ufrn.ppgti.servio.model.OrderStatusHistory;
import com.ufrn.ppgti.servio.model.User;
import com.ufrn.ppgti.servio.model.enums.OrderStatus;
import com.ufrn.ppgti.servio.model.enums.Role;
import com.ufrn.ppgti.servio.repository.OrderRepository;
import com.ufrn.ppgti.servio.repository.OrderStatusHistoryRepository;

@Service
public class OrderJourneyService {

    /** Etapas canônicas da jornada, na ordem. */
    private static final String[][] CANONICAL = {
            { "REQUESTED", "Solicitado" },
            { "ACCEPTED", "Aceito" },
            { "IN_PROGRESS", "Em andamento" },
            { "COMPLETED", "Concluído" },
    };

    private static final OrderStatus[] CANONICAL_STATUS = {
            OrderStatus.PENDING,
            OrderStatus.CONFIRMED,
            OrderStatus.IN_PROGRESS,
            OrderStatus.COMPLETED,
    };

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final AuthService authService;

    public OrderJourneyService(
            OrderRepository orderRepository,
            OrderStatusHistoryRepository historyRepository,
            AuthService authService) {
        this.orderRepository = orderRepository;
        this.historyRepository = historyRepository;
        this.authService = authService;
    }

    @Transactional(readOnly = true)
    public OrderJourneyResponseDTO getJourney(Long orderId) {
        User user = authService.getAuthenticadUser();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado."));

        ensureParticipant(user, order);

        List<OrderStatusHistory> history = historyRepository.findByOrderIdOrderByChangedAtAsc(orderId);

        return build(order, history);
    }

    private void ensureParticipant(User user, Order order) {
        if (user.getRole() == Role.ADMIN) {
            return;
        }

        boolean isClient = order.getClient() != null
                && order.getClient().getId().equals(user.getId());

        boolean isProvider = user.getProviderProfile() != null
                && order.getProvider() != null
                && order.getProvider().getId().equals(user.getProviderProfile().getId());

        if (!isClient && !isProvider) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não participa deste pedido.");
        }
    }

    private OrderJourneyResponseDTO build(Order order, List<OrderStatusHistory> history) {
        Map<OrderStatus, LocalDateTime> firstReachedAt = new EnumMap<>(OrderStatus.class);
        for (OrderStatusHistory event : history) {
            firstReachedAt.putIfAbsent(event.getStatus(), event.getChangedAt());
        }

        LocalDateTime fallbackBase = order.getCreatedAt() != null ? order.getCreatedAt() : LocalDateTime.now();

        boolean cancelled = order.getStatus() == OrderStatus.CANCELLED;
        LocalDateTime cancelledAt = firstReachedAt.get(OrderStatus.CANCELLED);

        int reachedIndex = cancelled
                ? highestCanonicalReached(firstReachedAt)
                : canonicalIndexOf(order.getStatus());

        List<OrderJourneyStepDTO> steps = new ArrayList<>();
        for (int i = 0; i < CANONICAL.length; i++) {
            String state;
            if (cancelled) {
                state = i <= reachedIndex ? "DONE" : "SKIPPED";
            } else if (i <= reachedIndex) {
                state = "DONE";
            } else if (i == reachedIndex + 1) {
                state = "CURRENT";
            } else {
                state = "PENDING";
            }

            LocalDateTime reachedAt = null;
            if ("DONE".equals(state)) {
                reachedAt = firstReachedAt.getOrDefault(CANONICAL_STATUS[i], fallbackBase);
            }

            steps.add(new OrderJourneyStepDTO(CANONICAL[i][0], CANONICAL[i][1], state, reachedAt));
        }

        return new OrderJourneyResponseDTO(order.getId(), order.getStatus(), steps, cancelledAt);
    }

    /** Índice da etapa canônica correspondente ao status; -1 se não canônico. */
    private int canonicalIndexOf(OrderStatus status) {
        for (int i = 0; i < CANONICAL_STATUS.length; i++) {
            if (CANONICAL_STATUS[i] == status) {
                return i;
            }
        }
        return -1;
    }

    /** Maior etapa canônica já atingida segundo o histórico; 0 (Solicitado) como piso. */
    private int highestCanonicalReached(Map<OrderStatus, LocalDateTime> firstReachedAt) {
        int highest = 0;
        for (int i = 0; i < CANONICAL_STATUS.length; i++) {
            if (firstReachedAt.containsKey(CANONICAL_STATUS[i])) {
                highest = i;
            }
        }
        return highest;
    }
}
