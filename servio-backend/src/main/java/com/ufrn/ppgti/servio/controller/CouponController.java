package com.ufrn.ppgti.servio.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ufrn.ppgti.servio.annotations.Client;
import com.ufrn.ppgti.servio.annotations.Provider;
import com.ufrn.ppgti.servio.dto.request.CouponCreateRequestDTO;
import com.ufrn.ppgti.servio.dto.response.CouponResponseDTO;
import com.ufrn.ppgti.servio.dto.response.CouponValidationResponseDTO;
import com.ufrn.ppgti.servio.service.CouponService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/coupons")
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @Provider
    @PostMapping
    public ResponseEntity<CouponResponseDTO> create(@Valid @RequestBody CouponCreateRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(couponService.create(dto));
    }

    @Provider
    @GetMapping("/my-coupons")
    public ResponseEntity<List<CouponResponseDTO>> listMine() {
        return ResponseEntity.ok(couponService.listMyCoupons());
    }

    @Provider
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        couponService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @Client
    @GetMapping("/validate")
    public ResponseEntity<CouponValidationResponseDTO> validate(
            @RequestParam String code,
            @RequestParam Long serviceId) {
        return ResponseEntity.ok(couponService.validate(code, serviceId));
    }
}
