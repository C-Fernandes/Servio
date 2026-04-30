package com.ufrn.ppgti.servio.dto.response;

import java.util.List;

import com.ufrn.ppgti.servio.model.Category;
import com.ufrn.ppgti.servio.model.ProviderProfile;

public class ServiceResponseDTO {

    private Long id;

    private String title;
    private String description;
    private Double price;

    private ProviderProfile provider;

    private List<Category> categories;

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

    public ProviderProfile getProvider() {
        return this.provider;
    }

    public void setProvider(ProviderProfile provider) {
        this.provider = provider;
    }

    public List<Category> getCategories() {
        return this.categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

}
