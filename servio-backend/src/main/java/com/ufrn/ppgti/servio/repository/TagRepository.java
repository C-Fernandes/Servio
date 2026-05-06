package com.ufrn.ppgti.servio.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ufrn.ppgti.servio.model.Tag;

public interface TagRepository extends JpaRepository<Tag, Long> {
    boolean existsByNameIgnoreCase(String name);
    Optional<Tag> findByNameIgnoreCase(String name);
}