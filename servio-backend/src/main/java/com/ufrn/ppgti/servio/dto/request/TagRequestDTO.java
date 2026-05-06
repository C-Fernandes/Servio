package com.ufrn.ppgti.servio.dto.request;

import jakarta.validation.constraints.NotBlank;

public class TagRequestDTO {

    @NotBlank(message = "O nome da tag é obrigatório.")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}