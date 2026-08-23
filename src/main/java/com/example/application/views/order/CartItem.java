package com.example.application.views.order;

import java.io.Serializable;

public class CartItem implements Serializable {
    private String id;
    private Long productId;
    private String storeName;
    private String storeBadge;
    private String storeBadgeClass; // gold / blue
    private String title;
    private String variant;
    private double price;
    private double originalPrice;
    private String imgUrl;
    private String itemBadge; // e.g. "Pre-Loved"
    private int quantity;
    private boolean selected;
    private boolean isSmkn24Item;
    private int maxStock = 99;

    public CartItem(String id, String storeName, String storeBadge, String storeBadgeClass,
                    String title, String variant, double price, double originalPrice,
                    String imgUrl, String itemBadge, int quantity, boolean selected, boolean isSmkn24Item) {
        this(id, storeName, storeBadge, storeBadgeClass, title, variant, price, originalPrice, imgUrl, itemBadge, quantity, selected, isSmkn24Item, 99);
    }

    public CartItem(String id, String storeName, String storeBadge, String storeBadgeClass,
                    String title, String variant, double price, double originalPrice,
                    String imgUrl, String itemBadge, int quantity, boolean selected, boolean isSmkn24Item, int maxStock) {
        this.id = id;
        this.storeName = storeName;
        this.storeBadge = storeBadge;
        this.storeBadgeClass = storeBadgeClass;
        this.title = title;
        this.variant = variant;
        this.price = price;
        this.originalPrice = originalPrice;
        this.imgUrl = imgUrl;
        this.itemBadge = itemBadge;
        this.quantity = quantity;
        this.selected = selected;
        this.isSmkn24Item = isSmkn24Item;
        this.maxStock = Math.max(1, maxStock);
    }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }

    public String getStoreBadge() { return storeBadge; }
    public void setStoreBadge(String storeBadge) { this.storeBadge = storeBadge; }

    public String getStoreBadgeClass() { return storeBadgeClass; }
    public void setStoreBadgeClass(String storeBadgeClass) { this.storeBadgeClass = storeBadgeClass; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getVariant() { return variant; }
    public void setVariant(String variant) { this.variant = variant; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(double originalPrice) { this.originalPrice = originalPrice; }

    public String getImgUrl() { return imgUrl; }
    public void setImgUrl(String imgUrl) { this.imgUrl = imgUrl; }

    public String getItemBadge() { return itemBadge; }
    public void setItemBadge(String itemBadge) { this.itemBadge = itemBadge; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }

    public boolean isSmkn24Item() { return isSmkn24Item; }
    public void setSmkn24Item(boolean smkn24Item) { isSmkn24Item = smkn24Item; }

    public int getMaxStock() { return maxStock; }
    public void setMaxStock(int maxStock) { this.maxStock = maxStock; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
}
