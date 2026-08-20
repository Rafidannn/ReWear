package com.example.application.views.admin;

import com.example.application.model.moderation.Report;
import com.example.application.model.moderation.ReportStatus;
import com.example.application.model.order.Order;
import com.example.application.model.order.OrderStatus;
import com.example.application.model.payment.PayoutStatus;
import com.example.application.model.payment.SellerPayout;
import com.example.application.model.product.Product;
import com.example.application.model.product.ProductStatus;
import com.example.application.model.user.AccountStatus;
import com.example.application.model.user.Role;
import com.example.application.model.user.School;
import com.example.application.model.user.User;
import com.example.application.service.moderation.ModerationService;
import com.example.application.service.order.OrderService;
import com.example.application.service.payment.PaymentService;
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

@Route(value = "admin", layout = MainLayout.class)
@RouteAlias(value = "panel-admin", layout = MainLayout.class)
@PageTitle("Panel Admin & Moderasi | ReWear SMKN 24")
@Menu(order = 4, icon = "line-awesome/svg/shield-alt-solid.svg", title = "Admin & Moderasi")
public class AdminDashboardView extends VerticalLayout implements BeforeEnterObserver {

    private final ModerationService moderationService;
    private final UserService userService;
    private final ProductService productService;
    private final OrderService orderService;
    private final PaymentService paymentService;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    private String activeTab = "overview";
    private final Div contentContainer = new Div();

    // Tab buttons references for active state highlighting
    private Button tabOverviewBtn;
    private Button tabUsersBtn;
    private Button tabProductsBtn;
    private Button tabReportsBtn;
    private Button tabOrdersBtn;
    private Button tabPayoutsBtn;

    public AdminDashboardView(ModerationService moderationService,
                              UserService userService,
                              ProductService productService,
                              OrderService orderService,
                              PaymentService paymentService) {
        this.moderationService = moderationService;
        this.userService = userService;
        this.productService = productService;
        this.orderService = orderService;
        this.paymentService = paymentService;

        setSpacing(false);
        setPadding(false);
        setWidthFull();
        getElement().getStyle()
            .set("background", "#F8FAFC")
            .set("min-height", "100vh");

        buildLayout();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!AuthGuard.requireLogin(event.getUI())) return;

        User current = AuthGuard.getCurrentUser();
        if (current != null && current.getRole() != Role.SUPER_ADMIN && current.getRole() != Role.MODERATOR) {
            Notification notif = Notification.show("Mode Demonstrasi Admin: Anda dapat meninjau data platform secara lengkap.", 3500, Notification.Position.TOP_CENTER);
            notif.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
        }
    }

    private void buildLayout() {
        removeAll();

        Div maxWrapper = new Div();
        maxWrapper.getElement().getStyle()
            .set("max-width", "1280px")
            .set("margin", "0 auto")
            .set("padding", "24px 20px 60px 20px")
            .set("width", "100%")
            .set("box-sizing", "border-box");

        // 1. Header Section
        HorizontalLayout headerRow = new HorizontalLayout();
        headerRow.setWidthFull();
        headerRow.setAlignItems(FlexComponent.Alignment.CENTER);
        headerRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        headerRow.getElement().getStyle().set("margin-bottom", "20px");

        Div headerLeft = new Div();
        H2 pageTitle = new H2("Panel Admin & Moderasi");
        pageTitle.getStyle()
            .set("font-size", "24px")
            .set("font-weight", "800")
            .set("color", "#001934")
            .set("margin", "0 0 4px 0");

        Paragraph pageSub = new Paragraph("Pusat kendali ekosistem ReWear: kelola pengguna, verifikasi sekolah, moderasi katalog produk, dan laporan pelanggaran.");
        pageSub.getStyle().set("font-size", "14px").set("color", "#64748B").set("margin", "0");
        headerLeft.add(pageTitle, pageSub);

        Button btnBackHome = new Button("Kembali ke Beranda", VaadinIcon.ARROW_LEFT.create(), e -> UI.getCurrent().navigate(""));
        btnBackHome.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnBackHome.getStyle().set("font-weight", "700").set("color", "#0A3D7A");

        headerRow.add(headerLeft, btnBackHome);
        maxWrapper.add(headerRow);

        // 2. Navigation Tabs Bar
        HorizontalLayout tabsBar = new HorizontalLayout();
        tabsBar.setWidthFull();
        tabsBar.setSpacing(true);
        tabsBar.getElement().getStyle()
            .set("background", "#FFFFFF")
            .set("padding", "6px")
            .set("border-radius", "12px")
            .set("border", "1px solid #E2E8F0")
            .set("margin-bottom", "24px")
            .set("box-shadow", "0 1px 3px rgba(0,25,52,0.04)");

        tabOverviewBtn = createTabButton("Ringkasan Platform", VaadinIcon.DASHBOARD, "overview");
        tabUsersBtn = createTabButton("Manajemen Pengguna", VaadinIcon.USERS, "users");
        tabProductsBtn = createTabButton("Moderasi Produk", VaadinIcon.PACKAGE, "products");
        tabReportsBtn = createTabButton("Laporan & Pengaduan", VaadinIcon.WARNING, "reports");
        tabOrdersBtn = createTabButton("Transaksi Global", VaadinIcon.MONEY_EXCHANGE, "orders");
        tabPayoutsBtn = createTabButton("Pencairan Dana", VaadinIcon.MONEY_WITHDRAW, "payouts");

        tabsBar.add(tabOverviewBtn, tabUsersBtn, tabProductsBtn, tabReportsBtn, tabOrdersBtn, tabPayoutsBtn);
        maxWrapper.add(tabsBar);

        // 3. Dynamic Tab Content
        contentContainer.setWidthFull();
        renderActiveTab();
        maxWrapper.add(contentContainer);

        add(maxWrapper);
    }

    private Button createTabButton(String label, VaadinIcon icon, String tabKey) {
        Button btn = new Button(label, icon.create());
        btn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btn.getStyle()
            .set("font-size", "13px")
            .set("font-weight", "700")
            .set("border-radius", "8px")
            .set("padding", "10px 18px")
            .set("cursor", "pointer")
            .set("transition", "all 0.2s ease");

        updateTabButtonHighlight(btn, tabKey.equals(activeTab));

        btn.addClickListener(e -> {
            activeTab = tabKey;
            refreshTabButtonStyles();
            renderActiveTab();
        });

        return btn;
    }

    private void updateTabButtonHighlight(Button btn, boolean isActive) {
        if (btn == null) return;
        if (isActive) {
            btn.getStyle()
                .set("background", "#001934")
                .set("color", "#FFFFFF");
        } else {
            btn.getStyle()
                .set("background", "transparent")
                .set("color", "#475569");
        }
    }

    private void refreshTabButtonStyles() {
        updateTabButtonHighlight(tabOverviewBtn, "overview".equals(activeTab));
        updateTabButtonHighlight(tabUsersBtn, "users".equals(activeTab));
        updateTabButtonHighlight(tabProductsBtn, "products".equals(activeTab));
        updateTabButtonHighlight(tabReportsBtn, "reports".equals(activeTab));
        updateTabButtonHighlight(tabOrdersBtn, "orders".equals(activeTab));
        updateTabButtonHighlight(tabPayoutsBtn, "payouts".equals(activeTab));
    }

    private void renderActiveTab() {
        contentContainer.removeAll();

        switch (activeTab) {
            case "users" -> contentContainer.add(renderUsersTab());
            case "products" -> contentContainer.add(renderProductsTab());
            case "reports" -> contentContainer.add(renderReportsTab());
            case "orders" -> contentContainer.add(renderOrdersTab());
            case "payouts" -> contentContainer.add(renderPayoutsTab());
            default -> contentContainer.add(renderOverviewTab());
        }
    }

    // ==========================================
    // TAB 1: RINGKASAN PLATFORM (ANALYTICS)
    // ==========================================

    private Component renderOverviewTab() {
        Div wrapper = new Div();
        wrapper.getStyle().set("display", "flex").set("flex-direction", "column").set("gap", "20px");

        List<User> users = userService.findAllUsers();
        List<Product> products = productService.findAllProducts();
        List<Order> orders = orderService.getAllOrders();
        List<Report> pendingReports = moderationService.getPendingReports();

        double totalGmv = orders.stream()
            .filter(o -> o.getStatus() == OrderStatus.DIBAYAR || o.getStatus() == OrderStatus.DIPROSES || o.getStatus() == OrderStatus.DIKIRIM || o.getStatus() == OrderStatus.SELESAI)
            .mapToDouble(o -> o.getTotalAmount() != null ? o.getTotalAmount().doubleValue() : 0)
            .sum();

        long activeProductsCount = products.stream().filter(p -> p.getStatus() == ProductStatus.ACTIVE).count();
        long verifiedUsersCount = users.stream().filter(u -> u.getSchool() != null).count();

        // 4 Stat Cards
        Div statsGrid = new Div();
        statsGrid.getStyle()
            .set("display", "grid")
            .set("grid-template-columns", "repeat(4, 1fr)")
            .set("gap", "16px");

        statsGrid.add(createMetricCard("TOTAL TRANSAKSI (GMV)", "Rp " + String.format("%,.0f", totalGmv), orders.size() + " Pesanan", "#15803D", "#DCFCE7"));
        statsGrid.add(createMetricCard("TOTAL PENGGUNA", users.size() + " User", verifiedUsersCount + " Warga SMKN 24", "#1E40AF", "#DBEAFE"));
        statsGrid.add(createMetricCard("KATALOG PRODUK AKTIF", activeProductsCount + " Produk", products.size() + " Total Listing", "#D97706", "#FEF3C7"));
        statsGrid.add(createMetricCard("LAPORAN MENUNGGU", pendingReports.size() + " Tiket", pendingReports.isEmpty() ? "Bersih" : "Perlu Tindakan", "#DC2626", "#FEE2E2"));

        wrapper.add(statsGrid);

        // Recent Activity / Quick Oversight
        Div overviewRow = new Div();
        overviewRow.getStyle().set("display", "grid").set("grid-template-columns", "1.2fr 0.8fr").set("gap", "20px");

        // Left: Recent Transactions
        Div leftCard = createCardContainer("Transaksi Terbaru Platform", "5 transaksi terakhir yang tercatat di sistem");
        List<Order> recentOrders = orders.stream().limit(5).toList();

        if (recentOrders.isEmpty()) {
            leftCard.add(new Paragraph("Belum ada transaksi di platform."));
        } else {
            for (Order o : recentOrders) {
                HorizontalLayout row = new HorizontalLayout();
                row.setWidthFull();
                row.setAlignItems(FlexComponent.Alignment.CENTER);
                row.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
                row.getStyle().set("padding", "10px 0").set("border-bottom", "1px solid #F1F5F9");

                Div info = new Div();
                Span oNum = new Span("Order #" + o.getOrderNumber());
                oNum.getStyle().set("font-weight", "700").set("color", "#001934").set("font-size", "13px");
                String buyerName = o.getBuyer() != null ? o.getBuyer().getFullName() : "Pembeli";
                String sellerName = o.getSeller() != null ? o.getSeller().getFullName() : "Penjual";
                Paragraph detail = new Paragraph(buyerName + " -> " + sellerName + " • " + (o.getCreatedAt() != null ? o.getCreatedAt().format(DATE_FMT) : "-"));
                detail.getStyle().set("font-size", "12px").set("color", "#64748B").set("margin", "0");
                info.add(oNum, detail);

                Span amount = new Span("Rp " + String.format("%,.0f", o.getTotalAmount() != null ? o.getTotalAmount().doubleValue() : 0));
                amount.getStyle().set("font-weight", "800").set("color", "#001934").set("font-size", "14px");

                row.add(info, amount);
                leftCard.add(row);
            }
        }

        // Right: System Health & School Info
        Div rightCard = createCardContainer("Status Ekosistem", "Informasi integritas data dan kebijakan sekolah");
        Div schoolInfo = new Div();
        schoolInfo.getStyle().set("padding", "12px 16px").set("background", "#F8FAFC").set("border-radius", "8px").set("font-size", "13px").set("color", "#334155");
        schoolInfo.getElement().setProperty("innerHTML",
            "<p style='margin:0 0 8px 0;'><strong>Institusi Utama:</strong> SMKN 24 Jakarta</p>" +
            "<p style='margin:0 0 8px 0;'><strong>Sistem Escrow:</strong> Aktif (Saldo dilepas setelah status SELESAI)</p>" +
            "<p style='margin:0;'><strong>Kebijakan Listing:</strong> Wajib barang seragam, peralatan sekolah, atau preloved layak pakai.</p>"
        );
        rightCard.add(schoolInfo);

        overviewRow.add(leftCard, rightCard);
        wrapper.add(overviewRow);

        return wrapper;
    }

    private Div createMetricCard(String label, String value, String subtext, String textColor, String bgBadge) {
        Div card = new Div();
        card.getStyle()
            .set("background", "#FFFFFF")
            .set("border", "1px solid #E2E8F0")
            .set("border-radius", "14px")
            .set("padding", "18px 20px")
            .set("box-shadow", "0 2px 6px rgba(0,25,52,0.03)");

        Span lbl = new Span(label);
        lbl.getStyle().set("font-size", "11px").set("font-weight", "800").set("color", "#64748B").set("letter-spacing", "0.5px");

        H3 val = new H3(value);
        val.getStyle().set("font-size", "22px").set("font-weight", "800").set("color", "#001934").set("margin", "6px 0");

        Span sub = new Span(subtext);
        sub.getStyle().set("font-size", "12px").set("font-weight", "700").set("color", textColor).set("background", bgBadge).set("padding", "3px 8px").set("border-radius", "6px");

        card.add(lbl, val, sub);
        return card;
    }

    private Div createCardContainer(String title, String subtitle) {
        Div container = new Div();
        container.getStyle()
            .set("background", "#FFFFFF")
            .set("border", "1px solid #E2E8F0")
            .set("border-radius", "14px")
            .set("padding", "20px")
            .set("box-shadow", "0 2px 6px rgba(0,25,52,0.03)");

        H3 t = new H3(title);
        t.getStyle().set("font-size", "16px").set("font-weight", "800").set("color", "#001934").set("margin", "0 0 4px 0");

        Paragraph s = new Paragraph(subtitle);
        s.getStyle().set("font-size", "13px").set("color", "#64748B").set("margin", "0 0 16px 0");

        container.add(t, s);
        return container;
    }

    // ==========================================
    // TAB 2: MANAJEMEN PENGGUNA
    // ==========================================

    private Component renderUsersTab() {
        Div wrapper = new Div();
        wrapper.getStyle().set("display", "flex").set("flex-direction", "column").set("gap", "16px");

        List<User> users = userService.findAllUsers();

        Grid<User> grid = new Grid<>(User.class, false);
        grid.setWidthFull();

        grid.addColumn(User::getId).setHeader("ID").setWidth("60px").setFlexGrow(0);
        grid.addColumn(User::getFullName).setHeader("Nama Lengkap").setFlexGrow(2);
        grid.addColumn(User::getEmail).setHeader("Email").setFlexGrow(2);

        grid.addComponentColumn(u -> {
            Span roleBadge = new Span(u.getRole() != null ? u.getRole().name() : "BUYER_SELLER");
            roleBadge.getStyle().set("font-size", "11px").set("font-weight", "700").set("padding", "3px 8px").set("border-radius", "6px");
            if (u.getRole() == Role.SUPER_ADMIN) {
                roleBadge.getStyle().set("background", "#001934").set("color", "#F5C45E");
            } else if (u.getRole() == Role.MODERATOR) {
                roleBadge.getStyle().set("background", "#EFF6FF").set("color", "#1E40AF");
            } else {
                roleBadge.getStyle().set("background", "#F1F5F9").set("color", "#475569");
            }
            return roleBadge;
        }).setHeader("Peran (Role)").setWidth("130px").setFlexGrow(0);

        grid.addComponentColumn(u -> {
            boolean isVerified = u.getSchool() != null;
            Span badge = new Span(isVerified ? "WARGA SMKN 24" : "UMUM");
            badge.getStyle().set("font-size", "11px").set("font-weight", "700").set("padding", "3px 8px").set("border-radius", "6px");
            badge.getStyle().set("background", isVerified ? "#FEF3C7" : "#F1F5F9").set("color", isVerified ? "#92400E" : "#64748B");
            return badge;
        }).setHeader("Status Sekolah").setWidth("140px").setFlexGrow(0);

        grid.addComponentColumn(u -> {
            boolean isSuspended = u.getAccountStatus() == AccountStatus.SUSPENDED;
            Span badge = new Span(isSuspended ? "Diblokir" : "Aktif");
            badge.getStyle().set("font-size", "11px").set("font-weight", "700").set("padding", "3px 8px").set("border-radius", "6px");
            badge.getStyle().set("background", isSuspended ? "#FEE2E2" : "#DCFCE7").set("color", isSuspended ? "#DC2626" : "#15803D");
            return badge;
        }).setHeader("Status Akun").setWidth("110px").setFlexGrow(0);

        grid.addComponentColumn(u -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(true);

            // Verifikasi Sekolah Button
            if (u.getSchool() == null) {
                Button btnVerify = new Button("Verifikasi", e -> {
                    List<School> schools = userService.findAllSchools();
                    School defaultSchool = schools.isEmpty() ? null : schools.get(0);
                    userService.verifyUserSchool(u, defaultSchool);
                    Notification.show("Pengguna " + u.getFullName() + " berhasil diverifikasi sebagai Siswa SMKN 24.", 2500, Notification.Position.TOP_CENTER);
                    renderActiveTab();
                });
                btnVerify.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
                btnVerify.getStyle().set("font-size", "12px").set("font-weight", "700").set("color", "#2563EB");
                actions.add(btnVerify);
            }

            // Suspend / Unsuspend Button
            boolean isSuspended = u.getAccountStatus() == AccountStatus.SUSPENDED;
            Button btnSuspend = new Button(isSuspended ? "Pulihkan" : "Blokir", e -> {
                userService.toggleAccountSuspension(u);
                Notification.show("Status akun " + u.getFullName() + " berhasil diubah.", 2500, Notification.Position.TOP_CENTER);
                renderActiveTab();
            });
            btnSuspend.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            btnSuspend.getStyle().set("font-size", "12px").set("font-weight", "700").set("color", isSuspended ? "#16A34A" : "#EF4444");

            // Change Role Button
            Button btnRole = new Button("Role", e -> openChangeRoleDialog(u));
            btnRole.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            btnRole.getStyle().set("font-size", "12px").set("font-weight", "700").set("color", "#475569");

            actions.add(btnSuspend, btnRole);
            return actions;
        }).setHeader("Aksi Admin").setWidth("220px").setFlexGrow(0);

        grid.setItems(users);

        Div card = createCardContainer("Daftar Pengguna Platform (" + users.size() + ")", "Kelola hak akses, status verifikasi siswa SMKN 24, dan pemblokiran akun");
        card.add(grid);
        wrapper.add(card);

        return wrapper;
    }

    private void openChangeRoleDialog(User user) {
        Dialog d = new Dialog();
        d.setHeaderTitle("Ubah Peran Pengguna: " + user.getFullName());
        d.setWidth("400px");

        ComboBox<Role> roleCombo = new ComboBox<>("Pilih Peran Baru");
        roleCombo.setItems(Role.values());
        roleCombo.setValue(user.getRole() != null ? user.getRole() : Role.BUYER_SELLER);
        roleCombo.setWidthFull();

        Button btnCancel = new Button("Batal", e -> d.close());
        Button btnSave = new Button("Simpan Peran", e -> {
            userService.changeUserRole(user, roleCombo.getValue());
            Notification.show("Peran " + user.getFullName() + " berhasil diubah ke " + roleCombo.getValue().name(), 2500, Notification.Position.TOP_CENTER);
            d.close();
            renderActiveTab();
        });
        btnSave.getStyle().set("background", "#001934").set("color", "#FFFFFF").set("font-weight", "700");

        d.add(new VerticalLayout(roleCombo));
        d.getFooter().add(btnCancel, btnSave);
        d.open();
    }

    // ==========================================
    // TAB 3: MODERASI KATALOG PRODUK
    // ==========================================

    private Component renderProductsTab() {
        Div wrapper = new Div();
        wrapper.getStyle().set("display", "flex").set("flex-direction", "column").set("gap", "16px");

        List<Product> products = productService.findAllProducts();

        Grid<Product> grid = new Grid<>(Product.class, false);
        grid.setWidthFull();

        grid.addColumn(Product::getId).setHeader("ID").setWidth("60px").setFlexGrow(0);
        grid.addColumn(Product::getName).setHeader("Nama Barang").setFlexGrow(2);
        grid.addColumn(p -> p.getSeller() != null ? p.getSeller().getFullName() : "-").setHeader("Penjual").setFlexGrow(1);
        grid.addColumn(p -> "Rp " + String.format("%,.0f", p.getPrice() != null ? p.getPrice().doubleValue() : 0)).setHeader("Harga").setWidth("120px").setFlexGrow(0);
        grid.addColumn(p -> p.getStock() != null ? p.getStock() + " buah" : "-").setHeader("Stok").setWidth("90px").setFlexGrow(0);

        grid.addComponentColumn(p -> {
            ProductStatus s = p.getStatus() != null ? p.getStatus() : ProductStatus.ACTIVE;
            Span badge = new Span(s.name());
            badge.getStyle().set("font-size", "11px").set("font-weight", "700").set("padding", "3px 8px").set("border-radius", "6px");
            if (s == ProductStatus.ACTIVE) {
                badge.getStyle().set("background", "#DCFCE7").set("color", "#15803D");
            } else if (s == ProductStatus.REMOVED) {
                badge.getStyle().set("background", "#FEE2E2").set("color", "#DC2626");
            } else {
                badge.getStyle().set("background", "#F1F5F9").set("color", "#64748B");
            }
            return badge;
        }).setHeader("Status").setWidth("110px").setFlexGrow(0);

        grid.addComponentColumn(p -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(true);

            if (p.getStatus() == ProductStatus.REMOVED) {
                Button btnRestore = new Button("Pulihkan", e -> {
                    productService.activateProduct(p);
                    Notification.show("Produk #" + p.getId() + " berhasil dipulihkan ke pasar.", 2500, Notification.Position.TOP_CENTER);
                    renderActiveTab();
                });
                btnRestore.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
                btnRestore.getStyle().set("font-size", "12px").set("font-weight", "700").set("color", "#16A34A");
                actions.add(btnRestore);
            } else {
                Button btnTakedown = new Button("Takedown", e -> {
                    productService.takedownProduct(p, "Pelanggaran aturan listing");
                    Notification.show("Produk #" + p.getId() + " berhasil di-takedown dari pasar.", 2500, Notification.Position.TOP_CENTER);
                    renderActiveTab();
                });
                btnTakedown.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
                btnTakedown.getStyle().set("font-size", "12px").set("font-weight", "700").set("color", "#EF4444");
                actions.add(btnTakedown);
            }

            Button btnView = new Button("Lihat", e -> UI.getCurrent().navigate("product/" + p.getId()));
            btnView.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            btnView.getStyle().set("font-size", "12px").set("font-weight", "700").set("color", "#0A3D7A");
            actions.add(btnView);

            return actions;
        }).setHeader("Aksi Moderasi").setWidth("180px").setFlexGrow(0);

        grid.setItems(products);

        Div card = createCardContainer("Moderasi Katalog Produk (" + products.size() + ")", "Tinjau dan lakukan takedown pada barang yang tidak sesuai atau melanggar aturan");
        card.add(grid);
        wrapper.add(card);

        return wrapper;
    }

    // ==========================================
    // TAB 4: PUSAT LAPORAN & PENGADUAN
    // ==========================================

    private Component renderReportsTab() {
        Div wrapper = new Div();
        wrapper.getStyle().set("display", "flex").set("flex-direction", "column").set("gap", "16px");

        List<Report> reports = moderationService.getAllReports();

        Grid<Report> grid = new Grid<>(Report.class, false);
        grid.setWidthFull();

        grid.addColumn(Report::getId).setHeader("ID").setWidth("60px").setFlexGrow(0);
        grid.addColumn(r -> r.getReporter() != null ? r.getReporter().getFullName() : "-").setHeader("Pelapor").setFlexGrow(1);
        grid.addColumn(r -> r.getReportedUser() != null ? r.getReportedUser().getFullName() : "-").setHeader("Pihak Terlapor").setFlexGrow(1);
        grid.addColumn(r -> r.getType() != null ? r.getType().getValue() : "-").setHeader("Tipe").setWidth("140px").setFlexGrow(0);
        grid.addColumn(Report::getReason).setHeader("Alasan Laporan").setFlexGrow(2);

        grid.addComponentColumn(r -> {
            ReportStatus s = r.getStatus() != null ? r.getStatus() : ReportStatus.PENDING;
            Span badge = new Span(s.getValue());
            badge.getStyle().set("font-size", "11px").set("font-weight", "700").set("padding", "3px 8px").set("border-radius", "6px");
            if (s == ReportStatus.RESOLVED) {
                badge.getStyle().set("background", "#DCFCE7").set("color", "#15803D");
            } else if (s == ReportStatus.REJECTED) {
                badge.getStyle().set("background", "#F1F5F9").set("color", "#64748B");
            } else {
                badge.getStyle().set("background", "#FEE2E2").set("color", "#DC2626");
            }
            return badge;
        }).setHeader("Status").setWidth("120px").setFlexGrow(0);

        grid.addComponentColumn(r -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(true);

            if (r.getStatus() == ReportStatus.PENDING) {
                Button btnResolve = new Button("Tindak / Selesai", e -> {
                    moderationService.resolveReport(r, "Laporan disetujui dan ditindaklanjuti admin");
                    Notification.show("Laporan #" + r.getId() + " ditandai selesai.", 2500, Notification.Position.TOP_CENTER);
                    renderActiveTab();
                });
                btnResolve.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
                btnResolve.getStyle().set("font-size", "12px").set("font-weight", "700").set("color", "#16A34A");

                Button btnReject = new Button("Tolak", e -> {
                    moderationService.rejectReport(r, "Laporan tidak terbukti");
                    Notification.show("Laporan #" + r.getId() + " ditolak.", 2500, Notification.Position.TOP_CENTER);
                    renderActiveTab();
                });
                btnReject.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
                btnReject.getStyle().set("font-size", "12px").set("font-weight", "700").set("color", "#64748B");

                actions.add(btnResolve, btnReject);
            } else {
                Span closed = new Span("Ditutup");
                closed.getStyle().set("font-size", "12px").set("color", "#94A3B8");
                actions.add(closed);
            }

            return actions;
        }).setHeader("Aksi").setWidth("180px").setFlexGrow(0);

        grid.setItems(reports);

        Div card = createCardContainer("Pusat Laporan & Pengaduan Pelanggaran (" + reports.size() + ")", "Proses aduan dari pembeli dan penjual terkait konten tidak pantas atau transaksi bermasalah");
        card.add(grid);
        wrapper.add(card);

        return wrapper;
    }

    // ==========================================
    // TAB 5: PENGAWASAN TRANSAKSI GLOBAL
    // ==========================================

    private Component renderOrdersTab() {
        Div wrapper = new Div();
        wrapper.getStyle().set("display", "flex").set("flex-direction", "column").set("gap", "16px");

        List<Order> orders = orderService.getAllOrders();

        Grid<Order> grid = new Grid<>(Order.class, false);
        grid.setWidthFull();

        grid.addColumn(Order::getOrderNumber).setHeader("No. Pesanan").setWidth("130px").setFlexGrow(0);
        grid.addColumn(o -> o.getCreatedAt() != null ? o.getCreatedAt().format(DATE_FMT) : "-").setHeader("Tanggal").setWidth("150px").setFlexGrow(0);
        grid.addColumn(o -> o.getBuyer() != null ? o.getBuyer().getFullName() : "-").setHeader("Pembeli").setFlexGrow(1);
        grid.addColumn(o -> o.getSeller() != null ? o.getSeller().getFullName() : "-").setHeader("Penjual").setFlexGrow(1);
        grid.addColumn(o -> "Rp " + String.format("%,.0f", o.getTotalAmount() != null ? o.getTotalAmount().doubleValue() : 0)).setHeader("Total").setWidth("120px").setFlexGrow(0);

        grid.addComponentColumn(o -> {
            OrderStatus s = o.getStatus() != null ? o.getStatus() : OrderStatus.MENUNGGU_PEMBAYARAN;
            Span badge = new Span(s.name());
            badge.getStyle().set("font-size", "11px").set("font-weight", "700").set("padding", "3px 8px").set("border-radius", "6px");
            if (s == OrderStatus.SELESAI) {
                badge.getStyle().set("background", "#DCFCE7").set("color", "#15803D");
            } else if (s == OrderStatus.DIBATALKAN) {
                badge.getStyle().set("background", "#F1F5F9").set("color", "#64748B");
            } else if (s == OrderStatus.DIKIRIM || s == OrderStatus.DIPROSES || s == OrderStatus.DIBAYAR) {
                badge.getStyle().set("background", "#EFF6FF").set("color", "#1E40AF");
            } else {
                badge.getStyle().set("background", "#FEF3C7").set("color", "#92400E");
            }
            return badge;
        }).setHeader("Status").setWidth("130px").setFlexGrow(0);

        grid.setItems(orders);

        Div card = createCardContainer("Pengawasan Transaksi Global (" + orders.size() + ")", "Pemantauan real-time seluruh pesanan dan status pergerakan dana di platform");
        card.add(grid);
        wrapper.add(card);

        return wrapper;
    }

    // ==========================================
    // TAB 6: PERSETUJUAN PENCAIRAN DANA (PAYOUTS)
    // ==========================================

    private Component renderPayoutsTab() {
        Div wrapper = new Div();
        wrapper.getStyle().set("display", "flex").set("flex-direction", "column").set("gap", "16px");

        List<SellerPayout> payouts = paymentService != null ? paymentService.getAllPayouts() : List.of();

        Grid<SellerPayout> grid = new Grid<>(SellerPayout.class, false);
        grid.setWidthFull();

        grid.addColumn(p -> p.getReferenceNumber() != null ? p.getReferenceNumber() : "WD-" + p.getId()).setHeader("No. Referensi").setWidth("140px").setFlexGrow(0);
        grid.addColumn(p -> p.getCreatedAt() != null ? p.getCreatedAt().format(DATE_FMT) : "-").setHeader("Tanggal").setWidth("150px").setFlexGrow(0);
        grid.addColumn(p -> p.getSeller() != null ? p.getSeller().getFullName() : "-").setHeader("Penjual").setFlexGrow(1);
        grid.addColumn(p -> p.getBankAccount() != null ? (p.getBankAccount().getBankName() + " (" + p.getBankAccount().getAccountNumber() + ")") : "-").setHeader("Tujuan Transfer").setFlexGrow(1);
        grid.addColumn(p -> p.getBankAccount() != null ? p.getBankAccount().getAccountHolderName() : "-").setHeader("Atas Nama").setFlexGrow(1);
        grid.addColumn(p -> "Rp " + String.format("%,.0f", p.getAmount() != null ? p.getAmount().doubleValue() : 0)).setHeader("Nominal").setWidth("120px").setFlexGrow(0);

        grid.addComponentColumn(p -> {
            PayoutStatus s = p.getStatus() != null ? p.getStatus() : PayoutStatus.REQUESTED;
            Span badge = new Span(s.name());
            badge.getStyle().set("font-size", "11px").set("font-weight", "700").set("padding", "3px 8px").set("border-radius", "6px");
            if (s == PayoutStatus.COMPLETED) {
                badge.setText("Berhasil / Cair");
                badge.getStyle().set("background", "#DCFCE7").set("color", "#15803D");
            } else if (s == PayoutStatus.REJECTED) {
                badge.setText("Ditolak");
                badge.getStyle().set("background", "#FEE2E2").set("color", "#DC2626");
            } else {
                badge.setText("Menunggu Approval");
                badge.getStyle().set("background", "#FEF3C7").set("color", "#92400E");
            }
            return badge;
        }).setHeader("Status").setWidth("150px").setFlexGrow(0);

        grid.addComponentColumn(p -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(true);

            if (p.getStatus() == PayoutStatus.REQUESTED || p.getStatus() == PayoutStatus.PROCESSING) {
                Button btnApprove = new Button("Setujui & Cairkan", e -> {
                    paymentService.approvePayout(p, null, "Disetujui admin");
                    Notification.show("Penarikan dana #" + p.getReferenceNumber() + " berhasil disetujui & dicairkan.", 2500, Notification.Position.TOP_CENTER);
                    renderActiveTab();
                });
                btnApprove.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
                btnApprove.getStyle().set("font-size", "12px").set("font-weight", "700").set("color", "#16A34A");

                Button btnReject = new Button("Tolak", e -> {
                    paymentService.rejectPayout(p, "Nomor rekening atau identitas tidak valid");
                    Notification.show("Penarikan dana #" + p.getReferenceNumber() + " ditolak.", 2500, Notification.Position.TOP_CENTER);
                    renderActiveTab();
                });
                btnReject.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
                btnReject.getStyle().set("font-size", "12px").set("font-weight", "700").set("color", "#DC2626");

                actions.add(btnApprove, btnReject);
            } else {
                Span done = new Span("Selesai diproses");
                done.getStyle().set("font-size", "12px").set("color", "#94A3B8");
                actions.add(done);
            }

            return actions;
        }).setHeader("Aksi").setWidth("210px").setFlexGrow(0);

        grid.setItems(payouts);

        Div card = createCardContainer("Permohonan Pencairan Dana Penjual (" + payouts.size() + ")", "Verifikasi dan setujui penarikan saldo penjualan ke rekening bank / e-wallet penjual");
        card.add(grid);
        wrapper.add(card);

        return wrapper;
    }
}
