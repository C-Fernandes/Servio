package com.ufrn.ppgti.servio.mappers;

import com.ufrn.ppgti.servio.dto.CategoryDTO;
import com.ufrn.ppgti.servio.dto.request.ServiceRequestDTO;
import com.ufrn.ppgti.servio.dto.response.ProviderProfileResponseDTO;
import com.ufrn.ppgti.servio.dto.response.ServiceResponseDTO;
import com.ufrn.ppgti.servio.model.Category;
import com.ufrn.ppgti.servio.model.ProviderProfile;
import com.ufrn.ppgti.servio.model.Service;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class ServiceMapper {

    private final ProviderProfileMapper providerMapper;

    public ServiceMapper(ProviderProfileMapper providerMapper) {
        this.providerMapper = providerMapper;
    }

    public Service toEntity(ServiceRequestDTO dto) {
        if (dto == null)
            return null;

        Service service = new Service();
        service.setTitle(dto.getTitle());
        service.setDescription(dto.getDescription());
        service.setPrice(dto.getPrice());
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

        // Mapeia o perfil do prestador usando o mapper especializado
        dto.setProvider(providerMapper.toResponseDTO(entity.getProvider()));

        if (entity.getCategories() != null) {
            dto.setCategories(entity.getCategories().stream()
                    .map(cat -> {
                        return new CategoryDTO(cat.getId(), cat.getName());
                    })
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}