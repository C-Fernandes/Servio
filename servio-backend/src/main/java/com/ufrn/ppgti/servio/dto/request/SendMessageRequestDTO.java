package com.ufrn.ppgti.servio.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SendMessageRequestDTO {

    @NotBlank(message = "A mensagem não pode estar vazia.")
    @Size(max = 2000, message = "A mensagem deve ter no máximo 2000 caracteres.")
    private String content;

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
