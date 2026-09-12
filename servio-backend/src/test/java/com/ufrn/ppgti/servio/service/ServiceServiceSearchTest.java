package com.ufrn.ppgti.servio.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.ufrn.ppgti.servio.dto.request.ServiceSearchRequestDTO;
import com.ufrn.ppgti.servio.dto.response.ServiceResponseDTO;
import com.ufrn.ppgti.servio.exceptions.BusinessException;
import com.ufrn.ppgti.servio.mappers.AvailabilityMapper;
import com.ufrn.ppgti.servio.mappers.ServiceMapper;
import com.ufrn.ppgti.servio.model.User;
import com.ufrn.ppgti.servio.repository.CategoryRepository;
import com.ufrn.ppgti.servio.repository.FavoriteRepository;
import com.ufrn.ppgti.servio.repository.OrderRepository;
import com.ufrn.ppgti.servio.repository.ServiceRepository;
import com.ufrn.ppgti.servio.repository.TagRepository;

/**
 * Cobre apenas {@link ServiceService#search}, o método do RF-05 (Busca
 * Avançada de Serviços — SPEC-007). SPEC-007 seção 7 justifica a ausência de
 * ADR dedicado (reuso do padrão Specification já usado no projeto); esta
 * classe fecha o gap remanescente, que era só a ausência de teste.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ServiceService.search - SPEC-007 (RF-05 Busca Avançada de Serviços)")
class ServiceServiceSearchTest {

    @Mock
    private ServiceRepository repository;
    @Mock
    private ServiceMapper mapper;
    @Mock
    private AuthService authService;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private TagRepository tagRepository;
    @Mock
    private AvailabilityMapper availabilityMapper;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private AvailabilityService availabilityService;
    @Mock
    private ReviewService reviewService;
    @Mock
    private FavoriteRepository favoriteRepository;

    @InjectMocks
    private ServiceService serviceService;

    @Test
    @DisplayName("Edge 1: preço mínimo maior que o máximo é rejeitado antes de consultar o repositório")
    void search_minPriceGreaterThanMaxPrice_throwsBusinessException() {
        when(authService.getAuthenticadUser()).thenReturn(clientUser());

        ServiceSearchRequestDTO filters = new ServiceSearchRequestDTO();
        filters.setMinPrice(1000.0);
        filters.setMaxPrice(100.0);

        BusinessException ex = assertThrows(BusinessException.class, () -> serviceService.search(filters));
        assertEquals("O preço mínimo não pode ser maior que o preço máximo.", ex.getMessage());
    }

    @Test
    @DisplayName("Edge 2: nenhum filtro aplicado retorna todos os serviços ativos, sem erro")
    void search_noFilters_returnsAllActiveServices() {
        when(authService.getAuthenticadUser()).thenReturn(clientUser());
        when(repository.findAll(org.mockito.ArgumentMatchers.<Specification<com.ufrn.ppgti.servio.model.Service>>any(),
                any(Sort.class))).thenReturn(List.of(service(1L), service(2L)));
        when(mapper.toResponseDTO(any())).thenAnswer(inv -> {
            com.ufrn.ppgti.servio.model.Service entity = inv.getArgument(0);
            ServiceResponseDTO dto = new ServiceResponseDTO();
            dto.setId(entity.getId());
            return dto;
        });
        when(reviewService.getAverageRatingByServiceId(anyLong())).thenReturn(0.0);
        when(reviewService.getReviewCountByServiceId(anyLong())).thenReturn(0L);
        when(favoriteRepository.existsByUserIdAndServiceId(anyLong(), anyLong())).thenReturn(false);

        List<ServiceResponseDTO> result = serviceService.search(new ServiceSearchRequestDTO());

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Cenário 3: ordenação por melhor avaliados reordena após a consulta, pela média agregada")
    void search_sortByRatingDesc_reordersByAggregatedAverage() {
        when(authService.getAuthenticadUser()).thenReturn(clientUser());
        when(repository.findAll(org.mockito.ArgumentMatchers.<Specification<com.ufrn.ppgti.servio.model.Service>>any(),
                any(Sort.class))).thenReturn(List.of(service(1L), service(2L)));
        when(mapper.toResponseDTO(any())).thenAnswer(inv -> {
            com.ufrn.ppgti.servio.model.Service entity = inv.getArgument(0);
            ServiceResponseDTO dto = new ServiceResponseDTO();
            dto.setId(entity.getId());
            return dto;
        });
        // Serviço 1 tem média menor que o serviço 2, mesmo consultado primeiro.
        when(reviewService.getAverageRatingByServiceId(1L)).thenReturn(3.0);
        when(reviewService.getAverageRatingByServiceId(2L)).thenReturn(4.5);
        when(reviewService.getReviewCountByServiceId(anyLong())).thenReturn(1L);
        when(favoriteRepository.existsByUserIdAndServiceId(anyLong(), anyLong())).thenReturn(false);

        ServiceSearchRequestDTO filters = new ServiceSearchRequestDTO();
        filters.setSortBy("rating_desc");

        List<ServiceResponseDTO> result = serviceService.search(filters);

        assertEquals(2L, result.get(0).getId());
        assertEquals(1L, result.get(1).getId());
        assertTrue(result.get(0).getAverageRating() >= result.get(1).getAverageRating());
    }

    // ----- helpers -----

    private User clientUser() {
        User user = new User();
        user.setId(30L);
        return user;
    }

    private com.ufrn.ppgti.servio.model.Service service(Long id) {
        com.ufrn.ppgti.servio.model.Service service = new com.ufrn.ppgti.servio.model.Service();
        service.setId(id);
        service.setTitle("Serviço " + id);
        return service;
    }
}
