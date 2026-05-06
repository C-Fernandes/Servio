package com.ufrn.ppgti.servio.repository;

import com.ufrn.ppgti.servio.model.Locality;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocalityRepository extends JpaRepository<Locality, Long> {
}