package com.ufrn.ppgti.servio.service;

import com.ufrn.ppgti.servio.dto.request.UserUpdateRequestDTO;
import com.ufrn.ppgti.servio.dto.response.UserResponseDTO;
import com.ufrn.ppgti.servio.model.Locality;
import com.ufrn.ppgti.servio.model.User;
import com.ufrn.ppgti.servio.repository.LocalityRepository;
import com.ufrn.ppgti.servio.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final LocalityRepository localityRepository;

    public UserService(UserRepository userRepository, LocalityRepository localityRepository) {
        this.userRepository = userRepository;
        this.localityRepository = localityRepository;
    }

    public List<UserResponseDTO> findAll() {
        return userRepository.findAllByDeletedFalse()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public UserResponseDTO findById(Long id) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return toResponseDTO(user);
    }

    @Transactional
    public UserResponseDTO update(Long id, UserUpdateRequestDTO dto) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        user.setName(dto.getName());
        user.setPhone(dto.getPhone());
        user.setStreet(dto.getStreet());
        user.setNumber(dto.getNumber());
        user.setComplement(dto.getComplement());
        user.setNeighborhood(dto.getNeighborhood());

        if (dto.getZipCode() != null && !dto.getZipCode().isBlank()) {
            String normalizedZipCode = dto.getZipCode().replaceAll("\\D", "");
            String normalizedState = dto.getState() != null ? dto.getState().trim().toUpperCase() : null;
            String normalizedCity = dto.getCity() != null ? dto.getCity().trim() : null;

            if (normalizedCity == null || normalizedCity.isBlank()) {
                throw new RuntimeException("Cidade é obrigatória quando o CEP for informado");
            }

            if (normalizedState == null || normalizedState.isBlank()) {
                throw new RuntimeException("UF é obrigatória quando o CEP for informado");
            }

            Locality locality = localityRepository.findById(normalizedZipCode)
                    .map(existingLocality -> {
                        existingLocality.setCity(normalizedCity);
                        existingLocality.setState(normalizedState);
                        return localityRepository.save(existingLocality);
                    })
                    .orElseGet(() -> {
                        Locality newLocality = new Locality();
                        newLocality.setCep(normalizedZipCode);
                        newLocality.setCity(normalizedCity);
                        newLocality.setState(normalizedState);
                        return localityRepository.save(newLocality);
                    });

            user.setLocality(locality);
        } else {
            user.setLocality(null);
        }

        return toResponseDTO(userRepository.save(user));
    }

    private UserResponseDTO toResponseDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.getPhone(),
                user.getStreet(),
                user.getNumber(),
                user.getComplement(),
                user.getNeighborhood(),
                user.getLocality() != null ? user.getLocality().getCep() : null,
                user.getLocality() != null ? user.getLocality().getCity() : null,
                user.getLocality() != null ? user.getLocality().getState() : null
        );
    }

    @Transactional
    public void delete(Long id) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        user.setDeleted(true);
        userRepository.save(user);
    }
}