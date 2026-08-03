package com.example.application.repository.order;

import com.example.application.model.order.Order;
import com.example.application.model.order.OrderStatusLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderStatusLogRepository extends JpaRepository<OrderStatusLog, Long> {
    List<OrderStatusLog> findByOrderOrderByCreatedAtAsc(Order order);
}
