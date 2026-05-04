package com.ufrn.ppgti.servio.dto.response;

import java.util.List;

import com.ufrn.ppgti.servio.dto.AvailabilityDTO;
import com.ufrn.ppgti.servio.dto.AvailableSlotDTO;

public class ServiceResponseDTO {

    private Long id;

    private String title;
    private String description;
    private Double price;

    private String provider;

    private String image;
    private int durationInMinutes;
    private boolean active;
    private String category;
    private List<String> tags;
    private List<AvailableSlotDTO> availableSlots;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return this.price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getProvider() {
        return this.provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getImage() {
        return this.image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getDurationInMinutes() {
        return this.durationInMinutes;
    }

    public void setDurationInMinutes(int durationInMinutes) {
        this.durationInMinutes = durationInMinutes;
    }

    public boolean isActive() {
        return this.active;
    }

    public boolean getActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getCategory() {
        return this.category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<String> getTags() {
        return this.tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<AvailableSlotDTO> getAvailableSlots() {
        return this.availableSlots;
    }

    public void setAvailableSlots(List<AvailableSlotDTO> availableSlots) {
        this.availableSlots = availableSlots;
    }

}
