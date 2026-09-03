package com.example.application.controller;

import com.example.application.config.WebMvcConfig;
import com.example.application.model.order.Order;
import com.example.application.model.payment.Payment;
import com.example.application.model.user.Role;
import com.example.application.model.user.User;
import com.example.application.repository.payment.PaymentRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.Optional;

@RestController
public class PaymentProofController {

    private final PaymentRepository paymentRepository;

    public PaymentProofController(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @GetMapping("/api/payment-proofs/{filename}")
    public ResponseEntity<Resource> getPaymentProof(@PathVariable("filename") String filename, HttpSession session) {
        // P1.5: Proteksi path traversal
        if (filename == null || filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            return ResponseEntity.badRequest().build();
        }

        // P1.5: Cek autentikasi user dari session
        User currentUser = null;
        if (session != null) {
            currentUser = (User) session.getAttribute("CURRENT_USER");
        }

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Cari payment yang memuat bukti pembayaran ini
        Optional<Payment> paymentOpt = paymentRepository.findAllWithOrderAndBuyer().stream()
            .filter(p -> p.getPaymentProofUrl() != null && p.getPaymentProofUrl().contains(filename))
            .findFirst();

        // Otorisasi:
        // 1. ADMIN/MODERATOR selalu diizinkan
        boolean isAuthorized = currentUser.getRole() == Role.SUPER_ADMIN || currentUser.getRole() == Role.MODERATOR;

        if (!isAuthorized) {
            if (paymentOpt.isPresent()) {
                Payment p = paymentOpt.get();
                Order order = p.getOrder();
                if (order != null) {
                    if (order.getBuyer() != null && order.getBuyer().getId().equals(currentUser.getId())) {
                        isAuthorized = true;
                    } else if (order.getSeller() != null && order.getSeller().getId().equals(currentUser.getId())) {
                        isAuthorized = true;
                    }
                }
            } else {
                // Berkas baru diunggah pada sesi checkout (belum tersimpan di entitas Payment) oleh user yang sudah login
                isAuthorized = true;
            }
        }

        if (!isAuthorized) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Cari file di PROOFS_BASE_DIR terproteksi, fallback ke UPLOAD_BASE_DIR untuk data lama
        File file = new File(WebMvcConfig.PROOFS_BASE_DIR, filename);
        if (!file.exists()) {
            file = new File(WebMvcConfig.UPLOAD_BASE_DIR, filename);
        }

        if (!file.exists() || !file.isFile()) {
            return ResponseEntity.notFound().build();
        }

        MediaType mediaType = MediaType.IMAGE_JPEG;
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png")) {
            mediaType = MediaType.IMAGE_PNG;
        } else if (lower.endsWith(".webp")) {
            mediaType = MediaType.parseMediaType("image/webp");
        }

        return ResponseEntity.ok()
            .contentType(mediaType)
            .body(new FileSystemResource(file));
    }
}
