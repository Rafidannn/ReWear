package com.example.application.views.product;

import com.example.application.model.moderation.Review;
import com.example.application.model.product.*;

import com.example.application.model.user.User;
import com.example.application.service.moderation.ModerationService;
import com.example.application.service.order.CartService;
import com.example.application.service.product.ProductService;
import com.example.application.service.user.WishlistService;
import com.example.application.util.AuthGuard;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.*;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Route(value = "product", layout = MainLayout.class)
@PageTitle("Detail Produk | ReWear SMKN 24")
public class ProductDetailView extends VerticalLayout implements HasUrlParameter<Long> {

    private final ProductService productService;
    private final CartService cartService;
    private final WishlistService wishlistService;
    private final ModerationService moderationService;
    private final Div contentArea = new Div();

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    public ProductDetailView(ProductService productService, CartService cartService,
                             WishlistService wishlistService, ModerationService moderationService) {
        this.productService = productService;
        this.cartService = cartService;
        this.wishlistService = wishlistService;
        this.moderationService = moderationService;

        setSpacing(false);
        setPadding(false);
        setWidthFull();
        getElement().getStyle()
            .set("padding", "0")
            .set("margin", "0")
            .set("width", "100%")
            .set("background-color", "#F8F9FF");

        contentArea.setWidthFull();
        add(contentArea);
    }

    @Override
    public void setParameter(BeforeEvent event, Long productId) {
        contentArea.removeAll();

        Long targetId = productId != null ? productId : 1L;
        Product product = null;

        try {
            Optional<Product> productOpt = productService.findById(targetId);
            if (productOpt.isPresent()) {
                product = productOpt.get();
            } else {
                List<Product> activeList = productService.findActiveProducts();
                if (activeList != null && !activeList.isEmpty()) {
                    product = activeList.get(0);
                } else {
                    product = createMockProduct(targetId);
                }
            }
        } catch (Exception ex) {
            product = createMockProduct(targetId);
        }

        contentArea.add(buildProductDetailUI(product));
    }

    private Product createMockProduct(Long id) {
        Product p = new Product();
        p.setId(id != null ? id : 1L);
        p.setName("Vintage Denim Jacket 90s");
        p.setPrice(new java.math.BigDecimal("125000"));
        p.setConditionType(ConditionType.BEKAS);


        p.setStock(5);
        p.setSoldCount(12);
        p.setImages("[\"images/buku.jpeg\"]");
        p.setDescription("Jaket denim koleksi pribadi, jarang dipakai. Kondisi masih sangat oke 9/10. Tidak ada robek atau noda. Sangat cocok buat hangout atau sekolah pas lagi santai. Gaya retro abis!\n\nDijual karena sudah kekecilan. Yuk diangkut sebelum keduluan yang lain!");

        User seller = new User();
        seller.setId(100L);
        seller.setFullName("Budi Setiawan");
        p.setSeller(seller);

        Category cat = new Category();
        cat.setName("Pakaian");
        cat.setSlug("pakaian");
        p.setCategory(cat);

        p.setSchoolMarket(true);
        p.setStatus(ProductStatus.ACTIVE);
        return p;
    }


    private Component buildProductDetailUI(Product product) {
        Div containerWrapper = new Div();
        containerWrapper.addClassName("product-detail-page-container");

        // ---- 0. MOBILE TOP HEADER (Figma Exact: Back Icon + "Detail") ----
        Div mobTopHeader = new Div();
        mobTopHeader.addClassName("pd-top-mobile-header");

        Icon mobNavBack = VaadinIcon.ARROW_LEFT.create();
        mobNavBack.getElement().getStyle()
            .set("cursor", "pointer")
            .set("color", "#001934")
            .set("font-size", "18px");
        mobNavBack.addClickListener(e -> UI.getCurrent().getPage().getHistory().back());

        H2 mobTitle = new H2("Detail");
        mobTitle.addClassName("pd-top-mobile-header-title");

        Icon mobShareBtn = VaadinIcon.SHARE.create();
        mobShareBtn.getElement().getStyle()
            .set("cursor", "pointer")
            .set("color", "#001934")
            .set("font-size", "18px");
        mobShareBtn.addClickListener(e -> {
            UI.getCurrent().getPage().executeJs(
                "if (navigator.share) {" +
                "  navigator.share({title: $0, url: window.location.href});" +
                "} else {" +
                "  navigator.clipboard.writeText(window.location.href);" +
                "  alert('Tautan produk berhasil disalin!');" +
                "}", product.getName()
            );
        });

        mobTopHeader.add(mobNavBack, mobTitle, mobShareBtn);


        // ---- 1. TOP NAV / BREADCRUMBS (Desktop Only) ----
        HorizontalLayout topNavRow = new HorizontalLayout();
        topNavRow.addClassName("pd-desktop-top-nav");
        topNavRow.setWidthFull();
        topNavRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        topNavRow.setAlignItems(FlexComponent.Alignment.CENTER);
        topNavRow.getElement().getStyle().set("margin-bottom", "20px");

        Button btnBack = new Button("Kembali", VaadinIcon.ARROW_LEFT.create());
        btnBack.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnBack.getElement().getStyle()
            .set("color", "#001934")
            .set("font-weight", "700")
            .set("font-size", "13px")
            .set("cursor", "pointer")
            .set("padding", "6px 14px")
            .set("background", "#FFFFFF")
            .set("border", "1px solid #E2E8F0")
            .set("border-radius", "8px");
        btnBack.addClickListener(e -> UI.getCurrent().getPage().getHistory().back());

        Div breadcrumb = new Div();
        breadcrumb.addClassName("pd-breadcrumb");
        breadcrumb.getElement().getStyle().set("font-size", "13px").set("color", "#64748B");

        Anchor b1 = new Anchor("", "Beranda");
        b1.getElement().getStyle().set("color", "#64748B").set("text-decoration", "none").set("font-weight", "600");
        Span s1 = new Span(" › ");
        Anchor b2 = new Anchor("pasar-smkn24", getCategoryName(product));
        b2.getElement().getStyle().set("color", "#64748B").set("text-decoration", "none").set("font-weight", "600");
        Span s2 = new Span(" › ");
        Span currentName = new Span(product.getName());
        currentName.getElement().getStyle().set("color", "#001934").set("font-weight", "700");

        breadcrumb.add(b1, s1, b2, s2, currentName);
        topNavRow.add(btnBack, breadcrumb);

        // ---- 2. MAIN GRID (Left Gallery | Right Details) ----
        HorizontalLayout mainGrid = new HorizontalLayout();
        mainGrid.addClassName("pd-main-grid");
        mainGrid.setWidthFull();
        mainGrid.setSpacing(true);
        mainGrid.setAlignItems(FlexComponent.Alignment.START);

        // --- LEFT GALLERY ---
        HorizontalLayout leftGallery = new HorizontalLayout();
        leftGallery.addClassName("pd-left-gallery");
        leftGallery.setWidth("48%");
        leftGallery.setSpacing(true);

        String imagesJson = product.getImages();
        String mainImgUrl = extractImgUrl(imagesJson, "images/buku.jpeg");

        // Main Image Display
        Div mainImgBox = new Div();
        mainImgBox.addClassName("pd-main-img-box");
        mainImgBox.getElement().getStyle()
            .set("position", "relative")
            .set("flex", "1")
            .set("height", "440px")
            .set("border-radius", "16px")
            .set("overflow", "hidden")
            .set("background", "#FFFFFF")
            .set("border", "1px solid #E2E8F0");

        Image heroImg = new Image(mainImgUrl, product.getName());
        heroImg.getElement().getStyle().set("width", "100%").set("height", "100%").set("object-fit", "cover").set("cursor", "zoom-in");
        heroImg.addClickListener(e -> openImageLightbox(heroImg.getSrc()));
        mainImgBox.add(heroImg);

        // Floating Overlay Back Button (Mobile Only)
        Div mobFloatingBack = new Div();
        mobFloatingBack.addClassName("pd-mobile-floating-back");
        Icon mobOverlayArrow = VaadinIcon.ARROW_LEFT.create();
        mobOverlayArrow.getElement().getStyle().set("width", "18px").set("height", "18px").set("color", "#001934");
        mobFloatingBack.add(mobOverlayArrow);
        mobFloatingBack.addClickListener(e -> UI.getCurrent().getPage().getHistory().back());
        mainImgBox.add(mobFloatingBack);

        // Floating Dots Indicator (Mobile Only)
        Div mobDots = new Div();
        mobDots.addClassName("pd-mobile-dots-indicator");
        Span d1 = new Span(); d1.addClassNames("pd-dot", "active");
        Span d2 = new Span(); d2.addClassName("pd-dot");
        Span d3 = new Span(); d3.addClassName("pd-dot");
        mobDots.add(d1, d2, d3);

        List<Span> dotList = List.of(d1, d2, d3);
        d1.addClickListener(e -> {
            dotList.forEach(d -> d.removeClassName("active"));
            d1.addClassName("active");
            heroImg.setSrc(mainImgUrl);
        });
        d2.addClickListener(e -> {
            dotList.forEach(d -> d.removeClassName("active"));
            d2.addClassName("active");
        });
        d3.addClickListener(e -> {
            dotList.forEach(d -> d.removeClassName("active"));
            d3.addClassName("active");
        });
        mainImgBox.add(mobDots);


        // Desktop Multiple Thumbnails
        VerticalLayout thumbsCol = new VerticalLayout();
        thumbsCol.addClassName("pd-thumbs-col");
        if (imagesJson != null && imagesJson.contains("\",\"")) {
            String[] imgList = imagesJson.replace("[\"", "").replace("\"]", "").split("\",\"");
            if (imgList.length > 1) {
                List<Div> thumbDivs = new ArrayList<>();
                for (int i = 0; i < imgList.length && i < 4; i++) {
                    String url = imgList[i].trim();
                    Div thumb = new Div();
                    thumb.getElement().getStyle()
                        .set("width", "72px")
                        .set("height", "72px")
                        .set("border-radius", "8px")
                        .set("overflow", "hidden")
                        .set("border", i == 0 ? "2px solid #001934" : "1.5px solid #E2E8F0")
                        .set("cursor", "pointer");
                    Image tImg = new Image(url, "Thumb");
                    tImg.getElement().getStyle().set("width", "100%").set("height", "100%").set("object-fit", "cover");
                    thumb.add(tImg);

                    final int currentIdx = i;
                    thumb.addClickListener(e -> {
                        heroImg.setSrc(url);
                        for (int k = 0; k < thumbDivs.size(); k++) {
                            thumbDivs.get(k).getElement().getStyle().set("border", k == currentIdx ? "2px solid #001934" : "1.5px solid #E2E8F0");
                        }
                    });

                    thumbDivs.add(thumb);
                    thumbsCol.add(thumb);
                }
                leftGallery.add(thumbsCol);
            }
        }

        leftGallery.add(mainImgBox);

        // --- RIGHT PRODUCT DETAILS & ACTIONS ---
        Div rightCol = new Div();
        rightCol.addClassName("pd-right-col");
        rightCol.setWidth("52%");
        rightCol.getElement().getStyle()
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("gap", "14px");

        // 1. Condition Badge & Wishlist Heart Row
        Div condWishRow = new Div();
        condWishRow.addClassName("pd-cond-wish-row");

        String rawCond = (product.getCondition() != null) ? product.getCondition().name().replace("_", " ") : "Barang Bekas";
        String condBadgeText = rawCond.equalsIgnoreCase("USED") || rawCond.equalsIgnoreCase("BEKAS") || rawCond.equalsIgnoreCase("LIKE NEW") ? "Barang Bekas" : rawCond;
        Span badgeCond = new Span(condBadgeText);
        badgeCond.addClassName("pd-badge-cond");

        User currentViewer = AuthGuard.getCurrentUser();
        boolean initialWishlistState = false;
        if (currentViewer != null && wishlistService != null && product != null) {
            try {
                initialWishlistState = wishlistService.isWishlisted(currentViewer, product);
            } catch (Exception ignored) {}
        }

        final boolean[] isWishlisted = {initialWishlistState};
        Button mobHeartBtn = new Button(initialWishlistState ? VaadinIcon.HEART.create() : VaadinIcon.HEART_O.create());
        mobHeartBtn.addClassName("pd-heart-btn");
        mobHeartBtn.addClickListener(e -> {
            if (!AuthGuard.requireLogin(UI.getCurrent())) return;
            User user = AuthGuard.getCurrentUser();
            boolean state = wishlistService.toggleWishlist(user, product);
            isWishlisted[0] = state;
            mobHeartBtn.setIcon(state ? VaadinIcon.HEART.create() : VaadinIcon.HEART_O.create());
            mobHeartBtn.getElement().getStyle().set("color", state ? "#DC2626" : "#EF4444");
            Notification.show(state ? "Ditambahkan ke Wishlist!" : "Dihapus dari Wishlist", 2000, Notification.Position.TOP_CENTER);
        });

        condWishRow.add(badgeCond, mobHeartBtn);

        // Title
        H1 productTitle = new H1(product.getName());
        productTitle.addClassName("pd-title");

        // Price
        BigDecimal price = (product.getPrice() != null) ? product.getPrice() : BigDecimal.ZERO;
        Span priceVal = new Span("Rp " + String.format("%,.0f", price).replace(",", "."));
        priceVal.addClassName("pd-price");

        // Rating & Sales Count
        List<Review> reviews = List.of();
        try {
            reviews = moderationService.getProductReviews(product);
        } catch (Exception ignored) {}
        if (reviews == null) reviews = List.of();

        int soldCount = product.getSoldCount() != null ? product.getSoldCount() : 0;
        double avgRating = 4.9;
        if (!reviews.isEmpty()) {
            avgRating = reviews.stream().mapToInt(r -> r.getRating() != null ? r.getRating() : 5).average().orElse(4.9);
        }

        // Seller Card
        Div sellerCard = new Div();
        sellerCard.addClassName("pd-seller-card");

        Div sellerLeft = new Div();
        sellerLeft.addClassName("pd-seller-left");
        sellerLeft.getElement().getStyle()
            .set("display", "flex")
            .set("align-items", "center")
            .set("gap", "12px");

        Component sellerAvatar;
        String avatarUrl = (product.getSeller() != null) ? product.getSeller().getAvatarUrl() : null;
        if (avatarUrl != null && !avatarUrl.isBlank() && !avatarUrl.contains("buku.jpeg")) {
            Image img = new Image(avatarUrl, "Seller");
            img.getElement().getStyle()
                .set("width", "44px").set("height", "44px").set("border-radius", "9999px").set("object-fit", "cover");
            sellerAvatar = img;
        } else {
            String initials = getInitials(getSellerName(product));
            Div badge = new Div(new Span(initials));
            badge.getElement().getStyle()
                .set("width", "44px").set("height", "44px").set("border-radius", "9999px")
                .set("background", "#001934").set("color", "#F5C45E")
                .set("font-weight", "800").set("display", "flex")
                .set("align-items", "center").set("justify-content", "center");
            sellerAvatar = badge;
        }

        Div sellerInfo = new Div();
        sellerInfo.addClassName("pd-seller-info");
        sellerInfo.getElement().getStyle()
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("align-items", "flex-start")
            .set("justify-content", "center")
            .set("gap", "2px");

        Div sNameRow = new Div();
        sNameRow.addClassName("pd-seller-name-row");
        sNameRow.getElement().getStyle()
            .set("display", "flex")
            .set("align-items", "center")
            .set("gap", "6px");

        Span sellerName = new Span(getSellerName(product));
        sellerName.addClassName("pd-seller-name");

        Span sellerRole = new Span("WARGA 24");
        sellerRole.addClassName("pd-seller-warga-badge");

        sNameRow.add(sellerName, sellerRole);

        int totalReviewCount = reviews.isEmpty() ? 42 : reviews.size();
        HorizontalLayout sellerSub = new HorizontalLayout();
        sellerSub.addClassName("pd-seller-sub");
        sellerSub.setAlignItems(FlexComponent.Alignment.CENTER);
        sellerSub.setSpacing(false);
        sellerSub.getElement().getStyle().set("margin-top", "2px");
        Icon yellowStar = VaadinIcon.STAR.create();
        yellowStar.getStyle().set("width", "13px").set("height", "13px").set("color", "#F59E0B").set("margin-right", "4px");
        Span subText = new Span(String.format("%.1f", avgRating) + " (" + totalReviewCount + " ulasan)");
        subText.getStyle().set("font-size", "12px").set("color", "#64748B").set("font-weight", "600");
        sellerSub.add(yellowStar, subText);

        sellerInfo.add(sNameRow, sellerSub);

        sellerLeft.add(sellerAvatar, sellerInfo);

        HorizontalLayout sellerBtns = new HorizontalLayout();
        sellerBtns.setAlignItems(FlexComponent.Alignment.CENTER);
        sellerBtns.setSpacing(true);

        Button btnChatSeller = new Button("Chat", VaadinIcon.COMMENT.create());
        btnChatSeller.getElement().getStyle()
            .set("background", "#001934")
            .set("color", "#F5C45E")
            .set("font-weight", "700")
            .set("font-size", "12px")
            .set("border-radius", "8px")
            .set("border", "none")
            .set("padding", "6px 12px")
            .set("cursor", "pointer");
        btnChatSeller.addClickListener(e -> {
            if (!AuthGuard.requireLogin(UI.getCurrent())) return;
            if (product.getSeller() != null) {
                UI.getCurrent().navigate("chat", new QueryParameters(java.util.Map.of(
                    "sellerId", List.of(String.valueOf(product.getSeller().getId())),
                    "productId", List.of(String.valueOf(product.getId()))
                )));
            }
        });

        Button btnToko = new Button("Toko");
        btnToko.addClassName("pd-btn-toko");
        btnToko.addClickListener(e -> {
            if (product.getSeller() != null) {
                UI.getCurrent().navigate("profile/" + product.getSeller().getId() + "?tab=products");
            }
        });

        sellerBtns.add(btnChatSeller, btnToko);
        sellerCard.add(sellerLeft, sellerBtns);

        // Stock & Quantity Selector for Desktop
        int maxStock = product.getStock() != null ? Math.max(0, product.getStock()) : 1;
        int[] selectedQty = {maxStock > 0 ? 1 : 0};
        boolean isOutOfStock = maxStock <= 0;

        // Desktop Quantity & Purchase Box
        Div desktopPurchaseBox = new Div();
        desktopPurchaseBox.getElement().getStyle()
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("gap", "16px")
            .set("margin-top", "8px")
            .set("padding-top", "16px")
            .set("border-top", "1px solid #E2E8F0");

        // Qty Stepper Row
        Div qtyRow = new Div();
        qtyRow.addClassName("rw-qty-row-box");

        Span qtyLabel = new Span("Jumlah Pembelian");
        qtyLabel.getElement().getStyle().set("font-size", "14px").set("font-weight", "700").set("color", "#0F172A");

        Div qtyStepper = new Div();
        qtyStepper.addClassName("rw-qty-stepper-box");

        Button btnMinus = new Button("-");
        btnMinus.addClassName("rw-qty-btn");
        Span qtyDisplay = new Span(String.valueOf(selectedQty[0]));
        qtyDisplay.getElement().getStyle().set("font-size", "15px").set("font-weight", "800").set("min-width", "32px").set("text-align", "center");
        Button btnPlus = new Button("+");
        btnPlus.addClassName("rw-qty-btn");

        btnMinus.addClickListener(e -> {
            if (selectedQty[0] > 1) {
                selectedQty[0]--;
                qtyDisplay.setText(String.valueOf(selectedQty[0]));
            }
        });

        btnPlus.addClickListener(e -> {
            if (selectedQty[0] < maxStock) {
                selectedQty[0]++;
                qtyDisplay.setText(String.valueOf(selectedQty[0]));
            } else {
                Notification.show("Maksimal stok tercapai (" + maxStock + ")", 2000, Notification.Position.TOP_CENTER);
            }
        });

        qtyStepper.add(btnMinus, qtyDisplay, btnPlus);

        Span stockBadge = new Span(isOutOfStock ? "Stok Habis" : "Stok: " + maxStock + " unit");
        stockBadge.getElement().getStyle()
            .set("font-size", "12px")
            .set("font-weight", "700")
            .set("color", isOutOfStock ? "#DC2626" : "#059669")
            .set("background", isOutOfStock ? "#FEE2E2" : "#D1FAE5")
            .set("padding", "4px 10px")
            .set("border-radius", "9999px")
            .set("white-space", "nowrap");

        Div qtyRight = new Div(qtyStepper, stockBadge);
        qtyRight.getElement().getStyle().set("display", "flex").set("flex-direction", "row").set("align-items", "center").set("gap", "10px");

        qtyRow.add(qtyLabel, qtyRight);

        // Desktop Action Buttons Row (+ Keranjang, Beli Sekarang, Chat)
        HorizontalLayout desktopActionRow = new HorizontalLayout();
        desktopActionRow.setWidthFull();
        desktopActionRow.setSpacing(true);

        Button btnAddToCart = new Button("+ Keranjang", VaadinIcon.CART_O.create());
        btnAddToCart.getElement().getStyle()
            .set("flex", "1")
            .set("height", "48px")
            .set("background", "#FFFFFF")
            .set("color", "#001934")
            .set("border", "2px solid #001934")
            .set("border-radius", "10px")
            .set("font-weight", "700")
            .set("font-size", "14px")
            .set("cursor", "pointer");

        if (isOutOfStock) btnAddToCart.setEnabled(false);

        btnAddToCart.addClickListener(e -> {
            if (isOutOfStock) return;
            if (!AuthGuard.requireLogin(UI.getCurrent())) return;
            User user = AuthGuard.getCurrentUser();
            cartService.addToCart(user, product, selectedQty[0]);
            MainLayout.reloadCartBadge(UI.getCurrent());
            Notification notif = Notification.show("Berhasil menambahkan " + selectedQty[0] + " item ke keranjang!", 2500, Notification.Position.TOP_CENTER);
            notif.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        Button btnBuyNow = new Button(isOutOfStock ? "Stok Habis" : "Beli Sekarang");
        btnBuyNow.getElement().getStyle()
            .set("flex", "1")
            .set("height", "48px")
            .set("background", "#001934")
            .set("color", "#FFFFFF")
            .set("border", "none")
            .set("border-radius", "10px")
            .set("font-weight", "800")
            .set("font-size", "14px")
            .set("cursor", "pointer");

        if (isOutOfStock) btnBuyNow.setEnabled(false);

        btnBuyNow.addClickListener(e -> {
            if (isOutOfStock) return;
            if (!AuthGuard.requireLogin(UI.getCurrent())) return;
            User user = AuthGuard.getCurrentUser();
            cartService.addToCart(user, product, selectedQty[0]);
            MainLayout.reloadCartBadge(UI.getCurrent());
            UI.getCurrent().navigate("checkout");
        });

        desktopActionRow.add(btnAddToCart, btnBuyNow);

        // Security Guarantee Info Box
        Div guaranteeBox = new Div();
        guaranteeBox.getElement().getStyle()
            .set("background", "#F0FDF4")
            .set("border", "1px solid #BBF7D0")
            .set("border-radius", "12px")
            .set("padding", "12px 16px")
            .set("display", "flex")
            .set("align-items", "center")
            .set("gap", "12px")
            .set("margin-top", "4px");

        Icon shieldIcon = VaadinIcon.SHIELD.create();
        shieldIcon.getElement().getStyle().set("color", "#166534").set("width", "20px").set("height", "20px").set("flex-shrink", "0");

        Div gTextCol = new Div();
        gTextCol.getElement().getStyle().set("display", "flex").set("flex-direction", "column");

        Span gTitle = new Span("Jaminan Transaksi SMKN 24");
        gTitle.getElement().getStyle().set("font-size", "13px").set("font-weight", "700").set("color", "#166534");

        Span gSub = new Span("Bebas penipuan dengan Sistem COD Sekolah & Moderation Protection ReWear.");
        gSub.getElement().getStyle().set("font-size", "11px").set("color", "#15803D");

        gTextCol.add(gTitle, gSub);
        guaranteeBox.add(shieldIcon, gTextCol);

        // Report Product Link Row
        HorizontalLayout reportRow = new HorizontalLayout();
        reportRow.setAlignItems(FlexComponent.Alignment.CENTER);
        reportRow.setSpacing(true);
        reportRow.getElement().getStyle().set("cursor", "pointer").set("margin-top", "4px");

        Icon flagIcon = VaadinIcon.FLAG.create();
        flagIcon.getElement().getStyle().set("width", "13px").set("height", "13px").set("color", "#94A3B8");

        Span reportLink = new Span("Laporkan produk ini");
        reportLink.getElement().getStyle().set("font-size", "12px").set("color", "#64748B").set("font-weight", "600");

        reportRow.add(flagIcon, reportLink);
        reportRow.addClickListener(e -> openReportProductDialog(product));

        desktopPurchaseBox.add(qtyRow, desktopActionRow, guaranteeBox, reportRow);

        rightCol.add(condWishRow, productTitle, priceVal, sellerCard, desktopPurchaseBox);
        mainGrid.add(leftGallery, rightCol);


        // ---- 4. TABS SECTION (Deskripsi | Spesifikasi | Ulasan) ----
        Div tabsSection = new Div();
        tabsSection.addClassName("pd-tabs-section");
        tabsSection.getElement().getStyle()
            .set("margin-top", "24px")
            .set("padding", "0 16px")
            .set("box-sizing", "border-box");

        Div tabHeader = new Div();
        tabHeader.addClassName("pd-tabs-header");

        Span tDesc = new Span("Deskripsi");
        tDesc.addClassNames("pd-tab-item", "active");


        Span tSpec = new Span("Spesifikasi");
        tSpec.addClassName("pd-tab-item");

        Span tReview = new Span("Ulasan");
        tReview.addClassName("pd-tab-item");

        tabHeader.add(tDesc, tSpec, tReview);

        // --- Container Tab 1: Description ---
        Div descTabContent = createDescriptionContent(product);

        // --- Container Tab 2: Specifications ---
        Div specTabContent = createSpecificationsContent(product);
        specTabContent.setVisible(false);

        // --- Container Tab 3: Reviews ---
        Div reviewsTabContent = createReviewsContent(reviews, avgRating);
        reviewsTabContent.setVisible(false);

        tDesc.addClickListener(e -> {
            tDesc.addClassName("active");
            tSpec.removeClassName("active");
            tReview.removeClassName("active");
            descTabContent.setVisible(true);
            specTabContent.setVisible(false);
            reviewsTabContent.setVisible(false);
        });

        tSpec.addClickListener(e -> {
            tSpec.addClassName("active");
            tDesc.removeClassName("active");
            tReview.removeClassName("active");
            descTabContent.setVisible(false);
            specTabContent.setVisible(true);
            reviewsTabContent.setVisible(false);
        });

        tReview.addClickListener(e -> {
            tReview.addClassName("active");
            tDesc.removeClassName("active");
            tSpec.removeClassName("active");
            descTabContent.setVisible(false);
            specTabContent.setVisible(false);
            reviewsTabContent.setVisible(true);
        });

        tabsSection.add(tabHeader, descTabContent, specTabContent, reviewsTabContent);

        // ---- 5. "MUNGKIN KAMU SUKA" RECOMMENDATIONS SECTION ----
        Div recommendSection = createRecommendationsSection(product);

        // ---- 6. MOBILE FIXED STICKY ACTION BAR (Matching Figma Exact Design) ----
        Div mobBottomActionBar = new Div();
        mobBottomActionBar.addClassName("rw-mobile-product-bottom-bar");

        // Action 1: Chat Icon + Label
        Div mobChatBtn = new Div();
        mobChatBtn.addClassName("rw-mob-action-item");
        Icon chatIcon = VaadinIcon.COMMENT_O.create();
        chatIcon.getElement().getStyle().set("width", "22px").set("height", "22px").set("color", "#001934");
        Span chatLbl = new Span("Chat");
        chatLbl.getElement().getStyle().set("font-size", "10px").set("font-weight", "700").set("color", "#475569");
        mobChatBtn.add(chatIcon, chatLbl);
        mobChatBtn.addClickListener(e -> {
            if (!AuthGuard.requireLogin(UI.getCurrent())) return;
            if (product.getSeller() != null) {
                UI.getCurrent().navigate("chat", new QueryParameters(java.util.Map.of(
                    "sellerId", List.of(String.valueOf(product.getSeller().getId())),
                    "productId", List.of(String.valueOf(product.getId()))
                )));
            }
        });

        // Action 2: Keranjang Icon + Label
        Div mobCartBtn = new Div();
        mobCartBtn.addClassName("rw-mob-action-item");
        Icon cartIcon = VaadinIcon.CART_O.create();
        cartIcon.getElement().getStyle().set("width", "22px").set("height", "22px").set("color", "#001934");
        Span cartLbl = new Span("Keranjang");
        cartLbl.getElement().getStyle().set("font-size", "10px").set("font-weight", "700").set("color", "#475569");
        mobCartBtn.add(cartIcon, cartLbl);
        mobCartBtn.addClickListener(e -> {
            if (isOutOfStock) {
                Notification.show("Produk ini sudah habis (Stok: 0)", 3000, Notification.Position.TOP_CENTER);
                return;
            }
            if (!AuthGuard.requireLogin(UI.getCurrent())) return;
            openMobileVariantSheet(product, false);
        });

        // Action 3: Beli Sekarang Primary Button
        Button mobBtnBuyNow = new Button(isOutOfStock ? "Stok Habis" : "Beli Sekarang");
        mobBtnBuyNow.addClassName("rw-mob-detail-buy-btn");
        if (isOutOfStock) mobBtnBuyNow.setEnabled(false);
        mobBtnBuyNow.addClickListener(e -> {
            if (isOutOfStock) return;
            if (!AuthGuard.requireLogin(UI.getCurrent())) return;
            openMobileVariantSheet(product, true);
        });


        mobBottomActionBar.add(mobChatBtn, mobCartBtn, mobBtnBuyNow);

        containerWrapper.add(mobTopHeader, topNavRow, mainGrid, tabsSection, recommendSection, mobBottomActionBar);
        return containerWrapper;
    }

    private Div createDescriptionContent(Product product) {
        Div container = new Div();
        container.setWidthFull();
        container.addClassName("pd-tab-content");

        String descText = product.getDescription();
        if (descText == null || descText.isBlank()) {
            descText = "Jaket denim koleksi pribadi, jarang dipakai. Kondisi masih sangat oke 9/10. Tidak ada robek atau noda. Sangat cocok buat hangout atau sekolah pas lagi santai. Gaya retro abis!\n\nDijual karena sudah kekecilan. Yuk diangkut sebelum keduluan yang lain!";
        }

        for (String p : descText.split("\n\n")) {
            Paragraph pDesc = new Paragraph(p.trim());
            pDesc.getElement().getStyle()
                .set("color", "#475569")
                .set("line-height", "1.6")
                .set("font-size", "14px")
                .set("margin-bottom", "12px");
            container.add(pDesc);
        }

        return container;
    }


    private Div createReviewsContent(List<Review> reviews, double avgRating) {
        Div container = new Div();
        container.setWidthFull();

        Div reviewsWrapper = new Div();
        reviewsWrapper.getElement().getStyle().set("display", "flex").set("flex-direction", "column").set("gap", "24px");

        // Rating Overview Summary Box
        Div ratingSummaryBox = new Div();
        ratingSummaryBox.getElement().getStyle()
            .set("background", "#F8FAFC")
            .set("border", "1px solid #E2E8F0")
            .set("border-radius", "16px")
            .set("padding", "20px 24px")
            .set("display", "flex")
            .set("align-items", "center")
            .set("gap", "32px");

        Div scoreBox = new Div();
        scoreBox.getStyle().set("text-align", "center");
        H2 bigScore = new H2(reviews == null || reviews.isEmpty() ? "0.0" : String.format("%.1f", avgRating));
        bigScore.getStyle().set("font-size", "40px").set("font-weight", "900").set("color", "#001934").set("margin", "0");
        Span outOf = new Span("dari 5.0");
        outOf.getStyle().set("font-size", "12px").set("color", "#64748B");
        scoreBox.add(bigScore, outOf);

        Div scoreDetails = new Div();
        scoreDetails.getStyle().set("display", "flex").set("flex-direction", "column").set("gap", "4px");
        Component starRender = renderStars((int) Math.round(avgRating));
        int totalRev = (reviews != null) ? reviews.size() : 0;
        Span totalRevText = new Span("Berdasarkan " + totalRev + " ulasan pembeli terverifikasi");
        totalRevText.getStyle().set("font-size", "13px").set("color", "#475569").set("font-weight", "600");
        scoreDetails.add(starRender, totalRevText);

        ratingSummaryBox.add(scoreBox, scoreDetails);
        reviewsWrapper.add(ratingSummaryBox);

        // List of Review Cards
        if (reviews == null || reviews.isEmpty()) {
            Div emptyRev = new Div();
            emptyRev.getElement().setProperty("innerHTML",
                "<div style='text-align:center;padding:48px 20px;background:#FFFFFF;border-radius:16px;border:1px dashed #CBD5E1;'>" +
                "<div style='width:44px;height:44px;margin:0 auto 10px;border-radius:10px;background:#F1F5F9;display:flex;align-items:center;justify-content:center;'>" +
                "<svg width='22' height='22' viewBox='0 0 24 24' fill='none' stroke='#64748B' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><path d='M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z'></path></svg>" +
                "</div>" +
                "<h4 style='color:#001934;margin:0 0 6px;font-size:16px;font-weight:700;'>Belum Ada Ulasan Pembeli</h4>" +
                "<p style='color:#64748B;font-size:13px;margin:0;'>Jadilah pembeli pertama yang memberikan ulasan setelah menyelesaikan pesanan!</p>" +
                "</div>");
            reviewsWrapper.add(emptyRev);
        } else {
            Div reviewsList = new Div();
            reviewsList.getStyle().set("display", "flex").set("flex-direction", "column").set("gap", "16px");

            for (Review rev : reviews) {
                Div revCard = new Div();
                revCard.getStyle()
                    .set("background", "#FFFFFF")
                    .set("border", "1px solid #E2E8F0")
                    .set("border-radius", "14px")
                    .set("padding", "18px 20px")
                    .set("display", "flex")
                    .set("flex-direction", "column")
                    .set("gap", "8px");

                HorizontalLayout topRow = new HorizontalLayout();
                topRow.setWidthFull();
                topRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
                topRow.setAlignItems(FlexComponent.Alignment.CENTER);

                HorizontalLayout userLeft = new HorizontalLayout();
                userLeft.setAlignItems(FlexComponent.Alignment.CENTER);
                userLeft.setSpacing(true);

                String buyerName = "Pembeli ReWear";
                String buyerAvatar = null;
                try {
                    if (rev.getBuyer() != null) {
                        if (rev.getBuyer().getFullName() != null && !rev.getBuyer().getFullName().isBlank()) {
                            buyerName = rev.getBuyer().getFullName();
                        }
                        buyerAvatar = rev.getBuyer().getAvatarUrl();
                    }
                } catch (Exception ignored) {}

                Component bAvatar;
                if (buyerAvatar != null && !buyerAvatar.isBlank() && !buyerAvatar.contains("buku.jpeg")) {
                    Image bImg = new Image(buyerAvatar, buyerName);
                    bImg.getStyle().set("width", "36px").set("height", "36px").set("border-radius", "9999px").set("object-fit", "cover");
                    bAvatar = bImg;
                } else {
                    Div bInit = new Div(new Span(getInitials(buyerName)));
                    bInit.getStyle().set("width", "36px").set("height", "36px").set("border-radius", "9999px")
                        .set("background", "#001934").set("color", "#F5C45E").set("font-weight", "800")
                        .set("font-size", "13px").set("display", "flex").set("align-items", "center").set("justify-content", "center");
                    bAvatar = bInit;
                }

                Div nameAndDate = new Div();
                nameAndDate.getStyle().set("display", "flex").set("flex-direction", "column");
                Span bNameSpan = new Span(buyerName);
                bNameSpan.getStyle().set("font-size", "14px").set("font-weight", "700").set("color", "#001934");
                String dateStr = rev.getCreatedAt() != null ? rev.getCreatedAt().format(DATE_FMT) : "";
                Span bDateSpan = new Span(dateStr);
                bDateSpan.getStyle().set("font-size", "11px").set("color", "#94A3B8");
                nameAndDate.add(bNameSpan, bDateSpan);

                userLeft.add(bAvatar, nameAndDate);

                Component revStars = renderStars(rev.getRating() != null ? rev.getRating() : 5);

                topRow.add(userLeft, revStars);

                if (rev.getComment() != null && !rev.getComment().isBlank()) {
                    Paragraph commentText = new Paragraph(rev.getComment());
                    commentText.getStyle().set("color", "#334155").set("font-size", "13px").set("line-height", "1.6").set("margin", "4px 0 0 0");
                    revCard.add(topRow, commentText);
                } else {
                    revCard.add(topRow);
                }

                reviewsList.add(revCard);
            }
            reviewsWrapper.add(reviewsList);
        }

        container.add(reviewsWrapper);
        return container;
    }

    private Component renderStars(int rating) {
        HorizontalLayout starsLayout = new HorizontalLayout();
        starsLayout.setSpacing(false);
        for (int i = 1; i <= 5; i++) {
            Icon star = VaadinIcon.STAR.create();
            star.getStyle().set("width", "14px").set("height", "14px");
            if (i <= rating) {
                star.getStyle().set("color", "#F59E0B");
            } else {
                star.getStyle().set("color", "#CBD5E1");
            }
            starsLayout.add(star);
        }
        return starsLayout;
    }

    private Div createWhyFeature(String svgIcon, String titleText, String descText) {
        Div feat = new Div();
        feat.getElement().setProperty("innerHTML",
            "<div style='display:flex;align-items:flex-start;gap:12px;'>" +
            "<div style='flex-shrink:0;margin-top:2px;'>" + svgIcon + "</div>" +
            "<div><span style='font-weight:700;font-size:14px;color:#0F172A;display:block;margin-bottom:3px;'>" + titleText + "</span>" +
            "<span style='font-size:12px;color:#64748B;line-height:1.5;'>" + descText + "</span></div></div>");
        return feat;
    }

    private Component buildFallbackUI(Long productId) {
        Div wrapper = new Div();
        wrapper.getElement().getStyle()
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("align-items", "center")
            .set("justify-content", "center")
            .set("padding", "80px 24px")
            .set("text-align", "center");

        Icon searchIcon = VaadinIcon.SEARCH.create();
        searchIcon.getStyle().set("width", "36px").set("height", "36px").set("color", "#64748B").set("margin-bottom", "12px");

        Paragraph title = new Paragraph("Produk tidak ditemukan");
        title.getElement().getStyle().set("font-size", "22px").set("font-weight", "700").set("color", "#001934").set("margin", "0 0 8px");

        Paragraph subtitle = new Paragraph("Produk dengan ID " + productId + " tidak ada atau sudah tidak tersedia.");
        subtitle.getElement().getStyle().set("font-size", "14px").set("color", "#64748B").set("margin", "0 0 24px");


        Button btnBack = new Button("Kembali ke Beranda");
        btnBack.getElement().getStyle()
            .set("background", "#001934")
            .set("color", "#FFFFFF")
            .set("border", "none")
            .set("padding", "12px 24px")
            .set("border-radius", "8px")
            .set("font-weight", "600")
            .set("cursor", "pointer");
        btnBack.addClickListener(e -> UI.getCurrent().navigate(""));

        wrapper.add(title, subtitle, btnBack);
        return wrapper;
    }

    private String getCategoryName(Product product) {
        try {
            if (product != null && product.getCategory() != null) {
                return product.getCategory().getName();
            }
        } catch (Exception ignored) {}
        return "Katalog";
    }

    private String getSellerName(Product product) {
        try {
            if (product != null && product.getSeller() != null) {
                return product.getSeller().getFullName();
            }
        } catch (Exception ignored) {}
        return "Warga SMKN 24";
    }

    private String getInitials(String name) {
        if (name == null || name.isBlank()) return "RW";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
    }

    private String extractImgUrl(String imagesJson, String fallback) {
        if (imagesJson == null || !imagesJson.contains("images/")) {
            return fallback;
        }
        return imagesJson.replace("[\"", "").replace("\"]", "").trim();
    }

    private String getSellerSchool(Product product) {
        try {
            if (product != null && product.getSeller() != null && product.getSeller().getSchool() != null) {
                return product.getSeller().getSchool().getName();
            }
        } catch (Exception ignored) {}
        return "SMKN 24 Jakarta";
    }

    private void openReportProductDialog(Product product) {
        if (!AuthGuard.requireLogin(UI.getCurrent())) return;
        User user = AuthGuard.getCurrentUser();

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Laporkan Produk");
        dialog.setWidth("460px");

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(false);

        Paragraph info = new Paragraph("Laporan Anda akan ditinjau secara rahasia oleh tim Moderator ReWear SMKN 24.");
        info.getElement().getStyle().set("font-size", "13px").set("color", "#64748B").set("margin", "0");

        ComboBox<String> reasonCombo = new ComboBox<>("Alasan Pelaporan");
        reasonCombo.setItems(
            "Barang Tiruan / Palsu",
            "Kondisi Tidak Sesuai Foto / Deskripsi",
            "Indikasi Penipuan / Harga Tidak Wajar",
            "Konten / Foto Tidak Pantas",
            "Barang Dilarang di Lingkungan Sekolah",
            "Lainnya"
        );
        reasonCombo.setValue("Kondisi Tidak Sesuai Foto / Deskripsi");
        reasonCombo.setWidthFull();

        TextArea descField = new TextArea("Jelaskan Masalahnya (Opsional)");
        descField.setPlaceholder("Berikan rincian singkat mengenai pelanggaran produk ini...");
        descField.setWidthFull();
        descField.setMaxLength(500);

        layout.add(info, reasonCombo, descField);

        Button btnCancel = new Button("Batal", e -> dialog.close());
        btnCancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button btnSubmit = new Button("Kirim Laporan", e -> {
            String reason = reasonCombo.getValue();
            String desc = descField.getValue();
            if (reason == null || reason.isBlank()) {
                Notification.show("Harap pilih alasan pelaporan.", 3000, Notification.Position.TOP_CENTER);
                return;
            }

            try {
                moderationService.reportProduct(user, product, reason, desc);
                Notification.show("Laporan berhasil dikirim ke tim Moderator ReWear. Terima kasih.", 3500, Notification.Position.TOP_CENTER);
                dialog.close();
            } catch (Exception ex) {
                Notification.show("Gagal mengirim laporan: " + ex.getMessage(), 3000, Notification.Position.TOP_CENTER);
            }
        });
        btnSubmit.getElement().getStyle()
            .set("background", "#DC2626").set("color", "#FFFFFF").set("font-weight", "700")
            .set("border-radius", "8px").set("border", "none").set("cursor", "pointer");

        dialog.getFooter().add(btnCancel, btnSubmit);
        dialog.add(layout);
        dialog.open();
    }

    private Div createSpecificationsContent(Product product) {
        Div container = new Div();
        container.setWidthFull();
        container.getElement().getStyle()
            .set("background", "#F8FAFC")
            .set("border", "1px solid #E2E8F0")
            .set("border-radius", "14px")
            .set("padding", "20px")
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("gap", "12px");

        String cond = product.getCondition() != null ? product.getCondition().name().replace("_", " ") : "Bagus / Seperti Baru";
        String cat = getCategoryName(product);
        String seller = getSellerName(product);
        String stock = (product.getStock() != null ? product.getStock() : 1) + " buah";

        container.add(createSpecRow("Kondisi Barang", cond));
        container.add(createSpecRow("Kategori Produk", cat));
        container.add(createSpecRow("Penjual Terverifikasi", seller));
        container.add(createSpecRow("Jumlah Stok Tersedia", stock));
        container.add(createSpecRow("Sistem Pengiriman", "COD SMKN 24 / Escrow Protected"));

        return container;
    }

    private Div createSpecRow(String label, String value) {
        Div row = new Div();
        row.getElement().getStyle()
            .set("display", "flex")
            .set("justify-content", "space-between")
            .set("font-size", "13px")
            .set("border-bottom", "1px solid #E2E8F0")
            .set("padding-bottom", "8px");

        Span l = new Span(label);
        l.getStyle().set("color", "#64748B").set("font-weight", "600");

        Span v = new Span(value);
        v.getStyle().set("color", "#001934").set("font-weight", "700");

        row.add(l, v);
        return row;
    }

    private Div createRecommendationsSection(Product currentProduct) {
        Div section = new Div();
        section.addClassName("pd-recommend-section");
        section.getElement().getStyle()
            .set("margin-top", "32px")
            .set("margin-bottom", "16px")
            .set("padding", "0 16px")
            .set("box-sizing", "border-box");

        H3 title = new H3("Mungkin Kamu Suka");
        title.getElement().getStyle()
            .set("font-size", "17px")
            .set("font-weight", "800")
            .set("color", "#0F172A")
            .set("margin", "0 0 16px 0");

        Div scrollContainer = new Div();
        scrollContainer.getElement().getStyle()
            .set("display", "flex")
            .set("gap", "14px")
            .set("overflow-x", "auto")
            .set("padding-bottom", "12px")
            .set("-webkit-overflow-scrolling", "touch");

        List<Product> recommendedList = List.of();
        try {
            recommendedList = productService.findActiveWithCategory();
        } catch (Exception ignored) {}

        int count = 0;
        for (Product p : recommendedList) {
            if (p.getId().equals(currentProduct.getId())) continue;
            if (count >= 6) break;

            Div card = new Div();
            card.getElement().getStyle()
                .set("min-width", "145px")
                .set("width", "145px")
                .set("background", "#FFFFFF")
                .set("border", "1px solid #E2E8F0")
                .set("border-radius", "14px")
                .set("overflow", "hidden")
                .set("padding", "10px")
                .set("box-sizing", "border-box")
                .set("cursor", "pointer")
                .set("flex-shrink", "0")
                .set("box-shadow", "0 2px 8px rgba(0, 25, 52, 0.03)");

            card.addClickListener(e -> {
                UI.getCurrent().navigate("product/" + p.getId());
                UI.getCurrent().getPage().executeJs("window.scrollTo(0, 0);");
            });


            String imgUrl = extractImgUrl(p.getImages(), "images/buku.jpeg");
            Image img = new Image(imgUrl, p.getName());
            img.getElement().getStyle()
                .set("width", "100%")
                .set("height", "110px")
                .set("object-fit", "cover")
                .set("border-radius", "10px")
                .set("margin-bottom", "8px");

            H4 pName = new H4(p.getName());
            pName.getElement().getStyle()
                .set("font-size", "12px")
                .set("font-weight", "700")
                .set("color", "#0F172A")
                .set("margin", "0 0 4px 0")
                .set("white-space", "nowrap")
                .set("overflow", "hidden")
                .set("text-overflow", "ellipsis");

            BigDecimal pPriceVal = p.getPrice() != null ? p.getPrice() : BigDecimal.ZERO;
            Span pPrice = new Span("Rp " + String.format("%,.0f", pPriceVal));
            pPrice.getElement().getStyle()
                .set("font-size", "13px")
                .set("font-weight", "800")
                .set("color", "#001934");

            card.add(img, pName, pPrice);
            scrollContainer.add(card);
            count++;
        }

        section.add(title, scrollContainer);
        return section;
    }

    private void openMobileVariantSheet(Product product, boolean isBuyNow) {
        Dialog sheet = new Dialog();
        sheet.addClassName("rw-bottom-sheet-dialog");
        sheet.setWidth("100%");
        sheet.setMaxWidth("480px");
        sheet.getElement().getStyle()
            .set("margin", "0 auto 0 auto")
            .set("position", "fixed")
            .set("bottom", "0")
            .set("border-radius", "24px 24px 0 0");

        Div content = new Div();
        content.getStyle()
            .set("background", "#FFFFFF")
            .set("padding", "20px 24px 28px")
            .set("border-radius", "24px 24px 0 0")
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("gap", "16px");

        Div handle = new Div();
        handle.getStyle().set("width", "38px").set("height", "4px").set("background", "#CBD5E1").set("border-radius", "99px").set("margin", "0 auto 8px");

        HorizontalLayout topRow = new HorizontalLayout();
        topRow.setWidthFull();
        topRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        HorizontalLayout prodInfo = new HorizontalLayout();
        prodInfo.setAlignItems(FlexComponent.Alignment.CENTER);
        prodInfo.setSpacing(true);

        String mainImgUrl = extractImgUrl(product.getImages(), "images/buku.jpeg");
        Image thumb = new Image(mainImgUrl, "Thumb");
        thumb.getStyle().set("width", "56px").set("height", "56px").set("border-radius", "10px").set("object-fit", "cover");

        Div textCol = new Div();
        textCol.getStyle().set("display", "flex").set("flex-direction", "column");
        BigDecimal price = product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO;
        Span pPrice = new Span("Rp " + String.format("%,.0f", price).replace(",", "."));
        pPrice.getStyle().set("font-size", "18px").set("font-weight", "800").set("color", "#001934");
        Span pStock = new Span("Stok: " + (product.getStock() != null ? product.getStock() : 1) + " unit");
        pStock.getStyle().set("font-size", "12px").set("color", "#64748B");
        textCol.add(pPrice, pStock);

        prodInfo.add(thumb, textCol);

        Button closeBtn = new Button(VaadinIcon.CLOSE_SMALL.create(), e -> sheet.close());
        closeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        topRow.add(prodInfo, closeBtn);

        Div sizeSec = new Div();
        Span sizeTitle = new Span("Pilih Ukuran:");
        sizeTitle.getStyle().set("font-size", "13px").set("font-weight", "700").set("color", "#0F172A").set("margin-bottom", "8px").set("display", "block");

        HorizontalLayout sizeChips = new HorizontalLayout();
        sizeChips.setSpacing(true);
        String[] sizes = {"All Size", "S", "M", "L", "XL"};
        String[] selectedSize = {sizes[0]};

        for (int i = 0; i < sizes.length; i++) {
            String sz = sizes[i];
            Div chip = new Div(new Span(sz));
            chip.addClassName("rw-sheet-chip");
            if (i == 0) chip.addClassName("selected");
            chip.addClickListener(e -> {
                sizeChips.getChildren().forEach(c -> c.removeClassName("selected"));
                chip.addClassName("selected");
                selectedSize[0] = sz;
            });
            sizeChips.add(chip);
        }
        sizeSec.add(sizeTitle, sizeChips);

        Div qtySec = new Div();
        qtySec.addClassName("rw-qty-row-box");
        qtySec.getStyle().set("margin-top", "12px").set("margin-bottom", "12px");
        Span qtyTitle = new Span("Jumlah Pembelian:");
        qtyTitle.getStyle().set("font-size", "14px").set("font-weight", "700").set("color", "#0F172A");

        int maxStock = product.getStock() != null ? Math.max(1, product.getStock()) : 1;
        int[] qty = {1};

        Div stepper = new Div();
        stepper.addClassName("rw-qty-stepper-box");

        Button minusBtn = new Button("-");
        minusBtn.addClassName("rw-qty-btn");
        Span qtyVal = new Span("1");
        qtyVal.getStyle().set("font-size", "15px").set("font-weight", "800").set("min-width", "32px").set("text-align", "center");
        Button plusBtn = new Button("+");
        plusBtn.addClassName("rw-qty-btn");

        minusBtn.addClickListener(e -> {
            if (qty[0] > 1) {
                qty[0]--;
                qtyVal.setText(String.valueOf(qty[0]));
            }
        });
        plusBtn.addClickListener(e -> {
            if (qty[0] < maxStock) {
                qty[0]++;
                qtyVal.setText(String.valueOf(qty[0]));
            } else {
                Notification.show("Maksimal stok tercapai (" + maxStock + ")", 2000, Notification.Position.TOP_CENTER);
            }
        });

        stepper.add(minusBtn, qtyVal, plusBtn);
        qtySec.add(qtyTitle, stepper);

        Button confirmBtn = new Button(isBuyNow ? "Beli Sekarang" : "Konfirmasi (+ Keranjang)");
        confirmBtn.getStyle()
            .set("background", "#001934")
            .set("color", "#FFFFFF")
            .set("font-weight", "800")
            .set("font-size", "15px")
            .set("border-radius", "12px")
            .set("padding", "14px")
            .set("width", "100%")
            .set("margin-top", "8px")
            .set("cursor", "pointer");

        confirmBtn.addClickListener(e -> {
            sheet.close();
            if (!AuthGuard.requireLogin(UI.getCurrent())) return;
            User user = AuthGuard.getCurrentUser();
            cartService.addToCart(user, product, qty[0]);
            MainLayout.reloadCartBadge(UI.getCurrent());

            if (isBuyNow) {
                UI.getCurrent().navigate("checkout");
            } else {
                Notification notif = Notification.show("Ditambahkan (" + qty[0] + "x " + selectedSize[0] + ") ke keranjang!", 2500, Notification.Position.TOP_CENTER);
                notif.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            }
        });

        content.add(handle, topRow, sizeSec, qtySec, confirmBtn);
        sheet.add(content);
        sheet.open();
    }

    private void openImageLightbox(String imageUrl) {
        Dialog dialog = new Dialog();
        dialog.setWidth("100vw");
        dialog.setHeight("100vh");
        dialog.getElement().getStyle()
            .set("background", "rgba(0, 0, 0, 0.95)")
            .set("padding", "0")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center");

        Div wrapper = new Div();
        wrapper.getStyle().set("position", "relative").set("width", "100%").set("height", "100%").set("display", "flex").set("align-items", "center").set("justify-content", "center");

        Image img = new Image(imageUrl, "Zoomed Image");
        img.getStyle().set("max-width", "95%").set("max-height", "85vh").set("object-fit", "contain").set("border-radius", "12px");

        Button closeBtn = new Button("✕", e -> dialog.close());
        closeBtn.getStyle()
            .set("position", "absolute")
            .set("top", "20px")
            .set("right", "20px")
            .set("background", "rgba(255,255,255,0.2)")
            .set("color", "#FFF")
            .set("border", "none")
            .set("border-radius", "99px")
            .set("width", "40px")
            .set("height", "40px")
            .set("font-size", "20px")
            .set("cursor", "pointer");

        wrapper.add(img, closeBtn);
        dialog.add(wrapper);
        dialog.open();
    }
}

