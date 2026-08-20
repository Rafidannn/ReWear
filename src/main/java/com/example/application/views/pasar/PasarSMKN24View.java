package com.example.application.views.pasar;

import com.example.application.model.moderation.Review;
import com.example.application.model.product.Category;
import com.example.application.model.product.ConditionType;
import com.example.application.model.product.Product;
import com.example.application.model.user.User;
import com.example.application.service.moderation.ModerationService;
import com.example.application.service.product.CategoryService;
import com.example.application.service.product.ProductService;
import com.example.application.util.AuthGuard;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Route(value = "pasar-smkn24", layout = MainLayout.class)
@PageTitle("Pasar SMKN 24 Jakarta | ReWear Marketplace")
public class PasarSMKN24View extends VerticalLayout implements BeforeEnterObserver {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final ModerationService moderationService;

    private static final String SVG_CHECK =
        "<svg width='11' height='11' viewBox='0 0 12 12' fill='none' xmlns='http://www.w3.org/2000/svg'>" +
        "<path d='M2 6L5 9L10 3' stroke='#001934' stroke-width='1.8' stroke-linecap='round' stroke-linejoin='round'/>" +
        "</svg>";

    private static final String SVG_STAR_FILLED =
        "<svg width='12' height='12' viewBox='0 0 24 24' fill='#F0BF5A' xmlns='http://www.w3.org/2000/svg'>" +
        "<polygon points='12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2'/>" +
        "</svg>";

    // State Filter Controls
    private final TextField searchField = new TextField("Pencarian Produk");
    private final RadioButtonGroup<String> categoryRadio = new RadioButtonGroup<>("Kategori");
    private final RadioButtonGroup<String> conditionRadio = new RadioButtonGroup<>("Kondisi Barang");
    private final ComboBox<String> sortCombo = new ComboBox<>("Urutkan");

    private final Div cardsGrid = new Div();
    private final Span totalCountBadge = new Span("0 Produk Found");

    private List<Product> allSchoolProducts;

    public PasarSMKN24View(ProductService productService, CategoryService categoryService, ModerationService moderationService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.moderationService = moderationService;

        setSpacing(false);
        setPadding(false);
        setWidthFull();
        getElement().getStyle()
            .set("background-color", "#F8F9FF")
            .set("min-height", "100vh");

        add(createPageHeader(), createMainContent());
        // Data loaded in beforeEnter so URL params are applied first
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        var queryParams = event.getLocation().getQueryParameters().getParameters();

        // Read optional ?q=keyword query parameter
        if (queryParams.containsKey("q") && !queryParams.get("q").isEmpty()) {
            String keyword = queryParams.get("q").get(0);
            if (keyword != null && !keyword.isBlank()) {
                searchField.setValue(keyword);
            }
        }

        // Read optional ?category=... or ?cat=... or ?kategori=...
        for (String key : List.of("category", "cat", "kategori")) {
            if (queryParams.containsKey(key) && !queryParams.get(key).isEmpty()) {
                String catParam = queryParams.get(key).get(0);
                if (catParam != null && !catParam.isBlank()) {
                    List<Category> categories = categoryService.findAllSorted();
                    for (Category c : categories) {
                        if (c.getName().equalsIgnoreCase(catParam) || (c.getSlug() != null && c.getSlug().equalsIgnoreCase(catParam))) {
                            categoryRadio.setValue(c.getName());
                            break;
                        }
                    }
                }
            }
        }

        loadProductsAndApplyFilters();
    }

    private Component createPageHeader() {
        Div headerWrapper = new Div();
        headerWrapper.setWidthFull();
        headerWrapper.getElement().getStyle()
            .set("background", "linear-gradient(135deg, #001934 0%, #002B5B 100%)")
            .set("color", "#FFFFFF")
            .set("padding", "36px 48px")
            .set("box-sizing", "border-box");

        Div inner = new Div();
        inner.getElement().getStyle()
            .set("max-width", "1280px")
            .set("margin", "0 auto");

        Span badge = new Span("EKSKLUSIF WARGA SMKN 24");
        badge.getElement().getStyle()
            .set("background", "#F5C45E")
            .set("color", "#001934")
            .set("font-weight", "800")
            .set("font-size", "11px")
            .set("padding", "4px 12px")
            .set("border-radius", "9999px")
            .set("letter-spacing", "0.5px")
            .set("display", "inline-block")
            .set("margin-bottom", "12px");

        H1 title = new H1("Pasar SMKN 24 Jakarta");
        title.getElement().getStyle()
            .set("font-size", "32px")
            .set("font-weight", "800")
            .set("color", "#FFFFFF")
            .set("margin", "0 0 8px 0");

        Paragraph sub = new Paragraph("Temukan barang preloved, buku, dan seragam terverifikasi langsung dari siswa dan warga SMKN 24.");
        sub.getElement().getStyle()
            .set("font-size", "15px")
            .set("color", "rgba(255, 255, 255, 0.8)")
            .set("margin", "0");

        inner.add(badge, title, sub);
        headerWrapper.add(inner);
        return headerWrapper;
    }

    private Component createMainContent() {
        Div mainLayout = new Div();
        mainLayout.addClassName("rw-catalog-container");

        // ---- LEFT SIDEBAR FILTER (260px) ----
        Div sidebar = new Div();
        sidebar.addClassName("rw-catalog-sidebar");

        H3 filterHeading = new H3("Filter Produk");
        filterHeading.getElement().getStyle()
            .set("font-size", "18px")
            .set("font-weight", "800")
            .set("color", "#001934")
            .set("margin", "0 0 20px 0");

        // 1. Search Field
        searchField.setPlaceholder("Cari nama barang...");
        searchField.setWidthFull();
        searchField.setClearButtonVisible(true);
        searchField.getElement().getStyle().set("margin-bottom", "20px");
        searchField.addValueChangeListener(e -> applyFilters());

        // 2. Kategori Filter
        List<Category> categories = categoryService.findAllSorted();
        List<String> catOptions = categories.stream().map(Category::getName).collect(Collectors.toList());
        catOptions.add(0, "Semua Kategori");

        categoryRadio.setItems(catOptions);
        categoryRadio.setValue("Semua Kategori");
        categoryRadio.getElement().getStyle().set("margin-bottom", "20px");
        categoryRadio.addValueChangeListener(e -> applyFilters());

        // 3. Kondisi Barang Filter
        conditionRadio.setItems("Semua Kondisi", "Bekas (Preloved)", "Baru");
        conditionRadio.setValue("Semua Kondisi");
        conditionRadio.getElement().getStyle().set("margin-bottom", "20px");
        conditionRadio.addValueChangeListener(e -> applyFilters());

        // 4. Urutan / Sorting
        sortCombo.setItems("Terbaru", "Harga Termurah", "Harga Tertinggi", "Nama A-Z");
        sortCombo.setValue("Terbaru");
        sortCombo.setWidthFull();
        sortCombo.getElement().getStyle().set("margin-bottom", "24px");
        sortCombo.addValueChangeListener(e -> applyFilters());

        // Reset Button
        Button btnReset = new Button("Reset Filter");
        btnReset.setWidthFull();
        btnReset.getElement().getStyle()
            .set("background", "#F1F5F9")
            .set("color", "#475569")
            .set("font-weight", "700")
            .set("border", "none")
            .set("border-radius", "8px")
            .set("cursor", "pointer");

        btnReset.addClickListener(e -> {
            searchField.clear();
            categoryRadio.setValue("Semua Kategori");
            conditionRadio.setValue("Semua Kondisi");
            sortCombo.setValue("Terbaru");
            applyFilters();
        });

        sidebar.add(filterHeading, searchField, categoryRadio, conditionRadio, sortCombo, btnReset);

        // ---- RIGHT PRODUCT GRID SIDE ----
        Div rightSide = new Div();
        rightSide.getElement().getStyle()
            .set("flex", "1")
            .set("min-width", "0")
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("gap", "16px");

        // Top Grid Status Row
        HorizontalLayout gridHeader = new HorizontalLayout();
        gridHeader.setWidthFull();
        gridHeader.setJustifyContentMode(JustifyContentMode.BETWEEN);
        gridHeader.setAlignItems(Alignment.CENTER);

        H2 gridTitle = new H2("Daftar Produk Warga SMKN 24");
        gridTitle.getElement().getStyle()
            .set("font-size", "20px")
            .set("font-weight", "800")
            .set("color", "#001934")
            .set("margin", "0");

        totalCountBadge.getElement().getStyle()
            .set("font-size", "13px")
            .set("font-weight", "700")
            .set("color", "#64748B")
            .set("background", "#E2E8F0")
            .set("padding", "4px 12px")
            .set("border-radius", "9999px");

        gridHeader.add(gridTitle, totalCountBadge);

        cardsGrid.addClassName("products-grid-container");
        cardsGrid.setWidthFull();
        cardsGrid.getElement().getStyle()
            .set("display", "grid")
            .set("grid-template-columns", "repeat(auto-fill, minmax(240px, 1fr))")
            .set("gap", "20px")
            .set("box-sizing", "border-box");

        rightSide.add(gridHeader, cardsGrid);

        mainLayout.add(sidebar, rightSide);
        return mainLayout;
    }

    private void loadProductsAndApplyFilters() {
        // Ambil produk khusus pasar SMKN 24 dari DB (filter out produk milik seller yang sedang login)
        User currentUser = AuthGuard.getCurrentUser();
        List<Product> list = productService.findSchoolMarketWithCategory();
        if (currentUser != null && currentUser.getId() != null) {
            list = list.stream()
                .filter(p -> p.getSeller() == null || !p.getSeller().getId().equals(currentUser.getId()))
                .toList();
        }
        allSchoolProducts = list;
        applyFilters();
    }

    private void applyFilters() {
        cardsGrid.removeAll();

        if (allSchoolProducts == null) return;

        String searchKeyword = searchField.getValue() != null ? searchField.getValue().trim().toLowerCase() : "";
        String selectedCategory = categoryRadio.getValue();
        String selectedCondition = conditionRadio.getValue();
        String selectedSort = sortCombo.getValue();

        List<Product> filtered = allSchoolProducts.stream().filter(p -> {
            // 1. Keyword Filter
            if (!searchKeyword.isEmpty() && !p.getName().toLowerCase().contains(searchKeyword)) {
                return false;
            }

            // 2. Category Filter
            if (selectedCategory != null && !"Semua Kategori".equalsIgnoreCase(selectedCategory)) {
                if (p.getCategory() == null || !selectedCategory.equalsIgnoreCase(p.getCategory().getName())) {
                    return false;
                }
            }

            // 3. Condition Filter
            if (selectedCondition != null && !"Semua Kondisi".equalsIgnoreCase(selectedCondition)) {
                if ("Bekas (Preloved)".equalsIgnoreCase(selectedCondition)) {
                    if (p.getConditionType() != ConditionType.BEKAS) return false;
                } else if ("Baru".equalsIgnoreCase(selectedCondition)) {
                    if (p.getConditionType() != ConditionType.BARU) return false;
                }
            }

            return true;
        }).collect(Collectors.toList());

        // 4. Apply Sorting
        if ("Harga Termurah".equals(selectedSort)) {
            filtered.sort(Comparator.comparing(Product::getPrice));
        } else if ("Harga Tertinggi".equals(selectedSort)) {
            filtered.sort(Comparator.comparing(Product::getPrice).reversed());
        } else if ("Nama A-Z".equals(selectedSort)) {
            filtered.sort(Comparator.comparing(Product::getName));
        } else {
            // Terbaru (ID Descending)
            filtered.sort(Comparator.comparing(Product::getId).reversed());
        }

        totalCountBadge.setText(filtered.size() + " Produk Ditemukan");

        if (filtered.isEmpty()) {
            Paragraph empty = new Paragraph("Belum ada produk Warga SMKN 24 yang sesuai dengan filter Anda.");
            empty.getElement().getStyle()
                .set("color", "#94A3B8")
                .set("font-size", "15px")
                .set("padding", "40px 0")
                .set("text-align", "center")
                .set("width", "100%");
            cardsGrid.add(empty);
            return;
        }

        filtered.forEach(p -> {
            String imgUrl = extractImgUrl(p.getImages(), "images/buku.jpeg");
            List<Review> reviews = moderationService.getProductReviews(p);
            String ratingStr = null;
            int reviewCount = reviews.size();
            if (reviewCount > 0) {
                double avg = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
                ratingStr = String.format("%.1f", avg);
            }

            cardsGrid.add(createProductCard(
                p.getId(), p.getName(), imgUrl, p.getPrice(), ratingStr, reviewCount, p.isSchoolMarket()
            ));
        });
    }

    private Component createProductCard(Long id, String name, String imgUrl,
                                        BigDecimal priceVal, String rating,
                                        int reviewCount, boolean isVerified) {
        Div card = new Div();
        card.addClassName("product-card");
        card.getElement().getStyle()
            .set("cursor", "pointer")
            .set("background", "#FFFFFF")
            .set("border-radius", "16px")
            .set("border", "1px solid #E2E8F0")
            .set("overflow", "hidden")
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("box-shadow", "0 2px 10px rgba(0,25,52,0.04)")
            .set("transition", "all 0.2s ease");

        String badgeHtml = isVerified
            ? "<div class='verified-badge' style='position:absolute;top:12px;left:12px;display:flex;align-items:center;gap:5px;background:#F5C45E;color:#001934;font-weight:700;font-size:11px;padding:4px 10px;border-radius:9999px;z-index:2;'>"
              + SVG_CHECK + "Warga SMKN 24</div>"
            : "";

        Div imgWrapper = new Div();
        imgWrapper.addClassName("product-img-wrapper");
        imgWrapper.getElement().getStyle()
            .set("position", "relative")
            .set("width", "100%")
            .set("height", "220px")
            .set("overflow", "hidden")
            .set("background", "#F1F5F9");
        imgWrapper.getElement().setProperty("innerHTML",
            "<img src='" + imgUrl + "' alt='" + name + "' class='product-img' style='width:100%;height:100%;object-fit:cover;'/>"
            + badgeHtml
        );

        Div infoArea = new Div();
        infoArea.addClassName("product-info-area");

        H4 title = new H4(name);
        title.addClassName("product-title");

        Div price = new Div(new Text("Rp " + String.format("%,.0f", priceVal)));
        price.addClassName("product-price");

        Div ratingRow = new Div();
        if (rating != null) {
            ratingRow.getElement().setProperty("innerHTML",
                "<div style='display:flex;align-items:center;gap:6px;margin-bottom:12px;'>"
                + "<span style='display:inline-flex;align-items:center;gap:2px;'>" + SVG_STAR_FILLED + "</span>"
                + "<span style='font-size:12px;font-weight:700;color:#F0BF5A;'>" + rating + "</span>"
                + "<span style='font-size:12px;color:#94A3B8;'>(" + reviewCount + ")</span>"
                + "</div>"
            );
        }

        Button btnDetail = new Button("Lihat Detail");
        btnDetail.setWidthFull();
        btnDetail.addClassName("btn-lihat-detail");
        btnDetail.addClickListener(e -> UI.getCurrent().navigate("product/" + id));

        card.addClickListener(e -> UI.getCurrent().navigate("product/" + id));
        
        infoArea.add(title, price);
        if (rating != null) {
            infoArea.add(ratingRow);
        }
        infoArea.add(btnDetail);

        card.add(imgWrapper, infoArea);
        return card;
    }

    private String extractImgUrl(String imagesJson, String fallback) {
        if (imagesJson == null || !imagesJson.contains("images/")) {
            return fallback;
        }
        return imagesJson.replace("[\"", "").replace("\"]", "").split("\",\"")[0].trim();
    }
}
