package com.ufrn.ppgti.servio.mappers;

import com.ufrn.ppgti.servio.dto.CategoryDTO;
import com.ufrn.ppgti.servio.dto.TagDTO;
import com.ufrn.ppgti.servio.dto.request.ServiceRequestDTO;
import com.ufrn.ppgti.servio.dto.response.ProviderProfileResponseDTO;
import com.ufrn.ppgti.servio.dto.response.ServiceResponseDTO;
import com.ufrn.ppgti.servio.model.Category;
import com.ufrn.ppgti.servio.model.ProviderProfile;
import com.ufrn.ppgti.servio.model.Service;
import com.ufrn.ppgti.servio.model.Tag;

import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class ServiceMapper {

    public Service toEntity(ServiceRequestDTO dto) {
        if (dto == null)
            return null;

        Service service = new Service();
        service.setTitle(dto.getTitle());
        service.setDescription(dto.getDescription());
        service.setPrice(dto.getPrice());
        service.setImageUrl(dto.getImageUrl());
        service.setDurationInMinutes(dto.getDurationInMinutes());
        return service;
    }

    public ServiceResponseDTO toResponseDTO(Service entity) {
        if (entity == null)
            return null;

        ServiceResponseDTO dto = new ServiceResponseDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setPrice(entity.getPrice());
        dto.setDurationInMinutes(entity.getDurationInMinutes());
        dto.setActive(entity.isActive());

        if (entity.getCategory() != null) {
            dto.setCategory(entity.getCategory().getName());
        }

        if (entity.getProvider() != null && entity.getProvider().getUser() != null) {
            dto.setProvider(entity.getProvider().getUser().getName());
        }

        if (entity.getTags() != null) {
            dto.setTags(entity.getTags().stream()
                    .map(Tag::getName)
                    .collect(Collectors.toList()));
        }
        return dto;
    }
}