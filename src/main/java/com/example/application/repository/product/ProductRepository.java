package com.example.application.repository.product;

import com.example.application.model.product.Category;
import com.example.application.model.product.Product;
import com.example.application.model.product.ProductStatus;
import com.example.application.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findBySellerAndDeletedAtIsNull(User seller);
    List<Product> findByStatusAndDeletedAtIsNull(ProductStatus status);
    List<Product> findByCategoryAndStatusAndDeletedAtIsNull(Category category, ProductStatus status);
    List<Product> findByIsSchoolMarketTrueAndStatusAndDeletedAtIsNull(ProductStatus status);

    // Eager-load category & seller dalam satu query (hindari LazyInitializationException), urutkan produk terbaru paling atas
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.seller WHERE p.status = :status AND p.deletedAt IS NULL ORDER BY p.id DESC")
    List<Product> findActiveWithCategory(@Param("status") ProductStatus status);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.seller WHERE p.isSchoolMarket = true AND p.status = :status AND p.deletedAt IS NULL ORDER BY p.id DESC")
    List<Product> findSchoolMarketWithCategory(@Param("status") ProductStatus status);
}
