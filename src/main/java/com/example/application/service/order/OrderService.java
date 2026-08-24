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
    private final com.example.application.repository.product.ProductRepository productRepository;
    private final com.example.application.repository.user.UserRepository userRepository;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        OrderStatusLogRepository statusLogRepository,
                        OrderReturnRepository returnRepository,
                        com.example.application.repository.product.ProductRepository productRepository,
                        com.example.application.repository.user.UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.statusLogRepository = statusLogRepository;
        this.returnRepository = returnRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public List<Order> getBuyerOrders(User buyer) {
        if (buyer == null) return List.of();
        return orderRepository.findBuyerOrdersWithDetails(buyer);
    }

    public List<Order> getSellerOrders(User seller) {
        if (seller == null) return List.of();
        return orderRepository.findSellerOrdersWithDetails(seller);
    }

    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAllWithDetails();
    }

    @Transactional
    public Order createOrder(Order order, List<OrderItem> items, User actor) {
        // Validate stock for all items before saving anything
        for (OrderItem item : items) {
            if (item.getProduct() != null) {
                com.example.application.model.product.Product product = item.getProduct();
                int requestedQty = item.getQuantity() != null ? item.getQuantity() : 1;
                int currentStock = product.getStock() != null ? product.getStock() : 0;
                if (currentStock < requestedQty) {
                    throw new IllegalStateException("Stok produk '" + product.getName() + "' tidak mencukupi! Sisa stok: " + currentStock + ", diminta: " + requestedQty);
                }
            }
        }

        Order savedOrder = orderRepository.save(order);
        for (OrderItem item : items) {
            item.setOrder(savedOrder);
            orderItemRepository.save(item);

            // Deduct stock and increment sold count in database
            if (item.getProduct() != null) {
                com.example.application.model.product.Product product = item.getProduct();
                int requestedQty = item.getQuantity() != null ? item.getQuantity() : 1;
                int currentStock = product.getStock() != null ? product.getStock() : 0;
                int newStock = currentStock - requestedQty;
                int currentSold = product.getSoldCount() != null ? product.getSoldCount() : 0;

                product.setStock(newStock);
                product.setSoldCount(currentSold + requestedQty);

                if (newStock == 0) {
                    product.setStatus(com.example.application.model.product.ProductStatus.SOLD_OUT);
                }
                productRepository.save(product);
            }
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

    @Transactional
    public Order updateOrderStatus(Order order, OrderStatus newStatus, String notes, User actor) {
        OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);
        Order updated = orderRepository.save(order);

        OrderStatusLog log = new OrderStatusLog();
        log.setOrder(updated);
        log.setStatus(newStatus);
        log.setNotes(notes);
        log.setActor(actor);
        statusLogRepository.save(log);

        // Jika status berubah menjadi DIBATALKAN dari status sebelumnya, kembalikan stok produk (restock)
        if (newStatus == OrderStatus.DIBATALKAN && oldStatus != OrderStatus.DIBATALKAN) {
            List<OrderItem> items = orderItemRepository.findByOrder(updated);
            for (OrderItem item : items) {
                if (item.getProduct() != null) {
                    com.example.application.model.product.Product product = item.getProduct();
                    int qty = item.getQuantity() != null ? item.getQuantity() : 1;
                    int currentStock = product.getStock() != null ? product.getStock() : 0;
                    int currentSold = product.getSoldCount() != null ? product.getSoldCount() : 0;

                    product.setStock(currentStock + qty);
                    product.setSoldCount(Math.max(0, currentSold - qty));

                    if (product.getStatus() == com.example.application.model.product.ProductStatus.SOLD_OUT) {
                        product.setStatus(com.example.application.model.product.ProductStatus.ACTIVE);
                    }
                    productRepository.save(product);
                }
            }
        }

        // Jika status menjadi SELESAI, dana Escrow dicairkan ke saldo penjual
        if (newStatus == OrderStatus.SELESAI && updated.getSeller() != null && oldStatus != OrderStatus.SELESAI) {
            User seller = updated.getSeller();
            java.math.BigDecimal amount = updated.getTotalAmount() != null ? updated.getTotalAmount() : java.math.BigDecimal.ZERO;
            seller.setBalance(seller.getBalance().add(amount));
            userRepository.save(seller);
            System.out.println("Dana Escrow sebesar Rp " + amount + " berhasil dicairkan ke saldo penjual: " + seller.getFullName());
        }

        return updated;
    }

    @Transactional
    public Order shipOrder(Order order, CourierName courierName, String trackingNumber, String notes, User actor) {
        if (courierName != null) {
            order.setCourierName(courierName);
        }
        if (trackingNumber != null && !trackingNumber.isBlank()) {
            order.setTrackingNumber(trackingNumber.trim());
        }
        order.setStatus(OrderStatus.DIKIRIM);
        Order updated = orderRepository.save(order);

        OrderStatusLog log = new OrderStatusLog();
        log.setOrder(updated);
        log.setStatus(OrderStatus.DIKIRIM);
        log.setNotes(notes != null && !notes.isBlank() ? notes : "Pesanan telah dikirim oleh penjual.");
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

    public Optional<OrderReturn> getReturnByOrder(Order order) {
        if (order == null) return Optional.empty();
        return returnRepository.findByOrder(order);
    }


    public List<OrderReturn> getAllReturns() {
        return returnRepository.findAllWithDetails();
    }

    public List<OrderReturn> getPendingReturns() {
        return returnRepository.findAllWithDetails().stream()
            .filter(r -> r.getStatus() == ReturnStatus.PENDING)
            .toList();
    }

    public List<OrderReturn> getReturnsByBuyer(User buyer) {
        if (buyer == null) return List.of();
        return returnRepository.findByBuyerOrderByCreatedAtDesc(buyer);
    }

    @Transactional
    public OrderReturn createOrderReturn(Order order, User buyer, String reason, String evidenceUrl, java.math.BigDecimal refundAmount) {
        if (order == null) throw new IllegalArgumentException("Order tidak ditemukan");
        if (buyer == null) throw new IllegalArgumentException("User pembeli tidak valid");

        // Cek jika sudah pernah diajukan
        Optional<OrderReturn> existing = returnRepository.findByOrder(order);
        if (existing.isPresent()) {
            OrderReturn r = existing.get();
            r.setReason(reason);
            r.setEvidenceUrl(evidenceUrl);
            r.setRefundAmount(refundAmount != null ? refundAmount : order.getTotalAmount());
            r.setStatus(ReturnStatus.PENDING);
            return returnRepository.save(r);
        }

        OrderReturn orderReturn = new OrderReturn();
        orderReturn.setOrder(order);
        orderReturn.setBuyer(buyer);
        orderReturn.setReason(reason != null ? reason.trim() : "Komplain pesanan");
        orderReturn.setEvidenceUrl(evidenceUrl != null ? evidenceUrl.trim() : null);
        orderReturn.setRefundAmount(refundAmount != null ? refundAmount : order.getTotalAmount());
        orderReturn.setStatus(ReturnStatus.PENDING);

        OrderReturn savedReturn = returnRepository.save(orderReturn);

        // Update status pesanan menjadi KOMPLAIN sehingga dana Escrow ditahan
        order.setStatus(OrderStatus.KOMPLAIN);
        orderRepository.save(order);

        // Catat di log status pesanan
        OrderStatusLog log = new OrderStatusLog();
        log.setOrder(order);
        log.setStatus(OrderStatus.KOMPLAIN);
        log.setNotes("Pembeli mengajukan komplain/retur: " + reason);
        log.setActor(buyer);
        statusLogRepository.save(log);

        return savedReturn;
    }

    @Transactional
    public OrderReturn approveOrderReturn(OrderReturn ret, String adminNotes, User admin) {
        if (ret == null) throw new IllegalArgumentException("Data komplain tidak ditemukan");

        ret.setStatus(ReturnStatus.APPROVED);
        OrderReturn updatedReturn = returnRepository.save(ret);

        Order order = ret.getOrder();
        if (order != null) {
            // 1. Ubah status pesanan menjadi DIBATALKAN (Refunded)
            order.setStatus(OrderStatus.DIBATALKAN);
            orderRepository.save(order);

            // 2. Kembalikan dana ke saldo ReWear Pay pembeli
            User buyer = ret.getBuyer();
            if (buyer != null) {
                java.math.BigDecimal refund = ret.getRefundAmount() != null ? ret.getRefundAmount() : order.getTotalAmount();
                buyer.setBalance(buyer.getBalance().add(refund));
                userRepository.save(buyer);
            }

            // 3. Restock barang kembali ke inventaris produk
            List<OrderItem> items = orderItemRepository.findByOrder(order);
            for (OrderItem item : items) {
                if (item.getProduct() != null) {
                    com.example.application.model.product.Product product = item.getProduct();
                    int qty = item.getQuantity() != null ? item.getQuantity() : 1;
                    int currentStock = product.getStock() != null ? product.getStock() : 0;
                    int currentSold = product.getSoldCount() != null ? product.getSoldCount() : 0;

                    product.setStock(currentStock + qty);
                    product.setSoldCount(Math.max(0, currentSold - qty));
                    if (product.getStatus() == com.example.application.model.product.ProductStatus.SOLD_OUT) {
                        product.setStatus(com.example.application.model.product.ProductStatus.ACTIVE);
                    }
                    productRepository.save(product);
                }
            }

            // 4. Catat riwayat log
            OrderStatusLog log = new OrderStatusLog();
            log.setOrder(order);
            log.setStatus(OrderStatus.DIBATALKAN);
            log.setNotes("Komplain disetujui Admin. Dana sebesar Rp " + String.format("%,.0f", ret.getRefundAmount()) + " dikembalikan ke pembeli. Catatan: " + (adminNotes != null ? adminNotes : "-"));
            log.setActor(admin);
            statusLogRepository.save(log);
        }

        return updatedReturn;
    }

    @Transactional
    public OrderReturn rejectOrderReturn(OrderReturn ret, String adminNotes, User admin) {
        if (ret == null) throw new IllegalArgumentException("Data komplain tidak ditemukan");

        ret.setStatus(ReturnStatus.REJECTED);
        OrderReturn updatedReturn = returnRepository.save(ret);

        Order order = ret.getOrder();
        if (order != null) {
            // 1. Ubah status pesanan menjadi SELESAI
            order.setStatus(OrderStatus.SELESAI);
            orderRepository.save(order);

            // 2. Cairkan dana Escrow ke saldo penjual
            User seller = order.getSeller();
            if (seller != null) {
                java.math.BigDecimal amount = order.getTotalAmount() != null ? order.getTotalAmount() : java.math.BigDecimal.ZERO;
                seller.setBalance(seller.getBalance().add(amount));
                userRepository.save(seller);
            }

            // 3. Catat riwayat log
            OrderStatusLog log = new OrderStatusLog();
            log.setOrder(order);
            log.setStatus(OrderStatus.SELESAI);
            log.setNotes("Komplain ditolak Admin. Alasan penolakan: " + (adminNotes != null ? adminNotes : "-") + ". Dana Escrow dicairkan ke penjual.");
            log.setActor(admin);
            statusLogRepository.save(log);
        }

        return updatedReturn;
    }
}
