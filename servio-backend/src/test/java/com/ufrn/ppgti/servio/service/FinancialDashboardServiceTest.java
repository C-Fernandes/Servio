package com.ufrn.ppgti.servio.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ufrn.ppgti.servio.dto.response.TopServiceResponseDTO;
import com.ufrn.ppgti.servio.exceptions.BusinessException;
import com.ufrn.ppgti.servio.model.ProviderProfile;
import com.ufrn.ppgti.servio.model.User;
import com.ufrn.ppgti.servio.model.enums.Role;
import com.ufrn.ppgti.servio.repository.OrderRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("FinancialDashboardService - SPEC-004 (RF-15 Relatório de Desempenho)")
class FinancialDashboardServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private AuthService authService;
    @Mock
    private ReviewService reviewService;

    @InjectMocks
    private FinancialDashboardService financialDashboardService;

    private static final Long PROVIDER_PROFILE_ID = 77L;

    @Test
    @DisplayName("Cenário 1: ranking vem ordenado por concluídos, com avaliação de cada serviço")
    void getTopServices_returnsRankedWithRatings() {
        when(authService.getAuthenticadUser()).thenReturn(providerUser());
        when(orderRepository.countCompletedOrdersGroupedByService(PROVIDER_PROFILE_ID)).thenReturn(List.of(
                new Object[] { 1L, "Limpeza", 5L },
                new Object[] { 2L, "Pintura", 2L }));
        when(reviewService.getAverageRatingByServiceId(1L)).thenReturn(4.5);
        when(reviewService.getReviewCountByServiceId(1L)).thenReturn(3L);
        when(reviewService.getAverageRatingByServiceId(2L)).thenReturn(5.0);
        when(reviewService.getReviewCountByServiceId(2L)).thenReturn(1L);

        List<TopServiceResponseDTO> result = financialDashboardService.getTopServices();

        assertEquals(2, result.size());
        assertEquals("Limpeza", result.get(0).getTitle());
        assertEquals(5L, result.get(0).getCompletedOrders());
        assertEquals(4.5, result.get(0).getAverageRating());
        assertEquals("Pintura", result.get(1).getTitle());
    }

    @Test
    @DisplayName("Edge 1: prestador sem pedidos concluídos recebe lista vazia (sem erro)")
    void getTopServices_noCompletedOrders_returnsEmpty() {
        when(authService.getAuthenticadUser()).thenReturn(providerUser());
        when(orderRepository.countCompletedOrdersGroupedByService(PROVIDER_PROFILE_ID)).thenReturn(List.of());

        List<TopServiceResponseDTO> result = financialDashboardService.getTopServices();

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Edge 2: usuário sem perfil de prestador é recusado")
    void getTopServices_noProviderProfile_throwsBusinessException() {
        User client = new User();
        client.setId(1L);
        client.setRole(Role.CLIENT);
        when(authService.getAuthenticadUser()).thenReturn(client);

        assertThrows(BusinessException.class, () -> financialDashboardService.getTopServices());
    }

    private User providerUser() {
        User user = new User();
        user.setId(50L);
        user.setRole(Role.PROVIDER);
        ProviderProfile profile = new ProviderProfile();
        profile.setId(PROVIDER_PROFILE_ID);
        user.setProviderProfile(profile);
        return user;
    }
}
