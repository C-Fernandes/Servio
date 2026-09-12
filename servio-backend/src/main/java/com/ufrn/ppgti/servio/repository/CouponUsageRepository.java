package com.ufrn.ppgti.servio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ufrn.ppgti.servio.model.CouponUsage;

@Repository
public interface CouponUsageRepository extends JpaRepository<CouponUsage, Long> {

    boolean existsByCoupon_IdAndClient_Id(Long couponId, Long clientId);
}
