package com.example.web_bookstore_be.dao;

import com.example.web_bookstore_be.entity.LibraryCard;
import com.example.web_bookstore_be.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;

@RepositoryRestResource(path = "library-cards")
public interface LibraryCardRepository extends JpaRepository<LibraryCard, Integer> {
    public boolean existsByCardNumber(String cardNumber);

    Optional<LibraryCard> findByUser(User user);
    Optional<LibraryCard> findByCardNumber(String cardNumber);
    Page<LibraryCard> findByActivated(boolean activated, Pageable pageable);
}