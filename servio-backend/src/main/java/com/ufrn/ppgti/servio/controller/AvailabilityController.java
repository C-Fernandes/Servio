package com.ufrn.ppgti.servio.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}