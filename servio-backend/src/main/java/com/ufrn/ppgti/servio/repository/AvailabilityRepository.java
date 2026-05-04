package com.ufrn.ppgti.servio.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ufrn.ppgti.servio.model.Availability;

@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, Long> {

    List<Availability> findByProviderId(Long providerId);

    void deleteByProviderIdAndOrderIsNull(Long providerId);
}