package com.example.web_qltv_be.dao;

import com.example.web_qltv_be.entity.LibraryViolationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;

@RepositoryRestResource(path = "library-violation-types")
public interface LibraryViolationTypeRepository extends JpaRepository<LibraryViolationType, Integer> {
    Optional<LibraryViolationType> findByCode(String code);

}