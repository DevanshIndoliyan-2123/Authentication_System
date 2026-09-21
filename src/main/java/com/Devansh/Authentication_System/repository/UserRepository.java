package com.Devansh.Authentication_System.repository;

import com.Devansh.Authentication_System.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;


public interface  UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
    Boolean existsByEmail(String email);
}
