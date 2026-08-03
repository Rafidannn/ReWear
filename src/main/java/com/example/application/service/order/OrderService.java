package com.example.application.service.order;

import com.example.application.model.order.*;
import com.example.application.model.user.User;
import com.example.application.repository.order.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusLogRepository statusLogRepository;
    private final OrderReturnRepository returnRepository;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        OrderStatusLogRepository statusLogRepository,
                        OrderReturnRepository returnRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.statusLogRepository = statusLogRepository;
        this.returnRepository = returnRepository;
    }

    public List<Order> getBuyerOrders(User buyer) {
        return orderRepository.findByBuyerOrderByCreatedAtDesc(buyer);
    }

    public List<Order> getSellerOrders(User seller) {
        return orderRepository.findBySellerOrderByCreatedAtDesc(seller);
    }

    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    public Order createOrder(Order order, List<OrderItem> items, User actor) {
        Order savedOrder = orderRepository.save(order);
        for (OrderItem item : items) {
            item.setOrder(savedOrder);
            orderItemRepository.save(item);
        }

        // Add initial status log
        OrderStatusLog log = new OrderStatusLog();
        log.setOrder(savedOrder);
        log.setStatus(savedOrder.getStatus());
        log.setNotes("Order created");
        log.setActor(actor);
        statusLogRepository.save(log);

        return savedOrder;
    }

    public Order updateOrderStatus(Order order, OrderStatus newStatus, String notes, User actor) {
        order.setStatus(newStatus);
        Order updated = orderRepository.save(order);

        OrderStatusLog log = new OrderStatusLog();
        log.setOrder(updated);
        log.setStatus(newStatus);
        log.setNotes(notes);
        log.setActor(actor);
        statusLogRepository.save(log);

        return updated;
    }

    public List<OrderItem> getOrderItems(Order order) {
        return orderItemRepository.findByOrder(order);
    }

    public List<OrderStatusLog> getStatusLogs(Order order) {
        return statusLogRepository.findByOrderOrderByCreatedAtAsc(order);
    }

    public OrderReturn requestReturn(OrderReturn orderReturn) {
        return returnRepository.save(orderReturn);
    }
}
