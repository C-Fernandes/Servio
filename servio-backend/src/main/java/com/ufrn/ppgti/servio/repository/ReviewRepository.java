package com.ufrn.ppgti.servio.repository;

import com.ufrn.ppgti.servio.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("""
                SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
                FROM Review r
                WHERE r.order.id = :orderId
            """)
    boolean existsByOrderId(@Param("orderId") Long orderId);

    @Query("""
                SELECT r
                FROM Review r
                JOIN FETCH r.order o
                JOIN FETCH o.client c
                JOIN FETCH o.service s
                JOIN FETCH s.provider p
                WHERE o.id = :orderId
            """)
    Optional<Review> findByOrderId(@Param("orderId") Long orderId);

    @Query("""
                SELECT r
                FROM Review r
                JOIN FETCH r.order o
                JOIN FETCH o.client c
                JOIN FETCH o.service s
                JOIN FETCH s.provider p
                WHERE c.id = :clientId
                ORDER BY r.createdAt DESC
            """)
    List<Review> findByClientId(@Param("clientId") Long clientId);

    @Query("""
                SELECT r
                FROM Review r
                JOIN FETCH r.order o
                JOIN FETCH o.client c
                JOIN FETCH o.service s
                JOIN FETCH s.provider p
                WHERE s.id = :serviceId
                ORDER BY r.createdAt DESC
            """)
    List<Review> findByServiceId(@Param("serviceId") Long serviceId);

    @Query("""
                SELECT r
                FROM Review r
                JOIN FETCH r.order o
                JOIN FETCH o.client c
                JOIN FETCH o.service s
                JOIN FETCH s.provider p
                WHERE p.id = :providerId
                ORDER BY r.createdAt DESC
            """)
    List<Review> findByProviderId(@Param("providerId") Long providerId);

    @Query("""
                SELECT r
                FROM Review r
                JOIN FETCH r.order o
                JOIN FETCH o.client c
                JOIN FETCH o.service s
                JOIN FETCH s.provider p
                WHERE r.id = :reviewId
            """)
    Optional<Review> findByIdWithRelations(@Param("reviewId") Long reviewId);
}