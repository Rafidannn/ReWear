package com.example.application.views.product;

import com.example.application.model.product.Product;
import com.example.application.service.product.ProductService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;


import java.util.List;
import java.util.Optional;

@Route(value = "product", layout = MainLayout.class)
@PageTitle("Detail Produk | ReWear SMKN 24")
public class ProductDetailView extends VerticalLayout implements HasUrlParameter<Long> {

    private final ProductService productService;
    private final Div contentArea = new Div();

    public ProductDetailView(ProductService productService) {
        this.productService = productService;

        setSpacing(false);
        setPadding(false);
        setWidthFull();
        getElement().getStyle()
            .set("padding", "0")
            .set("margin", "0")
            .set("width", "100%")
            .set("background-color", "#F8F9FF");

        contentArea.setWidthFull();
        add(contentArea);
    }

    @Override
    public void setParameter(BeforeEvent event, Long productId) {
        contentArea.removeAll();

        Optional<Product> productOpt = productService.findById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            contentArea.add(buildProductDetailUI(product));
        } else {
            contentArea.add(buildFallbackUI(productId));
        }
    }

    private Component buildProductDetailUI(Product product) {
        Div containerWrapper = new Div();
        containerWrapper.addClassName("product-detail-page-container");
        containerWrapper.getElement().getStyle()
            .set("max-width", "1280px")
            .set("margin", "0 auto")
            .set("padding", "24px 48px 64px 48px")
            .set("box-sizing", "border-box");

        // ---- 1. Breadcrumbs ----
        Div breadcrumb = new Div();
        breadcrumb.addClassName("pd-breadcrumb");
        breadcrumb.getElement().getStyle()
            .set("font-size", "13px")
            .set("color", "#64748B")
            .set("margin-bottom", "24px");

        Anchor b1 = new Anchor("", "Beranda");
        b1.getElement().getStyle().set("color", "#64748B").set("text-decoration", "none");
        Span s1 = new Span(" › ");
        Anchor b2 = new Anchor("#", getCategoryName(product));
        b2.getElement().getStyle().set("color", "#64748B").set("text-decoration", "none");
        Span s2 = new Span(" › ");
        Span currentName = new Span(product.getName());
        currentName.getElement().getStyle().set("color", "#001934").set("font-weight", "600");

        breadcrumb.add(b1, s1, b2, s2, currentName);

        // ---- 2. Main Grid (Left Gallery 48% | Right Actions 52%) ----
        HorizontalLayout mainGrid = new HorizontalLayout();
        mainGrid.setWidthFull();
        mainGrid.setSpacing(true);
        mainGrid.setAlignItems(FlexComponent.Alignment.START);

        // --- LEFT GALLERY ---
        HorizontalLayout leftGallery = new HorizontalLayout();
        leftGallery.setWidth("48%");
        leftGallery.setSpacing(true);

        // Thumbnails (Vertical Column)
        VerticalLayout thumbsCol = new VerticalLayout();
        // Parse image list
        String imagesJson = product.getImages();
        String mainImgUrl = extractImgUrl(imagesJson, "images/buku.jpeg");

        // Only add thumbnail gallery sidebar if there are multiple images
        if (imagesJson != null && imagesJson.contains("\",\"")) {
            String[] imgList = imagesJson.replace("[\"", "").replace("\"]", "").split("\",\"");
            if (imgList.length > 1) {
                for (int i = 0; i < imgList.length && i < 4; i++) {
                    String url = imgList[i].trim();
                    Div thumb = new Div();
                    thumb.getElement().getStyle()
                        .set("width", "72px")
                        .set("height", "72px")
                        .set("border-radius", "8px")
                        .set("overflow", "hidden")
                        .set("border", i == 0 ? "2px solid #001934" : "1px solid #E2E8F0")
                        .set("cursor", "pointer");
                    Image tImg = new Image(url, "Thumb");
                    tImg.getElement().getStyle().set("width", "100%").set("height", "100%").set("object-fit", "cover");
                    thumb.add(tImg);
                    thumbsCol.add(thumb);
                }
                leftGallery.add(thumbsCol);
            }
        }

        // Main Image Display
        Div mainImgBox = new Div();
        mainImgBox.getElement().getStyle()
            .set("position", "relative")
            .set("flex", "1")
            .set("height", "440px")
            .set("border-radius", "16px")
            .set("overflow", "hidden")
            .set("background", "#FFFFFF")
            .set("border", "1px solid #E2E8F0");

        Image heroImg = new Image(mainImgUrl, product.getName());
        heroImg.getElement().getStyle().set("width", "100%").set("height", "100%").set("object-fit", "cover");
        mainImgBox.add(heroImg);

        // Badge Warga SMKN 24 (Top Left)
        Div verBadge = new Div();
        verBadge.getElement().setProperty("innerHTML",
            "<div style='display:flex;align-items:center;gap:6px;background:#F5C45E;color:#001934;" +
            "font-weight:700;padding:6px 14px;border-radius:9999px;font-size:12px;position:absolute;top:16px;left:16px;z-index:2;'>" +
            "<svg width='12' height='12' viewBox='0 0 12 12' fill='none' xmlns='http://www.w3.org/2000/svg'>" +
            "<path d='M2 6L5 9L10 3' stroke='#001934' stroke-width='1.8' stroke-linecap='round' stroke-linejoin='round'/>" +
            "</svg>Warga SMKN 24</div>");
        mainImgBox.add(verBadge);

        leftGallery.add(mainImgBox);

        // --- RIGHT PRODUCT DETAILS & ACTIONS ---
        Div rightCol = new Div();
        rightCol.setWidth("52%");
        rightCol.getElement().getStyle()
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("gap", "16px")
            .set("padding-left", "16px");

        // Title
        H1 productTitle = new H1(product.getName());
        productTitle.getElement().getStyle()
            .set("font-size", "26px")
            .set("font-weight", "800")
            .set("color", "#0F172A")
            .set("line-height", "1.3")
            .set("margin", "0");

        // Price + Condition Chip Row
        HorizontalLayout priceRow = new HorizontalLayout();
        priceRow.setAlignItems(FlexComponent.Alignment.CENTER);
        priceRow.setSpacing(true);

        Span priceVal = new Span("Rp " + String.format("%,.0f", product.getPrice()));
        priceVal.getElement().getStyle().set("font-size", "30px").set("font-weight", "800").set("color", "#0F172A");

        Span condChip = new Span("KONDISI: LIKE NEW");
        condChip.getElement().getStyle()
            .set("background", "#FFDEA2")
            .set("color", "#261900")
            .set("font-weight", "700")
            .set("font-size", "11px")
            .set("padding", "4px 10px")
            .set("border-radius", "6px")
            .set("letter-spacing", "0.5px");

        priceRow.add(priceVal, condChip);

        // Rating & Sales Count
        Div ratingRow = new Div();
        ratingRow.getElement().setProperty("innerHTML",
            "<div style='display:flex;align-items:center;gap:8px;'>" +
            "<span style='display:inline-flex;align-items:center;gap:2px;'>" +
            "<svg width='13' height='13' viewBox='0 0 24 24' fill='#F0BF5A' xmlns='http://www.w3.org/2000/svg'><polygon points='12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2'/></svg>" +
            "<svg width='13' height='13' viewBox='0 0 24 24' fill='#F0BF5A' xmlns='http://www.w3.org/2000/svg'><polygon points='12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2'/></svg>" +
            "<svg width='13' height='13' viewBox='0 0 24 24' fill='#F0BF5A' xmlns='http://www.w3.org/2000/svg'><polygon points='12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2'/></svg>" +
            "<svg width='13' height='13' viewBox='0 0 24 24' fill='#F0BF5A' xmlns='http://www.w3.org/2000/svg'><polygon points='12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2'/></svg>" +
            "<svg width='13' height='13' viewBox='0 0 24 24' fill='none' xmlns='http://www.w3.org/2000/svg'><polygon points='12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2' stroke='#F0BF5A' stroke-width='1.8'/></svg>" +
            "</span>" +
            "<span style='color:#64748B;font-size:13px;'>(42 Ulasan)</span>" +
            "<span style='color:#CBD5E1;'>•</span>" +
            "<span style='color:#64748B;font-size:13px;'>Terjual 12</span>" +
            "</div>");

        // Seller Card (Koperasi Siswa 24)
        Div sellerCard = new Div();
        sellerCard.getElement().getStyle()
            .set("background", "#EFF4FF")
            .set("border-radius", "12px")
            .set("padding", "16px")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "space-between");

        HorizontalLayout sellerLeft = new HorizontalLayout();
        sellerLeft.setAlignItems(FlexComponent.Alignment.CENTER);
        sellerLeft.setSpacing(true);

        Image sellerAvatar = new Image("images/buku.jpeg", "Seller");
        sellerAvatar.getElement().getStyle()
            .set("width", "44px").set("height", "44px").set("border-radius", "9999px").set("object-fit", "cover");

        Div sellerMeta = new Div();
        HorizontalLayout sellerNameRow = new HorizontalLayout();
        sellerNameRow.setAlignItems(FlexComponent.Alignment.CENTER);
        Span sName = new Span(getSellerName(product));
        sName.getElement().getStyle().set("font-weight", "700").set("color", "#0F172A").set("font-size", "14px");
        Span offBadge = new Span("OFFICIAL");
        offBadge.getElement().getStyle()
            .set("background", "#FFDEA2").set("color", "#261900").set("font-size", "9px")
            .set("font-weight", "800").set("padding", "2px 6px").set("border-radius", "4px");
        sellerNameRow.add(sName, offBadge);

        Span sActive = new Span("Aktif 5 menit yang lalu");
        sActive.getElement().getStyle().set("font-size", "12px").set("color", "#64748B").set("display", "block");
        sellerMeta.add(sellerNameRow, sActive);
        sellerLeft.add(sellerAvatar, sellerMeta);
        if (product.getSeller() != null) {
            Long sellerId = product.getSeller().getId();
            sellerLeft.getElement().getStyle().set("cursor", "pointer");
            sellerLeft.addClickListener(e -> UI.getCurrent().navigate("profile/" + sellerId));
        }

        Button btnChatSeller = new Button("Chat Penjual");
        btnChatSeller.setIcon(VaadinIcon.COMMENT.create());
        btnChatSeller.getElement().getStyle()
            .set("background", "#FF9E59")
            .set("color", "#FFFFFF")
            .set("font-weight", "700")
            .set("border", "none")
            .set("border-radius", "8px")
            .set("padding", "10px 18px")
            .set("cursor", "pointer");
        btnChatSeller.addClickListener(e -> {
            String sellerNameStr = getSellerName(product);
            if (sellerNameStr.isEmpty()) sellerNameStr = "Rafidan Athariz";
            String pName = product.getName();
            String priceStr = "Rp " + String.format("%,.0f", product.getPrice());
            String imgUrl = extractImgUrl(product.getImages(), "images/buku.jpeg");
            UI.getCurrent().navigate("chat?seller=" + sellerNameStr + "&product=" + pName + "&price=" + priceStr + "&img=" + imgUrl);
        });

        sellerCard.add(sellerLeft, btnChatSeller);

        // Size Selector Options
        Div sizeBox = new Div();
        Span sizeLabel = new Span("Pilih Ukuran:");
        sizeLabel.getElement().getStyle().set("font-size", "13px").set("font-weight", "700").set("color", "#0F172A").set("display", "block").set("margin-bottom", "8px");

        HorizontalLayout sizePills = new HorizontalLayout();
        sizePills.setSpacing(true);
        String[] sizes = {"S", "M", "L", "XL"};
        for (String sz : sizes) {
            Span pill = new Span(sz);
            boolean isM = sz.equals("M");
            pill.getElement().getStyle()
                .set("padding", "8px 16px")
                .set("border-radius", "8px")
                .set("font-weight", "700")
                .set("font-size", "13px")
                .set("cursor", "pointer")
                .set("background", isM ? "#001934" : "#FFFFFF")
                .set("color", isM ? "#FFFFFF" : "#0F172A")
                .set("border", isM ? "2px solid #001934" : "1px solid #CBD5E1");
            sizePills.add(pill);
        }
        sizeBox.add(sizeLabel, sizePills);

        // Action Buttons Row 1: Keranjang & Wishlist
        HorizontalLayout secondaryBtns = new HorizontalLayout();
        secondaryBtns.setWidthFull();
        secondaryBtns.setSpacing(true);

        Button btnCart = new Button("Keranjang", VaadinIcon.CART.create());
        btnCart.setWidth("50%");
        btnCart.getElement().getStyle()
            .set("border", "2px solid #001934").set("color", "#001934").set("background", "#FFFFFF")
            .set("border-radius", "8px").set("font-weight", "700").set("padding", "12px").set("cursor", "pointer");
        btnCart.addClickListener(e -> Notification.show("Ditambahkan ke Keranjang!"));

        Button btnWish = new Button("Wishlist", VaadinIcon.HEART_O.create());
        btnWish.setWidth("50%");
        btnWish.getElement().getStyle()
            .set("border", "2px solid #001934").set("color", "#001934").set("background", "#FFFFFF")
            .set("border-radius", "8px").set("font-weight", "700").set("padding", "12px").set("cursor", "pointer");
        btnWish.addClickListener(e -> Notification.show("Disimpan ke Wishlist!"));

        secondaryBtns.add(btnCart, btnWish);

        // Action Button Row 2: Beli Sekarang (Full Width Dark Navy)
        Div btnBuyNow = new Div();
        btnBuyNow.getElement().setProperty("innerHTML",
            "<button onclick=\"this.style.opacity='0.85'\" style='width:100%;background:#001934;color:#FFFFFF;" +
            "font-weight:800;font-size:16px;border-radius:8px;padding:16px;border:none;cursor:pointer;" +
            "display:flex;align-items:center;justify-content:center;gap:10px;font-family:Inter,sans-serif;'>" +
            "<svg width='20' height='20' viewBox='0 0 24 24' fill='none' xmlns='http://www.w3.org/2000/svg'>" +
            "<path d='M6 2L3 6v14a2 2 0 002 2h14a2 2 0 002-2V6l-3-4z' stroke='#FFF' stroke-width='1.8' stroke-linecap='round' stroke-linejoin='round'/>" +
            "<line x1='3' y1='6' x2='21' y2='6' stroke='#FFF' stroke-width='1.8' stroke-linecap='round'/>" +
            "<path d='M16 10a4 4 0 01-8 0' stroke='#FFF' stroke-width='1.8' stroke-linecap='round' stroke-linejoin='round'/>" +
            "</svg>Beli Sekarang</button>");
        btnBuyNow.addClickListener(e -> Notification.show("Melanjutkan ke Pembelian COD SMKN 24..."));

        // Guarantee Badges Row
        Div guaranteeRow = new Div();
        guaranteeRow.getElement().setProperty("innerHTML",
            "<div style='display:flex;align-items:center;justify-content:center;gap:16px;margin-top:4px;'>" +
            "<span style='display:flex;align-items:center;gap:5px;font-size:12px;color:#64748B;font-weight:600;'>" +
            "<svg width='14' height='14' viewBox='0 0 24 24' fill='none' xmlns='http://www.w3.org/2000/svg'>" +
            "<path d='M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z' stroke='#64748B' stroke-width='1.8' stroke-linecap='round' stroke-linejoin='round'/>" +
            "</svg>Escrow Protected</span>" +
            "<span style='color:#CBD5E1'>•</span>" +
            "<span style='display:flex;align-items:center;gap:5px;font-size:12px;color:#64748B;font-weight:600;'>" +
            "<svg width='14' height='14' viewBox='0 0 24 24' fill='none' xmlns='http://www.w3.org/2000/svg'>" +
            "<rect x='1' y='3' width='15' height='13' rx='2' stroke='#64748B' stroke-width='1.8'/>" +
            "<path d='M16 8h4l3 5v3h-7V8z' stroke='#64748B' stroke-width='1.8' stroke-linejoin='round'/>" +
            "<circle cx='5.5' cy='18.5' r='2.5' stroke='#64748B' stroke-width='1.8'/>" +
            "<circle cx='18.5' cy='18.5' r='2.5' stroke='#64748B' stroke-width='1.8'/>" +
            "</svg>COD SMKN 24</span></div>");

        rightCol.add(productTitle, priceRow, ratingRow, sellerCard);
        
        // Show Size selector ONLY for Pakaian category
        boolean isPakaian = product.getCategory() != null && "pakaian".equalsIgnoreCase(product.getCategory().getSlug());
        if (isPakaian) {
            rightCol.add(sizeBox);
        }

        rightCol.add(secondaryBtns, btnBuyNow, guaranteeRow);

        mainGrid.add(leftGallery, rightCol);

        // ---- 3. Tabs Section (Deskripsi, Spesifikasi, Ulasan Pembeli) ----
        Div tabsSection = new Div();
        tabsSection.getElement().getStyle().set("margin-top", "48px");

        HorizontalLayout tabHeader = new HorizontalLayout();
        tabHeader.getElement().getStyle().set("border-bottom", "2px solid #E2E8F0").set("margin-bottom", "24px");
        tabHeader.setSpacing(true);

        Span tDesc = new Span("Deskripsi");
        tDesc.getElement().getStyle().set("font-size", "16px").set("font-weight", "700").set("color", "#001934").set("border-bottom", "3px solid #001934").set("padding-bottom", "8px").set("cursor", "pointer");
        Span tReview = new Span("Ulasan Pembeli");
        tReview.getElement().getStyle().set("font-size", "16px").set("font-weight", "600").set("color", "#64748B").set("padding-bottom", "8px").set("cursor", "pointer");

        tabHeader.add(tDesc, tReview);

        // Tab Content Grid (Left Text 60% | Right Blue Box "Kenapa Beli Ini?" 40%)
        HorizontalLayout tabContentGrid = new HorizontalLayout();
        tabContentGrid.setWidthFull();
        tabContentGrid.setSpacing(true);

        Div descTextCol = new Div();
        descTextCol.setWidth("62%");
        if (product.getDescription() != null && !product.getDescription().isBlank()) {
            Paragraph pDesc = new Paragraph(product.getDescription());
            pDesc.getElement().getStyle().set("color", "#475569").set("line-height", "1.7").set("font-size", "14px").set("margin-bottom", "16px");
            descTextCol.add(pDesc);
        }

        // Right Blue Box "Kenapa Beli Ini?"
        Div whyBuyBox = new Div();
        whyBuyBox.setWidth("38%");
        whyBuyBox.getElement().getStyle()
            .set("background", "#EFF4FF")
            .set("border-radius", "16px")
            .set("padding", "24px")
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("gap", "16px");

        H3 whyTitle = new H3("Kenapa Beli Ini?");
        whyTitle.getElement().getStyle().set("font-size", "18px").set("font-weight", "800").set("color", "#0F172A").set("margin", "0");

        // SVG icons for why-buy features
        String svgLeaf = "<svg width='16' height='16' viewBox='0 0 24 24' fill='none' xmlns='http://www.w3.org/2000/svg'><path d='M12 22C6 16 2 12 2 8a10 10 0 0120 0c0 4-4 8-10 14z' stroke='#16A34A' stroke-width='1.8' stroke-linecap='round' stroke-linejoin='round'/></svg>";
        String svgGrad = "<svg width='16' height='16' viewBox='0 0 24 24' fill='none' xmlns='http://www.w3.org/2000/svg'><path d='M22 10v1a10 10 0 11-5.93-9.14' stroke='#2563EB' stroke-width='1.8' stroke-linecap='round'/><polyline points='22 4 12 14.01 9 11.01' stroke='#2563EB' stroke-width='1.8' stroke-linecap='round' stroke-linejoin='round'/></svg>";
        String svgCheck = "<svg width='16' height='16' viewBox='0 0 24 24' fill='none' xmlns='http://www.w3.org/2000/svg'><circle cx='12' cy='12' r='10' stroke='#944A07' stroke-width='1.8'/><path d='M8 12l3 3 5-5' stroke='#944A07' stroke-width='1.8' stroke-linecap='round' stroke-linejoin='round'/></svg>";
        Div feat1 = createWhyFeature(svgLeaf, "Sustainable Choice", "Mengurangi limbah tekstil dengan membeli barang pre-loved berkualitas.");
        Div feat2 = createWhyFeature(svgGrad, "Support Students", "Mendukung ekosistem wirausaha siswa SMKN 24 Jakarta.");
        Div feat3 = createWhyFeature(svgCheck, "Verified Item", "Sudah melalui proses pengecekan kualitas oleh tim ReWear.");

        whyBuyBox.add(whyTitle, feat1, feat2, feat3);

        tabContentGrid.add(descTextCol, whyBuyBox);
        tabsSection.add(tabHeader, tabContentGrid);

        containerWrapper.add(breadcrumb, mainGrid, tabsSection);
        return containerWrapper;
    }

    private Div createWhyFeature(String svgIcon, String titleText, String descText) {
        Div feat = new Div();
        feat.getElement().setProperty("innerHTML",
            "<div style='display:flex;align-items:flex-start;gap:12px;'>" +
            "<div style='flex-shrink:0;margin-top:2px;'>" + svgIcon + "</div>" +
            "<div><span style='font-weight:700;font-size:14px;color:#0F172A;display:block;margin-bottom:3px;'>" + titleText + "</span>" +
            "<span style='font-size:12px;color:#64748B;line-height:1.5;'>" + descText + "</span></div></div>");
        return feat;
    }

    /* createRelatedCard dihapus — section Produk Serupa akan dibuat ulang dengan data real dari DB */

    private Component buildFallbackUI(Long productId) {
        Div wrapper = new Div();
        wrapper.getElement().getStyle()
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("align-items", "center")
            .set("justify-content", "center")
            .set("padding", "80px 24px")
            .set("text-align", "center");

        Paragraph title = new Paragraph("🔍 Produk tidak ditemukan");
        title.getElement().getStyle().set("font-size", "24px").set("font-weight", "700").set("color", "#001934").set("margin", "0 0 12px");

        Paragraph subtitle = new Paragraph("Produk dengan ID " + productId + " tidak ada atau sudah tidak tersedia.");
        subtitle.getElement().getStyle().set("font-size", "15px").set("color", "#64748B").set("margin", "0 0 24px");

        Button btnBack = new Button("Kembali ke Beranda");
        btnBack.getElement().getStyle()
            .set("background", "#001934")
            .set("color", "#FFFFFF")
            .set("border", "none")
            .set("padding", "12px 24px")
            .set("border-radius", "8px")
            .set("font-weight", "600")
            .set("cursor", "pointer");
        btnBack.addClickListener(e -> UI.getCurrent().navigate(""));

        wrapper.add(title, subtitle, btnBack);
        return wrapper;
    }

    private String getCategoryName(Product product) {
        try {
            if (product != null && product.getCategory() != null) {
                return product.getCategory().getName();
            }
        } catch (Exception ignored) {}
        return "";
    }

    private String getSellerName(Product product) {
        try {
            if (product != null && product.getSeller() != null) {
                return product.getSeller().getFullName();
            }
        } catch (Exception ignored) {}
        return "";
    }

    private String extractImgUrl(String imagesJson, String fallback) {
        if (imagesJson == null || !imagesJson.contains("images/")) {
            return fallback;
        }
        return imagesJson.replace("[\"", "").replace("\"]", "").trim();
    }
}
