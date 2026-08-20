package com.example.application.views.order;

import com.example.application.model.moderation.Review;
import com.example.application.model.order.Order;
import com.example.application.model.order.OrderItem;
import com.example.application.model.order.OrderStatus;
import com.example.application.model.user.User;
import com.example.application.service.moderation.ModerationService;
import com.example.application.service.order.OrderService;
import com.example.application.util.AuthGuard;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

@PageTitle("Riwayat Pesanan — ReWear")
@Route(value = "orders", layout = MainLayout.class)
public class OrderHistoryView extends Div implements BeforeEnterObserver {

    private final OrderService orderService;
    private final ModerationService moderationService;
    private final Div contentContainer = new Div();
    private String activeFilter = "SEMUA";

    private static final DateTimeFormatter DATE_FMT =
        DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    public OrderHistoryView(OrderService orderService, ModerationService moderationService) {
        this.orderService = orderService;
        this.moderationService = moderationService;
        setWidthFull();
        getElement().getStyle()
            .set("background", "linear-gradient(160deg, #F0F4FF 0%, #F8FAFF 100%)")
            .set("min-height", "100vh")
            .set("padding", "40px 0 80px");
        add(contentContainer);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!AuthGuard.requireLogin(UI.getCurrent())) return;
        buildView();
    }

    // ══════════════════════════════════════════════════════════
    // MAIN VIEW
    // ══════════════════════════════════════════════════════════

    private void buildView() {
        contentContainer.removeAll();
        contentContainer.getElement().getStyle()
            .set("max-width", "900px").set("margin", "0 auto").set("padding", "0 24px");

        User user = AuthGuard.getCurrentUser();
        if (user == null) return;

        List<Order> allOrders = orderService.getBuyerOrders(user);

        // ── Page Header ──────────────────────────────────────────
        Div header = new Div();
        header.getElement().getStyle()
            .set("margin-bottom", "32px")
            .set("padding-bottom", "24px")
            .set("border-bottom", "1px solid #E2E8F0");

        Div navRow = new Div();
        navRow.getElement().getStyle().set("margin-bottom", "16px");

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
        navRow.add(btnBack);
        header.add(navRow);

        Div titleRow = new Div();
        titleRow.getElement().getStyle()
            .set("display", "flex").set("align-items", "center").set("gap", "12px");

        Div iconBox = new Div();
        iconBox.getElement().setProperty("innerHTML",
            "<div style='width:48px;height:48px;border-radius:14px;" +
            "background:linear-gradient(135deg,#001934,#0A3D7A);" +
            "display:flex;align-items:center;justify-content:center;font-size:22px;'>📦</div>");

        Div textBlock = new Div();
        H1 title = new H1("Riwayat Pesanan");
        title.getElement().getStyle()
            .set("font-size", "26px").set("font-weight", "900")
            .set("color", "#001934").set("margin", "0 0 4px 0")
            .set("letter-spacing", "-0.5px");
        Span sub = new Span("Pantau dan kelola semua pesananmu dengan mudah");
        sub.getElement().getStyle()
            .set("font-size", "14px").set("color", "#64748B");
        textBlock.add(title, sub);
        titleRow.add(iconBox, textBlock);

        // Stats row
        long countSemua = allOrders.size();
        long countProses = allOrders.stream().filter(o ->
            o.getStatus() == OrderStatus.DIPROSES || o.getStatus() == OrderStatus.DIBAYAR).count();
        long countKirim  = allOrders.stream().filter(o ->
            o.getStatus() == OrderStatus.DIKIRIM).count();
        long countSelesai = allOrders.stream().filter(o ->
            o.getStatus() == OrderStatus.SELESAI).count();

        Div statsRow = new Div();
        statsRow.getElement().getStyle()
            .set("display", "flex").set("gap", "12px").set("margin-top", "20px").set("flex-wrap", "wrap");
        statsRow.add(
            buildStatChip("📋 " + countSemua, "Total Pesanan", "#EFF6FF", "#1E40AF"),
            buildStatChip("⚙️ " + countProses, "Diproses", "#FEF3C7", "#92400E"),
            buildStatChip("🚚 " + countKirim, "Dikirim", "#F0FDF4", "#15803D"),
            buildStatChip("🎉 " + countSelesai, "Selesai", "#F0FDF4", "#166534")
        );

        header.add(titleRow, statsRow);
        contentContainer.add(header);

        // ── Filter Tabs ──────────────────────────────────────────
        Div filterTabs = new Div();
        filterTabs.getElement().getStyle()
            .set("display", "flex").set("gap", "6px")
            .set("margin-bottom", "24px").set("flex-wrap", "wrap");

        String[][] tabs = {
            {"SEMUA", "Semua"},
            {"MENUNGGU_PEMBAYARAN", "Belum Bayar"},
            {"DIPROSES", "Diproses"},
            {"DIKIRIM", "Dikirim"},
            {"SELESAI", "Selesai"},
            {"DIBATALKAN", "Dibatalkan"},
        };
        for (String[] tab : tabs) {
            Button btn = new Button(tab[1]);
            boolean isActive = activeFilter.equals(tab[0]);
            btn.getElement().getStyle()
                .set("background", isActive ? "#001934" : "#FFFFFF")
                .set("color", isActive ? "#F5C45E" : "#64748B")
                .set("border", isActive ? "none" : "1.5px solid #E2E8F0")
                .set("border-radius", "20px")
                .set("font-weight", "700").set("font-size", "13px")
                .set("padding", "8px 16px").set("cursor", "pointer")
                .set("transition", "all 0.2s ease");
            final String key = tab[0];
            btn.addClickListener(e -> { activeFilter = key; buildView(); });
            filterTabs.add(btn);
        }
        contentContainer.add(filterTabs);

        // ── Order list (filtered) ────────────────────────────────
        List<Order> displayed = allOrders.stream().filter(o -> {
            if (activeFilter.equals("SEMUA")) return true;
            if (activeFilter.equals("DIPROSES"))
                return o.getStatus() == OrderStatus.DIPROSES || o.getStatus() == OrderStatus.DIBAYAR;
            return o.getStatus().name().equals(activeFilter);
        }).toList();

        if (displayed.isEmpty()) {
            contentContainer.add(buildEmptyState());
        } else {
            for (Order order : displayed) {
                contentContainer.add(buildOrderCard(order, user));
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // ORDER CARD
    // ══════════════════════════════════════════════════════════

    private Div buildOrderCard(Order order, User user) {
        Div card = new Div();
        card.getElement().getStyle()
            .set("background", "#FFFFFF")
            .set("border-radius", "20px")
            .set("border", "1px solid #E8EEF8")
            .set("box-shadow", "0 2px 16px rgba(0,25,52,0.05)")
            .set("margin-bottom", "16px")
            .set("overflow", "hidden")
            .set("transition", "box-shadow 0.2s ease");
        card.getElement().addEventListener("mouseover", e ->
            card.getElement().getStyle().set("box-shadow", "0 8px 32px rgba(0,25,52,0.10)"));
        card.getElement().addEventListener("mouseout", e ->
            card.getElement().getStyle().set("box-shadow", "0 2px 16px rgba(0,25,52,0.05)"));

        // ── Status stripe at top ─────────────────────────────────
        Div stripe = new Div();
        stripe.getElement().getStyle()
            .set("height", "4px")
            .set("background", statusGradient(order.getStatus()));
        card.add(stripe);

        // ── Card Header ─────────────────────────────────────────
        Div cardHeader = new Div();
        cardHeader.getElement().getStyle()
            .set("display", "flex").set("align-items", "center")
            .set("justify-content", "space-between")
            .set("padding", "16px 24px 12px")
            .set("border-bottom", "1px solid #F1F5F9");

        // Order number + date
        Div metaLeft = new Div();
        metaLeft.getElement().getStyle().set("display", "flex").set("flex-direction", "column").set("gap", "3px");

        Span orderNum = new Span("# " + order.getOrderNumber());
        orderNum.getElement().getStyle()
            .set("font-size", "13px").set("font-weight", "800")
            .set("color", "#001934").set("font-family", "monospace")
            .set("letter-spacing", "0.5px");

        String dateStr = order.getCreatedAt() != null ? order.getCreatedAt().format(DATE_FMT) : "-";
        Span orderDate = new Span(dateStr);
        orderDate.getElement().getStyle()
            .set("font-size", "12px").set("color", "#94A3B8");

        metaLeft.add(orderNum, orderDate);

        Span statusBadge = buildStatusBadge(order.getStatus());
        cardHeader.add(metaLeft, statusBadge);
        card.add(cardHeader);

        // ── Items Section ────────────────────────────────────────
        List<OrderItem> items = orderService.getOrderItems(order);
        Div itemsSection = new Div();
        itemsSection.getElement().getStyle()
            .set("padding", "16px 24px")
            .set("display", "flex").set("flex-direction", "column").set("gap", "10px");

        if (!items.isEmpty()) {
            for (int i = 0; i < Math.min(items.size(), 3); i++) {
                OrderItem item = items.get(i);
                Div itemRow = new Div();
                itemRow.getElement().getStyle()
                    .set("display", "flex").set("align-items", "center")
                    .set("gap", "14px").set("padding", "10px 14px")
                    .set("background", "#F8FAFF").set("border-radius", "12px")
                    .set("border", "1px solid #EEF2FF");

                // Product icon box
                Div productIcon = new Div();
                productIcon.getElement().setProperty("innerHTML",
                    "<div style='width:36px;height:36px;border-radius:10px;" +
                    "background:linear-gradient(135deg,#001934,#0A3D7A);" +
                    "display:flex;align-items:center;justify-content:center;flex-shrink:0;'>" +
                    "<svg width='18' height='18' viewBox='0 0 24 24' fill='none' stroke='#F5C45E' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><path d='M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z'></path><line x1='3' y1='6' x2='21' y2='6'></line><path d='M16 10a4 4 0 0 1-8 0'></path></svg>" +
                    "</div>");

                Div itemText = new Div();
                itemText.getElement().getStyle().set("flex", "1");

                Span itemName = new Span(item.getProductNameSnapshot());
                itemName.getElement().getStyle()
                    .set("font-size", "14px").set("font-weight", "700")
                    .set("color", "#1E293B").set("display", "block");

                Span itemMeta = new Span(
                    "Qty: " + item.getQuantity() +
                    "   ×   Rp " + String.format("%,.0f", item.getPriceSnapshot()));
                itemMeta.getElement().getStyle()
                    .set("font-size", "12px").set("color", "#64748B").set("margin-top", "2px").set("display", "block");

                itemText.add(itemName, itemMeta);

                // Item subtotal
                Span itemTotal = new Span("Rp " + String.format("%,.0f",
                    item.getPriceSnapshot().multiply(java.math.BigDecimal.valueOf(item.getQuantity()))));
                itemTotal.getElement().getStyle()
                    .set("font-size", "13px").set("font-weight", "800")
                    .set("color", "#001934").set("flex-shrink", "0");

                itemRow.add(productIcon, itemText, itemTotal);
                itemsSection.add(itemRow);
            }

            if (items.size() > 3) {
                Span more = new Span("+" + (items.size() - 3) + " produk lainnya");
                more.getElement().getStyle()
                    .set("font-size", "12px").set("color", "#64748B")
                    .set("font-style", "italic").set("padding-left", "4px");
                itemsSection.add(more);
            }
        }
        card.add(itemsSection);

        // ── Card Footer ──────────────────────────────────────────
        Div cardFooter = new Div();
        cardFooter.getElement().getStyle()
            .set("display", "flex").set("align-items", "center")
            .set("justify-content", "space-between").set("flex-wrap", "wrap").set("gap", "12px")
            .set("padding", "14px 24px 18px")
            .set("background", "#FAFBFF")
            .set("border-top", "1px solid #F1F5F9");

        // Total
        Div totalWrap = new Div();
        totalWrap.getElement().getStyle().set("display", "flex").set("flex-direction", "column").set("gap", "2px");
        Span totalLabel = new Span("Total Pembayaran");
        totalLabel.getElement().getStyle().set("font-size", "12px").set("color", "#94A3B8").set("font-weight", "600");
        Span totalValue = new Span("Rp " + String.format("%,.0f", order.getTotalAmount()));
        totalValue.getElement().getStyle()
            .set("font-size", "20px").set("font-weight", "900").set("color", "#001934");
        totalWrap.add(totalLabel, totalValue);

        // Action buttons
        Div actionsWrap = new Div();
        actionsWrap.getElement().getStyle()
            .set("display", "flex").set("gap", "8px").set("align-items", "center").set("flex-wrap", "wrap");

        // Detail button (always visible)
        Button btnDetail = new Button("Lihat Detail");
        btnDetail.getElement().getStyle()
            .set("background", "transparent").set("color", "#0A3D7A")
            .set("border", "1.5px solid #BFDBFE").set("border-radius", "10px")
            .set("font-weight", "700").set("font-size", "13px")
            .set("padding", "8px 16px").set("cursor", "pointer");
        btnDetail.addClickListener(e -> openOrderDetailModal(order));
        actionsWrap.add(btnDetail);

        if (order.getStatus() == OrderStatus.DIKIRIM && order.getTrackingNumber() != null && !order.getTrackingNumber().isBlank()) {
            Div trackingBox = new Div();
            trackingBox.getElement().getStyle()
                .set("margin", "0 24px 14px")
                .set("padding", "10px 16px")
                .set("background", "#F0FDF4")
                .set("border", "1px solid #BBF7D0")
                .set("border-radius", "8px")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "space-between");

            Div trackLeft = new Div();
            String courier = order.getCourierName() != null ? order.getCourierName().name() : "Kurir";
            Span tTitle = new Span("Pengiriman (" + courier + "): ");
            tTitle.getStyle().set("font-size", "12px").set("font-weight", "700").set("color", "#166534");
            Span tNum = new Span(order.getTrackingNumber());
            tNum.getStyle().set("font-size", "12px").set("font-weight", "800").set("color", "#001934");
            trackLeft.add(tTitle, tNum);

            Button btnCopy = new Button("Salin", VaadinIcon.COPY.create(), e -> {
                UI.getCurrent().getPage().executeJs("navigator.clipboard.writeText($0);", order.getTrackingNumber());
                Notification.show("Nomor resi berhasil disalin!", 2000, Notification.Position.TOP_CENTER);
            });
            btnCopy.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            btnCopy.getStyle().set("font-size", "11px").set("font-weight", "700").set("color", "#15803D").set("padding", "0").set("cursor", "pointer");

            trackingBox.add(trackLeft, btnCopy);
            card.add(trackingBox);
        }

        // Conditional action buttons
        if (order.getStatus() == OrderStatus.MENUNGGU_PEMBAYARAN) {
            Button btnBayar = new Button("Bayar Sekarang");
            btnBayar.getElement().getStyle()
                .set("background", "#001934")
                .set("color", "#FFFFFF").set("border", "none")
                .set("border-radius", "8px").set("font-weight", "700")
                .set("font-size", "13px").set("padding", "8px 18px").set("cursor", "pointer");
            btnBayar.addClickListener(e -> {
                orderService.updateOrderStatus(order, OrderStatus.DIBAYAR,
                    "Pembayaran berhasil dikonfirmasi oleh pembeli.", user);
                Notification.show("Pembayaran berhasil dikonfirmasi.", 2500, Notification.Position.TOP_CENTER);
                buildView();
            });

            Button btnBatal = new Button("Batalkan");
            btnBatal.getElement().getStyle()
                .set("background", "transparent").set("color", "#EF4444")
                .set("border", "1.5px solid #FCA5A5").set("border-radius", "8px")
                .set("font-weight", "700").set("font-size", "13px")
                .set("padding", "8px 16px").set("cursor", "pointer");
            btnBatal.addClickListener(e -> {
                orderService.updateOrderStatus(order, OrderStatus.DIBATALKAN,
                    "Pesanan dibatalkan oleh pembeli.", user);
                Notification.show("Pesanan #" + order.getOrderNumber() + " dibatalkan.", 2500, Notification.Position.TOP_CENTER);
                buildView();
            });
            actionsWrap.add(btnBatal, btnBayar);
        }

        if (order.getStatus() == OrderStatus.DIPROSES || order.getStatus() == OrderStatus.DIBAYAR || order.getStatus() == OrderStatus.DIKIRIM || order.getStatus() == OrderStatus.DITERIMA) {
            Button btnTerima = new Button("Konfirmasi Diterima", VaadinIcon.CHECK.create());
            btnTerima.getElement().getStyle()
                .set("background", "#16A34A")
                .set("color", "#FFFFFF").set("border", "none")
                .set("border-radius", "8px").set("font-weight", "700")
                .set("font-size", "13px").set("padding", "8px 18px").set("cursor", "pointer");
            btnTerima.addClickListener(e -> {
                Dialog confirmDialog = new Dialog();
                confirmDialog.setWidth("440px");
                confirmDialog.setHeaderTitle("Konfirmasi Penerimaan Barang");
                VerticalLayout body = new VerticalLayout();
                body.setSpacing(true);
                body.setPadding(false);
                Paragraph p1 = new Paragraph("Apakah Anda telah menerima pesanan #" + order.getOrderNumber() + " dalam kondisi baik?");
                p1.getStyle().set("color", "#1E293B").set("margin", "0").set("font-weight", "700");
                Paragraph p2 = new Paragraph("Setelah dikonfirmasi, status pesanan akan menjadi 'Selesai' dan dana pembayaran akan diteruskan dari Escrow ke saldo penjual.");
                p2.getStyle().set("color", "#64748B").set("font-size", "13px").set("line-height", "1.5").set("margin", "0");
                body.add(p1, p2);
                confirmDialog.add(body);

                Button btnYes = new Button("Ya, Sudah Diterima", e2 -> {
                    orderService.updateOrderStatus(order, OrderStatus.SELESAI,
                        "Dikonfirmasi diterima oleh pembeli.", user);
                    Notification.show("Pesanan #" + order.getOrderNumber() + " selesai!", 2500, Notification.Position.TOP_CENTER);
                    confirmDialog.close();
                    buildView();
                    openReviewModal(order, user);
                });
                btnYes.getStyle().set("background", "#16A34A").set("color", "#FFFFFF").set("font-weight", "700");
                Button btnNo = new Button("Batal", e2 -> confirmDialog.close());
                confirmDialog.getFooter().add(btnNo, btnYes);
                confirmDialog.open();
            });
            actionsWrap.add(btnTerima);
        }

        if (order.getStatus() == OrderStatus.SELESAI) {
            User currentUser = AuthGuard.getCurrentUser();
            boolean alreadyReviewed = currentUser != null &&
                moderationService.hasReviewed(order.getId(), currentUser.getId());

            if (alreadyReviewed) {
                actionsWrap.add(buildReviewedBadge());
            } else {
                Button btnReview = new Button("Beri Ulasan", VaadinIcon.STAR.create());
                btnReview.getElement().getStyle()
                    .set("background", "#D97706")
                    .set("color", "#FFFFFF").set("border", "none")
                    .set("border-radius", "8px").set("font-weight", "700")
                    .set("font-size", "13px").set("padding", "8px 18px").set("cursor", "pointer");
                btnReview.addClickListener(e -> openReviewModal(order, user));
                actionsWrap.add(btnReview);
            }
        }

        cardFooter.add(totalWrap, actionsWrap);
        card.add(cardFooter);
        return card;
    }

    // ══════════════════════════════════════════════════════════
    // REVIEW MODAL
    // ══════════════════════════════════════════════════════════

    private void openReviewModal(Order order, User buyer) {
        List<OrderItem> items = orderService.getOrderItems(order);

        Dialog d = new Dialog();
        d.setWidth("500px");
        d.setCloseOnOutsideClick(false);

        // ── Modal Header ──
        Div header = new Div();
        header.getElement().getStyle()
            .set("display", "flex").set("align-items", "center").set("gap", "12px")
            .set("padding", "24px 24px 16px")
            .set("border-bottom", "1px solid #E8EEF8");
        Div iconBox = new Div();
        iconBox.getElement().setProperty("innerHTML",
            "<div style='width:40px;height:40px;border-radius:12px;" +
            "background:linear-gradient(135deg,#F59E0B,#D97706);" +
            "display:flex;align-items:center;justify-content:center;font-size:18px;'>⭐</div>");
        Div titleBlock = new Div();
        Span modalTitle = new Span("Beri Ulasan");
        modalTitle.getElement().getStyle()
            .set("font-size", "17px").set("font-weight", "900")
            .set("color", "#001934").set("display", "block");
        Span sub = new Span("Pesanan #" + order.getOrderNumber());
        sub.getElement().getStyle().set("font-size", "12px").set("color", "#94A3B8").set("font-family", "monospace");
        titleBlock.add(modalTitle, sub);
        header.add(iconBox, titleBlock);

        // ── Modal Body ──
        Div body = new Div();
        body.getElement().getStyle()
            .set("padding", "20px 24px").set("display", "flex")
            .set("flex-direction", "column").set("gap", "20px");

        // Product chips (show items in this order)
        if (!items.isEmpty()) {
            Div itemsWrap = new Div();
            itemsWrap.getElement().getStyle().set("display", "flex").set("flex-direction", "column").set("gap", "8px");
            Span itemsLabel = new Span("Produk yang dibeli:");
            itemsLabel.getElement().getStyle().set("font-size", "12px").set("font-weight", "700")
                .set("color", "#64748B").set("text-transform", "uppercase").set("letter-spacing", "0.5px");
            itemsWrap.add(itemsLabel);
            for (OrderItem it : items) {
                Span chip = new Span("👕 " + it.getProductNameSnapshot());
                chip.getElement().getStyle()
                    .set("background", "#EFF6FF").set("color", "#1E40AF")
                    .set("font-size", "13px").set("font-weight", "700")
                    .set("padding", "4px 12px").set("border-radius", "20px")
                    .set("display", "inline-block").set("margin-right", "6px").set("margin-bottom", "4px");
                itemsWrap.add(chip);
            }
            body.add(itemsWrap);
        }

        // ── Star Rating ──
        Div starSection = new Div();
        starSection.getElement().getStyle().set("display", "flex").set("flex-direction", "column").set("gap", "10px");
        Span starLabel = new Span("Rating:");
        starLabel.getElement().getStyle().set("font-size", "14px").set("font-weight", "700").set("color", "#001934");

        // Rating state holder (1 element array to allow mutation inside lambda)
        int[] selectedRating = {5};
        Span[] stars = new Span[5];
        Span ratingHint = new Span("Sangat Puas");
        ratingHint.getElement().getStyle().set("font-size", "13px").set("color", "#F59E0B").set("font-weight", "700");

        String[] ratingLabels = {"", "Sangat Buruk", "Buruk", "Cukup", "Bagus", "Sangat Puas"};

        HorizontalLayout starsRow = new HorizontalLayout();
        starsRow.setSpacing(false);
        starsRow.getElement().getStyle().set("gap", "4px");

        for (int i = 1; i <= 5; i++) {
            Span star = new Span("★");
            final int starVal = i;
            star.getElement().getStyle()
                .set("font-size", "36px")
                .set("color", i <= selectedRating[0] ? "#F59E0B" : "#E2E8F0")
                .set("cursor", "pointer")
                .set("transition", "color 0.15s ease");
            star.getElement().addEventListener("mouseover", e -> {
                for (int j = 0; j < 5; j++) {
                    stars[j].getElement().getStyle().set("color", j < starVal ? "#FBBF24" : "#E2E8F0");
                }
            });
            star.getElement().addEventListener("mouseout", e -> {
                for (int j = 0; j < 5; j++) {
                    stars[j].getElement().getStyle().set("color", j < selectedRating[0] ? "#F59E0B" : "#E2E8F0");
                }
            });
            star.getElement().addEventListener("click", e -> {
                selectedRating[0] = starVal;
                for (int j = 0; j < 5; j++) {
                    stars[j].getElement().getStyle().set("color", j < starVal ? "#F59E0B" : "#E2E8F0");
                }
                ratingHint.setText(ratingLabels[starVal]);
            });
            stars[i - 1] = star;
            starsRow.add(star);
        }

        starSection.add(starLabel, starsRow, ratingHint);
        body.add(starSection);

        // ── Komentar ──
        TextArea commentArea = new TextArea();
        commentArea.setPlaceholder("Ceritakan pengalamanmu dengan produk ini... (opsional)");
        commentArea.setLabel("Komentar Ulasan");
        commentArea.setWidthFull();
        commentArea.setMaxLength(500);
        commentArea.getElement().getStyle().set("min-height", "100px");
        body.add(commentArea);

        // ── Footer Buttons ──
        Div footer = new Div();
        footer.getElement().getStyle()
            .set("padding", "12px 24px 20px")
            .set("display", "flex").set("justify-content", "flex-end")
            .set("gap", "10px").set("border-top", "1px solid #E8EEF8");

        Button btnCancel = new Button("Batal");
        btnCancel.getElement().getStyle()
            .set("background", "transparent").set("color", "#64748B")
            .set("border", "1.5px solid #E2E8F0").set("border-radius", "10px")
            .set("font-weight", "700").set("padding", "10px 20px").set("cursor", "pointer");
        btnCancel.addClickListener(e -> d.close());

        Button btnSubmit = new Button("Kirim Ulasan");
        btnSubmit.getElement().getStyle()
            .set("background", "linear-gradient(135deg, #F59E0B, #D97706)")
            .set("color", "#FFFFFF").set("border", "none")
            .set("border-radius", "10px").set("font-weight", "800")
            .set("padding", "10px 24px").set("cursor", "pointer");
        btnSubmit.addClickListener(e -> {
            int rating = selectedRating[0];
            String comment = commentArea.getValue();

            // Use first item's product and its seller; if multiple items, review the order seller
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
                buildView(); // refresh to show Sudah Diulas badge
            } catch (Exception ex) {
                Notification.show("Gagal mengirim ulasan: " + ex.getMessage(), 3000, Notification.Position.TOP_CENTER);
            }
        });

        footer.add(btnCancel, btnSubmit);
        d.getElement().getStyle().set("border-radius", "24px").set("overflow", "hidden").set("padding", "0");
        d.add(header, body, footer);
        d.open();
    }

    private Span buildReviewedBadge() {
        Span badge = new Span("Sudah Diulas");
        badge.getElement().getStyle()
            .set("background", "#DCFCE7").set("color", "#166534")
            .set("font-size", "12px").set("font-weight", "800")
            .set("padding", "6px 14px").set("border-radius", "20px")
            .set("border", "1px solid #BBF7D0");
        return badge;
    }

    // ══════════════════════════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════════════════════════

    private Div buildStatChip(String value, String label, String bg, String color) {
        Div chip = new Div();
        chip.getElement().getStyle()
            .set("background", bg).set("border-radius", "12px")
            .set("padding", "10px 16px").set("display", "flex")
            .set("flex-direction", "column").set("gap", "2px")
            .set("border", "1px solid " + color + "33");

        Span v = new Span(value);
        v.getElement().getStyle()
            .set("font-size", "16px").set("font-weight", "900").set("color", color);

        Span l = new Span(label);
        l.getElement().getStyle()
            .set("font-size", "11px").set("color", color).set("font-weight", "600")
            .set("opacity", "0.8");

        chip.add(v, l);
        return chip;
    }

    private Span buildStatusBadge(OrderStatus status) {
        Span badge = new Span();
        badge.getElement().getStyle()
            .set("font-size", "12px").set("font-weight", "800")
            .set("padding", "5px 12px").set("border-radius", "20px")
            .set("white-space", "nowrap").set("letter-spacing", "0.3px");

        switch (status) {
            case MENUNGGU_PEMBAYARAN -> {
                badge.setText("Menunggu Pembayaran");
                badge.getElement().getStyle().set("background", "#FEF3C7").set("color", "#92400E");
            }
            case DIBAYAR -> {
                badge.setText("Sudah Dibayar");
                badge.getElement().getStyle().set("background", "#DCFCE7").set("color", "#166534");
            }
            case DIPROSES -> {
                badge.setText("Sedang Diproses");
                badge.getElement().getStyle().set("background", "#EFF6FF").set("color", "#1E40AF");
            }
            case DIKIRIM -> {
                badge.setText("Dalam Pengiriman");
                badge.getElement().getStyle().set("background", "#ECFDF5").set("color", "#065F46");
            }
            case DITERIMA -> {
                badge.setText("Pesanan Diterima");
                badge.getElement().getStyle().set("background", "#F0FDF4").set("color", "#15803D");
            }
            case SELESAI -> {
                badge.setText("Selesai");
                badge.getElement().getStyle().set("background", "#DCFCE7").set("color", "#166534");
            }
            case KOMPLAIN -> {
                badge.setText("Komplain / Retur");
                badge.getElement().getStyle().set("background", "#FEF2F2").set("color", "#991B1B");
            }
            case DIBATALKAN -> {
                badge.setText("Dibatalkan");
                badge.getElement().getStyle().set("background", "#F1F5F9").set("color", "#64748B");
            }
        }
        return badge;
    }

    private String statusGradient(OrderStatus status) {
        return switch (status) {
            case MENUNGGU_PEMBAYARAN -> "linear-gradient(90deg, #F59E0B, #FBBF24)";
            case DIBAYAR            -> "linear-gradient(90deg, #10B981, #34D399)";
            case DIPROSES           -> "linear-gradient(90deg, #3B82F6, #60A5FA)";
            case DIKIRIM            -> "linear-gradient(90deg, #059669, #10B981)";
            case DITERIMA           -> "linear-gradient(90deg, #0EA5E9, #38BDF8)";
            case SELESAI            -> "linear-gradient(90deg, #16A34A, #22C55E)";
            case KOMPLAIN           -> "linear-gradient(90deg, #DC2626, #F87171)";
            case DIBATALKAN         -> "linear-gradient(90deg, #94A3B8, #CBD5E1)";
            default                 -> "linear-gradient(90deg, #CBD5E1, #E2E8F0)";
        };
    }

    private Div buildEmptyState() {
        Div empty = new Div();
        empty.getElement().getStyle()
            .set("text-align", "center").set("padding", "80px 32px")
            .set("background", "#FFFFFF").set("border-radius", "24px")
            .set("border", "1px solid #E2E8F0")
            .set("box-shadow", "0 2px 16px rgba(0,25,52,0.04)")
            .set("margin-top", "8px");
        empty.getElement().setProperty("innerHTML",
            "<div style='width:64px;height:64px;margin:0 auto 20px;border-radius:16px;background:#F1F5F9;display:flex;align-items:center;justify-content:center;'>" +
            "<svg width='32' height='32' viewBox='0 0 24 24' fill='none' stroke='#64748B' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><path d='M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z'></path><line x1='3' y1='6' x2='21' y2='6'></line><path d='M16 10a4 4 0 0 1-8 0'></path></svg>" +
            "</div>" +
            "<h3 style='font-size:22px;font-weight:900;color:#001934;margin:0 0 10px;'>Belum Ada Pesanan</h3>" +
            "<p style='font-size:14px;color:#64748B;margin:0 0 28px;line-height:1.6;'>Kamu belum pernah melakukan pembelian.<br>Yuk mulai temukan barang thrift favoritmu!</p>"
        );
        Button btnShop = new Button("Jelajahi Produk");
        btnShop.getElement().getStyle()
            .set("background", "linear-gradient(135deg, #001934, #0A3D7A)")
            .set("color", "#F5C45E").set("border", "none")
            .set("border-radius", "12px").set("font-weight", "800")
            .set("font-size", "15px").set("padding", "12px 28px").set("cursor", "pointer");
        btnShop.addClickListener(e -> UI.getCurrent().navigate(""));
        empty.add(btnShop);
        return empty;
    }

    private void openOrderDetailModal(Order order) {
        Dialog d = new Dialog();
        d.setWidth("520px");

        // Custom header
        Div modalHeader = new Div();
        modalHeader.getElement().getStyle()
            .set("display", "flex").set("align-items", "center")
            .set("gap", "12px").set("padding", "24px 24px 16px")
            .set("border-bottom", "1px solid #E8EEF8");

        Div modalIconBox = new Div();
        modalIconBox.getElement().setProperty("innerHTML",
            "<div style='width:40px;height:40px;border-radius:12px;" +
            "background:linear-gradient(135deg,#001934,#0A3D7A);" +
            "display:flex;align-items:center;justify-content:center;'>" +
            "<svg width='20' height='20' viewBox='0 0 24 24' fill='none' stroke='#F5C45E' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><path d='M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z'></path><polyline points='3.27 6.96 12 12.01 20.73 6.96'></polyline><line x1='12' y1='22.08' x2='12' y2='12'></line></svg>" +
            "</div>");

        Div modalTitleBlock = new Div();
        Span modalTitle = new Span("Rincian Pesanan");
        modalTitle.getElement().getStyle()
            .set("font-size", "17px").set("font-weight", "900")
            .set("color", "#001934").set("display", "block");
        Span modalOrderNum = new Span("# " + order.getOrderNumber());
        modalOrderNum.getElement().getStyle()
            .set("font-size", "12px").set("color", "#94A3B8")
            .set("font-family", "monospace");
        modalTitleBlock.add(modalTitle, modalOrderNum);
        modalHeader.add(modalIconBox, modalTitleBlock);

        // Body
        Div body = new Div();
        body.getElement().getStyle()
            .set("padding", "20px 24px").set("display", "flex")
            .set("flex-direction", "column").set("gap", "12px");

        body.add(buildDetailRow("📍", "Alamat Pengiriman",
            order.getShippingAddress() != null ? order.getShippingAddress() : "-"));
        body.add(buildDetailRow("💳", "Metode Pembayaran",
            order.getPaymentMethod() != null ? order.getPaymentMethod() : "-"));
        body.add(buildDetailRow("🚚", "Metode Pengiriman",
            order.getShippingMethod() != null ? order.getShippingMethod().name() : "-"));
        body.add(buildDetailRow("💰", "Total Pembayaran",
            "Rp " + String.format("%,.0f", order.getTotalAmount())));
        body.add(buildDetailRow("📊", "Status",
            order.getStatus().name().replace("_", " ")));

        // Footer
        Div footer = new Div();
        footer.getElement().getStyle()
            .set("padding", "12px 24px 20px")
            .set("display", "flex").set("justify-content", "flex-end")
            .set("border-top", "1px solid #E8EEF8");

        Button btnClose = new Button("Tutup");
        btnClose.getElement().getStyle()
            .set("background", "#001934").set("color", "#F5C45E")
            .set("border", "none").set("border-radius", "10px")
            .set("font-weight", "700").set("padding", "10px 24px")
            .set("cursor", "pointer");
        btnClose.addClickListener(e -> d.close());
        footer.add(btnClose);

        d.getElement().getStyle().set("border-radius", "24px").set("overflow", "hidden").set("padding", "0");
        d.add(modalHeader, body, footer);
        d.open();
    }

    private Div buildDetailRow(String icon, String label, String value) {
        Div row = new Div();
        row.getElement().getStyle()
            .set("display", "flex").set("align-items", "flex-start")
            .set("gap", "12px").set("padding", "12px 16px")
            .set("background", "#F8FAFF").set("border-radius", "12px")
            .set("border", "1px solid #EEF2FF");

        Span ic = new Span(icon);
        ic.getElement().getStyle().set("font-size", "18px").set("flex-shrink", "0").set("margin-top", "1px");

        Div textCol = new Div();
        textCol.getElement().getStyle().set("display", "flex").set("flex-direction", "column").set("gap", "2px");

        Span lbl = new Span(label);
        lbl.getElement().getStyle()
            .set("font-size", "11px").set("color", "#94A3B8").set("font-weight", "700")
            .set("text-transform", "uppercase").set("letter-spacing", "0.5px");

        Span val = new Span(value);
        val.getElement().getStyle()
            .set("font-size", "14px").set("font-weight", "700").set("color", "#1E293B");

        textCol.add(lbl, val);
        row.add(ic, textCol);
        return row;
    }
}
