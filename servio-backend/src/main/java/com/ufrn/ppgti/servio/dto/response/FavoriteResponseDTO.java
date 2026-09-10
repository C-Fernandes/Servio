package com.ufrn.ppgti.servio.dto.response;

import java.time.LocalDateTime;

public class FavoriteResponseDTO {

    private Long favoriteId;
    private LocalDateTime favoritedAt;

    private Long serviceId;
    private String title;
    private String description;
    private Double price;
    private int durationInMinutes;
    private boolean active;

    private String category;
    private Long providerId;
    private String provider;
    private String image;

    private Double averageRating;
    private Long reviewCount;

    public FavoriteResponseDTO(
            Long favoriteId,
            LocalDateTime favoritedAt,
            Long serviceId,
            String title,
            String description,
            Double price,
            int durationInMinutes,
            boolean active,
            String category,
            Long providerId,
            String provider,
            String image,
            Double averageRating,
            Long reviewCount) {
        this.favoriteId = favoriteId;
        this.favoritedAt = favoritedAt;
        this.serviceId = serviceId;
        this.title = title;
        this.description = description;
        this.price = price;
        this.durationInMinutes = durationInMinutes;
        this.active = active;
        this.category = category;
        this.providerId = providerId;
        this.provider = provider;
        this.image = image;
        this.averageRating = averageRating;
        this.reviewCount = reviewCount;
    }

    public Long getFavoriteId() {
        return favoriteId;
    }

    public LocalDateTime getFavoritedAt() {
        return favoritedAt;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Double getPrice() {
        return price;
    }

    public int getDurationInMinutes() {
        return durationInMinutes;
    }

    public boolean isActive() {
        return active;
    }

    public String getCategory() {
        return category;
    }

    public Long getProviderId() {
        return providerId;
    }

    public String getProvider() {
        return provider;
    }

    public String getImage() {
        return image;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public Long getReviewCount() {
        return reviewCount;
    }
}
