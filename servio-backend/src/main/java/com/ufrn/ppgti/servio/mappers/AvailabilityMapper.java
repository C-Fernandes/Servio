package com.ufrn.ppgti.servio.mappers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.ufrn.ppgti.servio.dto.AvailabilityDTO;
import com.ufrn.ppgti.servio.dto.CalendarDTO;
import com.ufrn.ppgti.servio.model.Availability;
import com.ufrn.ppgti.servio.model.ProviderProfile;

@Component
public class AvailabilityMapper {

    public AvailabilityDTO toDTO(Availability entity) {
        if (entity == null)
            return null;

        AvailabilityDTO dto = new AvailabilityDTO();
        dto.setId(entity.getId());
        dto.setDayOfWeek(entity.getDayOfWeek());
        dto.setSpecificDate(entity.getSpecificDate());
        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());
        dto.setIsAvailable(entity.getIsAvailable());

        return dto;
    }

    public Availability toEntity(AvailabilityDTO dto, ProviderProfile provider) {
        if (dto == null)
            return null;

        Availability entity = new Availability();
        entity.setDayOfWeek(dto.getDayOfWeek());
        entity.setSpecificDate(dto.getSpecificDate());
        entity.setStartTime(dto.getStartTime());
        entity.setEndTime(dto.getEndTime());
        entity.setIsAvailable(true);
        entity.setProvider(provider);

        return entity;
    }

    public CalendarDTO toCalendarRequest(List<Availability> entities) {
        CalendarDTO request = new CalendarDTO();
        List<AvailabilityDTO> weekly = new ArrayList<>();
        List<AvailabilityDTO> extra = new ArrayList<>();

        for (Availability entity : entities) {
            AvailabilityDTO dto = toDTO(entity);
            if (entity.getDayOfWeek() != null) {
                weekly.add(dto);
            } else {
                extra.add(dto);
            }
        }

        request.setWeeklyRules(weekly);
        request.setExtraSlots(extra);
        return request;
    }
}
