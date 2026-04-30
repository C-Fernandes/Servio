package com.ufrn.ppgti.servio.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ufrn.ppgti.servio.dto.response.ServiceResponseDTO;
import com.ufrn.ppgti.servio.dto.request.ServiceRequestDTO;
import com.ufrn.ppgti.servio.exceptions.BusinessException;
import com.ufrn.ppgti.servio.repository.ServiceRepository;

import com.ufrn.ppgti.servio.mappers.ServiceMapper;

@Service
public class ServiceService {

    private final ServiceRepository repository;
    private final ServiceMapper mapper;

    public ServiceService(ServiceRepository repository, ServiceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<ServiceResponseDTO> findAll() {
        return repository.findAll().stream()
                .map(mapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ServiceResponseDTO findById(Long id) {
        return repository.findById(id)
                .map(mapper::toResponseDTO)
                .orElseThrow(() -> new BusinessException("Serviço não encontrado."));
    }

    @Transactional
    public ServiceResponseDTO save(ServiceRequestDTO dto) {
        validatePrice(dto.getPrice());

        com.ufrn.ppgti.servio.model.Service entity = mapper.toEntity(dto);
        entity = repository.save(entity);
        return mapper.toResponseDTO(entity);
    }

    @Transactional
    public ServiceResponseDTO update(Long id, ServiceRequestDTO dto) {
        validatePrice(dto.getPrice());

        com.ufrn.ppgti.servio.model.Service service = repository.findById(id)
                .orElseThrow(() -> new BusinessException("Serviço não encontrado."));

        service.setTitle(dto.getTitle());
        service.setDescription(dto.getDescription());
        service.setPrice(dto.getPrice());

        return mapper.toResponseDTO(repository.save(service));
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new BusinessException("Não é possível deletar: Serviço não encontrado.");
        }
        repository.deleteById(id);
    }

    private void validatePrice(Double price) {
        if (price == null || price <= 0) {
            throw new BusinessException("O preço deve ser um valor positivo.");
        }
    }
}