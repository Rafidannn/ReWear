package com.example.application.views.seller;

import com.example.application.views.MainLayout;
import com.example.application.model.product.Product;
import com.example.application.service.product.ProductService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "seller", layout = MainLayout.class)
@PageTitle("Dashboard Penjual | Rewear")
@Menu(order = 2, icon = "line-awesome/svg/store-solid.svg", title = "Produk Saya")
public class SellerDashboardView extends VerticalLayout {

    private final ProductService productService;
    private final Grid<Product> productGrid = new Grid<>(Product.class, false);

    public SellerDashboardView(ProductService productService) {
        this.productService = productService;

        setSpacing(true);
        setPadding(true);
        setSizeFull();

        add(createHeader(), createProductGrid());
        refreshGrid();
    }

    private VerticalLayout createHeader() {
        H2 title = new H2("📦 Dashboard Penjual");
        Paragraph description = new Paragraph("Kelola barang jualan preloved dan seragam sekolah Anda.");

        Button addProductBtn = new Button("Tambah Produk Baru", VaadinIcon.PLUS.create());
        addProductBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addProductBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("sell")));

        HorizontalLayout topLayout = new HorizontalLayout(title, addProductBtn);
        topLayout.setWidthFull();
        topLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        topLayout.setAlignItems(Alignment.CENTER);

        VerticalLayout header = new VerticalLayout(topLayout, description);
        header.setPadding(false);
        header.setSpacing(false);
        return header;
    }

    private Grid<Product> createProductGrid() {
        productGrid.addColumn(Product::getName).setHeader("Nama Produk");
        productGrid.addColumn(p -> "Rp " + String.format("%,.0f", p.getPrice())).setHeader("Harga");
        productGrid.addColumn(Product::getStock).setHeader("Stok");
        productGrid.addColumn(Product::getSoldCount).setHeader("Terjual");
        productGrid.addColumn(p -> p.getStatus() != null ? p.getStatus().getValue() : "-").setHeader("Status");

        productGrid.setSizeFull();
        return productGrid;
    }

    private void refreshGrid() {
        productGrid.setItems(productService.findActiveProducts());
    }
}
