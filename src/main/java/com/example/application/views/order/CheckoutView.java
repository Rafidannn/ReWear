package com.example.application.views.order;

import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import java.util.ArrayList;
import java.util.List;

@PageTitle("Checkout / Pembayaran - ReWear")
@Route(value = "checkout", layout = MainLayout.class)
public class CheckoutView extends Div {

    // Mode checkout: true = Pasar SMKN 24 (COD Sekolah), false = Reguler/Ekspedisi
    private boolean isPasarSmkn24Mode = true;

    private int selectedAddressIndex = 0; // 0: Rumah Utama, 1: Kos Bambu
    private int selectedShippingIndex = 0; // 0: Ambil Sendiri (0), 1: Instan (22000), 2: Reguler (9000)
    private int selectedPaymentIndex = 0; // 0: COD / Escrow, 1: QRIS / Bank Transfer

    private final Span subtotalSpan = new Span("Rp0");
    private final Span shippingFeeSpan = new Span("Gratis");
    private final Span serviceFeeSpan = new Span("Gratis");
    private final Span totalTagihanSpan = new Span("Rp0");

    private final Div leftCol = new Div();
    private final Div rightCol = new Div();
    private final Button btnTabSmkn24 = new Button("🏪 Pasar SMKN 24 (COD Sekolah)");
    private final Button btnTabRegular = new Button("📦 Barang Reguler / Ekspedisi");

    private final Div addressSectionContainer = new Div();
    private final Div shippingSectionContainer = new Div();
    private final Div paymentSectionContainer = new Div();
    private final Div orderItemsContainer = new Div();

    private List<CartItem> allCartItems = new ArrayList<>();

    public CheckoutView() {
        addClassName("rw-checkout-page");

        // Load active items from session
        loadCartFromSession();

        Div wrapper = new Div();
        wrapper.addClassName("rw-checkout-wrapper");

        // ---- Page Title ----
        H2 pageTitle = new H2("Konfirmasi Pesanan");
        pageTitle.addClassName("rw-checkout-page-title");
        wrapper.add(pageTitle);

        // ---- Mode Toggle Tabs (Pasar SMKN 24 vs Reguler) ----
        Div toggleBar = createModeToggleBar();
        wrapper.add(toggleBar);

        // ---- Main Grid (Left Column 62%, Right Column 38%) ----
        Div mainGrid = new Div();
        mainGrid.addClassName("rw-checkout-grid");

        leftCol.addClassName("rw-checkout-left");
        rightCol.addClassName("rw-checkout-right");

        mainGrid.add(leftCol, rightCol);
        wrapper.add(mainGrid);
        add(wrapper);

        renderView();
    }

    @SuppressWarnings("unchecked")
    private void loadCartFromSession() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            List<CartItem> items = (List<CartItem>) session.getAttribute(CartView.SESSION_CART_KEY);
            if (items != null) {
                this.allCartItems = items;
            } else {
                this.allCartItems = createFallbackCartItems();
                session.setAttribute(CartView.SESSION_CART_KEY, this.allCartItems);
            }
        } else {
            this.allCartItems = createFallbackCartItems();
        }
    }

    private void syncCartToSession() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            session.setAttribute(CartView.SESSION_CART_KEY, this.allCartItems);
        }
    }

    private List<CartItem> getSelectedItems() {
        return allCartItems.stream()
            .filter(CartItem::isSelected)
            .filter(item -> isPasarSmkn24Mode ? item.isSmkn24Item() : !item.isSmkn24Item())
            .toList();
    }

    private List<CartItem> createFallbackCartItems() {
        List<CartItem> list = new ArrayList<>();
        list.add(new CartItem(
            "1", "Butik Siswa SMKN 24", "Warga SMKN 24", "badge-gold",
            "Jaket Denim Custom SMKN 24", "Size: L | Warna: Biru Indigo",
            120000, 0, "images/buku.jpeg", null, 1, true, true
        ));
        list.add(new CartItem(
            "2", "Butik Siswa SMKN 24", "Warga SMKN 24", "badge-gold",
            "Minimalist Graphic Tee", "One Size | Material: Cotton",
            45000, 0, "images/colokan.webp", null, 1, true, true
        ));
        list.add(new CartItem(
            "3", "Thrift By Alif", "Verifikasi Member", "badge-blue",
            "Vans Old Skool Classic", "Size: 41 | Kondisi: 9/10",
            350000, 700000, "images/kipas.jpg", "Pre-Loved", 1, true, false
        ));
        return list;
    }

    private Div createModeToggleBar() {
        Div bar = new Div();
        bar.addClassName("rw-checkout-toggle-bar");

        btnTabSmkn24.addClassName("rw-toggle-btn");
        if (isPasarSmkn24Mode) {
            btnTabSmkn24.addClassName("active");
        }
        btnTabSmkn24.addClickListener(e -> {
            isPasarSmkn24Mode = true;
            selectedShippingIndex = 0;
            updateToggleStyles();
            renderView();
        });

        btnTabRegular.addClassName("rw-toggle-btn");
        if (!isPasarSmkn24Mode) {
            btnTabRegular.addClassName("active");
        }
        btnTabRegular.addClickListener(e -> {
            isPasarSmkn24Mode = false;
            selectedShippingIndex = 0;
            updateToggleStyles();
            renderView();
        });

        bar.add(btnTabSmkn24, btnTabRegular);
        return bar;
    }

    private void updateToggleStyles() {
        if (isPasarSmkn24Mode) {
            btnTabSmkn24.addClassName("active");
            btnTabRegular.removeClassName("active");
        } else {
            btnTabRegular.addClassName("active");
            btnTabSmkn24.removeClassName("active");
        }
    }

    private void renderView() {
        leftCol.removeAll();
        rightCol.removeAll();

        // Left Column
        leftCol.add(
            createAddressSection(),
            createOrderDetailsSection(),
            createShippingSection()
        );

        // Right Column
        rightCol.add(createRightColumn());

        updateCalculations();
    }

    // ==========================================
    // LEFT COLUMN SECTIONS
    // ==========================================

    private Component createAddressSection() {
        Div card = new Div();
        card.addClassName("rw-checkout-card");

        if (isPasarSmkn24Mode) {
            // COD Sekolah: Tidak perlu alamat rumah
            Div header = new Div();
            header.addClassName("rw-checkout-card-header");
            header.getElement().setProperty("innerHTML",
                "<div style='display:flex;align-items:center;gap:8px;'>" +
                "<svg width='20' height='20' viewBox='0 0 24 24' fill='none' stroke='#B45309' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><path d='M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z'/><circle cx='12' cy='10' r='3'/></svg>" +
                "<span class='rw-card-header-title'>Titik Temu COD Sekolah (Pasar SMKN 24)</span>" +
                "</div>" +
                "<span class='rw-badge-smkn24'>BEBAS BIAYA KIRIM</span>"
            );

            Div infoBox = new Div();
            infoBox.addClassName("rw-address-smkn24-info");
            infoBox.getElement().setProperty("innerHTML",
                "<div style='font-weight:800;color:#001934;margin-bottom:4px;'>Lobby Utama / Kantin SMKN 24 Jakarta</div>" +
                "<div style='font-size:13px;color:#64748B;line-height:1.4;'>Jl. Bambu Apus No. 24, Cipayung, Jakarta Timur</div>" +
                "<div style='margin-top:8px;font-size:12px;color:#B45309;background:#FEF3C7;padding:8px 12px;border-radius:6px;'>" +
                "ℹ️ <strong>Khusus Pesanan Pasar SMKN 24:</strong> Tidak memerlukan alamat rumah. Pembeli dan penjual akan bertemu langsung di area sekolah." +
                "</div>"
            );

            card.add(header, infoBox);
        } else {
            // Reguler: Perlu alamat rumah
            Div header = new Div();
            header.addClassName("rw-checkout-card-header");
            header.getElement().setProperty("innerHTML",
                "<div style='display:flex;align-items:center;gap:8px;'>" +
                "<svg width='20' height='20' viewBox='0 0 24 24' fill='none' stroke='#001934' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><path d='M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z'/><circle cx='12' cy='10' r='3'/></svg>" +
                "<span class='rw-card-header-title'>Alamat Pengiriman</span>" +
                "</div>" +
                "<span class='rw-card-header-link'>+ Tambah Alamat Baru</span>"
            );

            renderAddressCards();
            card.add(header, addressSectionContainer);
        }

        return card;
    }

    private void renderAddressCards() {
        addressSectionContainer.removeAll();
        addressSectionContainer.addClassName("rw-address-list");

        // Address 1: Rumah Utama
        Div addr1 = new Div();
        addr1.addClassName("rw-address-card");
        if (selectedAddressIndex == 0) {
            addr1.addClassName("selected");
        }
        addr1.getElement().setProperty("innerHTML",
            "<div class='rw-address-header-row'>" +
            "<span class='rw-address-name-tag'>Rumah Utama <span class='rw-badge-utama'>UTAMA</span></span>" +
            "</div>" +
            "<div class='rw-address-recipient'>Budi Santoso (0812-3456-7890)</div>" +
            "<div class='rw-address-detail'>Jl. Bambu Apus No. 24, Cipayung, Jakarta Timur, 13890 (Samping SMKN 24 Jakarta)</div>"
        );
        addr1.addClickListener(e -> {
            selectedAddressIndex = 0;
            renderAddressCards();
        });

        // Address 2: Kos Bambu
        Div addr2 = new Div();
        addr2.addClassName("rw-address-card");
        if (selectedAddressIndex == 1) {
            addr2.addClassName("selected");
        }
        addr2.getElement().setProperty("innerHTML",
            "<div class='rw-address-header-row'>" +
            "<span class='rw-address-name-tag'>Kos Bambu</span>" +
            "</div>" +
            "<div class='rw-address-recipient'>Budi Santoso</div>" +
            "<div class='rw-address-detail'>Gg. Haji Naman No. 12, Bambu Apus, Jakarta Timur</div>"
        );
        addr2.addClickListener(e -> {
            selectedAddressIndex = 1;
            renderAddressCards();
        });

        addressSectionContainer.add(addr1, addr2);
    }

    // ==========================================
    // ORDER DETAILS & CANCEL/REMOVE ITEM FEATURE
    // ==========================================

    private Component createOrderDetailsSection() {
        Div card = new Div();
        card.addClassName("rw-checkout-card");

        Div header = new Div();
        header.addClassName("rw-checkout-card-header");
        header.getElement().setProperty("innerHTML",
            "<div style='display:flex;align-items:center;gap:8px;'>" +
            "<svg width='20' height='20' viewBox='0 0 24 24' fill='none' stroke='#001934' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><path d='M6 2L3 6v14a2 2 0 002 2h14a2 2 0 002-2V6l-3-4z'/><line x1='3' y1='6' x2='21' y2='6'/><path d='M16 10a4 4 0 01-8 0'/></svg>" +
            "<span class='rw-card-header-title'>Rincian Pesanan (" + (isPasarSmkn24Mode ? "Pasar SMKN 24" : "Pasar Umum") + ")</span>" +
            "</div>"
        );

        renderOrderItems();
        card.add(header, orderItemsContainer);
        return card;
    }

    private void renderOrderItems() {
        orderItemsContainer.removeAll();
        orderItemsContainer.addClassName("rw-checkout-items-list");

        List<CartItem> selectedItems = getSelectedItems();

        if (selectedItems.isEmpty()) {
            Div emptyNotice = new Div();
            emptyNotice.addClassName("rw-checkout-empty-notice");
            emptyNotice.getElement().setProperty("innerHTML",
                "<div style='padding:24px;text-align:center;background:#F8FAFC;border-radius:10px;border:1px dashed #CBD5E1;'>" +
                "<p style='color:#64748B;font-size:14px;margin-bottom:12px;'>Belum ada barang yang dipilih untuk di-checkout pada kategori ini.</p>" +
                "</div>"
            );

            Button btnBackCart = new Button("Kembali ke Keranjang", VaadinIcon.ARROW_LEFT.create());
            btnBackCart.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            btnBackCart.addClickListener(e -> UI.getCurrent().navigate("cart"));
            emptyNotice.add(btnBackCart);

            orderItemsContainer.add(emptyNotice);
            return;
        }

        for (int i = 0; i < selectedItems.size(); i++) {
            CartItem item = selectedItems.get(i);

            Div itemRow = new Div();
            itemRow.addClassName("rw-checkout-item-row");

            Div thumbWrap = new Div();
            thumbWrap.addClassName("rw-item-thumb-wrap");
            Image img = new Image(item.getImgUrl(), item.getTitle());
            img.addClassName("rw-item-thumb");
            thumbWrap.add(img);

            Div infoCol = new Div();
            infoCol.addClassName("rw-item-info-col");

            Div nameDiv = new Div();
            nameDiv.setText(item.getTitle());
            nameDiv.addClassName("rw-item-name");

            Div metaDiv = new Div();
            metaDiv.setText(item.getVariant() + " | Qty: " + item.getQuantity());
            metaDiv.addClassName("rw-item-meta");

            infoCol.add(nameDiv, metaDiv);
            if (item.isSmkn24Item()) {
                Span badge = new Span("WARGA SMKN 24");
                badge.addClassName("rw-badge-smkn24");
                infoCol.add(badge);
            }

            Div priceCol = new Div();
            priceCol.addClassName("rw-item-price-col");
            priceCol.setText("Rp" + String.format("%,.0f", item.getPrice() * item.getQuantity()));

            // Tombol Batalkan / Remove dari checkout
            Button btnCancelItem = new Button("Batalkan", VaadinIcon.CLOSE_SMALL.create());
            btnCancelItem.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            btnCancelItem.addClassName("rw-btn-cancel-checkout-item");
            btnCancelItem.addClickListener(e -> {
                item.setSelected(false);
                syncCartToSession();
                renderView();
                Notification.show("Item " + item.getTitle() + " dibatalkan dari checkout.", 2000, Notification.Position.TOP_CENTER);
            });

            itemRow.add(thumbWrap, infoCol, priceCol, btnCancelItem);
            orderItemsContainer.add(itemRow);

            if (i < selectedItems.size() - 1) {
                Hr divider = new Hr();
                divider.addClassName("rw-item-divider");
                orderItemsContainer.add(divider);
            }
        }
    }

    private Component createShippingSection() {
        Div card = new Div();
        card.addClassName("rw-checkout-card");

        Div header = new Div();
        header.addClassName("rw-checkout-card-header");
        header.getElement().setProperty("innerHTML",
            "<div style='display:flex;align-items:center;gap:8px;'>" +
            "<svg width='20' height='20' viewBox='0 0 24 24' fill='none' stroke='#001934' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><rect x='1' y='3' width='15' height='13'/><polygon points='16 8 20 8 23 11 23 16 16 16 16 8'/><circle cx='5.5' cy='18.5' r='2.5'/><circle cx='18.5' cy='18.5' r='2.5'/></svg>" +
            "<span class='rw-card-header-title'>Opsi Pengiriman & Biaya Ekspedisi</span>" +
            "</div>"
        );

        renderShippingOptions();
        card.add(header, shippingSectionContainer);
        return card;
    }

    private void renderShippingOptions() {
        shippingSectionContainer.removeAll();
        shippingSectionContainer.addClassName("rw-shipping-grid");

        if (isPasarSmkn24Mode) {
            // Hanya 1 opsi: Ambil Sendiri / COD Sekolah
            Div opt0 = new Div();
            opt0.addClassName("rw-shipping-card");
            opt0.addClassName("selected");
            opt0.getElement().setProperty("innerHTML",
                "<div class='rw-shipping-title-row'>" +
                "<span class='rw-shipping-title'>Ambil Sendiri / COD Sekolah</span>" +
                "<span class='rw-shipping-badge-free'>Gratis</span>" +
                "</div>" +
                "<div class='rw-shipping-desc'>Titik temu langsung di Lobby SMKN 24 Jakarta. Estimasi 1 jam setelah konfirmasi penjual.</div>"
            );
            shippingSectionContainer.add(opt0);
        } else {
            // 3 opsi pengiriman reguler
            Div opt0 = new Div();
            opt0.addClassName("rw-shipping-card");
            if (selectedShippingIndex == 0) opt0.addClassName("selected");
            opt0.getElement().setProperty("innerHTML",
                "<div class='rw-shipping-title-row'>" +
                "<span class='rw-shipping-title'>Ambil Sendiri (Gratis)</span>" +
                "<span class='rw-shipping-badge-free'>Gratis</span>" +
                "</div>" +
                "<div class='rw-shipping-desc'>Ambil barang di lokasi penjual (Bambu Apus, Cipayung).</div>"
            );
            opt0.addClickListener(e -> {
                selectedShippingIndex = 0;
                renderShippingOptions();
                updateCalculations();
            });

            Div opt1 = new Div();
            opt1.addClassName("rw-shipping-card");
            if (selectedShippingIndex == 1) opt1.addClassName("selected");
            opt1.getElement().setProperty("innerHTML",
                "<div class='rw-shipping-title-row'>" +
                "<span class='rw-shipping-title'>Instan (Gojek/Grab - Max 10km)</span>" +
                "<span class='rw-shipping-price'>Rp22.000</span>" +
                "</div>" +
                "<div class='rw-shipping-desc'>Pengiriman kurir instan cepat tiba (1-2 jam). Sesuaikan jarak dari lokasi toko.</div>"
            );
            opt1.addClickListener(e -> {
                selectedShippingIndex = 1;
                renderShippingOptions();
                updateCalculations();
            });

            Div opt2 = new Div();
            opt2.addClassName("rw-shipping-card");
            if (selectedShippingIndex == 2) opt2.addClassName("selected");
            opt2.getElement().setProperty("innerHTML",
                "<div class='rw-shipping-title-row'>" +
                "<span class='rw-shipping-title'>Reguler (JNE/J&T Ekspedisi Nasional)</span>" +
                "<span class='rw-shipping-price'>Rp9.000</span>" +
                "</div>" +
                "<div class='rw-shipping-desc'>Pengiriman ekspedisi ke seluruh Indonesia (2-3 hari kerja).</div>"
            );
            opt2.addClickListener(e -> {
                selectedShippingIndex = 2;
                renderShippingOptions();
                updateCalculations();
            });

            shippingSectionContainer.add(opt0, opt1, opt2);
        }
    }

    // ==========================================
    // RIGHT COLUMN: PAYMENT & SUMMARY
    // ==========================================

    private Div createRightColumn() {
        Div rightDiv = new Div();

        Div payCard = new Div();
        payCard.addClassName("rw-payment-box");

        H3 payTitle = new H3("Pilih Metode Pembayaran");
        payTitle.addClassName("rw-payment-box-title");

        renderPaymentOptions();

        // Note Info Banner
        Div noteBanner = new Div();
        noteBanner.addClassName("rw-payment-note-banner");
        if (isPasarSmkn24Mode) {
            noteBanner.getElement().setProperty("innerHTML",
                "<svg width='16' height='16' viewBox='0 0 24 24' fill='none' stroke='#001934' stroke-width='2' style='flex-shrink:0;'><circle cx='12' cy='12' r='10'/><line x1='12' y1='16' x2='12' y2='12'/><line x1='12' y1='8' x2='12.01' y2='8'/></svg>" +
                "<span><strong>COD Sekolah & QRIS:</strong> Bebas biaya pengiriman & biaya layanan bagi komunitas SMKN 24 Jakarta.</span>"
            );
        } else {
            noteBanner.getElement().setProperty("innerHTML",
                "<svg width='16' height='16' viewBox='0 0 24 24' fill='none' stroke='#001934' stroke-width='2' style='flex-shrink:0;'><circle cx='12' cy='12' r='10'/><line x1='12' y1='16' x2='12' y2='12'/><line x1='12' y1='8' x2='12.01' y2='8'/></svg>" +
                "<span><strong>ReWear Escrow Protection:</strong> Biaya layanan dan ongkir disesuaikan dengan jarak & nilai produk demi keamanan transaksi 100%.</span>"
            );
        }

        // Price Breakdown
        Div summaryRows = new Div();
        summaryRows.addClassName("rw-checkout-summary-rows");

        Div rowSubtotal = createRowSpan("Subtotal Barang", subtotalSpan);
        Div rowShipping = createRowSpan("Biaya Pengiriman", shippingFeeSpan);
        Div rowService = createRowSpan("Biaya Layanan", serviceFeeSpan);

        summaryRows.add(rowSubtotal, rowShipping, rowService);

        // Total Row
        Div rowTotal = new Div();
        rowTotal.addClassName("rw-checkout-total-row");
        Span totalLabel = new Span("Total Tagihan");
        totalLabel.addClassName("rw-checkout-total-label");
        totalTagihanSpan.addClassName("rw-checkout-total-val");
        rowTotal.add(totalLabel, totalTagihanSpan);

        // Confirm Button (Opens Validation Confirmation Dialog)
        Button btnConfirm = new Button("Konfirmasi Bayar");
        btnConfirm.addClassName("btn-confirm-pay");
        btnConfirm.addClickListener(e -> {
            List<CartItem> selected = getSelectedItems();
            if (selected.isEmpty()) {
                Notification.show("Tidak ada barang yang terpilih untuk di-checkout.", 2500, Notification.Position.TOP_CENTER);
            } else {
                openOrderValidationDialog(selected);
            }
        });

        Paragraph finePrint = new Paragraph("Dengan menekan tombol di atas, Anda menyetujui Syarat & Ketentuan transaksi di ReWear.");
        finePrint.addClassName("rw-checkout-fine-print");

        payCard.add(payTitle, paymentSectionContainer, noteBanner, summaryRows, rowTotal, btnConfirm, finePrint);

        // Escrow Protected Notice Card below
        Div escrowBox = new Div();
        escrowBox.addClassName("rw-escrow-protected-box");
        escrowBox.getElement().setProperty("innerHTML",
            "<svg width='22' height='22' viewBox='0 0 24 24' fill='none' stroke='#001934' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><path d='M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z'/></svg>" +
            "<div>" +
            "<div style='font-size:12px;font-weight:800;color:#001934;letter-spacing:0.5px;'>ESCROW PROTECTED</div>" +
            "<div style='font-size:12px;color:#475569;'>Transaksi dilindungi oleh sistem keamanan SMKN 24.</div>" +
            "</div>"
        );

        rightDiv.add(payCard, escrowBox);
        return rightDiv;
    }

    // ==========================================
    // VALIDATION CONFIRMATION MODAL DIALOG
    // ==========================================

    private void openOrderValidationDialog(List<CartItem> itemsToPay) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Verifikasi & Konfirmasi Pesanan");
        dialog.setWidth("520px");

        Div body = new Div();
        body.getStyle().set("padding", "8px 0");

        Paragraph subhead = new Paragraph("Mohon pastikan rincian barang, alamat pengiriman, dan metode pembayaran Anda sudah sesuai sebelum melanjutkan:");
        subhead.getStyle().set("font-size", "14px").set("color", "#64748B").set("margin-bottom", "16px");
        body.add(subhead);

        // Box 1: Items List Summary
        Div itemsBox = new Div();
        itemsBox.getStyle()
            .set("background", "#F8FAFC")
            .set("border", "1px solid #E2E8F0")
            .set("border-radius", "8px")
            .set("padding", "12px 16px")
            .set("margin-bottom", "14px");

        H5 itemsTitle = new H5("📦 Barang yang Dibeli (" + itemsToPay.size() + " produk):");
        itemsTitle.getStyle().set("margin", "0 0 8px 0").set("color", "#001934").set("font-weight", "700");
        itemsBox.add(itemsTitle);

        for (CartItem item : itemsToPay) {
            Div itemRow = new Div();
            itemRow.getStyle().set("display", "flex").set("justify-content", "space-between").set("font-size", "13px").set("margin-bottom", "6px");
            Span name = new Span("• " + item.getTitle() + " (x" + item.getQuantity() + ")");
            name.getStyle().set("color", "#1E293B").set("font-weight", "600");
            Span price = new Span("Rp " + String.format("%,.0f", item.getPrice() * item.getQuantity()));
            price.getStyle().set("color", "#001934").set("font-weight", "700");
            itemRow.add(name, price);
            itemsBox.add(itemRow);
        }
        body.add(itemsBox);

        // Box 2: Fulfillment & Shipping Detail
        Div shipBox = new Div();
        shipBox.getStyle()
            .set("background", "#F8FAFC")
            .set("border", "1px solid #E2E8F0")
            .set("border-radius", "8px")
            .set("padding", "12px 16px")
            .set("margin-bottom", "14px");

        String locText = isPasarSmkn24Mode ? "Lobby / Kantin Utama SMKN 24 Jakarta (COD Sekolah)" : (selectedAddressIndex == 0 ? "Rumah Utama - Jl. Bambu Apus No. 24" : "Kos Bambu - Gg. Haji Naman");
        String shipText = isPasarSmkn24Mode ? "COD Ambil Sendiri (Bebas Ongkir)" : (selectedShippingIndex == 0 ? "Ambil Sendiri (Gratis)" : (selectedShippingIndex == 1 ? "Instan Gojek/Grab (Rp22.000)" : "Reguler JNE/J&T (Rp9.000)"));

        Div rowLoc = new Div(new Span("📍 Tujuan: "), new Span(locText));
        rowLoc.getStyle().set("font-size", "13px").set("margin-bottom", "4px").set("color", "#334155");
        Div rowShip = new Div(new Span("🚚 Pengiriman: "), new Span(shipText));
        rowShip.getStyle().set("font-size", "13px").set("color", "#334155");
        shipBox.add(rowLoc, rowShip);
        body.add(shipBox);

        // Box 3: Payment Method & Total
        Div payBox = new Div();
        payBox.getStyle()
            .set("background", "#EFF6FF")
            .set("border", "1px solid #BFDBFE")
            .set("border-radius", "8px")
            .set("padding", "12px 16px");

        String payText = isPasarSmkn24Mode ? (selectedPaymentIndex == 0 ? "COD Sekolah (Bayar Cash saat COD)" : "QRIS Instan SMKN 24") : (selectedPaymentIndex == 0 ? "Escrow Rekber Safety" : "Transfer Bank / Virtual Account");

        Div rowPay = new Div(new Span("💳 Pembayaran: "), new Span(payText));
        rowPay.getStyle().set("font-size", "13px").set("margin-bottom", "6px").set("color", "#1E40AF").set("font-weight", "600");

        Div rowTotalVal = new Div(new Span("💰 Total Tagihan: "), new Span(totalTagihanSpan.getText()));
        rowTotalVal.getStyle().set("font-size", "16px").set("font-weight", "800").set("color", "#001934");

        payBox.add(rowPay, rowTotalVal);
        body.add(payBox);

        dialog.add(body);

        // Footer buttons
        Button btnBack = new Button("Periksa Kembali", e -> dialog.close());
        btnBack.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button btnProceed = new Button("Ya, Pesanan Sesuai & Bayar", VaadinIcon.CHECK_CIRCLE.create(), e -> {
            dialog.close();

            // Clear purchased items from session cart
            allCartItems.removeIf(i -> i.isSelected() && (isPasarSmkn24Mode ? i.isSmkn24Item() : !i.isSmkn24Item()));
            syncCartToSession();

            Notification.show("Pesanan Berhasil Dikonfirmasi & Diproses!", 3000, Notification.Position.TOP_CENTER);
            UI.getCurrent().navigate("profile?tab=orders");
        });
        btnProceed.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnProceed.getStyle().set("background", "#001934").set("color", "#FFFFFF");

        dialog.getFooter().add(btnBack, btnProceed);
        dialog.open();
    }

    private void renderPaymentOptions() {
        paymentSectionContainer.removeAll();
        paymentSectionContainer.addClassName("rw-payment-methods-grid");

        if (isPasarSmkn24Mode) {
            // Opsi untuk Pasar SMKN 24: COD Sekolah & QRIS
            Div pay0 = new Div();
            pay0.addClassName("rw-pay-card");
            if (selectedPaymentIndex == 0) pay0.addClassName("selected");
            pay0.getElement().setProperty("innerHTML",
                "<div class='rw-pay-icon-wrap gold-wrap'>" +
                "<svg width='22' height='22' viewBox='0 0 24 24' fill='none' stroke='#B45309' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><path d='M17 9V7a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2'/><rect x='9' y='9' width='12' height='10' rx='2'/><circle cx='15' cy='14' r='1'/></svg>" +
                "</div>" +
                "<div class='rw-pay-title'>COD Sekolah</div>" +
                "<span class='rw-pay-badge-gold'>Tunai (Bebas Biaya)</span>" +
                "<div class='rw-pay-subtext'>Bayar cash langsung saat COD di area SMKN 24.</div>"
            );
            pay0.addClickListener(e -> {
                selectedPaymentIndex = 0;
                renderPaymentOptions();
            });

            Div pay1 = new Div();
            pay1.addClassName("rw-pay-card");
            if (selectedPaymentIndex == 1) pay1.addClassName("selected");
            pay1.getElement().setProperty("innerHTML",
                "<div class='rw-pay-icon-wrap blue-wrap'>" +
                "<svg width='22' height='22' viewBox='0 0 24 24' fill='none' stroke='#3730A3' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><rect x='3' y='3' width='18' height='18' rx='2'/><path d='M7 7h3v3H7zM14 7h3v3h-3zM7 14h3v3H7z'/></svg>" +
                "</div>" +
                "<div class='rw-pay-title'>QRIS Instan</div>" +
                "<span class='rw-pay-badge-blue'>E-Wallet / Bank</span>" +
                "<div class='rw-pay-subtext'>Scan QRIS Gopay/OVO/Dana/BCA tanpa biaya admin.</div>"
            );
            pay1.addClickListener(e -> {
                selectedPaymentIndex = 1;
                renderPaymentOptions();
            });

            paymentSectionContainer.add(pay0, pay1);
        } else {
            // Opsi Pembayaran Reguler: Escrow & Transfer Bank
            Div pay0 = new Div();
            pay0.addClassName("rw-pay-card");
            if (selectedPaymentIndex == 0) pay0.addClassName("selected");
            pay0.getElement().setProperty("innerHTML",
                "<div class='rw-pay-icon-wrap blue-wrap'>" +
                "<svg width='22' height='22' viewBox='0 0 24 24' fill='none' stroke='#3730A3' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><path d='M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z'/></svg>" +
                "</div>" +
                "<div class='rw-pay-title'>Escrow (Rekber)</div>" +
                "<span class='rw-pay-badge-blue'>Terjamin 100%</span>" +
                "<div class='rw-pay-subtext'>Dana ditahan sistem hingga barang diterima.</div>"
            );
            pay0.addClickListener(e -> {
                selectedPaymentIndex = 0;
                renderPaymentOptions();
            });

            Div pay1 = new Div();
            pay1.addClassName("rw-pay-card");
            if (selectedPaymentIndex == 1) pay1.addClassName("selected");
            pay1.getElement().setProperty("innerHTML",
                "<div class='rw-pay-icon-wrap gold-wrap'>" +
                "<svg width='22' height='22' viewBox='0 0 24 24' fill='none' stroke='#B45309' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><rect x='2' y='5' width='20' height='14' rx='2'/><line x1='2' y1='10' x2='22' y2='10'/></svg>" +
                "</div>" +
                "<div class='rw-pay-title'>Transfer Bank</div>" +
                "<span class='rw-pay-badge-gold'>Virtual Account</span>" +
                "<div class='rw-pay-subtext'>BCA, Mandiri, BRI, BNI Virtual Account.</div>"
            );
            pay1.addClickListener(e -> {
                selectedPaymentIndex = 1;
                renderPaymentOptions();
            });

            paymentSectionContainer.add(pay0, pay1);
        }
    }

    private Div createRowSpan(String label, Span valSpan) {
        Span lSpan = new Span(label);
        Div r = new Div(lSpan, valSpan);
        r.addClassName("rw-checkout-summary-row");
        return r;
    }

    private void updateCalculations() {
        List<CartItem> selectedItems = getSelectedItems();
        double currentSubtotal = selectedItems.stream()
            .mapToDouble(i -> i.getPrice() * i.getQuantity())
            .sum();

        subtotalSpan.setText("Rp" + String.format("%,.0f", currentSubtotal));

        double currentShippingFee = 0;
        double currentServiceFee = 0;

        if (currentSubtotal == 0) {
            currentShippingFee = 0;
            currentServiceFee = 0;
            shippingFeeSpan.setText("Rp0");
            serviceFeeSpan.setText("Rp0");
        } else if (isPasarSmkn24Mode) {
            // Pasar SMKN 24: Gratis Ongkir & Layanan
            currentShippingFee = 0;
            currentServiceFee = 0;

            shippingFeeSpan.setText("Gratis");
            shippingFeeSpan.getElement().getStyle().set("color", "#16A34A").set("font-weight", "700");

            serviceFeeSpan.setText("Gratis (SMKN 24)");
            serviceFeeSpan.getElement().getStyle().set("color", "#16A34A").set("font-weight", "700");
        } else {
            // Reguler: Disesuaikan dengan opsi pengiriman & nilai produk
            if (selectedShippingIndex == 0) {
                currentShippingFee = 0;
                shippingFeeSpan.setText("Gratis");
                shippingFeeSpan.getElement().getStyle().set("color", "#16A34A").set("font-weight", "700");
            } else if (selectedShippingIndex == 1) {
                currentShippingFee = 22000;
                shippingFeeSpan.setText("Rp22.000");
                shippingFeeSpan.getElement().getStyle().set("color", "#001934").set("font-weight", "700");
            } else {
                currentShippingFee = 9000;
                shippingFeeSpan.setText("Rp9.000");
                shippingFeeSpan.getElement().getStyle().set("color", "#001934").set("font-weight", "700");
            }

            // Biaya Layanan Disesuaikan dengan harga produk
            currentServiceFee = Math.max(2500, currentSubtotal * 0.01);
            serviceFeeSpan.setText("Rp" + String.format("%,.0f", currentServiceFee));
            serviceFeeSpan.getElement().getStyle().set("color", "#001934").set("font-weight", "700");
        }

        double total = currentSubtotal > 0 ? (currentSubtotal + currentShippingFee + currentServiceFee) : 0;
        totalTagihanSpan.setText("Rp" + String.format("%,.0f", total));
    }
}
