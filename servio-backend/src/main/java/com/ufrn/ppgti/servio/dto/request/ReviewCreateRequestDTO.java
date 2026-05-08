package com.ufrn.ppgti.servio.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ReviewCreateRequestDTO {

    @NotNull(message = "O pedido é obrigatório.")
    private Long orderId;

    @NotNull(message = "A nota é obrigatória.")
    @Min(value = 1, message = "A nota mínima é 1.")
    @Max(value = 5, message = "A nota máxima é 5.")
    private Integer rating;

    @Size(max = 500, message = "O comentário deve ter no máximo 500 caracteres.")
    private String comment;

    public Long getOrderId() {
        return orderId;
    }

    public Integer getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
