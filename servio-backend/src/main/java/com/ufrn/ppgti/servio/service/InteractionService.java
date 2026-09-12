package com.ufrn.ppgti.servio.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ufrn.ppgti.servio.dto.response.InteractionEventDTO;
import com.ufrn.ppgti.servio.model.Message;
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

@Service
public class InteractionService {

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final ReviewRepository reviewRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final AuthService authService;

    public InteractionService(
            OrderRepository orderRepository,
            OrderStatusHistoryRepository orderStatusHistoryRepository,
            ReviewRepository reviewRepository,
            MessageRepository messageRepository,
            UserRepository userRepository,
            AuthService authService) {
        this.orderRepository = orderRepository;
        this.orderStatusHistoryRepository = orderStatusHistoryRepository;
        this.reviewRepository = reviewRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.authService = authService;
    }

    @Transactional(readOnly = true)
    public List<InteractionEventDTO> getHistory(Long otherUserId) {
        User me = authService.getAuthenticadUser();

        if (me.getRole() != Role.CLIENT && me.getRole() != Role.PROVIDER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Apenas clientes e prestadores têm histórico de interações.");
        }

        User other = userRepository.findById(otherUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        Long clientId = me.getRole() == Role.CLIENT ? me.getId() : other.getId();
        Long providerId = me.getRole() == Role.PROVIDER ? me.getId() : other.getId();

        List<InteractionEventDTO> events = new ArrayList<>();

        addOrderEvents(events, clientId, providerId);
        addReviewEvents(events, clientId, providerId);
        addMessageEvents(events, clientId, providerId, me);

        events.sort(Comparator.comparing(InteractionEventDTO::getTimestamp).reversed());

        return events;
    }

    private void addOrderEvents(List<InteractionEventDTO> events, Long clientId, Long providerId) {
        List<Order> orders = orderRepository.findByClient_IdAndProvider_IdOrderByCreatedAtDesc(clientId, providerId);
        List<OrderStatusHistory> histories = orderStatusHistoryRepository
                .findByOrder_Client_IdAndOrder_Provider_IdOrderByChangedAtAsc(clientId, providerId);

        Set<Long> ordersWithHistory = new HashSet<>();
        for (OrderStatusHistory history : histories) {
            ordersWithHistory.add(history.getOrder().getId());
            String serviceTitle = serviceTitleOf(history.getOrder());
            events.add(new InteractionEventDTO(
                    "ORDER_STATUS",
                    history.getChangedAt(),
                    "Pedido \"" + serviceTitle + "\": " + labelOf(history.getStatus()),
                    history.getOrder().getId()));
        }

        // Pedidos legados, sem histórico registrado: sintetiza um único evento a
        // partir do status atual, para não desaparecerem da linha do tempo.
        for (Order order : orders) {
            if (!ordersWithHistory.contains(order.getId())) {
                events.add(new InteractionEventDTO(
                        "ORDER_STATUS",
                        order.getCreatedAt(),
                        "Pedido \"" + serviceTitleOf(order) + "\": " + labelOf(order.getStatus()),
                        order.getId()));
            }
        }
    }

    private void addReviewEvents(List<InteractionEventDTO> events, Long clientId, Long providerId) {
        List<Review> reviews = reviewRepository.findByClientIdAndProviderId(clientId, providerId);

        for (Review review : reviews) {
            String stars = review.getRating() + " estrela" + (review.getRating() == 1 ? "" : "s");
            String comment = review.getComment() != null && !review.getComment().isBlank()
                    ? " — \"" + review.getComment() + "\""
                    : "";

            events.add(new InteractionEventDTO(
                    "REVIEW",
                    review.getCreatedAt(),
                    "Avaliação (" + stars + ")" + comment,
                    review.getId()));
        }
    }

    private void addMessageEvents(List<InteractionEventDTO> events, Long clientId, Long providerId, User me) {
        List<Message> messages = messageRepository
                .findByConversation_Client_IdAndConversation_Provider_IdOrderBySentAtAsc(clientId, providerId);

        for (Message message : messages) {
            boolean mine = message.getSender().getId().equals(me.getId());
            String senderLabel = mine ? "Você" : message.getSender().getName();

            events.add(new InteractionEventDTO(
                    "MESSAGE",
                    message.getSentAt(),
                    senderLabel + ": " + message.getContent(),
                    message.getId()));
        }
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
}
