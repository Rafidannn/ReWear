package com.example.application.views.order;

import com.example.application.model.order.CartItemEntity;
import com.example.application.model.user.User;
import com.example.application.service.order.CartService;
import com.example.application.util.AuthGuard;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import java.util.ArrayList;
import java.util.List;

@PageTitle("Keranjang Belanja - ReWear")
@Route(value = "cart", layout = MainLayout.class)
public class CartView extends Div {

    public static final String SESSION_CART_KEY = "REWEAR_USER_CART_ITEMS_V2";
    public static final String SESSION_CART_INIT_KEY = "REWEAR_USER_CART_INITIALIZED_V2";

    private final CartService cartService;
    private List<CartItem> cartItems;
    private final Div leftContainer = new Div();
    private final Span totalHargaSpan = new Span("Rp 0");
    private final Span diskonSpan = new Span("-Rp 0");
    private final Span biayaLayananSpan = new Span("Rp 0");
    private final Span totalTagihanSpan = new Span("Rp 0");
    private final Span totalCountSpan = new Span("Total Harga (0 barang)");
    private final Checkbox selectAllCheckbox = new Checkbox("Pilih Semua");

    private double discountAmount = 0;
    private double dynamicServiceFee = 0;

    public CartView(CartService cartService) {
        this.cartService = cartService;
        if (!AuthGuard.requireLogin(UI.getCurrent())) return;

        addClassName("rw-cart-page");

        // Load cart items from DB via CartService
        this.cartItems = loadCartFromDatabase();

        Div wrapper = new Div();
        wrapper.addClassName("rw-cart-wrapper");

        // ---- Page Header ----
        H2 pageTitle = new H2("Keranjang Belanja");
        pageTitle.addClassName("rw-cart-page-title");

        Paragraph pageSub = new Paragraph("Kelola barang-barang pilihanmu sebelum checkout.");
        pageSub.addClassName("rw-cart-page-sub");

        Div headerArea = new Div(pageTitle, pageSub);
        headerArea.addClassName("rw-cart-header-area");

        // ---- Main Grid (2 Columns: Left 65%, Right 35%) ----
        Div mainGrid = new Div();
        mainGrid.addClassName("rw-cart-grid");

        // Left Container
        leftContainer.addClassName("rw-cart-left");
        renderLeftColumn();

        // Right Container
        Div rightContainer = createRightColumn();
        rightContainer.addClassName("rw-cart-right");

        mainGrid.add(leftContainer, rightContainer);
        wrapper.add(headerArea, mainGrid);
        add(wrapper);

        recalculateTotal();
    }


    private void renderLeftColumn() {
        leftContainer.removeAll();

        if (cartItems.isEmpty()) {
            Div emptyDiv = new Div();
            emptyDiv.addClassName("rw-cart-empty-box");
            emptyDiv.getElement().setProperty("innerHTML",
                "<div style='text-align:center;padding:48px 20px;background:#FFF;border-radius:12px;border:1px solid #E2E8F0;'>" +
                "<svg width='64' height='64' viewBox='0 0 24 24' fill='none' stroke='#CBD5E1' stroke-width='1.5' style='margin-bottom:16px;'><circle cx='9' cy='21' r='1'/><circle cx='20' cy='21' r='1'/><path d='M1 1h4l2.68 13.39a2 2 0 002 1.61h9.72a2 2 0 002-1.61L23 6H6'/></svg>" +
                "<h3 style='color:#001934;margin:0 0 8px 0;font-size:18px;'>Keranjang Belanjamu Kosong</h3>" +
                "<p style='color:#64748B;margin:0 0 24px 0;font-size:14px;'>Semua produk telah dihapus dari keranjang Anda.</p>" +
                "</div>"
            );

            Button btnExplore = new Button("Jelajahi Produk Pasar SMKN 24");
            btnExplore.addClassName("btn-confirm-pay");
            btnExplore.addClickListener(e -> UI.getCurrent().navigate(""));
            emptyDiv.add(btnExplore);

            leftContainer.add(emptyDiv);
            return;
        }

        // ---- Header Box: Pilih Semua & Hapus Terpilih ----
        Div selectAllBox = new Div();
        selectAllBox.addClassName("rw-cart-select-all-box");

        boolean allSelected = !cartItems.isEmpty() && cartItems.stream().allMatch(CartItem::isSelected);
        selectAllCheckbox.setValue(allSelected);
        selectAllCheckbox.addClassName("rw-cart-checkbox");
        selectAllCheckbox.addValueChangeListener(e -> {
            boolean val = e.getValue();
            cartItems.forEach(i -> i.setSelected(val));
            syncCartToSession();
            renderLeftColumn();
            recalculateTotal();
        });

        // Tombol Hapus Terpilih
        Button deleteSelectedBtn = new Button("Hapus Terpilih", VaadinIcon.TRASH.create());
        deleteSelectedBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        deleteSelectedBtn.addClassName("rw-cart-delete-selected");
        deleteSelectedBtn.addClickListener(e -> {
            List<CartItem> selectedList = cartItems.stream().filter(CartItem::isSelected).toList();
            if (!selectedList.isEmpty()) {
                for (CartItem item : selectedList) {
                    try {
                        cartService.removeFromCart(Long.parseLong(item.getId()));
                    } catch (Exception ignored) {}
                }
                cartItems.removeAll(selectedList);
                syncCartToSession();
                renderLeftColumn();
                recalculateTotal();
                Notification.show("Item terpilih berhasil dihapus.", 2000, Notification.Position.TOP_CENTER);
            } else {
                Notification.show("Pilih produk yang ingin dihapus terlebih dahulu.", 2000, Notification.Position.TOP_CENTER);
            }
        });

        selectAllBox.add(selectAllCheckbox, deleteSelectedBtn);
        leftContainer.add(selectAllBox);

        // Grouping items by Store Name
        List<String> stores = cartItems.stream()
            .map(CartItem::getStoreName)
            .distinct()
            .toList();

        for (String storeName : stores) {
            List<CartItem> storeItems = cartItems.stream()
                .filter(i -> i.getStoreName().equals(storeName))
                .toList();

            if (storeItems.isEmpty()) continue;

            CartItem firstItem = storeItems.get(0);
            Div storeCard = new Div();
            storeCard.addClassName("rw-cart-store-group");

            // Store Header
            Div storeHeader = new Div();
            storeHeader.addClassName("rw-cart-store-header");

            Checkbox storeCheckbox = new Checkbox();
            storeCheckbox.addClassName("rw-cart-checkbox");
            boolean storeAllSelected = storeItems.stream().allMatch(CartItem::isSelected);
            storeCheckbox.setValue(storeAllSelected);
            storeCheckbox.addValueChangeListener(e -> {
                boolean val = e.getValue();
                storeItems.forEach(i -> i.setSelected(val));
                syncCartToSession();
                renderLeftColumn();
                recalculateTotal();
            });

            Span storeIcon = new Span();
            storeIcon.getElement().setProperty("innerHTML",
                "<svg width='18' height='18' viewBox='0 0 24 24' fill='none' stroke='#001934' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><path d='M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z'/><polyline points='9 22 9 12 15 12 15 22'/></svg>"
            );

            Span storeNameSpan = new Span(storeName);
            storeNameSpan.addClassName("rw-cart-store-name");

            Span storeBadge = new Span(firstItem.getStoreBadge());
            storeBadge.addClassNames("rw-cart-badge", firstItem.getStoreBadgeClass());

            Div storeTitleWrap = new Div(storeIcon, storeNameSpan, storeBadge);
            storeTitleWrap.addClassName("rw-cart-store-title-wrap");

            storeHeader.add(storeCheckbox, storeTitleWrap);
            storeCard.add(storeHeader);

            // Store Items List
            double storeSubtotal = 0;
            Div itemsContainer = new Div();
            itemsContainer.addClassName("rw-cart-items-container");

            for (CartItem item : storeItems) {
                if (item.isSelected()) {
                    storeSubtotal += item.getPrice() * item.getQuantity();
                }

                Div itemRow = new Div();
                itemRow.addClassName("rw-cart-item-row");

                Checkbox itemCheckbox = new Checkbox();
                itemCheckbox.addClassName("rw-cart-checkbox");
                itemCheckbox.setValue(item.isSelected());
                itemCheckbox.addValueChangeListener(e -> {
                    item.setSelected(e.getValue());
                    syncCartToSession();
                    renderLeftColumn();
                    recalculateTotal();
                });

                // Image with optional badge
                Div imgWrap = new Div();
                imgWrap.addClassName("rw-cart-img-wrap");

                Image img = new Image(item.getImgUrl(), item.getTitle());
                img.addClassName("rw-cart-img");
                imgWrap.add(img);

                if (item.getItemBadge() != null) {
                    Span badge = new Span(item.getItemBadge());
                    badge.addClassName("rw-cart-item-badge");
                    imgWrap.add(badge);
                }

                // Details Area
                Div infoDiv = new Div();
                infoDiv.addClassName("rw-cart-item-info");

                H4 itemTitle = new H4(item.getTitle());
                itemTitle.addClassName("rw-cart-item-title");

                Span variantSpan = new Span(item.getVariant());
                variantSpan.addClassName("rw-cart-item-variant");

                // Price display
                Div priceWrap = new Div();
                priceWrap.addClassName("rw-cart-price-wrap");

                Span mainPrice = new Span("Rp " + String.format("%,.0f", item.getPrice()));
                mainPrice.addClassName("rw-cart-item-price");
                priceWrap.add(mainPrice);

                if (item.getOriginalPrice() > item.getPrice()) {
                    Span origPrice = new Span("Rp " + String.format("%,.0f", item.getOriginalPrice()));
                    origPrice.addClassName("rw-cart-item-orig-price");
                    priceWrap.add(origPrice);
                }

                infoDiv.add(itemTitle, variantSpan, priceWrap);

                // Right Actions: Trash & Quantity
                Div actionsDiv = new Div();
                actionsDiv.addClassName("rw-cart-item-actions");

                // Tombol Trash Vaadin Button
                Button trashBtn = new Button(VaadinIcon.TRASH.create());
                trashBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
                trashBtn.setTooltipText("Hapus barang ini");
                trashBtn.addClickListener(e -> {
                    try {
                        cartService.removeFromCart(Long.parseLong(item.getId()));
                    } catch (Exception ignored) {}
                    cartItems.remove(item);
                    syncCartToSession();
                    renderLeftColumn();
                    recalculateTotal();
                    Notification.show("Produk " + item.getTitle() + " berhasil dihapus.", 2000, Notification.Position.TOP_CENTER);
                });

                // Qty controls [-] [1] [+]
                Div qtyWrap = new Div();
                qtyWrap.addClassName("rw-qty-controller");

                Button minusBtn = new Button("-");
                minusBtn.addClassName("rw-qty-btn");
                minusBtn.addClickListener(e -> {
                    if (item.getQuantity() > 1) {
                        int newQty = item.getQuantity() - 1;
                        item.setQuantity(newQty);
                        try {
                            cartService.updateQuantity(Long.parseLong(item.getId()), newQty);
                        } catch (Exception ignored) {}
                        syncCartToSession();
                        renderLeftColumn();
                        recalculateTotal();
                    } else {
                        // Confirm deletion if quantity is 1
                        try {
                            cartService.removeFromCart(Long.parseLong(item.getId()));
                        } catch (Exception ignored) {}
                        cartItems.remove(item);
                        syncCartToSession();
                        renderLeftColumn();
                        recalculateTotal();
                        Notification.show("Produk " + item.getTitle() + " dihapus dari keranjang.", 2000, Notification.Position.TOP_CENTER);
                    }
                });

                Span qtyVal = new Span(String.valueOf(item.getQuantity()));
                qtyVal.addClassName("rw-qty-val");

                Button plusBtn = new Button("+");
                plusBtn.addClassName("rw-qty-btn");
                plusBtn.addClickListener(e -> {
                    int maxLimit = item.getMaxStock();
                    if (item.getQuantity() < maxLimit) {
                        int newQty = item.getQuantity() + 1;
                        item.setQuantity(newQty);
                        try {
                            cartService.updateQuantity(Long.parseLong(item.getId()), newQty);
                        } catch (Exception ignored) {}
                        syncCartToSession();
                        renderLeftColumn();
                        recalculateTotal();
                    } else {
                        Notification.show("Maksimal stok tercapai (" + maxLimit + " barang)", 2000, Notification.Position.TOP_CENTER);
                    }
                });

                qtyWrap.add(minusBtn, qtyVal, plusBtn);
                actionsDiv.add(trashBtn, qtyWrap);

                itemRow.add(itemCheckbox, imgWrap, infoDiv, actionsDiv);
                itemsContainer.add(itemRow);
            }

            storeCard.add(itemsContainer);

            // Store Subtotal Footer
            Div storeFooter = new Div();
            storeFooter.addClassName("rw-cart-store-footer");

            double storeSubtotal = storeItems.stream()
                .filter(CartItem::isSelected)
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum();

            Span storeSubtotalLabel = new Span("Subtotal Toko:");
            storeSubtotalLabel.addClassName("rw-store-subtotal-label");

            Span storeSubtotalVal = new Span("Rp " + String.format("%,.0f", storeSubtotal));
            storeSubtotalVal.addClassName("rw-store-subtotal-val");

            storeFooter.add(storeSubtotalLabel, storeSubtotalVal);
            storeCard.add(storeFooter);

            leftContainer.add(storeCard);
        }
    }

    private Div createRightColumn() {
        Div rightDiv = new Div();

        // 1. Voucher Card Box
        Div voucherCard = new Div();
        voucherCard.addClassName("rw-voucher-card");

        Div voucherHeader = new Div();
        voucherHeader.addClassName("rw-voucher-header");
        voucherHeader.getElement().setProperty("innerHTML",
            "<svg width='20' height='20' viewBox='0 0 24 24' fill='none' stroke='#78350F' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><path d='M15 5v2m0 4v2m0 4v2M5 5h14a2 2 0 012 2v3a2 2 0 000 4v3a2 2 0 01-2 2H5a2 2 0 01-2-2v-3a2 2 0 000-4V7a2 2 0 012-2z'/></svg>" +
            "<span>Gunakan Voucher Belanja</span>"
        );

        Div voucherForm = new Div();
        voucherForm.addClassName("rw-voucher-form");

        TextField promoInput = new TextField();
        promoInput.setPlaceholder("Kode Promo (cth: REWEAR24)");
        promoInput.addClassName("rw-voucher-input");

        Button btnPakai = new Button("Pakai");
        btnPakai.addClassName("rw-voucher-btn");
        btnPakai.addClickListener(e -> {
            String code = promoInput.getValue() != null ? promoInput.getValue().trim().toUpperCase() : "";
            if ("REWEAR24".equals(code) || "SMKN24".equals(code)) {
                discountAmount = 15000;
                Notification.show("Voucher " + code + " berhasil dipasang (-Rp 15.000)!", 2500, Notification.Position.TOP_CENTER);
            } else if ("HEMAT".equals(code)) {
                discountAmount = 10000;
                Notification.show("Voucher " + code + " berhasil dipasang (-Rp 10.000)!", 2500, Notification.Position.TOP_CENTER);
            } else if (!code.isEmpty()) {
                Notification.show("Kode voucher tidak valid.", 2000, Notification.Position.TOP_CENTER);
            }
            recalculateTotal();
        });

        voucherForm.add(promoInput, btnPakai);
        voucherCard.add(voucherHeader, voucherForm);

        // 2. Ringkasan Belanja Box
        Div summaryCard = new Div();
        summaryCard.addClassName("rw-summary-card");

        H3 summaryTitle = new H3("Ringkasan Belanja");
        summaryTitle.addClassName("rw-summary-title");

        Div rowTotalHarga = createSummaryRow(totalCountSpan, totalHargaSpan);
        Div rowDiskon = createSummaryRow(new Span("Diskon Produk"), diskonSpan);
        diskonSpan.getElement().getStyle().set("color", "#DC2626");

        Div rowBiaya = createSummaryRow(new Span("Biaya Layanan Penanganan"), biayaLayananSpan);

        Hr divider = new Hr();
        divider.addClassName("rw-summary-divider");

        Div rowTotalTagihan = new Div();
        rowTotalTagihan.addClassName("rw-summary-row-total");
        Span tagihanLabel = new Span("Total Tagihan");
        tagihanLabel.addClassName("rw-tagihan-label");
        totalTagihanSpan.addClassName("rw-tagihan-value");
        rowTotalTagihan.add(tagihanLabel, totalTagihanSpan);

        Button btnCheckout = new Button("Lanjut ke Checkout");
        btnCheckout.setIcon(VaadinIcon.ARROW_RIGHT.create());
        btnCheckout.setIconAfterText(true);
        btnCheckout.addClassName("btn-checkout-cart");
        btnCheckout.addClickListener(e -> {
            long selectedCount = cartItems.stream().filter(CartItem::isSelected).count();
            if (selectedCount == 0) {
                Notification.show("Pilih setidaknya 1 barang untuk di-checkout.", 2000, Notification.Position.TOP_CENTER);
            } else {
                syncCartToSession();
                UI.getCurrent().navigate("checkout");
            }
        });

        // Security / Escrow Notice
        Div escrowNotice = new Div();
        escrowNotice.addClassName("rw-escrow-notice");
        escrowNotice.getElement().setProperty("innerHTML",
            "<svg width='18' height='18' viewBox='0 0 24 24' fill='none' stroke='#001934' stroke-width='2' stroke-linecap='round' stroke-linejoin='round' style='flex-shrink:0;margin-top:2px;'><path d='M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z'/></svg>" +
            "<span>Transaksi dilindungi oleh ReWear Escrow. Biaya layanan disesuaikan secara transparan demi keamanan transaksi Anda.</span>"
        );

        summaryCard.add(summaryTitle, rowTotalHarga, rowDiskon, rowBiaya, divider, rowTotalTagihan, btnCheckout, escrowNotice);

        rightDiv.add(voucherCard, summaryCard);
        return rightDiv;
    }

    private Div createSummaryRow(Component left, Component right) {
        Div row = new Div(left, right);
        row.addClassName("rw-summary-row");
        return row;
    }

    private void recalculateTotal() {
        double subtotal = 0;
        int count = 0;
        boolean hasRegularItem = false;

        for (CartItem item : cartItems) {
            if (item.isSelected()) {
                subtotal += item.getPrice() * item.getQuantity();
                count += item.getQuantity();
                if (!item.isSmkn24Item()) {
                    hasRegularItem = true;
                }
            }
        }

        // Biaya layanan dinamis: Bebas Biaya untuk Pasar SMKN 24, Rp 2.500 untuk Pasar Umum
        if (count == 0) {
            dynamicServiceFee = 0;
            biayaLayananSpan.setText("Rp 0");
        } else if (!hasRegularItem) {
            dynamicServiceFee = 0;
            biayaLayananSpan.setText("Gratis (SMKN 24)");
            biayaLayananSpan.getElement().getStyle().set("color", "#16A34A").set("font-weight", "700");
        } else {
            dynamicServiceFee = 2500;
            biayaLayananSpan.setText("Rp 2.500");
            biayaLayananSpan.getElement().getStyle().set("color", "#001934").set("font-weight", "700");
        }

        totalCountSpan.setText("Total Harga (" + count + " barang)");
        totalHargaSpan.setText("Rp " + String.format("%,.0f", subtotal));
        diskonSpan.setText("-Rp " + String.format("%,.0f", discountAmount));

        double totalTagihan = subtotal > 0 ? Math.max(0, subtotal - discountAmount + dynamicServiceFee) : 0;
        totalTagihanSpan.setText("Rp " + String.format("%,.0f", totalTagihan));
    }

    private List<CartItem> loadCartFromDatabase() {
        User user = AuthGuard.getCurrentUser();
        if (user == null) return new ArrayList<>();
        List<CartItemEntity> entities = cartService.getCartItems(user);
        List<CartItem> list = cartService.convertToUiCartItemList(entities);
        syncCartToSession(list);
        return list;
    }

    private void syncCartToSession() {
        syncCartToSession(this.cartItems);
    }

    private void syncCartToSession(List<CartItem> items) {
        if (VaadinSession.getCurrent() != null) {
            VaadinSession.getCurrent().setAttribute(SESSION_CART_KEY, items);
        }
        MainLayout.reloadCartBadge(UI.getCurrent());
    }
}
