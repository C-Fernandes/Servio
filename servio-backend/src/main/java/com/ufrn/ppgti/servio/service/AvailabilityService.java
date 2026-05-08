package com.ufrn.ppgti.servio.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ufrn.ppgti.servio.dto.AvailabilityDTO;
import com.ufrn.ppgti.servio.dto.AvailableSlotDTO;
import com.ufrn.ppgti.servio.dto.CalendarDTO;
import com.ufrn.ppgti.servio.mappers.AvailabilityMapper;
import com.ufrn.ppgti.servio.model.Availability;
import com.ufrn.ppgti.servio.model.Order;
import com.ufrn.ppgti.servio.model.ProviderProfile;
import com.ufrn.ppgti.servio.model.User;
import com.ufrn.ppgti.servio.repository.AvailabilityRepository;
import com.ufrn.ppgti.servio.repository.OrderRepository;
import com.ufrn.ppgti.servio.repository.ProviderProfileRepository;

import jakarta.transaction.Transactional;

@Service
public class AvailabilityService {

    private final AvailabilityRepository availabilityRepository;
    private final ProviderProfileRepository providerRepository;
    private final AuthService authService;
    private final AvailabilityMapper mapper;
    private final OrderRepository orderRepository;

    public AvailabilityService(AvailabilityRepository availabilityRepository,
            ProviderProfileRepository providerRepository, AuthService authService, AvailabilityMapper mapper,
            OrderRepository orderRepository) {
        this.availabilityRepository = availabilityRepository;
        this.providerRepository = providerRepository;
        this.authService = authService;
        this.orderRepository = orderRepository;
        this.mapper = mapper;
    }

    @Transactional
    public void syncCalendar(CalendarDTO request) {
        ProviderProfile provider = getAuthenticatedProvider();
        List<Availability> existingAvailabilities = availabilityRepository.findByProviderId(provider.getId());

        List<Availability> toSave = new ArrayList<>();
        Set<Long> incomingIds = new HashSet<>();

        processWeeklyRules(request.getWeeklyRules(), existingAvailabilities, provider, toSave, incomingIds);
        processExtraSlots(request.getExtraSlots(), existingAvailabilities, provider, toSave, incomingIds);

        List<Availability> toDelete = determineDeletions(existingAvailabilities, incomingIds);

        availabilityRepository.deleteAll(toDelete);
        availabilityRepository.saveAll(toSave);
    }

    public CalendarDTO getCalendar() {
        User user = authService.getAuthenticadUser();
        List<Availability> allAvailabilities = availabilityRepository.findByProviderId(user.getId());
        return mapper.toCalendarRequest(allAvailabilities);
    }

    public List<AvailableSlotDTO> generateAvailableSlots(com.ufrn.ppgti.servio.model.Service service) {
        List<AvailableSlotDTO> allSlots = new ArrayList<>();

        if (service.getProvider() == null || service.getProvider().getAvailabilitySlots() == null) {
            return allSlots;
        }

        int duration = service.getDurationInMinutes() > 0 ? service.getDurationInMinutes() : 60;
        LocalDate today = LocalDate.now();
        List<Availability> allRules = service.getProvider().getAvailabilitySlots();

        List<Order> bookedOrders = orderRepository.findByProvider_IdAndDateBetween(
                service.getProvider().getId(), today, today.plusDays(7));

        for (int i = 0; i < 7; i++) {
            LocalDate currentDate = today.plusDays(i);

            List<Availability> dayRules = getRulesForDate(allRules, currentDate);

            allSlots.addAll(generateSlotsForDay(currentDate, dayRules, duration, bookedOrders));
        }
        allSlots.sort(Comparator.comparing(AvailableSlotDTO::getDate)
                .thenComparing(AvailableSlotDTO::getTime));
        return allSlots;
    }

    private List<Availability> getRulesForDate(List<Availability> allRules, LocalDate date) {
        List<Availability> dayRules = allRules.stream()
                .filter(a -> a.getSpecificDate() == null
                        && date.getDayOfWeek().equals(a.getDayOfWeek())
                        && a.getIsAvailable())
                .collect(Collectors.toList());

        dayRules.addAll(allRules.stream()
                .filter(a -> date.equals(a.getSpecificDate()) && a.getIsAvailable())
                .collect(Collectors.toList()));

        return dayRules;
    }

    private List<AvailableSlotDTO> generateSlotsForDay(
            LocalDate date,
            List<Availability> dayRules,
            int duration,
            List<Order> bookedOrders) {
        List<AvailableSlotDTO> dailySlots = new ArrayList<>();
        Set<LocalTime> generatedTimesForDay = new HashSet<>();

        LocalDateTime now = LocalDateTime.now();

        for (Availability rule : dayRules) {
            LocalTime slotTime = rule.getStartTime();
            LocalTime endTime = rule.getEndTime();

            while (!slotTime.plusMinutes(duration).isAfter(endTime)) {
                LocalTime currentSlotStart = slotTime;
                LocalTime currentSlotEnd = slotTime.plusMinutes(duration);

                LocalDateTime slotDateTime = LocalDateTime.of(date, currentSlotStart);

                boolean isPastOrNow = !slotDateTime.isAfter(now);
                boolean conflict = hasTimeConflict(date, currentSlotStart, currentSlotEnd, bookedOrders);

                if (!isPastOrNow && !conflict && !generatedTimesForDay.contains(currentSlotStart)) {
                    dailySlots.add(new AvailableSlotDTO(date, currentSlotStart));
                    generatedTimesForDay.add(currentSlotStart);
                }

                slotTime = slotTime.plusMinutes(duration);
            }
        }

        return dailySlots;
    }

    private boolean hasTimeConflict(LocalDate date, LocalTime start, LocalTime end, List<Order> bookedOrders) {
        return bookedOrders.stream()
                .anyMatch(order -> order.getDate().equals(date) &&
                        start.isBefore(order.getEndTime()) &&
                        end.isAfter(order.getStartTime()));
    }

    private ProviderProfile getAuthenticatedProvider() {
        User user = authService.getAuthenticadUser();
        return providerRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Prestador não encontrado"));
    }

    private void processWeeklyRules(List<AvailabilityDTO> rules, List<Availability> existing,
            ProviderProfile provider, List<Availability> toSave, Set<Long> incomingIds) {
        if (rules == null)
            return;

        for (AvailabilityDTO dto : rules) {
            Availability entity = matchOrCreate(existing, dto.getId());
            if (entity.getId() != null)
                incomingIds.add(entity.getId());

            entity.setProvider(provider);
            entity.setDayOfWeek(dto.getDayOfWeek());
            entity.setSpecificDate(null);
            entity.setIsAvailable(true);
            entity.setStartTime(dto.getStartTime());
            entity.setEndTime(dto.getEndTime());

            toSave.add(entity);
        }
    }

    private void processExtraSlots(List<AvailabilityDTO> extraSlots, List<Availability> existing,
            ProviderProfile provider, List<Availability> toSave, Set<Long> incomingIds) {
        if (extraSlots == null)
            return;

        for (AvailabilityDTO dto : extraSlots) {
            Availability entity = matchOrCreate(existing, dto.getId());
            if (entity.getId() != null)
                incomingIds.add(entity.getId());

            entity.setProvider(provider);
            entity.setDayOfWeek(null);
            entity.setSpecificDate(dto.getSpecificDate());
            entity.setIsAvailable(true);
            entity.setStartTime(dto.getStartTime());
            entity.setEndTime(dto.getEndTime());

            toSave.add(entity);
        }
    }

    private List<Availability> determineDeletions(List<Availability> existing, Set<Long> incomingIds) {
        return existing.stream()
                .filter(a -> a.getId() != null && !incomingIds.contains(a.getId()))
                .collect(Collectors.toList());
    }

    private Availability matchOrCreate(List<Availability> existing, Long id) {
        if (id == null) {
            return new Availability();
        }
        return existing.stream()
                .filter(a -> id.equals(a.getId()))
                .findFirst()
                .orElse(new Availability());
    }
}