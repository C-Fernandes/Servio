package com.ufrn.ppgti.servio.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ufrn.ppgti.servio.annotations.Client;
import com.ufrn.ppgti.servio.dto.response.ServiceResponseDTO;
import com.ufrn.ppgti.servio.service.FavoriteServiceService;

@RestController
@RequestMapping("/favorites")
public class FavoriteServiceController {

    private final FavoriteServiceService favoriteService;

    public FavoriteServiceController(FavoriteServiceService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @Client
    @GetMapping
    public ResponseEntity<List<ServiceResponseDTO>> findMyFavorites() {
        return ResponseEntity.ok(favoriteService.findCurrentUserFavorites());
    }

    @Client
    @PostMapping("/{serviceId}")
    public ResponseEntity<ServiceResponseDTO> add(@PathVariable Long serviceId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(favoriteService.add(serviceId));
    }

    @Client
    @DeleteMapping("/{serviceId}")
    public ResponseEntity<Void> remove(@PathVariable Long serviceId) {
        favoriteService.remove(serviceId);
        return ResponseEntity.noContent().build();
    }
}

