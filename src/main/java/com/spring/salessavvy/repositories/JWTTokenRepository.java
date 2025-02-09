package com.spring.salessavvy.repositories;

import com.spring.salessavvy.entities.JWTToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface JWTTokenRepository extends JpaRepository<JWTToken, Integer> {

    @Query("SELECT t from JWTToken t WHERE t.user.userId = :userId")
    JWTToken findByUserId(int userId);


    @Modifying
    @Transactional
    @Query("DELETE FROM JWTToken t WHERE t.user.userId = :userId")
    void deleteByUserId(int userId);

    Optional<JWTToken> findByToken(String token);
}
