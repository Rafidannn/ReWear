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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;

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

    // Active tab state: "ringkasan", "produk", "pesanan", "laporan", "pengaturan"
    private String activeTab = "ringkasan";

    private final Div contentContainer = new Div();
    private final Div rightContentArea = new Div();

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    public SellerDashboardView(ProductService productService, OrderService orderService) {
        this.productService = productService;
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
        if (!AuthGuard.requireLogin(UI.getCurrent())) return;
        buildMainLayout();
    }

    private void buildMainLayout() {
        contentContainer.removeAll();

        HorizontalLayout gridLayout = new HorizontalLayout();
        gridLayout.setWidthFull();
        gridLayout.setSpacing(true);
        gridLayout.getElement().getStyle().set("gap", "28px");

        // LEFT SIDEBAR NAVIGATION
        Div leftSidebar = createLeftSidebar();

        // RIGHT CONTENT AREA
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

        statsGrid.add(createStatCard("TOTAL OMSET PENJUALAN", "Rp " + String.format("%,.0f", totalPenjualan), "Real DB", "#15803D", "#DCFCE7", "💰"));
        statsGrid.add(createStatCard("TOTAL PESANAN MASUK", totalPesanan + " Transaksi", orders.isEmpty() ? "Belum ada" : "Aktif", "#1E40AF", "#DBEAFE", "📦"));
        statsGrid.add(createStatCard("PRODUK DIJUAL", totalProduk + " Barang", "Katalog", "#475569", "#F1F5F9", "🏷️"));

        wrapper.add(statsGrid);

        // Section Pesanan Terbaru
        Div sectionTitle = new Div();
        H3 h3 = new H3("📋 Pesanan Terbaru dari Pembeli");
        h3.getStyle().set("font-size", "18px").set("font-weight", "800").set("color", "#001934").set("margin", "16px 0 12px 0");
        sectionTitle.add(h3);
        wrapper.add(sectionTitle);

        wrapper.add(buildSellerOrdersList(orders));

        return wrapper;
    }

    private Div createStatCard(String label, String value, String badgeText, String badgeColor, String badgeBg, String emojiIcon) {
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

        Div iconBox = new Div(new Span(emojiIcon));
        iconBox.getElement().getStyle()
            .set("width", "36px").set("height", "36px").set("border-radius", "10px").set("background", "#EFF6FF")
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
        H2 title = new H2("🛍️ Kelola Pesanan Masuk");
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
                "<div style='font-size:44px;margin-bottom:12px;'>📬</div>" +
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
            Div bRow = new Div(new Span("👤 Pembeli: "), new Span(buyerName));
            bRow.getStyle().set("font-weight", "700").set("color", "#001934").set("margin-bottom", "4px");

            Div aRow = new Div(new Span("📍 Alamat Tujuan: "), new Span(order.getShippingAddress() != null ? order.getShippingAddress() : "-"));
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
                Button btnProses = new Button("⚙️ Terima & Diproses", e -> {
                    orderService.updateOrderStatus(order, OrderStatus.DIPROSES, "Pesanan diterima dan sedang diproses penjual.", sellerActor);
                    Notification success = Notification.show("✅ Pesanan #" + order.getOrderNumber() + " berhasil diproses!", 2500, Notification.Position.TOP_CENTER);
                    success.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    buildMainLayout();
                });
                btnProses.getStyle().set("background", "#001934").set("color", "#FFFFFF").set("font-size", "12px").set("font-weight", "700").set("border-radius", "8px");
                actionBtns.add(btnProses);
            }

            if (order.getStatus() == OrderStatus.DIPROSES) {
                Button btnKirim = new Button("🚚 Kirim / Siap COD", e -> {
                    orderService.updateOrderStatus(order, OrderStatus.DIKIRIM, "Pesanan diserahkan ke pengiriman / titik temui COD.", sellerActor);
                    Notification success = Notification.show("🚚 Pesanan #" + order.getOrderNumber() + " ditandai dikirim!", 2500, Notification.Position.TOP_CENTER);
                    success.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    buildMainLayout();
                });
                btnKirim.getStyle().set("background", "#16A34A").set("color", "#FFFFFF").set("font-size", "12px").set("font-weight", "700").set("border-radius", "8px");
                actionBtns.add(btnKirim);
            }

            if (order.getStatus() == OrderStatus.DIKIRIM) {
                Button btnSelesai = new Button("🎉 Tandai Selesai", e -> {
                    orderService.updateOrderStatus(order, OrderStatus.SELESAI, "Pesanan dikonfirmasi selesai.", sellerActor);
                    Notification success = Notification.show("🎉 Pesanan #" + order.getOrderNumber() + " selesai!", 2500, Notification.Position.TOP_CENTER);
                    success.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    buildMainLayout();
                });
                btnSelesai.getStyle().set("background", "#16A34A").set("color", "#FFFFFF").set("font-size", "12px").set("font-weight", "700").set("border-radius", "8px");
                actionBtns.add(btnSelesai);
            }

            footer.add(totalBox, actionBtns);
            card.add(header, buyerBox, itemsBox, footer);
            container.add(card);
        }

        return container;
    }

    private Span buildStatusBadge(OrderStatus status) {
        Span badge = new Span();
        if (status == null) status = OrderStatus.MENUNGGU_PEMBAYARAN;

        switch (status) {
            case MENUNGGU_PEMBAYARAN -> {
                badge.setText("⏳ Menunggu Pembayaran");
                badge.getStyle().set("background", "#FEF3C7").set("color", "#92400E");
            }
            case DIBAYAR -> {
                badge.setText("✅ Dibayar");
                badge.getStyle().set("background", "#DCFCE7").set("color", "#166534");
            }
            case DIPROSES -> {
                badge.setText("⚙️ Diproses");
                badge.getStyle().set("background", "#EFF6FF").set("color", "#1E40AF");
            }
            case DIKIRIM -> {
                badge.setText("🚚 Dikirim / Siap COD");
                badge.getStyle().set("background", "#F0FDF4").set("color", "#15803D");
            }
            case DITERIMA -> {
                badge.setText("📬 Diterima");
                badge.getStyle().set("background", "#F0FDF4").set("color", "#15803D");
            }
            case SELESAI -> {
                badge.setText("🎉 Selesai");
                badge.getStyle().set("background", "#DCFCE7").set("color", "#166534");
            }
            case KOMPLAIN -> {
                badge.setText("⚠️ Komplain");
                badge.getStyle().set("background", "#FEF2F2").set("color", "#991B1B");
            }
            case DIBATALKAN -> {
                badge.setText("❌ Dibatalkan");
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

        H2 title = new H2("📦 Daftar Produk Saya");
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
        H2 title = new H2("📊 Laporan Keuangan Toko");
        title.getElement().getStyle().set("font-size", "24px").set("font-weight", "800").set("color", "#001934").set("margin", "0 0 8px 0");

        User seller = AuthGuard.getCurrentUser();
        List<Order> orders = seller != null ? orderService.getSellerOrders(seller) : List.of();

        double totalOmset = orders.stream()
            .filter(o -> o.getStatus() == OrderStatus.SELESAI || o.getStatus() == OrderStatus.DITERIMA || o.getStatus() == OrderStatus.DIBAYAR || o.getStatus() == OrderStatus.DIPROSES || o.getStatus() == OrderStatus.DIKIRIM)
            .mapToDouble(o -> o.getTotalAmount() != null ? o.getTotalAmount().doubleValue() : 0)
            .sum();

        Div card = new Div();
        card.getElement().getStyle()
            .set("background", "#FFFFFF").set("border-radius", "16px").set("padding", "24px").set("border", "1px solid #E2E8F0");

        H4 subTitle = new H4("Total Omset Real dari Database");
        subTitle.getElement().getStyle().set("color", "#64748B").set("margin", "0 0 4px 0");

        H3 totalVal = new H3("Rp " + String.format("%,.0f", totalOmset));
        totalVal.getElement().getStyle().set("font-size", "32px").set("font-weight", "800").set("color", "#001934").set("margin", "0 0 16px 0");

        card.add(subTitle, totalVal);
        wrapper.add(title, card);
        return wrapper;
    }

    private Component renderPengaturanTab() {
        Div wrapper = new Div();
        H2 title = new H2("⚙️ Pengaturan Toko");
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
