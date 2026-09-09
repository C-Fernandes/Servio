package com.ufrn.ppgti.servio.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ufrn.ppgti.servio.model.FavoriteService;

@Repository
public interface FavoriteServiceRepository extends JpaRepository<FavoriteService, Long> {

    List<FavoriteService> findByClientIdAndServiceDeletedFalseOrderByCreatedAtDesc(Long clientId);

    Optional<FavoriteService> findByClientIdAndServiceId(Long clientId, Long serviceId);

    boolean existsByClientIdAndServiceId(Long clientId, Long serviceId);
}

