package com.example.myproject.repository;
import com.example.myproject.entity.Board;
import com.example.myproject.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findById(String id);
    boolean existsByEmail(String email);
}