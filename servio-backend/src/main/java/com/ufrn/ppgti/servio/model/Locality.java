package com.ufrn.ppgti.servio.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Locality {

    @Id
    @Column(length = 8)
    private String cep;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false, length = 2)
    private String state;

    public String getCep() {
        return this.cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getCity() {
        return this.city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return this.state;
    }

    public void setState(String state) {
        this.state = state;
    }

}