package com.example.web_qltv_be.dao;

import com.example.web_qltv_be.entity.DdcCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "ddc-categories")
public interface DdcCategoryRepository extends JpaRepository<DdcCategory, Integer> {
}