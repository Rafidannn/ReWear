package com.example.application.views.user;

import com.example.application.model.order.Order;
import com.example.application.model.order.OrderStatus;
import com.example.application.model.order.ShippingMethod;
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
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@PageTitle("Pusat Notifikasi — ReWear SMKN 24")
@Route(value = "notifications", layout = MainLayout.class)
public class NotificationView extends Div implements BeforeEnterObserver {

    private final OrderService orderService;
    private final Div contentContainer = new Div();
    private String activeCategory = "SEMUA"; // SEMUA, PESANAN, PEMBAYARAN, AKUN
    private boolean isAllMarkedRead = false;

    public NotificationView(OrderService orderService) {
        this.orderService = orderService;

        setWidthFull();
        getElement().getStyle()
            .set("background", "linear-gradient(160deg, #F0F4FF 0%, #F8FAFF 100%)")
            .set("min-height", "100vh")
            .set("padding", "32px 16px 80px 16px")
            .set("box-sizing", "border-box");

        add(contentContainer);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!AuthGuard.requireLogin(UI.getCurrent())) return;
        buildView();
    }

    private void buildView() {
        contentContainer.removeAll();
        contentContainer.getElement().getStyle()
            .set("max-width", "680px")
            .set("margin", "0 auto")
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("gap", "16px");

        User user = AuthGuard.getCurrentUser();
        if (user == null) return;

        // ── 1. Top Header ──────────────────────────────────────────
        Div headerCard = new Div();
        headerCard.getElement().getStyle()
            .set("background", "#FFFFFF")
            .set("border-radius", "16px")
            .set("padding", "20px")
            .set("box-shadow", "0 2px 10px rgba(0,25,52,0.04)")
            .set("border", "1px solid #E2E8F0");

        HorizontalLayout topRow = new HorizontalLayout();
        topRow.setWidthFull();
        topRow.setAlignItems(FlexComponent.Alignment.CENTER);
        topRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Div titleGroup = new Div();
        H2 mainTitle = new H2("Pusat Notifikasi");
        mainTitle.getStyle()
            .set("font-size", "22px").set("font-weight", "800")
            .set("color", "#001934").set("margin", "0 0 4px 0");

        Paragraph subText = new Paragraph("Update aktivitas pesanan, transaksi, dan sistem ReWear SMKN 24");
        subText.getStyle().set("font-size", "13px").set("color", "#64748B").set("margin", "0");
        titleGroup.add(mainTitle, subText);

        Button btnMarkRead = new Button("Tandai Sudah Dibaca", VaadinIcon.CHECK_CIRCLE.create(), e -> {
            isAllMarkedRead = true;
            Notification.show("Semua notifikasi telah ditandai sudah dibaca.", 2500, Notification.Position.TOP_CENTER);
            buildView();
        });
        btnMarkRead.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnMarkRead.getStyle().set("font-size", "12px").set("font-weight", "700").set("color", "#2563EB");

        topRow.add(titleGroup, btnMarkRead);
        headerCard.add(topRow);

        // ── 2. Category Filter Pills ───────────────────────────────
        HorizontalLayout filterRow = new HorizontalLayout();
        filterRow.setWidthFull();
        filterRow.setSpacing(true);
        filterRow.getElement().getStyle()
            .set("overflow-x", "auto")
            .set("padding", "12px 0 0 0")
            .set("border-top", "1px solid #F1F5F9")
            .set("margin-top", "16px");

        filterRow.add(
            createFilterPill("Semua", "SEMUA"),
            createFilterPill("Pesanan", "PESANAN"),
            createFilterPill("Pembayaran & Escrow", "PEMBAYARAN"),
            createFilterPill("Akun & Sekolah", "AKUN")
        );
        headerCard.add(filterRow);
        contentContainer.add(headerCard);

        // ── 3. Load & Render Notifications List ────────────────────
        List<NotificationItem> items = collectUserNotifications(user);

        // Filter items
        List<NotificationItem> filtered = items.stream().filter(item -> {
            if ("PESANAN".equals(activeCategory)) return "PESANAN".equals(item.category);
            if ("PEMBAYARAN".equals(activeCategory)) return "PEMBAYARAN".equals(item.category);
            if ("AKUN".equals(activeCategory)) return "AKUN".equals(item.category);
            return true;
        }).toList();

        if (filtered.isEmpty()) {
            Div emptyCard = new Div();
            emptyCard.getElement().getStyle()
                .set("background", "#FFFFFF").set("border-radius", "16px")
                .set("padding", "48px 20px").set("text-align", "center")
                .set("border", "1px solid #E2E8F0");

            Div iconBox = new Div();
            iconBox.getElement().setProperty("innerHTML",
                "<div style='width:56px;height:56px;border-radius:50%;background:#F1F5F9;margin:0 auto 16px auto;display:flex;align-items:center;justify-content:center;'>" +
                "<svg width='28' height='28' viewBox='0 0 24 24' fill='none' stroke='#94A3B8' stroke-width='2'><path d='M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9'/><path d='M13.73 21a2 2 0 0 1-3.46 0'/></svg>" +
                "</div>"
            );

            H3 emptyTitle = new H3("Belum Ada Notifikasi");
            emptyTitle.getStyle().set("font-size", "16px").set("font-weight", "700").set("color", "#001934").set("margin", "0 0 6px 0");

            Paragraph emptySub = new Paragraph("Aktivitas pesanan, pembayaran, dan informasi terbaru akan muncul di sini.");
            emptySub.getStyle().set("font-size", "13px").set("color", "#64748B").set("margin", "0");

            emptyCard.add(iconBox, emptyTitle, emptySub);
            contentContainer.add(emptyCard);
        } else {
            Div listContainer = new Div();
            listContainer.getElement().getStyle()
                .set("display", "flex").set("flex-direction", "column").set("gap", "10px");

            for (NotificationItem notif : filtered) {
                listContainer.add(renderNotificationCard(notif));
            }
            contentContainer.add(listContainer);
        }
    }

    private Button createFilterPill(String label, String catKey) {
        boolean isActive = catKey.equals(activeCategory);
        Button btn = new Button(label);
        btn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btn.getElement().getStyle()
            .set("font-size", "12px").set("font-weight", "700")
            .set("padding", "6px 14px")
            .set("border-radius", "20px")
            .set("cursor", "pointer")
            .set("transition", "all 0.2s ease")
            .set("background", isActive ? "#001934" : "#F1F5F9")
            .set("color", isActive ? "#FFFFFF" : "#475569");

        btn.addClickListener(e -> {
            activeCategory = catKey;
            buildView();
        });
        return btn;
    }

    private Div renderNotificationCard(NotificationItem item) {
        Div card = new Div();
        card.getElement().getStyle()
            .set("background", isAllMarkedRead || item.isRead ? "#FFFFFF" : "#F8FAFF")
            .set("border-radius", "14px")
            .set("padding", "16px")
            .set("border", isAllMarkedRead || item.isRead ? "1px solid #E2E8F0" : "1.5px solid #BFDBFE")
            .set("box-shadow", "0 1px 3px rgba(0,25,52,0.03)")
            .set("display", "flex")
            .set("align-items", "flex-start")
            .set("gap", "14px")
            .set("transition", "transform 0.15s ease");

        // Icon circle
        Div iconWrap = new Div();
        iconWrap.getElement().getStyle()
            .set("width", "40px").set("height", "40px").set("border-radius", "10px")
            .set("background", item.iconBg).set("flex-shrink", "0")
            .set("display", "flex").set("align-items", "center").set("justify-content", "center");
        iconWrap.getElement().setProperty("innerHTML", item.iconSvg);

        // Body
        Div body = new Div();
        body.getElement().getStyle().set("flex", "1");

        HorizontalLayout topMeta = new HorizontalLayout();
        topMeta.setWidthFull();
        topMeta.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        topMeta.setAlignItems(FlexComponent.Alignment.CENTER);

        Span titleSpan = new Span(item.title);
        titleSpan.getStyle().set("font-size", "14px").set("font-weight", "800").set("color", "#001934");

        Span timeSpan = new Span(item.timeAgo);
        timeSpan.getStyle().set("font-size", "11px").set("color", "#94A3B8").set("font-weight", "600");
        topMeta.add(titleSpan, timeSpan);

        Paragraph desc = new Paragraph(item.message);
        desc.getStyle().set("font-size", "13px").set("color", "#475569").set("margin", "4px 0 10px 0").set("line-height", "1.4");

        body.add(topMeta, desc);

        if (item.actionRoute != null && !item.actionRoute.isBlank()) {
            Button btnAction = new Button(item.actionLabel != null ? item.actionLabel : "Lihat Detail", e -> {
                UI.getCurrent().navigate(item.actionRoute);
            });
            btnAction.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            btnAction.getStyle()
                .set("font-size", "12px").set("font-weight", "800")
                .set("color", "#001934").set("padding", "0").set("cursor", "pointer");
            body.add(btnAction);
        }

        card.add(iconWrap, body);
        return card;
    }

    private List<NotificationItem> collectUserNotifications(User user) {
        List<NotificationItem> list = new ArrayList<>();

        // 1. Buyer Orders
        List<Order> buyerOrders = orderService.getBuyerOrders(user);
        for (Order o : buyerOrders) {
            String timeAgo = formatTimeAgo(o.getUpdatedAt() != null ? o.getUpdatedAt() : o.getCreatedAt());
            LocalDateTime orderTime = o.getUpdatedAt() != null ? o.getUpdatedAt() : o.getCreatedAt();

            if (o.getStatus() == OrderStatus.MENUNGGU_PEMBAYARAN) {
                list.add(new NotificationItem(
                    "Menunggu Pembayaran",
                    "Pesanan #" + o.getOrderNumber() + " telah dibuat. Segera selesaikan pembayaran dan unggah bukti transfer agar pesanan segera diproses.",
                    timeAgo, orderTime, "PEMBAYARAN",
                    "<svg width='20' height='20' viewBox='0 0 24 24' fill='none' stroke='#D97706' stroke-width='2'><rect x='2' y='5' width='20' height='14' rx='2'/><line x1='2' y1='10' x2='22' y2='10'/></svg>",
                    "#FEF3C7", "orders", "Unggah Bukti Bayar ›", false
                ));
            } else if (o.getStatus() == OrderStatus.DIPROSES) {
                list.add(new NotificationItem(
                    "Pembayaran Diverifikasi",
                    "Pembayaran pesanan #" + o.getOrderNumber() + " telah diverifikasi. Penjual sedang menyiapkan pesanan Anda.",
                    timeAgo, orderTime, "PESANAN",
                    "<svg width='20' height='20' viewBox='0 0 24 24' fill='none' stroke='#2563EB' stroke-width='2'><path d='M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z'/></svg>",
                    "#DBEAFE", "orders", "Lacak Pesanan ›", false
                ));
            } else if (o.getStatus() == OrderStatus.DIKIRIM) {
                boolean isCod = o.getShippingMethod() == ShippingMethod.COD_SEKOLAH;
                String msg = isCod
                    ? "Jadwal COD telah ditentukan! Titik temu di " + (o.getShippingAddress() != null ? o.getShippingAddress() : "SMKN 24") + "."
                    : "Pesanan #" + o.getOrderNumber() + " telah dikirim oleh penjual melalui " + (o.getCourierName() != null ? o.getCourierName().name() : "Kurir") + ".";
                list.add(new NotificationItem(
                    isCod ? "Jadwal COD Siap" : "Pesanan Dikirim",
                    msg, timeAgo, orderTime, "PESANAN",
                    "<svg width='20' height='20' viewBox='0 0 24 24' fill='none' stroke='#16A34A' stroke-width='2'><path d='M1 3h15v13H1z'/><path d='M16 8h4l3 3v5h-7V8z'/><circle cx='5.5' cy='18.5' r='2.5'/><circle cx='18.5' cy='18.5' r='2.5'/></svg>",
                    "#DCFCE7", "orders", "Lihat Jadwal & Titik Temu ›", false
                ));
            } else if (o.getStatus() == OrderStatus.SELESAI) {
                list.add(new NotificationItem(
                    "Pesanan Selesai",
                    "Pesanan #" + o.getOrderNumber() + " telah selesai. Terima kasih telah bertransaksi di ReWear!",
                    timeAgo, orderTime, "PESANAN",
                    "<svg width='20' height='20' viewBox='0 0 24 24' fill='none' stroke='#16A34A' stroke-width='2'><path d='M22 11.08V12a10 10 0 1 1-5.93-9.14'/><polyline points='22 4 12 14.01 9 11.01'/></svg>",
                    "#DCFCE7", "orders", "Beri Ulasan ›", true
                ));
            } else if (o.getStatus() == OrderStatus.KOMPLAIN) {
                list.add(new NotificationItem(
                    "Komplain Dalam Peninjauan",
                    "Pengajuan komplain pesanan #" + o.getOrderNumber() + " sedang ditinjau oleh Admin ReWear. Dana Escrow tetap aman tertahan.",
                    timeAgo, orderTime, "PEMBAYARAN",
                    "<svg width='20' height='20' viewBox='0 0 24 24' fill='none' stroke='#DC2626' stroke-width='2'><circle cx='12' cy='12' r='10'/><line x1='12' y1='8' x2='12' y2='12'/><line x1='12' y1='16' x2='12.01' y2='16'/></svg>",
                    "#FEE2E2", "orders", "Lihat Sengketa ›", false
                ));
            }
        }

        // 2. Seller Orders
        List<Order> sellerOrders = orderService.getSellerOrders(user);
        for (Order so : sellerOrders) {
            String timeAgo = formatTimeAgo(so.getUpdatedAt() != null ? so.getUpdatedAt() : so.getCreatedAt());
            LocalDateTime soTime = so.getUpdatedAt() != null ? so.getUpdatedAt() : so.getCreatedAt();

            if (so.getStatus() == OrderStatus.DIPROSES) {
                list.add(new NotificationItem(
                    "Pesanan Baru Masuk!",
                    "Pembeli telah membayar pesanan #" + so.getOrderNumber() + " (Rp " + String.format("%,.0f", so.getTotalAmount().doubleValue()) + "). Silakan atur titik temu COD atau kirim paket.",
                    timeAgo, soTime, "PESANAN",
                    "<svg width='20' height='20' viewBox='0 0 24 24' fill='none' stroke='#B45309' stroke-width='2'><path d='M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z'/></svg>",
                    "#FEF3C7", "seller", "Proses Pesanan Sekarang ›", false
                ));
            } else if (so.getStatus() == OrderStatus.KOMPLAIN) {
                list.add(new NotificationItem(
                    "Pemberitahuan Komplain Pembeli",
                    "Pembeli mengajukan komplain atas pesanan #" + so.getOrderNumber() + ". Dana Escrow tertahan hingga keputusan admin.",
                    timeAgo, soTime, "PEMBAYARAN",
                    "<svg width='20' height='20' viewBox='0 0 24 24' fill='none' stroke='#DC2626' stroke-width='2'><path d='M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z'/></svg>",
                    "#FEE2E2", "seller", "Lihat Bukti Komplain ›", false
                ));
            } else if (so.getStatus() == OrderStatus.SELESAI) {
                list.add(new NotificationItem(
                    "Dana Penjualan Masuk!",
                    "Pesanan #" + so.getOrderNumber() + " telah diselesaikan pembeli. Dana sebesar Rp " + String.format("%,.0f", so.getTotalAmount().doubleValue()) + " telah cair ke saldo ReWear Pay Anda.",
                    timeAgo, soTime, "PEMBAYARAN",
                    "<svg width='20' height='20' viewBox='0 0 24 24' fill='none' stroke='#16A34A' stroke-width='2'><line x1='12' y1='1' x2='12' y2='23'/><path d='M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6'/></svg>",
                    "#DCFCE7", "profile?tab=rewearpay", "Cek Saldo Dompet ›", true
                ));
            }
        }

        // 3. School Verification Status
        if (user.getSchool() != null) {
            list.add(new NotificationItem(
                "Akun Siswa Terverifikasi",
                "Selamat! Akun Anda telah resmi terverifikasi sebagai Warga " + user.getSchool().getName() + ". Anda bebas bertransaksi di Pasar SMKN 24.",
                formatTimeAgo(user.getCreatedAt()), user.getCreatedAt(), "AKUN",
                "<svg width='20' height='20' viewBox='0 0 24 24' fill='none' stroke='#2563EB' stroke-width='2'><path d='M22 10v6M2 10l10-5 10 5-10 5z'/><path d='M6 12v5c3 3 9 3 12 0v-5'/></svg>",
                "#DBEAFE", "profile", "Lihat Profil ›", true
            ));
        }

        // Sort descending by time
        list.sort(Comparator.comparing(NotificationItem::getTime, Comparator.nullsLast(Comparator.reverseOrder())));
        return list;
    }

    private String formatTimeAgo(LocalDateTime time) {
        if (time == null) return "Baru saja";
        LocalDateTime now = LocalDateTime.now();
        long mins = ChronoUnit.MINUTES.between(time, now);
        if (mins < 1) return "Baru saja";
        if (mins < 60) return mins + " menit lalu";
        long hours = ChronoUnit.HOURS.between(time, now);
        if (hours < 24) return hours + " jam lalu";
        long days = ChronoUnit.DAYS.between(time, now);
        if (days < 7) return days + " hari lalu";
        return time.format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
    }

    private static class NotificationItem {
        String title;
        String message;
        String timeAgo;
        LocalDateTime time;
        String category;
        String iconSvg;
        String iconBg;
        String actionRoute;
        String actionLabel;
        boolean isRead;

        public NotificationItem(String title, String message, String timeAgo, LocalDateTime time, String category,
                                String iconSvg, String iconBg, String actionRoute, String actionLabel, boolean isRead) {
            this.title = title;
            this.message = message;
            this.timeAgo = timeAgo;
            this.time = time;
            this.category = category;
            this.iconSvg = iconSvg;
            this.iconBg = iconBg;
            this.actionRoute = actionRoute;
            this.actionLabel = actionLabel;
            this.isRead = isRead;
        }

        public LocalDateTime getTime() {
            return time;
        }
    }
}
