package com.ufrn.ppgti.servio.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ufrn.ppgti.servio.dto.CategoryDTO;
import com.ufrn.ppgti.servio.model.Category;
import com.ufrn.ppgti.servio.repository.CategoryRepository;

@RestController
@RequestMapping("/category")
public class CategoryController {
    private final CategoryRepository categoryRepository;

    public CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    public ResponseEntity<List<CategoryDTO>> findAll() {
        List<CategoryDTO> categories = categoryRepository.findAll().stream()
                .map(cat -> new CategoryDTO(cat.getId(), cat.getName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(categories);
    }
}
