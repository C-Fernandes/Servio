package com.ufrn.ppgti.servio.dto;

import java.util.List;

public class CalendarDTO {

    private List<AvailabilityDTO> weeklyRules;
    private List<AvailabilityDTO> extraSlots;

    public CalendarDTO() {
    }

    public List<AvailabilityDTO> getWeeklyRules() {
        return this.weeklyRules;
    }

    public void setWeeklyRules(List<AvailabilityDTO> weeklyRules) {
        this.weeklyRules = weeklyRules;
    }

    public List<AvailabilityDTO> getExtraSlots() {
        return this.extraSlots;
    }

    public void setExtraSlots(List<AvailabilityDTO> extraSlots) {
        this.extraSlots = extraSlots;
    }

}