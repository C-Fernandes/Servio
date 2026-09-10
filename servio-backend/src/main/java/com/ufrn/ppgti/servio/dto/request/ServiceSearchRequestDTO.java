package com.ufrn.ppgti.servio.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.PositiveOrZero;

public class ServiceSearchRequestDTO {

    private String term;

    private Long categoryId;

    @PositiveOrZero(message = "O preço mínimo não pode ser negativo.")
    private Double minPrice;

    @PositiveOrZero(message = "O preço máximo não pode ser negativo.")
    private Double maxPrice;

    @DecimalMin(value = "0", message = "A avaliação mínima deve estar entre 0 e 5.")
    @DecimalMax(value = "5", message = "A avaliação mínima deve estar entre 0 e 5.")
    private Double minRating;

    private String city;

    private String state;

    private String sortBy;

    public String getTerm() {
        return this.term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public Long getCategoryId() {
        return this.categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Double getMinPrice() {
        return this.minPrice;
    }

    public void setMinPrice(Double minPrice) {
        this.minPrice = minPrice;
    }

    public Double getMaxPrice() {
        return this.maxPrice;
    }

    public void setMaxPrice(Double maxPrice) {
        this.maxPrice = maxPrice;
    }

    public Double getMinRating() {
        return this.minRating;
    }

    public void setMinRating(Double minRating) {
        this.minRating = minRating;
    }

    public String getCity() {
        return this.city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return this.state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getSortBy() {
        return this.sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }
}
