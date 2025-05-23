package com.example.web_bookstore_be.dao;

import com.example.web_bookstore_be.entity.DdcCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "ddc_category")
public interface DdcCategoryRepository extends JpaRepository<DdcCategory, Integer> {
}