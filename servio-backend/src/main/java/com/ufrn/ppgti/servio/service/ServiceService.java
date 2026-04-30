package com.ufrn.ppgti.servio.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ufrn.ppgti.servio.repository.ServiceRepository;

@Service
public class ServiceService {

    private final ServiceRepository repository;

    public ServiceService(@Autowired ServiceRepository repository) {
        this.repository = repository;
    }

    public List<Service> findAll() {
        return repository.findAll();
    }

    public Service findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("Serviço não encontrado."));
    }

    public Service save(Service service) {
        if (service.getPrice() == null || service.getPrice() <= 0) {
            throw new BusinessException("O preço deve ser um valor positivo.");
        }
        return repository.save(service);
    }

    public Service update(Long id, Service serviceDetails) {
        Service service = findById(id);
        service.setTitle(serviceDetails.getTitle());
        service.setPrice(serviceDetails.getPrice());
        // Aqui você pode adicionar outros campos como description conforme necessário
        return repository.save(service);
    }

    public void delete(Long id) {
        Service service = findById(id);
        repository.delete(service);
    }
}