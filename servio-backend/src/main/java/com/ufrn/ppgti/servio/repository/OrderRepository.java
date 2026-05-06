package com.ufrn.ppgti.servio.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ufrn.ppgti.servio.model.Order;
import com.ufrn.ppgti.servio.model.enums.OrderStatus;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByProvider_IdAndDateBetween(Long providerId, LocalDate startDate, LocalDate endDate);

    List<Order> findByClient_IdOrderByCreatedAtDesc(Long clientId);

    List<Order> findByProvider_IdOrderByCreatedAtDesc(Long providerId);

    boolean existsByProvider_IdAndDateAndStatusNotAndStartTimeLessThanAndEndTimeGreaterThan(
            Long providerId,
            LocalDate date,
            OrderStatus status,
            LocalTime startTime,
            LocalTime endTime);

    java.util.Optional<Order> findByIdAndClient_Id(Long id, Long clientId);

    java.util.Optional<Order> findByIdAndProvider_Id(Long id, Long providerId);

    @Query("""
        select coalesce(sum(o.service.price), 0)
        from Order o
        where o.service.provider.id = :providerId
          and o.status = com.ufrn.ppgti.servio.model.enums.OrderStatus.COMPLETED
    """)
    BigDecimal sumCompletedEarningsByProviderId(Long providerId);

    @Query("""
        select count(o)
        from Order o
        where o.service.provider.id = :providerId
          and o.status = com.ufrn.ppgti.servio.model.enums.OrderStatus.COMPLETED
    """)
    Long countCompletedOrdersByProviderId(Long providerId);
}