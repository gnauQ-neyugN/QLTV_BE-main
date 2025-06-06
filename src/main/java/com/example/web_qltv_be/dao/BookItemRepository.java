package com.example.web_qltv_be.dao;

import com.example.web_qltv_be.entity.BookItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "book-items")
public interface BookItemRepository extends JpaRepository<BookItem, Integer> {
}