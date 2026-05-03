package com.ufrn.ppgti.servio.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.nio.file.Path;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.ufrn.ppgti.servio.dto.response.ServiceResponseDTO;
import com.ufrn.ppgti.servio.dto.request.ServiceRequestDTO;
import com.ufrn.ppgti.servio.exceptions.BusinessException;
import com.ufrn.ppgti.servio.repository.ServiceRepository;

import com.ufrn.ppgti.servio.mappers.ServiceMapper;
import com.ufrn.ppgti.servio.model.ProviderProfile;

@Service
public class ServiceService {

    private final String UPLOAD_DIR = "uploads/";

    private final ServiceRepository repository;
    private final ServiceMapper mapper;
    private final ProviderProfileService providerProfileService;

    public ServiceService(ServiceRepository repository, ServiceMapper mapper,
            ProviderProfileService providerProfileService) {
        this.repository = repository;
        this.mapper = mapper;
        this.providerProfileService = providerProfileService;
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
    public ServiceResponseDTO save(ServiceRequestDTO dto, MultipartFile image) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long providerId = Long.parseLong(authentication.getName());

        ProviderProfile provider = providerProfileService.findByIdEntity(providerId);
        validatePrice(dto.getPrice());

        if (image != null && !image.isEmpty()) {
            String imageUrl = saveImageToDisk(image);
            dto.setImageUrl(imageUrl);
        }

        com.ufrn.ppgti.servio.model.Service entity = mapper.toEntity(dto);
        entity.setProvider(provider);
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

    private String saveImageToDisk(MultipartFile image) {
        try {

            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = image.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFilename = UUID.randomUUID().toString() + extension;

            Path filePath = uploadPath.resolve(newFilename);
            Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/" + newFilename;

        } catch (IOException e) {
            throw new BusinessException("Erro ao salvar a imagem do serviço.");
        }
    }
}