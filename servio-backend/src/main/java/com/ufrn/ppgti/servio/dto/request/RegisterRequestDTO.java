package com.ufrn.ppgti.servio.dto.request;

import com.ufrn.ppgti.servio.model.enums.Role;

public class RegisterRequestDTO {

    private String name;
    private String email;
    private String password;
    private Role role;

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
}