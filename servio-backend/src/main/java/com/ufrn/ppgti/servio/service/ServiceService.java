package com.ufrn.ppgti.servio.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.nio.file.Path;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.ufrn.ppgti.servio.dto.response.ServiceResponseDTO;
import com.ufrn.ppgti.servio.dto.request.ServiceRequestDTO;
import com.ufrn.ppgti.servio.exceptions.BusinessException;
import com.ufrn.ppgti.servio.repository.CategoryRepository;
import com.ufrn.ppgti.servio.repository.ServiceRepository;
import com.ufrn.ppgti.servio.repository.TagRepository;
import com.ufrn.ppgti.servio.mappers.ServiceMapper;
import com.ufrn.ppgti.servio.model.Category;
import com.ufrn.ppgti.servio.model.ProviderProfile;
import com.ufrn.ppgti.servio.model.Tag;
import com.ufrn.ppgti.servio.model.User;

@Service
public class ServiceService {

    private final String UPLOAD_DIR = "uploads/";

    private final ServiceRepository repository;
    private final ServiceMapper mapper;
    private final AuthService authService;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;

    public ServiceService(ServiceRepository repository, ServiceMapper mapper,
            AuthService authService, CategoryRepository categoryRepository, TagRepository tagRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.authService = authService;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
    }

    @Transactional(readOnly = true)
    public List<ServiceResponseDTO> findAll() {
        return repository.findAll().stream()
                .map(entity -> {
                    ServiceResponseDTO dto = mapper.toResponseDTO(entity);

                    dto.setImage(extractBase64(entity.getImageUrl()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ServiceResponseDTO findById(Long id) {
        com.ufrn.ppgti.servio.model.Service entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException("Serviço não encontrado."));

        ServiceResponseDTO dto = mapper.toResponseDTO(entity);
        dto.setImage(extractBase64(entity.getImageUrl()));
        return dto;
    }

    @Transactional
    public ServiceResponseDTO save(ServiceRequestDTO dto, MultipartFile image) {
        User user = authService.getAuthenticadUser();

        if (user.getProviderProfile() == null) {
            throw new BusinessException("Apenas prestadores podem cadastrar serviços.");
        }

        ProviderProfile provider = user.getProviderProfile();
        validatePrice(dto.getPrice());

        if (image != null && !image.isEmpty()) {
            String imageUrl = saveImageToDisk(image);
            dto.setImageUrl(imageUrl);
        }

        com.ufrn.ppgti.servio.model.Service entity = mapper.toEntity(dto);
        entity.setProvider(provider);
        Category category = categoryRepository.findById(dto.getCategory())
                .orElseThrow(() -> new BusinessException("Categoria não encontrada."));
        entity.setCategory(category);
        System.out.println("Tags IDs recebidos: " + dto.getTags());
        if (dto.getTags() != null && !dto.getTags().isEmpty()) {
            List<Tag> tags = tagRepository.findAllById(dto.getTags());
            entity.setTags(tags);
        }
        entity = repository.save(entity);
        ServiceResponseDTO responseDTO = mapper.toResponseDTO(entity);

        responseDTO.setImage(extractBase64(entity.getImageUrl()));
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

    private String extractBase64(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }

        try {
            String fileName = imageUrl.startsWith("/") ? imageUrl.substring(1) : imageUrl;

            Path path = Paths.get(fileName);

            if (Files.exists(path)) {
                byte[] bytes = Files.readAllBytes(path);
                return Base64.getEncoder().encodeToString(bytes);
            }
        } catch (IOException e) {
            System.err.println("Erro ao converter imagem para Base64: " + e.getMessage());
        }
        return null;
    }
}