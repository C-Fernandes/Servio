package com.ufrn.ppgti.servio.controller;

import com.ufrn.ppgti.servio.annotations.Client;
import com.ufrn.ppgti.servio.annotations.Provider;
import com.ufrn.ppgti.servio.dto.request.ReviewCreateRequestDTO;
import com.ufrn.ppgti.servio.dto.request.ReviewUpdateRequestDTO;
import com.ufrn.ppgti.servio.dto.response.ReviewResponseDTO;
import com.ufrn.ppgti.servio.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @Client
    @PostMapping
    public ResponseEntity<ReviewResponseDTO> create(@RequestBody @Valid ReviewCreateRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.create(dto));
    }

    @Client
    @GetMapping("/my-reviews")
    public ResponseEntity<List<ReviewResponseDTO>> findMyReviews() {
        return ResponseEntity.ok(reviewService.findMyReviews());
    }

    @Provider
    @GetMapping("/provider")
    public ResponseEntity<List<ReviewResponseDTO>> findProviderReviews() {
        return ResponseEntity.ok(reviewService.findProviderReviews());
    }

    @GetMapping("/service/{serviceId}")
    public ResponseEntity<List<ReviewResponseDTO>> findByService(@PathVariable Long serviceId) {
        return ResponseEntity.ok(reviewService.findByService(serviceId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.findById(id));
    }

    @Client
    @PutMapping("/{id}")
    public ResponseEntity<ReviewResponseDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid ReviewUpdateRequestDTO dto) {
        return ResponseEntity.ok(reviewService.update(id, dto));
    }

    @Client
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reviewService.delete(id);
        return ResponseEntity.noContent().build();
    }
}