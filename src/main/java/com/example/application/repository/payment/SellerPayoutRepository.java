package com.example.application.repository.payment;

import com.example.application.model.payment.PayoutStatus;
import com.example.application.model.payment.SellerPayout;
import com.example.application.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SellerPayoutRepository extends JpaRepository<SellerPayout, Long> {
    List<SellerPayout> findBySeller(User seller);
    List<SellerPayout> findByStatus(PayoutStatus status);
}
