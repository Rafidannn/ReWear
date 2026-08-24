package com.example.application.repository.user;

import com.example.application.model.user.User;
import com.example.application.model.user.UserSchoolVerification;
import com.example.application.model.user.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface UserSchoolVerificationRepository extends JpaRepository<UserSchoolVerification, Long> {
    
    @Query("SELECT v FROM UserSchoolVerification v LEFT JOIN FETCH v.user LEFT JOIN FETCH v.school WHERE v.user = :user")
    Optional<UserSchoolVerification> findByUser(@Param("user") User user);

    @Query("SELECT v FROM UserSchoolVerification v LEFT JOIN FETCH v.user LEFT JOIN FETCH v.school WHERE v.user.id = :userId")
    Optional<UserSchoolVerification> findByUserId(@Param("userId") Long userId);

    @Query("SELECT v FROM UserSchoolVerification v LEFT JOIN FETCH v.user LEFT JOIN FETCH v.school WHERE v.status = :status ORDER BY v.createdAt DESC")
    List<UserSchoolVerification> findByStatus(@Param("status") VerificationStatus status);

    @Query("SELECT v FROM UserSchoolVerification v LEFT JOIN FETCH v.user LEFT JOIN FETCH v.school ORDER BY v.createdAt DESC")
    List<UserSchoolVerification> findAllWithDetails();
}
