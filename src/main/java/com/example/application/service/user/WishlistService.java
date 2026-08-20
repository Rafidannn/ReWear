package com.example.application.service.user;

import com.example.application.model.product.Product;
import com.example.application.model.user.User;
import com.example.application.model.user.Wishlist;
import com.example.application.repository.user.WishlistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class WishlistService {

    private final WishlistRepository wishlistRepository;

    public WishlistService(WishlistRepository wishlistRepository) {
        this.wishlistRepository = wishlistRepository;
    }

    public boolean isWishlisted(User user, Product product) {
        if (user == null || product == null) return false;
        return wishlistRepository.existsByUserAndProduct(user, product);
    }

    @Transactional
    public boolean toggleWishlist(User user, Product product) {
        if (user == null || product == null) return false;

        if (wishlistRepository.existsByUserAndProduct(user, product)) {
            wishlistRepository.deleteByUserAndProduct(user, product);
            return false; // removed
        } else {
            Wishlist wishlist = new Wishlist(user, product);
            wishlistRepository.save(wishlist);
            return true; // added
        }
    }

    public List<Wishlist> getUserWishlist(User user) {
        if (user == null) return List.of();
        return wishlistRepository.findWishlistsWithDetails(user);
    }

    @Transactional
    public void removeFromWishlist(User user, Product product) {
        if (user == null || product == null) return;
        wishlistRepository.deleteByUserAndProduct(user, product);
    }
}
