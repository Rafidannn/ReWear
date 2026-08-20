package com.example.application.repository.user;

import com.example.application.model.product.Product;
import com.example.application.model.user.User;
import com.example.application.model.user.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    List<Wishlist> findByUserOrderByCreatedAtDesc(User user);

    @Query("SELECT DISTINCT w FROM Wishlist w LEFT JOIN FETCH w.product p LEFT JOIN FETCH p.seller WHERE w.user = :user ORDER BY w.createdAt DESC")
    List<Wishlist> findWishlistsWithDetails(@Param("user") User user);

    Optional<Wishlist> findByUserAndProduct(User user, Product product);

    boolean existsByUserAndProduct(User user, Product product);

    void deleteByUserAndProduct(User user, Product product);
}
