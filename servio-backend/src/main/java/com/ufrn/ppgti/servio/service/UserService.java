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

        if (dto.getLocalityId() != null) {
            Locality locality = localityRepository.findById(dto.getLocalityId())
                    .orElseThrow(() -> new RuntimeException("Localidade não encontrada"));
            user.setLocality(locality);
        } else {
            user.setLocality(null);
        }

        return toResponseDTO(userRepository.save(user));
    }

    @Transactional
    public void delete(Long id) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        user.setDeleted(true);
        userRepository.save(user);
    }

    private UserResponseDTO toResponseDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }
}