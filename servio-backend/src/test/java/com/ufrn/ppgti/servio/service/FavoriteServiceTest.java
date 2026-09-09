package com.ufrn.ppgti.servio.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.ufrn.ppgti.servio.dto.response.FavoriteResponseDTO;
import com.ufrn.ppgti.servio.exceptions.BusinessException;
import com.ufrn.ppgti.servio.model.Category;
import com.ufrn.ppgti.servio.model.Favorite;
import com.ufrn.ppgti.servio.model.ProviderProfile;
import com.ufrn.ppgti.servio.model.Service;
import com.ufrn.ppgti.servio.model.User;
import com.ufrn.ppgti.servio.model.enums.Role;
import com.ufrn.ppgti.servio.repository.FavoriteRepository;
import com.ufrn.ppgti.servio.repository.ServiceRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("FavoriteService - SPEC-001 (RF-01 Sistema de Favoritos)")
class FavoriteServiceTest {

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private AuthService authService;

    @Mock
    private ReviewService reviewService;

    @InjectMocks
    private FavoriteService favoriteService;

    private User client;

    @BeforeEach
    void setUp() {
        client = buildUser(10L, "Maria", Role.CLIENT);
        org.mockito.Mockito.lenient().when(reviewService.getAverageRatingByServiceId(anyLong())).thenReturn(0.0);
        org.mockito.Mockito.lenient().when(reviewService.getReviewCountByServiceId(anyLong())).thenReturn(0L);
    }

    @Test
    @DisplayName("Cenário 1: cliente favorita um serviço ativo com sucesso (201)")
    void addFavorite_activeService_success() {
        Service service = buildService(1L, "Limpeza Residencial", true, false);
        when(authService.getAuthenticadUser()).thenReturn(client);
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(service));
        when(favoriteRepository.findByUserIdAndServiceId(10L, 1L)).thenReturn(Optional.empty());
        when(favoriteRepository.save(any(Favorite.class))).thenAnswer(invocation -> {
            Favorite saved = invocation.getArgument(0);
            saved.setId(99L);
            saved.setCreatedAt(LocalDateTime.now());
            return saved;
        });

        FavoriteResponseDTO dto = favoriteService.addFavorite(1L);

        assertEquals(99L, dto.getFavoriteId());
        assertEquals(1L, dto.getServiceId());
        assertEquals("Limpeza Residencial", dto.getTitle());
        verify(favoriteRepository).save(any(Favorite.class));
    }

    @Test
    @DisplayName("Cenário 2: cliente remove um serviço dos favoritos (204)")
    void removeFavorite_existing_success() {
        Favorite favorite = buildFavorite(50L, buildService(1L, "Limpeza Residencial", true, false));
        when(authService.getAuthenticadUser()).thenReturn(client);
        when(favoriteRepository.findByUserIdAndServiceId(10L, 1L)).thenReturn(Optional.of(favorite));

        favoriteService.removeFavorite(1L);

        verify(favoriteRepository).delete(favorite);
    }

    @Test
    @DisplayName("Cenário 2b: remover favorito inexistente retorna 404")
    void removeFavorite_notFound_throws404() {
        when(authService.getAuthenticadUser()).thenReturn(client);
        when(favoriteRepository.findByUserIdAndServiceId(10L, 1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> favoriteService.removeFavorite(1L));
        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    @DisplayName("Cenário 3: cliente visualiza sua lista de favoritos (200)")
    void listMyFavorites_returnsAll() {
        when(authService.getAuthenticadUser()).thenReturn(client);
        when(favoriteRepository.findByUserIdWithService(10L)).thenReturn(List.of(
                buildFavorite(1L, buildService(1L, "Pintura", true, false)),
                buildFavorite(2L, buildService(2L, "Elétrica", true, false)),
                buildFavorite(3L, buildService(3L, "Jardinagem", true, false))));

        List<FavoriteResponseDTO> favorites = favoriteService.listMyFavorites();

        assertEquals(3, favorites.size());
        assertEquals("Pintura", favorites.get(0).getTitle());
    }

    @Test
    @DisplayName("Edge 1: POST duplicado é idempotente - não cria novo registro")
    void addFavorite_alreadyFavorited_isIdempotent() {
        Service service = buildService(1L, "Pintura de Parede", true, false);
        Favorite existing = buildFavorite(50L, service);
        when(authService.getAuthenticadUser()).thenReturn(client);
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(service));
        when(favoriteRepository.findByUserIdAndServiceId(10L, 1L)).thenReturn(Optional.of(existing));

        FavoriteResponseDTO dto = favoriteService.addFavorite(1L);

        assertEquals(50L, dto.getFavoriteId());
        verify(favoriteRepository, never()).save(any(Favorite.class));
    }

    @Test
    @DisplayName("Edge 2: serviço inativo ou deletado não pode ser favoritado (400)")
    void addFavorite_inactiveOrDeletedService_throwsBusinessException() {
        Service inactive = buildService(1L, "Manutenção Elétrica", false, false);
        when(authService.getAuthenticadUser()).thenReturn(client);
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(inactive));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> favoriteService.addFavorite(1L));
        assertEquals("Serviço não está disponível para ser favoritado", ex.getMessage());
        verify(favoriteRepository, never()).save(any(Favorite.class));
    }

    @Test
    @DisplayName("Edge 3: usuário com perfil PROVIDER é barrado com 403")
    void addFavorite_providerRole_throws403() {
        User provider = buildUser(20L, "Prestador", Role.PROVIDER);
        when(authService.getAuthenticadUser()).thenReturn(provider);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> favoriteService.addFavorite(1L));
        assertEquals(403, ex.getStatusCode().value());
        verify(serviceRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("Serviço inexistente retorna 404")
    void addFavorite_serviceNotFound_throws404() {
        when(authService.getAuthenticadUser()).thenReturn(client);
        when(serviceRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> favoriteService.addFavorite(1L));
        assertEquals(404, ex.getStatusCode().value());
    }

    // ----- helpers -----

    private User buildUser(Long id, String name, Role role) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setRole(role);
        return user;
    }

    private Service buildService(Long id, String title, boolean active, boolean deleted) {
        Service service = new Service();
        service.setId(id);
        service.setTitle(title);
        service.setDescription("desc " + title);
        service.setPrice(100.0);
        service.setDurationInMinutes(60);
        service.setActive(active);
        service.setDeleted(deleted);

        Category category = new Category();
        category.setName("Casa");
        service.setCategory(category);

        User providerUser = buildUser(500L + id, "Prestador " + id, Role.PROVIDER);
        ProviderProfile profile = new ProviderProfile();
        profile.setId(500L + id);
        profile.setUser(providerUser);
        service.setProvider(profile);

        return service;
    }

    private Favorite buildFavorite(Long id, Service service) {
        Favorite favorite = new Favorite(client, service);
        favorite.setId(id);
        favorite.setCreatedAt(LocalDateTime.now());
        return favorite;
    }
}
