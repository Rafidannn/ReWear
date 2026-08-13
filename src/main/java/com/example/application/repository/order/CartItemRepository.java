package com.example.application.repository.order;

import com.example.application.model.order.CartItemEntity;
import com.example.application.model.product.Product;
import com.example.application.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItemEntity, Long> {

    List<CartItemEntity> findByUserOrderByCreatedAtDesc(User user);

    Optional<CartItemEntity> findByUserAndProduct(User user, Product product);

    int countByUser(User user);

    void deleteByUser(User user);
}
