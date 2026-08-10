package com.example.application.views.order;

import com.example.application.views.MainLayout;
import com.example.application.model.order.Order;
import com.example.application.service.order.OrderService;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "orders-old", layout = MainLayout.class)
@PageTitle("Riwayat Pesanan | Rewear")
@Menu(order = 3, icon = "line-awesome/svg/receipt-solid.svg", title = "Pesanan Saya")
public class OrderHistoryView extends VerticalLayout {

    private final OrderService orderService;
    private final Grid<Order> orderGrid = new Grid<>(Order.class, false);

    public OrderHistoryView(OrderService orderService) {
        this.orderService = orderService;

        setSpacing(true);
        setPadding(true);
        setSizeFull();

        add(createHeader(), createOrderGrid());
    }

    private VerticalLayout createHeader() {
        H2 title = new H2("📋 Riwayat Pesanan & Transaksi");
        Paragraph description = new Paragraph("Pantau status pengiriman dan riwayat belanja Anda.");
        VerticalLayout header = new VerticalLayout(title, description);
        header.setPadding(false);
        header.setSpacing(false);
        return header;
    }

    private Grid<Order> createOrderGrid() {
        orderGrid.addColumn(Order::getOrderNumber).setHeader("No. Pesanan");
        orderGrid.addColumn(p -> "Rp " + String.format("%,.0f", p.getTotalAmount())).setHeader("Total Belanja");
        orderGrid.addColumn(p -> p.getShippingMethod() != null ? p.getShippingMethod().getValue() : "-").setHeader("Metode Kirim");
        orderGrid.addColumn(p -> p.getStatus() != null ? p.getStatus().getValue() : "-").setHeader("Status");
        orderGrid.addColumn(Order::getCreatedAt).setHeader("Tanggal");

        orderGrid.setSizeFull();
        return orderGrid;
    }
}
