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
            .set("max-width", "760px")
            .set("margin", "0 auto")
            .set("padding", "0 24px");

        User user = AuthGuard.getCurrentUser();
        if (user == null) return;

        List<NotifItem> allNotifs = buildNotifications(user);

        // Count unread
        long unreadCount = allNotifs.stream().filter(n -> !isRead(n.key)).count();

        // ─── Page Header ───
        Div header = new Div();
        header.getElement().getStyle()
            .set("display", "flex").set("align-items", "center")
            .set("justify-content", "space-between").set("margin-bottom", "24px");

        Div titleWrap = new Div();
        Div titleRow = new Div();
        titleRow.getElement().getStyle().set("display", "flex").set("align-items", "center").set("gap", "10px");

        Button btnBack = new Button(VaadinIcon.ARROW_LEFT.create());
        btnBack.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnBack.getElement().getStyle()
            .set("color", "#001934")
            .set("font-weight", "700")
            .set("cursor", "pointer")
            .set("padding", "6px 10px")
            .set("background", "#FFFFFF")
            .set("border", "1px solid #E2E8F0")
            .set("border-radius", "8px");
        btnBack.addClickListener(e -> UI.getCurrent().getPage().getHistory().back());

        H2 title = new H2("Notifikasi");
        title.getElement().getStyle()
            .set("font-size", "24px").set("font-weight", "900")
            .set("color", "#001934").set("margin", "0");
        titleRow.add(btnBack, title);

        if (unreadCount > 0) {
            Span badge = new Span(String.valueOf(unreadCount));
            badge.getElement().getStyle()
                .set("background", "#EF4444").set("color", "#FFFFFF")
                .set("font-size", "12px").set("font-weight", "800")
                .set("padding", "2px 8px").set("border-radius", "999px");
            titleRow.add(badge);
        }

        Span sub = new Span("Semua aktivitas akun dan transaksimu");
        sub.getElement().getStyle()
            .set("font-size", "13px").set("color", "#64748B")
            .set("display", "block").set("margin-top", "4px");
        titleWrap.add(titleRow, sub);

        Button btnMarkAll = new Button("Tandai semua dibaca", VaadinIcon.CHECK_CIRCLE.create());
        btnMarkAll.getElement().getStyle()
            .set("background", unreadCount > 0 ? "#001934" : "#E2E8F0")
            .set("color", unreadCount > 0 ? "#F5C45E" : "#94A3B8")
            .set("border", "none").set("border-radius", "8px")
            .set("font-weight", "700").set("font-size", "13px")
            .set("cursor", unreadCount > 0 ? "pointer" : "default")
            .set("padding", "8px 14px");
        btnMarkAll.setEnabled(unreadCount > 0);
        btnMarkAll.addClickListener(e -> {
            markAllRead(allNotifs);
            Notification.show("Semua notifikasi ditandai sudah dibaca.", 2000, Notification.Position.TOP_CENTER);
            buildView();
        });

        header.add(titleWrap, btnMarkAll);
        contentWrap.add(header);

        // ─── Filter Tabs ───
        Div filterRow = new Div();
        filterRow.getElement().getStyle()
            .set("display", "flex").set("gap", "8px").set("margin-bottom", "20px");

        long unreadTransaksi = allNotifs.stream().filter(n -> n.category.equals("transaksi") && !isRead(n.key)).count();
        long unreadSistem    = allNotifs.stream().filter(n -> n.category.equals("sistem")    && !isRead(n.key)).count();

        String[][] filters = {
            {"semua",     "Semua" + (unreadCount > 0 ? " (" + unreadCount + ")" : "")},
            {"transaksi", "Transaksi" + (unreadTransaksi > 0 ? " (" + unreadTransaksi + ")" : "")},
            {"sistem",    "Sistem" + (unreadSistem > 0 ? " (" + unreadSistem + ")" : "")},
        };
        for (String[] f : filters) {
            Button btn = new Button(f[1]);
            boolean isActive = activeFilter.equals(f[0]);
            btn.getElement().getStyle()
                .set("background", isActive ? "#001934" : "#FFFFFF")
                .set("color", isActive ? "#F5C45E" : "#64748B")
                .set("border", isActive ? "none" : "1.5px solid #E2E8F0")
                .set("border-radius", "20px").set("font-weight", "700")
                .set("font-size", "13px").set("padding", "7px 16px").set("cursor", "pointer");
            final String key = f[0];
            btn.addClickListener(e -> { activeFilter = key; buildView(); });
            filterRow.add(btn);
        }
        contentWrap.add(filterRow);

        // ─── Render filtered list ───
        List<NotifItem> filtered = allNotifs.stream()
            .filter(n -> activeFilter.equals("semua") || n.category.equals(activeFilter))
            .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            contentWrap.add(buildEmptyState());
            return;
        }

        // Group by dateLabel
        String prevDate = null;
        for (NotifItem notif : filtered) {
            if (!notif.dateLabel.equals(prevDate)) {
                Span dateHeader = new Span(notif.dateLabel);
                dateHeader.getElement().getStyle()
                    .set("font-size", "12px").set("font-weight", "700")
                    .set("color", "#94A3B8").set("text-transform", "uppercase")
                    .set("letter-spacing", "0.8px").set("display", "block")
                    .set("margin", "16px 0 8px 4px");
                contentWrap.add(dateHeader);
                prevDate = notif.dateLabel;
            }
            contentWrap.add(buildNotifCard(notif));
        }
    }

    // ─── Build data from real DB ────────────────────────────────────────────────

    private List<NotifItem> buildNotifications(User user) {
        List<NotifItem> list = new ArrayList<>();

        try {
            List<Order> buyerOrders = orderService.getBuyerOrders(user);
            for (Order o : buyerOrders) {
                String key = "buyer-order-" + o.getId();
                String dateLabel = o.getCreatedAt() != null
                    ? o.getCreatedAt().toLocalDate().toString() : "Hari ini";
                String time = o.getCreatedAt() != null ? o.getCreatedAt().format(FMT) : "";
                // Auto-read jika SELESAI atau DIBATALKAN (dan belum pernah di-mark-read oleh user)
                boolean autoRead = (o.getStatus() == OrderStatus.SELESAI || o.getStatus() == OrderStatus.DIBATALKAN);
                if (autoRead) markRead(key);
                list.add(new NotifItem(key, statusIcon(o.getStatus()), statusTitle(o.getStatus()),
                    "Pesanan #" + o.getOrderNumber() + " — " + statusDesc(o.getStatus()),
                    time, dateLabel, "transaksi", "orders"));
            }
            List<Order> sellerOrders = orderService.getSellerOrders(user);
            for (Order o : sellerOrders) {
                if (o.getStatus() == OrderStatus.DIBAYAR || o.getStatus() == OrderStatus.MENUNGGU_PEMBAYARAN) {
                    String key = "seller-order-" + o.getId();
                    String dateLabel = o.getCreatedAt() != null
                        ? o.getCreatedAt().toLocalDate().toString() : "Hari ini";
                    String time = o.getCreatedAt() != null ? o.getCreatedAt().format(FMT) : "";
                    list.add(new NotifItem(key, "<svg width='20' height='20' viewBox='0 0 24 24' fill='none' stroke='#001934' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><path d='M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z'></path><line x1='3' y1='6' x2='21' y2='6'></line><path d='M16 10a4 4 0 0 1-8 0'></path></svg>", "Pesanan Baru Masuk",
                        "Pesanan baru #" + o.getOrderNumber() + " — segera proses!",
                        time, dateLabel, "transaksi", "seller/dashboard"));
                }
            }
        } catch (Exception ignored) {}

        // Sistem statis
        list.add(new NotifItem("sys-welcome", "<svg width='20' height='20' viewBox='0 0 24 24' fill='none' stroke='#001934' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><path d='M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2'></path><circle cx='12' cy='7' r='4'></circle></svg>", "Selamat datang di ReWear!",
            "Akun kamu sudah aktif. Mulai jual atau beli barang thrift sekarang.",
            "", "Sistem", "sistem", ""));
        list.add(new NotifItem("sys-escrow", "<svg width='20' height='20' viewBox='0 0 24 24' fill='none' stroke='#0A3D7A' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><path d='M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z'></path></svg>", "Transaksi Aman dengan Escrow",
            "Semua transaksi dilindungi sistem Escrow ReWear. Dana hanya cair saat pembeli konfirmasi terima barang.",
            "", "Sistem", "sistem", ""));
        // Auto-mark sistem notifs as read on first visit
        markRead("sys-welcome");
        markRead("sys-escrow");

        // Unread first, then newest
        list.sort(Comparator
            .comparing((NotifItem n) -> isRead(n.key))
            .thenComparing(n -> n.time, Comparator.reverseOrder()));

        return list;
    }

    // ─── Card builder ───────────────────────────────────────────────────────────

    private Div buildNotifCard(NotifItem n) {
        boolean read = isRead(n.key);

        Div card = new Div();
        card.getElement().getStyle()
            .set("background", read ? "#FFFFFF" : "#EFF6FF")
            .set("border", read ? "1px solid #E2E8F0" : "1px solid #BFDBFE")
            .set("border-radius", "14px").set("padding", "16px 20px")
            .set("margin-bottom", "10px").set("display", "flex")
            .set("align-items", "flex-start").set("gap", "16px")
            .set("cursor", "pointer").set("transition", "all 0.15s ease")
            .set("position", "relative");

        // Unread blue dot
        if (!read) {
            Div dot = new Div();
            dot.getElement().getStyle()
                .set("width", "9px").set("height", "9px")
                .set("background", "#3B82F6").set("border-radius", "999px")
                .set("position", "absolute").set("top", "16px").set("right", "16px")
                .set("box-shadow", "0 0 6px rgba(59,130,246,0.5)");
            card.add(dot);
        }

        // SVG icon container
        Div iconBox = new Div();
        iconBox.getElement().setProperty("innerHTML",
            "<div style='width:44px;height:44px;border-radius:12px;" +
            "background:" + (read ? "#F8FAFC" : "#DBEAFE") + ";" +
            "display:flex;align-items:center;justify-content:center;" +
            "flex-shrink:0;'>" + n.icon + "</div>");

        // Text
        Div textBlock = new Div();
        textBlock.getElement().getStyle().set("flex", "1");

        Span titleSpan = new Span(n.title);
        titleSpan.getElement().getStyle()
            .set("font-weight", read ? "700" : "900").set("font-size", "14px")
            .set("color", "#001934").set("display", "block").set("margin-bottom", "4px");

        Span bodySpan = new Span(n.body);
        bodySpan.getElement().getStyle()
            .set("font-size", "13px").set("color", "#475569")
            .set("display", "block").set("line-height", "1.55");
        textBlock.add(titleSpan, bodySpan);

        if (!n.time.isEmpty()) {
            Span timeSpan = new Span(n.time);
            timeSpan.getElement().getStyle()
                .set("font-size", "11px").set("color", "#94A3B8")
                .set("display", "block").set("margin-top", "6px");
            textBlock.add(timeSpan);
        }

        card.add(iconBox, textBlock);

        // Click: mark read, then navigate (if has route)
        card.addClickListener(e -> {
            markRead(n.key);
            if (!n.route.isEmpty()) {
                UI.getCurrent().navigate(n.route);
            } else {
                buildView(); // just refresh to remove dot
            }
        });

        card.getElement().addEventListener("mouseover", e ->
            card.getElement().getStyle().set("box-shadow", "0 4px 16px rgba(0,25,52,0.08)").set("transform", "translateY(-1px)"));
        card.getElement().addEventListener("mouseout", e ->
            card.getElement().getStyle().set("box-shadow", "none").set("transform", "translateY(0)"));

        return card;
    }

    // ─── Empty state ────────────────────────────────────────────────────────────

    private Div buildEmptyState() {
        Div empty = new Div();
        empty.getElement().getStyle()
            .set("text-align", "center").set("padding", "80px 24px")
            .set("background", "#FFFFFF").set("border-radius", "20px")
            .set("border", "1px solid #E2E8F0");
        empty.getElement().setProperty("innerHTML",
            "<div style='width:56px;height:56px;margin:0 auto 16px;border-radius:14px;background:#F1F5F9;display:flex;align-items:center;justify-content:center;'>" +
            "<svg width='28' height='28' viewBox='0 0 24 24' fill='none' stroke='#64748B' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><path d='M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9'></path><path d='M13.73 21a2 2 0 0 1-3.46 0'></path></svg>" +
            "</div>" +
            "<h3 style='font-size:18px;font-weight:800;color:#001934;margin:0 0 8px 0;'>Tidak Ada Notifikasi</h3>" +
            "<p style='font-size:14px;color:#64748B;margin:0;'>Kamu akan menerima notifikasi saat ada aktivitas baru di akunmu.</p>"
        );
        return empty;
    }

    // ─── Helpers ────────────────────────────────────────────────────────────────

    private String statusIcon(OrderStatus s) {
        return switch (s) {
            case MENUNGGU_PEMBAYARAN -> "<svg width='20' height='20' viewBox='0 0 24 24' fill='none' stroke='#D97706' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><circle cx='12' cy='12' r='10'/><polyline points='12 6 12 12 16 14'/></svg>";
            case DIBAYAR -> "<svg width='20' height='20' viewBox='0 0 24 24' fill='none' stroke='#16A34A' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><path d='M22 11.08V12a10 10 0 1 1-5.93-9.14'/><polyline points='22 4 12 14.01 9 11.01'/></svg>";
            case DIPROSES -> "<svg width='20' height='20' viewBox='0 0 24 24' fill='none' stroke='#2563EB' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><circle cx='12' cy='12' r='3'/><path d='M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z'/></svg>";
            case DIKIRIM -> "<svg width='20' height='20' viewBox='0 0 24 24' fill='none' stroke='#059669' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><rect x='1' y='3' width='15' height='13'/><polygon points='16 8 20 8 23 11 23 16 16 16 16 8'/><circle cx='5.5' cy='18.5' r='2.5'/><circle cx='18.5' cy='18.5' r='2.5'/></svg>";
            case DITERIMA, SELESAI -> "<svg width='20' height='20' viewBox='0 0 24 24' fill='none' stroke='#16A34A' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><polyline points='20 6 9 17 4 12'/></svg>";
            case DIBATALKAN -> "<svg width='20' height='20' viewBox='0 0 24 24' fill='none' stroke='#EF4444' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><circle cx='12' cy='12' r='10'/><line x1='15' y1='9' x2='9' y2='15'/><line x1='9' y1='9' x2='15' y2='15'/></svg>";
            default -> "<svg width='20' height='20' viewBox='0 0 24 24' fill='none' stroke='#0A3D7A' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><path d='M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z'/><polyline points='14 2 14 8 20 8'/></svg>";
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
        String key, icon, title, body, time, dateLabel, category, route;

        NotifItem(String key, String icon, String title, String body,
                  String time, String dateLabel, String category, String route) {
            this.key = key; this.icon = icon; this.title = title;
            this.body = body; this.time = time; this.dateLabel = dateLabel;
            this.category = category; this.route = route;
        }
    }
}
