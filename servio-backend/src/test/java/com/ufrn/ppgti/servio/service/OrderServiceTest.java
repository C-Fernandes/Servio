package com.ufrn.ppgti.servio.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ufrn.ppgti.servio.dto.response.OrderResponseDTO;
import com.ufrn.ppgti.servio.exceptions.BusinessException;
import com.ufrn.ppgti.servio.mappers.OrderMapper;
import com.ufrn.ppgti.servio.model.Order;
import com.ufrn.ppgti.servio.model.OrderStatusHistory;
import com.ufrn.ppgti.servio.model.ProviderProfile;
import com.ufrn.ppgti.servio.model.User;
import com.ufrn.ppgti.servio.model.enums.OrderStatus;
import com.ufrn.ppgti.servio.model.enums.Role;
import com.ufrn.ppgti.servio.repository.OrderRepository;
import com.ufrn.ppgti.servio.repository.OrderStatusHistoryRepository;
import com.ufrn.ppgti.servio.repository.ServiceRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService - SPEC-002 (transições e histórico de status)")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ServiceRepository serviceRepository;
    @Mock
    private AuthService authService;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private OrderStatusHistoryRepository orderStatusHistoryRepository;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private OrderService orderService;

    private static final Long PROVIDER_PROFILE_ID = 77L;

    @Test
    @DisplayName("T3: mudança de status grava um evento no histórico com o papel de quem mudou")
    void updateStatus_recordsHistory() {
        User provider = providerUser();
        Order order = order(OrderStatus.PENDING);
        when(authService.getAuthenticadUser()).thenReturn(provider);
        when(orderRepository.findByIdAndProvider_Id(1L, PROVIDER_PROFILE_ID)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderMapper.toResponseDTO(any(Order.class))).thenReturn(new OrderResponseDTO());

        orderService.updateStatus(1L, OrderStatus.CONFIRMED);

        assertEquals(OrderStatus.CONFIRMED, order.getStatus());

        ArgumentCaptor<OrderStatusHistory> captor = ArgumentCaptor.forClass(OrderStatusHistory.class);
        verify(orderStatusHistoryRepository).save(captor.capture());
        assertEquals(OrderStatus.CONFIRMED, captor.getValue().getStatus());
        assertEquals(Role.PROVIDER, captor.getValue().getChangedByRole());
    }

    @Test
    @DisplayName("T4 / Edge 4: transição IN_PROGRESS -> CONFIRMED é inválida e não gera histórico")
    void updateStatus_invalidBackwardTransition_rejected() {
        User provider = providerUser();
        Order order = order(OrderStatus.IN_PROGRESS);
        when(authService.getAuthenticadUser()).thenReturn(provider);
        when(orderRepository.findByIdAndProvider_Id(1L, PROVIDER_PROFILE_ID)).thenReturn(Optional.of(order));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.updateStatus(1L, OrderStatus.CONFIRMED));
        assertEquals("Transição de status inválida para o prestador.", ex.getMessage());

        verify(orderStatusHistoryRepository, never()).save(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("T4: PENDING -> IN_PROGRESS deixa de ser permitido (precisa passar por CONFIRMED)")
    void updateStatus_pendingToInProgress_rejected() {
        User provider = providerUser();
        Order order = order(OrderStatus.PENDING);
        when(authService.getAuthenticadUser()).thenReturn(provider);
        when(orderRepository.findByIdAndProvider_Id(1L, PROVIDER_PROFILE_ID)).thenReturn(Optional.of(order));

        assertThrows(BusinessException.class,
                () -> orderService.updateStatus(1L, OrderStatus.IN_PROGRESS));
        verify(orderStatusHistoryRepository, never()).save(any());
    }

    // ----- helpers -----

    private User providerUser() {
        User user = new User();
        user.setId(50L);
        user.setName("Prestador");
        user.setRole(Role.PROVIDER);
        ProviderProfile profile = new ProviderProfile();
        profile.setId(PROVIDER_PROFILE_ID);
        user.setProviderProfile(profile);
        return user;
    }

    private Order order(OrderStatus status) {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(status);
        return order;
    }
}
