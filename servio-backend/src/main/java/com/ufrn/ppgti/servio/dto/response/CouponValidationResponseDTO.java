package com.ufrn.ppgti.servio.dto.response;

public class CouponValidationResponseDTO {

    private String code;
    private Double discountPercentage;
    private Double originalPrice;
    private Double finalPrice;

    public CouponValidationResponseDTO(String code, Double discountPercentage, Double originalPrice,
            Double finalPrice) {
        this.code = code;
        this.discountPercentage = discountPercentage;
        this.originalPrice = originalPrice;
        this.finalPrice = finalPrice;
    }

    public String getCode() {
        return code;
    }

    public Double getDiscountPercentage() {
        return discountPercentage;
    }

    public Double getOriginalPrice() {
        return originalPrice;
    }

    public Double getFinalPrice() {
        return finalPrice;
    }
}
