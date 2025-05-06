package com.example.web_bookstore_be.dao;

import com.example.web_bookstore_be.entity.LibraryCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

@RepositoryRestResource(path = "library-cards")
public interface LibraryCardRepository extends JpaRepository<LibraryCard, Integer> {
    public boolean existsByCardNumber(String cardNumber);
}