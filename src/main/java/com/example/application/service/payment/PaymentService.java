package com.example.application.service.payment;

import com.example.application.model.order.Order;
import com.example.application.model.order.OrderStatus;
import com.example.application.model.payment.Payment;
import com.example.application.model.payment.PayoutStatus;
import com.example.application.model.payment.SellerPayout;
import com.example.application.model.user.BankAccount;
import com.example.application.model.user.User;
import com.example.application.repository.order.OrderRepository;
import com.example.application.repository.payment.PaymentRepository;
import com.example.application.repository.payment.SellerPayoutRepository;
import com.example.application.repository.user.BankAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final SellerPayoutRepository sellerPayoutRepository;
    private final OrderRepository orderRepository;
    private final BankAccountRepository bankAccountRepository;
    private final com.example.application.repository.user.UserRepository userRepository;
    private final com.example.application.repository.order.OrderStatusLogRepository orderStatusLogRepository;

    public PaymentService(PaymentRepository paymentRepository,
                          SellerPayoutRepository sellerPayoutRepository,
                          OrderRepository orderRepository,
                          BankAccountRepository bankAccountRepository,
                          com.example.application.repository.user.UserRepository userRepository,
                          com.example.application.repository.order.OrderStatusLogRepository orderStatusLogRepository) {
        this.paymentRepository = paymentRepository;
        this.sellerPayoutRepository = sellerPayoutRepository;
        this.orderRepository = orderRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.userRepository = userRepository;
        this.orderStatusLogRepository = orderStatusLogRepository;
    }

    public Payment createOrUpdatePayment(Order order, String paymentMethod, String paymentGateway, String paymentProofUrl, BigDecimal grossAmount) {
        if (order == null) return null;
        Payment payment = paymentRepository.findFirstByOrderOrderByCreatedAtDesc(order).orElseGet(() -> {
            Payment p = new Payment();
            p.setOrder(order);
            return p;
        });

        payment.setPaymentMethod(paymentMethod != null ? paymentMethod : "TRANSFER");
        payment.setPaymentGateway(paymentGateway != null ? paymentGateway : "MANUAL");
        if (paymentProofUrl != null && !paymentProofUrl.isBlank()) {
            payment.setPaymentProofUrl(paymentProofUrl);
        }
        payment.setGrossAmount(grossAmount != null ? grossAmount : order.getTotalAmount());
        payment.setTransactionStatus(com.example.application.model.payment.TransactionStatus.PENDING);
        return paymentRepository.save(payment);
    }

    public List<Payment> getPendingVerificationPayments() {
        return paymentRepository.findAllWithOrderAndBuyer().stream()
            .filter(p -> p.getTransactionStatus() == com.example.application.model.payment.TransactionStatus.PENDING &&
                         p.getPaymentProofUrl() != null && !p.getPaymentProofUrl().isBlank())
            .toList();
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAllWithOrderAndBuyer();
    }

    public void approvePayment(Payment payment, String adminNotes, User admin) {
        if (payment == null) return;
        payment.setTransactionStatus(com.example.application.model.payment.TransactionStatus.SETTLEMENT);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        Order order = payment.getOrder();
        if (order != null) {
            order.setStatus(OrderStatus.DIPROSES);
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);

            com.example.application.model.order.OrderStatusLog log = new com.example.application.model.order.OrderStatusLog();
            log.setOrder(order);
            log.setStatus(OrderStatus.DIPROSES);
            log.setNotes("Pembayaran diverifikasi oleh Admin (" + (admin != null ? admin.getFullName() : "Admin") + "). " + (adminNotes != null && !adminNotes.isBlank() ? adminNotes : "Dana masuk ke Escrow."));
            log.setActor(admin);
            orderStatusLogRepository.save(log);
        }
    }

    public void rejectPayment(Payment payment, String reason, User admin) {
        if (payment == null) return;
        payment.setTransactionStatus(com.example.application.model.payment.TransactionStatus.FAILURE);
        paymentRepository.save(payment);

        Order order = payment.getOrder();
        if (order != null) {
            com.example.application.model.order.OrderStatusLog log = new com.example.application.model.order.OrderStatusLog();
            log.setOrder(order);
            log.setStatus(order.getStatus());
            log.setNotes("Bukti pembayaran ditolak Admin: " + (reason != null ? reason : "Bukti tidak valid / nominal tidak cocok."));
            log.setActor(admin);
            orderStatusLogRepository.save(log);
        }
    }

    public List<Payment> getPaymentsForOrder(Order order) {
        return paymentRepository.findByOrder(order);
    }

    public Payment savePayment(Payment payment) {
        return paymentRepository.save(payment);
    }

    public List<SellerPayout> getSellerPayouts(User seller) {
        if (seller == null) return List.of();
        return sellerPayoutRepository.findBySellerOrderByCreatedAtDesc(seller);
    }

    public List<SellerPayout> getAllPayouts() {
        return sellerPayoutRepository.findAllByOrderByCreatedAtDesc();
    }

    public BigDecimal getAvailableBalance(User seller) {
        if (seller == null) return BigDecimal.ZERO;

        List<Order> sellerOrders = orderRepository.findBySellerOrderByCreatedAtDesc(seller);
        double totalCompletedSales = sellerOrders.stream()
            .filter(o -> o.getStatus() == OrderStatus.SELESAI)
            .mapToDouble(o -> o.getTotalAmount() != null ? o.getTotalAmount().doubleValue() : 0.0)
            .sum();

        List<SellerPayout> payouts = sellerPayoutRepository.findBySeller(seller);
        double totalDrawnOrPending = payouts.stream()
            .filter(p -> p.getStatus() == PayoutStatus.COMPLETED || p.getStatus() == PayoutStatus.REQUESTED || p.getStatus() == PayoutStatus.PROCESSING)
            .mapToDouble(p -> p.getAmount() != null ? p.getAmount().doubleValue() : 0.0)
            .sum();

        double available = Math.max(0.0, totalCompletedSales - totalDrawnOrPending);
        return BigDecimal.valueOf(available);
    }

    public BigDecimal getEscrowBalance(User user) {
        if (user == null) return BigDecimal.ZERO;

        List<Order> buyerOrders = orderRepository.findByBuyerOrderByCreatedAtDesc(user);
        double escrowBuyer = buyerOrders.stream()
            .filter(o -> o.getStatus() == OrderStatus.MENUNGGU_PEMBAYARAN || o.getStatus() == OrderStatus.DIBAYAR || o.getStatus() == OrderStatus.DIPROSES || o.getStatus() == OrderStatus.DIKIRIM)
            .mapToDouble(o -> o.getTotalAmount() != null ? o.getTotalAmount().doubleValue() : 0.0)
            .sum();

        return BigDecimal.valueOf(escrowBuyer);
    }

    public SellerPayout requestPayout(User seller, String bankName, String accountNumber, String accountHolderName, BigDecimal amount) {
        if (seller == null) throw new IllegalArgumentException("User penjual tidak valid");
        if (amount == null || amount.compareTo(BigDecimal.valueOf(10000)) < 0) {
            throw new IllegalArgumentException("Minimal penarikan dana adalah Rp 10.000");
        }

        BigDecimal available = getAvailableBalance(seller);
        if (amount.compareTo(available) > 0) {
            throw new IllegalArgumentException("Saldo tidak mencukupi untuk penarikan sebesar Rp " + String.format("%,.0f", amount));
        }

        // Find or create bank account
        List<BankAccount> existingAccounts = bankAccountRepository.findByUser(seller);
        BankAccount account = existingAccounts.stream()
            .filter(a -> a.getBankName().equalsIgnoreCase(bankName) && a.getAccountNumber().equals(accountNumber))
            .findFirst()
            .orElseGet(() -> {
                BankAccount newAcc = new BankAccount();
                newAcc.setUser(seller);
                newAcc.setBankName(bankName);
                newAcc.setAccountNumber(accountNumber);
                newAcc.setAccountHolderName(accountHolderName);
                newAcc.setPrimary(existingAccounts.isEmpty());
                return bankAccountRepository.save(newAcc);
            });

        // Deduct seller user balance
        BigDecimal currentBalance = seller.getBalance();
        if (currentBalance != null && currentBalance.compareTo(amount) >= 0) {
            seller.setBalance(currentBalance.subtract(amount));
            userRepository.save(seller);
        }

        SellerPayout payout = new SellerPayout();
        payout.setSeller(seller);
        payout.setBankAccount(account);
        payout.setAmount(amount);
        payout.setStatus(PayoutStatus.REQUESTED);
        payout.setReferenceNumber("WD-RW-" + (System.currentTimeMillis() % 100000000L));
        return sellerPayoutRepository.save(payout);
    }

    public SellerPayout approvePayout(SellerPayout payout, String referenceNumber, String adminNotes) {
        if (payout == null) return null;
        payout.setStatus(PayoutStatus.COMPLETED);
        payout.setProcessedAt(LocalDateTime.now());
        if (referenceNumber != null && !referenceNumber.isBlank()) {
            payout.setReferenceNumber(referenceNumber.trim());
        }
        if (adminNotes != null && !adminNotes.isBlank()) {
            payout.setAdminNotes(adminNotes.trim());
        }
        return sellerPayoutRepository.save(payout);
    }

    public SellerPayout rejectPayout(SellerPayout payout, String rejectionNotes) {
        if (payout == null) return null;
        payout.setStatus(PayoutStatus.REJECTED);
        payout.setProcessedAt(LocalDateTime.now());
        payout.setAdminNotes(rejectionNotes != null ? rejectionNotes.trim() : "Ditolak oleh admin");
        return sellerPayoutRepository.save(payout);
    }
}

