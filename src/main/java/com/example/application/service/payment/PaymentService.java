package com.example.application.service.payment;

import com.example.application.model.order.Order;
import com.example.application.model.payment.*;
import com.example.application.model.user.User;
import com.example.application.repository.payment.PaymentRepository;
import com.example.application.repository.payment.SellerPayoutRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final SellerPayoutRepository sellerPayoutRepository;

    public PaymentService(PaymentRepository paymentRepository, SellerPayoutRepository sellerPayoutRepository) {
        this.paymentRepository = paymentRepository;
        this.sellerPayoutRepository = sellerPayoutRepository;
    }

    public List<Payment> getPaymentsForOrder(Order order) {
        return paymentRepository.findByOrder(order);
    }

    public Payment savePayment(Payment payment) {
        return paymentRepository.save(payment);
    }

    public List<SellerPayout> getSellerPayouts(User seller) {
        return sellerPayoutRepository.findBySeller(seller);
    }

    public SellerPayout requestPayout(SellerPayout payout) {
        payout.setStatus(PayoutStatus.REQUESTED);
        return sellerPayoutRepository.save(payout);
    }
}
