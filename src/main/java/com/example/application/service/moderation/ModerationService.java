package com.example.application.service.moderation;

import com.example.application.model.chat.Conversation;
import com.example.application.model.moderation.*;
import com.example.application.model.order.Order;
import com.example.application.model.order.OrderItem;
import com.example.application.model.product.Product;
import com.example.application.model.user.User;
import com.example.application.repository.moderation.ReportRepository;
import com.example.application.repository.moderation.ReviewRepository;
import com.example.application.repository.order.OrderItemRepository;
import com.example.application.repository.product.ProductRepository;
import com.example.application.repository.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@Transactional
public class ModerationService {

    private final ReviewRepository reviewRepository;
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;

    public ModerationService(ReviewRepository reviewRepository,
                             ReportRepository reportRepository,
                             UserRepository userRepository,
                             ProductRepository productRepository,
                             OrderItemRepository orderItemRepository) {
        this.reviewRepository = reviewRepository;
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderItemRepository = orderItemRepository;
    }

    public List<Review> getProductReviews(Product product) {
        if (product == null || product.getId() == null) return List.of();
        try {
            List<Review> list = reviewRepository.findByProductId(product.getId());
            if (list == null) return List.of();
            for (Review r : list) {
                touchReview(r);
            }
            return list;
        } catch (Exception ex) {
            return List.of();
        }
    }

    private void touchReview(Review r) {
        if (r == null) return;
        try {
            if (r.getBuyer() != null) {
                r.getBuyer().getFullName();
                r.getBuyer().getAvatarUrl();
            }
            if (r.getSeller() != null) {
                r.getSeller().getFullName();
                r.getSeller().getAvatarUrl();
            }
            if (r.getProduct() != null) {
                r.getProduct().getName();
            }
        } catch (Exception ignored) {}
    }

    public Review saveReview(Review review) {
        return reviewRepository.save(review);
    }

    /**
     * Submit a review for a completed order item.
     * Robustly resolves product fallback if null, and recalculates seller's average rating.
     */
    public Review submitReview(Order order, Product product, User buyer, User seller,
                               int rating, String comment) {
        if (rating < 1 || rating > 5) throw new IllegalArgumentException("Rating harus antara 1–5");

        // Fallback: If product is null, try finding it from OrderItems or Seller
        if (product == null && order != null) {
            List<OrderItem> items = orderItemRepository.findByOrder(order);
            for (OrderItem it : items) {
                if (it.getProduct() != null) {
                    product = it.getProduct();
                    break;
                }
            }
            if (product == null && seller != null) {
                product = productRepository.findBySellerAndDeletedAtIsNull(seller).stream().findFirst().orElse(null);
            }
            if (product == null) {
                product = productRepository.findAll().stream().findFirst().orElse(null);
            }
        }

        Review review = new Review();
        review.setOrder(order);
        review.setProduct(product);
        review.setBuyer(buyer);
        review.setSeller(seller);
        review.setRating(rating);
        review.setComment(comment != null ? comment.trim() : "");
        Review saved = reviewRepository.save(review);

        // Recalculate seller average rating
        if (seller != null) {
            recalcSellerRating(seller);
        }

        return saved;
    }

    /**
     * Check if a buyer has already reviewed a specific order.
     */
    public boolean hasReviewed(Long orderId, Long buyerId) {
        if (orderId == null || buyerId == null) return false;
        return reviewRepository.existsByOrderIdAndBuyerId(orderId, buyerId);
    }

    /**
     * Recalculate and persist the seller's average rating from all their reviews.
     */
    public void recalcSellerRating(User seller) {
        if (seller == null) return;
        List<Review> allReviews = reviewRepository.findBySeller(seller);
        if (allReviews.isEmpty()) return;

        double avg = allReviews.stream()
            .mapToInt(Review::getRating)
            .average()
            .orElse(0.0);

        seller.setAverageRating(BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP));
        seller.setTotalReviews(allReviews.size());
        userRepository.save(seller);
    }

    public List<Report> getPendingReports() {
        return reportRepository.findByStatus(ReportStatus.PENDING);
    }

    public Report submitReport(Report report) {
        report.setStatus(ReportStatus.PENDING);
        return reportRepository.save(report);
    }

    /**
     * Submit a report against a product.
     */
    public Report reportProduct(User reporter, Product product, String reason, String description) {
        Report report = new Report();
        report.setReporter(reporter);
        report.setType(ReportType.PRODUCT_VIOLATION);
        if (product != null) {
            report.setReportedUser(product.getSeller());
        }
        report.setReason(reason);
        report.setDescription(description != null ? description.trim() : "");
        report.setStatus(ReportStatus.PENDING);
        return reportRepository.save(report);
    }

    /**
     * Submit a report against a user from chat or profile.
     */
    public Report reportUser(User reporter, User reportedUser, Conversation conversation, String reason, String description) {
        Report report = new Report();
        report.setReporter(reporter);
        report.setType(ReportType.USER_VIOLATION);
        report.setReportedUser(reportedUser);
        report.setConversation(conversation);
        report.setReason(reason);
        report.setDescription(description != null ? description.trim() : "");
        report.setStatus(ReportStatus.PENDING);
        return reportRepository.save(report);
    }

    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    public Report resolveReport(Report report, String notes) {
        if (report == null) return null;
        report.setStatus(ReportStatus.RESOLVED);
        return reportRepository.save(report);
    }

    public Report rejectReport(Report report, String notes) {
        if (report == null) return null;
        report.setStatus(ReportStatus.REJECTED);
        return reportRepository.save(report);
    }
}
