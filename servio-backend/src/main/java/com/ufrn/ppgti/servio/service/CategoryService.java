package com.ufrn.ppgti.servio.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ufrn.ppgti.servio.dto.CategoryDTO;
import com.ufrn.ppgti.servio.dto.request.CategoryRequestDTO;
import com.ufrn.ppgti.servio.exceptions.BusinessException;
import com.ufrn.ppgti.servio.model.Category;
import com.ufrn.ppgti.servio.repository.CategoryRepository;
import com.ufrn.ppgti.servio.repository.ServiceRepository;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ServiceService serviceService;

    public CategoryService(CategoryRepository categoryRepository, ServiceService serviceService) {
        this.categoryRepository = categoryRepository;
        this.serviceService = serviceService;
    }

    public List<CategoryDTO> findAll() {
        return categoryRepository.findAll().stream()
                .map(category -> new CategoryDTO(category.getId(), category.getName()))
                .toList();
    }

    public CategoryDTO findById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Categoria não encontrada."));

        return new CategoryDTO(category.getId(), category.getName());
    }

    public CategoryDTO create(CategoryRequestDTO dto) {
        if (categoryRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new BusinessException("Já existe uma categoria com esse nome.");
        }

        Category category = new Category();
        category.setName(dto.getName());

        category = categoryRepository.save(category);

        return new CategoryDTO(category.getId(), category.getName());
    }

    public CategoryDTO update(Long id, CategoryRequestDTO dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Categoria não encontrada."));

        categoryRepository.findByNameIgnoreCase(dto.getName())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new BusinessException("Já existe uma categoria com esse nome.");
                });

        category.setName(dto.getName());
        category = categoryRepository.save(category);

        return new CategoryDTO(category.getId(), category.getName());
    }

    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Categoria não encontrada."));

        if (serviceService.existsByCategoryId(id)) {
            throw new BusinessException(
                    "Não é possível excluir esta categoria, pois existem serviços cadastrados usando ela.");
        }

        categoryRepository.delete(category);
    }
}