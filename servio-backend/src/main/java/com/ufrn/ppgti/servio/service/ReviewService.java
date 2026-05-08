package com.ufrn.ppgti.servio.service;

import com.ufrn.ppgti.servio.dto.request.ReviewCreateRequestDTO;
import com.ufrn.ppgti.servio.dto.request.ReviewUpdateRequestDTO;
import com.ufrn.ppgti.servio.dto.response.ReviewResponseDTO;
import com.ufrn.ppgti.servio.exceptions.BusinessException;
import com.ufrn.ppgti.servio.model.Order;
import com.ufrn.ppgti.servio.model.Review;
import com.ufrn.ppgti.servio.model.User;
import com.ufrn.ppgti.servio.model.enums.OrderStatus;
import com.ufrn.ppgti.servio.repository.OrderRepository;
import com.ufrn.ppgti.servio.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final AuthService authService;

    public ReviewService(
            ReviewRepository reviewRepository,
            OrderRepository orderRepository,
            AuthService authService) {
        this.reviewRepository = reviewRepository;
        this.orderRepository = orderRepository;
        this.authService = authService;
    }

    @Transactional
    public ReviewResponseDTO create(ReviewCreateRequestDTO dto) {
        User user = authService.getAuthenticadUser();

        Order order = orderRepository.findById(dto.getOrderId())
                .orElseThrow(() -> new BusinessException("Pedido não encontrado."));

        validateOrderBelongsToClient(order, user);

        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new BusinessException("Só é possível avaliar pedidos concluídos.");
        }

        if (reviewRepository.existsByOrderId(order.getId())) {
            throw new BusinessException("Este pedido já foi avaliado.");
        }

        Review review = new Review(
                dto.getRating(),
                normalizeComment(dto.getComment()),
                order);

        Review savedReview = reviewRepository.save(review);

        return toResponseDTO(savedReview);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> findMyReviews() {
        User user = authService.getAuthenticadUser();

        return reviewRepository.findByClientId(user.getId())
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> findByService(Long serviceId) {
        return reviewRepository.findByServiceId(serviceId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> findProviderReviews() {
        User user = authService.getAuthenticadUser();

        if (user.getProviderProfile() == null) {
            throw new BusinessException("Apenas prestadores podem visualizar essas avaliações.");
        }

        Long providerId = user.getProviderProfile().getId();

        return reviewRepository.findByProviderId(providerId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReviewResponseDTO findById(Long id) {
        Review review = reviewRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new BusinessException("Avaliação não encontrada."));

        return toResponseDTO(review);
    }

    @Transactional
    public ReviewResponseDTO update(Long id, ReviewUpdateRequestDTO dto) {
        User user = authService.getAuthenticadUser();

        Review review = reviewRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new BusinessException("Avaliação não encontrada."));

        validateOrderBelongsToClient(review.getOrder(), user);

        review.setRating(dto.getRating());
        review.setComment(normalizeComment(dto.getComment()));

        Review updatedReview = reviewRepository.save(review);

        return toResponseDTO(updatedReview);
    }

    @Transactional
    public void delete(Long id) {
        User user = authService.getAuthenticadUser();

        Review review = reviewRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new BusinessException("Avaliação não encontrada."));

        validateOrderBelongsToClient(review.getOrder(), user);

        reviewRepository.delete(review);
    }

    private void validateOrderBelongsToClient(Order order, User user) {
        if (!order.getClient().getId().equals(user.getId())) {
            throw new BusinessException("Você não tem permissão para acessar este pedido.");
        }
    }

    private String normalizeComment(String comment) {
        if (comment == null || comment.trim().isEmpty()) {
            return null;
        }

        return comment.trim();
    }

    private ReviewResponseDTO toResponseDTO(Review review) {
        Order order = review.getOrder();

        return new ReviewResponseDTO(
                review.getId(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt(),
                order.getId(),
                order.getClient().getId(),
                order.getClient().getName(),
                order.getService().getProvider().getId(),
                order.getService().getProvider().getUser().getName(),
                order.getService().getId(),
                order.getService().getTitle());
    }
}