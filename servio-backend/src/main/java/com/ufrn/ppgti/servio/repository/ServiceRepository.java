package com.ufrn.ppgti.servio.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ufrn.ppgti.servio.dto.LocalityDTO;
import com.ufrn.ppgti.servio.model.Service;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long>, JpaSpecificationExecutor<Service> {

    List<Service> findByProviderIdAndDeletedFalse(Long providerId);

    List<Service> findByActiveTrueAndDeletedFalse();

    Optional<Service> findByIdAndDeletedFalse(Long id);

    boolean existsByIdAndDeletedFalse(Long id);

    boolean existsByCategoryId(Long categoryId);

    boolean existsByTagsId(Long tagId);

    @Query("""
                SELECT DISTINCT new com.ufrn.ppgti.servio.dto.LocalityDTO(l.city, l.state)
                FROM Service s
                JOIN s.provider p
                JOIN p.user u
                JOIN u.locality l
                WHERE s.active = true AND s.deleted = false
                ORDER BY l.state, l.city
            """)
    List<LocalityDTO> findAvailableLocalities();
}
