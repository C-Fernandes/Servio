package com.ufrn.ppgti.servio.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ufrn.ppgti.servio.model.Favorite;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    boolean existsByUserIdAndServiceId(Long userId, Long serviceId);

    Optional<Favorite> findByUserIdAndServiceId(Long userId, Long serviceId);

    @Query("""
                SELECT f
                FROM Favorite f
                JOIN FETCH f.service s
                LEFT JOIN FETCH s.category c
                LEFT JOIN FETCH s.provider p
                LEFT JOIN FETCH p.user u
                WHERE f.user.id = :userId
                ORDER BY f.createdAt DESC
            """)
    List<Favorite> findByUserIdWithService(@Param("userId") Long userId);
}
