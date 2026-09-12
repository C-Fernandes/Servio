package com.ufrn.ppgti.servio.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ufrn.ppgti.servio.dto.request.CouponCreateRequestDTO;
import com.ufrn.ppgti.servio.dto.response.CouponResponseDTO;
import com.ufrn.ppgti.servio.dto.response.CouponValidationResponseDTO;
import com.ufrn.ppgti.servio.exceptions.BusinessException;
import com.ufrn.ppgti.servio.model.Coupon;
import com.ufrn.ppgti.servio.model.CouponUsage;
import com.ufrn.ppgti.servio.model.Order;
import com.ufrn.ppgti.servio.model.User;
import com.ufrn.ppgti.servio.repository.CouponRepository;
import com.ufrn.ppgti.servio.repository.CouponUsageRepository;
import com.ufrn.ppgti.servio.repository.ServiceRepository;

@Service
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;
    private final ServiceRepository serviceRepository;
    private final AuthService authService;

    public CouponService(
            CouponRepository couponRepository,
            CouponUsageRepository couponUsageRepository,
            ServiceRepository serviceRepository,
            AuthService authService) {
        this.couponRepository = couponRepository;
        this.couponUsageRepository = couponUsageRepository;
        this.serviceRepository = serviceRepository;
        this.authService = authService;
    }

    @Transactional
    public CouponResponseDTO create(CouponCreateRequestDTO dto) {
        User currentUser = authService.getAuthenticadUser();

        com.ufrn.ppgti.servio.model.Service service = serviceRepository.findById(dto.getServiceId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Serviço não encontrado."));

        requireOwner(service, currentUser);

        if (couponRepository.existsByCodeIgnoreCase(dto.getCode())) {
            throw new BusinessException("Já existe um cupom com esse código.");
        }

        if (dto.getExpiresAt() != null && dto.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("A data de expiração deve ser futura.");
        }

        Coupon coupon = new Coupon();
        coupon.setCode(dto.getCode().trim().toUpperCase());
        coupon.setDiscountPercentage(dto.getDiscountPercentage());
        coupon.setService(service);
        coupon.setExpiresAt(dto.getExpiresAt());
        coupon.setActive(true);

        return toResponseDTO(couponRepository.save(coupon));
    }

    @Transactional(readOnly = true)
    public List<CouponResponseDTO> listMyCoupons() {
        User currentUser = authService.getAuthenticadUser();

        return couponRepository.findByService_Provider_IdOrderByCreatedAtDesc(currentUser.getId())
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional
    public void deactivate(Long id) {
        User currentUser = authService.getAuthenticadUser();

        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cupom não encontrado."));

        requireOwner(coupon.getService(), currentUser);

        coupon.setActive(false);
        couponRepository.save(coupon);
    }

    @Transactional(readOnly = true)
    public CouponValidationResponseDTO validate(String code, Long serviceId) {
        User currentUser = authService.getAuthenticadUser();
        Coupon coupon = findUsableCoupon(code, serviceId, currentUser);

        double originalPrice = coupon.getService().getPrice() != null ? coupon.getService().getPrice() : 0;
        double finalPrice = computeFinalPrice(originalPrice, coupon.getDiscountPercentage());

        return new CouponValidationResponseDTO(coupon.getCode(), coupon.getDiscountPercentage(), originalPrice,
                finalPrice);
    }

    /** Reaproveitado pela criação do pedido (RF-06) para aplicar o mesmo desconto validado aqui. */
    public Coupon findUsableCoupon(String code, Long serviceId, User client) {
        Coupon coupon = couponRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new BusinessException("Cupom inválido."));

        if (!coupon.isActive()) {
            throw new BusinessException("Cupom inativo.");
        }

        if (coupon.isExpired()) {
            throw new BusinessException("Cupom expirado.");
        }

        if (!coupon.getService().getId().equals(serviceId)) {
            throw new BusinessException("Cupom não é válido para este serviço.");
        }

        if (couponUsageRepository.existsByCoupon_IdAndClient_Id(coupon.getId(), client.getId())) {
            throw new BusinessException("Você já utilizou este cupom.");
        }

        return coupon;
    }

    public double computeFinalPrice(double originalPrice, double discountPercentage) {
        double discounted = originalPrice - (originalPrice * discountPercentage / 100);
        return Math.max(discounted, 0);
    }

    @Transactional
    public void registerUsage(Coupon coupon, User client, Order order) {
        couponUsageRepository.save(new CouponUsage(coupon, client, order));
    }

    private void requireOwner(com.ufrn.ppgti.servio.model.Service service, User user) {
        if (service.getProvider() == null || !service.getProvider().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Você só pode gerenciar cupons dos seus próprios serviços.");
        }
    }

    private CouponResponseDTO toResponseDTO(Coupon coupon) {
        return new CouponResponseDTO(
                coupon.getId(),
                coupon.getCode(),
                coupon.getDiscountPercentage(),
                coupon.getService().getId(),
                coupon.getService().getTitle(),
                coupon.isActive(),
                coupon.getExpiresAt(),
                coupon.getCreatedAt());
    }
}
