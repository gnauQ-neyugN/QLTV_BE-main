package com.example.web_qltv_be.dao;

import com.example.web_qltv_be.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;

@RepositoryRestResource(excerptProjection = User.class, path = "users")
public interface UserRepository extends JpaRepository<User, Integer> {
    public boolean existsByUsername(String username);
    public boolean existsByEmail(String email);
    public boolean existsByIdentifierCode(String identifierCode);
    public User findByIdentifierCode(String identifierCode);
    public User findByUsername(String username);
    public User findByEmail(String email);

    Optional<User> findUsersByLibraryCard_IdLibraryCard(int libraryCardIdLibraryCard);
}
