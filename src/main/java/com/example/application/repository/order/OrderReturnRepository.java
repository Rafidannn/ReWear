package com.example.application.repository.order;

import com.example.application.model.order.Order;
import com.example.application.model.order.OrderReturn;
import com.example.application.model.order.ReturnStatus;
import com.example.application.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderReturnRepository extends JpaRepository<OrderReturn, Long> {
    Optional<OrderReturn> findByOrder(Order order);
    List<OrderReturn> findByStatus(ReturnStatus status);
    List<OrderReturn> findByBuyerOrderByCreatedAtDesc(User buyer);
    List<OrderReturn> findAllByOrderByCreatedAtDesc();
    List<OrderReturn> findByStatusOrderByCreatedAtDesc(ReturnStatus status);

    @Query("SELECT r FROM OrderReturn r LEFT JOIN FETCH r.order o LEFT JOIN FETCH r.buyer b LEFT JOIN FETCH o.seller s ORDER BY r.createdAt DESC")
    List<OrderReturn> findAllWithDetails();
}
