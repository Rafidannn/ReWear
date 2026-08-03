package com.example.application.service.moderation;

import com.example.application.model.moderation.*;
import com.example.application.model.product.Product;
import com.example.application.model.user.User;
import com.example.application.repository.moderation.ReportRepository;
import com.example.application.repository.moderation.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ModerationService {

    private final ReviewRepository reviewRepository;
    private final ReportRepository reportRepository;

    public ModerationService(ReviewRepository reviewRepository, ReportRepository reportRepository) {
        this.reviewRepository = reviewRepository;
        this.reportRepository = reportRepository;
    }

    public List<Review> getProductReviews(Product product) {
        return reviewRepository.findByProduct(product);
    }

    public Review saveReview(Review review) {
        return reviewRepository.save(review);
    }

    public List<Report> getPendingReports() {
        return reportRepository.findByStatus(ReportStatus.PENDING);
    }

    public Report submitReport(Report report) {
        report.setStatus(ReportStatus.PENDING);
        return reportRepository.save(report);
    }
}
