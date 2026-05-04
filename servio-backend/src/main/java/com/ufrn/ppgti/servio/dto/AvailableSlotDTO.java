package com.ufrn.ppgti.servio.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class AvailableSlotDTO {
    private LocalDate date;
    private LocalTime time;

    public AvailableSlotDTO() {
    }

    public AvailableSlotDTO(LocalDate date, LocalTime time) {
        this.date = date;
        this.time = time;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }
}
