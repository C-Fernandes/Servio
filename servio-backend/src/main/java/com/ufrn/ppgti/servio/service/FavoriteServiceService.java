package com.ufrn.ppgti.servio.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

import com.ufrn.ppgti.servio.dto.response.ServiceResponseDTO;
import com.ufrn.ppgti.servio.exceptions.BusinessException;
import com.ufrn.ppgti.servio.mappers.ServiceMapper;
import com.ufrn.ppgti.servio.model.FavoriteService;
import com.ufrn.ppgti.servio.model.User;
import com.ufrn.ppgti.servio.repository.FavoriteServiceRepository;
import com.ufrn.ppgti.servio.repository.ServiceRepository;

@org.springframework.stereotype.Service
public class FavoriteServiceService {

    private final FavoriteServiceRepository favoriteRepository;
    private final ServiceRepository serviceRepository;
    private final AuthService authService;
    private final ServiceMapper serviceMapper;
    private final ReviewService reviewService;

    public FavoriteServiceService(
            FavoriteServiceRepository favoriteRepository,
            ServiceRepository serviceRepository,
            AuthService authService,
            ServiceMapper serviceMapper,
            ReviewService reviewService) {
        this.favoriteRepository = favoriteRepository;
        this.serviceRepository = serviceRepository;
        this.authService = authService;
        this.serviceMapper = serviceMapper;
        this.reviewService = reviewService;
    }

    @Transactional(readOnly = true)
    public List<ServiceResponseDTO> findCurrentUserFavorites() {
        User user = authService.getAuthenticadUser();

        return favoriteRepository.findByClientIdAndServiceDeletedFalseOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(FavoriteService::getService)
                .filter(service -> service.isActive() && !service.isDeleted())
                .map(this::toFavoriteResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ServiceResponseDTO add(Long serviceId) {
        User user = authService.getAuthenticadUser();

        com.ufrn.ppgti.servio.model.Service service = serviceRepository.findByIdAndDeletedFalse(serviceId)
                .orElseThrow(() -> new BusinessException("Serviço não encontrado."));

        if (!service.isActive()) {
            throw new BusinessException("Não é possível favoritar um serviço inativo.");
        }

        if (!favoriteRepository.existsByClientIdAndServiceId(user.getId(), serviceId)) {
            FavoriteService favorite = new FavoriteService();
            favorite.setClient(user);
            favorite.setService(service);
            favoriteRepository.save(favorite);
        }

        return toFavoriteResponse(service);
    }

    @Transactional
    public void remove(Long serviceId) {
        User user = authService.getAuthenticadUser();

        FavoriteService favorite = favoriteRepository.findByClientIdAndServiceId(user.getId(), serviceId)
                .orElseThrow(() -> new BusinessException("Favorito não encontrado."));

        favoriteRepository.delete(favorite);
    }

    private ServiceResponseDTO toFavoriteResponse(com.ufrn.ppgti.servio.model.Service service) {
        ServiceResponseDTO dto = serviceMapper.toResponseDTO(service);
        dto.setAverageRating(reviewService.getAverageRatingByServiceId(service.getId()));
        dto.setReviewCount(reviewService.getReviewCountByServiceId(service.getId()));
        dto.setFavorite(true);
        return dto;
    }
}

