package com.ufrn.ppgti.servio.dto.request;

import java.util.List;

import com.ufrn.ppgti.servio.dto.CategoryDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class ServiceRequestDTO {

    @NotBlank(message = "O título é obrigatório.")
    @Size(min = 5, max = 100, message = "O título deve ter entre 5 e 100 caracteres.")
    private String title;

    @NotBlank(message = "A descrição é obrigatória.")
    @Size(min = 10, message = "A descrição deve ter pelo menos 10 caracteres.")
    private String description;

    @NotNull(message = "O preço é obrigatório.")
    @Positive(message = "O preço deve ser um valor positivo.")
    private Double price;

    @NotNull(message = "A categoria é obrigatória.")
    private Long category;
    private List<Long> tags;
    private String imageUrl;
    private int durationInMinutes;

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

    public Long getCategory() {
        return this.category;
    }

    public void setCategory(Long category) {
        this.category = category;
    }

    public List<Long> getTags() {
        return this.tags;
    }

    public void setTags(List<Long> tags) {
        this.tags = tags;
    }

    public String getImageUrl() {
        return this.imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getDurationInMinutes() {
        return this.durationInMinutes;
    }

    public void setDurationInMinutes(int durationInMinutes) {
        this.durationInMinutes = durationInMinutes;
    }

}
