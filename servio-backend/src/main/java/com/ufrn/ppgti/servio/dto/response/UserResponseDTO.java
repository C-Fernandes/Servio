package com.ufrn.ppgti.servio.dto.response;

import com.ufrn.ppgti.servio.model.enums.Role;

public class UserResponseDTO {

    private Long id;
    private String name;
    private String email;
    private Role role;

    public UserResponseDTO(Long id, String name, String email, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }
}