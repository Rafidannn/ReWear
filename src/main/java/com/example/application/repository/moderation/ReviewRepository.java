package com.example.application.repository.moderation;

import com.example.application.model.moderation.Review;
import com.example.application.model.product.Product;
import com.example.application.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT r FROM Review r LEFT JOIN FETCH r.buyer LEFT JOIN FETCH r.seller LEFT JOIN FETCH r.product WHERE r.product.id = :productId ORDER BY r.id DESC")
    List<Review> findByProductId(@Param("productId") Long productId);

    @Query("SELECT r FROM Review r LEFT JOIN FETCH r.buyer LEFT JOIN FETCH r.seller LEFT JOIN FETCH r.product WHERE r.product = :product ORDER BY r.id DESC")
    List<Review> findByProduct(@Param("product") Product product);

    @Query("SELECT r FROM Review r LEFT JOIN FETCH r.buyer LEFT JOIN FETCH r.seller LEFT JOIN FETCH r.product WHERE r.seller.id = :sellerId ORDER BY r.id DESC")
    List<Review> findBySellerId(@Param("sellerId") Long sellerId);

    @Query("SELECT r FROM Review r LEFT JOIN FETCH r.buyer LEFT JOIN FETCH r.seller LEFT JOIN FETCH r.product WHERE r.seller = :seller ORDER BY r.id DESC")
    List<Review> findBySeller(@Param("seller") User seller);

    List<Review> findByBuyer(User buyer);

    @Query("SELECT COUNT(r) > 0 FROM Review r WHERE r.order.id = :orderId AND r.buyer.id = :buyerId")
    boolean existsByOrderIdAndBuyerId(@Param("orderId") Long orderId, @Param("buyerId") Long buyerId);

    @Query("SELECT r FROM Review r LEFT JOIN FETCH r.buyer LEFT JOIN FETCH r.seller LEFT JOIN FETCH r.product WHERE r.order.id = :orderId")
    List<Review> findByOrderId(@Param("orderId") Long orderId);
}
