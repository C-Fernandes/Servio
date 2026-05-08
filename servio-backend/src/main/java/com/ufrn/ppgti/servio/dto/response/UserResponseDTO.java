package com.ufrn.ppgti.servio.dto.response;

public class UserResponseDTO {

    private Long id;
    private String name;
    private String email;
    private String role;
    private String phone;
    private String street;
    private String number;
    private String complement;
    private String neighborhood;
    private String zipCode;
    private String city;
    private String state;

    public UserResponseDTO(
            Long id,
            String name,
            String email,
            String role,
            String phone,
            String street,
            String number,
            String complement,
            String neighborhood,
            String zipCode,
            String city,
            String state
    ) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.phone = phone;
        this.street = street;
        this.number = number;
        this.complement = complement;
        this.neighborhood = neighborhood;
        this.zipCode = zipCode;
        this.city = city;
        this.state = state;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getPhone() { return phone; }
    public String getStreet() { return street; }
    public String getNumber() { return number; }
    public String getComplement() { return complement; }
    public String getNeighborhood() { return neighborhood; }
    public String getZipCode() { return zipCode; }
    public String getCity() { return city; }
    public String getState() { return state; }
}