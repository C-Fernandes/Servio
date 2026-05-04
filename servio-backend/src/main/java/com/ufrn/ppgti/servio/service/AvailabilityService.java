package com.ufrn.ppgti.servio.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ufrn.ppgti.servio.dto.AvailabilityDTO;
import com.ufrn.ppgti.servio.dto.CalendarDTO;
import com.ufrn.ppgti.servio.mappers.AvailabilityMapper;
import com.ufrn.ppgti.servio.model.Availability;
import com.ufrn.ppgti.servio.model.ProviderProfile;
import com.ufrn.ppgti.servio.model.User;
import com.ufrn.ppgti.servio.repository.AvailabilityRepository;
import com.ufrn.ppgti.servio.repository.ProviderProfileRepository;

import jakarta.transaction.Transactional;

@Service
public class AvailabilityService {

    private final AvailabilityRepository availabilityRepository;
    private final ProviderProfileRepository providerRepository;
    private final AuthService authService;
    private final AvailabilityMapper mapper;

    public AvailabilityService(AvailabilityRepository availabilityRepository,
            ProviderProfileRepository providerRepository, AuthService authService, AvailabilityMapper mapper) {
        this.availabilityRepository = availabilityRepository;
        this.providerRepository = providerRepository;
        this.authService = authService;
        this.mapper = mapper;
    }

    @Transactional
    public void syncCalendar(CalendarDTO request) {
        User user = authService.getAuthenticadUser();
        ProviderProfile provider = providerRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Prestador não encontrado"));
        availabilityRepository.deleteByProviderIdAndOrderIsNull(user.getId());

        List<Availability> toSave = new ArrayList<>();
        if (request.getWeeklyRules() != null) {
            for (AvailabilityDTO dto : request.getWeeklyRules()) {
                Availability entity = new Availability();
                entity.setProvider(provider);
                entity.setDayOfWeek(dto.getDayOfWeek());
                entity.setIsAvailable(true);

                entity.setStartTime(dto.getStartTime());
                entity.setEndTime(dto.getEndTime());

                toSave.add(entity);
            }
        }

        if (request.getExtraSlots() != null) {
            for (AvailabilityDTO dto : request.getExtraSlots()) {
                Availability entity = new Availability();
                entity.setProvider(provider);
                entity.setSpecificDate(dto.getSpecificDate());
                entity.setIsAvailable(true);

                entity.setStartTime(dto.getStartTime());
                entity.setEndTime(dto.getEndTime());

                toSave.add(entity);
            }
        }

        availabilityRepository.saveAll(toSave);
    }

    public CalendarDTO getCalendar() {
        User user = authService.getAuthenticadUser();

        List<Availability> allAvailabilities = availabilityRepository.findByProviderId(user.getId());

        return mapper.toCalendarRequest(allAvailabilities);
    }

}