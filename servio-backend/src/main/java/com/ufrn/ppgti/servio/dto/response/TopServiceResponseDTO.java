package com.ufrn.ppgti.servio.dto.response;

public class TopServiceResponseDTO {

    private Long serviceId;
    private String title;
    private Long completedOrders;
    private Double averageRating;
    private Long reviewCount;

    public TopServiceResponseDTO(
            Long serviceId,
            String title,
            Long completedOrders,
            Double averageRating,
            Long reviewCount) {
        this.serviceId = serviceId;
        this.title = title;
        this.completedOrders = completedOrders;
        this.averageRating = averageRating;
        this.reviewCount = reviewCount;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public String getTitle() {
        return title;
    }

    public Long getCompletedOrders() {
        return completedOrders;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public Long getReviewCount() {
        return reviewCount;
    }
}
