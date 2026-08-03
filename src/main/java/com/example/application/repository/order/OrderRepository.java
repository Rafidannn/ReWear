package com.example.application.repository.order;

import com.example.application.model.order.Order;
import com.example.application.model.order.OrderStatus;
import com.example.application.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findByBuyerOrderByCreatedAtDesc(User buyer);
    List<Order> findBySellerOrderByCreatedAtDesc(User seller);
    List<Order> findByStatus(OrderStatus status);
}
