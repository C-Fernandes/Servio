package com.ufrn.ppgti.servio.dto.request;

import com.ufrn.ppgti.servio.model.enums.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class RegisterRequestDTO {
    @NotBlank(message = "O nome é obrigatório")
    @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres")
    private String name;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "E-mail em formato inválido")
    private String email;

    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
    private String password;

    @NotNull(message = "O tipo de usuário é obrigatório")
    private Role role;

    @Size(max = 5000, message = "A bio não pode exceder 5000 caracteres")
    private String bio;

    @Min(value = 0, message = "Os anos de experiência não podem ser negativos")
    @Max(value = 70, message = "Valor de experiência inválido")
    private int experience;

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }

    public String getBio() {
        return bio;
    }

    public int getExperience() {
        return experience;
    }

}