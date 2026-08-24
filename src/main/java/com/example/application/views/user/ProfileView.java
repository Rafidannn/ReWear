package com.example.application.views.user;

import com.example.application.model.product.Product;
import com.example.application.model.user.User;
import com.example.application.repository.moderation.ReviewRepository;
import com.example.application.repository.order.OrderRepository;
import com.example.application.service.moderation.ModerationService;
import com.example.application.service.product.ProductService;
import com.example.application.service.user.UserService;
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
import com.example.application.config.WebMvcConfig;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinSession;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.example.application.model.payment.PayoutStatus;
import com.example.application.model.payment.SellerPayout;
import com.example.application.model.user.Wishlist;
import com.example.application.service.order.CartService;
import com.example.application.service.payment.PaymentService;
import com.example.application.service.user.WishlistService;

@Route(value = "profile", layout = MainLayout.class)
@RouteAlias(value = "profil", layout = MainLayout.class)
@RouteAlias(value = "pesanan", layout = MainLayout.class)
@PageTitle("Profil Saya | ReWear SMKN 24")
public class ProfileView extends VerticalLayout implements HasUrlParameter<Long>, BeforeEnterObserver {

    private final UserService userService;
    private final ProductService productService;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final com.example.application.service.order.OrderService orderService;
    private final WishlistService wishlistService;
    private final CartService cartService;
    private final ModerationService moderationService;
    private final PaymentService paymentService;

    private User targetUser;
    private boolean isOwnProfile = true;

    // Active tab state: "profile", "orders", "wishlist", "rewearpay", "settings"
    private String activeTab = "profile";
    // Filter state for Pesanan Saya: "Semua", "Diproses", "Dikirim", "Selesai", "Komplain"
    private String orderFilter = "Semua";

    private final Div contentContainer = new Div();
    private final Div rightContentArea = new Div();

    public ProfileView(UserService userService, ProductService productService,
                       OrderRepository orderRepository, ReviewRepository reviewRepository,
                       com.example.application.service.order.OrderService orderService,
                       WishlistService wishlistService, CartService cartService,
                       ModerationService moderationService,
                       PaymentService paymentService) {
        this.userService = userService;
        this.productService = productService;
        this.orderRepository = orderRepository;
        this.reviewRepository = reviewRepository;
        this.orderService = orderService;
        this.wishlistService = wishlistService;
        this.cartService = cartService;
        this.moderationService = moderationService;
        this.paymentService = paymentService;

        setSpacing(false);
        setPadding(false);
        setWidthFull();
        getElement().getStyle()
            .set("background-color", "#F8F9FF")
            .set("min-height", "100vh")
            .set("padding", "24px 0 64px 0")
            .set("max-width", "100%")
            .set("overflow-x", "hidden");

        contentContainer.setWidthFull();
        contentContainer.addClassName("rw-profile-container");
        contentContainer.getElement().getStyle()
            .set("max-width", "1280px")
            .set("margin", "0 auto")
            .set("padding", "0 16px")
            .set("box-sizing", "border-box");

        add(contentContainer);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String path = event.getLocation().getPath();
        Map<String, List<String>> queryParams = event.getLocation().getQueryParameters().getParameters();

        if (path.contains("orders") || path.contains("pesanan")) {
            event.forwardTo("orders");
            return;
        } else if (queryParams.containsKey("tab")) {
            String tab = queryParams.get("tab").get(0);
            if ("orders".equalsIgnoreCase(tab) || "pesanan".equalsIgnoreCase(tab)) {
                event.forwardTo("orders");
                return;
            }
            else if ("profile".equalsIgnoreCase(tab) || "profil".equalsIgnoreCase(tab)) activeTab = "profile";
            else if ("products".equalsIgnoreCase(tab) || "barang".equalsIgnoreCase(tab) || "jual".equalsIgnoreCase(tab)) activeTab = "products";
            else if ("wishlist".equalsIgnoreCase(tab)) activeTab = "wishlist";
            else if ("rewearpay".equalsIgnoreCase(tab) || "pay".equalsIgnoreCase(tab)) activeTab = "rewearpay";
            else if ("settings".equalsIgnoreCase(tab) || "pengaturan".equalsIgnoreCase(tab)) activeTab = "settings";
        }

        if (targetUser != null) {
            buildMainLayout();
        }
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter Long userId) {
        User loggedInUser = VaadinSession.getCurrent() != null
            ? VaadinSession.getCurrent().getAttribute(User.class)
            : null;

        if (userId != null) {
            targetUser = userService.findByIdWithSchool(userId).orElse(null);
            isOwnProfile = (loggedInUser != null && targetUser != null && loggedInUser.getId().equals(targetUser.getId()));
        } else if (loggedInUser != null) {
            targetUser = userService.findByIdWithSchool(loggedInUser.getId()).orElse(loggedInUser);
            isOwnProfile = true;
        } else {
            event.forwardTo("login");
            return;
        }

        buildMainLayout();
    }

    private void buildMainLayout() {
        contentContainer.removeAll();

        if (targetUser == null) {
            Div notFound = new Div(new Paragraph("Pengguna tidak ditemukan atau Anda belum login."));
            notFound.getElement().getStyle().set("padding", "48px").set("text-align", "center");
            contentContainer.add(notFound);
            return;
        }

        if (!isOwnProfile) {
            Div topNav = new Div();
            topNav.getElement().getStyle().set("margin-bottom", "16px");

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
            topNav.add(btnBack);
            contentContainer.add(topNav);
        }

        // 2-Column Dashboard Grid: Left Sidebar | Right Active Tab Content
        HorizontalLayout gridLayout = new HorizontalLayout();
        gridLayout.addClassName("rw-profile-dashboard-grid");
        gridLayout.setWidthFull();
        gridLayout.setSpacing(true);

        // LEFT SIDEBAR NAVIGATION
        Div leftSidebar = createLeftSidebar();
        leftSidebar.addClassName("rw-profile-left-sidebar");

        // RIGHT CONTENT CONTAINER
        rightContentArea.setWidthFull();
        rightContentArea.addClassName("rw-profile-right-content");
        rightContentArea.getElement().getStyle().set("flex", "1");
        renderRightTabContent();

        gridLayout.add(leftSidebar, rightContentArea);
        gridLayout.expand(rightContentArea);

        contentContainer.add(gridLayout);
    }

    // ==========================================
    // LEFT SIDEBAR NAVIGATION
    // ==========================================

    private Div createLeftSidebar() {
        Div sidebar = new Div();
        sidebar.getElement().getStyle()
            .set("width", "280px")
            .set("flex-shrink", "0")
            .set("background", "#EFF4FF")
            .set("border-radius", "16px")
            .set("padding", "24px 16px")
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("gap", "20px")
            .set("box-sizing", "border-box");

        // User Avatar + Info Card
        Div userCard = new Div();
        userCard.getElement().getStyle()
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("align-items", "center")
            .set("padding-bottom", "16px")
            .set("border-bottom", "1px solid #DBEAFE");

        Component avatarComponent;
        String avatarUrl = targetUser.getAvatarUrl();
        if (avatarUrl != null && !avatarUrl.isBlank() && !avatarUrl.contains("buku.jpeg")) {
            Image img = new Image(avatarUrl, targetUser.getFullName());
            img.getElement().getStyle()
                .set("width", "72px")
                .set("height", "72px")
                .set("border-radius", "9999px")
                .set("object-fit", "cover")
                .set("border", "3px solid #F5C45E")
                .set("margin-bottom", "12px")
                .set("box-shadow", "0 4px 12px rgba(0,25,52,0.15)");
            avatarComponent = img;
        } else {
            String initial = (targetUser.getFullName() != null && !targetUser.getFullName().isEmpty())
                ? String.valueOf(targetUser.getFullName().charAt(0)).toUpperCase()
                : "R";

            Div avatarCircle = new Div();
            avatarCircle.getElement().getStyle()
                .set("width", "72px")
                .set("height", "72px")
                .set("border-radius", "9999px")
                .set("background", "#001934")
                .set("color", "#F5C45E")
                .set("font-size", "28px")
                .set("font-weight", "800")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("border", "3px solid #F5C45E")
                .set("margin-bottom", "12px")
                .set("box-shadow", "0 4px 12px rgba(0,25,52,0.15)");
            avatarCircle.setText(initial);
            avatarComponent = avatarCircle;
        }

        H4 userName = new H4(targetUser.getFullName() != null ? targetUser.getFullName() : "Profil Saya");
        userName.getElement().getStyle()
            .set("font-size", "17px")
            .set("font-weight", "800")
            .set("color", "#001934")
            .set("margin", "0 0 4px 0")
            .set("text-align", "center");

        String subRoleText = (targetUser.getSchool() != null && targetUser.getSchool().getName() != null)
            ? targetUser.getSchool().getName()
            : (targetUser.getEmail() != null ? targetUser.getEmail() : "Warga SMKN 24 Jakarta");
        Span userRole = new Span(subRoleText);
        userRole.getElement().getStyle()
            .set("font-size", "13px")
            .set("color", "#64748B")
            .set("font-weight", "600");

        userCard.add(avatarComponent, userName, userRole);

        // Sidebar Navigation Links
        Div navList = new Div();
        navList.getElement().getStyle()
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("gap", "6px");

        // Always visible tabs
        navList.add(createNavItem("Profil", "profile", VaadinIcon.USER));
        navList.add(createNavItem("Barang Dijual", "products", VaadinIcon.SHOP));

        // Only show private tabs if viewing own profile
        if (isOwnProfile) {
            navList.add(createNavItem("Pesanan Saya", "orders", VaadinIcon.CART));
            navList.add(createNavItem("Wishlist", "wishlist", VaadinIcon.HEART));
            navList.add(createNavItem("ReWear Pay", "rewearpay", VaadinIcon.CREDIT_CARD));
            navList.add(createNavItem("Pengaturan", "settings", VaadinIcon.COG));
        } else {
            // For other's profile: show Chat button instead
            Button btnChatSeller = new Button("Chat Penjual", VaadinIcon.COMMENT.create());
            btnChatSeller.getElement().getStyle()
                .set("background", "#001934").set("color", "#F5C45E")
                .set("font-weight", "700").set("border", "none")
                .set("border-radius", "10px").set("padding", "10px 16px")
                .set("cursor", "pointer").set("width", "100%")
                .set("margin-top", "8px").set("font-size", "14px");
            btnChatSeller.addClickListener(e ->
                UI.getCurrent().navigate("chat")
            );
            navList.add(btnChatSeller);
        }

        sidebar.add(userCard, navList);
        return sidebar;
    }

    private Div createNavItem(String label, String key, VaadinIcon icon) {
        Div item = new Div();
        boolean isActive = activeTab.equalsIgnoreCase(key);

        item.getElement().getStyle()
            .set("display", "flex")
            .set("align-items", "center")
            .set("gap", "12px")
            .set("padding", "12px 16px")
            .set("border-radius", "12px")
            .set("font-weight", isActive ? "700" : "600")
            .set("font-size", "14px")
            .set("cursor", "pointer")
            .set("transition", "all 0.2s ease")
            .set("background", isActive ? "#DBEAFE" : "transparent")
            .set("color", isActive ? "#001934" : "#475569")
            .set("border-left", isActive ? "4px solid #001934" : "4px solid transparent");

        Icon ic = icon.create();
        ic.getElement().getStyle().set("width", "18px").set("height", "18px");

        Span text = new Span(label);

        item.add(ic, text);

        item.addClickListener(e -> {
            if ("orders".equalsIgnoreCase(key) || "pesanan".equalsIgnoreCase(key)) {
                UI.getCurrent().navigate("orders");
                return;
            }
            activeTab = key;
            buildMainLayout();
        });

        return item;
    }

    // ==========================================
    // RIGHT TAB CONTENT SWITCHER
    // ==========================================

    private void renderRightTabContent() {
        rightContentArea.removeAll();

        Div mainCard = new Div();
        mainCard.getElement().getStyle()
            .set("background", "#FFFFFF")
            .set("border-radius", "20px")
            .set("border", "1px solid #E2E8F0")
            .set("padding", "32px")
            .set("box-shadow", "0 4px 20px rgba(0, 25, 52, 0.04)");

        switch (activeTab.toLowerCase()) {
            case "products":
            case "barang":
                mainCard.add(renderProductsTab());
                break;
            case "orders":
                if (!isOwnProfile) { mainCard.add(renderProfileInfoTab()); break; }
                mainCard.add(renderOrdersTab());
                break;
            case "profile":
                mainCard.add(renderProfileInfoTab());
                break;
            case "wishlist":
                if (!isOwnProfile) { mainCard.add(renderProfileInfoTab()); break; }
                mainCard.add(renderWishlistTab());
                break;
            case "rewearpay":
                if (!isOwnProfile) { mainCard.add(renderProfileInfoTab()); break; }
                mainCard.add(renderReWearPayTab());
                break;
            case "settings":
                if (!isOwnProfile) { mainCard.add(renderProfileInfoTab()); break; }
                mainCard.add(renderSettingsTab());
                break;
            default:
                mainCard.add(renderProfileInfoTab());
                break;
        }

        rightContentArea.add(mainCard);
    }

    // ==========================================
    // TAB 1: PESANAN SAYA (RIWAYAT PESANAN)
    // ==========================================

    private Component renderOrdersTab() {
        Div wrapper = new Div();

        // 1. Header Title & Subtitle
        H2 title = new H2("Riwayat Pesanan");
        title.getElement().getStyle()
            .set("font-size", "26px")
            .set("font-weight", "800")
            .set("color", "#001934")
            .set("margin", "0 0 6px 0");

        Paragraph subtitle = new Paragraph("Pantau status pengiriman dan keamanan dana Anda dalam sistem Escrow ReWear.");
        subtitle.getElement().getStyle()
            .set("font-size", "14px")
            .set("color", "#64748B")
            .set("margin", "0 0 24px 0");

        wrapper.add(title, subtitle);

        // 2. Filter Navigation Bar (Semua | Diproses | Dikirim | Selesai | Komplain)
        HorizontalLayout filterBar = new HorizontalLayout();
        filterBar.getElement().getStyle()
            .set("border-bottom", "1px solid #E2E8F0")
            .set("margin-bottom", "28px")
            .set("gap", "24px");

        String[] filters = {"Semua", "Diproses", "Dikirim", "Selesai", "Komplain"};
        for (String f : filters) {
            Span pill = new Span(f);
            boolean isSel = orderFilter.equalsIgnoreCase(f);
            pill.getElement().getStyle()
                .set("padding-bottom", "10px")
                .set("font-size", "14px")
                .set("font-weight", isSel ? "700" : "600")
                .set("color", isSel ? "#001934" : "#64748B")
                .set("border-bottom", isSel ? "3px solid #001934" : "3px solid transparent")
                .set("cursor", "pointer");

            pill.addClickListener(e -> {
                orderFilter = f;
                renderRightTabContent();
            });

            filterBar.add(pill);
        }

        wrapper.add(filterBar);

        // 3. Order Item Cards List matching screenshot
        Div ordersList = new Div();
        ordersList.getElement().getStyle()
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("gap", "20px");

        List<com.example.application.model.order.Order> allOrders = targetUser != null ? orderService.getBuyerOrders(targetUser) : List.of();

        List<com.example.application.model.order.Order> filteredOrders = allOrders.stream().filter(o -> {
            if ("Semua".equalsIgnoreCase(orderFilter)) return true;
            if ("Diproses".equalsIgnoreCase(orderFilter)) return o.getStatus() == com.example.application.model.order.OrderStatus.DIPROSES || o.getStatus() == com.example.application.model.order.OrderStatus.DIBAYAR || o.getStatus() == com.example.application.model.order.OrderStatus.MENUNGGU_PEMBAYARAN;
            if ("Dikirim".equalsIgnoreCase(orderFilter)) return o.getStatus() == com.example.application.model.order.OrderStatus.DIKIRIM || o.getStatus() == com.example.application.model.order.OrderStatus.DITERIMA;
            if ("Selesai".equalsIgnoreCase(orderFilter)) return o.getStatus() == com.example.application.model.order.OrderStatus.SELESAI;
            if ("Komplain".equalsIgnoreCase(orderFilter)) return o.getStatus() == com.example.application.model.order.OrderStatus.KOMPLAIN;
            return true;
        }).toList();

        if (filteredOrders.isEmpty()) {
            Div empty = new Div();
            empty.getElement().setProperty("innerHTML",
                "<div style='text-align:center;padding:48px 24px;background:#F8FAFC;border-radius:16px;border:1px dashed #CBD5E1;'>" +
                "<div style='width:48px;height:48px;margin:0 auto 12px;border-radius:12px;background:#E2E8F0;display:flex;align-items:center;justify-content:center;'>" +
                "<svg width='24' height='24' viewBox='0 0 24 24' fill='none' stroke='#64748B' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><path d='M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z'></path></svg>" +
                "</div>" +
                "<h3 style='color:#001934;font-size:18px;font-weight:700;margin:0 0 6px 0;'>Belum Ada Pesanan</h3>" +
                "<p style='color:#64748B;font-size:14px;margin:0;'>Tidak ada pesanan dalam kategori " + orderFilter + ".</p>" +
                "</div>"
            );
            ordersList.add(empty);
        } else {
            for (com.example.application.model.order.Order order : filteredOrders) {
                ordersList.add(buildRealOrderCard(order));
            }
        }

        wrapper.add(ordersList);
        return wrapper;
    }

    private Div buildRealOrderCard(com.example.application.model.order.Order order) {
        Div card = new Div();
        card.getElement().getStyle()
            .set("background", "#FFFFFF")
            .set("border", "1px solid #E2E8F0")
            .set("border-radius", "16px")
            .set("overflow", "hidden")
            .set("padding", "20px")
            .set("box-shadow", "0 2px 8px rgba(0, 25, 52, 0.03)");

        // Top Row Meta: Order Number, Date, Status
        HorizontalLayout topMeta = new HorizontalLayout();
        topMeta.setWidthFull();
        topMeta.setAlignItems(FlexComponent.Alignment.CENTER);
        topMeta.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        topMeta.getElement().getStyle().set("margin-bottom", "14px");

        Span invCode = new Span("INV / " + order.getOrderNumber());
        invCode.getElement().getStyle().set("font-size", "14px").set("color", "#001934").set("font-weight", "800");

        Span statusBadge = buildStatusBadge(order.getStatus());
        topMeta.add(invCode, statusBadge);

        // Item List
        List<com.example.application.model.order.OrderItem> items = orderService.getOrderItems(order);
        Div itemsBox = new Div();
        itemsBox.getStyle().set("margin-bottom", "14px");

        for (com.example.application.model.order.OrderItem item : items) {
            Div itemRow = new Div();
            itemRow.getStyle().set("display", "flex").set("justify-content", "space-between").set("font-size", "13px").set("margin-bottom", "6px");

            Span name = new Span("• " + item.getProductNameSnapshot() + " (x" + item.getQuantity() + ")");
            name.getStyle().set("color", "#1E293B").set("font-weight", "600");

            Span price = new Span("Rp " + String.format("%,.0f", item.getPriceSnapshot().doubleValue() * item.getQuantity()));
            price.getStyle().set("color", "#001934").set("font-weight", "700");

            itemRow.add(name, price);
            itemsBox.add(itemRow);
        }

        // Total & Action Buttons
        HorizontalLayout footer = new HorizontalLayout();
        footer.setWidthFull();
        footer.setAlignItems(FlexComponent.Alignment.CENTER);
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Div totalBox = new Div();
        Span tLbl = new Span("Total Belanja: ");
        tLbl.getStyle().set("font-size", "13px").set("color", "#64748B");

        Span tVal = new Span("Rp " + String.format("%,.0f", order.getTotalAmount() != null ? order.getTotalAmount().doubleValue() : 0));
        tVal.getStyle().set("font-size", "16px").set("font-weight", "800").set("color", "#001934");

        totalBox.add(tLbl, tVal);

        HorizontalLayout actionBtns = new HorizontalLayout();
        actionBtns.setSpacing(true);

        if (order.getStatus() == com.example.application.model.order.OrderStatus.KOMPLAIN) {
            Button btnDetailKomplain = new Button("Detail Komplain", VaadinIcon.INFO_CIRCLE.create());
            btnDetailKomplain.getStyle().set("background", "#FEF2F2").set("color", "#991B1B").set("font-weight", "700").set("font-size", "12px").set("border-radius", "8px").set("border", "1px solid #FCA5A5").set("cursor", "pointer");
            btnDetailKomplain.addClickListener(e -> openComplainDetailModal(order));
            actionBtns.add(btnDetailKomplain);
        } else if (order.getStatus() == com.example.application.model.order.OrderStatus.DIPROSES ||
            order.getStatus() == com.example.application.model.order.OrderStatus.DIBAYAR ||
            order.getStatus() == com.example.application.model.order.OrderStatus.DIKIRIM ||
            order.getStatus() == com.example.application.model.order.OrderStatus.DITERIMA) {

            Button btnKomplain = new Button("Ajukan Komplain", VaadinIcon.EXCLAMATION_CIRCLE.create());
            btnKomplain.getStyle().set("background", "#FFFFFF").set("color", "#991B1B").set("font-weight", "700").set("font-size", "12px").set("border-radius", "8px").set("border", "1px solid #FCA5A5").set("cursor", "pointer");
            btnKomplain.addClickListener(e -> openComplainDialogForOrder(order));
            actionBtns.add(btnKomplain);

            Button btnTerima = new Button("Konfirmasi Diterima", VaadinIcon.CHECK.create());
            btnTerima.getStyle().set("background", "#16A34A").set("color", "#FFFFFF").set("font-weight", "700").set("font-size", "12px").set("border-radius", "8px");
            btnTerima.addClickListener(e -> {
                orderService.updateOrderStatus(order, com.example.application.model.order.OrderStatus.SELESAI, "Dikonfirmasi diterima oleh pembeli.", targetUser);
                Notification.show("Pesanan Dikonfirmasi Diterima!", 2500, Notification.Position.TOP_CENTER);
                renderRightTabContent();
            });
            actionBtns.add(btnTerima);
        }
        if (order.getStatus() == com.example.application.model.order.OrderStatus.SELESAI) {
            User currentUser = AuthGuard.getCurrentUser();
            boolean alreadyReviewed = currentUser != null &&
                moderationService.hasReviewed(order.getId(), currentUser.getId());

            if (alreadyReviewed) {
                Span reviewedBadge = new Span("Sudah Diulas");
                reviewedBadge.getStyle().set("background", "#DCFCE7").set("color", "#166534")
                    .set("font-size", "11px").set("font-weight", "700").set("padding", "4px 8px").set("border-radius", "6px");
                actionBtns.add(reviewedBadge);
            } else {
                Button btnReview = new Button("Beri Ulasan", VaadinIcon.STAR.create());
                btnReview.getStyle().set("background", "#F59E0B").set("color", "#FFFFFF").set("font-weight", "700").set("font-size", "12px").set("border-radius", "8px").set("cursor", "pointer");
                btnReview.addClickListener(e -> openReviewModal(order, targetUser));
                actionBtns.add(btnReview);
            }
        }

        Button btnDetail = new Button("Rincian", VaadinIcon.EYE.create());
        btnDetail.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnDetail.getStyle().set("font-size", "12px");
        btnDetail.addClickListener(e -> openOrderDetailModal(order));

        actionBtns.add(btnDetail);
        footer.add(totalBox, actionBtns);

        card.add(topMeta, itemsBox, footer);
        return card;
    }

    private void openReviewModal(com.example.application.model.order.Order order, User buyer) {
        List<com.example.application.model.order.OrderItem> items = orderService.getOrderItems(order);

        Dialog d = new Dialog();
        d.setHeaderTitle("Beri Ulasan Pesanan #" + order.getOrderNumber());
        d.setWidth("480px");

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(false);

        // Star Rating
        Span starLabel = new Span("Rating Produk & Penjual:");
        starLabel.getStyle().set("font-size", "13px").set("font-weight", "700").set("color", "#001934");

        int[] selectedRating = {5};
        Span[] stars = new Span[5];
        Span ratingHint = new Span("Sangat Puas");
        ratingHint.getStyle().set("font-size", "13px").set("color", "#F59E0B").set("font-weight", "700");

        String[] ratingLabels = {"", "Sangat Buruk", "Buruk", "Cukup", "Bagus", "Sangat Puas"};

        HorizontalLayout starsRow = new HorizontalLayout();
        starsRow.setSpacing(false);
        starsRow.getStyle().set("gap", "6px");

        for (int i = 1; i <= 5; i++) {
            Span star = new Span("★");
            final int starVal = i;
            star.getStyle()
                .set("font-size", "32px")
                .set("color", i <= selectedRating[0] ? "#F59E0B" : "#CBD5E1")
                .set("cursor", "pointer")
                .set("transition", "color 0.15s ease");
            star.getElement().addEventListener("mouseover", e -> {
                for (int j = 0; j < 5; j++) {
                    stars[j].getStyle().set("color", j < starVal ? "#FBBF24" : "#CBD5E1");
                }
            });
            star.getElement().addEventListener("mouseout", e -> {
                for (int j = 0; j < 5; j++) {
                    stars[j].getStyle().set("color", j < selectedRating[0] ? "#F59E0B" : "#CBD5E1");
                }
            });
            star.getElement().addEventListener("click", e -> {
                selectedRating[0] = starVal;
                for (int j = 0; j < 5; j++) {
                    stars[j].getStyle().set("color", j < starVal ? "#F59E0B" : "#CBD5E1");
                }
                ratingHint.setText(ratingLabels[starVal]);
            });
            stars[i - 1] = star;
            starsRow.add(star);
        }

        TextArea commentArea = new TextArea("Komentar Ulasan");
        commentArea.setPlaceholder("Ceritakan pengalamanmu membeli barang ini...");
        commentArea.setWidthFull();
        commentArea.setMaxLength(500);

        layout.add(starLabel, starsRow, ratingHint, commentArea);

        Button btnCancel = new Button("Batal", e -> d.close());
        btnCancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button btnSubmit = new Button("Kirim Ulasan", e -> {
            int rating = selectedRating[0];
            String comment = commentArea.getValue();

            com.example.application.model.product.Product reviewedProduct = null;
            User seller = order.getSeller();
            if (!items.isEmpty() && items.get(0).getProduct() != null) {
                reviewedProduct = items.get(0).getProduct();
            }

            if (seller == null) {
                Notification.show("Gagal: data penjual tidak ditemukan.", 3000, Notification.Position.TOP_CENTER);
                return;
            }

            try {
                moderationService.submitReview(order, reviewedProduct, buyer, seller, rating, comment);
                Notification.show("Ulasan berhasil dikirim. Terima kasih.", 3000, Notification.Position.TOP_CENTER);
                d.close();
                renderRightTabContent(); // refresh
            } catch (Exception ex) {
                Notification.show("Gagal mengirim ulasan: " + ex.getMessage(), 3000, Notification.Position.TOP_CENTER);
            }
        });
        btnSubmit.getStyle().set("background", "#F59E0B").set("color", "#FFFFFF").set("font-weight", "700").set("border-radius", "8px");

        d.getFooter().add(btnCancel, btnSubmit);
        d.add(layout);
        d.open();
    }

    private void openOrderDetailModal(com.example.application.model.order.Order order) {
        Dialog d = new Dialog();
        d.setHeaderTitle("Rincian Pesanan #" + order.getOrderNumber());
        d.setWidth("450px");

        Div body = new Div();
        body.getStyle().set("font-size", "13px").set("color", "#334155").set("display", "flex").set("flex-direction", "column").set("gap", "8px");

        body.add(new Div(new Span("Alamat Pengiriman: "), new Span(order.getShippingAddress() != null ? order.getShippingAddress() : "-")));
        body.add(new Div(new Span("Metode Pembayaran: "), new Span(order.getPaymentMethod() != null ? order.getPaymentMethod() : "-")));
        body.add(new Div(new Span("Metode Pengiriman: "), new Span(order.getShippingMethod() != null ? order.getShippingMethod().name() : "-")));

        d.add(body);
        Button btnClose = new Button("Tutup", e -> d.close());
        d.getFooter().add(btnClose);
        d.open();
    }

    private Span buildStatusBadge(com.example.application.model.order.OrderStatus status) {
        Span badge = new Span();
        if (status == null) status = com.example.application.model.order.OrderStatus.MENUNGGU_PEMBAYARAN;
        switch (status) {
            case MENUNGGU_PEMBAYARAN -> { badge.setText("Menunggu Pembayaran"); badge.getStyle().set("background", "#FEF3C7").set("color", "#92400E"); }
            case DIBAYAR          -> { badge.setText("Dibayar");              badge.getStyle().set("background", "#DCFCE7").set("color", "#166534"); }
            case DIPROSES         -> { badge.setText("Diproses");            badge.getStyle().set("background", "#EFF6FF").set("color", "#1E40AF"); }
            case DIKIRIM          -> { badge.setText("Dikirim");             badge.getStyle().set("background", "#F0FDF4").set("color", "#15803D"); }
            case DITERIMA         -> { badge.setText("Diterima");            badge.getStyle().set("background", "#F0FDF4").set("color", "#15803D"); }
            case SELESAI          -> { badge.setText("Selesai");             badge.getStyle().set("background", "#DCFCE7").set("color", "#166534"); }
            case KOMPLAIN         -> { badge.setText("Komplain");            badge.getStyle().set("background", "#FEF2F2").set("color", "#991B1B"); }
            case DIBATALKAN       -> { badge.setText("Dibatalkan");          badge.getStyle().set("background", "#F1F5F9").set("color", "#64748B"); }
        }
        badge.getStyle().set("font-size", "12px").set("font-weight", "700").set("padding", "4px 10px").set("border-radius", "20px");
        return badge;
    }

    private Div createActiveEscrowOrderCard() {
        Div card = new Div();
        card.getElement().getStyle()
            .set("background", "#FFFFFF")
            .set("border", "1px solid #E2E8F0")
            .set("border-radius", "16px")
            .set("overflow", "hidden")
            .set("box-shadow", "0 2px 8px rgba(0, 25, 52, 0.03)");

        // Top Row Meta: Badges & Invoice
        Div topMeta = new Div();
        topMeta.getElement().getStyle()
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "space-between")
            .set("padding", "16px 20px")
            .set("border-bottom", "1px solid #F1F5F9");

        HorizontalLayout leftMeta = new HorizontalLayout();
        leftMeta.setAlignItems(FlexComponent.Alignment.CENTER);
        leftMeta.setSpacing(true);

        Span badgeSmk = new Span("Warga SMKN 24");
        badgeSmk.getElement().getStyle()
            .set("background", "#FFDEA2")
            .set("color", "#261900")
            .set("font-weight", "800")
            .set("font-size", "11px")
            .set("padding", "4px 10px")
            .set("border-radius", "6px");

        Span invCode = new Span("INV/20240320/RW/9812");
        invCode.getElement().getStyle().set("font-size", "12px").set("color", "#64748B").set("font-weight", "600");

        leftMeta.add(badgeSmk, invCode);

        // Right Escrow Badge (Dana Ditahan)
        Div escrowBadge = new Div();
        escrowBadge.getElement().setProperty("innerHTML",
            "<div style='background:#78350F;color:#FFFFFF;padding:6px 14px;border-radius:8px;font-size:12px;font-weight:700;display:flex;align-items:center;gap:6px;'>" +
            "Dana Ditahan (Escrow)" +
            "<span style='font-weight:400;font-size:10px;opacity:0.85;'>(Aman hingga pesanan selesai)</span></div>"
        );

        topMeta.add(leftMeta, escrowBadge);

        // Main Item Content (Product Image, Name, Price)
        Div body = new Div();
        body.getElement().getStyle()
            .set("padding", "20px")
            .set("display", "flex")
            .set("gap", "20px")
            .set("align-items", "center");

        Image img = new Image("images/buku.jpeg", "Blazer Almamater SMKN 24");
        img.getElement().getStyle()
            .set("width", "96px").set("height", "96px").set("border-radius", "12px").set("object-fit", "cover").set("border", "1px solid #E2E8F0");

        Div details = new Div();
        details.getElement().getStyle().set("flex", "1");

        H4 pTitle = new H4("Blazer Almamater SMKN 24 - Ukuran L");
        pTitle.getElement().getStyle().set("font-size", "17px").set("font-weight", "800").set("color", "#001934").set("margin", "0 0 6px 0");

        Span pPrice = new Span("Rp 185.000");
        pPrice.getElement().getStyle().set("font-size", "18px").set("font-weight", "800").set("color", "#001934");

        details.add(pTitle, pPrice);
        body.add(img, details);

        // Progress Line Tracker (Dibayar -> Diproses -> Dikirim -> Selesai)
        Div trackerSection = new Div();
        trackerSection.getElement().getStyle()
            .set("padding", "0 24px 20px 24px");

        Div stepsRow = new Div();
        stepsRow.getElement().setProperty("innerHTML",
            "<div style='display:flex;align-items:center;justify-content:space-between;position:relative;max-width:550px;margin:0 auto;'>" +
            "<div style='position:absolute;top:14px;left:40px;right:40px;height:3px;background:#E2E8F0;z-index:1;'></div>" +
            "<div style='position:absolute;top:14px;left:40px;width:33%;height:3px;background:#001934;z-index:2;'></div>" +

            // Step 1: Dibayar
            "<div style='z-index:3;display:flex;flex-direction:column;align-items:center;gap:6px;'>" +
            "<div style='width:28px;height:28px;border-radius:9999px;background:#001934;color:#FFF;display:flex;align-items:center;justify-content:center;font-size:12px;font-weight:700;'>✓</div>" +
            "<span style='font-size:11px;font-weight:700;color:#001934;'>Dibayar</span></div>" +

            // Step 2: Diproses
            "<div style='z-index:3;display:flex;flex-direction:column;align-items:center;gap:6px;'>" +
            "<div style='width:28px;height:28px;border-radius:9999px;background:#001934;color:#FFF;display:flex;align-items:center;justify-content:center;font-size:12px;font-weight:700;'>2</div>" +
            "<span style='font-size:11px;font-weight:700;color:#001934;'>Diproses</span></div>" +

            // Step 3: Dikirim
            "<div style='z-index:3;display:flex;flex-direction:column;align-items:center;gap:6px;'>" +
            "<div style='width:28px;height:28px;border-radius:9999px;background:#FFF;border:2px solid #CBD5E1;color:#64748B;display:flex;align-items:center;justify-content:center;font-size:12px;'>3</div>" +
            "<span style='font-size:11px;font-weight:600;color:#94A3B8;'>Dikirim</span></div>" +

            // Step 4: Selesai
            "<div style='z-index:3;display:flex;flex-direction:column;align-items:center;gap:6px;'>" +
            "<div style='width:28px;height:28px;border-radius:9999px;background:#FFF;border:2px solid #CBD5E1;color:#64748B;display:flex;align-items:center;justify-content:center;font-size:12px;'>4</div>" +
            "<span style='font-size:11px;font-weight:600;color:#94A3B8;'>Selesai</span></div>" +

            "</div>"
        );
        trackerSection.add(stepsRow);

        // Bottom Banner Footer
        Div footer = new Div();
        footer.getElement().getStyle()
            .set("background", "#EFF6FF")
            .set("padding", "14px 20px")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "space-between");

        Span updateText = new Span("Update terakhir: Penjual sedang menyiapkan paket.");
        updateText.getElement().getStyle().set("font-size", "13px").set("color", "#2563EB").set("font-weight", "600");

        HorizontalLayout btns = new HorizontalLayout();
        btns.setSpacing(true);

        Button btnChat = new Button("Chat Penjual");
        btnChat.getElement().getStyle()
            .set("background", "#E2E8F0").set("color", "#001934").set("font-weight", "700")
            .set("border-radius", "8px").set("border", "none").set("padding", "8px 16px").set("cursor", "pointer");
        btnChat.addClickListener(e -> UI.getCurrent().navigate("chat?seller=Budi+Warga+SMKN+24&product=Blazer+Almamater+SMKN+24"));

        Button btnDetail = new Button("Lihat Detail");
        btnDetail.getElement().getStyle()
            .set("background", "#001934").set("color", "#FFFFFF").set("font-weight", "700")
            .set("border-radius", "8px").set("border", "none").set("padding", "8px 16px").set("cursor", "pointer");
        btnDetail.addClickListener(e -> Notification.show("Menampilkan detail rincian pesanan INV/20240320/RW/9812"));

        btns.add(btnChat, btnDetail);
        footer.add(updateText, btns);

        card.add(topMeta, body, trackerSection, footer);
        return card;
    }

    private Div createCompletedEscrowOrderCard() {
        Div card = new Div();
        card.getElement().getStyle()
            .set("background", "#FFFFFF")
            .set("border", "1px solid #E2E8F0")
            .set("border-radius", "16px")
            .set("overflow", "hidden")
            .set("box-shadow", "0 2px 8px rgba(0, 25, 52, 0.03)");

        // Top Row Meta: Badges & Invoice
        Div topMeta = new Div();
        topMeta.getElement().getStyle()
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "space-between")
            .set("padding", "16px 20px")
            .set("border-bottom", "1px solid #F1F5F9");

        HorizontalLayout leftMeta = new HorizontalLayout();
        leftMeta.setAlignItems(FlexComponent.Alignment.CENTER);
        leftMeta.setSpacing(true);

        Span badgeCat = new Span("Katalog Umum");
        badgeCat.getElement().getStyle()
            .set("background", "#FFDEA2")
            .set("color", "#261900")
            .set("font-weight", "800")
            .set("font-size", "11px")
            .set("padding", "4px 10px")
            .set("border-radius", "6px");

        Span invCode = new Span("INV/20240318/RW/2204");
        invCode.getElement().getStyle().set("font-size", "12px").set("color", "#64748B").set("font-weight", "600");

        leftMeta.add(badgeCat, invCode);

        // Right Escrow Status Badge (Dana Dicairkan)
        Div escrowBadge = new Div();
        escrowBadge.getElement().setProperty("innerHTML",
            "<div style='background:#1E3A8A;color:#FFFFFF;padding:6px 14px;border-radius:8px;font-size:12px;font-weight:700;display:flex;align-items:center;gap:6px;'>" +
            "Dana Dicairkan" +
            "<span style='font-weight:400;font-size:10px;opacity:0.85;'>(Telah diteruskan ke penjual)</span></div>"
        );

        topMeta.add(leftMeta, escrowBadge);

        // Main Item Content (Product Image, Name, Price)
        Div body = new Div();
        body.getElement().getStyle()
            .set("padding", "20px")
            .set("display", "flex")
            .set("gap", "20px")
            .set("align-items", "center");

        Image img = new Image("images/colokan.webp", "Polo Shirt Ekstrakurikuler - Putih");
        img.getElement().getStyle()
            .set("width", "96px").set("height", "96px").set("border-radius", "12px").set("object-fit", "cover").set("border", "1px solid #E2E8F0");

        Div details = new Div();
        details.getElement().getStyle().set("flex", "1");

        H4 pTitle = new H4("Polo Shirt Ekstrakurikuler - Putih");
        pTitle.getElement().getStyle().set("font-size", "17px").set("font-weight", "800").set("color", "#001934").set("margin", "0 0 6px 0");

        Span pPrice = new Span("Rp 65.000");
        pPrice.getElement().getStyle().set("font-size", "18px").set("font-weight", "800").set("color", "#001934");

        details.add(pTitle, pPrice);
        body.add(img, details);

        // Komplain Red Warning Card Box (Matching screenshot)
        Div complainBox = new Div();
        complainBox.getElement().getStyle()
            .set("background", "#FEE2E2")
            .set("border", "1px solid #FCA5A5")
            .set("border-radius", "12px")
            .set("padding", "14px 18px")
            .set("margin", "0 20px 20px 20px")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "space-between");

        Div complainText = new Div();
        complainText.getElement().setProperty("innerHTML",
            "<div style='display:flex;align-items:center;gap:10px;color:#991B1B;font-weight:700;font-size:13px;'>" +
            "<div><span>Masalah dengan barang?</span><br/>" +
            "<span style='font-size:11px;font-weight:500;color:#B91C1C;'>Anda memiliki waktu sisa 32 jam untuk mengajukan komplain.</span></div>" +
            "</div>"
        );

        Button btnComplain = new Button("Ajukan Komplain");
        btnComplain.getElement().getStyle()
            .set("background", "#B91C1C").set("color", "#FFFFFF").set("font-weight", "700")
            .set("border-radius", "8px").set("border", "none").set("padding", "8px 14px").set("font-size", "12px").set("cursor", "pointer");
        btnComplain.addClickListener(e -> openComplainDialog());

        complainBox.add(complainText, btnComplain);

        // Bottom Footer Banner
        Div footer = new Div();
        footer.getElement().getStyle()
            .set("background", "#EFF6FF")
            .set("padding", "14px 20px")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "space-between");

        Span recText = new Span("Diterima oleh Pembeli (20 Mar 2024)");
        recText.getElement().getStyle().set("font-size", "13px").set("color", "#16A34A").set("font-weight", "700");

        Button btnReview = new Button("Beri Ulasan");
        btnReview.getElement().getStyle()
            .set("background", "#001934").set("color", "#FFFFFF").set("font-weight", "700")
            .set("border-radius", "8px").set("border", "none").set("padding", "8px 16px").set("cursor", "pointer");
        btnReview.addClickListener(e -> openReviewDialog());

        footer.add(recText, btnReview);

        card.add(topMeta, body, complainBox, footer);
        return card;
    }

    private void openComplainDialogForOrder(com.example.application.model.order.Order order) {
        if (order == null) return;
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Ajukan Komplain Pesanan #" + order.getOrderNumber());
        dialog.setWidth("500px");

        Div body = new Div();
        body.getElement().getStyle().set("display", "flex").set("flex-direction", "column").set("gap", "14px");

        Paragraph desc = new Paragraph("Sampaikan kendala barang (rusak/tidak sesuai deskripsi). Tim Escrow ReWear akan menahan pencairan dana hingga kendala terselesaikan.");
        desc.getElement().getStyle().set("font-size", "13px").set("color", "#64748B");

        ComboBox<String> reasonBox = new ComboBox<>("Kategori Kendala");
        reasonBox.setItems(
            "Barang Rusak / Cacat Fisik",
            "Barang Tidak Sesuai Foto / Deskripsi",
            "Ukuran / Varian Salah Kirim",
            "Barang Kurang / Paket Kosong",
            "Pesanan Tidak Kunjung Sampai",
            "Lainnya"
        );
        reasonBox.setValue("Barang Tidak Sesuai Foto / Deskripsi");
        reasonBox.setWidthFull();

        TextArea reasonDesc = new TextArea("Rincian Kendala yang Dialami");
        reasonDesc.setPlaceholder("Jelaskan kondisi barang secara lengkap...");
        reasonDesc.setWidthFull();

        // Photo Upload
        Span uploadLabel = new Span("Unggah Foto Bukti Fisik / Cacat Barang");
        uploadLabel.getElement().getStyle().set("font-size", "13px").set("font-weight", "700").set("color", "#001934");

        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes("image/jpeg", "image/png", "image/webp");
        upload.setMaxFileSize(5 * 1024 * 1024);

        Button uploadBtn = new Button("Pilih Foto Bukti", VaadinIcon.UPLOAD.create());
        uploadBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        uploadBtn.getElement().getStyle().set("font-weight", "700").set("color", "#0A3D7A");
        upload.setUploadButton(uploadBtn);

        String[] uploadedEvidencePath = new String[1];
        Div previewWrap = new Div();
        previewWrap.getElement().getStyle().set("display", "none").set("align-items", "center").set("gap", "10px").set("margin-top", "4px");

        Image previewImg = new Image();
        previewImg.getElement().getStyle().set("width", "60px").set("height", "60px").set("border-radius", "8px").set("object-fit", "cover").set("border", "1px solid #CBD5E1");

        Span previewText = new Span("Foto bukti terunggah");
        previewText.getElement().getStyle().set("font-size", "12px").set("color", "#16A34A").set("font-weight", "700");
        previewWrap.add(previewImg, previewText);

        upload.addSucceededListener(event -> {
            try {
                InputStream inputStream = buffer.getInputStream();
                String origName = event.getFileName();
                String ext = "";
                int dotIdx = origName.lastIndexOf('.');
                if (dotIdx > 0) ext = origName.substring(dotIdx);

                String newFileName = "return_" + System.currentTimeMillis() + ext;
                String relativePath = "images/uploads/" + newFileName;

                File uploadDir = new File(WebMvcConfig.UPLOAD_BASE_DIR);
                if (!uploadDir.exists()) uploadDir.mkdirs();

                try (FileOutputStream out = new FileOutputStream(new File(uploadDir, newFileName))) {
                    out.write(inputStream.readAllBytes());
                }

                uploadedEvidencePath[0] = relativePath;
                previewImg.setSrc(relativePath);
                previewWrap.getElement().getStyle().set("display", "flex");
                Notification.show("Foto bukti berhasil diunggah.", 2500, Notification.Position.TOP_CENTER);
            } catch (Exception ex) {
                Notification.show("Gagal menyimpan foto bukti: " + ex.getMessage(), 3000, Notification.Position.TOP_CENTER);
            }
        });

        body.add(desc, reasonBox, reasonDesc, uploadLabel, upload, previewWrap);
        dialog.add(body);

        Button btnClose = new Button("Batal", e -> dialog.close());
        Button btnSubmit = new Button("Kirim Pengajuan Komplain", e -> {
            String cat = reasonBox.getValue();
            String detail = reasonDesc.getValue();
            if (detail == null || detail.isBlank()) {
                Notification.show("Silakan isi rincian kendala yang dialami.", 2500, Notification.Position.TOP_CENTER);
                return;
            }
            try {
                String fullReason = (cat != null ? cat : "Komplain") + ": " + detail.trim();
                orderService.createOrderReturn(order, targetUser, fullReason, uploadedEvidencePath[0], order.getTotalAmount());
                dialog.close();
                Notification notif = Notification.show("Komplain berhasil diajukan! Dana Escrow ditahan untuk verifikasi Admin.", 3500, Notification.Position.TOP_CENTER);
                notif.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                renderRightTabContent();
            } catch (Exception ex) {
                Notification.show("Gagal mengajukan komplain: " + ex.getMessage(), 3000, Notification.Position.TOP_CENTER);
            }
        });
        btnSubmit.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnSubmit.getElement().getStyle().set("background", "#DC2626");

        dialog.getFooter().add(btnClose, btnSubmit);
        dialog.open();
    }

    private void openComplainDetailModal(com.example.application.model.order.Order order) {
        if (order == null) return;
        var retOpt = orderService.getReturnByOrder(order);
        var ret = retOpt.orElse(null);

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Status Komplain & Retur #" + order.getOrderNumber());
        dialog.setWidth("480px");

        Div body = new Div();
        body.getElement().getStyle().set("display", "flex").set("flex-direction", "column").set("gap", "12px").set("font-size", "13px").set("color", "#334155");

        String statusStr = ret != null ? ret.getStatus().name() : "PENDING";
        body.add(new Div(new Span("Status Peninjauan: "), new Span(statusStr.equals("PENDING") ? "Menunggu Keputusan Admin" : statusStr)));
        body.add(new Div(new Span("Alasan Komplain: "), new Span(ret != null ? ret.getReason() : "-")));
        if (ret != null && ret.getEvidenceUrl() != null && !ret.getEvidenceUrl().isBlank()) {
            body.add(new Div(new Span("Bukti Foto: "), new Span(ret.getEvidenceUrl())));
        }
        body.add(new Div(new Span("Nominal Tertahan di Escrow: "), new Span("Rp " + String.format("%,.0f", order.getTotalAmount()))));

        dialog.add(body);
        Button btnClose = new Button("Tutup", e -> dialog.close());
        dialog.getFooter().add(btnClose);
        dialog.open();
    }

    private void openComplainDialog() {
        List<com.example.application.model.order.Order> buyerOrders = orderService.getBuyerOrders(targetUser);
        if (!buyerOrders.isEmpty()) {
            openComplainDialogForOrder(buyerOrders.get(0));
        } else {
            Notification.show("Tidak ada pesanan aktif untuk diajukan komplain.", 2500, Notification.Position.TOP_CENTER);
        }
    }

    private void openReviewDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Beri Ulasan Produk");
        dialog.setWidth("450px");

        Div body = new Div();
        body.getElement().getStyle().set("display", "flex").set("flex-direction", "column").set("gap", "12px");

        Paragraph desc = new Paragraph("Bagikan pengalaman bertransaksi Anda untuk membantu siswa lain di ReWear SMKN 24.");
        desc.getElement().getStyle().set("font-size", "13px").set("color", "#64748B");

        TextField reviewField = new TextField("Ulasan Anda");
        reviewField.setPlaceholder("Barang sangat bagus, seperti baru!");
        reviewField.setWidthFull();

        body.add(desc, reviewField);
        dialog.add(body);

        Button btnClose = new Button("Batal", e -> dialog.close());
        Button btnSubmit = new Button("Kirim Ulasan", e -> {
            dialog.close();
            Notification.show("Terima kasih atas ulasan Anda!", 2500, Notification.Position.TOP_CENTER);
        });
        btnSubmit.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnSubmit.getElement().getStyle().set("background", "#001934");

        dialog.getFooter().add(btnClose, btnSubmit);
        dialog.open();
    }

    // ==========================================
    // TAB 2: PROFIL SAYA (ACCOUNT INFO)
    // ==========================================

    private Component renderProfileInfoTab() {
        Div wrapper = new Div();

        if (!isOwnProfile) {
            String sellerName = targetUser != null && targetUser.getFullName() != null ? targetUser.getFullName() : "Pengguna";
            H2 title = new H2("Profil " + sellerName);
            title.getElement().getStyle().set("font-size", "24px").set("font-weight", "800").set("color", "#001934").set("margin", "0 0 6px 0");

            Paragraph subtitle = new Paragraph("Informasi publik dan barang preloved yang dijual oleh " + sellerName);
            subtitle.getElement().getStyle().set("font-size", "14px").set("color", "#64748B").set("margin", "0 0 20px 0");

            wrapper.add(title, subtitle);

            // Bio Card
            Div bioCard = new Div();
            bioCard.getElement().getStyle()
                .set("background", "#F8FAFC")
                .set("border", "1px solid #E2E8F0")
                .set("border-radius", "16px")
                .set("padding", "24px")
                .set("margin-bottom", "24px");

            H4 bioHeader = new H4("Bio & Tentang Penjual");
            bioHeader.getElement().getStyle().set("font-size", "16px").set("font-weight", "800").set("color", "#001934").set("margin", "0 0 8px 0");

            String bioText = (targetUser != null && targetUser.getBio() != null && !targetUser.getBio().isBlank())
                ? targetUser.getBio()
                : "Pengguna ini belum menambahkan deskripsi bio.";

            Paragraph bioPara = new Paragraph(bioText);
            bioPara.getElement().getStyle().set("font-size", "14px").set("color", "#334155").set("line-height", "1.6").set("margin", "0 0 12px 0");

            Span schoolBadge = new Span(targetUser != null && targetUser.getSchool() != null ? targetUser.getSchool().getName() : "SMKN 24 Jakarta");
            schoolBadge.getElement().getStyle()
                .set("background", "#FEF3C7").set("color", "#92400E")
                .set("font-size", "12px").set("font-weight", "700")
                .set("padding", "4px 10px").set("border-radius", "6px");

            bioCard.add(bioHeader, bioPara, schoolBadge);
            wrapper.add(bioCard);

            // Products section
            wrapper.add(renderProductsTab());
            return wrapper;
        }

        // OWN PROFILE VIEW
        H2 title = new H2("Profil Saya");
        title.getElement().getStyle().set("font-size", "24px").set("font-weight", "800").set("color", "#001934").set("margin", "0 0 6px 0");

        Paragraph subtitle = new Paragraph("Kelola informasi profil Anda secara langsung dari database.");
        subtitle.getElement().getStyle().set("font-size", "14px").set("color", "#64748B").set("margin", "0 0 24px 0");

        wrapper.add(title, subtitle);

        Div formGrid = new Div();
        formGrid.getElement().getStyle()
            .set("display", "grid")
            .set("grid-template-columns", "1fr 1fr")
            .set("gap", "20px");

        String nameVal = targetUser != null && targetUser.getFullName() != null ? targetUser.getFullName() : "-";
        String emailVal = targetUser != null && targetUser.getEmail() != null ? targetUser.getEmail() : "-";
        String phoneVal = targetUser != null && targetUser.getPhone() != null ? targetUser.getPhone() : "";
        String schoolVal = (targetUser != null && targetUser.getSchool() != null && targetUser.getSchool().getName() != null)
            ? targetUser.getSchool().getName() : "SMKN 24 Jakarta";
        String roleVal = targetUser != null && targetUser.getRole() != null ? targetUser.getRole().name() : "Warga SMKN 24";

        TextField txtName = new TextField("Nama Lengkap", nameVal, "");
        txtName.setWidthFull();

        TextField txtRole = new TextField("Status / Peran", roleVal, "");
        txtRole.setReadOnly(true);
        txtRole.setWidthFull();

        TextField txtEmail = new TextField("Email", emailVal, "");
        txtEmail.setReadOnly(true);
        txtEmail.setWidthFull();

        TextField txtPhone = new TextField("Nomor HP / WhatsApp", phoneVal, "");
        txtPhone.setAllowedCharPattern("[0-9+]");
        txtPhone.setPlaceholder("08xxxxxxxxxx");
        txtPhone.setWidthFull();

        TextField txtSchool = new TextField("Sekolah Terverifikasi", schoolVal, "");
        txtSchool.setReadOnly(true);
        txtSchool.setWidthFull();

        TextField txtBio = new TextField("Bio / Deskripsi Profil", targetUser != null && targetUser.getBio() != null ? targetUser.getBio() : "", "");
        txtBio.setPlaceholder("Tuliskan sedikit tentang dirimu...");
        txtBio.setWidthFull();

        formGrid.add(txtName, txtRole, txtEmail, txtPhone, txtSchool, txtBio);

        Button btnSave = new Button("Simpan Perubahan");
        btnSave.getElement().getStyle()
            .set("background", "#001934").set("color", "#FFFFFF").set("font-weight", "700")
            .set("border-radius", "8px").set("padding", "12px 24px").set("margin-top", "24px").set("cursor", "pointer");
        btnSave.addClickListener(e -> {
            if (targetUser != null) {
                String newName = txtName.getValue() != null ? txtName.getValue().trim() : "";
                String newPhone = txtPhone.getValue() != null ? txtPhone.getValue().trim() : "";
                
                if (newName.isEmpty()) {
                    Notification.show("Nama lengkap tidak boleh kosong.", 2500, Notification.Position.TOP_CENTER);
                    return;
                }
                if (!newPhone.isEmpty() && !newPhone.matches("^[0-9+]{8,16}$")) {
                    Notification.show("Nomor HP harus berupa angka (contoh: 081234567890).", 3000, Notification.Position.TOP_CENTER);
                    return;
                }

                targetUser.setFullName(newName);
                targetUser.setPhone(newPhone);
                targetUser.setBio(txtBio.getValue() != null ? txtBio.getValue().trim() : "");
                userService.saveUser(targetUser);
                Notification.show("Informasi profil berhasil diperbarui!", 2500, Notification.Position.TOP_CENTER);
            }
        });

        wrapper.add(formGrid, btnSave);
        return wrapper;
    }

    // ==========================================
    // ==========================================
    // TAB 3: WISHLIST SAYA
    // ==========================================

    private Component renderWishlistTab() {
        Div wrapper = new Div();

        H2 title = new H2("Wishlist Saya");
        title.getElement().getStyle().set("font-size", "24px").set("font-weight", "800").set("color", "#001934").set("margin", "0 0 6px 0");

        Paragraph subtitle = new Paragraph("Daftar produk pre-loved impian yang Anda simpan.");
        subtitle.getElement().getStyle().set("font-size", "14px").set("color", "#64748B").set("margin", "0 0 24px 0");

        wrapper.add(title, subtitle);

        List<Wishlist> wishlists = List.of();
        if (targetUser != null && wishlistService != null) {
            try {
                wishlists = wishlistService.getUserWishlist(targetUser);
            } catch (Exception ex) {
                System.err.println("Error loading wishlist: " + ex.getMessage());
            }
        }

        if (wishlists.isEmpty()) {
            Div empty = new Div();
            empty.getElement().setProperty("innerHTML",
                "<div style='text-align:center;padding:56px 24px;background:#F8FAFC;border-radius:16px;border:1px dashed #CBD5E1;margin-top:12px;'>" +
                "<div style='width:48px;height:48px;margin:0 auto 16px;border-radius:12px;background:#FEE2E2;display:flex;align-items:center;justify-content:center;'>" +
                "<svg width='24' height='24' viewBox='0 0 24 24' fill='none' stroke='#DC2626' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><path d='M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z'></path></svg>" +
                "</div>" +
                "<h3 style='color:#001934;font-size:18px;font-weight:800;margin:0 0 8px 0;'>Belum Ada Produk di Wishlist</h3>" +
                "<p style='color:#64748B;font-size:14px;margin:0 0 24px 0;'>Jelajahi produk di Pasar SMKN 24 dan simpan barang favoritmu.</p>" +
                "</div>"
            );

            Button btnShop = new Button("Jelajahi Pasar SMKN 24", VaadinIcon.SHOP.create());
            btnShop.getElement().getStyle()
                .set("background", "#001934").set("color", "#F5C45E").set("font-weight", "700")
                .set("border-radius", "10px").set("border", "none").set("padding", "12px 24px").set("cursor", "pointer");
            btnShop.addClickListener(e -> UI.getCurrent().navigate("pasar-smkn24"));

            empty.add(btnShop);
            wrapper.add(empty);
        } else {
            Div grid = new Div();
            grid.addClassName("rw-products-grid");
            grid.getElement().getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(auto-fill, minmax(130px, 1fr))")
                .set("gap", "12px")
                .set("margin-top", "16px");

            for (Wishlist w : wishlists) {
                Product p = w.getProduct();
                if (p != null) {
                    grid.add(createRealWishlistCard(w, p));
                }
            }
            wrapper.add(grid);
        }
        return wrapper;
    }

    private Div createRealWishlistCard(Wishlist wishlist, Product p) {
        Div card = new Div();
        try {
            card.getElement().getStyle()
                .set("background", "#FFFFFF")
                .set("border", "1px solid #E2E8F0")
                .set("border-radius", "16px")
                .set("overflow", "hidden")
                .set("padding", "16px")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "10px")
                .set("position", "relative")
                .set("box-shadow", "0 2px 10px rgba(0,25,52,0.04)");

            String prodName = p != null && p.getName() != null ? p.getName() : "Produk Preloved";
            BigDecimal priceVal = (p != null && p.getPrice() != null) ? p.getPrice() : BigDecimal.ZERO;
            String imgUrl = p != null ? extractImgUrl(p.getImages(), "images/buku.jpeg") : "images/buku.jpeg";
            Long prodId = p != null ? p.getId() : null;

            Image img = new Image(imgUrl, prodName);
            img.getElement().getStyle()
                .set("width", "100%").set("height", "150px")
                .set("border-radius", "12px").set("object-fit", "cover")
                .set("cursor", "pointer");
            if (prodId != null) {
                img.addClickListener(e -> UI.getCurrent().navigate("product?id=" + prodId));
            }

            H5 t = new H5(prodName);
            t.getElement().getStyle()
                .set("font-size", "14px").set("font-weight", "700")
                .set("color", "#001934").set("margin", "0")
                .set("cursor", "pointer")
                .set("white-space", "nowrap").set("overflow", "hidden").set("text-overflow", "ellipsis");
            if (prodId != null) {
                t.addClickListener(e -> UI.getCurrent().navigate("product?id=" + prodId));
            }

            Span priceSpan = new Span("Rp " + String.format("%,.0f", priceVal));
            priceSpan.getElement().getStyle().set("font-size", "15px").set("font-weight", "800").set("color", "#001934");

            String sellerName = (p != null && p.getSeller() != null && p.getSeller().getFullName() != null)
                ? p.getSeller().getFullName() : "Warga SMKN 24";
            Span sellerSpan = new Span(sellerName);
            sellerSpan.getElement().getStyle().set("font-size", "12px").set("color", "#64748B");

            // Action Row
            HorizontalLayout actions = new HorizontalLayout();
            actions.setWidthFull();
            actions.setSpacing(true);
            actions.getElement().getStyle().set("gap", "8px").set("margin-top", "4px");

            Button btnCart = new Button("Keranjang", VaadinIcon.CART.create());
            btnCart.setWidth("75%");
            btnCart.getElement().getStyle()
                .set("background", "#001934").set("color", "#F5C45E").set("font-weight", "700")
                .set("font-size", "12px").set("border-radius", "8px").set("border", "none")
                .set("padding", "8px").set("cursor", "pointer");
            btnCart.addClickListener(e -> {
                User user = AuthGuard.getCurrentUser();
                if (user != null && p != null) {
                    cartService.addToCart(user, p, 1);
                    MainLayout.reloadCartBadge(UI.getCurrent());
                    Notification.show("Ditambahkan ke keranjang.", 2000, Notification.Position.TOP_CENTER);
                }
            });

            Button btnDelete = new Button(VaadinIcon.TRASH.create());
            btnDelete.setWidth("25%");
            btnDelete.getElement().getStyle()
                .set("background", "#FEF2F2").set("color", "#DC2626").set("font-weight", "700")
                .set("border-radius", "8px").set("border", "1px solid #FCA5A5")
                .set("padding", "8px").set("cursor", "pointer");
            btnDelete.addClickListener(e -> {
                User user = AuthGuard.getCurrentUser();
                if (user != null && p != null) {
                    wishlistService.removeFromWishlist(user, p);
                    buildMainLayout();
                    Notification.show("Dihapus dari Wishlist.", 2000, Notification.Position.TOP_CENTER);
                }
            });

            actions.add(btnCart, btnDelete);
            card.add(img, t, priceSpan, sellerSpan, actions);
        } catch (Exception ex) {
            System.err.println("Error building wishlist card: " + ex.getMessage());
        }
        return card;
    }

    // ==========================================
    // TAB 4: REWEAR PAY
    // ==========================================

    private Component renderReWearPayTab() {
        Div wrapper = new Div();
        wrapper.getElement().getStyle().set("display", "flex").set("flex-direction", "column").set("gap", "20px");

        H2 title = new H2("ReWear Pay");
        title.getElement().getStyle().set("font-size", "24px").set("font-weight", "800").set("color", "#001934").set("margin", "0 0 6px 0");

        Paragraph subtitle = new Paragraph("Pusat kendali saldo transaksi aman, penampungan escrow, dan penarikan dana ke rekening bank / e-wallet.");
        subtitle.getElement().getStyle().set("font-size", "14px").set("color", "#64748B").set("margin", "0 0 20px 0");

        wrapper.add(title, subtitle);

        BigDecimal saldoReWearPay = paymentService != null ? paymentService.getAvailableBalance(targetUser) : BigDecimal.ZERO;
        BigDecimal saldoEscrowTerikat = paymentService != null ? paymentService.getEscrowBalance(targetUser) : BigDecimal.ZERO;

        // Saldo Banner Hero Card
        Div cardHero = new Div();
        cardHero.getElement().getStyle()
            .set("background", "linear-gradient(135deg, #001934 0%, #002B5B 100%)")
            .set("border-radius", "18px")
            .set("padding", "28px")
            .set("color", "#FFFFFF")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "space-between")
            .set("box-shadow", "0 4px 14px rgba(0,25,52,0.12)");

        Div salLeft = new Div();
        Span lbl = new Span("Saldo ReWear Pay Aktif (Dapat Ditarik)");
        lbl.getElement().getStyle().set("font-size", "13px").set("opacity", "0.85").set("display", "block").set("margin-bottom", "4px");

        H3 val = new H3("Rp " + String.format("%,.0f", saldoReWearPay.doubleValue()));
        val.getElement().getStyle().set("font-size", "32px").set("font-weight", "800").set("margin", "0 0 8px 0").set("color", "#F5C45E");

        Span escrowTxt = new Span("Dana Escrow Terikat Pesanan: Rp " + String.format("%,.0f", saldoEscrowTerikat.doubleValue()));
        escrowTxt.getElement().getStyle().set("font-size", "12px").set("opacity", "0.9").set("color", "#93C5FD");

        salLeft.add(lbl, val, escrowTxt);

        HorizontalLayout btnGroup = new HorizontalLayout();
        btnGroup.setSpacing(true);

        Button btnTopUp = new Button("Isi Saldo (Top-Up)", VaadinIcon.PLUS_CIRCLE.create());
        btnTopUp.getElement().getStyle()
            .set("background", "#2563EB").set("color", "#FFFFFF").set("font-weight", "800")
            .set("border-radius", "8px").set("padding", "12px 20px").set("border", "none").set("cursor", "pointer");
        btnTopUp.addClickListener(e -> openTopUpDialog());

        Button btnWithdraw = new Button("Tarik Saldo", VaadinIcon.MONEY_WITHDRAW.create());
        btnWithdraw.getElement().getStyle()
            .set("background", "#F5C45E").set("color", "#001934").set("font-weight", "800")
            .set("border-radius", "8px").set("padding", "12px 20px").set("border", "none").set("cursor", "pointer")
            .set("box-shadow", "0 2px 8px rgba(245, 196, 94, 0.35)");
        btnWithdraw.addClickListener(e -> openWithdrawDialog(saldoReWearPay.doubleValue()));

        btnGroup.add(btnTopUp, btnWithdraw);
        cardHero.add(salLeft, btnGroup);
        wrapper.add(cardHero);

        // Riwayat Penarikan Dana
        List<SellerPayout> payouts = paymentService != null ? paymentService.getSellerPayouts(targetUser) : List.of();

        Div historyCard = new Div();
        historyCard.getElement().getStyle()
            .set("background", "#FFFFFF")
            .set("border", "1px solid #E2E8F0")
            .set("border-radius", "16px")
            .set("padding", "20px")
            .set("box-shadow", "0 2px 6px rgba(0,25,52,0.03)");

        H3 hTitle = new H3("Riwayat Penarikan Dana (" + payouts.size() + ")");
        hTitle.getElement().getStyle().set("font-size", "16px").set("font-weight", "800").set("color", "#001934").set("margin", "0 0 16px 0");
        historyCard.add(hTitle);

        if (payouts.isEmpty()) {
            Div emptyState = new Div();
            emptyState.getElement().getStyle().set("text-align", "center").set("padding", "32px 0").set("color", "#94A3B8");
            emptyState.add(new Paragraph("Belum ada riwayat penarikan dana."));
            historyCard.add(emptyState);
        } else {
            Div table = new Div();
            table.getElement().getStyle().set("display", "flex").set("flex-direction", "column").set("gap", "12px");

            for (SellerPayout p : payouts) {
                HorizontalLayout row = new HorizontalLayout();
                row.setWidthFull();
                row.setAlignItems(FlexComponent.Alignment.CENTER);
                row.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
                row.getElement().getStyle()
                    .set("padding", "14px 16px")
                    .set("background", "#F8FAFC")
                    .set("border-radius", "10px")
                    .set("border", "1px solid #E2E8F0");

                Div leftInfo = new Div();
                Span refNo = new Span(p.getReferenceNumber() != null ? p.getReferenceNumber() : "WD-RW");
                refNo.getElement().getStyle().set("font-weight", "700").set("color", "#001934").set("font-size", "13px");

                String bankInfo = p.getBankAccount() != null
                    ? (p.getBankAccount().getBankName() + " • " + p.getBankAccount().getAccountNumber() + " (a.n " + p.getBankAccount().getAccountHolderName() + ")")
                    : "Transfer Bank";
                Paragraph pSub = new Paragraph(bankInfo + " • " + (p.getCreatedAt() != null ? p.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")) : "-"));
                pSub.getElement().getStyle().set("font-size", "12px").set("color", "#64748B").set("margin", "2px 0 0 0");
                leftInfo.add(refNo, pSub);

                HorizontalLayout rightStatus = new HorizontalLayout();
                rightStatus.setAlignItems(FlexComponent.Alignment.CENTER);
                rightStatus.setSpacing(true);

                Span amt = new Span("Rp " + String.format("%,.0f", p.getAmount() != null ? p.getAmount().doubleValue() : 0));
                amt.getElement().getStyle().set("font-weight", "800").set("color", "#001934").set("font-size", "14px");

                Span statusBadge = new Span();
                statusBadge.getElement().getStyle().set("font-size", "11px").set("font-weight", "700").set("padding", "4px 8px").set("border-radius", "6px");

                if (p.getStatus() == PayoutStatus.COMPLETED) {
                    statusBadge.setText("Berhasil");
                    statusBadge.getElement().getStyle().set("background", "#DCFCE7").set("color", "#15803D");
                } else if (p.getStatus() == PayoutStatus.REJECTED) {
                    statusBadge.setText("Ditolak");
                    statusBadge.getElement().getStyle().set("background", "#FEE2E2").set("color", "#DC2626");
                } else {
                    statusBadge.setText("Menunggu Persetujuan");
                    statusBadge.getElement().getStyle().set("background", "#FEF3C7").set("color", "#92400E");
                }

                rightStatus.add(amt, statusBadge);
                row.add(leftInfo, rightStatus);
                table.add(row);
            }
            historyCard.add(table);
        }

        wrapper.add(historyCard);
        return wrapper;
    }

    private void openWithdrawDialog(double maxSaldo) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Penarikan Dana ReWear Pay");
        dialog.setWidth("440px");

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(false);

        Span saldoInfo = new Span("Saldo tersedia: Rp " + String.format("%,.0f", maxSaldo));
        saldoInfo.getElement().getStyle()
            .set("font-size", "13px")
            .set("font-weight", "700")
            .set("color", "#001934")
            .set("background", "#FEF3C7")
            .set("padding", "8px 14px")
            .set("border-radius", "8px")
            .set("width", "100%")
            .set("box-sizing", "border-box");

        ComboBox<String> bankCombo = new ComboBox<>("Tujuan Transfer (Bank / E-Wallet)");
        bankCombo.setItems("Bank BCA", "Bank Mandiri", "Bank BRI", "Bank BNI", "GoPay", "OVO", "DANA", "ShopeePay");
        bankCombo.setValue("Bank BCA");
        bankCombo.setWidthFull();

        TextField accNumField = new TextField("Nomor Rekening / HP E-Wallet");
        accNumField.setPlaceholder("Contoh: 1234567890 atau 08123456789");
        accNumField.setWidthFull();

        TextField accHolderField = new TextField("Nama Pemilik Rekening / Akun");
        accHolderField.setValue(targetUser != null && targetUser.getFullName() != null ? targetUser.getFullName() : "");
        accHolderField.setWidthFull();

        TextField amountField = new TextField("Nominal Penarikan (Rp)");
        amountField.setValue(String.format("%.0f", maxSaldo > 0 ? maxSaldo : 10000));
        amountField.setWidthFull();

        layout.add(saldoInfo, bankCombo, accNumField, accHolderField, amountField);

        Button btnCancel = new Button("Batal", e -> dialog.close());
        btnCancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button btnSubmit = new Button("Konfirmasi Penarikan", e -> {
            String bank = bankCombo.getValue();
            String accNum = accNumField.getValue() != null ? accNumField.getValue().trim() : "";
            String accHolder = accHolderField.getValue() != null ? accHolderField.getValue().trim() : "";
            String amountStr = amountField.getValue() != null ? amountField.getValue().trim() : "0";

            if (accNum.isEmpty()) {
                Notification.show("Harap isi nomor rekening / e-wallet!", 3000, Notification.Position.TOP_CENTER);
                return;
            }
            if (accHolder.isEmpty()) {
                Notification.show("Harap isi nama pemilik rekening!", 3000, Notification.Position.TOP_CENTER);
                return;
            }

            double amount = 0;
            try {
                amount = Double.parseDouble(amountStr.replaceAll("[^0-9]", ""));
            } catch (Exception ignored) {}

            if (amount < 10000) {
                Notification.show("Minimal penarikan dana adalah Rp 10.000", 3000, Notification.Position.TOP_CENTER);
                return;
            }
            if (amount > maxSaldo) {
                Notification.show("Nominal penarikan melebihi saldo aktif Anda!", 3000, Notification.Position.TOP_CENTER);
                return;
            }

            try {
                if (paymentService != null && targetUser != null) {
                    paymentService.requestPayout(targetUser, bank, accNum, accHolder, BigDecimal.valueOf(amount));
                }
                dialog.close();
                Notification.show("Permohonan penarikan dana sebesar Rp " + String.format("%,.0f", amount) + " berhasil diajukan.", 3500, Notification.Position.TOP_CENTER);
                buildMainLayout();
            } catch (Exception ex) {
                Notification.show("Gagal mengajukan penarikan: " + ex.getMessage(), 3500, Notification.Position.TOP_CENTER);
            }
        });
        btnSubmit.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnSubmit.getElement().getStyle().set("background", "#001934").set("color", "#FFFFFF").set("font-weight", "700");

        dialog.add(layout);
        dialog.getFooter().add(btnCancel, btnSubmit);
        dialog.open();
    }

    private void openTopUpDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Isi Saldo ReWear Pay (Top-Up)");
        dialog.setWidth("440px");

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(false);

        Paragraph desc = new Paragraph("Pilih nominal saldo yang ingin Anda tambahkan ke dompet ReWear Pay Anda:");
        desc.getStyle().set("font-size", "13px").set("color", "#475569").set("margin", "0 0 8px 0");

        TextField amountField = new TextField("Nominal Top-Up (Rp)");
        amountField.setValue("50000");
        amountField.setWidthFull();

        HorizontalLayout quickPills = new HorizontalLayout();
        quickPills.setSpacing(true);
        String[] presets = {"20000", "50000", "100000", "200000"};
        for (String p : presets) {
            Button pBtn = new Button("Rp " + String.format("%,d", Integer.parseInt(p)), e -> amountField.setValue(p));
            pBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            pBtn.getStyle().set("font-size", "11px").set("font-weight", "700").set("background", "#F1F5F9").set("color", "#001934");
            quickPills.add(pBtn);
        }

        ComboBox<String> methodCombo = new ComboBox<>("Metode Pembayaran");
        methodCombo.setItems("QRIS Instan SMKN 24", "Transfer Bank BCA", "Transfer Bank Mandiri", "GoPay / ShopeePay");
        methodCombo.setValue("QRIS Instan SMKN 24");
        methodCombo.setWidthFull();

        layout.add(desc, amountField, quickPills, methodCombo);

        Button btnCancel = new Button("Batal", e -> dialog.close());
        btnCancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button btnConfirm = new Button("Konfirmasi Isi Saldo", VaadinIcon.CHECK_CIRCLE.create(), e -> {
            try {
                double val = Double.parseDouble(amountField.getValue().replaceAll("[^0-9]", ""));
                if (val < 10000) {
                    Notification.show("Minimal top-up saldo adalah Rp 10.000", 2500, Notification.Position.TOP_CENTER);
                    return;
                }
                User current = AuthGuard.getCurrentUser();
                if (current != null) {
                    BigDecimal curBal = current.getBalance() != null ? current.getBalance() : BigDecimal.ZERO;
                    current.setBalance(curBal.add(BigDecimal.valueOf(val)));
                    userService.saveUser(current);
                    Notification notif = Notification.show("Top-Up Berhasil! Saldo sebesar Rp " + String.format("%,.0f", val) + " telah ditambahkan ke ReWear Pay.", 3500, Notification.Position.TOP_CENTER);
                    notif.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    dialog.close();
                    buildMainLayout();
                }
            } catch (Exception ex) {
                Notification.show("Nominal tidak valid: " + ex.getMessage(), 2500, Notification.Position.TOP_CENTER);
            }
        });
        btnConfirm.getStyle().set("background", "#2563EB").set("color", "#FFFFFF").set("font-weight", "700");

        dialog.add(layout);
        dialog.getFooter().add(btnCancel, btnConfirm);
        dialog.open();
    }

    // ==========================================
    // TAB 5: PENGATURAN AKUN
    // ==========================================

    private Component renderSettingsTab() {
        Div wrapper = new Div();
        wrapper.getElement().getStyle().set("display", "flex").set("flex-direction", "column").set("gap", "32px");

        // Header
        Div headerBox = new Div();
        H2 title = new H2("Pengaturan Akun & Profil");
        title.getElement().getStyle().set("font-size", "24px").set("font-weight", "800").set("color", "#001934").set("margin", "0 0 6px 0");
        Paragraph subtitle = new Paragraph("Perbarui informasi profil publik, kontak, dan keamanan akun Anda.");
        subtitle.getElement().getStyle().set("font-size", "14px").set("color", "#64748B").set("margin", "0");
        headerBox.add(title, subtitle);
        wrapper.add(headerBox);

        // Card 1: Informasi Profil
        Div profileCard = new Div();
        profileCard.getElement().getStyle()
            .set("background", "#F8FAFC")
            .set("border", "1px solid #E2E8F0")
            .set("border-radius", "16px")
            .set("padding", "24px")
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("gap", "18px");

        H3 sec1Title = new H3("Informasi Profil");
        sec1Title.getElement().getStyle().set("font-size", "18px").set("font-weight", "800").set("color", "#001934").set("margin", "0 0 4px 0");

        TextField nameField = new TextField("Nama Lengkap");
        nameField.setValue(targetUser != null && targetUser.getFullName() != null ? targetUser.getFullName() : "");
        nameField.setWidthFull();

        TextField emailField = new TextField("Alamat Email (Akun Utama)");
        emailField.setValue(targetUser != null && targetUser.getEmail() != null ? targetUser.getEmail() : "");
        emailField.setReadOnly(true);
        emailField.setHelperText("Email akun terverifikasi dan tidak dapat diubah.");
        emailField.setWidthFull();

        TextField phoneField = new TextField("Nomor Telepon / WhatsApp");
        phoneField.setValue(targetUser != null && targetUser.getPhone() != null ? targetUser.getPhone() : "");
        phoneField.setPlaceholder("Contoh: 081234567890");
        phoneField.setAllowedCharPattern("[0-9+]");
        phoneField.setWidthFull();

        TextField avatarField = new TextField("URL Foto Profil (Avatar)");
        avatarField.setValue(targetUser != null && targetUser.getAvatarUrl() != null ? targetUser.getAvatarUrl() : "");
        avatarField.setPlaceholder("Contoh: images/foto-profil.jpg atau https://...");
        avatarField.setWidthFull();

        TextArea bioField = new TextArea("Bio / Deskripsi Profil");
        bioField.setValue(targetUser != null && targetUser.getBio() != null ? targetUser.getBio() : "");
        bioField.setPlaceholder("Ceritakan tentang dirimu, kelas/jurusan di SMKN 24, atau barang yang sering kamu jual...");
        bioField.setWidthFull();
        bioField.setMaxLength(300);

        Button btnSaveProfile = new Button("Simpan Perubahan Profil", VaadinIcon.CHECK.create());
        btnSaveProfile.getElement().getStyle()
            .set("background", "#001934").set("color", "#F5C45E").set("font-weight", "800")
            .set("border-radius", "10px").set("padding", "12px 24px").set("border", "none")
            .set("cursor", "pointer").set("width", "fit-content");

        btnSaveProfile.addClickListener(e -> {
            if (targetUser == null) return;
            String newName = nameField.getValue() != null ? nameField.getValue().trim() : "";
            String newPhone = phoneField.getValue() != null ? phoneField.getValue().trim() : "";

            if (newName.isEmpty()) {
                Notification.show("Nama lengkap tidak boleh kosong.", 3000, Notification.Position.TOP_CENTER);
                return;
            }
            if (!newPhone.isEmpty() && !newPhone.matches("^[0-9+]{8,16}$")) {
                Notification.show("Nomor HP harus berupa angka (contoh: 081234567890).", 3000, Notification.Position.TOP_CENTER);
                return;
            }

            targetUser.setFullName(newName);
            targetUser.setPhone(newPhone);
            targetUser.setAvatarUrl(avatarField.getValue() != null ? avatarField.getValue().trim() : "");
            targetUser.setBio(bioField.getValue() != null ? bioField.getValue().trim() : "");

            User saved = userService.saveUser(targetUser);
            targetUser = saved;
            VaadinSession.getCurrent().setAttribute(User.class, saved);

            Notification.show("Profil berhasil diperbarui.", 3000, Notification.Position.TOP_CENTER);
            buildMainLayout();
        });

        profileCard.add(sec1Title, nameField, emailField, phoneField, avatarField, bioField, btnSaveProfile);
        wrapper.add(profileCard);

        // Card 2: Keamanan & Kata Sandi
        Div passwordCard = new Div();
        passwordCard.getElement().getStyle()
            .set("background", "#F8FAFC")
            .set("border", "1px solid #E2E8F0")
            .set("border-radius", "16px")
            .set("padding", "24px")
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("gap", "18px");

        H3 sec2Title = new H3("Keamanan Kata Sandi");
        sec2Title.getElement().getStyle().set("font-size", "18px").set("font-weight", "800").set("color", "#001934").set("margin", "0 0 4px 0");

        PasswordField oldPass = new PasswordField("Kata Sandi Saat Ini");
        oldPass.setWidthFull();

        PasswordField newPass = new PasswordField("Kata Sandi Baru");
        newPass.setHelperText("Minimal 6 karakter.");
        newPass.setWidthFull();

        PasswordField confirmPass = new PasswordField("Konfirmasi Kata Sandi Baru");
        confirmPass.setWidthFull();

        Button btnChangePass = new Button("Perbarui Kata Sandi", VaadinIcon.KEY.create());
        btnChangePass.getElement().getStyle()
            .set("background", "#1E293B").set("color", "#FFFFFF").set("font-weight", "700")
            .set("border-radius", "10px").set("padding", "12px 24px").set("border", "none")
            .set("cursor", "pointer").set("width", "fit-content");

        btnChangePass.addClickListener(e -> {
            if (targetUser == null) return;
            String oldVal = oldPass.getValue();
            String newVal = newPass.getValue();
            String confirmVal = confirmPass.getValue();

            if (oldVal == null || oldVal.isBlank()) {
                Notification.show("Harap masukkan kata sandi lama Anda.", 3000, Notification.Position.TOP_CENTER);
                return;
            }
            if (targetUser.getPasswordHash() != null && !oldVal.equals(targetUser.getPasswordHash())) {
                Notification.show("Kata sandi lama yang Anda masukkan salah.", 3000, Notification.Position.TOP_CENTER);
                return;
            }
            if (newVal == null || newVal.length() < 6) {
                Notification.show("Kata sandi baru minimal 6 karakter.", 3000, Notification.Position.TOP_CENTER);
                return;
            }
            if (!newVal.equals(confirmVal)) {
                Notification.show("Konfirmasi kata sandi baru tidak cocok.", 3000, Notification.Position.TOP_CENTER);
                return;
            }

            targetUser.setPasswordHash(newVal);
            User saved = userService.saveUser(targetUser);
            targetUser = saved;
            VaadinSession.getCurrent().setAttribute(User.class, saved);

            oldPass.clear();
            newPass.clear();
            confirmPass.clear();

            Notification.show("Kata sandi akun berhasil diperbarui.", 3000, Notification.Position.TOP_CENTER);
        });

        passwordCard.add(sec2Title, oldPass, newPass, confirmPass, btnChangePass);
        wrapper.add(passwordCard);

        return wrapper;
    }

    // ==========================================
    // TAB BARANG DIJUAL (PRODUK TOKO PENJUAL)
    // ==========================================

    private Component renderProductsTab() {
        Div wrapper = new Div();

        String sellerName = targetUser != null && targetUser.getFullName() != null ? targetUser.getFullName() : "Pengguna";
        H2 title = new H2("Barang yang Dijual oleh " + sellerName);
        title.getElement().getStyle().set("font-size", "24px").set("font-weight", "800").set("color", "#001934").set("margin", "0 0 6px 0");

        Paragraph sub = new Paragraph("Daftar produk thrifting & barang preloved yang diunggah oleh penjual ini.");
        sub.getElement().getStyle().set("font-size", "14px").set("color", "#64748B").set("margin", "0 0 24px 0");

        wrapper.add(title, sub);

        List<Product> products = targetUser != null ? productService.findProductsBySeller(targetUser) : List.of();
        if (products.isEmpty()) {
            Div empty = new Div();
            empty.getElement().setProperty("innerHTML",
                "<div style='text-align:center;padding:48px 20px;background:#F8FAFC;border-radius:16px;border:1px dashed #CBD5E1;margin-top:16px;'>" +
                "<div style='width:48px;height:48px;margin:0 auto 12px;border-radius:12px;background:#E2E8F0;display:flex;align-items:center;justify-content:center;'>" +
                "<svg width='24' height='24' viewBox='0 0 24 24' fill='none' stroke='#64748B' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><path d='M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z'></path><line x1='7' y1='7' x2='7.01' y2='7'></line></svg>" +
                "</div>" +
                "<h3 style='color:#001934;margin:0 0 8px 0;font-size:18px;font-weight:700;'>Belum Ada Barang yang Dijual</h3>" +
                "<p style='color:#64748B;margin:0 0 20px 0;font-size:14px;'>" +
                (isOwnProfile ? "Kamu belum mengunggah produk barang preloved. Yuk mulai jualan!" : "Penjual ini belum mengunggah barang preloved.") +
                "</p>" +
                "</div>"
            );
            if (isOwnProfile) {
                Button btnJual = new Button("Mulai Jual Barang", VaadinIcon.PLUS.create());
                btnJual.getStyle().set("background", "#001934").set("color", "#F5C45E").set("font-weight", "800").set("border-radius", "8px").set("margin-top", "16px");
                btnJual.addClickListener(e -> UI.getCurrent().navigate("sell"));
                empty.add(btnJual);
            }
            wrapper.add(empty);
        } else {
            Div grid = new Div();
            grid.addClassName("rw-products-grid");
            grid.getElement().getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(auto-fill, minmax(130px, 1fr))")
                .set("gap", "12px")
                .set("margin-top", "16px");

            for (Product p : products) {
                grid.add(createProductCard(p));
            }
            wrapper.add(grid);
        }

        return wrapper;
    }

    private Div createProductCard(Product p) {
        Div card = new Div();
        card.getElement().getStyle()
            .set("background", "#FFFFFF")
            .set("border", "1px solid #E2E8F0")
            .set("border-radius", "12px")
            .set("overflow", "hidden")
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("cursor", "pointer");

        card.addClickListener(e -> UI.getCurrent().navigate("product/" + p.getId()));

        String imgUrl = "images/placeholder.jpg";
        if (p.getImages() != null && !p.getImages().isBlank()) {
            String trimmed = p.getImages().trim();
            if (trimmed.startsWith("[")) {
                int start = trimmed.indexOf('"');
                int end = trimmed.indexOf('"', start + 1);
                if (start >= 0 && end > start) imgUrl = trimmed.substring(start + 1, end);
            } else {
                imgUrl = trimmed;
            }
        }

        Image img = new Image(imgUrl, p.getName());
        img.addClassName("rw-product-card-img");
        img.getElement().getStyle().set("width", "100%").set("height", "130px").set("object-fit", "cover");

        Div infoBox = new Div();
        infoBox.getElement().getStyle().set("padding", "10px").set("display", "flex").set("flex-direction", "column").set("gap", "4px");

        if (p.isSchoolMarket()) {
            Span badge = new Span("WARGA SMKN 24");
            badge.getStyle().set("font-size", "9px").set("font-weight", "800").set("background", "#FEF3C7").set("color", "#92400E").set("padding", "2px 6px").set("border-radius", "4px").set("width", "fit-content");
            infoBox.add(badge);
        }

        Span title = new Span(p.getName());
        title.getElement().getStyle().set("font-size", "13px").set("font-weight", "700").set("color", "#001934").set("white-space", "nowrap").set("overflow", "hidden").set("text-overflow", "ellipsis");

        Span price = new Span("Rp " + String.format("%,.0f", p.getPrice() != null ? p.getPrice() : 0));
        price.getElement().getStyle().set("font-size", "14px").set("font-weight", "800").set("color", "#001934");

        infoBox.add(title, price);
        card.add(img, infoBox);
        return card;
    }

    private String extractImgUrl(String imagesJson, String fallback) {
        if (imagesJson == null || imagesJson.isBlank()) return fallback;
        String clean = imagesJson.replace("[", "").replace("]", "").replace("\"", "").replace("'", "").replace("\\", "").trim();
        if (clean.isEmpty()) return fallback;
        String first = clean.split(",")[0].trim();
        if (first.isEmpty()) return fallback;
        return first.startsWith("/") ? first : "/" + first;
    }
}
