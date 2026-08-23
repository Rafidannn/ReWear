package com.example.application.service.product;

import com.example.application.model.product.*;
import com.example.application.model.user.User;
import com.example.application.repository.product.ProductRepository;
import com.example.application.repository.product.ProductVariantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;

    public ProductService(ProductRepository productRepository, ProductVariantRepository variantRepository) {
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
    }

    public List<Product> findActiveProducts() {
        return productRepository.findByStatusAndDeletedAtIsNull(ProductStatus.ACTIVE);
    }

    // Eager-load category agar tidak LazyInitializationException di View
    public List<Product> findActiveWithCategory() {
        return productRepository.findActiveWithCategory(ProductStatus.ACTIVE);
    }

    public List<Product> findSchoolMarketProducts() {
        return productRepository.findByIsSchoolMarketTrueAndStatusAndDeletedAtIsNull(ProductStatus.ACTIVE);
    }

    // Eager-load category untuk Pasar SMKN 24
    public List<Product> findSchoolMarketWithCategory() {
        return productRepository.findSchoolMarketWithCategory(ProductStatus.ACTIVE);
    }

    public List<Product> findProductsBySeller(User seller) {
        return productRepository.findBySellerAndDeletedAtIsNull(seller);
    }

    public Optional<Product> findById(Long id) {
        if (id == null) return Optional.empty();
        return productRepository.findById(id);
    }

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public List<ProductVariant> getVariants(Product product) {
        return variantRepository.findByProduct(product);
    }

    public ProductVariant saveVariant(ProductVariant variant) {
        return variantRepository.save(variant);
    }

    public List<Product> findAllProducts() {
        return productRepository.findAll();
    }

    public Product takedownProduct(Product product, String reason) {
        if (product == null) return null;
        product.setStatus(ProductStatus.REMOVED);
        return productRepository.save(product);
    }

    public Product activateProduct(Product product) {
        if (product == null) return null;
        product.setStatus(ProductStatus.ACTIVE);
        return productRepository.save(product);
    }

    public void deleteProduct(Product product) {
        if (product == null) return;
        product.setDeletedAt(java.time.LocalDateTime.now());
        product.setStatus(ProductStatus.REMOVED);
        productRepository.save(product);
    }
}
