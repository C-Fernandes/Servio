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

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    public AuthResponseDTO register(RegisterRequestDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("E-mail já cadastrado");
        }

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(dto.getRole() != null ? dto.getRole() : Role.CLIENT);

        userRepository.save(user);

        String token = jwtService.generateToken(user.getId());

        return new AuthResponseDTO(token);
    }

    public AuthResponseDTO login(LoginRequestDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("Senha inválida");
        }

        String token = jwtService.generateToken(user.getId());

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