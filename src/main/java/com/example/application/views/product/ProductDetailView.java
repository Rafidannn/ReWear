package com.example.application.views.product;

import com.example.application.model.moderation.Review;
import com.example.application.model.product.Product;
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

import com.example.application.views.order.CartItem;
import com.vaadin.flow.server.VaadinSession;

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

        if (productId == null) {
            contentArea.add(buildFallbackUI(0L));
            return;
        }

        try {
            Optional<Product> productOpt = productService.findById(productId);
            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                contentArea.add(buildProductDetailUI(product));
            } else {
                contentArea.add(buildFallbackUI(productId));
            }
        } catch (Exception ex) {
            contentArea.add(buildFallbackUI(productId));
        }
    }

    private Component buildProductDetailUI(Product product) {
        Div containerWrapper = new Div();
        containerWrapper.addClassName("product-detail-page-container");
        containerWrapper.getElement().getStyle()
            .set("max-width", "1280px")
            .set("margin", "0 auto")
            .set("padding", "24px 48px 64px 48px")
            .set("box-sizing", "border-box");

        // ---- 1. TOP NAV / BACK & BREADCRUMBS ----
        HorizontalLayout topNavRow = new HorizontalLayout();
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
        breadcrumb.getElement().getStyle()
            .set("font-size", "13px")
            .set("color", "#64748B");

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

        // ---- 2. MAIN GRID (Left Gallery 48% | Right Actions 52%) ----
        HorizontalLayout mainGrid = new HorizontalLayout();
        mainGrid.setWidthFull();
        mainGrid.setSpacing(true);
        mainGrid.setAlignItems(FlexComponent.Alignment.START);

        // --- LEFT GALLERY ---
        HorizontalLayout leftGallery = new HorizontalLayout();
        leftGallery.setWidth("48%");
        leftGallery.setSpacing(true);

        String imagesJson = product.getImages();
        String mainImgUrl = extractImgUrl(imagesJson, "images/buku.jpeg");

        // Main Image Display
        Div mainImgBox = new Div();
        mainImgBox.getElement().getStyle()
            .set("position", "relative")
            .set("flex", "1")
            .set("height", "440px")
            .set("border-radius", "16px")
            .set("overflow", "hidden")
            .set("background", "#FFFFFF")
            .set("border", "1px solid #E2E8F0");

        Image heroImg = new Image(mainImgUrl, product.getName());
        heroImg.getElement().getStyle().set("width", "100%").set("height", "100%").set("object-fit", "cover");
        mainImgBox.add(heroImg);

        // Multiple Thumbnails with Interactive Click
        VerticalLayout thumbsCol = new VerticalLayout();
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
                        .set("border", i == 0 ? "2px solid #001934" : "1px solid #E2E8F0")
                        .set("cursor", "pointer")
                        .set("transition", "border 0.2s ease");
                    Image tImg = new Image(url, "Thumb");
                    tImg.getElement().getStyle().set("width", "100%").set("height", "100%").set("object-fit", "cover");
                    thumb.add(tImg);

                    final int currentIdx = i;
                    thumb.addClickListener(e -> {
                        heroImg.setSrc(url);
                        for (int k = 0; k < thumbDivs.size(); k++) {
                            thumbDivs.get(k).getElement().getStyle().set("border", k == currentIdx ? "2px solid #001934" : "1px solid #E2E8F0");
                        }
                    });

                    thumbDivs.add(thumb);
                    thumbsCol.add(thumb);
                }
                leftGallery.add(thumbsCol);
            }
        }

        // Badge Warga SMKN 24
        if (product.isSchoolMarket()) {
            Div verBadge = new Div();
            verBadge.getElement().setProperty("innerHTML",
                "<div style='display:flex;align-items:center;gap:6px;background:#F5C45E;color:#001934;" +
                "font-weight:700;padding:6px 14px;border-radius:9999px;font-size:12px;position:absolute;top:16px;left:16px;z-index:2;'>" +
                "<svg width='12' height='12' viewBox='0 0 12 12' fill='none' xmlns='http://www.w3.org/2000/svg'>" +
                "<path d='M2 6L5 9L10 3' stroke='#001934' stroke-width='1.8' stroke-linecap='round' stroke-linejoin='round'/>" +
                "</svg>Warga SMKN 24</div>");
            mainImgBox.add(verBadge);
        }

        leftGallery.add(mainImgBox);

        // --- RIGHT PRODUCT DETAILS & ACTIONS ---
        Div rightCol = new Div();
        rightCol.setWidth("52%");
        rightCol.getElement().getStyle()
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("gap", "16px")
            .set("padding-left", "16px");

        // Title
        H1 productTitle = new H1(product.getName());
        productTitle.getElement().getStyle()
            .set("font-size", "26px")
            .set("font-weight", "800")
            .set("color", "#0F172A")
            .set("line-height", "1.3")
            .set("margin", "0");

        // Price + Condition Chip Row
        HorizontalLayout priceRow = new HorizontalLayout();
        priceRow.setAlignItems(FlexComponent.Alignment.CENTER);
        priceRow.setSpacing(true);

        BigDecimal price = (product.getPrice() != null) ? product.getPrice() : BigDecimal.ZERO;
        Span priceVal = new Span("Rp " + String.format("%,.0f", price));
        priceVal.getElement().getStyle().set("font-size", "30px").set("font-weight", "800").set("color", "#0F172A");

        Span condChip = new Span("KONDISI: " + (product.getCondition() != null ? product.getCondition().name().replace("_", " ") : "LIKE NEW"));
        condChip.getElement().getStyle()
            .set("background", "#FFDEA2")
            .set("color", "#261900")
            .set("font-weight", "700")
            .set("font-size", "11px")
            .set("padding", "4px 10px")
            .set("border-radius", "6px")
            .set("letter-spacing", "0.5px");

        priceRow.add(priceVal, condChip);

        // Rating & Sales Count (REAL DATABASE STATS)
        List<Review> reviews = List.of();
        try {
            reviews = moderationService.getProductReviews(product);
        } catch (Exception ignored) {}
        if (reviews == null) reviews = List.of();

        int soldCount = product.getSoldCount() != null ? product.getSoldCount() : 0;
        double avgRating = 0.0;
        if (!reviews.isEmpty()) {
            avgRating = reviews.stream().mapToInt(r -> r.getRating() != null ? r.getRating() : 5).average().orElse(0.0);
        }

        Div ratingRow = new Div();
        ratingRow.getElement().getStyle().set("display", "flex").set("align-items", "center").set("gap", "8px");

        if (!reviews.isEmpty()) {
            Span starIcons = new Span("Rating " + String.format("%.1f / 5.0", avgRating));
            starIcons.getStyle().set("font-size", "13px").set("font-weight", "800").set("color", "#D97706");
            Span revCount = new Span("(" + reviews.size() + " Ulasan)");
            revCount.getStyle().set("font-size", "13px").set("color", "#64748B");
            Span dot = new Span("•");
            dot.getStyle().set("color", "#CBD5E1");
            Span soldText = new Span("Terjual " + soldCount);
            soldText.getStyle().set("font-size", "13px").set("color", "#64748B");
            ratingRow.add(starIcons, revCount, dot, soldText);
        } else {
            Span noRev = new Span("Belum ada ulasan");
            noRev.getStyle().set("font-size", "13px").set("color", "#94A3B8");
            Span dot = new Span("•");
            dot.getStyle().set("color", "#CBD5E1");
            Span soldText = new Span("Terjual " + soldCount);
            soldText.getStyle().set("font-size", "13px").set("color", "#64748B");
            ratingRow.add(noRev, dot, soldText);
        }

        // Seller Card
        Div sellerCard = new Div();
        sellerCard.getElement().getStyle()
            .set("background", "#EFF4FF")
            .set("border-radius", "12px")
            .set("padding", "16px")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "space-between");

        HorizontalLayout sellerLeft = new HorizontalLayout();
        sellerLeft.setAlignItems(FlexComponent.Alignment.CENTER);
        sellerLeft.setSpacing(true);

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
        sellerInfo.getElement().getStyle().set("display", "flex").set("flex-direction", "column").set("gap", "2px");

        HorizontalLayout sNameRow = new HorizontalLayout();
        sNameRow.setAlignItems(FlexComponent.Alignment.CENTER);
        sNameRow.setSpacing(true);

        Span sellerName = new Span(getSellerName(product));
        sellerName.getElement().getStyle().set("font-weight", "700").set("font-size", "14px").set("color", "#001934");

        Span sellerRole = new Span("WARGA SMKN 24");
        sellerRole.getElement().getStyle()
            .set("background", "#FFDEA2").set("color", "#261900")
            .set("font-weight", "800").set("font-size", "10px")
            .set("padding", "2px 8px").set("border-radius", "4px");

        sNameRow.add(sellerName, sellerRole);

        Span sellerSchool = new Span("Terverifikasi • " + getSellerSchool(product));
        sellerSchool.getElement().getStyle().set("font-size", "12px").set("color", "#64748B");

        sellerInfo.add(sNameRow, sellerSchool);
        sellerLeft.add(sellerAvatar, sellerInfo);

        Button btnChatSeller = new Button("Chat Penjual", VaadinIcon.COMMENT.create());
        btnChatSeller.getElement().getStyle()
            .set("background", "#FF9E59").set("color", "#FFFFFF").set("font-weight", "700")
            .set("border", "none").set("border-radius", "8px").set("padding", "10px 18px").set("cursor", "pointer");
        btnChatSeller.addClickListener(e -> {
            if (!AuthGuard.requireLogin(UI.getCurrent())) return;
            if (product.getSeller() != null) {
                Long sellerIdParam = product.getSeller().getId();
                Long productIdParam = product.getId();
                UI.getCurrent().navigate("chat",
                    new QueryParameters(java.util.Map.of(
                        "sellerId", List.of(String.valueOf(sellerIdParam)),
                        "productId", List.of(String.valueOf(productIdParam))
                    )));
            } else {
                Notification.show("Info penjual tidak tersedia.", 2000, Notification.Position.TOP_CENTER);
            }
        });

        HorizontalLayout sellerActions = new HorizontalLayout();
        sellerActions.setAlignItems(FlexComponent.Alignment.CENTER);
        sellerActions.setSpacing(true);

        if (product.getSeller() != null) {
            Long sellerId = product.getSeller().getId();
            sellerLeft.getElement().getStyle().set("cursor", "pointer");
            sellerLeft.addClickListener(e -> UI.getCurrent().navigate("profile/" + sellerId + "?tab=products"));

            Button btnKunjungiToko = new Button("Lihat Toko", VaadinIcon.SHOP.create());
            btnKunjungiToko.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            btnKunjungiToko.getStyle().set("font-size", "12px").set("font-weight", "700");
            btnKunjungiToko.addClickListener(e -> UI.getCurrent().navigate("profile/" + sellerId + "?tab=products"));

            sellerActions.add(btnKunjungiToko, btnChatSeller);
        } else {
            sellerActions.add(btnChatSeller);
        }

        sellerCard.add(sellerLeft, sellerActions);

        // --- 1. INTERACTIVE SIZE SELECTOR (Only for Clothing / Apparel categories) ---
        boolean isPakaian = product.getCategory() != null &&
            (product.getCategory().getSlug().contains("pakaian") ||
             product.getCategory().getSlug().contains("seragam") ||
             product.getCategory().getSlug().contains("baju") ||
             product.getCategory().getSlug().contains("celana") ||
             product.getCategory().getSlug().contains("jaket") ||
             product.getCategory().getSlug().contains("sepatu"));

        String[] selectedSize = {isPakaian ? "M" : null};
        Div sizeBox = new Div();

        if (isPakaian) {
            Span sizeLabel = new Span("Pilih Ukuran:");
            sizeLabel.getElement().getStyle().set("font-size", "13px").set("font-weight", "700").set("color", "#0F172A").set("display", "block").set("margin-bottom", "8px");

            HorizontalLayout sizePills = new HorizontalLayout();
            sizePills.setSpacing(true);
            String[] sizes = {"S", "M", "L", "XL"};
            List<Span> pillList = new ArrayList<>();

            for (int i = 0; i < sizes.length; i++) {
                final String sz = sizes[i];
                Span pill = new Span(sz);
                boolean isDefault = sz.equals(selectedSize[0]);
                pill.getElement().getStyle()
                    .set("padding", "8px 18px")
                    .set("border-radius", "8px")
                    .set("font-weight", "700")
                    .set("font-size", "13px")
                    .set("cursor", "pointer")
                    .set("transition", "all 0.2s ease")
                    .set("background", isDefault ? "#001934" : "#FFFFFF")
                    .set("color", isDefault ? "#FFFFFF" : "#0F172A")
                    .set("border", isDefault ? "2px solid #001934" : "1.5px solid #CBD5E1");

                final int idx = i;
                pill.addClickListener(e -> {
                    selectedSize[0] = sz;
                    for (int j = 0; j < pillList.size(); j++) {
                        boolean active = (j == idx);
                        pillList.get(j).getElement().getStyle()
                            .set("background", active ? "#001934" : "#FFFFFF")
                            .set("color", active ? "#FFFFFF" : "#0F172A")
                            .set("border", active ? "2px solid #001934" : "1.5px solid #CBD5E1");
                    }
                });
                pillList.add(pill);
                sizePills.add(pill);
            }
            sizeBox.add(sizeLabel, sizePills);
        }

        // --- 2. INTERACTIVE QUANTITY SELECTOR & STOCK INDICATOR ---
        int maxStock = product.getStock() != null ? Math.max(0, product.getStock()) : 1;
        int[] selectedQty = {maxStock > 0 ? 1 : 0};

        Div qtyBox = new Div();
        qtyBox.getElement().getStyle().set("display", "flex").set("align-items", "center").set("gap", "16px").set("margin-top", "4px");

        Span qtyLabel = new Span("Jumlah:");
        qtyLabel.getStyle().set("font-size", "13px").set("font-weight", "700").set("color", "#0F172A");

        HorizontalLayout qtyControl = new HorizontalLayout();
        qtyControl.setSpacing(false);
        qtyControl.setAlignItems(FlexComponent.Alignment.CENTER);
        qtyControl.getStyle()
            .set("border", "1.5px solid #CBD5E1")
            .set("border-radius", "8px")
            .set("overflow", "hidden");

        Button btnMinus = new Button("-");
        btnMinus.getStyle()
            .set("width", "36px").set("height", "36px").set("border-radius", "0")
            .set("background", "#F1F5F9").set("color", "#0F172A").set("font-weight", "800")
            .set("border", "none").set("cursor", "pointer");

        Span qtyDisplay = new Span(String.valueOf(selectedQty[0]));
        qtyDisplay.getStyle()
            .set("width", "44px").set("text-align", "center").set("font-size", "14px")
            .set("font-weight", "800").set("color", "#001934");

        Button btnPlus = new Button("+");
        btnPlus.getStyle()
            .set("width", "36px").set("height", "36px").set("border-radius", "0")
            .set("background", "#F1F5F9").set("color", "#0F172A").set("font-weight", "800")
            .set("border", "none").set("cursor", "pointer");

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
                Notification.show("Maksimal stok tercapai (" + maxStock + " buah)", 2000, Notification.Position.TOP_CENTER);
            }
        });

        qtyControl.add(btnMinus, qtyDisplay, btnPlus);

        Span stockBadge = new Span();
        if (maxStock <= 0) {
            stockBadge.setText("Stok Habis");
            stockBadge.getStyle().set("font-size", "13px").set("font-weight", "700").set("color", "#EF4444");
        } else if (maxStock <= 3) {
            stockBadge.setText("Sisa " + maxStock + " buah!");
            stockBadge.getStyle().set("font-size", "13px").set("font-weight", "700").set("color", "#DC2626");
        } else {
            stockBadge.setText("Stok: " + maxStock + " tersedia");
            stockBadge.getStyle().set("font-size", "13px").set("font-weight", "600").set("color", "#64748B");
        }

        qtyBox.add(qtyLabel, qtyControl, stockBadge);

        // --- 3. ACTION BUTTONS: KERANJANG & WISHLIST ---
        HorizontalLayout secondaryBtns = new HorizontalLayout();
        secondaryBtns.setWidthFull();
        secondaryBtns.setSpacing(true);

        Button btnCart = new Button("Keranjang", VaadinIcon.CART.create());
        btnCart.setWidth("50%");
        btnCart.getElement().getStyle()
            .set("border", "2px solid #001934").set("color", "#001934").set("background", "#FFFFFF")
            .set("border-radius", "8px").set("font-weight", "700").set("padding", "12px").set("cursor", "pointer");

        boolean isOutOfStock = maxStock <= 0;

        btnCart.addClickListener(e -> {
            if (isOutOfStock) {
                Notification.show("Produk ini sudah habis (Stok: 0)", 3000, Notification.Position.TOP_CENTER);
                return;
            }
            if (!AuthGuard.requireLogin(UI.getCurrent())) return;
            User user = AuthGuard.getCurrentUser();
            if (product.getSeller() != null && user != null && user.getId().equals(product.getSeller().getId())) {
                Notification.show("Anda tidak dapat membeli produk yang Anda jual sendiri.", 3000, Notification.Position.TOP_CENTER);
                return;
            }
            cartService.addToCart(user, product, selectedQty[0]);
            MainLayout.reloadCartBadge(UI.getCurrent());

            String msg = (isPakaian && selectedSize[0] != null)
                ? "Ditambahkan " + selectedQty[0] + " barang (Ukuran " + selectedSize[0] + ") ke Keranjang!"
                : "Ditambahkan " + selectedQty[0] + " barang ke Keranjang!";
            Notification successNotif = Notification.show(msg, 2500, Notification.Position.TOP_CENTER);
            successNotif.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        User currentViewer = AuthGuard.getCurrentUser();
        boolean initialWishlistState = false;
        if (currentViewer != null && wishlistService != null && product != null) {
            try {
                initialWishlistState = wishlistService.isWishlisted(currentViewer, product);
            } catch (Exception ignored) {}
        }

        Button btnWish = new Button(initialWishlistState ? "Disimpan" : "Wishlist",
            initialWishlistState ? VaadinIcon.HEART.create() : VaadinIcon.HEART_O.create());
        btnWish.setWidth("50%");
        btnWish.getElement().getStyle()
            .set("border", initialWishlistState ? "2px solid #DC2626" : "2px solid #001934")
            .set("color", initialWishlistState ? "#FFFFFF" : "#001934")
            .set("background", initialWishlistState ? "#DC2626" : "#FFFFFF")
            .set("border-radius", "8px").set("font-weight", "700").set("padding", "12px").set("cursor", "pointer")
            .set("transition", "all 0.2s ease");

        btnWish.addClickListener(e -> {
            if (!AuthGuard.requireLogin(UI.getCurrent())) return;
            User user = AuthGuard.getCurrentUser();
            boolean isAdded = wishlistService.toggleWishlist(user, product);

            btnWish.setText(isAdded ? "Disimpan" : "Wishlist");
            btnWish.setIcon(isAdded ? VaadinIcon.HEART.create() : VaadinIcon.HEART_O.create());
            btnWish.getElement().getStyle()
                .set("border", isAdded ? "2px solid #DC2626" : "2px solid #001934")
                .set("color", isAdded ? "#FFFFFF" : "#001934")
                .set("background", isAdded ? "#DC2626" : "#FFFFFF");

            Notification.show(isAdded ? "Ditambahkan ke Wishlist!" : "Dihapus dari Wishlist", 2000, Notification.Position.TOP_CENTER);
        });

        secondaryBtns.add(btnCart, btnWish);

        // Action Button: Beli Sekarang
        Button btnBuyNow = new Button(isOutOfStock ? "Stok Habis (0)" : "Beli Sekarang", VaadinIcon.PACKAGE.create());
        btnBuyNow.setWidthFull();
        if (isOutOfStock) {
            btnBuyNow.setEnabled(false);
            btnBuyNow.getElement().getStyle()
                .set("background", "#94A3B8").set("color", "#FFFFFF").set("font-weight", "700")
                .set("font-size", "15px").set("border-radius", "8px").set("padding", "14px").set("cursor", "not-allowed");
        } else {
            btnBuyNow.getElement().getStyle()
                .set("background", "#001934").set("color", "#FFFFFF").set("font-weight", "800")
                .set("font-size", "15px").set("border-radius", "8px").set("padding", "14px").set("cursor", "pointer");
        }
        btnBuyNow.addClickListener(e -> {
            if (isOutOfStock) {
                Notification.show("Produk ini sudah habis (Stok: 0)", 3000, Notification.Position.TOP_CENTER);
                return;
            }
            if (!AuthGuard.requireLogin(UI.getCurrent())) return;
            User user = AuthGuard.getCurrentUser();
            if (product.getSeller() != null && user != null && user.getId().equals(product.getSeller().getId())) {
                Notification.show("Anda tidak dapat membeli produk yang Anda jual sendiri.", 3000, Notification.Position.TOP_CENTER);
                return;
            }

            String mainImg = "images/buku.jpeg";
            if (product.getImages() != null && !product.getImages().isBlank()) {
                String trimmed = product.getImages().trim();
                if (trimmed.startsWith("[")) {
                    int start = trimmed.indexOf('"');
                    int end = trimmed.indexOf('"', start + 1);
                    if (start >= 0 && end > start) mainImg = trimmed.substring(start + 1, end);
                } else {
                    mainImg = trimmed;
                }
            }

            String sName = (product.getSeller() != null && product.getSeller().getFullName() != null)
                ? product.getSeller().getFullName() : "Penjual ReWear";

            CartItem directItem = new CartItem(
                "direct-" + product.getId(),
                sName,
                product.isSchoolMarket() ? "Pasar SMKN 24" : "Penjual Umum",
                product.isSchoolMarket() ? "gold" : "blue",
                product.getName(),
                (selectedSize[0] != null ? "Ukuran: " + selectedSize[0] : (product.getCategory() != null ? product.getCategory().getName() : "Standar")),
                product.getPrice() != null ? product.getPrice().doubleValue() : 0.0,
                product.getPrice() != null ? product.getPrice().doubleValue() : 0.0,
                mainImg,
                product.isSchoolMarket() ? "Eksklusif SMKN 24" : "Preloved",
                selectedQty[0],
                true,
                product.isSchoolMarket(),
                maxStock
            );
            directItem.setProductId(product.getId());

            VaadinSession session = VaadinSession.getCurrent();
            if (session != null) {
                session.setAttribute("DIRECT_CHECKOUT_ITEM", directItem);
            }
            UI.getCurrent().navigate("checkout");
        });

        // Guarantee Badges
        Div guaranteeRow = new Div();
        guaranteeRow.getElement().setProperty("innerHTML",
            "<div style='display:flex;align-items:center;justify-content:center;gap:16px;margin-top:4px;'>" +
            "<span style='display:flex;align-items:center;gap:5px;font-size:12px;color:#64748B;font-weight:600;'>" +
            "<svg width='14' height='14' viewBox='0 0 24 24' fill='none' xmlns='http://www.w3.org/2000/svg'>" +
            "<path d='M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z' stroke='#64748B' stroke-width='1.8' stroke-linecap='round' stroke-linejoin='round'/>" +
            "</svg>Escrow Protected</span>" +
            "<span style='color:#CBD5E1'>•</span>" +
            "<span style='display:flex;align-items:center;gap:5px;font-size:12px;color:#64748B;font-weight:600;'>" +
            "<svg width='14' height='14' viewBox='0 0 24 24' fill='none' xmlns='http://www.w3.org/2000/svg'>" +
            "<rect x='1' y='3' width='15' height='13' rx='2' stroke='#64748B' stroke-width='1.8'/>" +
            "<path d='M16 8h4l3 5v3h-7V8z' stroke='#64748B' stroke-width='1.8' stroke-linejoin='round'/>" +
            "<circle cx='5.5' cy='18.5' r='2.5' stroke='#64748B' stroke-width='1.8'/>" +
            "<circle cx='18.5' cy='18.5' r='2.5' stroke='#64748B' stroke-width='1.8'/>" +
            "</svg>COD SMKN 24</span></div>");

        // Report Product Button
        Button btnReportProduct = new Button("Laporkan Produk Ini", VaadinIcon.FLAG.create());
        btnReportProduct.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnReportProduct.getElement().getStyle()
            .set("color", "#EF4444")
            .set("font-size", "12px")
            .set("font-weight", "700")
            .set("cursor", "pointer")
            .set("margin-top", "6px")
            .set("align-self", "center");
        btnReportProduct.addClickListener(e -> openReportProductDialog(product));

        rightCol.add(productTitle, priceRow, ratingRow, sellerCard);

        if (isPakaian) {
            rightCol.add(sizeBox);
        }

        rightCol.add(qtyBox, secondaryBtns, btnBuyNow, guaranteeRow, btnReportProduct);
        mainGrid.add(leftGallery, rightCol);

        // ---- 4. INTERACTIVE TABS (Deskripsi & Ulasan Pembeli via Visibility Toggle) ----
        Div tabsSection = new Div();
        tabsSection.getElement().getStyle().set("margin-top", "48px");

        HorizontalLayout tabHeader = new HorizontalLayout();
        tabHeader.getElement().getStyle().set("border-bottom", "2px solid #E2E8F0").set("margin-bottom", "24px");
        tabHeader.setSpacing(true);

        Span tDesc = new Span("Deskripsi");
        tDesc.getElement().getStyle()
            .set("font-size", "16px").set("font-weight", "800").set("color", "#001934")
            .set("border-bottom", "3px solid #001934").set("padding-bottom", "8px").set("cursor", "pointer");

        Span tReview = new Span("Ulasan Pembeli (" + reviews.size() + ")");
        tReview.getElement().getStyle()
            .set("font-size", "16px").set("font-weight", "600").set("color", "#64748B")
            .set("border-bottom", "3px solid transparent").set("padding-bottom", "8px").set("cursor", "pointer");

        tabHeader.add(tDesc, tReview);

        // --- Container Tab 1: Description ---
        Div descTabContent = createDescriptionContent(product);

        // --- Container Tab 2: Reviews ---
        Div reviewsTabContent = createReviewsContent(reviews, avgRating);
        reviewsTabContent.setVisible(false); // Initially hidden

        tDesc.addClickListener(e -> {
            tDesc.getElement().getStyle().set("color", "#001934").set("font-weight", "800").set("border-bottom", "3px solid #001934");
            tReview.getElement().getStyle().set("color", "#64748B").set("font-weight", "600").set("border-bottom", "3px solid transparent");
            descTabContent.setVisible(true);
            reviewsTabContent.setVisible(false);
        });

        tReview.addClickListener(e -> {
            tReview.getElement().getStyle().set("color", "#001934").set("font-weight", "800").set("border-bottom", "3px solid #001934");
            tDesc.getElement().getStyle().set("color", "#64748B").set("font-weight", "600").set("border-bottom", "3px solid transparent");
            descTabContent.setVisible(false);
            reviewsTabContent.setVisible(true);
        });

        tabsSection.add(tabHeader, descTabContent, reviewsTabContent);
        containerWrapper.add(topNavRow, mainGrid, tabsSection);
        return containerWrapper;
    }

    private Div createDescriptionContent(Product product) {
        Div container = new Div();
        container.setWidthFull();

        HorizontalLayout tabContentGrid = new HorizontalLayout();
        tabContentGrid.setWidthFull();
        tabContentGrid.setSpacing(true);

        Div descTextCol = new Div();
        descTextCol.setWidth("62%");
        if (product.getDescription() != null && !product.getDescription().isBlank()) {
            Paragraph pDesc = new Paragraph(product.getDescription());
            pDesc.getElement().getStyle().set("color", "#475569").set("line-height", "1.7").set("font-size", "14px").set("margin-bottom", "16px");
            descTextCol.add(pDesc);
        } else {
            Paragraph pDesc = new Paragraph("Penjual tidak menyertakan deskripsi tambahan.");
            pDesc.getElement().getStyle().set("color", "#94A3B8").set("font-style", "italic").set("font-size", "14px");
            descTextCol.add(pDesc);
        }

        // Right Blue Box "Kenapa Beli Ini?"
        Div whyBuyBox = new Div();
        whyBuyBox.setWidth("38%");
        whyBuyBox.getElement().getStyle()
            .set("background", "#EFF4FF")
            .set("border-radius", "16px")
            .set("padding", "24px")
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("gap", "16px");

        H3 whyTitle = new H3("Kenapa Beli Ini?");
        whyTitle.getElement().getStyle().set("font-size", "18px").set("font-weight", "800").set("color", "#0F172A").set("margin", "0");

        String svgLeaf = "<svg width='16' height='16' viewBox='0 0 24 24' fill='none' xmlns='http://www.w3.org/2000/svg'><path d='M12 22C6 16 2 12 2 8a10 10 0 0120 0c0 4-4 8-10 14z' stroke='#16A34A' stroke-width='1.8' stroke-linecap='round' stroke-linejoin='round'/></svg>";
        String svgGrad = "<svg width='16' height='16' viewBox='0 0 24 24' fill='none' xmlns='http://www.w3.org/2000/svg'><path d='M22 10v1a10 10 0 11-5.93-9.14' stroke='#2563EB' stroke-width='1.8' stroke-linecap='round'/><polyline points='22 4 12 14.01 9 11.01' stroke='#2563EB' stroke-width='1.8' stroke-linecap='round' stroke-linejoin='round'/></svg>";
        String svgCheck = "<svg width='16' height='16' viewBox='0 0 24 24' fill='none' xmlns='http://www.w3.org/2000/svg'><circle cx='12' cy='12' r='10' stroke='#944A07' stroke-width='1.8'/><path d='M8 12l3 3 5-5' stroke='#944A07' stroke-width='1.8' stroke-linecap='round' stroke-linejoin='round'/></svg>";
        Div feat1 = createWhyFeature(svgLeaf, "Sustainable Choice", "Mengurangi limbah tekstil dengan membeli barang pre-loved berkualitas.");
        Div feat2 = createWhyFeature(svgGrad, "Support Students", "Mendukung ekosistem wirausaha siswa SMKN 24 Jakarta.");
        Div feat3 = createWhyFeature(svgCheck, "Verified Item", "Sudah melalui proses pengecekan kualitas oleh tim ReWear.");

        whyBuyBox.add(whyTitle, feat1, feat2, feat3);
        tabContentGrid.add(descTextCol, whyBuyBox);
        container.add(tabContentGrid);
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
        Span starRender = new Span(renderStars((int) Math.round(avgRating)));
        starRender.getStyle().set("font-size", "20px");
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

                Span revStars = new Span(renderStars(rev.getRating() != null ? rev.getRating() : 5));
                revStars.getStyle().set("font-size", "16px");

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

    private String renderStars(int rating) {
        return rating + " / 5";
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

        Paragraph title = new Paragraph("Produk Tidak Ditemukan");
        title.getElement().getStyle().set("font-size", "24px").set("font-weight", "700").set("color", "#001934").set("margin", "0 0 12px");

        Paragraph subtitle = new Paragraph("Produk dengan ID " + productId + " tidak ada atau sudah tidak tersedia.");
        subtitle.getElement().getStyle().set("font-size", "15px").set("color", "#64748B").set("margin", "0 0 24px");

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
}
