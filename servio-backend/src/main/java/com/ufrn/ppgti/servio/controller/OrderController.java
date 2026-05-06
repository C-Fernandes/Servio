package com.ufrn.ppgti.servio.controller;

import java.util.List;

import com.ufrn.ppgti.servio.dto.request.OrderCreateRequestDTO;
import com.ufrn.ppgti.servio.dto.request.OrderStatusUpdateRequestDTO;
import com.ufrn.ppgti.servio.dto.response.OrderResponseDTO;
import com.ufrn.ppgti.servio.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponseDTO> create(@RequestBody @Valid OrderCreateRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.create(dto));
    }

    @GetMapping("/my-orders")
    public ResponseEntity<List<OrderResponseDTO>> findMyOrders() {
        return ResponseEntity.ok(orderService.findMyOrdersAsClient());
    }

    @GetMapping("/provider")
    public ResponseEntity<List<OrderResponseDTO>> findProviderOrders() {
        return ResponseEntity.ok(orderService.findMyOrdersAsProvider());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponseDTO> updateStatus(
            @PathVariable Long id,
            @RequestBody @Valid OrderStatusUpdateRequestDTO dto) {
        return ResponseEntity.ok(orderService.updateStatus(id, dto.getStatus()));
    }
}