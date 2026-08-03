package com.example.application.repository.user;

import com.example.application.model.user.User;
import com.example.application.model.user.UserSchoolVerification;
import com.example.application.model.user.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSchoolVerificationRepository extends JpaRepository<UserSchoolVerification, Long> {
    Optional<UserSchoolVerification> findByUser(User user);
    List<UserSchoolVerification> findByStatus(VerificationStatus status);
}
