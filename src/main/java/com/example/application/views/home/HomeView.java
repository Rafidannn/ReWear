package com.example.application.views.home;

import com.example.application.model.product.Product;
import com.example.application.model.product.Category;
import com.example.application.service.product.ProductService;
import com.example.application.service.product.CategoryService;
import com.example.application.service.moderation.ModerationService;
import com.example.application.model.moderation.Review;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.AttachEvent;
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

    public HomeView(ProductService productService, CategoryService categoryService, ModerationService moderationService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.moderationService = moderationService;

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
        heroContainer.setId("hero-section");
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
        sectionWrapper.getElement().setAttribute("data-aos", "fade-up");

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
        List<Category> categories = categoryService.findAllSorted();
        StringBuilder sb = new StringBuilder("<div class='rw-cat-grid'>");
        
        for (Category cat : categories) {
            String slug = cat.getSlug() != null ? cat.getSlug().toLowerCase() : "";
            String color = "#6B7280"; // default
            String svgPath = "";
            
            switch (slug) {
                case "pakaian":
                    color = "#2563EB";
                    svgPath = "<path d='M20.38 3.46L16 2a4 4 0 01-8 0L3.62 3.46a2 2 0 00-1.34 2.23l.58 3.57a1 1 0 00.99.84H6v10c0 1.1.9 2 2 2h8a2 2 0 002-2V10h2.15a1 1 0 00.99-.84l.58-3.57a2 2 0 00-1.34-2.23z' stroke='currentColor' stroke-width='1.8' fill='none' stroke-linecap='round' stroke-linejoin='round'/>";
                    break;
                case "buku":
                    color = "#7C3AED";
                    svgPath = "<path d='M4 19.5A2.5 2.5 0 016.5 17H20' stroke='currentColor' stroke-width='1.8' stroke-linecap='round'/><path d='M6.5 2H20v20H6.5A2.5 2.5 0 014 19.5v-15A2.5 2.5 0 016.5 2z' stroke='currentColor' stroke-width='1.8' fill='none'/>";
                    break;
                case "elektronik":
                    color = "#0891B2";
                    svgPath = "<rect x='2' y='3' width='20' height='14' rx='2' stroke='currentColor' stroke-width='1.8' fill='none'/><path d='M8 21h8M12 17v4' stroke='currentColor' stroke-width='1.8' stroke-linecap='round'/>";
                    break;
                case "hobi":
                    color = "#D97706";
                    svgPath = "<circle cx='12' cy='12' r='10' stroke='currentColor' stroke-width='1.8' fill='none'/><polygon points='10 8 16 12 10 16 10 8' stroke='currentColor' stroke-width='1.8' fill='none' stroke-linejoin='round'/>";
                    break;
                case "peralatan":
                    color = "#16A34A";
                    svgPath = "<path d='M14.7 6.3a1 1 0 000 1.4l1.6 1.6a1 1 0 001.4 0l3.77-3.77a6 6 0 01-7.94 7.94l-6.91 6.91a2.12 2.12 0 01-3-3l6.91-6.91a6 6 0 017.94-7.94l-3.76 3.76z' stroke='currentColor' stroke-width='1.8' fill='none' stroke-linecap='round' stroke-linejoin='round'/>";
                    break;
                case "jasa":
                    color = "#EC4899";
                    svgPath = "<path d='M16 16v1a2 2 0 01-2 2H3a2 2 0 01-2-2V7a2 2 0 012-2h3m3 0V3a2 2 0 012-2h2a2 2 0 012 2v2m-6 0h6' stroke='currentColor' stroke-width='1.8' fill='none' stroke-linecap='round' stroke-linejoin='round'/><rect x='2' y='7' width='20' height='12' rx='2' stroke='currentColor' stroke-width='1.8' fill='none'/>";
                    break;
                case "lainnya":
                default:
                    color = "#6B7280";
                    svgPath = "<rect x='3' y='3' width='7' height='7' rx='1' stroke='currentColor' stroke-width='1.8' fill='none'/><rect x='14' y='3' width='7' height='7' rx='1' stroke='currentColor' stroke-width='1.8' fill='none'/><rect x='3' y='14' width='7' height='7' rx='1' stroke='currentColor' stroke-width='1.8' fill='none'/><rect x='14' y='14' width='7' height='7' rx='1' stroke='currentColor' stroke-width='1.8' fill='none'/>";
                    break;
            }
            
            sb.append("<div class='category-card' onclick=\"var el=document.getElementById('pasar-section');if(el)el.scrollIntoView({behavior:'smooth'});\">")
              .append("<div class='category-icon-circle' style='color:").append(color).append(";'>")
              .append("<svg width='28' height='28' viewBox='0 0 24 24' fill='none' xmlns='http://www.w3.org/2000/svg'>")
              .append(svgPath).append("</svg></div>")
              .append("<span class='category-title'>").append(cat.getName()).append("</span>")
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
        container.getElement().setAttribute("data-aos", "fade-up");

        Div innerContainer = new Div();
        innerContainer.addClassName("pasar-sekolah-inner");

        Div sectionHeader = new Div();
        sectionHeader.getElement().setProperty("innerHTML",
            "<div class='pasar-header'>" +
            "<div class='pasar-icon-wrapper'>" +
            "<svg width='22' height='22' viewBox='0 0 24 24' fill='none' stroke='#001934' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><path d='M22 10v6M2 10l10-5 10 5-10 5z'/><path d='M6 12v5c3 3 9 3 12 0v-5'/></svg>" +
            "</div>" +
            "<div><h2 class='pasar-title-main'>Pasar SMKN 24</h2>" +
            "<p class='pasar-title-sub'>Produk eksklusif dari warga sekolah terverifikasi</p></div>" +
            "</div>"
        );

        Div cardsGrid = new Div();
        cardsGrid.addClassName("products-grid-container");
        cardsGrid.setWidthFull();

        // Load seluruh produk aktif dari database (baik Warga SMKN 24 maupun Publik, urut terbaru)
        List<Product> allProducts = productService.findActiveWithCategory();
        allProducts.forEach(p -> {
            String imgUrl = extractImgUrl(p.getImages(), "images/buku.jpeg");
            
            // Ambil review secara dinamis dari database
            List<Review> reviews = moderationService.getProductReviews(p);
            String ratingStr = null;
            int reviewCount = reviews.size();
            if (reviewCount > 0) {
                double avg = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
                ratingStr = String.format("%.1f", avg);
            }
            
            cardsGrid.add(createProductCard(
                p.getId(), p.getName(), imgUrl,
                p.getPrice(), ratingStr, reviewCount, p.isSchoolMarket()
            ));
        });

        if (allProducts.isEmpty()) {
            Paragraph empty = new Paragraph("Belum ada produk tersedia.");
            empty.getElement().getStyle().set("color", "#94A3B8").set("padding", "24px 0");
            cardsGrid.add(empty);
        }

        innerContainer.add(sectionHeader, cardsGrid);
        container.add(innerContainer);
        return container;
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

    /* -----------------------------------------------------------------
       5. CTA BANNER SECTION (Rounded Box 24px - Figma Exact)
       ----------------------------------------------------------------- */
    private Component createCtaBannerSection() {
        Div wrapper = new Div();
        wrapper.setWidthFull();
        wrapper.addClassName("cta-banner-wrapper");
        wrapper.getElement().setAttribute("data-aos", "zoom-in");

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
        footer.getElement().setAttribute("data-aos", "fade-up");
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

    @Override
    protected void onAttach(AttachEvent event) {
        super.onAttach(event);
        UI.getCurrent().getPage().executeJs(
            "setTimeout(function() {\n" +
            "    if (typeof AOS !== 'undefined') {\n" +
            "        AOS.init({ duration: 800, once: true });\n" +
            "    } else {\n" +
            "        console.warn('AOS library not loaded. Revealing hidden sections.');\n" +
            "        var els = document.querySelectorAll('[data-aos]');\n" +
            "        for (var i = 0; i < els.length; i++) {\n" +
            "            els[i].removeAttribute('data-aos');\n" +
            "            els[i].style.opacity = '1';\n" +
            "            els[i].style.transform = 'none';\n" +
            "        }\n" +
            "    }\n" +
            "}, 350);"
        );
    }
}
