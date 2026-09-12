package com.ufrn.ppgti.servio.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.ufrn.ppgti.servio.dto.request.CouponCreateRequestDTO;
import com.ufrn.ppgti.servio.dto.response.CouponResponseDTO;
import com.ufrn.ppgti.servio.dto.response.CouponValidationResponseDTO;
import com.ufrn.ppgti.servio.exceptions.BusinessException;
import com.ufrn.ppgti.servio.model.Coupon;
import com.ufrn.ppgti.servio.model.ProviderProfile;
import com.ufrn.ppgti.servio.model.User;
import com.ufrn.ppgti.servio.model.enums.Role;
import com.ufrn.ppgti.servio.repository.CouponRepository;
import com.ufrn.ppgti.servio.repository.CouponUsageRepository;
import com.ufrn.ppgti.servio.repository.ServiceRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("CouponService - SPEC-006 (RF-18 Cupons de Desconto)")
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;
    @Mock
    private CouponUsageRepository couponUsageRepository;
    @Mock
    private ServiceRepository serviceRepository;
    @Mock
    private AuthService authService;

    @InjectMocks
    private CouponService couponService;

    private static final Long PROVIDER_ID = 10L;
    private static final Long OTHER_PROVIDER_ID = 20L;
    private static final Long CLIENT_ID = 30L;
    private static final Long SERVICE_ID = 1L;

    @Test
    @DisplayName("Cenário 1: prestador cria cupom para o próprio serviço")
    void create_ownService_persistsCoupon() {
        when(authService.getAuthenticadUser()).thenReturn(providerUser());
        when(serviceRepository.findById(SERVICE_ID)).thenReturn(Optional.of(service(100.0)));
        when(couponRepository.existsByCodeIgnoreCase("PROMO10")).thenReturn(false);
        when(couponRepository.save(any(Coupon.class))).thenAnswer(inv -> {
            Coupon c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        CouponCreateRequestDTO dto = createRequest("PROMO10", 10.0, null);

        CouponResponseDTO result = couponService.create(dto);

        assertEquals("PROMO10", result.getCode());
        assertEquals(10.0, result.getDiscountPercentage());
        assertEquals(SERVICE_ID, result.getServiceId());
    }

    @Test
    @DisplayName("Cenário 2: cliente valida cupom válido e recebe o preço com desconto")
    void validate_usableCoupon_returnsDiscountedPrice() {
        Coupon coupon = coupon("PROMO10", 10.0, service(100.0), true, null);
        when(authService.getAuthenticadUser()).thenReturn(clientUser());
        when(couponRepository.findByCodeIgnoreCase("PROMO10")).thenReturn(Optional.of(coupon));
        when(couponUsageRepository.existsByCoupon_IdAndClient_Id(coupon.getId(), CLIENT_ID)).thenReturn(false);

        CouponValidationResponseDTO result = couponService.validate("PROMO10", SERVICE_ID);

        assertEquals(100.0, result.getOriginalPrice());
        assertEquals(90.0, result.getFinalPrice());
    }

    @Test
    @DisplayName("Cenário 3: cliente que já usou o cupom não pode reutilizá-lo")
    void findUsableCoupon_alreadyUsedByClient_throwsBusinessException() {
        Coupon coupon = coupon("PROMO10", 10.0, service(100.0), true, null);
        when(couponRepository.findByCodeIgnoreCase("PROMO10")).thenReturn(Optional.of(coupon));
        when(couponUsageRepository.existsByCoupon_IdAndClient_Id(coupon.getId(), CLIENT_ID)).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> couponService.findUsableCoupon("PROMO10", SERVICE_ID, clientUser()));
        assertEquals("Você já utilizou este cupom.", ex.getMessage());
    }

    @Test
    @DisplayName("Edge 1: prestador não pode criar cupom para serviço de outro prestador")
    void create_serviceFromOtherProvider_throws403() {
        com.ufrn.ppgti.servio.model.Service otherService = service(100.0);
        otherService.getProvider().setId(OTHER_PROVIDER_ID);

        when(authService.getAuthenticadUser()).thenReturn(providerUser());
        when(serviceRepository.findById(SERVICE_ID)).thenReturn(Optional.of(otherService));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> couponService.create(createRequest("PROMO10", 10.0, null)));
        assertEquals(403, ex.getStatusCode().value());
        verify(couponRepository, never()).save(any());
    }

    @Test
    @DisplayName("Edge 2: cupom expirado é recusado na validação")
    void findUsableCoupon_expired_throwsBusinessException() {
        Coupon coupon = coupon("PROMO10", 10.0, service(100.0), true, LocalDateTime.now().minusDays(1));
        when(couponRepository.findByCodeIgnoreCase("PROMO10")).thenReturn(Optional.of(coupon));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> couponService.findUsableCoupon("PROMO10", SERVICE_ID, clientUser()));
        assertEquals("Cupom expirado.", ex.getMessage());
    }

    @Test
    @DisplayName("Edge 3: código de cupom duplicado é recusado")
    void create_duplicateCode_throwsBusinessException() {
        when(authService.getAuthenticadUser()).thenReturn(providerUser());
        when(serviceRepository.findById(SERVICE_ID)).thenReturn(Optional.of(service(100.0)));
        when(couponRepository.existsByCodeIgnoreCase(anyString())).thenReturn(true);

        assertThrows(BusinessException.class,
                () -> couponService.create(createRequest("PROMO10", 10.0, null)));
        verify(couponRepository, never()).save(any());
    }

    @Test
    @DisplayName("Edge 4: cupom não é válido para outro serviço além do cadastrado")
    void findUsableCoupon_wrongService_throwsBusinessException() {
        Coupon coupon = coupon("PROMO10", 10.0, service(100.0), true, null);
        when(couponRepository.findByCodeIgnoreCase("PROMO10")).thenReturn(Optional.of(coupon));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> couponService.findUsableCoupon("PROMO10", 999L, clientUser()));
        assertEquals("Cupom não é válido para este serviço.", ex.getMessage());
    }

    // ----- helpers -----

    private User providerUser() {
        User user = new User();
        user.setId(PROVIDER_ID);
        user.setRole(Role.PROVIDER);
        return user;
    }

    private User clientUser() {
        User user = new User();
        user.setId(CLIENT_ID);
        user.setRole(Role.CLIENT);
        return user;
    }

    private com.ufrn.ppgti.servio.model.Service service(double price) {
        com.ufrn.ppgti.servio.model.Service service = new com.ufrn.ppgti.servio.model.Service();
        service.setId(SERVICE_ID);
        service.setTitle("Corte de cabelo");
        service.setPrice(price);

        ProviderProfile provider = new ProviderProfile();
        provider.setId(PROVIDER_ID);
        service.setProvider(provider);

        return service;
    }

    private Coupon coupon(String code, double discountPercentage, com.ufrn.ppgti.servio.model.Service service,
            boolean active, LocalDateTime expiresAt) {
        Coupon coupon = new Coupon();
        coupon.setId(1L);
        coupon.setCode(code);
        coupon.setDiscountPercentage(discountPercentage);
        coupon.setService(service);
        coupon.setActive(active);
        coupon.setExpiresAt(expiresAt);
        return coupon;
    }

    private CouponCreateRequestDTO createRequest(String code, double discountPercentage, LocalDateTime expiresAt) {
        CouponCreateRequestDTO dto = new CouponCreateRequestDTO();
        dto.setServiceId(SERVICE_ID);
        dto.setCode(code);
        dto.setDiscountPercentage(discountPercentage);
        dto.setExpiresAt(expiresAt);
        return dto;
    }
}
