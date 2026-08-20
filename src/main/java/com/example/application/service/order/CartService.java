package com.example.application.service.order;

import com.example.application.model.order.CartItemEntity;
import com.example.application.model.product.Product;
import com.example.application.model.user.User;
import com.example.application.repository.order.CartItemRepository;
import com.example.application.views.order.CartItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class CartService {

    private final CartItemRepository cartItemRepository;

    public CartService(CartItemRepository cartItemRepository) {
        this.cartItemRepository = cartItemRepository;
    }

    public List<CartItemEntity> getCartItems(User user) {
        if (user == null) return new ArrayList<>();
        return cartItemRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional
    public CartItemEntity addToCart(User user, Product product, int quantity) {
        if (user == null || product == null) return null;

        // Block seller from purchasing own product
        if (product.getSeller() != null && user.getId() != null && user.getId().equals(product.getSeller().getId())) {
            throw new IllegalArgumentException("Anda tidak dapat membeli produk milik Anda sendiri!");
        }

        int availableStock = (product.getStock() != null) ? Math.max(0, product.getStock()) : 1;
        if (availableStock <= 0) {
            throw new IllegalArgumentException("Stok produk ini sudah habis!");
        }

        Optional<CartItemEntity> existing = cartItemRepository.findByUserAndProduct(user, product);
        if (existing.isPresent()) {
            CartItemEntity item = existing.get();
            int newQty = Math.min(availableStock, item.getQuantity() + Math.max(1, quantity));
            item.setQuantity(newQty);
            return cartItemRepository.save(item);
        } else {
            CartItemEntity newItem = new CartItemEntity();
            newItem.setUser(user);
            newItem.setProduct(product);
            newItem.setQuantity(Math.min(availableStock, Math.max(1, quantity)));
            return cartItemRepository.save(newItem);
        }
    }

    @Transactional
    public CartItemEntity updateQuantity(Long cartItemId, int quantity) {
        Optional<CartItemEntity> optionalItem = cartItemRepository.findById(cartItemId);
        if (optionalItem.isPresent()) {
            CartItemEntity item = optionalItem.get();
            if (quantity <= 0) {
                cartItemRepository.delete(item);
                return null;
            } else {
                int stock = (item.getProduct() != null && item.getProduct().getStock() != null)
                    ? Math.max(1, item.getProduct().getStock())
                    : 99;
                item.setQuantity(Math.min(stock, quantity));
                return cartItemRepository.save(item);
            }
        }
        return null;
    }

    @Transactional
    public void removeFromCart(Long cartItemId) {
        if (cartItemId != null) {
            cartItemRepository.deleteById(cartItemId);
        }
    }

    @Transactional
    public void clearCart(User user) {
        if (user != null) {
            cartItemRepository.deleteByUser(user);
        }
    }

    public int getCartCount(User user) {
        if (user == null) return 0;
        return cartItemRepository.countByUser(user);
    }

    /**
     * Konversi dari CartItemEntity (database) ke CartItem (UI model)
     */
    public CartItem convertToUiCartItem(CartItemEntity entity) {
        if (entity == null || entity.getProduct() == null) return null;

        Product p = entity.getProduct();
        String storeName = "Penjual ReWear";
        try {
            User seller = p.getSeller();
            if (seller != null && seller.getFullName() != null) {
                storeName = seller.getFullName();
            }
        } catch (Exception ignored) {}

        boolean isSmkn24 = false;
        try {
            isSmkn24 = p.isSchoolMarket();
        } catch (Exception ignored) {}

        String storeBadge = isSmkn24 ? "Pasar SMKN 24" : "Penjual Umum";
        String storeBadgeClass = isSmkn24 ? "gold" : "blue";

        String title = p.getName() != null ? p.getName() : "Produk";

        String variant = "Umum";
        try {
            if (p.getCategory() != null && p.getCategory().getName() != null) {
                variant = p.getCategory().getName();
            }
        } catch (Exception ignored) {}

        double price = p.getPrice() != null ? p.getPrice().doubleValue() : 0;
        double originalPrice = price * 1.15;

        // Ambil gambar pertama dari JSON array images
        String imgUrl = "images/placeholder.jpg";
        String imagesJson = p.getImages();
        if (imagesJson != null && !imagesJson.isBlank()) {
            // Format: ["path1","path2",...] atau path langsung
            String trimmed = imagesJson.trim();
            if (trimmed.startsWith("[")) {
                int start = trimmed.indexOf('"');
                int end = trimmed.indexOf('"', start + 1);
                if (start >= 0 && end > start) {
                    imgUrl = trimmed.substring(start + 1, end);
                }
            } else {
                imgUrl = trimmed;
            }
        }

        int maxStock = p.getStock() != null ? Math.max(1, p.getStock()) : 1;
        int quantity = entity.getQuantity() != null ? entity.getQuantity() : 1;
        String itemBadge = isSmkn24 ? "Eksklusif SMKN 24" : "Preloved";

        CartItem item = new CartItem(
            String.valueOf(entity.getId()),
            storeName,
            storeBadge,
            storeBadgeClass,
            title,
            variant,
            price,
            originalPrice,
            imgUrl,
            itemBadge,
            quantity,
            true, // Default selected
            isSmkn24,
            maxStock
        );

        return item;
    }

    /**
     * Konversi daftar CartItemEntity ke List<CartItem> untuk UI
     */
    public List<CartItem> convertToUiCartItemList(List<CartItemEntity> entities) {
        List<CartItem> list = new ArrayList<>();
        if (entities == null) return list;

        for (CartItemEntity e : entities) {
            CartItem uiItem = convertToUiCartItem(e);
            if (uiItem != null) {
                list.add(uiItem);
            }
        }
        return list;
    }
}
