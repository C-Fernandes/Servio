package com.ufrn.ppgti.servio.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ufrn.ppgti.servio.model.Service;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {

    List<Service> findByProviderIdAndDeletedFalse(Long providerId);

    List<Service> findByActiveTrueAndDeletedFalse();

    Optional<Service> findByIdAndDeletedFalse(Long id);

    boolean existsByIdAndDeletedFalse(Long id);

    boolean existsByCategoryId(Long categoryId);

    boolean existsByTagsId(Long tagId);
}