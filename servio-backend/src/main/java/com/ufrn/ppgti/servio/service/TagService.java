package com.ufrn.ppgti.servio.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ufrn.ppgti.servio.dto.TagDTO;
import com.ufrn.ppgti.servio.dto.request.TagRequestDTO;
import com.ufrn.ppgti.servio.exceptions.BusinessException;
import com.ufrn.ppgti.servio.model.Tag;
import com.ufrn.ppgti.servio.repository.TagRepository;

@Service
public class TagService {

    private final TagRepository tagRepository;
    private final ServiceService serviceService;

    public TagService(TagRepository tagRepository, ServiceService serviceService) {
        this.tagRepository = tagRepository;
        this.serviceService = serviceService;
    }

    public List<TagDTO> findAll() {
        return tagRepository.findAll().stream()
                .map(tag -> new TagDTO(tag.getId(), tag.getName()))
                .toList();
    }

    public TagDTO findById(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Tag não encontrada."));

        return new TagDTO(tag.getId(), tag.getName());
    }

    public TagDTO create(TagRequestDTO dto) {
        if (tagRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new BusinessException("Já existe uma tag com esse nome.");
        }

        Tag tag = new Tag();
        tag.setName(dto.getName());

        tag = tagRepository.save(tag);

        return new TagDTO(tag.getId(), tag.getName());
    }

    public TagDTO update(Long id, TagRequestDTO dto) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Tag não encontrada."));

        tagRepository.findByNameIgnoreCase(dto.getName())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new BusinessException("Já existe uma tag com esse nome.");
                });

        tag.setName(dto.getName());
        tag = tagRepository.save(tag);

        return new TagDTO(tag.getId(), tag.getName());
    }

    public void delete(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Tag não encontrada."));

        if (serviceService.existsByTagId(id)) {
            throw new BusinessException(
                    "Não é possível excluir esta tag, pois existem serviços cadastrados usando ela.");
        }

        tagRepository.delete(tag);
    }
}