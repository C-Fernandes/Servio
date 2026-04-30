package com.ufrn.ppgti.servio.mappers;

import org.springframework.stereotype.Component;

import com.ufrn.ppgti.servio.dto.response.ProviderProfileResponseDTO;
import com.ufrn.ppgti.servio.model.Locality;
import com.ufrn.ppgti.servio.model.ProviderProfile;
import com.ufrn.ppgti.servio.model.User;

@Component
public class ProviderProfileMapper {

    public ProviderProfileResponseDTO toResponseDTO(ProviderProfile entity) {
        if (entity == null)
            return null;

        ProviderProfileResponseDTO dto = new ProviderProfileResponseDTO();
        dto.setId(entity.getId());
        dto.setBio(entity.getBio());
        dto.setExperience(entity.getExperience());

        User user = entity.getUser();
        if (user != null) {
            dto.setName(user.getName());
            dto.setPhone(user.getPhone());
            dto.setStreet(user.getStreet());
            dto.setNumber(user.getNumber());
            dto.setComplement(user.getComplement());
            dto.setNeighborhood(user.getNeighborhood());

            Locality locality = user.getLocality();
            if (locality != null) {
                dto.setCep(locality.getCep());
                dto.setCity(locality.getCity());
                dto.setState(locality.getState());
            }
        }

        return dto;
    }
}