package com.example.application.repository.user;

import com.example.application.model.user.Address;
import com.example.application.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUserAndDeletedAtIsNull(User user);
    Optional<Address> findByUserAndIsPrimaryTrueAndDeletedAtIsNull(User user);
}
