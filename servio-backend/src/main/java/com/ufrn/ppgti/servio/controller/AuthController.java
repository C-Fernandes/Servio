package com.ufrn.ppgti.servio.controller;

import com.ufrn.ppgti.servio.dto.request.LoginRequestDTO;
import com.ufrn.ppgti.servio.dto.request.RegisterRequestDTO;
import com.ufrn.ppgti.servio.dto.response.AuthResponseDTO;
import com.ufrn.ppgti.servio.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@RequestBody RegisterRequestDTO dto) {
        return ResponseEntity.ok(authService.register(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(authService.login(dto));
    }
}