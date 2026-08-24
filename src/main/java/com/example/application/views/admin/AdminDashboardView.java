package com.example.application.views.admin;

import com.example.application.model.moderation.Report;
import com.example.application.model.moderation.ReportStatus;
import com.example.application.model.order.Order;
import com.example.application.model.order.OrderReturn;
import com.example.application.model.order.OrderStatus;
import com.example.application.model.order.ReturnStatus;
import com.example.application.model.payment.Payment;
import com.example.application.model.payment.PayoutStatus;
import com.example.application.model.payment.SellerPayout;
import com.example.application.model.payment.TransactionStatus;
import com.example.application.model.product.Product;
import com.example.application.model.product.ProductStatus;
import com.example.application.model.user.AccountStatus;
import com.example.application.model.user.Role;
import com.example.application.model.user.School;
import com.example.application.model.user.User;
import com.example.application.model.user.UserSchoolVerification;
import com.example.application.model.user.VerificationStatus;
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
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

@PageTitle("Panel Admin & Moderasi - ReWear")
@Route(value = "admin", layout = MainLayout.class)
@RouteAlias(value = "panel-admin", layout = MainLayout.class)
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
    private Button tabPaymentsBtn;
    private Button tabDisputesBtn;
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

        Paragraph pageSub = new Paragraph("Pusat kendali ekosistem ReWear: kelola pengguna, verifikasi pembayaran struk transfer, moderasi katalog, dan arbitrase sengketa.");
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
            .set("padding", "6px 10px")
            .set("border-radius", "12px")
            .set("border", "1px solid #E2E8F0")
            .set("margin-bottom", "24px")
            .set("box-shadow", "0 1px 3px rgba(0,25,52,0.04)")
            .set("overflow-x", "auto")
            .set("display", "flex")
            .set("flex-wrap", "nowrap")
            .set("box-sizing", "border-box")
            .set("-webkit-overflow-scrolling", "touch");

        tabOverviewBtn = createTabButton("Ringkasan Platform", VaadinIcon.DASHBOARD, "overview");
        tabUsersBtn = createTabButton("Manajemen Pengguna", VaadinIcon.USERS, "users");
        tabProductsBtn = createTabButton("Moderasi Produk", VaadinIcon.PACKAGE, "products");
        tabPaymentsBtn = createTabButton("Verifikasi Pembayaran", VaadinIcon.CHECK_SQUARE_O, "payments");
        tabDisputesBtn = createTabButton("Komplain & Retur", VaadinIcon.EXCLAMATION_CIRCLE, "disputes");
        tabReportsBtn = createTabButton("Laporan Pelanggaran", VaadinIcon.WARNING, "reports");
        tabOrdersBtn = createTabButton("Transaksi Global", VaadinIcon.MONEY_EXCHANGE, "orders");
        tabPayoutsBtn = createTabButton("Pencairan Dana", VaadinIcon.MONEY_WITHDRAW, "payouts");

        tabsBar.add(tabOverviewBtn, tabUsersBtn, tabProductsBtn, tabPaymentsBtn, tabDisputesBtn, tabReportsBtn, tabOrdersBtn, tabPayoutsBtn);
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
            .set("padding", "8px 14px")
            .set("cursor", "pointer")
            .set("flex-shrink", "0")
            .set("white-space", "nowrap")
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
        updateTabButtonHighlight(tabPaymentsBtn, "payments".equals(activeTab));
        updateTabButtonHighlight(tabReportsBtn, "reports".equals(activeTab));
        updateTabButtonHighlight(tabDisputesBtn, "disputes".equals(activeTab));
        updateTabButtonHighlight(tabOrdersBtn, "orders".equals(activeTab));
        updateTabButtonHighlight(tabPayoutsBtn, "payouts".equals(activeTab));
    }

    private void renderActiveTab() {
        contentContainer.removeAll();

        switch (activeTab) {
            case "users" -> contentContainer.add(renderUsersTab());
            case "products" -> contentContainer.add(renderProductsTab());
            case "payments" -> contentContainer.add(renderPaymentsTab());
            case "reports" -> contentContainer.add(renderReportsTab());
            case "disputes" -> contentContainer.add(renderDisputesTab());
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
            .set("box-shadow", "0 2px 6px rgba(0,25,52,0.03)")
            .set("overflow-x", "auto")
            .set("max-width", "100%")
            .set("box-sizing", "border-box");

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
            if (isVerified) {
                Span badge = new Span("WARGA SMKN 24");
                badge.getStyle().set("font-size", "11px").set("font-weight", "700").set("padding", "3px 8px").set("border-radius", "6px");
                badge.getStyle().set("background", "#FEF3C7").set("color", "#92400E");
                return badge;
            }
            java.util.Optional<UserSchoolVerification> verOpt = userService.getVerification(u);
            if (verOpt.isPresent() && verOpt.get().getStatus() == VerificationStatus.PENDING) {
                Span badge = new Span("PENGAJUAN KTA");
                badge.getStyle().set("font-size", "11px").set("font-weight", "700").set("padding", "3px 8px").set("border-radius", "6px");
                badge.getStyle().set("background", "#EFF6FF").set("color", "#1E40AF").set("border", "1px solid #BFDBFE");
                return badge;
            }
            if (verOpt.isPresent() && verOpt.get().getStatus() == VerificationStatus.REJECTED) {
                Span badge = new Span("KTA DITOLAK");
                badge.getStyle().set("font-size", "11px").set("font-weight", "700").set("padding", "3px 8px").set("border-radius", "6px");
                badge.getStyle().set("background", "#FEE2E2").set("color", "#DC2626");
                return badge;
            }
            Span badge = new Span("UMUM");
            badge.getStyle().set("font-size", "11px").set("font-weight", "700").set("padding", "3px 8px").set("border-radius", "6px");
            badge.getStyle().set("background", "#F1F5F9").set("color", "#64748B");
            return badge;
        }).setHeader("Status Sekolah").setWidth("150px").setFlexGrow(0);

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

            java.util.Optional<UserSchoolVerification> verOpt = userService.getVerification(u);

            // Verifikasi Sekolah / Tinjau KTA Button
            if (u.getSchool() == null) {
                if (verOpt.isPresent() && verOpt.get().getStatus() == VerificationStatus.PENDING) {
                    Button btnReviewKta = new Button("Tinjau KTA", VaadinIcon.ACADEMY_CAP.create(), e -> openReviewKtaDialog(u, verOpt.get()));
                    btnReviewKta.getStyle().set("background", "#001934").set("color", "#F5C45E").set("font-size", "11px").set("font-weight", "700").set("border-radius", "6px").set("padding", "4px 8px");
                    actions.add(btnReviewKta);
                } else {
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
            }

            // Suspend / Unsuspend Button with ConfirmDialog
            boolean isSuspended = u.getAccountStatus() == AccountStatus.SUSPENDED;
            Button btnSuspend = new Button(isSuspended ? "Pulihkan" : "Blokir", e -> {
                ConfirmDialog dialog = new ConfirmDialog();
                dialog.setHeader(isSuspended ? "Konfirmasi Pemulihan Akun" : "Konfirmasi Blokir Akun");
                dialog.setText(isSuspended 
                    ? "Apakah Anda yakin ingin memulihkan akun " + u.getFullName() + "? Pengguna dapat login dan bertransaksi kembali."
                    : "Apakah Anda yakin ingin memblokir (suspend) akun " + u.getFullName() + "? Pengguna tidak akan dapat login atau melakukan transaksi.");
                dialog.setCancelable(true);
                dialog.setCancelText("Batal");
                dialog.setConfirmText(isSuspended ? "Ya, Pulihkan" : "Ya, Blokir");
                dialog.setConfirmButtonTheme(isSuspended ? "primary" : "error primary");
                dialog.addConfirmListener(event -> {
                    userService.toggleAccountSuspension(u);
                    Notification.show("Status akun " + u.getFullName() + " berhasil diubah.", 2500, Notification.Position.TOP_CENTER);
                    renderActiveTab();
                });
                dialog.open();
            });
            btnSuspend.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            btnSuspend.getStyle().set("font-size", "12px").set("font-weight", "700").set("color", isSuspended ? "#16A34A" : "#EF4444");

            // Change Role Button
            Button btnRole = new Button("Role", e -> openChangeRoleDialog(u));
            btnRole.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            btnRole.getStyle().set("font-size", "12px").set("font-weight", "700").set("color", "#475569");

            actions.add(btnSuspend, btnRole);
            return actions;
        }).setHeader("Aksi Admin").setWidth("280px").setFlexGrow(0);

        grid.setItems(users);

        Div card = createCardContainer("Daftar Pengguna Platform (" + users.size() + ")", "Kelola hak akses, status verifikasi siswa SMKN 24, dan pemblokiran akun");
        card.add(grid);
        wrapper.add(card);

        return wrapper;
    }

    private void openReviewKtaDialog(User u, UserSchoolVerification ver) {
        Dialog d = new Dialog();
        d.setHeaderTitle("Tinjau Pengajuan KTA - " + (u.getFullName() != null ? u.getFullName() : "Siswa"));
        d.setWidth("480px");

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(false);

        Div details = new Div();
        details.getStyle().set("background", "#F8FAFC").set("padding", "12px").set("border-radius", "8px").set("border", "1px solid #CBD5E1").set("width", "100%").set("box-sizing", "border-box");
        details.getElement().setProperty("innerHTML",
            "<div style='font-size:13px;color:#001934;margin-bottom:4px;'><strong>Nama Siswa:</strong> " + (u.getFullName() != null ? u.getFullName() : "-") + "</div>" +
            "<div style='font-size:13px;color:#001934;margin-bottom:4px;'><strong>Email:</strong> " + (u.getEmail() != null ? u.getEmail() : "-") + "</div>" +
            "<div style='font-size:13px;color:#001934;margin-bottom:4px;'><strong>Nomor Induk Siswa (NISN):</strong> <span style='color:#2563EB;font-weight:700;'>" + (ver.getSchoolNumber() != null ? ver.getSchoolNumber() : "-") + "</span></div>" +
            "<div style='font-size:12px;color:#64748B;'><strong>Sekolah Tujuan:</strong> " + (ver.getSchool() != null ? ver.getSchool().getName() : "SMKN 24 Jakarta") + "</div>"
        );
        layout.add(details);

        if (ver.getProofUrl() != null && !ver.getProofUrl().isBlank()) {
            String cleanUrl = ver.getProofUrl().startsWith("/") ? ver.getProofUrl() : "/" + ver.getProofUrl();
            Image proofImg = new Image(cleanUrl, "Foto Kartu Pelajar");
            proofImg.setWidth("100%");
            proofImg.setMaxHeight("260px");
            proofImg.getStyle().set("object-fit", "contain").set("border-radius", "8px").set("border", "1px solid #CBD5E1").set("background", "#FFFFFF").set("padding", "4px");
            layout.add(proofImg);
        } else {
            Div noImg = new Div(new Text("Tidak ada lampiran foto KTA."));
            noImg.getStyle().set("color", "#64748B").set("font-size", "13px").set("padding", "12px");
            layout.add(noImg);
        }

        Button btnApprove = new Button("Setujui Verifikasi", VaadinIcon.CHECK.create());
        btnApprove.getStyle().set("background", "#16A34A").set("color", "#FFFFFF").set("font-weight", "700");
        btnApprove.addClickListener(e -> {
            userService.verifyUserSchool(u, ver.getSchool());
            Notification notif = Notification.show("Verifikasi KTA disetujui! Pengguna kini berstatus Warga SMKN 24.", 3000, Notification.Position.TOP_CENTER);
            notif.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            d.close();
            renderActiveTab();
        });

        Button btnReject = new Button("Tolak Pengajuan", VaadinIcon.CLOSE.create());
        btnReject.getStyle().set("background", "#DC2626").set("color", "#FFFFFF").set("font-weight", "700");
        btnReject.addClickListener(e -> {
            Dialog rejectDialog = new Dialog();
            rejectDialog.setHeaderTitle("Alasan Penolakan KTA");
            rejectDialog.setWidth("380px");

            com.vaadin.flow.component.textfield.TextField reasonField = new com.vaadin.flow.component.textfield.TextField("Alasan:");
            reasonField.setPlaceholder("Contoh: Foto KTA buram / nama tidak sesuai");
            reasonField.setValue("Foto KTA buram atau tidak terbaca.");
            reasonField.setWidthFull();

            Button btnConfirmReject = new Button("Konfirmasi Tolak", evt -> {
                String reason = reasonField.getValue();
                userService.rejectUserSchoolVerification(u, reason);
                Notification.show("Pengajuan verifikasi KTA ditolak.", 2500, Notification.Position.TOP_CENTER);
                rejectDialog.close();
                d.close();
                renderActiveTab();
            });
            btnConfirmReject.getStyle().set("background", "#DC2626").set("color", "#FFFFFF");

            Button btnCancelReject = new Button("Batal", evt -> rejectDialog.close());

            rejectDialog.add(reasonField);
            rejectDialog.getFooter().add(btnCancelReject, btnConfirmReject);
            rejectDialog.open();
        });

        Button btnClose = new Button("Tutup", e -> d.close());

        d.getFooter().add(btnClose, btnReject, btnApprove);
        d.add(layout);
        d.open();
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
                    ConfirmDialog dialog = new ConfirmDialog();
                    dialog.setHeader("Konfirmasi Takedown Produk");
                    dialog.setText("Apakah Anda yakin ingin men-takedown produk '" + p.getName() + "' (ID: #" + p.getId() + ")? Produk akan segera disembunyikan dari seluruh katalog dan beranda.");
                    dialog.setCancelable(true);
                    dialog.setCancelText("Batal");
                    dialog.setConfirmText("Ya, Takedown");
                    dialog.setConfirmButtonTheme("error primary");
                    dialog.addConfirmListener(event -> {
                        productService.takedownProduct(p, "Pelanggaran aturan listing");
                        Notification.show("Produk #" + p.getId() + " berhasil di-takedown dari pasar.", 2500, Notification.Position.TOP_CENTER);
                        renderActiveTab();
                    });
                    dialog.open();
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

    // ==========================================
    // TAB 5: SENGKETA & KOMPLAIN PESANAN
    // ==========================================

    private Component renderDisputesTab() {
        Div wrapper = new Div();
        wrapper.getStyle().set("display", "flex").set("flex-direction", "column").set("gap", "16px");

        List<OrderReturn> returns = orderService.getAllReturns();
        long pendingCount = returns.stream().filter(r -> r.getStatus() == ReturnStatus.PENDING).count();
        long approvedCount = returns.stream().filter(r -> r.getStatus() == ReturnStatus.APPROVED).count();
        long rejectedCount = returns.stream().filter(r -> r.getStatus() == ReturnStatus.REJECTED).count();

        // 3 Metric Cards
        Div statsGrid = new Div();
        statsGrid.getStyle()
            .set("display", "grid")
            .set("grid-template-columns", "repeat(3, 1fr)")
            .set("gap", "14px");

        statsGrid.add(createMetricCard("MENUNGGU KEPUTUSAN", String.valueOf(pendingCount), "Sengketa yang memerlukan tindakan admin", "#D97706", "#FEF3C7"));
        statsGrid.add(createMetricCard("KOMPLAIN DISETUJUI", String.valueOf(approvedCount), "Dana dikembalikan ke pembeli & barang direstock", "#15803D", "#DCFCE7"));
        statsGrid.add(createMetricCard("KOMPLAIN DITOLAK", String.valueOf(rejectedCount), "Dana Escrow dicairkan ke saldo penjual", "#991B1B", "#FEE2E2"));
        wrapper.add(statsGrid);

        Grid<OrderReturn> grid = new Grid<>(OrderReturn.class, false);
        grid.setWidthFull();

        grid.addColumn(OrderReturn::getId).setHeader("ID").setWidth("60px").setFlexGrow(0);
        grid.addColumn(r -> r.getCreatedAt() != null ? r.getCreatedAt().format(DATE_FMT) : "-").setHeader("Tgl Diajukan").setWidth("140px").setFlexGrow(0);
        grid.addColumn(r -> r.getOrder() != null ? "#" + r.getOrder().getOrderNumber() : "-").setHeader("No. Pesanan").setWidth("130px").setFlexGrow(0);
        grid.addColumn(r -> r.getBuyer() != null ? r.getBuyer().getFullName() : "-").setHeader("Pembeli (Pelapor)").setFlexGrow(1);
        grid.addColumn(r -> (r.getOrder() != null && r.getOrder().getSeller() != null) ? r.getOrder().getSeller().getFullName() : "-").setHeader("Penjual").setFlexGrow(1);

        grid.addColumn(r -> "Rp " + String.format("%,.0f", r.getRefundAmount() != null ? r.getRefundAmount().doubleValue() : (r.getOrder() != null ? r.getOrder().getTotalAmount().doubleValue() : 0)))
            .setHeader("Nominal").setWidth("120px").setFlexGrow(0);

        grid.addComponentColumn(r -> {
            ReturnStatus s = r.getStatus() != null ? r.getStatus() : ReturnStatus.PENDING;
            Span badge = new Span(s.name());
            badge.getStyle().set("font-size", "11px").set("font-weight", "700").set("padding", "3px 8px").set("border-radius", "6px");
            if (s == ReturnStatus.PENDING) {
                badge.setText("MENUNGGU KEPUTUSAN");
                badge.getStyle().set("background", "#FEF3C7").set("color", "#92400E");
            } else if (s == ReturnStatus.APPROVED) {
                badge.setText("DISETUJUI (REFUND)");
                badge.getStyle().set("background", "#DCFCE7").set("color", "#15803D");
            } else if (s == ReturnStatus.REJECTED) {
                badge.setText("DITOLAK");
                badge.getStyle().set("background", "#FEE2E2").set("color", "#DC2626");
            } else {
                badge.getStyle().set("background", "#F1F5F9").set("color", "#64748B");
            }
            return badge;
        }).setHeader("Status").setWidth("160px").setFlexGrow(0);

        grid.addComponentColumn(r -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(true);

            if (r.getStatus() == ReturnStatus.PENDING) {
                Button btnApprove = new Button("Setujui Refund", e -> openApproveReturnDialog(r));
                btnApprove.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
                btnApprove.getStyle().set("font-size", "12px").set("font-weight", "700").set("color", "#16A34A");

                Button btnReject = new Button("Tolak", e -> openRejectReturnDialog(r));
                btnReject.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
                btnReject.getStyle().set("font-size", "12px").set("font-weight", "700").set("color", "#DC2626");

                actions.add(btnApprove, btnReject);
            }

            Button btnDetail = new Button("Rincian", e -> openReturnDetailDialog(r));
            btnDetail.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            btnDetail.getStyle().set("font-size", "12px").set("font-weight", "700").set("color", "#0A3D7A");
            actions.add(btnDetail);

            return actions;
        }).setHeader("Aksi Putusan").setWidth("230px").setFlexGrow(0);

        grid.setItems(returns);

        Div card = createCardContainer("Daftar Sengketa & Retur Pesanan (" + returns.size() + ")", "Tinjau klaim kerusakan/ketidaksesuaian barang dan putuskan pengembalian dana pembeli atau pencairan ke penjual");
        card.add(grid);
        wrapper.add(card);

        return wrapper;
    }

    private void openApproveReturnDialog(OrderReturn ret) {
        Dialog d = new Dialog();
        d.setHeaderTitle("Setujui Komplain & Kembalikan Dana (Refund)");
        d.setWidth("480px");

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(false);

        Paragraph info = new Paragraph("Tindakan ini akan:\n1. Mengembalikan saldo sebesar Rp " +
            String.format("%,.0f", ret.getRefundAmount()) + " ke akun pembeli (" + (ret.getBuyer() != null ? ret.getBuyer().getFullName() : "-") + ").\n" +
            "2. Mengubah status pesanan menjadi DIBATALKAN (Refunded).\n" +
            "3. Mengembalikan jumlah stok barang ke inventaris produk.");
        info.getStyle().set("font-size", "13px").set("color", "#334155").set("line-height", "1.5");

        TextArea adminNotes = new TextArea("Catatan Admin (Opsional)");
        adminNotes.setPlaceholder("Contoh: Bukti foto cacat terverifikasi jelas.");
        adminNotes.setWidthFull();

        layout.add(info, adminNotes);
        d.add(layout);

        Button btnCancel = new Button("Batal", e -> d.close());
        Button btnConfirm = new Button("Setujui & Proses Refund", e -> {
            try {
                orderService.approveOrderReturn(ret, adminNotes.getValue(), AuthGuard.getCurrentUser());
                Notification notif = Notification.show("Komplain #" + ret.getId() + " berhasil disetujui. Dana telah direfund ke pembeli.", 3500, Notification.Position.TOP_CENTER);
                notif.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                d.close();
                renderActiveTab();
            } catch (Exception ex) {
                Notification.show("Gagal memproses persetujuan: " + ex.getMessage(), 3000, Notification.Position.TOP_CENTER);
            }
        });
        btnConfirm.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnConfirm.getStyle().set("background", "#16A34A");

        d.getFooter().add(btnCancel, btnConfirm);
        d.open();
    }

    private void openRejectReturnDialog(OrderReturn ret) {
        Dialog d = new Dialog();
        d.setHeaderTitle("Tolak Komplain Pesanan");
        d.setWidth("480px");

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(false);

        Paragraph info = new Paragraph("Tindakan ini akan:\n1. Menolak pengajuan komplain pembeli.\n" +
            "2. Menyelesaikan pesanan (status SELESAI).\n" +
            "3. Meneruskan & mencairkan dana Escrow sebesar Rp " +
            String.format("%,.0f", ret.getRefundAmount()) + " ke saldo penjual.");
        info.getStyle().set("font-size", "13px").set("color", "#334155").set("line-height", "1.5");

        TextArea rejectionNotes = new TextArea("Alasan Penolakan (Wajib Diisi)");
        rejectionNotes.setPlaceholder("Contoh: Barang sesuai deskripsi atau kendala disebabkan oleh pembeli.");
        rejectionNotes.setWidthFull();
        rejectionNotes.setRequired(true);

        layout.add(info, rejectionNotes);
        d.add(layout);

        Button btnCancel = new Button("Batal", e -> d.close());
        Button btnConfirm = new Button("Tolak Komplain & Cairkan Dana", e -> {
            String note = rejectionNotes.getValue();
            if (note == null || note.isBlank()) {
                Notification.show("Silakan isi alasan penolakan komplain.", 2500, Notification.Position.TOP_CENTER);
                return;
            }
            try {
                orderService.rejectOrderReturn(ret, note.trim(), AuthGuard.getCurrentUser());
                Notification notif = Notification.show("Komplain #" + ret.getId() + " ditolak. Dana Escrow dicairkan ke penjual.", 3500, Notification.Position.TOP_CENTER);
                notif.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
                d.close();
                renderActiveTab();
            } catch (Exception ex) {
                Notification.show("Gagal menolak komplain: " + ex.getMessage(), 3000, Notification.Position.TOP_CENTER);
            }
        });
        btnConfirm.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnConfirm.getStyle().set("background", "#DC2626");

        d.getFooter().add(btnCancel, btnConfirm);
        d.open();
    }

    private void openReturnDetailDialog(OrderReturn ret) {
        Dialog d = new Dialog();
        d.setHeaderTitle("Rincian Sengketa Pesanan #" + (ret.getOrder() != null ? ret.getOrder().getOrderNumber() : "-"));
        d.setWidth("500px");

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(false);

        layout.add(new Paragraph("Tanggal Pengajuan: " + (ret.getCreatedAt() != null ? ret.getCreatedAt().format(DATE_FMT) : "-")));
        layout.add(new Paragraph("Pembeli (Pelapor): " + (ret.getBuyer() != null ? ret.getBuyer().getFullName() + " (" + ret.getBuyer().getEmail() + ")" : "-")));
        layout.add(new Paragraph("Penjual: " + (ret.getOrder() != null && ret.getOrder().getSeller() != null ? ret.getOrder().getSeller().getFullName() : "-")));
        layout.add(new Paragraph("Alasan Komplain: " + (ret.getReason() != null ? ret.getReason() : "-")));
        if (ret.getEvidenceUrl() != null && !ret.getEvidenceUrl().isBlank()) {
            layout.add(new Paragraph("Bukti Foto/URL: " + ret.getEvidenceUrl()));
        }
        layout.add(new Paragraph("Nominal Refund: Rp " + String.format("%,.0f", ret.getRefundAmount() != null ? ret.getRefundAmount().doubleValue() : 0)));
        layout.add(new Paragraph("Status Saat Ini: " + (ret.getStatus() != null ? ret.getStatus().name() : "-")));

        d.add(layout);
        Button btnClose = new Button("Tutup", e -> d.close());
        d.getFooter().add(btnClose);
        d.open();
    }

    // ==========================================
    // TAB: VERIFIKASI PEMBAYARAN
    // ==========================================

    private Component renderPaymentsTab() {
        Div wrapper = new Div();
        wrapper.getStyle().set("display", "flex").set("flex-direction", "column").set("gap", "20px");

        List<Payment> allPayments = paymentService.getAllPayments();
        List<Payment> pendingPayments = paymentService.getPendingVerificationPayments();
        long approvedCount = allPayments.stream().filter(p -> p.getTransactionStatus() == TransactionStatus.SETTLEMENT).count();

        // 1. Metric Cards
        HorizontalLayout metricsRow = new HorizontalLayout();
        metricsRow.setWidthFull();
        metricsRow.setSpacing(true);

        metricsRow.add(
            createMetricCard("MENUNGGU VERIFIKASI", String.valueOf(pendingPayments.size()) + " Struk", "Perlu tindakan admin", "#D97706", "#FEF3C7"),
            createMetricCard("PEMBAYARAN DITERIMA", String.valueOf(approvedCount) + " Transaksi", "Dana aman di Escrow", "#15803D", "#DCFCE7"),
            createMetricCard("TOTAL TRANSAKSI", String.valueOf(allPayments.size()) + " Pembayaran", "Semua kanal pembayaran", "#1E40AF", "#DBEAFE")
        );
        wrapper.add(metricsRow);

        // 2. Table of Payments
        Grid<Payment> grid = new Grid<>();
        grid.setWidthFull();
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);

        grid.addColumn(p -> p.getOrder() != null ? "#" + p.getOrder().getOrderNumber() : "-")
            .setHeader("No. Pesanan").setWidth("140px").setFlexGrow(0);

        grid.addColumn(p -> p.getCreatedAt() != null ? p.getCreatedAt().format(DATE_FMT) : "-")
            .setHeader("Tanggal").setWidth("160px").setFlexGrow(0);

        grid.addColumn(p -> (p.getOrder() != null && p.getOrder().getBuyer() != null) ? p.getOrder().getBuyer().getFullName() : "-")
            .setHeader("Pembeli").setWidth("160px");

        grid.addComponentColumn(p -> {
            Span methodBadge = new Span(p.getPaymentGateway() != null ? p.getPaymentGateway() : (p.getPaymentMethod() != null ? p.getPaymentMethod() : "MANUAL"));
            methodBadge.getStyle().set("font-size", "11px").set("font-weight", "700").set("padding", "3px 8px").set("border-radius", "6px")
                .set("background", "#EFF6FF").set("color", "#1E40AF");
            return methodBadge;
        }).setHeader("Metode / Channel").setWidth("150px").setFlexGrow(0);

        grid.addColumn(p -> "Rp " + String.format("%,.0f", p.getGrossAmount() != null ? p.getGrossAmount().doubleValue() : 0.0))
            .setHeader("Nominal").setWidth("130px").setFlexGrow(0);

        grid.addComponentColumn(p -> {
            if (p.getPaymentProofUrl() != null && !p.getPaymentProofUrl().isBlank()) {
                String imgUrl = p.getPaymentProofUrl().startsWith("/") ? p.getPaymentProofUrl() : "/" + p.getPaymentProofUrl();
                Image thumb = new Image(imgUrl, "Bukti");
                thumb.getStyle().set("width", "38px").set("height", "38px").set("object-fit", "cover").set("border-radius", "6px").set("cursor", "pointer").set("border", "1px solid #CBD5E1");
                thumb.addClickListener(e -> openPaymentProofPreviewDialog(p));
                return thumb;
            } else {
                Span noProof = new Span("Tanpa Bukti");
                noProof.getStyle().set("font-size", "11px").set("color", "#94A3B8");
                return noProof;
            }
        }).setHeader("Bukti Transfer").setWidth("120px").setFlexGrow(0);

        grid.addComponentColumn(p -> {
            Span statusBadge = new Span();
            statusBadge.getStyle().set("font-size", "11px").set("font-weight", "700").set("padding", "3px 8px").set("border-radius", "6px");
            if (p.getTransactionStatus() == TransactionStatus.SETTLEMENT) {
                statusBadge.setText("Terverifikasi");
                statusBadge.getStyle().set("background", "#DCFCE7").set("color", "#15803D");
            } else if (p.getTransactionStatus() == TransactionStatus.FAILURE || p.getTransactionStatus() == TransactionStatus.DENY) {
                statusBadge.setText("Ditolak");
                statusBadge.getStyle().set("background", "#FEE2E2").set("color", "#DC2626");
            } else {
                statusBadge.setText("Menunggu Cek");
                statusBadge.getStyle().set("background", "#FEF3C7").set("color", "#92400E");
            }
            return statusBadge;
        }).setHeader("Status").setWidth("130px").setFlexGrow(0);

        grid.addComponentColumn(p -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(true);

            if (p.getTransactionStatus() == TransactionStatus.PENDING) {
                Button btnApprove = new Button("Terima", VaadinIcon.CHECK.create(), e -> openApprovePaymentDialog(p));
                btnApprove.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
                btnApprove.getStyle().set("font-size", "12px").set("font-weight", "700").set("color", "#16A34A");

                Button btnReject = new Button("Tolak", VaadinIcon.CLOSE.create(), e -> openRejectPaymentDialog(p));
                btnReject.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
                btnReject.getStyle().set("font-size", "12px").set("font-weight", "700").set("color", "#DC2626");

                actions.add(btnApprove, btnReject);
            } else if (p.getPaymentProofUrl() != null && !p.getPaymentProofUrl().isBlank()) {
                Button btnView = new Button("Lihat Bukti", VaadinIcon.EYE.create(), e -> openPaymentProofPreviewDialog(p));
                btnView.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
                btnView.getStyle().set("font-size", "12px").set("font-weight", "700").set("color", "#2563EB");
                actions.add(btnView);
            }

            return actions;
        }).setHeader("Aksi Admin").setWidth("180px").setFlexGrow(0);

        grid.setItems(allPayments);

        Div card = createCardContainer("Daftar Pembayaran & Verifikasi Struk Transfer (" + allPayments.size() + ")",
            "Verifikasi struk pembayaran QRIS / Bank Transfer sebelum pesanan diproses oleh penjual");
        card.add(grid);
        wrapper.add(card);

        return wrapper;
    }

    private void openPaymentProofPreviewDialog(Payment payment) {
        Dialog d = new Dialog();
        d.setHeaderTitle("Bukti Pembayaran - #" + (payment.getOrder() != null ? payment.getOrder().getOrderNumber() : ""));
        d.setWidth("500px");

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);

        String imgUrl = payment.getPaymentProofUrl();
        if (imgUrl != null) {
            imgUrl = imgUrl.startsWith("/") ? imgUrl : "/" + imgUrl;
            Image img = new Image(imgUrl, "Bukti Pembayaran");
            img.setWidthFull();
            img.getStyle().set("max-height", "450px").set("object-fit", "contain").set("border-radius", "8px").set("border", "1px solid #E2E8F0");
            layout.add(img);
        }

        Span nominalText = new Span("Nominal: Rp " + String.format("%,.0f", payment.getGrossAmount() != null ? payment.getGrossAmount().doubleValue() : 0.0));
        nominalText.getStyle().set("font-weight", "800").set("color", "#001934");
        layout.add(nominalText);

        Button btnClose = new Button("Tutup", e -> d.close());
        d.getFooter().add(btnClose);
        d.add(layout);
        d.open();
    }

    private void openApprovePaymentDialog(Payment payment) {
        Dialog d = new Dialog();
        d.setHeaderTitle("Konfirmasi Terima Pembayaran");
        d.setWidth("450px");

        Paragraph p = new Paragraph("Verifikasi bahwa dana sebesar Rp " + String.format("%,.0f", payment.getGrossAmount() != null ? payment.getGrossAmount().doubleValue() : 0.0) + " telah masuk ke rekening / e-wallet ReWear. Pesanan akan otomatis berstatus DIPROSES.");
        p.getStyle().set("font-size", "14px").set("color", "#475569");

        TextField notesField = new TextField("Catatan Admin (Opsional)");
        notesField.setWidthFull();
        notesField.setPlaceholder("Contoh: Dana verified mutasi BCA");

        Button btnCancel = new Button("Batal", e -> d.close());
        btnCancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button btnConfirm = new Button("Ya, Terima & Proses Pesanan", VaadinIcon.CHECK_CIRCLE.create(), e -> {
            User admin = AuthGuard.getCurrentUser();
            paymentService.approvePayment(payment, notesField.getValue(), admin);
            Notification notif = Notification.show("Pembayaran berhasil diverifikasi! Pesanan siap diproses penjual.", 3000, Notification.Position.TOP_CENTER);
            notif.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            d.close();
            renderActiveTab();
        });
        btnConfirm.getStyle().set("background", "#16A34A").set("color", "#FFFFFF").set("font-weight", "700");

        d.add(new VerticalLayout(p, notesField));
        d.getFooter().add(btnCancel, btnConfirm);
        d.open();
    }

    private void openRejectPaymentDialog(Payment payment) {
        Dialog d = new Dialog();
        d.setHeaderTitle("Tolak Bukti Pembayaran");
        d.setWidth("450px");

        Paragraph p = new Paragraph("Tolak bukti pembayaran untuk pesanan #" + (payment.getOrder() != null ? payment.getOrder().getOrderNumber() : "") + ". Pembeli akan diminta mengunggah bukti yang valid.");
        p.getStyle().set("font-size", "14px").set("color", "#DC2626");

        TextField reasonField = new TextField("Alasan Penolakan");
        reasonField.setWidthFull();
        reasonField.setRequired(true);
        reasonField.setPlaceholder("Contoh: Struk tidak terbaca / Nominal kurang");

        Button btnCancel = new Button("Batal", e -> d.close());
        btnCancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button btnConfirm = new Button("Tolak Bukti", VaadinIcon.CLOSE_CIRCLE.create(), e -> {
            if (reasonField.isEmpty()) {
                Notification.show("Harap masukkan alasan penolakan.", 2500, Notification.Position.TOP_CENTER);
                return;
            }
            User admin = AuthGuard.getCurrentUser();
            paymentService.rejectPayment(payment, reasonField.getValue(), admin);
            Notification notif = Notification.show("Bukti pembayaran telah ditolak.", 3000, Notification.Position.TOP_CENTER);
            notif.addThemeVariants(NotificationVariant.LUMO_ERROR);
            d.close();
            renderActiveTab();
        });
        btnConfirm.getStyle().set("background", "#DC2626").set("color", "#FFFFFF").set("font-weight", "700");

        d.add(new VerticalLayout(p, reasonField));
        d.getFooter().add(btnCancel, btnConfirm);
        d.open();
    }
}
