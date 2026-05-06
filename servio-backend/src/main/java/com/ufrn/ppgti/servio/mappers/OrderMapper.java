package com.ufrn.ppgti.servio.mappers;

import com.ufrn.ppgti.servio.dto.response.OrderResponseDTO;
import org.springframework.stereotype.Component;

import com.ufrn.ppgti.servio.model.Order;

@Component
public class OrderMapper {

    public OrderResponseDTO toResponseDTO(Order entity) {
        if (entity == null) {
            return null;
        }

        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(entity.getId());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setDate(entity.getDate());
        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());

        if (entity.getClient() != null) {
            dto.setClientId(entity.getClient().getId());
            dto.setClientName(entity.getClient().getName());
        }

        if (entity.getProvider() != null) {
            dto.setProviderId(entity.getProvider().getId());
            if (entity.getProvider().getUser() != null) {
                dto.setProviderName(entity.getProvider().getUser().getName());
            }
        }

        if (entity.getService() != null) {
            dto.setServiceId(entity.getService().getId());
            dto.setServiceTitle(entity.getService().getTitle());
            dto.setServicePrice(entity.getService().getPrice());
        }

        return dto;
    }
}
