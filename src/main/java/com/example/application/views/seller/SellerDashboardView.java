package com.example.application.views.seller;

import com.example.application.model.order.Order;
import com.example.application.model.order.OrderItem;
import com.example.application.model.order.OrderStatus;
import com.example.application.model.product.Product;
import com.example.application.model.user.User;
import com.example.application.service.order.OrderService;
import com.example.application.service.product.ProductService;
import com.example.application.util.AuthGuard;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.example.application.model.order.CourierName;
import com.example.application.model.order.ShippingMethod;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.*;
import com.example.application.service.payment.PaymentService;
import com.example.application.model.payment.SellerPayout;
import com.example.application.model.payment.PayoutStatus;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "seller/dashboard", layout = MainLayout.class)
@RouteAlias(value = "seller", layout = MainLayout.class)
@RouteAlias(value = "dashboard-penjual", layout = MainLayout.class)
@PageTitle("Dashboard Penjual | ReWear SMKN 24")
@Menu(order = 2, icon = "line-awesome/svg/store-solid.svg", title = "Dashboard Penjual")
public class SellerDashboardView extends VerticalLayout implements BeforeEnterObserver {

    private final ProductService productService;
    private final OrderService orderService;
    private final PaymentService paymentService;

    // Active tab state: "ringkasan", "produk", "pesanan", "laporan", "pengaturan"
    private String activeTab = "ringkasan";

    private final Div contentContainer = new Div();
    private final Div rightContentArea = new Div();

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    private boolean isStoreActivated = false;

    public SellerDashboardView(ProductService productService, OrderService orderService, PaymentService paymentService) {
        this.productService = productService;
        this.orderService = orderService;
        this.paymentService = paymentService;

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
        if (!AuthGuard.requireLogin(UI.getCurrent())) return;
        buildMainLayout();
    }

    private void buildMainLayout() {
        contentContainer.removeAll();

        User currentSeller = AuthGuard.getCurrentUser();
        List<Product> products = currentSeller != null ? productService.findProductsBySeller(currentSeller) : List.of();
        List<Order> orders = currentSeller != null ? orderService.getSellerOrders(currentSeller) : List.of();

        boolean isActiveSeller = (products != null && !products.isEmpty()) || (orders != null && !orders.isEmpty()) || isStoreActivated;

        if (!isActiveSeller) {
            contentContainer.add(createSellerOnboardingView());
            return;
        }

        HorizontalLayout gridLayout = new HorizontalLayout();
        gridLayout.setWidthFull();
        gridLayout.setSpacing(true);
        gridLayout.addClassName("rw-seller-dashboard-grid");
        gridLayout.getElement().getStyle().set("gap", "28px");

        // LEFT SIDEBAR NAVIGATION
        Div leftSidebar = createLeftSidebar();

        // RIGHT CONTENT AREA
        rightContentArea.setWidthFull();
        rightContentArea.addClassName("rw-seller-content-area");
        rightContentArea.getElement().getStyle().set("flex", "1");
        renderRightTabContent();

        gridLayout.add(leftSidebar, rightContentArea);
        gridLayout.expand(rightContentArea);

        contentContainer.add(gridLayout);
    }

    private Component createSellerOnboardingView() {
        Div card = new Div();
        card.addClassName("rw-seller-onboarding-card");
        card.getElement().getStyle()
            .set("background", "#FFFFFF")
            .set("border", "1px solid #E2E8F0")
            .set("border-radius", "24px")
            .set("padding", "44px 36px")
            .set("box-shadow", "0 10px 30px rgba(0, 25, 52, 0.05)")
            .set("text-align", "center")
            .set("max-width", "880px")
            .set("margin", "20px auto 40px auto")
            .set("box-sizing", "border-box");

        // Top Banner Icon
        Div iconBadge = new Div();
        iconBadge.getElement().getStyle()
            .set("width", "80px")
            .set("height", "80px")
            .set("margin", "0 auto 20px auto")
            .set("border-radius", "20px")
            .set("background", "linear-gradient(135deg, #001934 0%, #002B5B 100%)")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center")
            .set("box-shadow", "0 8px 20px rgba(0, 25, 52, 0.15)");

        Icon storeIcon = VaadinIcon.SHOP.create();
        storeIcon.getElement().getStyle().set("width", "40px").set("height", "40px").set("color", "#F5C45E");
        iconBadge.add(storeIcon);

        Span badgeText = new Span("PROGRAM KEMITRAAN WARGA SMKN 24");
        badgeText.getElement().getStyle()
            .set("background", "#FEF3C7")
            .set("color", "#92400E")
            .set("font-weight", "800")
            .set("font-size", "12px")
            .set("padding", "6px 16px")
            .set("border-radius", "9999px")
            .set("letter-spacing", "0.5px")
            .set("display", "inline-block")
            .set("margin-bottom", "16px");

        H2 title = new H2("Mulai Berjualan di ReWear SMKN 24");
        title.getElement().getStyle()
            .set("font-size", "28px")
            .set("font-weight", "900")
            .set("color", "#001934")
            .set("margin", "0 0 10px 0");

        Paragraph sub = new Paragraph("Ubah seragam, buku, almamater, dan barang preloved-mu jadi uang tambahan. Transaksi cepat, aman, dan 100% tanpa biaya komisi!");
        sub.getElement().getStyle()
            .set("font-size", "15px")
            .set("color", "#64748B")
            .set("max-width", "620px")
            .set("margin", "0 auto 32px auto")
            .set("line-height", "1.6");

        // 3 Benefit Cards Grid
        Div benefitsGrid = new Div();
        benefitsGrid.addClassName("rw-onboarding-benefits-grid");
        benefitsGrid.getElement().getStyle()
            .set("display", "grid")
            .set("grid-template-columns", "repeat(3, 1fr)")
            .set("gap", "16px")
            .set("margin-bottom", "36px")
            .set("text-align", "left");

        benefitsGrid.add(createBenefitCard(VaadinIcon.MONEY_DEPOSIT, "100% Bebas Komisi", "Semua hasil penjualan barangmu utuh masuk ke dompet ReWearPay milikmu."));
        benefitsGrid.add(createBenefitCard(VaadinIcon.SHIELD, "COD & Escrow SMKN 24", "Sistem keamanan transaksi terjamin untuk sesama warga sekolah SMKN 24."));
        benefitsGrid.add(createBenefitCard(VaadinIcon.CART, "Langsung Dilihat Pembeli", "Produkmu otomatis tampil di Pasar SMKN 24 dan siap dibeli teman sekolah."));

        // Action Button
        Button btnRegisterSeller = new Button("🚀 Buka Toko & Tambah Produk Pertama", e -> {
            isStoreActivated = true;
            UI.getCurrent().navigate("sell");
        });
        btnRegisterSeller.getElement().getStyle()
            .set("background", "#001934")
            .set("color", "#F5C45E")
            .set("font-size", "15px")
            .set("font-weight", "800")
            .set("padding", "16px 32px")
            .set("border-radius", "12px")
            .set("border", "none")
            .set("cursor", "pointer")
            .set("box-shadow", "0 6px 16px rgba(0, 25, 52, 0.2)");

        card.add(iconBadge, badgeText, title, sub, benefitsGrid, btnRegisterSeller);
        return card;
    }

    private Div createBenefitCard(VaadinIcon icon, String title, String desc) {
        Div box = new Div();
        box.getElement().getStyle()
            .set("background", "#F8FAFC")
            .set("border", "1px solid #E2E8F0")
            .set("border-radius", "16px")
            .set("padding", "20px")
            .set("box-sizing", "border-box");

        Icon ic = icon.create();
        ic.getElement().getStyle().set("width", "24px").set("height", "24px").set("color", "#001934").set("margin-bottom", "10px");

        H4 h4 = new H4(title);
        h4.getElement().getStyle().set("font-size", "15px").set("font-weight", "800").set("color", "#001934").set("margin", "0 0 6px 0");

        Paragraph p = new Paragraph(desc);
        p.getElement().getStyle().set("font-size", "13px").set("color", "#64748B").set("margin", "0").set("line-height", "1.5");

        box.add(ic, h4, p);
        return box;
    }

    // ==========================================
    // LEFT SIDEBAR NAVIGATION
    // ==========================================

    private Div createLeftSidebar() {
        Div sidebar = new Div();
        sidebar.addClassName("rw-seller-sidebar");
        sidebar.getElement().getStyle()
            .set("width", "260px")
            .set("flex-shrink", "0")
            .set("background", "#EFF4FF")
            .set("border-radius", "16px")
            .set("padding", "24px 16px")
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("gap", "20px")
            .set("box-sizing", "border-box");

        // Sidebar Header Title
        Div headerCard = new Div();
        headerCard.getElement().getStyle()
            .set("padding-bottom", "16px")
            .set("border-bottom", "1px solid #DBEAFE");

        H3 title = new H3("Dashboard Penjual");
        title.getElement().getStyle()
            .set("font-size", "18px")
            .set("font-weight", "800")
            .set("color", "#001934")
            .set("margin", "0 0 4px 0");

        User currentSeller = AuthGuard.getCurrentUser();
        String sellerName = currentSeller != null && currentSeller.getFullName() != null ? currentSeller.getFullName() : "Penjual ReWear";

        Span statusSpan = new Span("● " + sellerName);
        statusSpan.getElement().getStyle()
            .set("font-size", "12px")
            .set("font-weight", "700")
            .set("color", "#15803D");

        headerCard.add(title, statusSpan);

        // Navigation Links
        Div navList = new Div();
        navList.addClassName("rw-seller-nav-list");
        navList.getElement().getStyle()
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("gap", "6px");

        navList.add(createNavItem("Ringkasan", "ringkasan", VaadinIcon.CHART));
        navList.add(createNavItem("Pesanan Masuk", "pesanan", VaadinIcon.CART));
        navList.add(createNavItem("Produk Saya", "produk", VaadinIcon.PACKAGE));
        navList.add(createNavItem("Laporan Keuangan", "laporan", VaadinIcon.WALLET));
        navList.add(createNavItem("Pengaturan Toko", "pengaturan", VaadinIcon.COG));

        sidebar.add(headerCard, navList);
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
            .set("background", isActive ? "#001934" : "transparent")
            .set("color", isActive ? "#F5C45E" : "#475569");

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

        switch (activeTab.toLowerCase()) {
            case "ringkasan":
                rightContentArea.add(renderRingkasanTab());
                break;
            case "produk":
                rightContentArea.add(renderProdukSayaTab());
                break;
            case "pesanan":
                rightContentArea.add(renderPesananMasukTab());
                break;
            case "laporan":
                rightContentArea.add(renderLaporanTab());
                break;
            case "pengaturan":
                rightContentArea.add(renderPengaturanTab());
                break;
            default:
                rightContentArea.add(renderRingkasanTab());
                break;
        }
    }

    // ==========================================
    // TAB 1: RINGKASAN
    // ==========================================

    private Component renderRingkasanTab() {
        Div wrapper = new Div();
        wrapper.getElement().getStyle()
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("gap", "24px");

        User seller = AuthGuard.getCurrentUser();
        List<Order> orders = seller != null ? orderService.getSellerOrders(seller) : List.of();
        List<Product> products = seller != null ? productService.findProductsBySeller(seller) : List.of();

        double totalPenjualan = orders.stream()
            .filter(o -> o.getStatus() == OrderStatus.DIBAYAR || o.getStatus() == OrderStatus.DIPROSES || o.getStatus() == OrderStatus.DIKIRIM || o.getStatus() == OrderStatus.DITERIMA || o.getStatus() == OrderStatus.SELESAI)
            .mapToDouble(o -> o.getTotalAmount() != null ? o.getTotalAmount().doubleValue() : 0)
            .sum();

        long totalPesanan = orders.size();
        long totalProduk = products.size();

        // Greeting Header
        HorizontalLayout greetingRow = new HorizontalLayout();
        greetingRow.setWidthFull();
        greetingRow.setAlignItems(FlexComponent.Alignment.CENTER);
        greetingRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Div greetLeft = new Div();
        String sellerName = seller != null && seller.getFullName() != null ? seller.getFullName() : "Penjual ReWear";
        H2 greetTitle = new H2("Selamat Datang, " + sellerName);
        greetTitle.getElement().getStyle()
            .set("font-size", "24px").set("font-weight", "800").set("color", "#001934").set("margin", "0 0 4px 0");

        Paragraph greetSub = new Paragraph("Berikut ringkasan performa toko dan transaksi pesananmu.");
        greetSub.getElement().getStyle().set("font-size", "14px").set("color", "#64748B").set("margin", "0");

        greetLeft.add(greetTitle, greetSub);
        greetingRow.add(greetLeft);
        wrapper.add(greetingRow);

        // Metrics Grid
        Div statsGrid = new Div();
        statsGrid.getElement().getStyle()
            .set("display", "grid")
            .set("grid-template-columns", "repeat(3, 1fr)")
            .set("gap", "16px");

        statsGrid.add(createStatCard("TOTAL OMSET PENJUALAN", "Rp " + String.format("%,.0f", totalPenjualan), "Real DB", "#15803D", "#DCFCE7", VaadinIcon.MONEY));
        statsGrid.add(createStatCard("TOTAL PESANAN MASUK", totalPesanan + " Transaksi", orders.isEmpty() ? "Belum ada" : "Aktif", "#1E40AF", "#DBEAFE", VaadinIcon.PACKAGE));
        statsGrid.add(createStatCard("PRODUK DIJUAL", totalProduk + " Barang", "Katalog", "#475569", "#F1F5F9", VaadinIcon.TAG));

        wrapper.add(statsGrid);

        // Section Pesanan Terbaru
        Div sectionTitle = new Div();
        H3 h3 = new H3("Pesanan Terbaru dari Pembeli");
        h3.getStyle().set("font-size", "18px").set("font-weight", "800").set("color", "#001934").set("margin", "16px 0 12px 0");
        sectionTitle.add(h3);
        wrapper.add(sectionTitle);

        wrapper.add(buildSellerOrdersList(orders));

        return wrapper;
    }

    private Div createStatCard(String label, String value, String badgeText, String badgeColor, String badgeBg, VaadinIcon icon) {
        Div card = new Div();
        card.getElement().getStyle()
            .set("background", "#FFFFFF")
            .set("border", "1px solid #E2E8F0")
            .set("border-radius", "16px")
            .set("padding", "20px")
            .set("box-shadow", "0 2px 8px rgba(0,25,52,0.03)")
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("justify-content", "space-between");

        HorizontalLayout top = new HorizontalLayout();
        top.setWidthFull();
        top.setAlignItems(FlexComponent.Alignment.CENTER);
        top.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Div iconBox = new Div(icon.create());
        iconBox.getElement().getStyle()
            .set("width", "36px").set("height", "36px").set("border-radius", "10px").set("background", "#EFF6FF")
            .set("color", "#0A3D7A")
            .set("display", "flex").set("align-items", "center").set("justify-content", "center").set("font-size", "18px");

        Span badge = new Span(badgeText);
        badge.getElement().getStyle()
            .set("background", badgeBg).set("color", badgeColor).set("font-weight", "800")
            .set("font-size", "11px").set("padding", "3px 8px").set("border-radius", "6px");

        top.add(iconBox, badge);

        Div meta = new Div();
        meta.getElement().getStyle().set("margin-top", "12px");

        Span lbl = new Span(label);
        lbl.getElement().getStyle().set("font-size", "11px").set("font-weight", "700").set("color", "#64748B").set("letter-spacing", "0.5px").set("display", "block").set("margin-bottom", "4px");

        Span val = new Span(value);
        val.getElement().getStyle().set("font-size", "20px").set("font-weight", "800").set("color", "#001934");

        meta.add(lbl, val);
        card.add(top, meta);
        return card;
    }

    // ==========================================
    // TAB 2: PESANAN MASUK (SELLER ORDER MANAGEMENT DARI DATABASE REAL)
    // ==========================================

    private Component renderPesananMasukTab() {
        Div wrapper = new Div();
        H2 title = new H2("Kelola Pesanan Masuk");
        title.getElement().getStyle().set("font-size", "24px").set("font-weight", "800").set("color", "#001934").set("margin", "0 0 4px 0");

        Paragraph sub = new Paragraph("Pantau dan proses semua pesanan dari pembeli secara real-time dari database.");
        sub.getElement().getStyle().set("color", "#64748B").set("margin", "0 0 24px 0");

        User seller = AuthGuard.getCurrentUser();
        List<Order> orders = seller != null ? orderService.getSellerOrders(seller) : List.of();

        wrapper.add(title, sub, buildSellerOrdersList(orders));
        return wrapper;
    }

    private Div buildSellerOrdersList(List<Order> orders) {
        Div container = new Div();
        container.getStyle().set("display", "flex").set("flex-direction", "column").set("gap", "16px");

        if (orders.isEmpty()) {
            Div empty = new Div();
            empty.getElement().setProperty("innerHTML",
                "<div style='text-align:center;padding:48px 24px;background:#FFFFFF;border-radius:16px;border:1px dashed #CBD5E1;'>" +
                "<div style='width:48px;height:48px;margin:0 auto 12px;border-radius:12px;background:#F1F5F9;display:flex;align-items:center;justify-content:center;'>" +
                "<svg width='24' height='24' viewBox='0 0 24 24' fill='none' stroke='#64748B' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><path d='M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z'></path><polyline points='3.27 6.96 12 12.01 20.73 6.96'></polyline><line x1='12' y1='22.08' x2='12' y2='12'></line></svg>" +
                "</div>" +
                "<h3 style='color:#001934;font-size:18px;font-weight:800;margin:0 0 6px 0;'>Belum Ada Pesanan Masuk</h3>" +
                "<p style='color:#64748B;font-size:14px;margin:0;'>Pesanan dari pembeli yang membeli barangmu akan muncul di sini secara otomatis.</p>" +
                "</div>"
            );
            container.add(empty);
            return container;
        }

        User sellerActor = AuthGuard.getCurrentUser();

        for (Order order : orders) {
            Div card = new Div();
            card.getStyle()
                .set("background", "#FFFFFF")
                .set("border", "1px solid #E2E8F0")
                .set("border-radius", "14px")
                .set("padding", "20px")
                .set("box-shadow", "0 2px 8px rgba(0,25,52,0.03)");

            // Header Row: Order Number & Date & Status
            HorizontalLayout header = new HorizontalLayout();
            header.setWidthFull();
            header.setAlignItems(FlexComponent.Alignment.CENTER);
            header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

            Div leftHead = new Div();
            Span orderNum = new Span("Order #" + order.getOrderNumber());
            orderNum.getStyle().set("font-weight", "800").set("color", "#001934").set("font-size", "15px");

            String dateStr = order.getCreatedAt() != null ? order.getCreatedAt().format(DATE_FMT) : "-";
            Span orderDate = new Span(" • " + dateStr);
            orderDate.getStyle().set("color", "#64748B").set("font-size", "13px");

            leftHead.add(orderNum, orderDate);

            Span statusBadge = buildStatusBadge(order.getStatus());
            header.add(leftHead, statusBadge);

            // Buyer & Address Info
            Div buyerBox = new Div();
            buyerBox.getStyle().set("margin", "12px 0").set("padding", "12px 16px").set("background", "#F8FAFC").set("border-radius", "8px").set("font-size", "13px");

            String buyerName = order.getBuyer() != null && order.getBuyer().getFullName() != null ? order.getBuyer().getFullName() : "Pembeli ReWear";
            Div bRow = new Div(new Span("Pembeli: "), new Span(buyerName));
            bRow.getStyle().set("font-weight", "700").set("color", "#001934").set("margin-bottom", "4px");

            Div aRow = new Div(new Span("Alamat Tujuan: "), new Span(order.getShippingAddress() != null ? order.getShippingAddress() : "-"));
            aRow.getStyle().set("color", "#475569");

            buyerBox.add(bRow, aRow);

            // Items list
            List<OrderItem> items = orderService.getOrderItems(order);
            Div itemsBox = new Div();
            itemsBox.getStyle().set("margin-bottom", "14px");

            for (OrderItem item : items) {
                Div itemRow = new Div();
                itemRow.getStyle().set("display", "flex").set("justify-content", "space-between").set("font-size", "13px").set("margin-bottom", "4px");
                Span name = new Span("• " + item.getProductNameSnapshot() + " (x" + item.getQuantity() + ")");
                name.getStyle().set("color", "#1E293B").set("font-weight", "600");
                Span price = new Span("Rp " + String.format("%,.0f", item.getPriceSnapshot().doubleValue() * item.getQuantity()));
                price.getStyle().set("color", "#001934").set("font-weight", "700");
                itemRow.add(name, price);
                itemsBox.add(itemRow);
            }

            // Total + Action Buttons for Seller
            HorizontalLayout footer = new HorizontalLayout();
            footer.setWidthFull();
            footer.setAlignItems(FlexComponent.Alignment.CENTER);
            footer.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

            Div totalBox = new Div();
            Span tLbl = new Span("Total Pembayaran: ");
            tLbl.getStyle().set("font-size", "13px").set("color", "#64748B");
            Span tVal = new Span("Rp " + String.format("%,.0f", order.getTotalAmount() != null ? order.getTotalAmount().doubleValue() : 0));
            tVal.getStyle().set("font-size", "16px").set("font-weight", "800").set("color", "#001934");
            totalBox.add(tLbl, tVal);

            HorizontalLayout actionBtns = new HorizontalLayout();
            actionBtns.setSpacing(true);

            // Tombol Penjual berdasarkan Status
            if (order.getStatus() == OrderStatus.MENUNGGU_PEMBAYARAN || order.getStatus() == OrderStatus.DIBAYAR) {
                Button btnProses = new Button("Proses Pesanan", e -> {
                    orderService.updateOrderStatus(order, OrderStatus.DIPROSES, "Pesanan diterima dan sedang diproses penjual.", sellerActor);
                    Notification success = Notification.show("Pesanan #" + order.getOrderNumber() + " sedang diproses.", 2500, Notification.Position.TOP_CENTER);
                    success.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    buildMainLayout();
                });
                btnProses.getStyle().set("background", "#001934").set("color", "#FFFFFF").set("font-size", "12px").set("font-weight", "700").set("border-radius", "8px").set("cursor", "pointer");
                actionBtns.add(btnProses);
            }

            if (order.getStatus() == OrderStatus.DIPROSES) {
                Button btnKirim = new Button("Atur Pengiriman", VaadinIcon.TRUCK.create(), e -> {
                    openShipOrderDialog(order, sellerActor);
                });
                btnKirim.getStyle().set("background", "#16A34A").set("color", "#FFFFFF").set("font-size", "12px").set("font-weight", "700").set("border-radius", "8px").set("cursor", "pointer");
                actionBtns.add(btnKirim);
            }

            if (order.getStatus() == OrderStatus.DIKIRIM) {
                Div shipInfo = new Div();
                shipInfo.getStyle().set("font-size", "12px").set("color", "#475569").set("background", "#F1F5F9").set("padding", "6px 12px").set("border-radius", "6px");
                String courier = order.getCourierName() != null ? order.getCourierName().name() : "Kurir";
                String tracking = order.getTrackingNumber() != null ? order.getTrackingNumber() : "-";
                shipInfo.setText("Pengiriman: " + courier + " (" + tracking + ")");
                actionBtns.add(shipInfo);
            }

            footer.add(totalBox, actionBtns);
            card.add(header, buyerBox, itemsBox, footer);
            container.add(card);
        }

        return container;
    }

    private void openShipOrderDialog(Order order, User sellerActor) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Atur Pengiriman Pesanan #" + order.getOrderNumber());
        dialog.setWidth("480px");

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(false);

        boolean isCod = order.getShippingMethod() == ShippingMethod.COD_SEKOLAH;

        if (isCod) {
            Paragraph info = new Paragraph("Pesanan ini menggunakan metode COD Sekolah (Pasar SMKN 24). Tentukan titik pertemuan dan waktu serah terima barang.");
            info.getStyle().set("font-size", "13px").set("color", "#64748B").set("margin", "0");

            ComboBox<String> locationCombo = new ComboBox<>("Titik Pertemuan di SMKN 24");
            locationCombo.setItems(
                "Kantin Utama SMKN 24",
                "Pos Satpam / Gerbang Depan",
                "Lobby Gedung Pusat",
                "Ruang OSIS / Lapangan",
                "Bengkel / Lab Kejuruan",
                "Lainnya"
            );
            locationCombo.setValue("Kantin Utama SMKN 24");
            locationCombo.setWidthFull();

            TextField timeField = new TextField("Waktu & Janji Temu");
            timeField.setPlaceholder("Contoh: Istirahat pertama jam 10.00 di meja kantin barat");
            timeField.setWidthFull();

            layout.add(info, locationCombo, timeField);

            Button btnCancel = new Button("Batal", e -> dialog.close());
            btnCancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

            Button btnSubmit = new Button("Konfirmasi Siap COD", e -> {
                String loc = locationCombo.getValue();
                String timeNote = timeField.getValue();
                String fullNotes = "COD di " + loc + (timeNote != null && !timeNote.isBlank() ? " (" + timeNote + ")" : "");

                orderService.shipOrder(order, CourierName.LAINNYA, loc, fullNotes, sellerActor);
                Notification.show("Pesanan ditandai siap COD di " + loc, 3000, Notification.Position.TOP_CENTER);
                dialog.close();
                buildMainLayout();
            });
            btnSubmit.getStyle().set("background", "#001934").set("color", "#FFFFFF").set("font-weight", "700").set("border-radius", "8px");

            dialog.getFooter().add(btnCancel, btnSubmit);
        } else {
            Paragraph info = new Paragraph("Pesanan ini dikirim via jasa ekspedisi reguler. Masukkan kurir dan nomor resi pengiriman.");
            info.getStyle().set("font-size", "13px").set("color", "#64748B").set("margin", "0");

            ComboBox<CourierName> courierCombo = new ComboBox<>("Jasa Kurir");
            courierCombo.setItems(CourierName.values());
            courierCombo.setItemLabelGenerator(Enum::name);
            courierCombo.setValue(CourierName.JNE);
            courierCombo.setWidthFull();

            TextField resiField = new TextField("Nomor Resi Pengiriman");
            resiField.setPlaceholder("Contoh: JP9821049281");
            resiField.setWidthFull();

            Button btnAutoResi = new Button("Buat Resi Otomatis", VaadinIcon.MAGIC.create());
            btnAutoResi.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            btnAutoResi.getStyle().set("font-size", "12px").set("font-weight", "700").set("color", "#2563EB").set("cursor", "pointer");
            btnAutoResi.addClickListener(e -> {
                CourierName c = courierCombo.getValue() != null ? courierCombo.getValue() : CourierName.JNE;
                long rand = (long) (Math.random() * 90000000L) + 10000000L;
                resiField.setValue("RW-" + c.name() + "-" + rand);
            });

            TextArea noteField = new TextArea("Catatan Tambahan untuk Pembeli (Opsional)");
            noteField.setPlaceholder("Paket sudah diserahkan ke kurir...");
            noteField.setWidthFull();

            layout.add(info, courierCombo, resiField, btnAutoResi, noteField);

            Button btnCancel = new Button("Batal", e -> dialog.close());
            btnCancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

            Button btnSubmit = new Button("Konfirmasi Pengiriman", e -> {
                String resi = resiField.getValue();
                if (resi == null || resi.isBlank()) {
                    Notification.show("Harap isi nomor resi atau gunakan 'Buat Resi Otomatis'", 3000, Notification.Position.TOP_CENTER);
                    return;
                }

                CourierName c = courierCombo.getValue() != null ? courierCombo.getValue() : CourierName.JNE;
                String note = noteField.getValue();
                orderService.shipOrder(order, c, resi, note, sellerActor);

                Notification.show("Pengiriman berhasil dikonfirmasi dengan resi: " + resi, 3000, Notification.Position.TOP_CENTER);
                dialog.close();
                buildMainLayout();
            });
            btnSubmit.getStyle().set("background", "#001934").set("color", "#FFFFFF").set("font-weight", "700").set("border-radius", "8px");

            dialog.getFooter().add(btnCancel, btnSubmit);
        }

        dialog.add(layout);
        dialog.open();
    }

    private Span buildStatusBadge(OrderStatus status) {
        Span badge = new Span();
        if (status == null) status = OrderStatus.MENUNGGU_PEMBAYARAN;

        switch (status) {
            case MENUNGGU_PEMBAYARAN -> {
                badge.setText("Menunggu Pembayaran");
                badge.getStyle().set("background", "#FEF3C7").set("color", "#92400E");
            }
            case DIBAYAR -> {
                badge.setText("Sudah Dibayar");
                badge.getStyle().set("background", "#DCFCE7").set("color", "#166534");
            }
            case DIPROSES -> {
                badge.setText("Sedang Diproses");
                badge.getStyle().set("background", "#EFF6FF").set("color", "#1E40AF");
            }
            case DIKIRIM -> {
                badge.setText("Dalam Pengiriman");
                badge.getStyle().set("background", "#F0FDF4").set("color", "#15803D");
            }
            case DITERIMA -> {
                badge.setText("Pesanan Diterima");
                badge.getStyle().set("background", "#F0FDF4").set("color", "#15803D");
            }
            case SELESAI -> {
                badge.setText("Selesai");
                badge.getStyle().set("background", "#DCFCE7").set("color", "#166534");
            }
            case KOMPLAIN -> {
                badge.setText("Komplain / Retur");
                badge.getStyle().set("background", "#FEF2F2").set("color", "#991B1B");
            }
            case DIBATALKAN -> {
                badge.setText("Dibatalkan");
                badge.getStyle().set("background", "#F1F5F9").set("color", "#64748B");
            }
        }

        badge.getStyle()
            .set("font-size", "12px")
            .set("font-weight", "700")
            .set("padding", "4px 10px")
            .set("border-radius", "20px");

        return badge;
    }

    // ==========================================
    // OTHER TABS (PRODUK, LAPORAN, PENGATURAN)
    // ==========================================

    private Component renderProdukSayaTab() {
        Div wrapper = new Div();

        HorizontalLayout top = new HorizontalLayout();
        top.setWidthFull();
        top.setAlignItems(FlexComponent.Alignment.CENTER);
        top.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        top.getElement().getStyle().set("margin-bottom", "20px");

        H2 title = new H2("Daftar Produk Saya");
        title.getElement().getStyle().set("font-size", "24px").set("font-weight", "800").set("color", "#001934").set("margin", "0");

        Button btnAdd = new Button("+ Tambah Produk Baru", e -> UI.getCurrent().navigate("sell"));
        btnAdd.getElement().getStyle()
            .set("background", "#001934").set("color", "#FFFFFF").set("font-weight", "700")
            .set("border-radius", "8px").set("padding", "10px 18px").set("cursor", "pointer");

        top.add(title, btnAdd);
        wrapper.add(top);

        User seller = AuthGuard.getCurrentUser();
        List<Product> products = seller != null ? productService.findProductsBySeller(seller) : List.of();

        Grid<Product> grid = new Grid<>(Product.class, false);
        grid.addColumn(Product::getName).setHeader("Nama Produk");
        grid.addColumn(p -> "Rp " + String.format("%,.0f", p.getPrice() != null ? p.getPrice() : 0)).setHeader("Harga");
        grid.addColumn(p -> p.getStock() != null ? p.getStock() : 1).setHeader("Stok");
        grid.addColumn(p -> p.getSoldCount() != null ? p.getSoldCount() : 0).setHeader("Terjual");
        grid.setItems(products);

        wrapper.add(grid);
        return wrapper;
    }

    private Component renderLaporanTab() {
        Div wrapper = new Div();
        wrapper.getElement().getStyle().set("display", "flex").set("flex-direction", "column").set("gap", "28px");

        // 1. Header Section
        Div header = new Div();
        H2 title = new H2("Laporan Keuangan & Saldo ReWearPay");
        title.getElement().getStyle().set("font-size", "24px").set("font-weight", "800").set("color", "#001934").set("margin", "0 0 6px 0");

        Paragraph sub = new Paragraph("Pantau penghasilan toko, dana escrow terikat pesanan, dan ajukan pencairan saldo langsung ke rekening bank atau e-wallet Anda.");
        sub.getElement().getStyle().set("font-size", "14px").set("color", "#64748B").set("margin", "0");
        header.add(title, sub);
        wrapper.add(header);

        User seller = AuthGuard.getCurrentUser();
        BigDecimal availableBalance = seller != null ? paymentService.getAvailableBalance(seller) : BigDecimal.ZERO;
        BigDecimal escrowBalance = seller != null ? paymentService.getEscrowBalance(seller) : BigDecimal.ZERO;

        List<Order> orders = seller != null ? orderService.getSellerOrders(seller) : List.of();
        double totalOmset = orders.stream()
            .filter(o -> o.getStatus() == OrderStatus.SELESAI || o.getStatus() == OrderStatus.DITERIMA || o.getStatus() == OrderStatus.DIBAYAR || o.getStatus() == OrderStatus.DIPROSES || o.getStatus() == OrderStatus.DIKIRIM)
            .mapToDouble(o -> o.getTotalAmount() != null ? o.getTotalAmount().doubleValue() : 0)
            .sum();

        // 2. Metrics Cards Grid (3 Kolom)
        Div statsGrid = new Div();
        statsGrid.getElement().getStyle()
            .set("display", "grid")
            .set("grid-template-columns", "repeat(auto-fit, minmax(280px, 1fr))")
            .set("gap", "20px");

        // Card 1: Saldo Siap Ditarik (Navy Hero Card)
        Div cardAvailable = new Div();
        cardAvailable.getElement().getStyle()
            .set("background", "linear-gradient(135deg, #001934 0%, #0A3D7A 100%)")
            .set("border-radius", "16px")
            .set("padding", "24px")
            .set("color", "#FFFFFF")
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("justify-content", "space-between")
            .set("box-shadow", "0 4px 20px rgba(0, 25, 52, 0.12)");

        Div availTop = new Div();
        Span availLabel = new Span("SALDO SIAP DITARIK");
        availLabel.getElement().getStyle().set("font-size", "11px").set("font-weight", "800").set("color", "#F5C45E").set("letter-spacing", "0.5px");
        H3 availVal = new H3("Rp " + String.format("%,.0f", availableBalance));
        availVal.getElement().getStyle().set("font-size", "28px").set("font-weight", "900").set("color", "#FFFFFF").set("margin", "6px 0 12px 0");
        Span availHint = new Span("Dapat langsung dicairkan kapan saja");
        availHint.getElement().getStyle().set("font-size", "12px").set("color", "rgba(255,255,255,0.75)");
        availTop.add(availLabel, availVal, availHint);

        Button btnWithdraw = new Button("Tarik Saldo Sekarang", VaadinIcon.WALLET.create(), e -> {
            if (seller != null) {
                openWithdrawDialog(seller, availableBalance);
            }
        });
        btnWithdraw.getElement().getStyle()
            .set("background", "#F5C45E")
            .set("color", "#001934")
            .set("font-weight", "800")
            .set("font-size", "13px")
            .set("border-radius", "10px")
            .set("border", "none")
            .set("padding", "12px 18px")
            .set("margin-top", "20px")
            .set("cursor", "pointer")
            .set("width", "100%");

        cardAvailable.add(availTop, btnWithdraw);

        // Card 2: Dana Escrow Tertahan (Light Blue Card)
        Div cardEscrow = new Div();
        cardEscrow.getElement().getStyle()
            .set("background", "#FFFFFF")
            .set("border", "1px solid #BFDBFE")
            .set("border-radius", "16px")
            .set("padding", "24px")
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("justify-content", "space-between");

        Div escTop = new Div();
        Span escLabel = new Span("DANA ESCROW TERIKAT PESANAN");
        escLabel.getElement().getStyle().set("font-size", "11px").set("font-weight", "800").set("color", "#2563EB").set("letter-spacing", "0.5px");
        H3 escVal = new H3("Rp " + String.format("%,.0f", escrowBalance));
        escVal.getElement().getStyle().set("font-size", "28px").set("font-weight", "900").set("color", "#001934").set("margin", "6px 0 12px 0");
        Span escHint = new Span("Dari pesanan yang sedang diproses atau dikirim. Otomatis cair setelah pembeli konfirmasi barang diterima.");
        escHint.getElement().getStyle().set("font-size", "12px").set("color", "#64748B").set("line-height", "1.4");
        escTop.add(escLabel, escVal, escHint);
        cardEscrow.add(escTop);

        // Card 3: Total Omset Kotor Penjualan
        Div cardOmset = new Div();
        cardOmset.getElement().getStyle()
            .set("background", "#FFFFFF")
            .set("border", "1px solid #E2E8F0")
            .set("border-radius", "16px")
            .set("padding", "24px")
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("justify-content", "space-between");

        Div omsTop = new Div();
        Span omsLabel = new Span("TOTAL OMSET PENJUALAN");
        omsLabel.getElement().getStyle().set("font-size", "11px").set("font-weight", "800").set("color", "#64748B").set("letter-spacing", "0.5px");
        H3 omsVal = new H3("Rp " + String.format("%,.0f", totalOmset));
        omsVal.getElement().getStyle().set("font-size", "28px").set("font-weight", "900").set("color", "#001934").set("margin", "6px 0 12px 0");
        Span omsHint = new Span("Akumulasi transaksi penjualan toko di ReWear SMKN 24.");
        omsHint.getElement().getStyle().set("font-size", "12px").set("color", "#64748B");
        omsTop.add(omsLabel, omsVal, omsHint);
        cardOmset.add(omsTop);

        statsGrid.add(cardAvailable, cardEscrow, cardOmset);
        wrapper.add(statsGrid);

        // 3. Payout Requests History Section
        Div payoutSection = new Div();
        payoutSection.getElement().getStyle()
            .set("background", "#FFFFFF")
            .set("border", "1px solid #E2E8F0")
            .set("border-radius", "16px")
            .set("padding", "24px");

        H3 payoutTitle = new H3("Riwayat Penarikan Saldo (Payout)");
        payoutTitle.getElement().getStyle().set("font-size", "18px").set("font-weight", "800").set("color", "#001934").set("margin", "0 0 16px 0");
        payoutSection.add(payoutTitle);

        List<SellerPayout> payouts = seller != null ? paymentService.getSellerPayouts(seller) : List.of();
        if (payouts.isEmpty()) {
            Div empty = new Div(new Paragraph("Belum ada riwayat penarikan dana. Klik tombol 'Tarik Saldo Sekarang' di atas untuk mencairkan hasil penjualan Anda."));
            empty.getElement().getStyle().set("color", "#64748B").set("font-size", "13px").set("text-align", "center").set("padding", "24px 0");
            payoutSection.add(empty);
        } else {
            Grid<SellerPayout> payoutGrid = new Grid<>(SellerPayout.class, false);
            payoutGrid.addColumn(SellerPayout::getReferenceNumber).setHeader("No. Tiket").setAutoWidth(true);
            payoutGrid.addColumn(p -> p.getCreatedAt() != null ? p.getCreatedAt().format(DATE_FMT) : "-").setHeader("Tanggal").setAutoWidth(true);
            payoutGrid.addColumn(p -> {
                if (p.getBankAccount() != null) {
                    return p.getBankAccount().getBankName() + " (" + p.getBankAccount().getAccountNumber() + ")";
                }
                return "-";
            }).setHeader("Tujuan Transfer").setAutoWidth(true);
            payoutGrid.addColumn(p -> "Rp " + String.format("%,.0f", p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO)).setHeader("Nominal").setAutoWidth(true);
            payoutGrid.addComponentColumn(this::buildPayoutStatusBadge).setHeader("Status").setAutoWidth(true);
            payoutGrid.addColumn(p -> p.getAdminNotes() != null ? p.getAdminNotes() : "-").setHeader("Catatan Admin").setAutoWidth(true);

            payoutGrid.setItems(payouts);
            payoutSection.add(payoutGrid);
        }

        wrapper.add(payoutSection);
        return wrapper;
    }

    private void openWithdrawDialog(User seller, BigDecimal availableBalance) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Pencairan Saldo ReWearPay Penjual");
        dialog.setWidth("480px");

        VerticalLayout formLayout = new VerticalLayout();
        formLayout.setPadding(false);
        formLayout.setSpacing(true);

        Div saldoInfo = new Div();
        saldoInfo.getElement().getStyle()
            .set("background", "#F0FDF4")
            .set("border", "1px solid #BBF7D0")
            .set("padding", "12px 16px")
            .set("border-radius", "8px")
            .set("font-size", "13px")
            .set("color", "#166534")
            .set("margin-bottom", "12px");
        saldoInfo.setText("Saldo Anda yang siap dicairkan: Rp " + String.format("%,.0f", availableBalance));

        ComboBox<String> bankCombo = new ComboBox<>("Bank atau E-Wallet Tujuan");
        bankCombo.setItems("Bank BCA", "Bank Mandiri", "Bank BRI", "Bank BNI", "GoPay", "OVO", "DANA", "ShopeePay");
        bankCombo.setValue("Bank BCA");
        bankCombo.setWidthFull();

        TextField accNumField = new TextField("Nomor Rekening / No. HP E-Wallet");
        accNumField.setPlaceholder("Contoh: 1234567890");
        accNumField.setWidthFull();

        TextField holderField = new TextField("Nama Pemilik Rekening / Akun");
        holderField.setValue(seller.getFullName() != null ? seller.getFullName() : "");
        holderField.setWidthFull();

        NumberField amountField = new NumberField("Nominal Penarikan (Rp)");
        amountField.setPlaceholder("Min. Rp 10.000");
        amountField.setMin(10000);
        amountField.setMax(availableBalance.doubleValue());
        amountField.setValue(availableBalance.compareTo(BigDecimal.valueOf(10000)) >= 0 ? availableBalance.doubleValue() : 10000.0);
        amountField.setWidthFull();

        // Quick nominal chips
        HorizontalLayout quickChips = new HorizontalLayout();
        quickChips.setSpacing(true);
        quickChips.getElement().getStyle().set("gap", "8px").set("flex-wrap", "wrap").set("margin-top", "4px");

        Button chipAll = new Button("Tarik Semua", e -> amountField.setValue(availableBalance.doubleValue()));
        chipAll.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        chipAll.getStyle().set("background", "#EFF6FF").set("color", "#1E40AF").set("font-weight", "700");

        Button chip50 = new Button("50rb", e -> amountField.setValue(50000.0));
        chip50.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);

        Button chip100 = new Button("100rb", e -> amountField.setValue(100000.0));
        chip100.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);

        Button chip500 = new Button("500rb", e -> amountField.setValue(500000.0));
        chip500.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);

        quickChips.add(chipAll, chip50, chip100, chip500);

        Paragraph infoNote = new Paragraph("Dana akan diproses dan ditransfer oleh Admin ReWear dalam 1x24 jam kerja.");
        infoNote.getElement().getStyle().set("font-size", "12px").set("color", "#64748B").set("margin", "10px 0 0 0");

        formLayout.add(saldoInfo, bankCombo, accNumField, holderField, amountField, quickChips, infoNote);

        Button btnCancel = new Button("Batal", e -> dialog.close());
        btnCancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button btnConfirm = new Button("Ajukan Penarikan", e -> {
            String bank = bankCombo.getValue();
            String accNum = accNumField.getValue();
            String holder = holderField.getValue();
            Double amt = amountField.getValue();

            if (bank == null || accNum == null || accNum.isBlank() || holder == null || holder.isBlank() || amt == null) {
                Notification.show("Harap lengkapi semua kolom penarikan.", 3000, Notification.Position.TOP_CENTER);
                return;
            }

            if (amt < 10000) {
                Notification.show("Minimal penarikan adalah Rp 10.000", 3000, Notification.Position.TOP_CENTER);
                return;
            }

            if (BigDecimal.valueOf(amt).compareTo(availableBalance) > 0) {
                Notification.show("Saldo Anda tidak mencukupi untuk penarikan sebesar Rp " + String.format("%,.0f", amt), 3000, Notification.Position.TOP_CENTER);
                return;
            }

            try {
                SellerPayout ticket = paymentService.requestPayout(seller, bank, accNum, holder, BigDecimal.valueOf(amt));
                Notification.show("Permohonan penarikan dana Rp " + String.format("%,.0f", amt) + " berhasil diajukan (Tiket: " + ticket.getReferenceNumber() + ")", 4000, Notification.Position.TOP_CENTER);
                dialog.close();
                buildMainLayout();
            } catch (Exception ex) {
                Notification.show("Gagal mengajukan penarikan: " + ex.getMessage(), 3500, Notification.Position.TOP_CENTER);
            }
        });
        btnConfirm.getStyle().set("background", "#001934").set("color", "#F5C45E").set("font-weight", "800").set("border-radius", "8px");

        dialog.add(formLayout);
        dialog.getFooter().add(btnCancel, btnConfirm);
        dialog.open();
    }

    private Span buildPayoutStatusBadge(SellerPayout payout) {
        Span badge = new Span();
        PayoutStatus status = payout != null ? payout.getStatus() : PayoutStatus.REQUESTED;

        switch (status) {
            case REQUESTED -> {
                badge.setText("Menunggu Persetujuan");
                badge.getStyle().set("background", "#FEF3C7").set("color", "#92400E");
            }
            case PROCESSING -> {
                badge.setText("Sedang Diproses");
                badge.getStyle().set("background", "#EFF6FF").set("color", "#1E40AF");
            }
            case COMPLETED -> {
                badge.setText("Selesai Ditransfer");
                badge.getStyle().set("background", "#DCFCE7").set("color", "#166534");
            }
            case REJECTED -> {
                badge.setText("Ditolak Admin");
                badge.getStyle().set("background", "#FEF2F2").set("color", "#991B1B");
            }
        }

        badge.getStyle()
            .set("font-size", "11px")
            .set("font-weight", "700")
            .set("padding", "4px 10px")
            .set("border-radius", "20px");

        return badge;
    }

    private Component renderPengaturanTab() {
        Div wrapper = new Div();
        H2 title = new H2("Pengaturan Toko");
        title.getElement().getStyle().set("font-size", "24px").set("font-weight", "800").set("color", "#001934").set("margin", "0 0 8px 0");

        User seller = AuthGuard.getCurrentUser();
        String currentName = seller != null && seller.getFullName() != null ? seller.getFullName() : "Penjual SMKN 24";

        Div form = new Div();
        form.getElement().getStyle().set("display", "flex").set("flex-direction", "column").set("gap", "16px").set("max-width", "500px");

        TextField txtShopName = new TextField("Nama Penjual", currentName, "");
        txtShopName.setWidthFull();

        TextField txtBank = new TextField("Rekening / E-Wallet Pencairan Escrow", "BCA 1234567890 a.n. " + currentName, "");
        txtBank.setWidthFull();

        Button btnSave = new Button("Simpan Pengaturan", e -> Notification.show("Pengaturan toko berhasil diperbarui!", 2500, Notification.Position.TOP_CENTER));
        btnSave.getElement().getStyle()
            .set("background", "#001934").set("color", "#FFFFFF").set("font-weight", "700")
            .set("border-radius", "8px").set("padding", "12px").set("cursor", "pointer");

        form.add(txtShopName, txtBank, btnSave);
        wrapper.add(title, form);
        return wrapper;
    }
}
