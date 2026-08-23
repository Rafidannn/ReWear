package com.example.application.views.user;

import com.example.application.model.order.Order;
import com.example.application.model.order.OrderStatus;
import com.example.application.model.user.User;
import com.example.application.service.order.OrderService;
import com.example.application.util.AuthGuard;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinSession;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Route(value = "notifications", layout = MainLayout.class)
@PageTitle("Notifikasi | ReWear SMKN 24")
public class NotificationView extends Div implements BeforeEnterObserver {

    private static final String SESSION_READ_KEY = "rewear_read_notifs";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    private final OrderService orderService;
    private final Div contentWrap = new Div();
    private String activeFilter = "semua";

    public NotificationView(OrderService orderService) {
        this.orderService = orderService;
        setWidthFull();
        getElement().getStyle()
            .set("background", "#F1F5FF")
            .set("min-height", "100vh")
            .set("padding", "40px 0 80px 0");
        add(contentWrap);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!AuthGuard.requireLogin(UI.getCurrent())) return;
        buildView();
    }

    // ─── Session-based read tracking ───────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private Set<String> getReadSet() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session == null) return new HashSet<>();
        Set<String> set = (Set<String>) session.getAttribute(SESSION_READ_KEY);
        if (set == null) {
            set = new HashSet<>();
            session.setAttribute(SESSION_READ_KEY, set);
        }
        return set;
    }

    private void markRead(String key) {
        getReadSet().add(key);
    }

    private void markAllRead(List<NotifItem> items) {
        Set<String> readSet = getReadSet();
        for (NotifItem n : items) readSet.add(n.key);
    }

    private boolean isRead(String key) {
        return getReadSet().contains(key);
    }

    // ─── View Builder ───────────────────────────────────────────────────────────

    private void buildView() {
        contentWrap.removeAll();
        contentWrap.getElement().getStyle()
            .set("max-width", "600px")
            .set("margin", "0 auto")
            .set("padding", "0 16px 100px 16px");

        User user = AuthGuard.getCurrentUser();
        if (user == null) return;

        List<NotifItem> allNotifs = buildNotifications(user);
        long unreadCount = allNotifs.stream().filter(n -> !isRead(n.key)).count();

        // ─── Page Header ───
        Div header = new Div();
        header.getElement().getStyle()
            .set("display", "flex").set("align-items", "center")
            .set("justify-content", "space-between").set("margin-bottom", "20px")
            .set("margin-top", "10px");

        H2 title = new H2("Aktivitas");
        title.getElement().getStyle()
            .set("font-size", "22px").set("font-weight", "900")
            .set("color", "#001934").set("margin", "0");

        Button btnMarkAll = new Button("Tandai Semua Dibaca", VaadinIcon.CHECK.create());
        btnMarkAll.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnMarkAll.getElement().getStyle()
            .set("color", "#334155").set("font-weight", "700")
            .set("font-size", "13px").set("cursor", "pointer")
            .set("padding", "0");
        btnMarkAll.addClickListener(e -> {
            markAllRead(allNotifs);
            Notification.show("Semua notifikasi ditandai sudah dibaca.", 2000, Notification.Position.TOP_CENTER);
            buildView();
        });

        header.add(title, btnMarkAll);
        contentWrap.add(header);

        // ─── Filter Tabs (Pills) ───
        Div filterRow = new Div();
        filterRow.getElement().getStyle()
            .set("display", "flex").set("gap", "10px")
            .set("overflow-x", "auto").set("padding-bottom", "14px")
            .set("-webkit-overflow-scrolling", "touch")
            .set("margin-bottom", "16px");

        String[][] filters = {
            {"semua", "Semua"},
            {"transaksi", "Transaksi"},
            {"promo", "Promo"},
            {"sistem", "Siswa / Sistem"}
        };

        for (String[] f : filters) {
            Button btn = new Button(f[1]);
            boolean isActive = activeFilter.equals(f[0]);
            btn.getElement().getStyle()
                .set("background", isActive ? "#FFDEA2" : "#EFF4FF")
                .set("color", isActive ? "#001934" : "#475569")
                .set("border", "none")
                .set("border-radius", "9999px")
                .set("font-weight", "800")
                .set("font-size", "13px")
                .set("padding", "8px 20px")
                .set("cursor", "pointer")
                .set("flex-shrink", "0");
            final String key = f[0];
            btn.addClickListener(e -> { activeFilter = key; buildView(); });
            filterRow.add(btn);
        }
        contentWrap.add(filterRow);

        // ─── Render filtered list ───
        List<NotifItem> filtered = allNotifs.stream()
            .filter(n -> activeFilter.equals("semua") || n.category.equalsIgnoreCase(activeFilter))
            .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            contentWrap.add(buildEmptyState());
            return;
        }

        // Group by dateLabel
        String prevDate = null;
        for (NotifItem notif : filtered) {
            if (!notif.dateLabel.equalsIgnoreCase(prevDate)) {
                Span dateHeader = new Span(notif.dateLabel);
                dateHeader.getElement().getStyle()
                    .set("font-size", "11px").set("font-weight", "800")
                    .set("color", "#94A3B8").set("text-transform", "uppercase")
                    .set("letter-spacing", "0.8px").set("display", "block")
                    .set("margin", "20px 0 10px 4px");
                contentWrap.add(dateHeader);
                prevDate = notif.dateLabel;
            }
            contentWrap.add(buildNotifCard(notif));
        }

        // ─── Bottom Watermark / Illustration ───
        Div watermark = new Div();
        watermark.getElement().getStyle()
            .set("text-align", "center")
            .set("margin-top", "40px")
            .set("opacity", "0.4");
        watermark.getElement().setProperty("innerHTML",
            "<svg width='48' height='48' viewBox='0 0 24 24' fill='none' stroke='#94A3B8' stroke-width='1.5' style='margin-bottom:6px;'>" +
            "<path d='M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9'></path>" +
            "<path d='M13.73 21a2 2 0 0 1-3.46 0'></path></svg>" +
            "<div style='font-size:12px;font-weight:700;color:#94A3B8;'>Notifikasi ReWear</div>"
        );
        contentWrap.add(watermark);
    }

    // ─── Build data ───

    private List<NotifItem> buildNotifications(User user) {
        List<NotifItem> list = new ArrayList<>();

        // Promo / Offer item
        list.add(new NotifItem(
            "promo-warga24",
            "<svg width='22' height='22' viewBox='0 0 24 24' fill='none' stroke='#D97706' stroke-width='2'><path d='M2 9a3 3 0 0 1 0 6v2a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-2a3 3 0 0 1 0-6V7a2 2 0 0 0-2-2H4a2 2 0 0 0-2 2v2z'/><line x1='12' y1='9' x2='12' y2='15'/></svg>",
            "Potongan Harga 20% Menanti",
            "Gunakan kode \"WARGA24\" untuk pembelian seragam bekas layak...",
            "10:45", "HARI INI", "promo", "", "#FEF3C7"
        ));

        // Transaksi item
        list.add(new NotifItem(
            "trans-dikirim",
            "<svg width='22' height='22' viewBox='0 0 24 24' fill='none' stroke='#2563EB' stroke-width='2'><path d='M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z'/><polyline points='3.27 6.96 12 12.01 20.73 6.96'/><line x1='12' y1='22.08' x2='12' y2='12'/></svg>",
            "Pesanan Dikirim",
            "Paket berisi \"Jaket Almamater SMKN 24\" sedang menuju lokasimu via...",
            "08:20", "HARI INI", "transaksi", "orders", "#DBEAFE"
        ));

        // Verifikasi item
        list.add(new NotifItem(
            "sys-verified",
            "<svg width='22' height='22' viewBox='0 0 24 24' fill='none' stroke='#2563EB' stroke-width='2'><path d='M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z'/><polyline points='9 12 11 14 15 10'/></svg>",
            "Verifikasi Warga Berhasil",
            "Akunmu telah diverifikasi sebagai siswa aktif SMKN 24 Jakarta....",
            "Kemarin", "KEMARIN", "sistem", "", "#DBEAFE"
        ));

        // Engagement item
        list.add(new NotifItem(
            "wish-liked",
            "<svg width='22' height='22' viewBox='0 0 24 24' fill='none' stroke='#2563EB' stroke-width='2'><path d='M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z'/></svg>",
            "Barangmu Disukai!",
            "Seorang calon pembeli menyukai \"Kemeja Praktik Boga\" yang kamu...",
            "Kemarin", "KEMARIN", "sistem", "", "#DBEAFE"
        ));

        try {
            List<Order> buyerOrders = orderService.getBuyerOrders(user);
            for (Order o : buyerOrders) {
                String key = "buyer-order-" + o.getId();
                String time = o.getCreatedAt() != null ? o.getCreatedAt().format(FMT) : "08:20";
                list.add(new NotifItem(key, statusIcon(o.getStatus()), statusTitle(o.getStatus()),
                    "Pesanan #" + o.getOrderNumber() + " — " + statusDesc(o.getStatus()),
                    time, "KEMARIN", "transaksi", "orders", "#DBEAFE"));
            }
        } catch (Exception ignored) {}

        return list;
    }

    // ─── Card builder ───────────────────────────────────────────────────────────

    private Div buildNotifCard(NotifItem n) {
        boolean read = isRead(n.key);

        Div card = new Div();
        card.getElement().getStyle()
            .set("background", read ? "#F8FAFC" : "#FFFFFF")
            .set("border", "1px solid #E2E8F0")
            .set("border-radius", "16px").set("padding", "14px 16px")
            .set("margin-bottom", "12px").set("display", "flex")
            .set("align-items", "flex-start").set("gap", "14px")
            .set("cursor", "pointer").set("transition", "all 0.15s ease")
            .set("position", "relative")
            .set("box-shadow", "0 2px 6px rgba(0, 25, 52, 0.02)");

        // SVG icon container (Circular)
        Div iconBox = new Div();
        iconBox.getElement().setProperty("innerHTML",
            "<div style='width:42px;height:42px;border-radius:50%;" +
            "background:" + (n.bgColor != null ? n.bgColor : "#DBEAFE") + ";" +
            "display:flex;align-items:center;justify-content:center;" +
            "flex-shrink:0;'>" + n.icon + "</div>");

        // Text & Time Block
        Div textBlock = new Div();
        textBlock.getElement().getStyle().set("flex", "1");

        Div topRow = new Div();
        topRow.getElement().getStyle()
            .set("display", "flex").set("align-items", "center")
            .set("justify-content", "space-between").set("margin-bottom", "4px");

        Span titleSpan = new Span(n.title);
        titleSpan.getElement().getStyle()
            .set("font-weight", "800").set("font-size", "14px")
            .set("color", "#001934");

        Div rightMeta = new Div();
        rightMeta.getElement().getStyle().set("display", "flex").set("align-items", "center").set("gap", "8px");

        Span timeSpan = new Span(n.time);
        timeSpan.getElement().getStyle()
            .set("font-size", "11px").set("color", "#94A3B8").set("font-weight", "600");

        rightMeta.add(timeSpan);

        // Unread gold/brown dot
        if (!read) {
            Span dot = new Span();
            dot.getElement().getStyle()
                .set("width", "7px").set("height", "7px")
                .set("background", "#B45309").set("border-radius", "50%")
                .set("display", "inline-block");
            rightMeta.add(dot);
        }

        topRow.add(titleSpan, rightMeta);

        Span bodySpan = new Span(n.body);
        bodySpan.getElement().getStyle()
            .set("font-size", "12px").set("color", "#64748B")
            .set("display", "block").set("line-height", "1.45");

        textBlock.add(topRow, bodySpan);
        card.add(iconBox, textBlock);

        // Click: mark read & navigate or refresh
        card.addClickListener(e -> {
            markRead(n.key);
            if (n.route != null && !n.route.isEmpty()) {
                UI.getCurrent().navigate(n.route);
            } else {
                buildView();
            }
        });

        return card;
    }

    // ─── Empty state ────────────────────────────────────────────────────────────

    private Div buildEmptyState() {
        Div empty = new Div();
        empty.getElement().getStyle()
            .set("text-align", "center").set("padding", "60px 20px")
            .set("background", "#FFFFFF").set("border-radius", "16px")
            .set("border", "1px solid #E2E8F0");
        empty.getElement().setProperty("innerHTML",
            "<div style='width:48px;height:48px;margin:0 auto 12px;border-radius:50%;background:#F1F5F9;display:flex;align-items:center;justify-content:center;'>" +
            "<svg width='24' height='24' viewBox='0 0 24 24' fill='none' stroke='#64748B' stroke-width='2'><path d='M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9'></path><path d='M13.73 21a2 2 0 0 1-3.46 0'></path></svg>" +
            "</div>" +
            "<h3 style='font-size:16px;font-weight:800;color:#001934;margin:0 0 6px 0;'>Tidak Ada Aktivitas</h3>" +
            "<p style='font-size:13px;color:#64748B;margin:0;'>Aktivitas transaksi dan notifikasimu akan muncul di sini.</p>"
        );
        return empty;
    }

    // ─── Helpers ────────────────────────────────────────────────────────────────

    private String statusIcon(OrderStatus s) {
        return switch (s) {
            case MENUNGGU_PEMBAYARAN -> "<svg width='20' height='20' viewBox='0 0 24 24' fill='none' stroke='#D97706' stroke-width='2'><circle cx='12' cy='12' r='10'/><polyline points='12 6 12 12 16 14'/></svg>";
            case DIBAYAR -> "<svg width='20' height='20' viewBox='0 0 24 24' fill='none' stroke='#2563EB' stroke-width='2'><path d='M22 11.08V12a10 10 0 1 1-5.93-9.14'/><polyline points='22 4 12 14.01 9 11.01'/></svg>";
            case DIPROSES -> "<svg width='20' height='20' viewBox='0 0 24 24' fill='none' stroke='#2563EB' stroke-width='2'><circle cx='12' cy='12' r='3'/><path d='M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z'/></svg>";
            case DIKIRIM -> "<svg width='20' height='20' viewBox='0 0 24 24' fill='none' stroke='#2563EB' stroke-width='2'><path d='M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z'/><polyline points='3.27 6.96 12 12.01 20.73 6.96'/><line x1='12' y1='22.08' x2='12' y2='12'/></svg>";
            case DITERIMA, SELESAI -> "<svg width='20' height='20' viewBox='0 0 24 24' fill='none' stroke='#16A34A' stroke-width='2'><polyline points='20 6 9 17 4 12'/></svg>";
            case DIBATALKAN -> "<svg width='20' height='20' viewBox='0 0 24 24' fill='none' stroke='#EF4444' stroke-width='2'><circle cx='12' cy='12' r='10'/><line x1='15' y1='9' x2='9' y2='15'/><line x1='9' y1='9' x2='15' y2='15'/></svg>";
            default -> "<svg width='20' height='20' viewBox='0 0 24 24' fill='none' stroke='#2563EB' stroke-width='2'><path d='M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z'/><polyline points='14 2 14 8 20 8'/></svg>";
        };
    }

    private String statusTitle(OrderStatus s) {
        return switch (s) {
            case MENUNGGU_PEMBAYARAN -> "Menunggu Pembayaran";
            case DIBAYAR -> "Pembayaran Diterima";
            case DIPROSES -> "Pesanan Sedang Diproses";
            case DIKIRIM -> "Pesanan Dikirim";
            case DITERIMA -> "Pesanan Diterima";
            case SELESAI -> "Pesanan Selesai";
            case DIBATALKAN -> "Pesanan Dibatalkan";
            default -> "Update Pesanan";
        };
    }

    private String statusDesc(OrderStatus s) {
        return switch (s) {
            case MENUNGGU_PEMBAYARAN -> "Selesaikan pembayaranmu sebelum kadaluarsa.";
            case DIBAYAR -> "Pembayaran berhasil! Menunggu penjual memproses pesanan.";
            case DIPROSES -> "Penjual sedang mempersiapkan barangmu.";
            case DIKIRIM -> "Barangmu sudah dalam perjalanan. Konfirmasi saat diterima.";
            case DITERIMA -> "Kamu sudah menerima barang.";
            case SELESAI -> "Transaksi selesai! Dana telah diteruskan ke penjual.";
            case DIBATALKAN -> "Pesanan ini telah dibatalkan.";
            default -> "Status pesanan diperbarui.";
        };
    }

    // ─── DTO ────────────────────────────────────────────────────────────────────

    private static class NotifItem {
        String key, icon, title, body, time, dateLabel, category, route, bgColor;

        NotifItem(String key, String icon, String title, String body,
                  String time, String dateLabel, String category, String route, String bgColor) {
            this.key = key; this.icon = icon; this.title = title;
            this.body = body; this.time = time; this.dateLabel = dateLabel;
            this.category = category; this.route = route; this.bgColor = bgColor;
        }
    }
}
