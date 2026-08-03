package com.example.application.repository.order;

import com.example.application.model.order.Order;
import com.example.application.model.order.OrderReturn;
import com.example.application.model.order.ReturnStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderReturnRepository extends JpaRepository<OrderReturn, Long> {
    Optional<OrderReturn> findByOrder(Order order);
    List<OrderReturn> findByStatus(ReturnStatus status);
}
