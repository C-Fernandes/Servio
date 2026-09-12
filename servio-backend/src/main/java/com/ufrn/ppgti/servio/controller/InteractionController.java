package com.ufrn.ppgti.servio.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ufrn.ppgti.servio.annotations.Client;
import com.ufrn.ppgti.servio.dto.response.InteractionEventDTO;
import com.ufrn.ppgti.servio.service.InteractionService;

@RestController
@RequestMapping("/interactions")
public class InteractionController {

    private final InteractionService interactionService;

    public InteractionController(InteractionService interactionService) {
        this.interactionService = interactionService;
    }

    @Client
    @GetMapping("/{otherUserId}")
    public ResponseEntity<List<InteractionEventDTO>> history(@PathVariable Long otherUserId) {
        return ResponseEntity.ok(interactionService.getHistory(otherUserId));
    }
}
