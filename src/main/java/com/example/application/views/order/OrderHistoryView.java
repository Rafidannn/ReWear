package com.example.application.views.order;

import com.example.application.model.order.Order;
import com.example.application.model.order.OrderItem;
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
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.format.DateTimeFormatter;
import java.util.List;

@PageTitle("Riwayat Pesanan - ReWear")
@Route(value = "orders", layout = MainLayout.class)
public class OrderHistoryView extends Div implements BeforeEnterObserver {

    private final OrderService orderService;
    private final Div contentContainer = new Div();

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    public OrderHistoryView(OrderService orderService) {
        this.orderService = orderService;
        addClassName("rw-orders-page");
        add(contentContainer);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!AuthGuard.requireLogin(UI.getCurrent())) return;
        buildView();
    }

    private void buildView() {
        contentContainer.removeAll();

        User user = AuthGuard.getCurrentUser();
        if (user == null) return;

        List<Order> orders = orderService.getBuyerOrders(user);

        Div wrapper = new Div();
        wrapper.addClassName("rw-orders-wrapper");

        // Header
        Div headerArea = new Div();
        headerArea.addClassName("rw-orders-header");

        H2 title = new H2("📦 Riwayat Pesanan");
        title.addClassName("rw-orders-title");

        Paragraph sub = new Paragraph("Pantau status semua pesananmu di sini.");
        sub.addClassName("rw-orders-subtitle");

        headerArea.add(title, sub);
        wrapper.add(headerArea);

        if (orders.isEmpty()) {
            wrapper.add(buildEmptyState());
        } else {
            Div orderList = new Div();
            orderList.addClassName("rw-order-list");

            for (Order order : orders) {
                orderList.add(buildOrderCard(order, user));
            }
            wrapper.add(orderList);
        }

        contentContainer.add(wrapper);
    }

    private Div buildOrderCard(Order order, User user) {
        Div card = new Div();
        card.addClassName("rw-order-card");

        // ---- Card Header ----
        Div cardHeader = new Div();
        cardHeader.addClassName("rw-order-card-header");

        Div orderMeta = new Div();
        orderMeta.addClassName("rw-order-meta");

        Span orderNum = new Span("# " + order.getOrderNumber());
        orderNum.addClassName("rw-order-number");

        String dateStr = order.getCreatedAt() != null ? order.getCreatedAt().format(DATE_FMT) : "-";
        Span orderDate = new Span(dateStr);
        orderDate.addClassName("rw-order-date");

        orderMeta.add(orderNum, orderDate);

        Span statusBadge = buildStatusBadge(order.getStatus());

        cardHeader.add(orderMeta, statusBadge);
        card.add(cardHeader);

        // ---- Items ----
        List<OrderItem> items = orderService.getOrderItems(order);
        if (!items.isEmpty()) {
            Div itemsSection = new Div();
            itemsSection.addClassName("rw-order-items-section");

            for (int i = 0; i < Math.min(items.size(), 3); i++) {
                OrderItem item = items.get(i);
                Div itemRow = new Div();
                itemRow.addClassName("rw-order-item-row");

                Div itemInfo = new Div();
                itemInfo.addClassName("rw-order-item-info");

                Span itemName = new Span(item.getProductNameSnapshot());
                itemName.addClassName("rw-order-item-name");

                Span itemMeta = new Span("Qty: " + item.getQuantity() + "  •  Rp " +
                    String.format("%,.0f", item.getPriceSnapshot()));
                itemMeta.addClassName("rw-order-item-meta");

                itemInfo.add(itemName, itemMeta);
                itemRow.add(itemInfo);
                itemsSection.add(itemRow);
            }

            if (items.size() > 3) {
                Span more = new Span("+" + (items.size() - 3) + " produk lainnya");
                more.addClassName("rw-order-more-items");
                itemsSection.add(more);
            }

            card.add(itemsSection);
        }

        // ---- Divider ----
        Hr divider = new Hr();
        divider.addClassName("rw-order-divider");
        card.add(divider);

        // ---- Footer: Total + Actions ----
        Div cardFooter = new Div();
        cardFooter.addClassName("rw-order-card-footer");

        Div totalSection = new Div();
        totalSection.addClassName("rw-order-total-section");

        Span totalLabel = new Span("Total Pembayaran");
        totalLabel.addClassName("rw-order-total-label");

        Span totalValue = new Span("Rp " + String.format("%,.0f", order.getTotalAmount()));
        totalValue.addClassName("rw-order-total-value");

        totalSection.add(totalLabel, totalValue);

        Div actionsSection = new Div();
        actionsSection.addClassName("rw-order-actions");

        // Tombol aksi berdasarkan status
        if (order.getStatus() == OrderStatus.MENUNGGU_PEMBAYARAN) {
            Button btnBayar = new Button("Bayar Sekarang", VaadinIcon.CREDIT_CARD.create());
            btnBayar.addClassName("rw-btn-bayar");
            btnBayar.getStyle()
                .set("background", "linear-gradient(135deg, #001934, #0A3D7A)")
                .set("color", "#F5C45E")
                .set("border", "none")
                .set("border-radius", "8px")
                .set("font-weight", "700")
                .set("padding", "8px 16px")
                .set("cursor", "pointer");
            btnBayar.addClickListener(e -> {
                orderService.updateOrderStatus(order, OrderStatus.DIBAYAR, "Pembayaran berhasil dikonfirmasi oleh pembeli.", user);
                Notification.show("✅ Pembayaran Berhasil! Pesanan Anda kini sedang diproses penjual.", 3000, Notification.Position.TOP_CENTER);
                buildView();
            });
            actionsSection.add(btnBayar);

            Button btnBatal = new Button("Batalkan");
            btnBatal.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            btnBatal.getStyle().set("font-size", "13px");
            btnBatal.addClickListener(e -> {
                orderService.updateOrderStatus(order, OrderStatus.DIBATALKAN, "Pesanan dibatalkan oleh pembeli.", user);
                Notification.show("Pesanan #" + order.getOrderNumber() + " telah dibatalkan.", 2500, Notification.Position.TOP_CENTER);
                buildView();
            });
            actionsSection.add(btnBatal);
        }

        if (order.getStatus() == OrderStatus.DIKIRIM || order.getStatus() == OrderStatus.DITERIMA) {
            Button btnTerima = new Button("Konfirmasi Diterima", VaadinIcon.CHECK.create());
            btnTerima.addClassName("rw-btn-terima");
            btnTerima.getStyle()
                .set("background", "#16A34A")
                .set("color", "#FFFFFF")
                .set("border", "none")
                .set("border-radius", "8px")
                .set("font-weight", "700")
                .set("padding", "8px 16px")
                .set("cursor", "pointer");
            btnTerima.addClickListener(e -> {
                orderService.updateOrderStatus(order, OrderStatus.SELESAI, "Dikonfirmasi diterima oleh pembeli.", user);
                Notification.show("🎉 Pesanan #" + order.getOrderNumber() + " selesai! Dana telah diteruskan ke penjual.", 3000, Notification.Position.TOP_CENTER);
                buildView();
            });
            actionsSection.add(btnTerima);
        }

        Button btnDetail = new Button("Lihat Detail", VaadinIcon.EYE.create());
        btnDetail.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnDetail.getStyle().set("font-size", "13px");
        btnDetail.addClickListener(e -> openOrderDetailModal(order));
        actionsSection.add(btnDetail);

        cardFooter.add(totalSection, actionsSection);
        card.add(cardFooter);

        return card;
    }

    private Span buildStatusBadge(OrderStatus status) {
        Span badge = new Span();
        badge.addClassName("rw-status-badge");

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
                badge.setText("⚙️ Diproses Penjual");
                badge.getStyle().set("background", "#EFF6FF").set("color", "#1E40AF");
            }
            case DIKIRIM -> {
                badge.setText("🚚 Dalam Pengiriman");
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
            .set("border-radius", "20px")
            .set("white-space", "nowrap");

        return badge;
    }

    private Div buildEmptyState() {
        Div empty = new Div();
        empty.addClassName("rw-orders-empty");
        empty.getStyle()
            .set("text-align", "center")
            .set("padding", "64px 24px")
            .set("background", "#FFFFFF")
            .set("border-radius", "16px")
            .set("border", "1px solid #E2E8F0")
            .set("margin-top", "24px");

        empty.getElement().setProperty("innerHTML",
            "<svg width='80' height='80' viewBox='0 0 24 24' fill='none' stroke='#CBD5E1' stroke-width='1.5' style='margin-bottom:20px;'>" +
            "<path d='M6 2L3 6v14a2 2 0 002 2h14a2 2 0 002-2V6l-3-4z'/>" +
            "<line x1='3' y1='6' x2='21' y2='6'/>" +
            "<path d='M16 10a4 4 0 01-8 0'/></svg>" +
            "<h3 style='color:#001934;font-size:20px;margin:0 0 8px 0;'>Belum Ada Pesanan</h3>" +
            "<p style='color:#64748B;font-size:14px;margin:0 0 24px 0;'>Kamu belum pernah melakukan pembelian. Yuk mulai belanja!</p>"
        );

        Button btnShop = new Button("Jelajahi Produk", VaadinIcon.CART.create());
        btnShop.getStyle()
            .set("background", "linear-gradient(135deg, #001934, #0A3D7A)")
            .set("color", "#F5C45E")
            .set("border", "none")
            .set("border-radius", "10px")
            .set("font-weight", "700")
            .set("padding", "12px 24px")
            .set("cursor", "pointer");
        btnShop.addClickListener(e -> UI.getCurrent().navigate(""));
        empty.add(btnShop);

        return empty;
    }

    private void openOrderDetailModal(Order order) {
        Dialog d = new Dialog();
        d.setHeaderTitle("Rincian Pesanan #" + order.getOrderNumber());
        d.setWidth("480px");

        Div body = new Div();
        body.getStyle().set("font-size", "13px").set("color", "#334155").set("display", "flex").set("flex-direction", "column").set("gap", "10px");

        body.add(new Div(new Span("📍 Alamat Pengiriman: "), new Span(order.getShippingAddress() != null ? order.getShippingAddress() : "-")));
        body.add(new Div(new Span("💳 Metode Pembayaran: "), new Span(order.getPaymentMethod() != null ? order.getPaymentMethod() : "-")));
        body.add(new Div(new Span("🚚 Kurir / Pengiriman: "), new Span(order.getShippingMethod() != null ? order.getShippingMethod().name() : "-")));

        d.add(body);
        Button btnClose = new Button("Tutup", e -> d.close());
        d.getFooter().add(btnClose);
        d.open();
    }
}
