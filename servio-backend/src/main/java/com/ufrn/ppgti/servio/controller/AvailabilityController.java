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
import org.springframework.web.bind.annotation.RestController;

import com.ufrn.ppgti.servio.annotations.Provider;
import com.ufrn.ppgti.servio.dto.AvailabilityDTO;
import com.ufrn.ppgti.servio.dto.CalendarDTO;
import com.ufrn.ppgti.servio.service.AvailabilityService;

@RestController
@RequestMapping("/api/calendar")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @PostMapping("/sync")
    public ResponseEntity<Void> sync(@RequestBody CalendarDTO request) {
        availabilityService.syncCalendar(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/calendar")
    public ResponseEntity<CalendarDTO> getCalendar() {
        CalendarDTO calendar = availabilityService.getCalendar();
        return ResponseEntity.ok(calendar);
    }

    @Provider
    @PostMapping("/blocks")
    public ResponseEntity<AvailabilityDTO> createBlock(@RequestBody AvailabilityDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(availabilityService.createBlock(dto));
    }

    @Provider
    @GetMapping("/blocks")
    public ResponseEntity<List<AvailabilityDTO>> listBlocks() {
        return ResponseEntity.ok(availabilityService.listBlocks());
    }

    @Provider
    @DeleteMapping("/blocks/{id}")
    public ResponseEntity<Void> removeBlock(@PathVariable Long id) {
        availabilityService.removeBlock(id);
        return ResponseEntity.noContent().build();
    }
}