package com.ufrn.ppgti.servio.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ufrn.ppgti.servio.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

}
