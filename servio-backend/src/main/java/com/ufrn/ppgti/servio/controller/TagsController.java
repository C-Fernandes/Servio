package com.ufrn.ppgti.servio.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ufrn.ppgti.servio.dto.TagDTO;
import com.ufrn.ppgti.servio.repository.TagRepository;

@RestController
@RequestMapping("/tags")
public class TagsController {

    private final TagRepository tagRepository;

    public TagsController(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @GetMapping
    public ResponseEntity<List<TagDTO>> findAll() {
        List<TagDTO> tags = tagRepository.findAll()
                .stream()
                .map(tag -> new TagDTO(tag.getId(), tag.getName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(tags);
    }

}
