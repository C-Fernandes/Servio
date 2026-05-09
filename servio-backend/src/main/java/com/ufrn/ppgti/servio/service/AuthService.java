package com.ufrn.ppgti.servio.service;

import com.ufrn.ppgti.servio.dto.request.LoginRequestDTO;
import com.ufrn.ppgti.servio.dto.request.RegisterRequestDTO;
import com.ufrn.ppgti.servio.dto.response.AuthResponseDTO;
import com.ufrn.ppgti.servio.exceptions.BusinessException;
import com.ufrn.ppgti.servio.model.ProviderProfile;
import com.ufrn.ppgti.servio.model.User;
import com.ufrn.ppgti.servio.model.enums.Role;
import com.ufrn.ppgti.servio.repository.UserRepository;
import com.ufrn.ppgti.servio.security.JwtService;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado");
        }

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        Role role = dto.getRole() != null ? dto.getRole() : Role.CLIENT;
        user.setRole(role);

        if (role == Role.PROVIDER) {
            ProviderProfile profile = new ProviderProfile();
            profile.setUser(user);
            profile.setBio(dto.getBio());
            profile.setExperience(dto.getExperience());
            user.setProviderProfile(profile);
        }

        userRepository.save(user);

        String token = jwtService.generateToken(user.getId(), user.getRole().name(), user.getName());

        return new AuthResponseDTO(token);
    }

    public AuthResponseDTO login(LoginRequestDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("Senha inválida");
        }

        String token = jwtService.generateToken(user.getId(), user.getRole().name(), user.getName());

        return new AuthResponseDTO(token);
    }

    public User getAuthenticadUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long providerId = Long.parseLong(authentication.getName());

        User user = userRepository.findById(providerId)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado."));
        return user;
    }
}