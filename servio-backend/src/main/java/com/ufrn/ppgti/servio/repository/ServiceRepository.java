package com.ufrn.ppgti.servio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ufrn.ppgti.servio.model.Service;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
}