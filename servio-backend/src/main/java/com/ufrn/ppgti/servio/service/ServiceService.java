package com.ufrn.ppgti.servio.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.nio.file.Path;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.ufrn.ppgti.servio.dto.LocalityDTO;
import com.ufrn.ppgti.servio.dto.response.ServiceResponseDTO;
import com.ufrn.ppgti.servio.dto.request.ServiceRequestDTO;
import com.ufrn.ppgti.servio.dto.request.ServiceSearchRequestDTO;
import com.ufrn.ppgti.servio.repository.specification.ServiceSpecifications;
import com.ufrn.ppgti.servio.exceptions.BusinessException;
import com.ufrn.ppgti.servio.repository.CategoryRepository;
import com.ufrn.ppgti.servio.repository.FavoriteRepository;
import com.ufrn.ppgti.servio.repository.OrderRepository;
import com.ufrn.ppgti.servio.repository.ServiceRepository;
import com.ufrn.ppgti.servio.repository.TagRepository;
import com.ufrn.ppgti.servio.mappers.AvailabilityMapper;
import com.ufrn.ppgti.servio.mappers.ServiceMapper;
import com.ufrn.ppgti.servio.model.Category;
import com.ufrn.ppgti.servio.model.Favorite;
import com.ufrn.ppgti.servio.model.Order;
import com.ufrn.ppgti.servio.model.ProviderProfile;
import com.ufrn.ppgti.servio.model.Review;
import com.ufrn.ppgti.servio.model.Tag;
import com.ufrn.ppgti.servio.model.User;
import com.ufrn.ppgti.servio.model.enums.Role;

@Service
public class ServiceService {

    private final String UPLOAD_DIR = "uploads/";

    private static final String SORT_PRICE_ASC = "price_asc";
    private static final String SORT_PRICE_DESC = "price_desc";
    private static final String SORT_TITLE_ASC = "title_asc";
    private static final String SORT_RATING_DESC = "rating_desc";

    private static final int RECOMMENDATION_LIMIT = 8;
    private static final int RECOMMENDATION_CATEGORY_WEIGHT = 2;
    private static final int RECOMMENDATION_TAG_WEIGHT = 1;

    private final ServiceRepository repository;
    private final ServiceMapper mapper;
    private final AuthService authService;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final AvailabilityMapper availabilityMapper;
    private final OrderRepository orderRepository;
    private final AvailabilityService availabilityService;
    private final ReviewService reviewService;
    private final FavoriteRepository favoriteRepository;

    public ServiceService(ServiceRepository repository, ServiceMapper mapper,
            AuthService authService, CategoryRepository categoryRepository, TagRepository tagRepository,
            AvailabilityMapper availabilityMapper, OrderRepository orderRepository,
            AvailabilityService availabilityService, ReviewService reviewService,
            FavoriteRepository favoriteRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.authService = authService;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
        this.availabilityMapper = availabilityMapper;
        this.orderRepository = orderRepository;
        this.availabilityService = availabilityService;
        this.reviewService = reviewService;
        this.favoriteRepository = favoriteRepository;
    }

    public List<ServiceResponseDTO> findAllActive() {
        User user = authService.getAuthenticadUser();

        return repository.findByActiveTrueAndDeletedFalse().stream()
                .map(entity -> toResponseDTOWithDetails(entity, user.getId()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ServiceResponseDTO> search(ServiceSearchRequestDTO filters) {
        User user = authService.getAuthenticadUser();

        validatePriceRange(filters.getMinPrice(), filters.getMaxPrice());

        List<ServiceResponseDTO> services = repository
                .findAll(ServiceSpecifications.withFilters(filters), resolveSort(filters.getSortBy()))
                .stream()
                .map(entity -> toResponseDTOWithDetails(entity, user.getId()))
                .collect(Collectors.toList());

        // A média das avaliações é calculada por serviço depois da consulta, então
        // essa ordenação não pode ser delegada ao banco como as demais.
        if (SORT_RATING_DESC.equalsIgnoreCase(filters.getSortBy())) {
            services.sort(Comparator.comparingDouble(this::ratingOf).reversed());
        }

        return services;
    }

    @Transactional(readOnly = true)
    public List<LocalityDTO> findAvailableLocalities() {
        return repository.findAvailableLocalities();
    }

    /**
     * Recomenda serviços com base no perfil de interesse do cliente: categorias e
     * tags dos serviços que ele já favoritou ou contratou. Sem sinal de interesse
     * (cliente novo, sem favoritos/pedidos), cai no fallback de melhor avaliados.
     */
    @Transactional(readOnly = true)
    public List<ServiceResponseDTO> getRecommendations() {
        User user = authService.getAuthenticadUser();

        if (user.getRole() != Role.CLIENT) {
            throw new BusinessException("Recomendações estão disponíveis apenas para clientes.");
        }

        Set<Long> interactedServiceIds = new HashSet<>();
        Set<Long> interestCategoryIds = new HashSet<>();
        Set<Long> interestTagIds = new HashSet<>();

        for (Favorite favorite : favoriteRepository.findByUserIdWithService(user.getId())) {
            collectInterest(favorite.getService(), interactedServiceIds, interestCategoryIds, interestTagIds);
        }
        for (Order order : orderRepository.findByClient_IdOrderByCreatedAtDesc(user.getId())) {
            collectInterest(order.getService(), interactedServiceIds, interestCategoryIds, interestTagIds);
        }

        boolean hasInterestSignal = !interestCategoryIds.isEmpty() || !interestTagIds.isEmpty();

        List<com.ufrn.ppgti.servio.model.Service> candidates = repository.findByActiveTrueAndDeletedFalse().stream()
                .filter(entity -> !interactedServiceIds.contains(entity.getId()))
                .toList();

        List<ServiceResponseDTO> recommendations = candidates.stream()
                .map(entity -> toResponseDTOWithDetails(entity, user.getId()))
                .collect(Collectors.toList());

        if (hasInterestSignal) {
            Map<Long, Integer> scoreByServiceId = candidates.stream()
                    .collect(Collectors.toMap(
                            com.ufrn.ppgti.servio.model.Service::getId,
                            entity -> interestScoreOf(entity, interestCategoryIds, interestTagIds)));

            recommendations.sort(
                    Comparator
                            .comparingInt((ServiceResponseDTO dto) -> scoreByServiceId.getOrDefault(dto.getId(), 0))
                            .reversed()
                            .thenComparing(this::ratingOf, Comparator.reverseOrder()));
        } else {
            recommendations.sort(Comparator.comparing(this::ratingOf, Comparator.reverseOrder()));
        }

        return recommendations.stream().limit(RECOMMENDATION_LIMIT).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ServiceResponseDTO> findAllByCurrentProvider() {
        User user = authService.getAuthenticadUser();

        if (user.getProviderProfile() == null) {
            throw new BusinessException("Perfil de prestador não encontrado.");
        }

        return repository.findByProviderIdAndDeletedFalse(user.getProviderProfile().getId())
                .stream()
                .map(entity -> {
                    ServiceResponseDTO dto = mapper.toResponseDTO(entity);
                    dto.setImage(extractBase64(entity.getImageUrl()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ServiceResponseDTO findById(Long id) {
        com.ufrn.ppgti.servio.model.Service entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException("Serviço não encontrado."));

        ServiceResponseDTO dto = mapper.toResponseDTO(entity);
        dto.setImage(extractBase64(entity.getImageUrl()));

        dto.setAvailableSlots(availabilityService.generateAvailableSlots(entity));

        return dto;
    }

    @Transactional
    public ServiceResponseDTO save(ServiceRequestDTO dto, MultipartFile image) {
        User user = authService.getAuthenticadUser();

        if (user.getProviderProfile() == null) {
            throw new BusinessException("Apenas prestadores podem cadastrar serviços.");
        }

        ProviderProfile provider = user.getProviderProfile();
        validatePrice(dto.getPrice());

        if (image != null && !image.isEmpty()) {
            String imageUrl = saveImageToDisk(image);
            dto.setImageUrl(imageUrl);
        }

        com.ufrn.ppgti.servio.model.Service entity = mapper.toEntity(dto);
        entity.setProvider(provider);

        Category category = categoryRepository.findById(dto.getCategory())
                .orElseThrow(() -> new BusinessException("Categoria não encontrada."));
        entity.setCategory(category);

        if (dto.getTags() != null && !dto.getTags().isEmpty()) {
            List<Tag> tags = tagRepository.findAllById(dto.getTags());
            entity.setTags(tags);
        }

        entity = repository.save(entity);

        ServiceResponseDTO responseDTO = mapper.toResponseDTO(entity);
        responseDTO.setImage(extractBase64(entity.getImageUrl()));

        return responseDTO;
    }

    @Transactional
    public ServiceResponseDTO update(Long id, ServiceRequestDTO dto, MultipartFile image) {
        validatePrice(dto.getPrice());

        com.ufrn.ppgti.servio.model.Service service = repository.findById(id)
                .orElseThrow(() -> new BusinessException("Serviço não encontrado."));

        service.setTitle(dto.getTitle());
        service.setDescription(dto.getDescription());
        service.setPrice(dto.getPrice());
        service.setDurationInMinutes(dto.getDurationInMinutes());

        if (dto.getCategory() != null) {
            Category category = categoryRepository.findById(dto.getCategory())
                    .orElseThrow(() -> new BusinessException("Categoria não encontrada."));
            service.setCategory(category);
        }

        if (dto.getTags() != null) {
            List<Tag> tags = tagRepository.findAllById(dto.getTags());
            service.setTags(tags);
        }

        if (image != null && !image.isEmpty()) {
            String imageUrl = saveImageToDisk(image);
            service.setImageUrl(imageUrl);
        }

        service = repository.save(service);
        ServiceResponseDTO responseDTO = mapper.toResponseDTO(service);

        responseDTO.setImage(extractBase64(service.getImageUrl()));

        return responseDTO;
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new BusinessException("Não é possível deletar: Serviço não encontrado.");
        }
        repository.deleteById(id);
    }

    @Transactional
    public ServiceResponseDTO toggleStatus(Long id) {
        com.ufrn.ppgti.servio.model.Service service = repository.findById(id)
                .orElseThrow(() -> new BusinessException("Serviço não encontrado."));

        service.setActive(!service.isActive());

        service = repository.save(service);
        ServiceResponseDTO responseDTO = mapper.toResponseDTO(service);
        responseDTO.setImage(extractBase64(service.getImageUrl()));

        return responseDTO;
    }

    private void validatePrice(Double price) {
        if (price == null || price <= 0) {
            throw new BusinessException("O preço deve ser um valor positivo.");
        }
    }

    private void validatePriceRange(Double minPrice, Double maxPrice) {
        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            throw new BusinessException("O preço mínimo não pode ser maior que o preço máximo.");
        }
    }

    private Sort resolveSort(String sortBy) {
        if (sortBy == null) {
            return Sort.by(Sort.Direction.DESC, "id");
        }

        return switch (sortBy.toLowerCase()) {
            case SORT_PRICE_ASC -> Sort.by(Sort.Direction.ASC, "price");
            case SORT_PRICE_DESC -> Sort.by(Sort.Direction.DESC, "price");
            case SORT_TITLE_ASC -> Sort.by(Sort.Direction.ASC, "title");
            default -> Sort.by(Sort.Direction.DESC, "id");
        };
    }

    private double ratingOf(ServiceResponseDTO dto) {
        return dto.getAverageRating() == null ? 0.0 : dto.getAverageRating();
    }

    private void collectInterest(
            com.ufrn.ppgti.servio.model.Service service,
            Set<Long> interactedServiceIds,
            Set<Long> interestCategoryIds,
            Set<Long> interestTagIds) {
        if (service == null || service.getId() == null) {
            return;
        }

        interactedServiceIds.add(service.getId());

        if (service.getCategory() != null) {
            interestCategoryIds.add(service.getCategory().getId());
        }

        if (service.getTags() != null) {
            for (Tag tag : service.getTags()) {
                interestTagIds.add(tag.getId());
            }
        }
    }

    private int interestScoreOf(
            com.ufrn.ppgti.servio.model.Service entity,
            Set<Long> interestCategoryIds,
            Set<Long> interestTagIds) {
        int score = 0;

        if (entity.getCategory() != null && interestCategoryIds.contains(entity.getCategory().getId())) {
            score += RECOMMENDATION_CATEGORY_WEIGHT;
        }

        if (entity.getTags() != null) {
            for (Tag tag : entity.getTags()) {
                if (interestTagIds.contains(tag.getId())) {
                    score += RECOMMENDATION_TAG_WEIGHT;
                }
            }
        }

        return score;
    }

    private String saveImageToDisk(MultipartFile image) {
        try {

            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = image.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFilename = UUID.randomUUID().toString() + extension;

            Path filePath = uploadPath.resolve(newFilename);
            Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/" + newFilename;

        } catch (IOException e) {
            throw new BusinessException("Erro ao salvar a imagem do serviço.");
        }
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

    private ServiceResponseDTO toResponseDTOWithDetails(com.ufrn.ppgti.servio.model.Service entity) {
        return toResponseDTOWithDetails(entity, null);
    }

    private ServiceResponseDTO toResponseDTOWithDetails(com.ufrn.ppgti.servio.model.Service entity, Long userId) {
        ServiceResponseDTO dto = mapper.toResponseDTO(entity);

        dto.setImage(extractBase64(entity.getImageUrl()));

        if (entity.getId() != null) {
            dto.setAverageRating(reviewService.getAverageRatingByServiceId(entity.getId()));
            dto.setReviewCount(reviewService.getReviewCountByServiceId(entity.getId()));
        } else {
            dto.setAverageRating(0.0);
            dto.setReviewCount(0L);
        }

        if (userId != null && entity.getId() != null) {
            dto.setFavorite(favoriteRepository.existsByUserIdAndServiceId(userId, entity.getId()));
        }

        return dto;
    }

    @Transactional(readOnly = true)
    public boolean existsByCategoryId(Long categoryId) {
        return repository.existsByCategoryId(categoryId);
    }

    @Transactional(readOnly = true)
    public boolean existsByTagId(Long tagId) {
        return repository.existsByTagsId(tagId);
    }
}
