package com.example.application.views.seller;

import com.example.application.model.product.Product;
import com.example.application.service.product.ProductService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;

import java.util.List;

@Route(value = "seller/dashboard", layout = MainLayout.class)
@RouteAlias(value = "seller", layout = MainLayout.class)
@RouteAlias(value = "dashboard-penjual", layout = MainLayout.class)
@PageTitle("Dashboard Penjual | ReWear SMKN 24")
@Menu(order = 2, icon = "line-awesome/svg/store-solid.svg", title = "Dashboard Penjual")
public class SellerDashboardView extends VerticalLayout implements BeforeEnterObserver {

    private final ProductService productService;

    // Active tab state: "ringkasan", "produk", "pesanan", "laporan", "pengaturan"
    private String activeTab = "ringkasan";

    private final Div contentContainer = new Div();
    private final Div rightContentArea = new Div();

    public SellerDashboardView(ProductService productService) {
        this.productService = productService;

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

        Span statusSpan = new Span("● Mode Jual Aktif");
        statusSpan.getElement().getStyle()
            .set("font-size", "12px")
            .set("color", "#D97706")
            .set("font-weight", "700");

        headerCard.add(title, statusSpan);

        // Navigation Menu Links
        Div navList = new Div();
        navList.getElement().getStyle()
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("gap", "6px");

        navList.add(createNavItem("Ringkasan", "ringkasan", VaadinIcon.GRID_SMALL));
        navList.add(createNavItem("Produk Saya", "produk", VaadinIcon.PACKAGE));
        navList.add(createNavItem("Pesanan Masuk", "pesanan", VaadinIcon.CART));
        navList.add(createNavItem("Laporan", "laporan", VaadinIcon.CHART));
        navList.add(createNavItem("Pengaturan Toko", "pengaturan", VaadinIcon.COG));

        // Bottom Action Button: + Tambah Produk
        Button btnAddProduct = new Button(" + Tambah Produk");
        btnAddProduct.getElement().getStyle()
            .set("background", "#001934")
            .set("color", "#FFFFFF")
            .set("font-weight", "700")
            .set("font-size", "14px")
            .set("border-radius", "10px")
            .set("border", "none")
            .set("padding", "14px")
            .set("width", "100%")
            .set("cursor", "pointer")
            .set("margin-top", "auto")
            .set("box-shadow", "0 4px 12px rgba(0,25,52,0.15)");
        btnAddProduct.addClickListener(e -> UI.getCurrent().navigate("sell"));

        sidebar.add(headerCard, navList, btnAddProduct);
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
    // RIGHT CONTENT CONTAINER SWITCHER
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
    // TAB 1: RINGKASAN (DASHBOARD OVERVIEW)
    // ==========================================

    private Component renderRingkasanTab() {
        Div wrapper = new Div();
        wrapper.getElement().getStyle()
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("gap", "24px");

        // 1. Top Greeting Header Bar
        HorizontalLayout greetingRow = new HorizontalLayout();
        greetingRow.setWidthFull();
        greetingRow.setAlignItems(FlexComponent.Alignment.CENTER);
        greetingRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Div greetLeft = new Div();
        H2 greetTitle = new H2("Selamat Datang, Admin ReWear");
        greetTitle.getElement().getStyle()
            .set("font-size", "26px")
            .set("font-weight", "800")
            .set("color", "#001934")
            .set("margin", "0 0 4px 0");

        Paragraph greetSub = new Paragraph("Berikut ringkasan performa tokomu hari ini.");
        greetSub.getElement().getStyle()
            .set("font-size", "14px")
            .set("color", "#64748B")
            .set("margin", "0");

        greetLeft.add(greetTitle, greetSub);

        // Date Filter Selector (7 Hari Terakhir)
        Button btnPeriod = new Button("🗓️ 7 Hari Terakhir");
        btnPeriod.getElement().getStyle()
            .set("background", "#EFF6FF")
            .set("color", "#1E40AF")
            .set("font-weight", "700")
            .set("font-size", "13px")
            .set("border-radius", "8px")
            .set("border", "1px solid #BFDBFE")
            .set("padding", "8px 16px")
            .set("cursor", "pointer");

        greetingRow.add(greetLeft, btnPeriod);
        wrapper.add(greetingRow);

        // 2. 4 Metrics Cards Grid (Total Penjualan | Produk Terlaris | Kunjungan | Status Stok)
        Div statsGrid = new Div();
        statsGrid.getElement().getStyle()
            .set("display", "grid")
            .set("grid-template-columns", "repeat(4, 1fr)")
            .set("gap", "16px");

        statsGrid.add(createStatCard("TOTAL PENJUALAN", "Rp 4.250.000", "+12%", "#15803D", "#DCFCE7", "💳"));
        statsGrid.add(createStatCard("PRODUK TERLARIS", "Jaket Vintage SMKN 24", "Top 1", "#475569", "#F1F5F9", "📈"));
        statsGrid.add(createStatCard("KUNJUNGAN PRODUK", "12,840", "+5.2k", "#1E40AF", "#DBEAFE", "👁️"));
        statsGrid.add(createStatCard("STATUS STOK", "42 Tersedia", "3 Kritis", "#B91C1C", "#FEE2E2", "📋"));

        wrapper.add(statsGrid);

        // 3. Middle Performance Section: Grafik Penjualan Mingguan (65%) & Kategori Terlaris (35%)
        HorizontalLayout middleRow = new HorizontalLayout();
        middleRow.setWidthFull();
        middleRow.setSpacing(true);
        middleRow.getElement().getStyle().set("gap", "20px");

        // Left 65%: Sales Chart Card
        Div chartCard = createSalesChartCard();
        chartCard.getElement().getStyle().set("flex", "1.8");

        // Right 35%: Category Breakdown Donut Card
        Div donutCard = createCategoryBreakdownCard();
        donutCard.getElement().getStyle().set("flex", "1");

        middleRow.add(chartCard, donutCard);
        wrapper.add(middleRow);

        // 4. Bottom Transactions Table Card (Riwayat Transaksi)
        Div txCard = createTransactionsTableCard();
        wrapper.add(txCard);

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
            .set("justify-content", "space-between")
            .set("min-height", "110px");

        // Top Row: Icon + Badge
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

        // Bottom Meta: Label & Value
        Div meta = new Div();
        meta.getElement().getStyle().set("margin-top", "12px");

        Span lbl = new Span(label);
        lbl.getElement().getStyle().set("font-size", "11px").set("font-weight", "700").set("color", "#64748B").set("letter-spacing", "0.5px").set("display", "block").set("margin-bottom", "4px");

        Span val = new Span(value);
        val.getElement().getStyle().set("font-size", value.length() > 18 ? "15px" : "22px").set("font-weight", "800").set("color", "#001934").set("line-height", "1.2");

        meta.add(lbl, val);
        card.add(top, meta);
        return card;
    }

    private Div createSalesChartCard() {
        Div card = new Div();
        card.getElement().getStyle()
            .set("background", "#FFFFFF")
            .set("border", "1px solid #E2E8F0")
            .set("border-radius", "16px")
            .set("padding", "24px")
            .set("box-shadow", "0 2px 8px rgba(0,25,52,0.03)");

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        H4 title = new H4("Grafik Penjualan Mingguan");
        title.getElement().getStyle().set("font-size", "17px").set("font-weight", "800").set("color", "#001934").set("margin", "0");

        Span dots = new Span("⋮");
        dots.getElement().getStyle().set("font-size", "18px").set("color", "#94A3B8").set("cursor", "pointer");

        header.add(title, dots);

        // Interactive Bar Chart Graphic Visualization
        Div chartArea = new Div();
        chartArea.getElement().getStyle()
            .set("margin-top", "24px")
            .set("display", "flex")
            .set("align-items", "flex-end")
            .set("justify-content", "space-between")
            .set("height", "180px")
            .set("padding-top", "20px")
            .set("border-bottom", "1px solid #E2E8F0");

        String[] days = {"Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min"};
        int[] heights = {45, 60, 40, 90, 75, 85, 55};
        boolean[] isKam = {false, false, false, true, false, false, false};

        for (int i = 0; i < days.length; i++) {
            Div col = new Div();
            col.getElement().getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("align-items", "center")
                .set("gap", "8px")
                .set("flex", "1");

            Div bar = new Div();
            bar.getElement().getStyle()
                .set("width", "28px")
                .set("height", heights[i] + "%")
                .set("border-radius", "6px 6px 0 0")
                .set("background", isKam[i] ? "#001934" : "#DBEAFE")
                .set("transition", "all 0.2s ease");

            Span dayLabel = new Span(days[i]);
            dayLabel.getElement().getStyle()
                .set("font-size", "12px")
                .set("font-weight", isKam[i] ? "800" : "600")
                .set("color", isKam[i] ? "#001934" : "#64748B");

            col.add(bar, dayLabel);
            chartArea.add(col);
        }

        card.add(header, chartArea);
        return card;
    }

    private Div createCategoryBreakdownCard() {
        Div card = new Div();
        card.getElement().getStyle()
            .set("background", "#FFFFFF")
            .set("border", "1px solid #E2E8F0")
            .set("border-radius", "16px")
            .set("padding", "24px")
            .set("box-shadow", "0 2px 8px rgba(0,25,52,0.03)");

        H4 title = new H4("Kategori Terlaris");
        title.getElement().getStyle().set("font-size", "17px").set("font-weight", "800").set("color", "#001934").set("margin", "0 0 16px 0");

        // SVG Donut Chart Visual
        Div donutWrap = new Div();
        donutWrap.getElement().setProperty("innerHTML",
            "<div style='display:flex;justify-content:center;align-items:center;margin:12px 0;position:relative;'>" +
            "<svg width='140' height='140' viewBox='0 0 36 36'>" +
            "<path d='M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831' fill='none' stroke='#001934' stroke-width='3.8' stroke-dasharray='60, 100'/>" +
            "<path d='M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831' fill='none' stroke='#F5C45E' stroke-width='3.8' stroke-dasharray='25, 100' stroke-dashoffset='-60'/>" +
            "<path d='M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831' fill='none' stroke='#FF9E59' stroke-width='3.8' stroke-dasharray='15, 100' stroke-dashoffset='-85'/>" +
            "</svg>" +
            "<div style='position:absolute;text-align:center;'>" +
            "<span style='font-size:18px;font-weight:800;color:#001934;display:block;'>100%</span>" +
            "<span style='font-size:10px;color:#64748B;'>Total</span>" +
            "</div></div>"
        );

        // Legend Breakdown Below
        Div legend = new Div();
        legend.getElement().getStyle().set("display", "flex").set("flex-direction", "column").set("gap", "8px").set("margin-top", "12px");

        legend.add(createLegendRow("● Atasan & Kaos", "60%", "#001934"));
        legend.add(createLegendRow("● Aksesoris", "25%", "#F5C45E"));
        legend.add(createLegendRow("● Tas & Sepatu", "15%", "#FF9E59"));

        card.add(title, donutWrap, legend);
        return card;
    }

    private Div createLegendRow(String label, String percent, String dotColor) {
        Div row = new Div();
        row.getElement().getStyle()
            .set("display", "flex").set("justify-content", "space-between")
            .set("font-size", "13px").set("font-weight", "600").set("color", "#475569");

        Span l = new Span(label);
        l.getElement().getStyle().set("color", dotColor);

        Span p = new Span(percent);
        p.getElement().getStyle().set("font-weight", "800").set("color", "#001934");

        row.add(l, p);
        return row;
    }

    private Div createTransactionsTableCard() {
        Div card = new Div();
        card.getElement().getStyle()
            .set("background", "#FFFFFF")
            .set("border", "1px solid #E2E8F0")
            .set("border-radius", "16px")
            .set("overflow", "hidden")
            .set("box-shadow", "0 2px 8px rgba(0,25,52,0.03)");

        // Table Header Bar
        HorizontalLayout tableHeader = new HorizontalLayout();
        tableHeader.setWidthFull();
        tableHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        tableHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        tableHeader.getElement().getStyle().set("padding", "20px 24px").set("border-bottom", "1px solid #F1F5F9");

        H4 title = new H4("Riwayat Transaksi");
        title.getElement().getStyle().set("font-size", "18px").set("font-weight", "800").set("color", "#001934").set("margin", "0");

        HorizontalLayout controls = new HorizontalLayout();
        controls.setSpacing(true);

        Button btnFilter = new Button("Semua Status ∨");
        btnFilter.getElement().getStyle()
            .set("background", "#EFF6FF").set("color", "#1E40AF").set("font-weight", "600")
            .set("font-size", "12px").set("border-radius", "8px").set("border", "none").set("padding", "6px 14px").set("cursor", "pointer");

        Button btnExport = new Button("Ekspor CSV");
        btnExport.getElement().getStyle()
            .set("background", "#EFF6FF").set("color", "#1E40AF").set("font-weight", "700")
            .set("font-size", "12px").set("border-radius", "8px").set("border", "none").set("padding", "6px 14px").set("cursor", "pointer");
        btnExport.addClickListener(e -> Notification.show("Menyiapkan data transaksi CSV...", 2000, Notification.Position.TOP_CENTER));

        controls.add(btnFilter, btnExport);
        tableHeader.add(title, controls);

        // Table Grid Container
        Div tableContainer = new Div();
        tableContainer.getElement().getStyle().set("width", "100%").set("overflow-x", "auto");

        // Header Columns Row
        Div headerRow = new Div();
        headerRow.getElement().getStyle()
            .set("display", "grid")
            .set("grid-template-columns", "1.2fr 2.5fr 1.2fr 1.5fr 1.2fr 1fr")
            .set("background", "#F8FAFC")
            .set("padding", "12px 24px")
            .set("font-size", "12px")
            .set("font-weight", "700")
            .set("color", "#64748B")
            .set("border-bottom", "1px solid #E2E8F0");

        headerRow.add(new Span("ID Pesanan"), new Span("Produk"), new Span("Tanggal"), new Span("Pelanggan"), new Span("Total"), new Span("Status"));
        tableContainer.add(headerRow);

        // Data Rows
        tableContainer.add(createTxGridRow("#RW-240101", "images/buku.jpeg", "Hoodie SMKN 24 Edition", "12 Mei 2024", "Budi Sudarsono", "Rp 150.000", "Selesai", true));
        tableContainer.add(createTxGridRow("#RW-240102", "images/colokan.webp", "Tote Bag Eco-Green", "11 Mei 2024", "Siti Rahmawati", "Rp 45.000", "Proses", false));
        tableContainer.add(createTxGridRow("#RW-240103", "images/kipas.jpg", "Jaket Varsity Premium", "10 Mei 2024", "Andi Wijaya", "Rp 275.000", "Selesai", true));

        // Footer Row: Lihat Semua Transaksi
        Div footer = new Div();
        footer.getElement().getStyle()
            .set("padding", "14px").set("text-align", "center").set("background", "#F8FAFC").set("border-top", "1px solid #F1F5F9");

        Anchor btnAll = new Anchor("#", "Lihat Semua Transaksi");
        btnAll.getElement().getStyle().set("font-size", "13px").set("font-weight", "700").set("color", "#001934").set("text-decoration", "none");
        btnAll.getElement().addEventListener("click", e -> activeTab = "pesanan");

        footer.add(btnAll);

        card.add(tableHeader, tableContainer, footer);
        return card;
    }

    private Div createTxGridRow(String id, String imgUrl, String pName, String date, String customer, String total, String status, boolean isSuccess) {
        Div row = new Div();
        row.getElement().getStyle()
            .set("display", "grid")
            .set("grid-template-columns", "1.2fr 2.5fr 1.2fr 1.5fr 1.2fr 1fr")
            .set("align-items", "center")
            .set("padding", "14px 24px")
            .set("font-size", "13px")
            .set("border-bottom", "1px solid #F1F5F9");

        // ID
        Span spanId = new Span(id);
        spanId.getElement().getStyle().set("font-weight", "700").set("color", "#001934");

        // Product Cell
        HorizontalLayout pCell = new HorizontalLayout();
        pCell.setAlignItems(FlexComponent.Alignment.CENTER);
        pCell.setSpacing(true);

        Image img = new Image(imgUrl, pName);
        img.getElement().getStyle().set("width", "36px").set("height", "36px").set("border-radius", "6px").set("object-fit", "cover");

        Span name = new Span(pName);
        name.getElement().getStyle().set("font-weight", "700").set("color", "#001934");

        pCell.add(img, name);

        // Date
        Span spanDate = new Span(date);
        spanDate.getElement().getStyle().set("color", "#64748B");

        // Customer
        Span spanCust = new Span(customer);
        spanCust.getElement().getStyle().set("color", "#334155").set("font-weight", "600");

        // Total
        Span spanTotal = new Span(total);
        spanTotal.getElement().getStyle().set("font-weight", "800").set("color", "#001934");

        // Status Badge
        Span stBadge = new Span(status);
        stBadge.getElement().getStyle()
            .set("background", isSuccess ? "#DCFCE7" : "#DBEAFE")
            .set("color", isSuccess ? "#15803D" : "#1D4ED8")
            .set("font-weight", "800")
            .set("font-size", "11px")
            .set("padding", "4px 10px")
            .set("border-radius", "9999px")
            .set("display", "inline-block")
            .set("text-align", "center");

        row.add(spanId, pCell, spanDate, spanCust, spanTotal, stBadge);
        return row;
    }

    // ==========================================
    // OTHER TABS (PRODUK, PESANAN, LAPORAN, PENGATURAN)
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

        // Product Table Grid
        Grid<Product> grid = new Grid<>(Product.class, false);
        grid.addColumn(Product::getName).setHeader("Nama Produk");
        grid.addColumn(p -> "Rp " + String.format("%,.0f", p.getPrice())).setHeader("Harga");
        grid.addColumn(Product::getStock).setHeader("Stok");
        grid.addColumn(Product::getSoldCount).setHeader("Terjual");
        grid.setItems(productService.findActiveProducts());

        wrapper.add(grid);
        return wrapper;
    }

    private Component renderPesananMasukTab() {
        Div wrapper = new Div();
        H2 title = new H2("🛍️ Pesanan Masuk");
        title.getElement().getStyle().set("font-size", "24px").set("font-weight", "800").set("color", "#001934").set("margin", "0 0 8px 0");
        Paragraph sub = new Paragraph("Kelola pemesanan dari pembeli untuk segera diproses dan dikirim.");
        sub.getElement().getStyle().set("color", "#64748B").set("margin", "0 0 24px 0");

        wrapper.add(title, sub, createTransactionsTableCard());
        return wrapper;
    }

    private Component renderLaporanTab() {
        Div wrapper = new Div();
        H2 title = new H2("📊 Laporan Keuangan");
        title.getElement().getStyle().set("font-size", "24px").set("font-weight", "800").set("color", "#001934").set("margin", "0 0 8px 0");

        Div card = new Div();
        card.getElement().getStyle()
            .set("background", "#FFFFFF").set("border-radius", "16px").set("padding", "24px").set("border", "1px solid #E2E8F0");

        H4 subTitle = new H4("Pendapatan Bersih Bulan Ini");
        subTitle.getElement().getStyle().set("color", "#64748B").set("margin", "0 0 4px 0");

        H3 totalVal = new H3("Rp 4.250.000");
        totalVal.getElement().getStyle().set("font-size", "32px").set("font-weight", "800").set("color", "#001934").set("margin", "0 0 16px 0");

        card.add(subTitle, totalVal);
        wrapper.add(title, card);
        return wrapper;
    }

    private Component renderPengaturanTab() {
        Div wrapper = new Div();
        H2 title = new H2("⚙️ Pengaturan Toko");
        title.getElement().getStyle().set("font-size", "24px").set("font-weight", "800").set("color", "#001934").set("margin", "0 0 8px 0");

        Div form = new Div();
        form.getElement().getStyle().set("display", "flex").set("flex-direction", "column").set("gap", "16px").set("max-width", "500px");

        TextField txtShopName = new TextField("Nama Toko / Penjual", "Toko SMKN 24 Jakarta", "");
        TextField txtBank = new TextField("Rekening / E-Wallet Pencairan Escrow", "BCA 1234567890 a.n. ReWear Seller", "");

        Button btnSave = new Button("Simpan Pengaturan", e -> Notification.show("Pengaturan toko berhasil disimpan!"));
        btnSave.getElement().getStyle()
            .set("background", "#001934").set("color", "#FFFFFF").set("font-weight", "700")
            .set("border-radius", "8px").set("padding", "12px").set("cursor", "pointer");

        form.add(txtShopName, txtBank, btnSave);
        wrapper.add(title, form);
        return wrapper;
    }
}
