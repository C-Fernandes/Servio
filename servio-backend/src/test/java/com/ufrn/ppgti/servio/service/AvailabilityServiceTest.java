package com.ufrn.ppgti.servio.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.ufrn.ppgti.servio.dto.AvailabilityDTO;
import com.ufrn.ppgti.servio.dto.AvailableSlotDTO;
import com.ufrn.ppgti.servio.exceptions.BusinessException;
import com.ufrn.ppgti.servio.mappers.AvailabilityMapper;
import com.ufrn.ppgti.servio.model.Availability;
import com.ufrn.ppgti.servio.model.ProviderProfile;
import com.ufrn.ppgti.servio.model.User;
import com.ufrn.ppgti.servio.model.enums.Role;
import com.ufrn.ppgti.servio.repository.AvailabilityRepository;
import com.ufrn.ppgti.servio.repository.OrderRepository;
import com.ufrn.ppgti.servio.repository.ProviderProfileRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("AvailabilityService - SPEC-005 (RF-17 Bloqueio de Horários)")
class AvailabilityServiceTest {

    @Mock
    private AvailabilityRepository availabilityRepository;
    @Mock
    private ProviderProfileRepository providerRepository;
    @Mock
    private AuthService authService;
    @Mock
    private OrderRepository orderRepository;

    private static final Long PROVIDER_ID = 77L;

    private AvailabilityService service(AvailabilityMapper mapper) {
        return new AvailabilityService(availabilityRepository, providerRepository, authService, mapper,
                orderRepository);
    }

    @Test
    @DisplayName("Cenário 1: horário bloqueado some da disponibilidade, mesmo com regra semanal")
    void generateAvailableSlots_excludesBlockedTime() {
        AvailabilityService availabilityService = service(new AvailabilityMapper());

        LocalDate targetDate = LocalDate.now().plusDays(1);

        Availability weeklyRule = new Availability();
        weeklyRule.setDayOfWeek(targetDate.getDayOfWeek());
        weeklyRule.setStartTime(LocalTime.of(8, 0));
        weeklyRule.setEndTime(LocalTime.of(12, 0));
        weeklyRule.setIsAvailable(true);

        Availability block = new Availability();
        block.setSpecificDate(targetDate);
        block.setStartTime(LocalTime.of(9, 0));
        block.setEndTime(LocalTime.of(10, 0));
        block.setIsAvailable(false);

        ProviderProfile provider = new ProviderProfile();
        provider.setId(PROVIDER_ID);
        provider.setAvailabilitySlots(List.of(weeklyRule, block));

        com.ufrn.ppgti.servio.model.Service serviceEntity = new com.ufrn.ppgti.servio.model.Service();
        serviceEntity.setProvider(provider);
        serviceEntity.setDurationInMinutes(60);

        when(orderRepository.findByProvider_IdAndDateBetween(eq(PROVIDER_ID), any(), any()))
                .thenReturn(List.of());

        List<AvailableSlotDTO> slots = availabilityService.generateAvailableSlots(serviceEntity);

        List<LocalTime> timesOnTargetDate = slots.stream()
                .filter(s -> s.getDate().equals(targetDate))
                .map(AvailableSlotDTO::getTime)
                .toList();

        assertTrue(timesOnTargetDate.contains(LocalTime.of(8, 0)));
        assertFalse(timesOnTargetDate.contains(LocalTime.of(9, 0)), "09:00 deveria estar bloqueado");
        assertTrue(timesOnTargetDate.contains(LocalTime.of(10, 0)));
        assertTrue(timesOnTargetDate.contains(LocalTime.of(11, 0)));
    }

    @Test
    @DisplayName("Criação de bloqueio grava isAvailable=false e specificDate preenchida")
    void createBlock_persistsAsBlock() {
        AvailabilityMapper mapper = new AvailabilityMapper();
        AvailabilityService availabilityService = service(mapper);

        when(authService.getAuthenticadUser()).thenReturn(providerUser());
        when(providerRepository.findById(PROVIDER_ID)).thenReturn(Optional.of(providerProfile()));
        when(availabilityRepository.save(any(Availability.class))).thenAnswer(inv -> {
            Availability a = inv.getArgument(0);
            a.setId(1L);
            return a;
        });

        AvailabilityDTO dto = new AvailabilityDTO(null, null, LocalDate.now().plusDays(2),
                LocalTime.of(14, 0), LocalTime.of(15, 0), null);

        AvailabilityDTO result = availabilityService.createBlock(dto);

        assertEquals(Boolean.FALSE, result.getIsAvailable());
        assertEquals(LocalDate.now().plusDays(2), result.getSpecificDate());
    }

    @Test
    @DisplayName("Edge 1: intervalo inválido no bloqueio é recusado")
    void createBlock_invalidRange_throwsBusinessException() {
        AvailabilityService availabilityService = service(new AvailabilityMapper());
        when(authService.getAuthenticadUser()).thenReturn(providerUser());
        when(providerRepository.findById(PROVIDER_ID)).thenReturn(Optional.of(providerProfile()));

        AvailabilityDTO dto = new AvailabilityDTO(null, null, LocalDate.now().plusDays(2),
                LocalTime.of(15, 0), LocalTime.of(14, 0), null);

        assertThrows(BusinessException.class, () -> availabilityService.createBlock(dto));
        verify(availabilityRepository, never()).save(any());
    }

    @Test
    @DisplayName("listBlocks retorna só registros com specificDate + isAvailable=false")
    void listBlocks_filtersOnlyBlocks() {
        AvailabilityMapper mapper = new AvailabilityMapper();
        AvailabilityService availabilityService = service(mapper);

        when(authService.getAuthenticadUser()).thenReturn(providerUser());
        when(providerRepository.findById(PROVIDER_ID)).thenReturn(Optional.of(providerProfile()));

        Availability weekly = new Availability();
        weekly.setDayOfWeek(java.time.DayOfWeek.MONDAY);
        weekly.setStartTime(LocalTime.of(8, 0));
        weekly.setEndTime(LocalTime.of(9, 0));
        weekly.setIsAvailable(true);

        Availability extra = new Availability();
        extra.setSpecificDate(LocalDate.now().plusDays(3));
        extra.setStartTime(LocalTime.of(8, 0));
        extra.setEndTime(LocalTime.of(9, 0));
        extra.setIsAvailable(true);

        Availability block = new Availability();
        block.setId(9L);
        block.setSpecificDate(LocalDate.now().plusDays(4));
        block.setStartTime(LocalTime.of(13, 0));
        block.setEndTime(LocalTime.of(14, 0));
        block.setIsAvailable(false);

        when(availabilityRepository.findByProviderId(PROVIDER_ID)).thenReturn(List.of(weekly, extra, block));

        List<AvailabilityDTO> result = availabilityService.listBlocks();

        assertEquals(1, result.size());
        assertEquals(9L, result.get(0).getId());
    }

    @Test
    @DisplayName("Remover bloqueio próprio funciona")
    void removeBlock_owner_deletes() {
        AvailabilityService availabilityService = service(new AvailabilityMapper());
        when(authService.getAuthenticadUser()).thenReturn(providerUser());
        when(providerRepository.findById(PROVIDER_ID)).thenReturn(Optional.of(providerProfile()));

        Availability block = new Availability();
        block.setId(9L);
        block.setSpecificDate(LocalDate.now().plusDays(1));
        block.setStartTime(LocalTime.of(13, 0));
        block.setEndTime(LocalTime.of(14, 0));
        block.setIsAvailable(false);
        block.setProvider(providerProfile());

        when(availabilityRepository.findById(9L)).thenReturn(Optional.of(block));

        availabilityService.removeBlock(9L);

        verify(availabilityRepository).delete(block);
    }

    @Test
    @DisplayName("Edge 2: remover bloqueio de outro prestador retorna 403")
    void removeBlock_otherProvider_throws403() {
        AvailabilityService availabilityService = service(new AvailabilityMapper());
        when(authService.getAuthenticadUser()).thenReturn(providerUser());
        when(providerRepository.findById(PROVIDER_ID)).thenReturn(Optional.of(providerProfile()));

        ProviderProfile otherProvider = new ProviderProfile();
        otherProvider.setId(999L);

        Availability block = new Availability();
        block.setId(9L);
        block.setSpecificDate(LocalDate.now().plusDays(1));
        block.setStartTime(LocalTime.of(13, 0));
        block.setEndTime(LocalTime.of(14, 0));
        block.setIsAvailable(false);
        block.setProvider(otherProvider);

        when(availabilityRepository.findById(9L)).thenReturn(Optional.of(block));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> availabilityService.removeBlock(9L));
        assertEquals(403, ex.getStatusCode().value());
        verify(availabilityRepository, never()).delete(any(Availability.class));
    }

    @Test
    @DisplayName("Remover bloqueio inexistente retorna 404")
    void removeBlock_notFound_throws404() {
        AvailabilityService availabilityService = service(new AvailabilityMapper());
        when(authService.getAuthenticadUser()).thenReturn(providerUser());
        when(providerRepository.findById(PROVIDER_ID)).thenReturn(Optional.of(providerProfile()));
        when(availabilityRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> availabilityService.removeBlock(1L));
        assertEquals(404, ex.getStatusCode().value());
    }

    // ----- helpers -----

    private User providerUser() {
        User user = new User();
        user.setId(PROVIDER_ID);
        user.setRole(Role.PROVIDER);
        return user;
    }

    private ProviderProfile providerProfile() {
        ProviderProfile profile = new ProviderProfile();
        profile.setId(PROVIDER_ID);
        return profile;
    }
}
