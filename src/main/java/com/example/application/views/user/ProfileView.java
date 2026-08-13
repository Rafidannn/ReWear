package com.example.application.views.user;

import com.example.application.model.product.Product;
import com.example.application.model.user.User;
import com.example.application.repository.moderation.ReviewRepository;
import com.example.application.repository.order.OrderRepository;
import com.example.application.service.product.ProductService;
import com.example.application.service.user.UserService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinSession;

import java.util.List;
import java.util.Map;

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
                       com.example.application.service.order.OrderService orderService) {
        this.userService = userService;
        this.productService = productService;
        this.orderRepository = orderRepository;
        this.reviewRepository = reviewRepository;
        this.orderService = orderService;

        setSpacing(false);
        setPadding(false);
        setWidthFull();
        getElement().getStyle()
            .set("background-color", "#F8F9FF")
            .set("min-height", "100vh")
            .set("padding", "24px 0 64px 0");

        contentContainer.setWidthFull();
        contentContainer.getElement().getStyle()
            .set("max-width", "1280px")
            .set("margin", "0 auto")
            .set("padding", "0 32px")
            .set("box-sizing", "border-box");

        add(contentContainer);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String path = event.getLocation().getPath();
        Map<String, List<String>> queryParams = event.getLocation().getQueryParameters().getParameters();

        if (path.contains("orders") || path.contains("pesanan")) {
            activeTab = "orders";
        } else if (queryParams.containsKey("tab")) {
            String tab = queryParams.get("tab").get(0);
            if ("orders".equalsIgnoreCase(tab) || "pesanan".equalsIgnoreCase(tab)) activeTab = "orders";
            else if ("profile".equalsIgnoreCase(tab) || "profil".equalsIgnoreCase(tab)) activeTab = "profile";
            else if ("products".equalsIgnoreCase(tab) || "barang".equalsIgnoreCase(tab) || "jual".equalsIgnoreCase(tab)) activeTab = "products";
            else if ("wishlist".equalsIgnoreCase(tab)) activeTab = "wishlist";
            else if ("rewearpay".equalsIgnoreCase(tab) || "pay".equalsIgnoreCase(tab)) activeTab = "rewearpay";
            else if ("settings".equalsIgnoreCase(tab) || "pengaturan".equalsIgnoreCase(tab)) activeTab = "settings";
        }

        buildMainLayout();
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
            User firstUser = userService.findAllUsers().stream().findFirst().orElse(null);
            targetUser = firstUser != null ? userService.findByIdWithSchool(firstUser.getId()).orElse(firstUser) : null;
            isOwnProfile = true;
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

        // 2-Column Dashboard Grid: Left Sidebar (300px) | Right Active Tab Content (Flex 1)
        HorizontalLayout gridLayout = new HorizontalLayout();
        gridLayout.setWidthFull();
        gridLayout.setSpacing(true);
        gridLayout.getElement().getStyle().set("gap", "28px");

        // LEFT SIDEBAR NAVIGATION
        Div leftSidebar = createLeftSidebar();

        // RIGHT CONTENT CONTAINER
        rightContentArea.setWidthFull();
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

        userCard.add(avatarCircle, userName, userRole);

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
            Button btnChatSeller = new Button("💬 Chat Penjual");
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
                "<div style='font-size:40px;margin-bottom:12px;'>📦</div>" +
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

        if (order.getStatus() == com.example.application.model.order.OrderStatus.DIKIRIM || order.getStatus() == com.example.application.model.order.OrderStatus.DITERIMA) {
            Button btnTerima = new Button("Konfirmasi Diterima", VaadinIcon.CHECK.create());
            btnTerima.getStyle().set("background", "#16A34A").set("color", "#FFFFFF").set("font-weight", "700").set("font-size", "12px").set("border-radius", "8px");
            btnTerima.addClickListener(e -> {
                orderService.updateOrderStatus(order, com.example.application.model.order.OrderStatus.SELESAI, "Dikonfirmasi diterima oleh pembeli.", targetUser);
                Notification.show("Pesanan Dikonfirmasi Diterima!", 2500, Notification.Position.TOP_CENTER);
                renderRightTabContent();
            });
            actionBtns.add(btnTerima);
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

    private void openOrderDetailModal(com.example.application.model.order.Order order) {
        Dialog d = new Dialog();
        d.setHeaderTitle("Rincian Pesanan #" + order.getOrderNumber());
        d.setWidth("450px");

        Div body = new Div();
        body.getStyle().set("font-size", "13px").set("color", "#334155").set("display", "flex").set("flex-direction", "column").set("gap", "8px");

        body.add(new Div(new Span("📍 Alamat Pengiriman: "), new Span(order.getShippingAddress() != null ? order.getShippingAddress() : "-")));
        body.add(new Div(new Span("💳 Metode Pembayaran: "), new Span(order.getPaymentMethod() != null ? order.getPaymentMethod() : "-")));
        body.add(new Div(new Span("🚚 Metode Pengiriman: "), new Span(order.getShippingMethod() != null ? order.getShippingMethod().name() : "-")));

        d.add(body);
        Button btnClose = new Button("Tutup", e -> d.close());
        d.getFooter().add(btnClose);
        d.open();
    }

    private Span buildStatusBadge(com.example.application.model.order.OrderStatus status) {
        Span badge = new Span();
        if (status == null) status = com.example.application.model.order.OrderStatus.MENUNGGU_PEMBAYARAN;
        switch (status) {
            case MENUNGGU_PEMBAYARAN -> { badge.setText("⏳ Menunggu Pembayaran"); badge.getStyle().set("background", "#FEF3C7").set("color", "#92400E"); }
            case DIBAYAR          -> { badge.setText("✅ Dibayar");              badge.getStyle().set("background", "#DCFCE7").set("color", "#166534"); }
            case DIPROSES         -> { badge.setText("⚙️ Diproses");            badge.getStyle().set("background", "#EFF6FF").set("color", "#1E40AF"); }
            case DIKIRIM          -> { badge.setText("🚚 Dikirim");             badge.getStyle().set("background", "#F0FDF4").set("color", "#15803D"); }
            case DITERIMA         -> { badge.setText("📬 Diterima");            badge.getStyle().set("background", "#F0FDF4").set("color", "#15803D"); }
            case SELESAI          -> { badge.setText("🎉 Selesai");             badge.getStyle().set("background", "#DCFCE7").set("color", "#166534"); }
            case KOMPLAIN         -> { badge.setText("⚠️ Komplain");            badge.getStyle().set("background", "#FEF2F2").set("color", "#991B1B"); }
            case DIBATALKAN       -> { badge.setText("❌ Dibatalkan");          badge.getStyle().set("background", "#F1F5F9").set("color", "#64748B"); }
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

        Span badgeSmk = new Span("🛡️ Warga SMKN 24");
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
            "🔒 Dana Ditahan (Escrow)" +
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
            "<div style='width:28px;height:28px;border-radius:9999px;background:#001934;color:#FFF;display:flex;align-items:center;justify-content:center;font-size:12px;font-weight:700;'>📦</div>" +
            "<span style='font-size:11px;font-weight:700;color:#001934;'>Diproses</span></div>" +

            // Step 3: Dikirim
            "<div style='z-index:3;display:flex;flex-direction:column;align-items:center;gap:6px;'>" +
            "<div style='width:28px;height:28px;border-radius:9999px;background:#FFF;border:2px solid #CBD5E1;color:#64748B;display:flex;align-items:center;justify-content:center;font-size:12px;'>🚚</div>" +
            "<span style='font-size:11px;font-weight:600;color:#94A3B8;'>Dikirim</span></div>" +

            // Step 4: Selesai
            "<div style='z-index:3;display:flex;flex-direction:column;align-items:center;gap:6px;'>" +
            "<div style='width:28px;height:28px;border-radius:9999px;background:#FFF;border:2px solid #CBD5E1;color:#64748B;display:flex;align-items:center;justify-content:center;font-size:12px;'>🏁</div>" +
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
            "✓ Dana Dicairkan" +
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
            "<span style='font-size:16px;'>⚠️</span>" +
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

        Span recText = new Span("✓ Diterima oleh Pembeli (20 Mar 2024)");
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

    private void openComplainDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Ajukan Komplain Pesanan");
        dialog.setWidth("500px");

        Div body = new Div();
        body.getElement().getStyle().set("display", "flex").set("flex-direction", "column").set("gap", "14px");

        Paragraph desc = new Paragraph("Sampaikan kendala barang (rusak/tidak sesuai deskripsi). Tim Escrow ReWear akan menahan pencairan dana hingga kendala terselesaikan.");
        desc.getElement().getStyle().set("font-size", "13px").set("color", "#64748B");

        TextField reasonField = new TextField("Alasan Komplain");
        reasonField.setPlaceholder("Contoh: Ukuran tidak sesuai / Cacat robek");
        reasonField.setWidthFull();

        body.add(desc, reasonField);
        dialog.add(body);

        Button btnClose = new Button("Batal", e -> dialog.close());
        Button btnSubmit = new Button("Kirim Komplain", e -> {
            dialog.close();
            Notification.show("Komplain berhasil diajukan! Tim ReWear akan menghubungi penjual.", 3000, Notification.Position.TOP_CENTER);
        });
        btnSubmit.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnSubmit.getElement().getStyle().set("background", "#B91C1C");

        dialog.getFooter().add(btnClose, btnSubmit);
        dialog.open();
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
        String phoneVal = targetUser != null && targetUser.getPhone() != null ? targetUser.getPhone() : "-";
        String schoolVal = (targetUser != null && targetUser.getSchool() != null && targetUser.getSchool().getName() != null)
            ? targetUser.getSchool().getName() : "SMKN 24 Jakarta";
        String roleVal = targetUser != null && targetUser.getRole() != null ? targetUser.getRole().name() : "Warga SMKN 24";

        TextField txtName = new TextField("Nama Lengkap", nameVal, "");
        txtName.setWidthFull();

        TextField txtRole = new TextField("Status / Peran", roleVal, "");
        txtRole.setWidthFull();

        TextField txtEmail = new TextField("Email", emailVal, "");
        txtEmail.setWidthFull();

        TextField txtPhone = new TextField("Nomor HP / WhatsApp", phoneVal, "");
        txtPhone.setWidthFull();

        TextField txtSchool = new TextField("Sekolah Terverifikasi", schoolVal, "");
        txtSchool.setWidthFull();

        TextField txtBio = new TextField("Bio / Deskripsi Profil", targetUser != null && targetUser.getBio() != null ? targetUser.getBio() : "Pengguna Aktif ReWear SMKN 24", "");
        txtBio.setWidthFull();

        formGrid.add(txtName, txtRole, txtEmail, txtPhone, txtSchool, txtBio);

        Button btnSave = new Button("Simpan Perubahan");
        btnSave.getElement().getStyle()
            .set("background", "#001934").set("color", "#FFFFFF").set("font-weight", "700")
            .set("border-radius", "8px").set("padding", "12px 24px").set("margin-top", "24px").set("cursor", "pointer");
        btnSave.addClickListener(e -> {
            if (targetUser != null) {
                targetUser.setFullName(txtName.getValue().trim());
                targetUser.setPhone(txtPhone.getValue().trim());
                targetUser.setBio(txtBio.getValue().trim());
                userService.saveUser(targetUser);
                Notification.show("Informasi profil berhasil diperbarui di Database!", 2500, Notification.Position.TOP_CENTER);
            }
        });

        wrapper.add(formGrid, btnSave);
        return wrapper;
    }

    // ==========================================
    // TAB 3: WISHLIST SAYA
    // ==========================================

    private Component renderWishlistTab() {
        Div wrapper = new Div();

        H2 title = new H2("Wishlist Saya");
        title.getElement().getStyle().set("font-size", "24px").set("font-weight", "800").set("color", "#001934").set("margin", "0 0 6px 0");

        Paragraph subtitle = new Paragraph("Daftar produk pre-loved yang Anda simpan.");
        subtitle.getElement().getStyle().set("font-size", "14px").set("color", "#64748B").set("margin", "0 0 24px 0");

        wrapper.add(title, subtitle);

        Div empty = new Div();
        empty.getElement().setProperty("innerHTML",
            "<div style='text-align:center;padding:48px 24px;background:#F8FAFC;border-radius:16px;border:1px dashed #CBD5E1;'>" +
            "<div style='font-size:40px;margin-bottom:12px;'>❤️</div>" +
            "<h3 style='color:#001934;font-size:18px;font-weight:700;margin:0 0 6px 0;'>Belum Ada Produk di Wishlist</h3>" +
            "<p style='color:#64748B;font-size:14px;margin:0;'>Jelajahi produk di Pasar SMKN 24 dan simpan barang favoritmu.</p>" +
            "</div>"
        );
        wrapper.add(empty);
        return wrapper;
    }

    private Div createWishlistCard(String pTitle, String pPrice, String imgUrl, String sellerName) {
        Div card = new Div();
        card.getElement().getStyle()
            .set("background", "#FFFFFF")
            .set("border", "1px solid #E2E8F0")
            .set("border-radius", "16px")
            .set("overflow", "hidden")
            .set("padding", "16px")
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("gap", "12px");

        Image img = new Image(imgUrl, pTitle);
        img.getElement().getStyle().set("width", "100%").set("height", "160px").set("border-radius", "12px").set("object-fit", "cover");

        H5 t = new H5(pTitle);
        t.getElement().getStyle().set("font-size", "15px").set("font-weight", "700").set("color", "#001934").set("margin", "0");

        Span p = new Span(pPrice);
        p.getElement().getStyle().set("font-size", "16px").set("font-weight", "800").set("color", "#001934");

        Span s = new Span("Penjual: " + sellerName);
        s.getElement().getStyle().set("font-size", "12px").set("color", "#64748B");

        Button btnCart = new Button("Pindahkan ke Keranjang");
        btnCart.getElement().getStyle()
            .set("background", "#001934").set("color", "#FFFFFF").set("font-weight", "700")
            .set("border-radius", "8px").set("border", "none").set("padding", "10px").set("cursor", "pointer");
        btnCart.addClickListener(e -> {
            Notification.show(pTitle + " berhasil dipindahkan ke keranjang!");
            UI.getCurrent().navigate("cart");
        });

        card.add(img, t, p, s, btnCart);
        return card;
    }

    // ==========================================
    // TAB 4: REWEAR PAY
    // ==========================================

    private Component renderReWearPayTab() {
        Div wrapper = new Div();

        H2 title = new H2("ReWear Pay");
        title.getElement().getStyle().set("font-size", "24px").set("font-weight", "800").set("color", "#001934").set("margin", "0 0 6px 0");

        Paragraph subtitle = new Paragraph("Kelola saldo transaksi aman, refund escrow, dan penarikan dana.");
        subtitle.getElement().getStyle().set("font-size", "14px").set("color", "#64748B").set("margin", "0 0 24px 0");

        wrapper.add(title, subtitle);

        // Saldo Banner Hero Card
        Div cardHero = new Div();
        cardHero.getElement().getStyle()
            .set("background", "linear-gradient(135deg, #001934 0%, #002B5B 100%)")
            .set("border-radius", "20px")
            .set("padding", "28px")
            .set("color", "#FFFFFF")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "space-between")
            .set("margin-bottom", "28px");

        List<com.example.application.model.order.Order> buyerOrders = targetUser != null ? orderService.getBuyerOrders(targetUser) : List.of();
        List<com.example.application.model.order.Order> sellerOrders = targetUser != null ? orderService.getSellerOrders(targetUser) : List.of();

        double saldoReWearPay = sellerOrders.stream()
            .filter(o -> o.getStatus() == com.example.application.model.order.OrderStatus.SELESAI)
            .mapToDouble(o -> o.getTotalAmount() != null ? o.getTotalAmount().doubleValue() : 0)
            .sum();

        double saldoEscrowTerikat = buyerOrders.stream()
            .filter(o -> o.getStatus() == com.example.application.model.order.OrderStatus.DIPROSES || o.getStatus() == com.example.application.model.order.OrderStatus.DIKIRIM || o.getStatus() == com.example.application.model.order.OrderStatus.MENUNGGU_PEMBAYARAN)
            .mapToDouble(o -> o.getTotalAmount() != null ? o.getTotalAmount().doubleValue() : 0)
            .sum();

        Div salLeft = new Div();
        Span lbl = new Span("Saldo ReWear Pay Aktif");
        lbl.getElement().getStyle().set("font-size", "13px").set("opacity", "0.8").set("display", "block").set("margin-bottom", "4px");

        H3 val = new H3("Rp " + String.format("%,.0f", saldoReWearPay));
        val.getElement().getStyle().set("font-size", "32px").set("font-weight", "800").set("margin", "0 0 8px 0").set("color", "#F5C45E");

        Span escrowTxt = new Span("🔒 Dana Escrow Terikat Pesanan Aktif: Rp " + String.format("%,.0f", saldoEscrowTerikat));
        escrowTxt.getElement().getStyle().set("font-size", "12px").set("opacity", "0.9");

        salLeft.add(lbl, val, escrowTxt);

        HorizontalLayout actBtns = new HorizontalLayout();
        actBtns.setSpacing(true);

        Button btnTopUp = new Button("+ Top Up");
        btnTopUp.getElement().getStyle()
            .set("background", "#F5C45E").set("color", "#001934").set("font-weight", "800")
            .set("border-radius", "8px").set("padding", "10px 20px").set("border", "none").set("cursor", "pointer");
        btnTopUp.addClickListener(e -> Notification.show("Fitur Top Up QRIS ReWear Pay dibuka."));

        Button btnWithdraw = new Button("💸 Tarik Dana");
        btnWithdraw.getElement().getStyle()
            .set("background", "rgba(255,255,255,0.15)").set("color", "#FFFFFF").set("font-weight", "700")
            .set("border-radius", "8px").set("padding", "10px 20px").set("border", "1px solid rgba(255,255,255,0.3)").set("cursor", "pointer");
        btnWithdraw.addClickListener(e -> Notification.show("Permintaan Penarikan Dana diproses ke Rekening/E-Wallet."));

        actBtns.add(btnTopUp, btnWithdraw);
        cardHero.add(salLeft, actBtns);

        wrapper.add(cardHero);
        return wrapper;
    }

    // ==========================================
    // TAB 5: PENGATURAN AKUN
    // ==========================================

    private Component renderSettingsTab() {
        Div wrapper = new Div();

        H2 title = new H2("Pengaturan Akun");
        title.getElement().getStyle().set("font-size", "24px").set("font-weight", "800").set("color", "#001934").set("margin", "0 0 6px 0");

        Paragraph subtitle = new Paragraph("Atur keamanan kata sandi dan preferensi notifikasi Anda.");
        subtitle.getElement().getStyle().set("font-size", "14px").set("color", "#64748B").set("margin", "0 0 24px 0");

        wrapper.add(title, subtitle);

        Div form = new Div();
        form.getElement().getStyle().set("display", "flex").set("flex-direction", "column").set("gap", "16px").set("max-width", "500px");

        PasswordField oldPass = new PasswordField("Kata Sandi Lama");
        oldPass.setWidthFull();

        PasswordField newPass = new PasswordField("Kata Sandi Baru");
        newPass.setWidthFull();

        PasswordField confirmPass = new PasswordField("Konfirmasi Kata Sandi Baru");
        confirmPass.setWidthFull();

        Button btnChange = new Button("Ubah Kata Sandi");
        btnChange.getElement().getStyle()
            .set("background", "#001934").set("color", "#FFFFFF").set("font-weight", "700")
            .set("border-radius", "8px").set("padding", "12px").set("cursor", "pointer");
        btnChange.addClickListener(e -> Notification.show("Kata sandi berhasil diperbarui!", 2500, Notification.Position.TOP_CENTER));

        form.add(oldPass, newPass, confirmPass, btnChange);
        wrapper.add(form);
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
                "<div style='font-size:40px;margin-bottom:12px;'>🏷️</div>" +
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
            grid.getElement().getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(auto-fill, minmax(220px, 1fr))")
                .set("gap", "20px")
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
            .set("border-radius", "14px")
            .set("overflow", "hidden")
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("cursor", "pointer")
            .set("transition", "transform 0.2s ease, box-shadow 0.2s ease");

        card.getElement().addEventListener("mouseenter", e -> {
            card.getElement().getStyle().set("transform", "translateY(-4px)").set("box-shadow", "0 10px 24px rgba(0,25,52,0.1)");
        });
        card.getElement().addEventListener("mouseleave", e -> {
            card.getElement().getStyle().set("transform", "translateY(0)").set("box-shadow", "none");
        });

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
        img.getElement().getStyle().set("width", "100%").set("height", "180px").set("object-fit", "cover");

        Div infoBox = new Div();
        infoBox.getElement().getStyle().set("padding", "14px").set("display", "flex").set("flex-direction", "column").set("gap", "6px");

        if (p.isSchoolMarket()) {
            Span badge = new Span("WARGA SMKN 24");
            badge.getStyle().set("font-size", "10px").set("font-weight", "800").set("background", "#FEF3C7").set("color", "#92400E").set("padding", "2px 8px").set("border-radius", "4px").set("width", "fit-content");
            infoBox.add(badge);
        }

        Span title = new Span(p.getName());
        title.getElement().getStyle().set("font-size", "14px").set("font-weight", "700").set("color", "#001934").set("white-space", "nowrap").set("overflow", "hidden").set("text-overflow", "ellipsis");

        Span price = new Span("Rp " + String.format("%,.0f", p.getPrice() != null ? p.getPrice() : 0));
        price.getElement().getStyle().set("font-size", "16px").set("font-weight", "800").set("color", "#001934");

        infoBox.add(title, price);
        card.add(img, infoBox);
        return card;
    }
}
