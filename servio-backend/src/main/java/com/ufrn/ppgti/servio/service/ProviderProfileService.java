package com.ufrn.ppgti.servio.service;

import org.springframework.stereotype.Service;

import com.ufrn.ppgti.servio.model.ProviderProfile;
import com.ufrn.ppgti.servio.repository.ProviderProfileRepository;

@Service
public class ProviderProfileService {
    private final ProviderProfileRepository providerProfileRepository;

    public ProviderProfileService(ProviderProfileRepository providerProfileRepository) {
        this.providerProfileRepository = providerProfileRepository;
    }

    public ProviderProfile findByIdEntity(Long id) {
        return providerProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prestador não encontrado"));
    }
}
