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
import com.ufrn.ppgti.servio.dto.response.FavoriteResponseDTO;
import com.ufrn.ppgti.servio.service.FavoriteService;

@RestController
@RequestMapping("/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @Client
    @PostMapping("/{serviceId}")
    public ResponseEntity<FavoriteResponseDTO> add(@PathVariable Long serviceId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(favoriteService.addFavorite(serviceId));
    }

    @Client
    @DeleteMapping("/{serviceId}")
    public ResponseEntity<Void> remove(@PathVariable Long serviceId) {
        favoriteService.removeFavorite(serviceId);
        return ResponseEntity.noContent().build();
    }

    @Client
    @GetMapping
    public ResponseEntity<List<FavoriteResponseDTO>> listMine() {
        return ResponseEntity.ok(favoriteService.listMyFavorites());
    }

    @Client
    @GetMapping("/check/{serviceId}")
    public ResponseEntity<Boolean> check(@PathVariable Long serviceId) {
        return ResponseEntity.ok(favoriteService.isFavorited(serviceId));
    }
}
