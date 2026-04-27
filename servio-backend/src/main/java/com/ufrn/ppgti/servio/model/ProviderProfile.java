package com.ufrn.ppgti.servio.model;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "provider_profiles")

public class ProviderProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private String experience;
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "provider", cascade = CascadeType.ALL)
    private List<Service> services;

    @OneToMany(mappedBy = "provider", cascade = CascadeType.ALL)
    private List<AvailabilitySlot> availabilitySlots;

    @OneToMany(mappedBy = "provider")
    private List<FinancialTransaction> transactions;
}