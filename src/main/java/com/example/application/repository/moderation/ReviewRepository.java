package com.example.application.repository.moderation;

import com.example.application.model.moderation.Review;
import com.example.application.model.product.Product;
import com.example.application.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProduct(Product product);
    List<Review> findBySeller(User seller);
    List<Review> findByBuyer(User buyer);
}
