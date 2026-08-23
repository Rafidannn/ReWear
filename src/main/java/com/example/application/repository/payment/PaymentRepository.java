package com.example.application.repository.payment;

import com.example.application.model.order.Order;
import com.example.application.model.payment.Payment;
import com.example.application.model.payment.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByOrder(Order order);
    Optional<Payment> findFirstByOrderOrderByCreatedAtDesc(Order order);
    List<Payment> findAllByOrderByCreatedAtDesc();
    List<Payment> findByTransactionStatusOrderByCreatedAtDesc(TransactionStatus transactionStatus);
}
