package com.example.application.repository.user;

import com.example.application.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByGoogleId(String googleId);
    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.school WHERE u.id = :id")
    Optional<User> findByIdWithSchool(@Param("id") Long id);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.school WHERE u.email = :email")
    Optional<User> findByEmailWithSchool(@Param("email") String email);
}
