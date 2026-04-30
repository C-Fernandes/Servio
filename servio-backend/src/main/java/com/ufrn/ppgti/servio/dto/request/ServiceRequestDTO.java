package com.ufrn.ppgti.servio.dto.request;

import java.util.List;

import com.ufrn.ppgti.servio.dto.CategoryDTO;

public class ServiceRequestDTO {

    private String title;
    private String description;
    private Double price;

    private Long provider;

    private List<CategoryDTO> categories;

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

    public Long getProvider() {
        return this.provider;
    }

    public void setProvider(Long provider) {
        this.provider = provider;
    }

    public List<CategoryDTO> getCategories() {
        return this.categories;
    }

    public void setCategories(List<CategoryDTO> categories) {
        this.categories = categories;
    }

}
