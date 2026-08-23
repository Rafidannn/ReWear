package com.example.application.views.order;

import com.example.application.model.moderation.Review;
import com.example.application.model.order.Order;
import com.example.application.model.order.OrderItem;
import com.example.application.model.order.OrderReturn;
import com.example.application.model.order.OrderStatus;
import com.example.application.model.order.ReturnStatus;
import com.example.application.model.user.User;
import com.example.application.service.moderation.ModerationService;
import com.example.application.service.order.OrderService;
import com.example.application.util.AuthGuard;
import com.example.application.views.MainLayout;
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
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

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
            .set("max-width", "600px").set("margin", "0 auto")
            .set("padding", "0 16px 100px 16px");

        User user = AuthGuard.getCurrentUser();
        if (user == null) return;

        List<Order> allOrders = orderService.getBuyerOrders(user);

        // ── Mobile Header ──────────────────────────────────────────
        Div header = new Div();
        header.getElement().getStyle()
            .set("display", "flex").set("align-items", "center")
            .set("justify-content", "space-between")
            .set("margin-bottom", "16px")
            .set("margin-top", "10px");

        Div headerLeft = new Div();
        headerLeft.getElement().getStyle()
            .set("display", "flex").set("align-items", "center").set("gap", "10px");

        Button btnBack = new Button(VaadinIcon.ARROW_LEFT.create());
        btnBack.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnBack.getElement().getStyle()
            .set("color", "#001934").set("padding", "0").set("cursor", "pointer");
        btnBack.addClickListener(e -> UI.getCurrent().getPage().getHistory().back());

        H2 title = new H2("Status Pesanan Saya");
        title.getElement().getStyle()
            .set("font-size", "20px").set("font-weight", "900")
            .set("color", "#001934").set("margin", "0");
        headerLeft.add(btnBack, title);

        Div headerRight = new Div();
        headerRight.getElement().getStyle()
            .set("display", "flex").set("align-items", "center").set("gap", "12px");

        Button btnBell = new Button(VaadinIcon.BELL.create(), e -> UI.getCurrent().navigate("notifications"));
        btnBell.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnBell.getElement().getStyle().set("color", "#001934").set("padding", "0").set("cursor", "pointer");

        Div avatar = new Div();
        avatar.getElement().getStyle()
            .set("width", "34px").set("height", "34px")
            .set("border-radius", "50%").set("background", "#001934")
            .set("color", "#FFFFFF").set("display", "flex")
            .set("align-items", "center").set("justify-content", "center")
            .set("font-size", "14px").set("font-weight", "800");
        String initial = user.getFullName() != null && !user.getFullName().isBlank()
            ? user.getFullName().substring(0, 1).toUpperCase() : "U";
        avatar.setText(initial);

        headerRight.add(btnBell, avatar);
        header.add(headerLeft, headerRight);
        contentContainer.add(header);

        // ── Filter Tabs (Pills Horizontal Scroll) ─────────────────
        Div filterTabs = new Div();
        filterTabs.getElement().getStyle()
            .set("display", "flex").set("gap", "10px")
            .set("overflow-x", "auto").set("padding-bottom", "14px")
            .set("-webkit-overflow-scrolling", "touch")
            .set("margin-bottom", "20px");

        String[][] tabs = {
            {"SEMUA", "Semua"},
            {"DIPROSES", "Diproses"},
            {"DIKIRIM", "Dikirim"},
            {"SELESAI", "Selesai"},
            {"DIBATALKAN", "Dibatalkan"}
        };

        for (String[] tab : tabs) {
            Button btn = new Button(tab[1]);
            boolean isActive = activeFilter.equals(tab[0]);
            btn.getElement().getStyle()
                .set("background", isActive ? "#001934" : "#EFF4FF")
                .set("color", isActive ? "#FFFFFF" : "#475569")
                .set("border", "none")
                .set("border-radius", "9999px")
                .set("font-weight", "800").set("font-size", "13px")
                .set("padding", "8px 20px").set("cursor", "pointer")
                .set("flex-shrink", "0");
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

            // Bottom Watermark / End Indicator
            Div watermark = new Div();
            watermark.getElement().getStyle()
                .set("text-align", "center")
                .set("margin-top", "32px")
                .set("padding-bottom", "20px");
            watermark.getElement().setProperty("innerHTML",
                "<div style='width:46px;height:46px;border-radius:50%;background:#EFF6FF;margin:0 auto 10px;display:flex;align-items:center;justify-content:center;'>" +
                "<svg width='22' height='22' viewBox='0 0 24 24' fill='none' stroke='#3B82F6' stroke-width='2'><path d='M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z'/><line x1='3' y1='6' x2='21' y2='6'/><path d='M16 10a4 4 0 0 1-8 0'/></svg>" +
                "</div>" +
                "<div style='font-size:13px;font-weight:700;color:#94A3B8;'>Sudah sampai bawah nih!</div>" +
                "<div style='font-size:12px;color:#CBD5E1;'>Gak ada pesanan lama lagi.</div>"
            );
            contentContainer.add(watermark);
        }
    }

    // ══════════════════════════════════════════════════════════
    // ORDER CARD
    // ══════════════════════════════════════════════════════════

    private Div buildOrderCard(Order order, User user) {
        Div card = new Div();
        card.getElement().getStyle()
            .set("background", "#FFFFFF")
            .set("border-radius", "18px")
            .set("border", "1px solid #E2E8F0")
            .set("box-shadow", "0 2px 10px rgba(0, 25, 52, 0.03)")
            .set("margin-bottom", "16px")
            .set("padding", "16px")
            .set("position", "relative");

        List<OrderItem> items = orderService.getOrderItems(order);

        // Check if completed order (collapsed style as in Figma lower card)
        if (order.getStatus() == OrderStatus.SELESAI || order.getStatus() == OrderStatus.DIBATALKAN) {
            // Collapsed Header
            Div header = new Div();
            header.getElement().getStyle()
                .set("display", "flex").set("align-items", "center")
                .set("justify-content", "space-between").set("margin-bottom", "12px");

            Div leftDate = new Div();
            leftDate.getElement().getStyle().set("display", "flex").set("align-items", "center").set("gap", "6px");
            leftDate.getElement().setProperty("innerHTML",
                "<svg width='16' height='16' viewBox='0 0 24 24' fill='none' stroke='#64748B' stroke-width='2'><circle cx='12' cy='12' r='10'/><polyline points='12 6 12 12 16 14'/></svg>" +
                "<span style='font-size:13px;font-weight:700;color:#475569;'>" +
                (order.getCreatedAt() != null ? order.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy")) : "-") +
                "</span>"
            );

            Span statusBadge = buildStatusBadge(order.getStatus());
            header.add(leftDate, statusBadge);
            card.add(header);

            // Collapsed Product Info
            if (!items.isEmpty()) {
                OrderItem firstItem = items.get(0);
                Div pRow = new Div();
                pRow.getElement().getStyle()
                    .set("display", "flex").set("align-items", "center").set("gap", "12px");

                Div thumb = new Div();
                thumb.getElement().getStyle()
                    .set("width", "54px").set("height", "54px").set("border-radius", "10px")
                    .set("background", "#F1F5F9").set("flex-shrink", "0")
                    .set("display", "flex").set("align-items", "center").set("justify-content", "center");
                if (firstItem.getProduct() != null && firstItem.getProduct().getImages() != null && !firstItem.getProduct().getImages().isBlank()) {
                    thumb.getElement().getStyle()
                        .set("background-image", "url('" + firstItem.getProduct().getImages() + "')")
                        .set("background-size", "cover").set("background-position", "center");
                } else {
                    thumb.getElement().setProperty("innerHTML", "<svg width='24' height='24' viewBox='0 0 24 24' fill='none' stroke='#94A3B8' stroke-width='2'><path d='M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z'/></svg>");
                }

                Div pInfo = new Div();
                pInfo.getElement().getStyle().set("flex", "1");
                Span pTitle = new Span(firstItem.getProductNameSnapshot());
                pTitle.getElement().getStyle().set("font-size", "14px").set("font-weight", "800").set("color", "#001934").set("display", "block");
                Span pMeta = new Span("Qty: " + firstItem.getQuantity() + " • Rp " + String.format("%,.0f", order.getTotalAmount()));
                pMeta.getElement().getStyle().set("font-size", "12px").set("color", "#64748B").set("margin-top", "2px").set("display", "block");
                pInfo.add(pTitle, pMeta);

                Button btnDetailLink = new Button("Lihat Detail ›");
                btnDetailLink.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
                btnDetailLink.getElement().getStyle()
                    .set("color", "#001934").set("font-size", "13px").set("font-weight", "800").set("padding", "0").set("cursor", "pointer");
                btnDetailLink.addClickListener(e -> openOrderDetailModal(order));

                pRow.add(thumb, pInfo, btnDetailLink);
                card.add(pRow);
            }
            return card;
        }

        // ── Active Detailed Order Card (Matches Figma Top Card) ───────────────
        Div header = new Div();
        header.getElement().getStyle()
            .set("display", "flex").set("align-items", "center")
            .set("justify-content", "space-between").set("margin-bottom", "14px");

        Div headerLeft = new Div();
        headerLeft.getElement().getStyle()
            .set("display", "flex").set("align-items", "center").set("gap", "10px");

        Div truckBox = new Div();
        truckBox.getElement().setProperty("innerHTML",
            "<div style='width:34px;height:34px;border-radius:50%;background:#DBEAFE;display:flex;align-items:center;justify-content:center;'>" +
            "<svg width='18' height='18' viewBox='0 0 24 24' fill='none' stroke='#2563EB' stroke-width='2'><path d='M1 3h15v13H1z'/><path d='M16 8h4l3 3v5h-7V8z'/><circle cx='5.5' cy='18.5' r='2.5'/><circle cx='18.5' cy='18.5' r='2.5'/></svg>" +
            "</div>"
        );

        Div orderMeta = new Div();
        Span orderNum = new Span("TRX-" + order.getOrderNumber());
        orderNum.getElement().getStyle()
            .set("font-size", "14px").set("font-weight", "800")
            .set("color", "#001934").set("display", "block");
        String dateStr = order.getCreatedAt() != null
            ? order.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy • HH:mm")) : "12 Mei 2024 • 14:20";
        Span orderDate = new Span(dateStr);
        orderDate.getElement().getStyle().set("font-size", "12px").set("color", "#64748B");
        orderMeta.add(orderNum, orderDate);
        headerLeft.add(truckBox, orderMeta);

        Span statusBadge = buildStatusBadge(order.getStatus());
        header.add(headerLeft, statusBadge);
        card.add(header);

        // Product Summary Box
        if (!items.isEmpty()) {
            OrderItem item = items.get(0);
            Div pBox = new Div();
            pBox.getElement().getStyle()
                .set("display", "flex").set("align-items", "center").set("gap", "12px")
                .set("margin-bottom", "12px");

            Div thumb = new Div();
            thumb.getElement().getStyle()
                .set("width", "56px").set("height", "56px").set("border-radius", "10px")
                .set("background", "#F1F5F9").set("flex-shrink", "0")
                .set("display", "flex").set("align-items", "center").set("justify-content", "center");
            if (item.getProduct() != null && item.getProduct().getImages() != null && !item.getProduct().getImages().isBlank()) {
                thumb.getElement().getStyle()
                    .set("background-image", "url('" + item.getProduct().getImages() + "')")
                    .set("background-size", "cover").set("background-position", "center");
            } else {
                thumb.getElement().setProperty("innerHTML", "<svg width='24' height='24' viewBox='0 0 24 24' fill='none' stroke='#94A3B8' stroke-width='2'><path d='M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z'/></svg>");
            }

            Div pDetails = new Div();
            pDetails.getElement().getStyle().set("flex", "1");
            Span pName = new Span(item.getProductNameSnapshot());
            pName.getElement().getStyle()
                .set("font-size", "14px").set("font-weight", "800")
                .set("color", "#001934").set("display", "block");
            Span pSpecs = new Span("Ukuran L • Kondisi Bekas");
            pSpecs.getElement().getStyle().set("font-size", "12px").set("color", "#64748B").set("display", "block").set("margin-top", "2px");
            Span pPrice = new Span("Rp " + String.format("%,.0f", order.getTotalAmount()));
            pPrice.getElement().getStyle().set("font-size", "13px").set("font-weight", "800").set("color", "#001934").set("display", "block").set("margin-top", "2px");

            pDetails.add(pName, pSpecs, pPrice);
            pBox.add(thumb, pDetails);
            card.add(pBox);
        }

        // ReWear Escrow Security Banner
        Div escrowBanner = new Div();
        escrowBanner.getElement().getStyle()
            .set("background", order.getStatus() == OrderStatus.KOMPLAIN ? "#FEF2F2" : "#EFF6FF")
            .set("border-radius", "10px")
            .set("padding", "8px 12px").set("margin-bottom", "16px")
            .set("display", "flex").set("align-items", "center").set("gap", "8px")
            .set("font-size", "12px")
            .set("color", order.getStatus() == OrderStatus.KOMPLAIN ? "#991B1B" : "#1E3A8A")
            .set("font-weight", "600")
            .set("border", order.getStatus() == OrderStatus.KOMPLAIN ? "1px solid #FECACA" : "none");

        if (order.getStatus() == OrderStatus.KOMPLAIN) {
            escrowBanner.getElement().setProperty("innerHTML",
                "<svg width='16' height='16' viewBox='0 0 24 24' fill='none' stroke='#DC2626' stroke-width='2'><path d='M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z'/><line x1='12' y1='9' x2='12' y2='13'/><line x1='12' y1='17' x2='12.01' y2='17'/></svg>" +
                "<span><strong>Dana Escrow Ditahan:</strong> Komplain pesanan sedang ditinjau oleh Admin ReWear.</span>"
            );
        } else {
            escrowBanner.getElement().setProperty("innerHTML",
                "<svg width='16' height='16' viewBox='0 0 24 24' fill='none' stroke='#B45309' stroke-width='2'><path d='M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z'/></svg>" +
                "<span>Dana aman di <strong style='color:#B45309;'>ReWear Escrow</strong></span>"
            );
        }
        card.add(escrowBanner);

        // Vertical Timeline Tracker
        card.add(buildTimelineTracker(order));

        // Action Buttons Row (Figma Bottom Bar)
        Div actionContainer = new Div();
        actionContainer.getElement().getStyle()
            .set("background", "#EFF6FF")
            .set("margin", "16px -16px -16px -16px")
            .set("padding", "12px 16px")
            .set("border-radius", "0 0 18px 18px")
            .set("display", "flex").set("gap", "10px");

        if (order.getStatus() == OrderStatus.KOMPLAIN) {
            Optional<OrderReturn> retOpt = orderService.getReturnByOrder(order);
            Button btnDetailKomplain = new Button("Detail Komplain & Sengketa", VaadinIcon.INFO_CIRCLE.create());
            btnDetailKomplain.getElement().getStyle()
                .set("background", "#FEF2F2").set("color", "#991B1B")
                .set("border", "1.5px solid #F87171").set("border-radius", "10px")
                .set("font-weight", "800").set("font-size", "13px")
                .set("padding", "10px").set("flex", "1").set("cursor", "pointer");
            btnDetailKomplain.addClickListener(e -> openComplainDetailModal(order, retOpt.orElse(null)));

            Button btnDetailTrx = new Button("Rincian Pesanan", VaadinIcon.FILE_TEXT_O.create());
            btnDetailTrx.getElement().getStyle()
                .set("background", "#001934").set("color", "#FFFFFF")
                .set("border", "none").set("border-radius", "10px")
                .set("font-weight", "800").set("font-size", "13px")
                .set("padding", "10px").set("flex", "1").set("cursor", "pointer");
            btnDetailTrx.addClickListener(e -> openOrderDetailModal(order));

            actionContainer.add(btnDetailKomplain, btnDetailTrx);
        } else {
            Button btnKomplain = new Button("Ajukan Komplain");
            btnKomplain.getElement().getStyle()
                .set("background", "#FFFFFF").set("color", "#991B1B")
                .set("border", "1.5px solid #FCA5A5").set("border-radius", "10px")
                .set("font-weight", "800").set("font-size", "13px")
                .set("padding", "10px").set("flex", "1").set("cursor", "pointer");
            btnKomplain.addClickListener(e -> openComplainModal(order, user));

            Button btnLacak = new Button(order.getStatus() == OrderStatus.DIKIRIM ? "Lacak Paket" : "Konfirmasi Diterima");
            btnLacak.getElement().getStyle()
                .set("background", "#001934").set("color", "#FFFFFF")
                .set("border", "none").set("border-radius", "10px")
                .set("font-weight", "800").set("font-size", "13px")
                .set("padding", "10px").set("flex", "1").set("cursor", "pointer");
            btnLacak.addClickListener(e -> {
                if (order.getStatus() == OrderStatus.DIKIRIM) {
                    openOrderDetailModal(order);
                } else {
                    orderService.updateOrderStatus(order, OrderStatus.SELESAI, "Dikonfirmasi diterima pembeli.", user);
                    Notification notif = Notification.show("Pesanan Selesai! Terima kasih telah bertransaksi.", 3000, Notification.Position.TOP_CENTER);
                    notif.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    buildView();
                }
            });

            actionContainer.add(btnKomplain, btnLacak);
        }
        card.add(actionContainer);

        return card;
    }

    private Div buildTimelineTracker(Order order) {
        Div tracker = new Div();
        tracker.getElement().getStyle()
            .set("position", "relative")
            .set("padding-left", "28px")
            .set("margin", "12px 0 16px 4px");

        OrderStatus s = order.getStatus();

        // Connecting vertical line
        Div line = new Div();
        line.getElement().getStyle()
            .set("position", "absolute").set("top", "12px").set("bottom", "20px").set("left", "10px")
            .set("width", "2px").set("background", "#CBD5E1");
        tracker.add(line);

        // Step 1: Pesanan Dibuat
        tracker.add(buildTimelineNode(true, false, "check", "Pesanan Dibuat", "Menunggu pembayaran dikonfirmasi", "12 Mei, 14:22"));

        // Step 2: Pembayaran Berhasil
        boolean step2Done = s != OrderStatus.MENUNGGU_PEMBAYARAN && s != OrderStatus.DIBATALKAN;
        tracker.add(buildTimelineNode(step2Done, false, "check", "Pembayaran Berhasil", "Saldo ditahan oleh sistem escrow", step2Done ? "12 Mei, 14:30" : "-"));

        // Step 3: Sedang Dikirim
        boolean step3Active = s == OrderStatus.DIKIRIM || s == OrderStatus.DIPROSES;
        boolean step3Done = s == OrderStatus.DITERIMA || s == OrderStatus.SELESAI;
        tracker.add(buildTimelineNode(step3Done, step3Active, "truck", "Sedang Dikirim", "Kurir sedang menuju lokasi drop-off sekolah", step3Active ? "Hari ini, 09:15" : "-"));

        // Step 4: Pesanan Selesai
        boolean step4Done = s == OrderStatus.SELESAI;
        tracker.add(buildTimelineNode(step4Done, false, "circle", "Pesanan Selesai", "Konfirmasi penerimaan oleh pembeli", step4Done ? "Hari ini" : "-"));

        return tracker;
    }

    private Div buildTimelineNode(boolean done, boolean active, String type, String title, String subtitle, String time) {
        Div node = new Div();
        node.getElement().getStyle()
            .set("position", "relative")
            .set("margin-bottom", "16px");

        // Circle indicator
        Div circle = new Div();
        circle.getElement().getStyle()
            .set("position", "absolute").set("left", "-28px").set("top", "2px")
            .set("width", "22px").set("height", "22px").set("border-radius", "50%")
            .set("display", "flex").set("align-items", "center").set("justify-content", "center")
            .set("z-index", "2");

        if (active) {
            circle.getElement().getStyle().set("background", "#F59E0B").set("color", "#FFFFFF");
            circle.getElement().setProperty("innerHTML", "<svg width='12' height='12' viewBox='0 0 24 24' fill='none' stroke='#FFF' stroke-width='2.5'><rect x='1' y='3' width='15' height='13'/><polygon points='16 8 20 8 23 11 23 16 16 16 16 8'/></svg>");
        } else if (done) {
            circle.getElement().getStyle().set("background", "#001934").set("color", "#FFFFFF");
            circle.getElement().setProperty("innerHTML", "<svg width='12' height='12' viewBox='0 0 24 24' fill='none' stroke='#FFF' stroke-width='3'><polyline points='20 6 9 17 4 12'/></svg>");
        } else {
            circle.getElement().getStyle().set("background", "#F1F5F9").set("border", "2px solid #CBD5E1");
        }

        Div content = new Div();
        Span tSpan = new Span(title);
        tSpan.getElement().getStyle()
            .set("font-size", "13px").set("font-weight", active || done ? "800" : "600")
            .set("color", active ? "#001934" : (done ? "#001934" : "#94A3B8"))
            .set("display", "block");

        Span subSpan = new Span(subtitle);
        subSpan.getElement().getStyle()
            .set("font-size", "11px").set("color", active ? "#475569" : "#94A3B8")
            .set("display", "block").set("margin-top", "1px");

        content.add(tSpan, subSpan);

        if (!time.equals("-")) {
            Span timeSpan = new Span(time);
            timeSpan.getElement().getStyle()
                .set("font-size", "11px").set("font-weight", active ? "800" : "600")
                .set("color", active ? "#B45309" : "#94A3B8")
                .set("display", "block").set("margin-top", "2px");
            content.add(timeSpan);
        }

        node.add(circle, content);
        return node;
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
            "display:flex;align-items:center;justify-content:center;'>" +
            "<svg width='22' height='22' viewBox='0 0 24 24' fill='#FFFFFF'><path d='M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z'/></svg>" +
            "</div>");
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
                Span chip = new Span(it.getProductNameSnapshot());
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
                .set("font-size", "32px")
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

    // ══════════════════════════════════════════════════════════
    // COMPLAIN / DISPUTE MODAL (ORDER RETURN)
    // ══════════════════════════════════════════════════════════

    private void openComplainModal(Order order, User buyer) {
        Dialog d = new Dialog();
        d.setWidth("520px");
        d.setCloseOnOutsideClick(false);

        // Header
        Div header = new Div();
        header.getElement().getStyle()
            .set("display", "flex").set("align-items", "center").set("gap", "12px")
            .set("padding", "24px 24px 16px")
            .set("border-bottom", "1px solid #E8EEF8");

        Div iconBox = new Div();
        iconBox.getElement().setProperty("innerHTML",
            "<div style='width:40px;height:40px;border-radius:12px;" +
            "background:linear-gradient(135deg,#DC2626,#991B1B);" +
            "display:flex;align-items:center;justify-content:center;'>" +
            "<svg width='22' height='22' viewBox='0 0 24 24' fill='none' stroke='#FFFFFF' stroke-width='2'><path d='M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z'/><line x1='12' y1='9' x2='12' y2='13'/><line x1='12' y1='17' x2='12.01' y2='17'/></svg>" +
            "</div>");

        Div titleBlock = new Div();
        Span modalTitle = new Span("Ajukan Komplain / Retur");
        modalTitle.getElement().getStyle()
            .set("font-size", "17px").set("font-weight", "900")
            .set("color", "#001934").set("display", "block");
        Span sub = new Span("Pesanan #" + order.getOrderNumber());
        sub.getElement().getStyle().set("font-size", "12px").set("color", "#94A3B8").set("font-family", "monospace");
        titleBlock.add(modalTitle, sub);
        header.add(iconBox, titleBlock);

        // Body
        Div body = new Div();
        body.getElement().getStyle()
            .set("padding", "20px 24px").set("display", "flex")
            .set("flex-direction", "column").set("gap", "16px");

        // Escrow Alert
        Div alertBox = new Div();
        alertBox.getElement().getStyle()
            .set("background", "#FEF2F2").set("border", "1px solid #FECACA")
            .set("border-radius", "12px").set("padding", "12px 14px")
            .set("font-size", "13px").set("color", "#991B1B").set("line-height", "1.5");
        alertBox.setText("Pengajuan ini akan menahan dana ReWear Escrow sebesar Rp " +
            String.format("%,.0f", order.getTotalAmount()) +
            ". Penjual tidak dapat mencairkan dana sampai Admin menyelesaikan sengketa.");
        body.add(alertBox);

        // Reason Selector
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
        reasonBox.setRequired(true);
        body.add(reasonBox);

        // Problem Description
        TextArea descArea = new TextArea("Rincian Kendala yang Dialami");
        descArea.setPlaceholder("Jelaskan kondisi barang secara lengkap dan alasan mengapa barang ingin diretur...");
        descArea.setWidthFull();
        descArea.setRequired(true);
        descArea.getElement().getStyle().set("min-height", "90px");
        body.add(descArea);

        // Photo Evidence URL
        TextField evidenceField = new TextField("Link / URL Foto Bukti (Opsional)");
        evidenceField.setPlaceholder("https://... (Foto kondisi cacat/resi paket)");
        evidenceField.setWidthFull();
        evidenceField.setHelperText("Unggah foto ke cloud/drive atau masukkan URL gambar bukti fisik.");
        body.add(evidenceField);

        // Refund Amount Readonly
        TextField refundDisplay = new TextField("Nominal Pengembalian Dana (Refund)");
        refundDisplay.setValue("Rp " + String.format("%,.0f", order.getTotalAmount()));
        refundDisplay.setReadOnly(true);
        refundDisplay.setWidthFull();
        body.add(refundDisplay);

        // Footer
        Div footer = new Div();
        footer.getElement().getStyle()
            .set("padding", "12px 24px 20px")
            .set("display", "flex").set("justify-content", "flex-end")
            .set("gap", "10px").set("border-top", "1px solid #E8EEF8");

        Button btnCancel = new Button("Batal", e -> d.close());
        btnCancel.getElement().getStyle()
            .set("background", "transparent").set("color", "#64748B")
            .set("border", "1.5px solid #E2E8F0").set("border-radius", "10px")
            .set("font-weight", "700").set("padding", "10px 20px").set("cursor", "pointer");

        Button btnSubmit = new Button("Kirim Pengajuan Komplain");
        btnSubmit.getElement().getStyle()
            .set("background", "#DC2626").set("color", "#FFFFFF").set("border", "none")
            .set("border-radius", "10px").set("font-weight", "800")
            .set("padding", "10px 24px").set("cursor", "pointer");

        btnSubmit.addClickListener(e -> {
            String selectedReason = reasonBox.getValue();
            String desc = descArea.getValue();
            String evidence = evidenceField.getValue();

            if (selectedReason == null || selectedReason.isBlank()) {
                Notification.show("Silakan pilih kategori kendala.", 3000, Notification.Position.TOP_CENTER);
                return;
            }
            if (desc == null || desc.isBlank()) {
                Notification.show("Silakan tuliskan rincian kendala yang dialami.", 3000, Notification.Position.TOP_CENTER);
                return;
            }

            try {
                String fullReason = selectedReason + ": " + desc.trim();
                orderService.createOrderReturn(order, buyer, fullReason, evidence, order.getTotalAmount());

                Notification notif = Notification.show("Komplain berhasil diajukan! Dana Escrow telah ditahan untuk peninjauan Admin.", 4000, Notification.Position.TOP_CENTER);
                notif.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                d.close();
                buildView();
            } catch (Exception ex) {
                Notification.show("Gagal mengajukan komplain: " + ex.getMessage(), 3500, Notification.Position.TOP_CENTER);
            }
        });

        footer.add(btnCancel, btnSubmit);
        d.getElement().getStyle().set("border-radius", "24px").set("overflow", "hidden").set("padding", "0");
        d.add(header, body, footer);
        d.open();
    }

    private void openComplainDetailModal(Order order, OrderReturn ret) {
        Dialog d = new Dialog();
        d.setWidth("500px");

        Div header = new Div();
        header.getElement().getStyle()
            .set("display", "flex").set("align-items", "center").set("gap", "12px")
            .set("padding", "24px 24px 16px")
            .set("border-bottom", "1px solid #E8EEF8");

        Div iconBox = new Div();
        iconBox.getElement().setProperty("innerHTML",
            "<div style='width:40px;height:40px;border-radius:12px;background:#FEE2E2;display:flex;align-items:center;justify-content:center;'>" +
            "<svg width='22' height='22' viewBox='0 0 24 24' fill='none' stroke='#DC2626' stroke-width='2'><path d='M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z'/><line x1='12' y1='9' x2='12' y2='13'/><line x1='12' y1='17' x2='12.01' y2='17'/></svg>" +
            "</div>");

        Div titleBlock = new Div();
        Span modalTitle = new Span("Status Komplain & Retur");
        modalTitle.getElement().getStyle().set("font-size", "17px").set("font-weight", "900").set("color", "#001934").set("display", "block");
        Span sub = new Span("Pesanan #" + order.getOrderNumber());
        sub.getElement().getStyle().set("font-size", "12px").set("color", "#94A3B8").set("font-family", "monospace");
        titleBlock.add(modalTitle, sub);
        header.add(iconBox, titleBlock);

        Div body = new Div();
        body.getElement().getStyle().set("padding", "20px 24px").set("display", "flex").set("flex-direction", "column").set("gap", "14px");

        String statusStr = ret != null ? ret.getStatus().name() : "PENDING";
        body.add(buildDetailRow(VaadinIcon.SHIELD, "Status Peninjauan", statusStr.equals("PENDING") ? "Menunggu Keputusan Admin" : statusStr));
        body.add(buildDetailRow(VaadinIcon.EXCLAMATION_CIRCLE, "Alasan Komplain", ret != null ? ret.getReason() : "Dalam proses peninjauan"));
        if (ret != null && ret.getEvidenceUrl() != null && !ret.getEvidenceUrl().isBlank()) {
            body.add(buildDetailRow(VaadinIcon.PICTURE, "Bukti Foto", ret.getEvidenceUrl()));
        }
        body.add(buildDetailRow(VaadinIcon.MONEY, "Nominal Tertahan di Escrow", "Rp " + String.format("%,.0f", order.getTotalAmount())));

        Div footer = new Div();
        footer.getElement().getStyle().set("padding", "12px 24px 20px").set("display", "flex").set("justify-content", "flex-end").set("border-top", "1px solid #E8EEF8");
        Button btnClose = new Button("Tutup", e -> d.close());
        btnClose.getElement().getStyle().set("background", "#001934").set("color", "#FFFFFF").set("border", "none").set("border-radius", "10px").set("font-weight", "700").set("padding", "10px 24px").set("cursor", "pointer");
        footer.add(btnClose);

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

        body.add(buildDetailRow(VaadinIcon.MAP_MARKER, "Alamat Pengiriman",
            order.getShippingAddress() != null ? order.getShippingAddress() : "-"));
        body.add(buildDetailRow(VaadinIcon.CREDIT_CARD, "Metode Pembayaran",
            order.getPaymentMethod() != null ? order.getPaymentMethod() : "-"));
        body.add(buildDetailRow(VaadinIcon.TRUCK, "Metode Pengiriman",
            order.getShippingMethod() != null ? order.getShippingMethod().name() : "-"));
        body.add(buildDetailRow(VaadinIcon.MONEY, "Total Pembayaran",
            "Rp " + String.format("%,.0f", order.getTotalAmount())));
        body.add(buildDetailRow(VaadinIcon.BAR_CHART, "Status",
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

    private Div buildDetailRow(VaadinIcon icon, String label, String value) {
        Div row = new Div();
        row.getElement().getStyle()
            .set("display", "flex").set("align-items", "flex-start")
            .set("gap", "12px").set("padding", "12px 16px")
            .set("background", "#F8FAFF").set("border-radius", "12px")
            .set("border", "1px solid #EEF2FF");

        Icon ic = icon.create();
        ic.getElement().getStyle().set("width", "18px").set("height", "18px").set("color", "#001934").set("flex-shrink", "0").set("margin-top", "2px");

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
