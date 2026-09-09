package com.ufrn.ppgti.servio.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ufrn.ppgti.servio.dto.response.FavoriteResponseDTO;
import com.ufrn.ppgti.servio.exceptions.BusinessException;
import com.ufrn.ppgti.servio.model.Favorite;
import com.ufrn.ppgti.servio.model.User;
import com.ufrn.ppgti.servio.model.enums.Role;
import com.ufrn.ppgti.servio.repository.FavoriteRepository;
import com.ufrn.ppgti.servio.repository.ServiceRepository;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ServiceRepository serviceRepository;
    private final AuthService authService;
    private final ReviewService reviewService;

    public FavoriteService(
            FavoriteRepository favoriteRepository,
            ServiceRepository serviceRepository,
            AuthService authService,
            ReviewService reviewService) {
        this.favoriteRepository = favoriteRepository;
        this.serviceRepository = serviceRepository;
        this.authService = authService;
        this.reviewService = reviewService;
    }

    @Transactional
    public FavoriteResponseDTO addFavorite(Long serviceId) {
        User user = requireClient();

        com.ufrn.ppgti.servio.model.Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Serviço não encontrado."));

        if (service.isDeleted() || !service.isActive()) {
            throw new BusinessException("Serviço não está disponível para ser favoritado");
        }

        Favorite favorite = favoriteRepository.findByUserIdAndServiceId(user.getId(), serviceId)
                .orElseGet(() -> favoriteRepository.save(new Favorite(user, service)));

        return toResponseDTO(favorite);
    }

    @Transactional
    public void removeFavorite(Long serviceId) {
        User user = requireClient();

        Favorite favorite = favoriteRepository.findByUserIdAndServiceId(user.getId(), serviceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Favorito não encontrado."));

        favoriteRepository.delete(favorite);
    }

    @Transactional(readOnly = true)
    public List<FavoriteResponseDTO> listMyFavorites() {
        User user = requireClient();

        return favoriteRepository.findByUserIdWithService(user.getId())
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean isFavorited(Long serviceId) {
        User user = requireClient();
        return favoriteRepository.existsByUserIdAndServiceId(user.getId(), serviceId);
    }

    private User requireClient() {
        User user = authService.getAuthenticadUser();
        if (user.getRole() != Role.CLIENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas clientes podem gerenciar favoritos.");
        }
        return user;
    }

    private FavoriteResponseDTO toResponseDTO(Favorite favorite) {
        com.ufrn.ppgti.servio.model.Service service = favorite.getService();

        String category = service.getCategory() != null ? service.getCategory().getName() : null;

        Long providerId = null;
        String providerName = null;
        if (service.getProvider() != null) {
            providerId = service.getProvider().getId();
            if (service.getProvider().getUser() != null) {
                providerName = service.getProvider().getUser().getName();
            }
        }

        return new FavoriteResponseDTO(
                favorite.getId(),
                favorite.getCreatedAt(),
                service.getId(),
                service.getTitle(),
                service.getDescription(),
                service.getPrice(),
                service.getDurationInMinutes(),
                service.isActive(),
                category,
                providerId,
                providerName,
                extractBase64(service.getImageUrl()),
                reviewService.getAverageRatingByServiceId(service.getId()),
                reviewService.getReviewCountByServiceId(service.getId()));
    }

    private String extractBase64(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }

        try {
            String fileName = imageUrl.startsWith("/") ? imageUrl.substring(1) : imageUrl;
            Path path = Paths.get(fileName);

            if (Files.exists(path)) {
                byte[] bytes = Files.readAllBytes(path);
                return Base64.getEncoder().encodeToString(bytes);
            }
        } catch (IOException e) {
            System.err.println("Erro ao converter imagem para Base64: " + e.getMessage());
        }
        return null;
    }
}
