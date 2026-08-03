package com.example.application.views.home;

import com.example.application.model.product.Product;
import com.example.application.service.product.ProductService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.math.BigDecimal;
import java.util.List;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Marketplace Preloved Sekolah | ReWear SMKN 24")
public class HomeView extends VerticalLayout {

    private final ProductService productService;

    private static final String SVG_CHECK =
        "<svg width='11' height='11' viewBox='0 0 12 12' fill='none' xmlns='http://www.w3.org/2000/svg'>" +
        "<path d='M2 6L5 9L10 3' stroke='#001934' stroke-width='1.8' stroke-linecap='round' stroke-linejoin='round'/>" +
        "</svg>";

    private static final String SVG_STAR_FILLED =
        "<svg width='12' height='12' viewBox='0 0 24 24' fill='#F0BF5A' xmlns='http://www.w3.org/2000/svg'>" +
        "<polygon points='12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2'/>" +
        "</svg>";

    public HomeView(ProductService productService) {
        this.productService = productService;

        setSpacing(false);
        setPadding(false);
        setWidthFull();
        getElement().getStyle()
            .set("padding", "0")
            .set("margin", "0")
            .set("width", "100%")
            .set("max-width", "100%");

        add(
            createHeroSection(),
            createCategorySection(),
            createSchoolMarketSection(),
            createRecommendationSection(),
            createCtaBannerSection(),
            createFooter()
        );
    }

    /* -----------------------------------------------------------------
       1. HERO BANNER — Full-Width, 0px radius (Figma Exact)
       ----------------------------------------------------------------- */
    private Component createHeroSection() {
        Div heroContainer = new Div();
        heroContainer.addClassNames("hero-banner-section", "hero-banner");
        heroContainer.setId("hero-git add ." +
                "" +
                "" +
                "section");
        heroContainer.getElement().getStyle()
            .set("border-radius", "0")
            .set("margin", "0")
            .set("padding", "0")
            .set("width", "100%");

        Span badge = new Span("Sustainable Fashion");
        badge.addClassName("hero-badge");

        H1 title = new H1("Thrift Local, Grow Global\nat SMKN 24");
        title.addClassName("hero-title");
        title.getElement().getStyle().set("white-space", "pre-line");

        Paragraph desc = new Paragraph(
            "Temukan barang berkualitas dari komunitas sekolahmu. Lebih hemat, lebih hijau, dan mendukung ekonomi lokal.");
        desc.addClassName("hero-desc");

        Button btnBelanja = new Button("Mulai Belanja");
        btnBelanja.addClassName("btn-gold");
        btnBelanja.addClickListener(e ->
            UI.getCurrent().getPage().executeJs("var el = document.getElementById($0); if(el) el.scrollIntoView({behavior:'smooth'});", "pasar-section")
        );

        Button btnLearn = new Button("Pelajari Selengkapnya");
        btnLearn.addClassName("btn-outline-light");
        btnLearn.addClickListener(e ->
            UI.getCurrent().getPage().executeJs("var el = document.getElementById($0); if(el) el.scrollIntoView({behavior:'smooth'});", "category-section")
        );

        Div btnLayout = new Div(btnBelanja, btnLearn);
        btnLayout.addClassName("hero-btns");

        // Carousel Dot Indicators (Figma Exact)
        Div dots = new Div();
        dots.getElement().setProperty("innerHTML",
            "<div style='display:flex;gap:6px;margin-top:24px;'>" +
            "<span style='width:8px;height:8px;border-radius:50%;background:#FFDEA2;'></span>" +
            "<span style='width:8px;height:8px;border-radius:50%;background:rgba(255,255,255,0.3);'></span>" +
            "</div>"
        );

        Div innerContainer = new Div(badge, title, desc, btnLayout, dots);
        innerContainer.addClassName("hero-inner-container");

        heroContainer.add(innerContainer);
        return heroContainer;
    }

    /* -----------------------------------------------------------------
       2. JELAJAHI KATEGORI SECTION
       ----------------------------------------------------------------- */
    private Component createCategorySection() {
        Div sectionWrapper = new Div();
        sectionWrapper.setWidthFull();
        sectionWrapper.addClassName("category-section-wrapper");
        sectionWrapper.setId("category-section");

        Div innerContainer = new Div();
        innerContainer.addClassName("category-inner-container");

        Div headerRow = new Div();
        headerRow.getElement().setProperty("innerHTML",
            "<div class='rw-section-header'>" +
            "<div><h2 class='rw-section-title'>Jelajahi Kategori</h2>" +
            "<p class='rw-section-sub'>Temukan apa yang kamu cari dengan mudah</p></div>" +
            "<a href='#' class='rw-link-more' onclick=\"var el=document.getElementById('pasar-section');if(el)el.scrollIntoView({behavior:'smooth'});return false;\">Lihat Semua</a>" +
            "</div>"
        );

        Div catGrid = new Div();
        catGrid.getElement().setProperty("innerHTML", buildCategoryGridHtml());

        innerContainer.add(headerRow, catGrid);
        sectionWrapper.add(innerContainer);
        return sectionWrapper;
    }

    private String buildCategoryGridHtml() {
        String[][] cats = {
            {"Pakaian", "#2563EB",
              "<path d='M20.38 3.46L16 2a4 4 0 01-8 0L3.62 3.46a2 2 0 00-1.34 2.23l.58 3.57a1 1 0 00.99.84H6v10c0 1.1.9 2 2 2h8a2 2 0 002-2V10h2.15a1 1 0 00.99-.84l.58-3.57a2 2 0 00-1.34-2.23z' stroke='currentColor' stroke-width='1.8' fill='none' stroke-linecap='round' stroke-linejoin='round'/>"},
            {"Buku", "#7C3AED",
              "<path d='M4 19.5A2.5 2.5 0 016.5 17H20' stroke='currentColor' stroke-width='1.8' stroke-linecap='round'/><path d='M6.5 2H20v20H6.5A2.5 2.5 0 014 19.5v-15A2.5 2.5 0 016.5 2z' stroke='currentColor' stroke-width='1.8' fill='none'/>"},
            {"Elektronik", "#0891B2",
              "<rect x='2' y='3' width='20' height='14' rx='2' stroke='currentColor' stroke-width='1.8' fill='none'/><path d='M8 21h8M12 17v4' stroke='currentColor' stroke-width='1.8' stroke-linecap='round'/>"},
            {"Hobi", "#D97706",
              "<circle cx='12' cy='12' r='10' stroke='currentColor' stroke-width='1.8' fill='none'/><polygon points='10 8 16 12 10 16 10 8' stroke='currentColor' stroke-width='1.8' fill='none' stroke-linejoin='round'/>"},
            {"Peralatan", "#16A34A",
              "<path d='M14.7 6.3a1 1 0 000 1.4l1.6 1.6a1 1 0 001.4 0l3.77-3.77a6 6 0 01-7.94 7.94l-6.91 6.91a2.12 2.12 0 01-3-3l6.91-6.91a6 6 0 017.94-7.94l-3.76 3.76z' stroke='currentColor' stroke-width='1.8' fill='none' stroke-linecap='round' stroke-linejoin='round'/>"},
            {"Lainnya", "#6B7280",
              "<rect x='3' y='3' width='7' height='7' rx='1' stroke='currentColor' stroke-width='1.8' fill='none'/><rect x='14' y='3' width='7' height='7' rx='1' stroke='currentColor' stroke-width='1.8' fill='none'/><rect x='3' y='14' width='7' height='7' rx='1' stroke='currentColor' stroke-width='1.8' fill='none'/><rect x='14' y='14' width='7' height='7' rx='1' stroke='currentColor' stroke-width='1.8' fill='none'/>"}
        };

        StringBuilder sb = new StringBuilder("<div class='rw-cat-grid'>");
        for (String[] cat : cats) {
            sb.append("<div class='category-card' onclick=\"var el=document.getElementById('pasar-section');if(el)el.scrollIntoView({behavior:'smooth'});\">")
              .append("<div class='category-icon-circle' style='color:").append(cat[1]).append(";'>")
              .append("<svg width='28' height='28' viewBox='0 0 24 24' fill='none' xmlns='http://www.w3.org/2000/svg'>")
              .append(cat[2]).append("</svg></div>")
              .append("<span class='category-title'>").append(cat[0]).append("</span>")
              .append("</div>");
        }
        sb.append("</div>");
        return sb.toString();
    }

    /* -----------------------------------------------------------------
       3. PASAR SMKN 24 SECTION (Background #EFF4FF - Figma Exact)
       ----------------------------------------------------------------- */
    private Component createSchoolMarketSection() {
        Div container = new Div();
        container.setWidthFull();
        container.addClassName("pasar-sekolah-container");
        container.setId("pasar-section");

        Div innerContainer = new Div();
        innerContainer.addClassName("pasar-sekolah-inner");

        Div sectionHeader = new Div();
        sectionHeader.getElement().setProperty("innerHTML",
            "<div class='rw-section-header'>" +
            "<div style='display:flex;align-items:center;gap:12px;'>" +
            "<div style='padding:8px;border-radius:10px;background:#F5C45E;display:flex;'>" +
            "<svg width='20' height='20' viewBox='0 0 24 24' fill='none'><path d='M22 10v1a10 10 0 11-5.93-9.14' stroke='#001934' stroke-width='1.8' stroke-linecap='round'/><polyline points='22 4 12 14.01 9 11.01' stroke='#001934' stroke-width='1.8' stroke-linecap='round' stroke-linejoin='round'/></svg>" +
            "</div>" +
            "<div><h2 class='rw-section-title' style='margin:0;'>Pasar SMKN 24</h2>" +
            "<p class='rw-section-sub' style='margin:0;'>Produk eksklusif dari warga sekolah terverifikasi</p></div>" +
            "</div>" +
            "</div>"
        );

        Div cardsGrid = new Div();
        cardsGrid.addClassName("products-grid-container");

        // Load produk dari database — pakai JOIN FETCH agar category tidak lazy
        List<Product> schoolProducts = productService.findSchoolMarketWithCategory();
        if (schoolProducts.isEmpty()) {
            schoolProducts = productService.findActiveWithCategory();
        }
        schoolProducts.stream().limit(4).forEach(p -> {
            String imgUrl = extractImgUrl(p.getImages(), "images/buku.jpeg");
            cardsGrid.add(createProductCard(
                p.getId(), p.getName(), imgUrl,
                p.getPrice(), "4.8", p.getSoldCount(), p.isSchoolMarket()
            ));
        });

        if (schoolProducts.isEmpty()) {
            Paragraph empty = new Paragraph("Belum ada produk tersedia.");
            empty.getElement().getStyle().set("color", "#94A3B8").set("padding", "24px 0");
            cardsGrid.add(empty);
        }

        innerContainer.add(sectionHeader, cardsGrid);
        container.add(innerContainer);
        return container;
    }

    /* -----------------------------------------------------------------
       4. REKOMENDASI UNTUK KAMU (5-Column Grid - Figma Exact)
       ----------------------------------------------------------------- */
    private Component createRecommendationSection() {
        Div wrapper = new Div();
        wrapper.setWidthFull();
        wrapper.addClassName("recommend-section-wrapper");
        wrapper.setId("recommend-section");

        Div innerContainer = new Div();
        innerContainer.addClassName("recommend-inner-container");

        Div sectionHeader = new Div();
        sectionHeader.getElement().setProperty("innerHTML",
            "<div class='rw-section-header'>" +
            "<div><h2 class='rw-section-title' style='margin:0;'>Rekomendasi Untuk Kamu</h2></div>" +
            "<div style='display:flex;gap:8px;'>" +
            "<button style='width:32px;height:32px;border-radius:50%;border:1px solid #CBD5E1;background:#FFFFFF;color:#001934;cursor:pointer;display:flex;align-items:center;justify-content:center;font-weight:700;'>‹</button>" +
            "<button style='width:32px;height:32px;border-radius:50%;border:1px solid #CBD5E1;background:#FFFFFF;color:#001934;cursor:pointer;display:flex;align-items:center;justify-content:center;font-weight:700;'>›</button>" +
            "</div>" +
            "</div>"
        );

        Div cardsGrid = new Div();
        cardsGrid.addClassName("recommend-grid-5");

        // Load rekomendasi dari database — JOIN FETCH agar tidak LazyInit
        List<Product> activeProducts = productService.findActiveWithCategory();
        activeProducts.stream().limit(5).forEach(p -> {
            String imgUrl = extractImgUrl(p.getImages(), "images/buku.jpeg");
            String catName = (p.getCategory() != null) ? p.getCategory().getName() : "Lainnya";
            String badge = (p.getConditionType() != null &&
                p.getConditionType().name().equalsIgnoreCase("BARU")) ? "BARU" : null;
            cardsGrid.add(createCompactCard(
                p.getId(), catName, p.getName(), p.getPrice(), imgUrl, badge
            ));
        });

        if (activeProducts.isEmpty()) {
            Paragraph empty = new Paragraph("Belum ada rekomendasi tersedia.");
            empty.getElement().getStyle().set("color", "#94A3B8").set("padding", "24px 0");
            cardsGrid.add(empty);
        }

        innerContainer.add(sectionHeader, cardsGrid);
        wrapper.add(innerContainer);
        return wrapper;
    }

    private Component createCompactCard(Long id, String categoryTag, String name, BigDecimal priceVal, String imgUrl, String badgeTag) {
        Div card = new Div();
        card.addClassName("recommend-card");
        card.getElement().getStyle().set("cursor", "pointer");

        String badgeHtml = badgeTag != null
            ? "<div style='position:absolute;top:8px;right:8px;background:#EF4444;color:#FFF;font-size:9px;font-weight:800;padding:2px 6px;border-radius:4px;'>" + badgeTag + "</div>"
            : "";

        Div imgWrapper = new Div();
        imgWrapper.getElement().getStyle().set("position", "relative").set("width", "100%").set("height", "170px").set("overflow", "hidden").set("background", "#F8FAFC");
        imgWrapper.getElement().setProperty("innerHTML",
            "<img src='" + imgUrl + "' alt='" + name + "' style='width:100%;height:100%;object-fit:cover;'/>" + badgeHtml
        );

        Div info = new Div();
        info.getElement().getStyle().set("padding", "12px").set("display", "flex").set("flex-direction", "column").set("gap", "4px");

        Span cat = new Span(categoryTag);
        cat.getElement().getStyle().set("font-size", "11px").set("color", "#74777F").set("font-weight", "500");

        Span title = new Span(name);
        title.getElement().getStyle().set("font-size", "13px").set("font-weight", "600").set("color", "#0B1C30").set("line-height", "1.4")
            .set("display", "-webkit-box").set("-webkit-line-clamp", "2").set("-webkit-box-orient", "vertical").set("overflow", "hidden");

        Span price = new Span("Rp " + String.format("%,.0f", priceVal));
        price.getElement().getStyle().set("font-size", "14px").set("font-weight", "700").set("color", "#001934").set("margin-top", "4px");

        info.add(cat, title, price);
        card.add(imgWrapper, info);

        card.addClickListener(e -> UI.getCurrent().navigate("product/" + id));
        return card;
    }

    /* -----------------------------------------------------------------
       HELPER: Product Card Generator (Pasar SMKN 24)
       ----------------------------------------------------------------- */
    private Component createProductCard(Long id, String name, String imgUrl,
                                        BigDecimal priceVal, String rating,
                                        int reviewCount, boolean isVerified) {
        Div card = new Div();
        card.addClassName("product-card");
        card.getElement().getStyle().set("cursor", "pointer");

        String badgeHtml = isVerified
            ? "<div class='verified-badge' style='position:absolute;top:12px;left:12px;display:flex;align-items:center;gap:5px;background:#F5C45E;color:#001934;font-weight:700;font-size:11px;padding:4px 10px;border-radius:9999px;z-index:2;'>"
              + SVG_CHECK + "Warga SMKN 24</div>"
            : "";

        Div imgWrapper = new Div();
        imgWrapper.addClassName("product-img-wrapper");
        imgWrapper.getElement().setProperty("innerHTML",
            "<img src='" + imgUrl + "' alt='" + name + "' class='product-img' style='width:100%;height:100%;object-fit:cover;'/>"
            + badgeHtml
        );

        H4 title = new H4(name);
        title.addClassName("product-title");

        Div price = new Div(new Text("Rp " + String.format("%,.0f", priceVal)));
        price.addClassName("product-price");

        Div ratingRow = new Div();
        ratingRow.getElement().setProperty("innerHTML",
            "<div style='display:flex;align-items:center;gap:6px;margin-bottom:12px;'>"
            + "<span style='display:inline-flex;align-items:center;gap:2px;'>" + SVG_STAR_FILLED + "</span>"
            + "<span style='font-size:12px;font-weight:700;color:#F0BF5A;'>" + rating + "</span>"
            + "<span style='font-size:12px;color:#94A3B8;'>(" + reviewCount + ")</span>"
            + "</div>"
        );

        Button btnDetail = new Button("Lihat Detail");
        btnDetail.setWidthFull();
        btnDetail.addClassName("btn-lihat-detail");
        btnDetail.addClickListener(e -> UI.getCurrent().navigate("product/" + id));

        card.addClickListener(e -> UI.getCurrent().navigate("product/" + id));
        card.add(imgWrapper, title, price, ratingRow, btnDetail);
        return card;
    }

    /* -----------------------------------------------------------------
       5. CTA BANNER SECTION (Rounded Box 24px - Figma Exact)
       ----------------------------------------------------------------- */
    private Component createCtaBannerSection() {
        Div wrapper = new Div();
        wrapper.setWidthFull();
        wrapper.addClassName("cta-banner-wrapper");

        Div innerContainer = new Div();
        innerContainer.addClassName("cta-banner-inner");

        Div ctaContainer = new Div();
        ctaContainer.addClassName("cta-banner");
        ctaContainer.getElement().setProperty("innerHTML",
            "<div style='display:flex;align-items:center;justify-content:space-between;flex-wrap:wrap;gap:24px;position:relative;z-index:1;'>" +
            "<div>" +
            "<h2 style='font-size:26px;font-weight:800;color:#FFFFFF;margin:0 0 10px 0;'>Siap untuk Berkontribusi?</h2>" +
            "<p style='color:#94A3B8;max-width:520px;margin:0;line-height:1.6;font-size:15px;'>Mulai jual barang yang tidak terpakai atau temukan harta karun baru hari ini. Bergabunglah dengan ribuan warga SMKN 24 lainnya.</p>" +
            "</div>" +
            "<div style='display:flex;gap:12px;flex-wrap:wrap;'>" +
            "<button id='btn-cta-jual' style='background:#FFFFFF;color:#0F172A;font-weight:700;border:none;border-radius:10px;padding:14px 28px;font-size:15px;cursor:pointer;font-family:Inter,sans-serif;transition:all 0.2s;'>Mulai Berjualan</button>" +
            "<button id='btn-cta-daftar' style='background:#FFDEA2;color:#261900;font-weight:700;border:none;border-radius:10px;padding:14px 28px;font-size:15px;cursor:pointer;font-family:Inter,sans-serif;transition:all 0.2s;'>Buat Akun</button>" +
            "</div></div>"
        );

        ctaContainer.addAttachListener(event -> {
            UI.getCurrent().getPage().executeJs(
                "var b1 = document.getElementById('btn-cta-jual');" +
                "if(b1) b1.onclick = function(){ window.location.href='/seller'; };" +
                "var b2 = document.getElementById('btn-cta-daftar');" +
                "if(b2) b2.onclick = function(){ alert('Halaman pendaftaran akan segera tersedia!'); };"
            );
        });

        innerContainer.add(ctaContainer);
        wrapper.add(innerContainer);
        return wrapper;
    }

    /* -----------------------------------------------------------------
       6. FOOTER (Light Blue #EFF4FF, 4 Columns + Bottom Bar - Figma Exact)
       ----------------------------------------------------------------- */
    private Component createFooter() {
        Div footer = new Div();
        footer.setWidthFull();
        footer.getElement().setProperty("innerHTML",
            "<footer class='rw-footer'>" +
            "<div class='rw-footer-inner'>" +

            // Col 1 — Brand
            "<div class='rw-footer-col'>" +
            "<span class='rw-footer-brand'>ReWear</span>" +
            "<p class='rw-footer-desc'>Platform marketplace komunitas SMKN 24 Jakarta. Mendorong ekonomi sirkular dan keberlanjutan di lingkungan sekolah.</p>" +
            "<div class='rw-footer-socials'>" +
            "<a href='#' class='rw-social-btn' title='Web'><svg width='16' height='16' viewBox='0 0 24 24' fill='none'><circle cx='12' cy='12' r='10' stroke='currentColor' stroke-width='1.8'/><path d='M2 12h20M12 2a15.3 15.3 0 014 10 15.3 15.3 0 01-4 10 15.3 15.3 0 01-4-10 15.3 15.3 0 014-10z' stroke='currentColor' stroke-width='1.8'/></svg></a>" +
            "<a href='#' class='rw-social-btn' title='Email'><svg width='16' height='16' viewBox='0 0 24 24' fill='none'><path d='M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z' stroke='currentColor' stroke-width='1.8'/><polyline points='22,6 12,13 2,6' stroke='currentColor' stroke-width='1.8'/></svg></a>" +
            "<a href='#' class='rw-social-btn' title='Kontak'><svg width='16' height='16' viewBox='0 0 24 24' fill='none'><path d='M22 16.92v3a2 2 0 01-2.18 2 19.79 19.79 0 01-8.63-3.07 19.5 19.5 0 01-6-6 19.79 19.79 0 01-3.07-8.67A2 2 0 014.11 2h3a2 2 0 012 1.72 12.84 12.84 0 00.7 2.81 2 2 0 01-.45 2.11L8.09 9.91a16 16 0 006 6l1.27-1.27a2 2 0 012.11-.45 12.84 12.84 0 002.81.7A2 2 0 0122 16.92z' stroke='currentColor' stroke-width='1.8'/></svg></a>" +
            "</div></div>" +

            // Col 2 — Belanja
            "<div class='rw-footer-col'>" +
            "<h4 class='rw-footer-heading'>Belanja</h4>" +
            "<ul class='rw-footer-links'>" +
            "<li><a href='#'>Semua Kategori</a></li>" +
            "<li><a href='#'>Pasar SMKN 24</a></li>" +
            "<li><a href='#'>Promo Hari Ini</a></li>" +
            "<li><a href='#'>Barang Thrift</a></li>" +
            "</ul></div>" +

            // Col 3 — Dukungan
            "<div class='rw-footer-col'>" +
            "<h4 class='rw-footer-heading'>Dukungan</h4>" +
            "<ul class='rw-footer-links'>" +
            "<li><a href='#'>Pusat Bantuan</a></li>" +
            "<li><a href='#'>Cara Berjualan</a></li>" +
            "<li><a href='#'>Kebijakan Privasi</a></li>" +
            "<li><a href='#'>Syarat &amp; Ketentuan</a></li>" +
            "</ul></div>" +

            // Col 4 — Tentang Kami
            "<div class='rw-footer-col'>" +
            "<h4 class='rw-footer-heading'>Tentang Kami</h4>" +
            "<ul class='rw-footer-links'>" +
            "<li><a href='#'>Tentang ReWear</a></li>" +
            "<li><a href='#'>Komunitas Kami</a></li>" +
            "<li><a href='#'>Blog</a></li>" +
            "<li><a href='#'>Kontak</a></li>" +
            "</ul></div>" +

            "</div>" +

            // Footer Bottom Bar
            "<div class='rw-footer-bottom-bar'>" +
            "<div class='rw-footer-bottom-inner'>" +
            "<span>&copy; 2024 ReWear SMKN 24 Jakarta. Sustainable Community Commerce.</span>" +
            "<span style='display:flex;align-items:center;gap:6px;'><svg width='14' height='14' viewBox='0 0 24 24' fill='none'><circle cx='12' cy='12' r='10' stroke='currentColor' stroke-width='1.8'/><path d='M2 12h20M12 2a15.3 15.3 0 014 10 15.3 15.3 0 01-4 10 15.3 15.3 0 01-4-10 15.3 15.3 0 014-10z' stroke='currentColor' stroke-width='1.8'/></svg> Bahasa Indonesia</span>" +
            "</div></div>" +

            "</footer>"
        );
        return footer;
    }

    private String extractImgUrl(String imagesJson, String fallback) {
        if (imagesJson == null || !imagesJson.contains("images/")) return fallback;
        return imagesJson.replace("[\"", "").replace("\"]", "").trim();
    }
}
