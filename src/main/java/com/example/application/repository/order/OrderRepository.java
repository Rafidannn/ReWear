package com.example.application.repository.order;

import com.example.application.model.order.Order;
import com.example.application.model.order.OrderStatus;
import com.example.application.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findByBuyerOrderByCreatedAtDesc(User buyer);
    List<Order> findBySellerOrderByCreatedAtDesc(User seller);

    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.buyer LEFT JOIN FETCH o.seller WHERE o.seller = :seller ORDER BY o.createdAt DESC")
    List<Order> findSellerOrdersWithDetails(@Param("seller") User seller);

    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.buyer LEFT JOIN FETCH o.seller WHERE o.buyer = :buyer ORDER BY o.createdAt DESC")
    List<Order> findBuyerOrdersWithDetails(@Param("buyer") User buyer);

    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.buyer LEFT JOIN FETCH o.seller ORDER BY o.createdAt DESC")
    List<Order> findAllWithDetails();

    List<Order> findByStatus(OrderStatus status);
    List<Order> findAllByOrderByCreatedAtDesc();
}
