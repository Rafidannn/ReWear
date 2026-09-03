package com.example.application.views.order;

import com.example.application.config.WebMvcConfig;
import com.example.application.model.order.*;
import com.example.application.model.product.Product;
import com.example.application.model.user.Address;
import com.example.application.model.user.User;
import com.example.application.service.order.CartService;
import com.example.application.service.order.OrderService;
import com.example.application.service.payment.PaymentService;
import com.example.application.service.product.ProductService;
import com.example.application.model.user.VerificationStatus;
import com.example.application.service.user.AddressService;
import com.example.application.service.user.UserService;
import com.example.application.util.AuthGuard;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import java.util.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@PageTitle("Checkout / Pembayaran - ReWear")
@Route(value = "checkout", layout = MainLayout.class)
public class CheckoutView extends Div {

    private final CartService cartService;
    private final OrderService orderService;
    private final AddressService addressService;
    private final ProductService productService;
    private final PaymentService paymentService;
    private final UserService userService;

    // Mode checkout: true = Pasar SMKN 24 (COD Sekolah), false = Reguler/Ekspedisi
    private boolean isPasarSmkn24Mode = true;

    // Address state
    private Address selectedAddress = null;
    private boolean showAddressForm = false;

    // Form fields for new address
    private final TextField fieldNamaPenerima = new TextField("Nama Penerima");
    private final TextField fieldTelepon = new TextField("No. Telepon");
    private final TextField fieldAlamat = new TextField("Alamat Lengkap");
    private final TextField fieldKota = new TextField("Kecamatan / Kota");
    private final TextField fieldKodePos = new TextField("Kode Pos");

    private int selectedShippingIndex = 0;
    private int selectedPaymentIndex = 0;
    private String selectedTransferChannel = "GOPAY";
    private String uploadedPaymentProofPath = null;

    private final Span subtotalSpan = new Span("Rp0");
    private final Span shippingFeeSpan = new Span("Gratis");
    private final Span serviceFeeSpan = new Span("Gratis");
    private final Span totalTagihanSpan = new Span("Rp0");

    private final Div leftCol = new Div();
    private final Div rightCol = new Div();
    private final Button btnTabSmkn24 = new Button("Pasar SMKN 24 (COD Sekolah)", VaadinIcon.INSTITUTION.create());
    private final Button btnTabRegular = new Button("Barang Reguler / Ekspedisi", VaadinIcon.PACKAGE.create());

    private final Div addressSectionContainer = new Div();
    private final Div shippingSectionContainer = new Div();
    private final Div paymentSectionContainer = new Div();
    private final Div orderItemsContainer = new Div();

    private List<CartItem> allCartItems = new ArrayList<>();

    public CheckoutView(CartService cartService, OrderService orderService, AddressService addressService, ProductService productService, PaymentService paymentService, UserService userService) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.addressService = addressService;
        this.productService = productService;
        this.paymentService = paymentService;
        this.userService = userService;

        if (!AuthGuard.requireLogin(UI.getCurrent())) return;

        addClassName("rw-checkout-page");

        // Load items dari DB (via CartService)
        loadCartFromDatabase();

        // Pre-load alamat utama user
        User user = AuthGuard.getCurrentUser();
        if (user != null) {
            addressService.getPrimaryAddress(user).ifPresent(a -> this.selectedAddress = a);
        }

        Div wrapper = new Div();
        wrapper.addClassName("rw-checkout-wrapper");

        Div titleRow = new Div();
        titleRow.addClassName("rw-checkout-title-row");

        Button btnBack = new Button("Kembali", VaadinIcon.ARROW_LEFT.create());
        btnBack.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnBack.addClassName("rw-checkout-back-btn");
        btnBack.addClickListener(e -> {
            VaadinSession s = VaadinSession.getCurrent();
            if (s != null) {
                s.setAttribute("DIRECT_CHECKOUT_ITEM", null);
            }
            UI.getCurrent().getPage().getHistory().back();
        });

        H2 pageTitle = new H2("Konfirmasi Pesanan");
        pageTitle.addClassName("rw-checkout-page-title");

        titleRow.add(btnBack, pageTitle);
        wrapper.add(titleRow);

        Div toggleBar = createModeToggleBar();
        wrapper.add(toggleBar);

        Div mainGrid = new Div();
        mainGrid.addClassName("rw-checkout-grid");

        leftCol.addClassName("rw-checkout-left");
        rightCol.addClassName("rw-checkout-right");

        mainGrid.add(leftCol, rightCol);
        wrapper.add(mainGrid);
        add(wrapper);

        renderView();
    }

    private void loadCartFromDatabase() {
        User user = AuthGuard.getCurrentUser();
        if (user == null) return;

        VaadinSession session = VaadinSession.getCurrent();
        CartItem directItem = (session != null) ? (CartItem) session.getAttribute("DIRECT_CHECKOUT_ITEM") : null;

        if (directItem != null) {
            this.allCartItems = new ArrayList<>(List.of(directItem));
            this.isPasarSmkn24Mode = directItem.isSmkn24Item();
            return;
        }

        var entities = cartService.getCartItems(user);
        this.allCartItems = cartService.convertToUiCartItemList(entities);

        // Sync to session for compatibility
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

    private Div createModeToggleBar() {
        Div bar = new Div();
        bar.addClassName("rw-checkout-toggle-bar");

        btnTabSmkn24.addClassName("rw-toggle-btn");
        if (isPasarSmkn24Mode) btnTabSmkn24.addClassName("active");
        btnTabSmkn24.addClickListener(e -> {
            isPasarSmkn24Mode = true;
            selectedShippingIndex = 0;
            updateToggleStyles();
            renderView();
        });

        btnTabRegular.addClassName("rw-toggle-btn");
        if (!isPasarSmkn24Mode) btnTabRegular.addClassName("active");
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
        long smkn24Count = allCartItems.stream().filter(CartItem::isSelected).filter(CartItem::isSmkn24Item).count();
        long regularCount = allCartItems.stream().filter(CartItem::isSelected).filter(item -> !item.isSmkn24Item()).count();

        btnTabSmkn24.setText("Pasar 24 (COD)" + (smkn24Count > 0 ? " (" + smkn24Count + ")" : ""));
        btnTabRegular.setText("Barang Reguler" + (regularCount > 0 ? " (" + regularCount + ")" : ""));

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

        leftCol.add(
            createAddressSection(),
            createOrderDetailsSection(),
            createShippingSection()
        );

        rightCol.add(createRightColumn());
        updateCalculations();
    }

    // ==========================================
    // LEFT COLUMN: ADDRESS SECTION (DYNAMIC)
    // ==========================================

    private Component createAddressSection() {
        Div card = new Div();
        card.addClassName("rw-checkout-card");

        if (isPasarSmkn24Mode) {
            // COD Sekolah: tidak perlu alamat rumah
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
                "<strong>Khusus Pesanan Pasar SMKN 24:</strong> Tidak memerlukan alamat rumah. Pembeli dan penjual akan bertemu langsung di area sekolah." +
                "</div>"
            );

            card.add(header, infoBox);
        } else {
            // Reguler: Perlu alamat + fitur GPS
            Div header = new Div();
            header.addClassName("rw-checkout-card-header");
            header.getElement().setProperty("innerHTML",
                "<div style='display:flex;align-items:center;gap:8px;'>" +
                "<svg width='20' height='20' viewBox='0 0 24 24' fill='none' stroke='#001934' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><path d='M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z'/><circle cx='12' cy='10' r='3'/></svg>" +
                "<span class='rw-card-header-title'>Alamat Pengiriman</span>" +
                "</div>"
            );
            card.add(header);

            renderAddressSection(card);
        }

        return card;
    }

    private void renderAddressSection(Div card) {
        addressSectionContainer.removeAll();

        User user = AuthGuard.getCurrentUser();
        List<Address> addresses = user != null ? addressService.getAddressesByUser(user) : List.of();

        if (!addresses.isEmpty() && !showAddressForm) {
            // Tampilkan daftar alamat yang ada
            Div addressList = new Div();
            addressList.addClassName("rw-address-list");

            for (Address addr : addresses) {
                Div addrCard = new Div();
                addrCard.addClassName("rw-address-card");
                if (selectedAddress != null && selectedAddress.getId().equals(addr.getId())) {
                    addrCard.addClassName("selected");
                }

                String primaryBadge = addr.isPrimary() ? "<span class='rw-badge-utama'>UTAMA</span>" : "";
                addrCard.getElement().setProperty("innerHTML",
                    "<div class='rw-address-header-row'>" +
                    "<span class='rw-address-name-tag'>" + (addr.getLabel() != null ? addr.getLabel() : "Alamat") + " " + primaryBadge + "</span>" +
                    "</div>" +
                    "<div class='rw-address-recipient'>" + addr.getRecipientName() + " (" + addr.getRecipientPhone() + ")</div>" +
                    "<div class='rw-address-detail'>" + addr.getFullAddress() +
                    (addr.getKecamatanKotaProvinsi() != null ? ", " + addr.getKecamatanKotaProvinsi() : "") +
                    (addr.getKodePos() != null ? " " + addr.getKodePos() : "") + "</div>"
                );
                addrCard.addClickListener(e -> {
                    this.selectedAddress = addr;
                    renderView();
                });
                addressList.add(addrCard);
            }

            // Tombol tambah alamat baru
            Button btnTambahAlamat = new Button("+ Tambah Alamat Baru");
            btnTambahAlamat.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            btnTambahAlamat.getStyle().set("margin-top", "8px").set("font-size", "13px");
            btnTambahAlamat.addClickListener(e -> {
                showAddressForm = true;
                renderView();
            });

            addressSectionContainer.add(addressList, btnTambahAlamat);

        } else {
            // Tampilkan form tambah alamat + GPS
            addressSectionContainer.add(buildAddressForm(user, !addresses.isEmpty()));
        }

        card.add(addressSectionContainer);
    }

    private Div buildAddressForm(User user, boolean showCancel) {
        Div formBox = new Div();
        formBox.addClassName("rw-address-form-box");

        // Tombol GPS
        Button btnGps = new Button("Gunakan Lokasi GPS Saya", VaadinIcon.MAP_MARKER.create());
        btnGps.addClassName("rw-btn-gps");
        btnGps.getStyle()
            .set("background", "linear-gradient(135deg, #001934, #0A3D7A)")
            .set("color", "#FFFFFF")
            .set("border", "none")
            .set("border-radius", "10px")
            .set("font-weight", "700")
            .set("font-size", "13px")
            .set("padding", "10px 16px")
            .set("cursor", "pointer")
            .set("display", "flex")
            .set("align-items", "center")
            .set("gap", "8px")
            .set("width", "100%")
            .set("margin-bottom", "12px");

        Span gpsStatus = new Span();
        gpsStatus.getStyle().set("font-size", "12px").set("color", "#64748B").set("display", "block").set("margin-bottom", "12px");

        btnGps.addClickListener(e -> {
            gpsStatus.setText("Mendapatkan lokasi GPS...");
            // Jalankan Geolocation + Nominatim reverse geocoding
            UI.getCurrent().getPage().executeJs("""
                navigator.geolocation.getCurrentPosition(
                    function(pos) {
                        var lat = pos.coords.latitude;
                        var lon = pos.coords.longitude;
                        fetch('/api/geocode/reverse?lat=' + lat + '&lon=' + lon)
                        .then(function(r) { return r.json(); })
                        .then(function(data) {
                            var addr = data.address || {};
                            var road = addr.road || addr.pedestrian || addr.footway || '';
                            var houseNum = addr.house_number ? ' No. ' + addr.house_number : '';
                            var sub = addr.suburb || addr.neighbourhood || addr.village || '';
                            var city = addr.city || addr.county || addr.town || '';
                            var province = addr.state || '';
                            var postcode = addr.postcode || '';
                            var fullAddr = (road + houseNum + (sub ? ', ' + sub : '')).trim();
                            var kotaProv = (city + (province ? ', ' + province : '')).trim();
                            
                            $0.value = fullAddr;
                            $1.value = kotaProv;
                            $2.value = postcode;
                            
                            // Trigger native events so Vaadin web components sync back to server
                            ['input', 'change', 'blur'].forEach(function(evtName) {
                                $0.dispatchEvent(new Event(evtName, { bubbles: true }));
                                $1.dispatchEvent(new Event(evtName, { bubbles: true }));
                                $2.dispatchEvent(new Event(evtName, { bubbles: true }));
                            });
                        })
                        .catch(function(err) { console.error('Nominatim error:', err); });
                    },
                    function(err) { console.error('GPS error:', err); },
                    { enableHighAccuracy: true, timeout: 10000 }
                );
                """,
                fieldAlamat.getElement(),
                fieldKota.getElement(),
                fieldKodePos.getElement()
            );
            gpsStatus.setText("Lokasi berhasil dideteksi. Periksa dan lengkapi data di bawah.");
        });

        // Style input fields
        styleFormField(fieldNamaPenerima, "Contoh: Budi Santoso");
        styleFormField(fieldTelepon, "Contoh: 081234567890");
        fieldTelepon.setAllowedCharPattern("[0-9+]");
        styleFormField(fieldAlamat, "Jl. Contoh No. 1, RT/RW, Kelurahan");
        styleFormField(fieldKota, "Kecamatan, Kota / Kabupaten, Provinsi");
        styleFormField(fieldKodePos, "Contoh: 13890");

        // Interactive Draggable Map Container (Leaflet.js)
        Div mapContainer = new Div();
        mapContainer.setId("rewear-map-picker");
        mapContainer.getStyle()
            .set("width", "100%")
            .set("height", "220px")
            .set("border-radius", "12px")
            .set("border", "2px solid #E2E8F0")
            .set("margin-bottom", "14px")
            .set("overflow", "hidden")
            .set("box-shadow", "0 2px 8px rgba(0,0,0,0.06)");

        // Inject Leaflet CSS & JS dynamically
        UI.getCurrent().getPage().executeJs("""
            if (!document.getElementById('leaflet-css')) {
                var link = document.createElement('link');
                link.id = 'leaflet-css';
                link.rel = 'stylesheet';
                link.href = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.css';
                document.head.appendChild(link);
            }
            if (!window.L) {
                var script = document.createElement('script');
                script.src = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.js';
                script.onload = function() { window.initReWearMap($0, $1, $2); };
                document.head.appendChild(script);
            } else {
                setTimeout(function() { window.initReWearMap($0, $1, $2); }, 200);
            }

            window.initReWearMap = function(fieldAlamat, fieldKota, fieldKodePos) {
                var mapElem = document.getElementById('rewear-map-picker');
                if (!mapElem || mapElem._leaflet_id) return;

                var defaultLat = -6.3031;
                var defaultLon = 106.8856;
                var map = L.map('rewear-map-picker').setView([defaultLat, defaultLon], 15);
                L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                    maxZoom: 19,
                    attribution: '© OpenStreetMap'
                }).addTo(map);

                var marker = L.marker([defaultLat, defaultLon], { draggable: true }).addTo(map);
                marker.bindPopup('<b>Lokasi Pengiriman</b><br/>Geser marker atau klik peta').openPopup();

                function updateAddressFromCoords(lat, lng) {
                    marker.setLatLng([lat, lng]);
                    fetch('/api/geocode/reverse?lat=' + lat + '&lon=' + lng)
                    .then(function(r) { return r.json(); })
                    .then(function(data) {
                        var addr = data.address || {};
                        var road = addr.road || addr.pedestrian || addr.footway || addr.suburb || '';
                        var houseNum = addr.house_number ? ' No. ' + addr.house_number : '';
                        var sub = addr.suburb || addr.neighbourhood || addr.village || '';
                        var city = addr.city || addr.county || addr.town || '';
                        var province = addr.state || '';
                        var postcode = addr.postcode || '';
                        var fullAddr = (road + houseNum + (sub ? ', ' + sub : '')).trim();
                        var kotaProv = (city + (province ? ', ' + province : '')).trim();

                        if (fullAddr) fieldAlamat.value = fullAddr;
                        if (kotaProv) fieldKota.value = kotaProv;
                        if (postcode) fieldKodePos.value = postcode;

                        ['input', 'change', 'blur'].forEach(function(evt) {
                            fieldAlamat.dispatchEvent(new Event(evt, { bubbles: true }));
                            fieldKota.dispatchEvent(new Event(evt, { bubbles: true }));
                            fieldKodePos.dispatchEvent(new Event(evt, { bubbles: true }));
                        });
                    })
                    .catch(function(e) { console.error(e); });
                }

                marker.on('dragend', function(e) {
                    var coord = e.target.getLatLng();
                    updateAddressFromCoords(coord.lat, coord.lng);
                });

                map.on('click', function(e) {
                    updateAddressFromCoords(e.latlng.lat, e.latlng.lng);
                });

                window._reWearMapRef = { map: map, marker: marker, updateFn: updateAddressFromCoords };
            };
        """, fieldAlamat.getElement(), fieldKota.getElement(), fieldKodePos.getElement());

        Div row1 = new Div(fieldNamaPenerima, fieldTelepon);
        row1.getStyle().set("display", "grid").set("grid-template-columns", "1fr 1fr").set("gap", "12px");

        Div row2 = new Div(fieldAlamat);
        Div row3 = new Div(fieldKota, fieldKodePos);
        row3.getStyle().set("display", "grid").set("grid-template-columns", "1fr 120px").set("gap", "12px");

        Button btnSimpanAlamat = new Button("Simpan Alamat & Lanjutkan");
        btnSimpanAlamat.addClassName("btn-confirm-pay");
        btnSimpanAlamat.getStyle().set("width", "100%").set("margin-top", "12px");
        btnSimpanAlamat.addClickListener(e -> {
            String valNama = getFieldValue(fieldNamaPenerima);
            String valTelp = getFieldValue(fieldTelepon);
            String valAlamat = getFieldValue(fieldAlamat);
            String valKota = getFieldValue(fieldKota);
            String valKodePos = getFieldValue(fieldKodePos);

            if (valNama.isEmpty() || valAlamat.isEmpty() || valKota.isEmpty() || valTelp.isEmpty()) {
                Notification.show("Lengkapi semua field alamat yang wajib diisi.", 2500, Notification.Position.TOP_CENTER);
                return;
            }
            Address newAddr = new Address();
            newAddr.setUser(user);
            newAddr.setLabel("Alamat Pengiriman");
            newAddr.setRecipientName(valNama);
            newAddr.setRecipientPhone(valTelp);
            newAddr.setFullAddress(valAlamat);
            newAddr.setKecamatanKotaProvinsi(valKota);
            newAddr.setKodePos(valKodePos);
            // Set sebagai primary jika ini pertama kali
            List<Address> existing = addressService.getAddressesByUser(user);
            newAddr.setPrimary(existing.isEmpty());

            Address saved = addressService.saveAddress(newAddr);
            this.selectedAddress = saved;
            this.showAddressForm = false;

            // Reset form
            fieldNamaPenerima.clear(); fieldTelepon.clear();
            fieldAlamat.clear(); fieldKota.clear(); fieldKodePos.clear();

            Notification notif = Notification.show("Alamat berhasil disimpan.", 2000, Notification.Position.TOP_CENTER);
            notif.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            renderView();
        });

        formBox.add(btnGps, gpsStatus, mapContainer, row1, row2, row3, btnSimpanAlamat);

        if (showCancel) {
            Button btnBatal = new Button("Batal");
            btnBatal.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            btnBatal.getStyle().set("margin-top", "8px").set("width", "100%");
            btnBatal.addClickListener(e -> {
                showAddressForm = false;
                renderView();
            });
            formBox.add(btnBatal);
        }

        return formBox;
    }

    private void styleFormField(TextField field, String placeholder) {
        field.setPlaceholder(placeholder);
        field.setWidthFull();
        field.setValueChangeMode(com.vaadin.flow.data.value.ValueChangeMode.EAGER);
        field.getStyle().set("font-size", "13px");
    }

    private String getFieldValue(TextField field) {
        if (field == null) return "";
        String val = field.getValue();
        if (val != null && !val.isBlank()) {
            return val.trim();
        }
        String propVal = field.getElement().getProperty("value");
        return propVal != null ? propVal.trim() : "";
    }

    // ==========================================
    // ORDER DETAILS
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

        // P2.10: Tampilkan notifikasi jika ada barang terpilih di mode lain yang tidak ikut checkout
        long otherModeCount = allCartItems.stream()
            .filter(CartItem::isSelected)
            .filter(item -> isPasarSmkn24Mode ? !item.isSmkn24Item() : item.isSmkn24Item())
            .count();

        if (otherModeCount > 0) {
            String otherModeName = isPasarSmkn24Mode ? "Barang Reguler / Ekspedisi" : "Pasar SMKN 24 (COD Sekolah)";
            Div otherModeNotice = new Div();
            otherModeNotice.getStyle()
                .set("background", "#FEF3C7")
                .set("border", "1px solid #F59E0B")
                .set("border-radius", "8px")
                .set("padding", "10px 14px")
                .set("margin-bottom", "14px")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "space-between")
                .set("gap", "10px");

            Span noticeText = new Span("Ada " + otherModeCount + " barang di " + otherModeName + " yang perlu di-checkout terpisah.");
            noticeText.getStyle().set("font-size", "12px").set("font-weight", "600").set("color", "#92400E");

            Button btnSwitch = new Button("Beralih", e -> {
                isPasarSmkn24Mode = !isPasarSmkn24Mode;
                selectedShippingIndex = 0;
                updateToggleStyles();
                renderView();
            });
            btnSwitch.getStyle()
                .set("background", "#92400E").set("color", "#FFFFFF")
                .set("border", "none").set("border-radius", "6px")
                .set("font-size", "11px").set("font-weight", "700")
                .set("padding", "4px 10px").set("cursor", "pointer").set("white-space", "nowrap");

            otherModeNotice.add(noticeText, btnSwitch);
            orderItemsContainer.add(otherModeNotice);
        }

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

            Div nameDiv = new Div(item.getTitle());
            nameDiv.addClassName("rw-item-name");

            Div metaDiv = new Div(item.getVariant() + " | Qty: " + item.getQuantity());
            metaDiv.addClassName("rw-item-meta");

            infoCol.add(nameDiv, metaDiv);
            if (item.isSmkn24Item()) {
                Span badge = new Span("WARGA SMKN 24");
                badge.addClassName("rw-badge-smkn24");
                infoCol.add(badge);
            }

            Div actionCol = new Div();
            actionCol.addClassName("rw-item-action-col");

            Div priceCol = new Div("Rp" + String.format("%,.0f", item.getPrice() * item.getQuantity()));
            priceCol.addClassName("rw-item-price-col");

            Button btnCancelItem = new Button("Batalkan", VaadinIcon.CLOSE_SMALL.create());
            btnCancelItem.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            btnCancelItem.addClassName("rw-btn-cancel-checkout-item");
            btnCancelItem.addClickListener(e -> {
                item.setSelected(false);
                renderView();
                Notification.show("Item " + item.getTitle() + " dibatalkan dari checkout.", 2000, Notification.Position.TOP_CENTER);
            });

            actionCol.add(priceCol, btnCancelItem);
            itemRow.add(thumbWrap, infoCol, actionCol);
            orderItemsContainer.add(itemRow);

            if (i < selectedItems.size() - 1) {
                Hr divider = new Hr();
                divider.addClassName("rw-item-divider");
                orderItemsContainer.add(divider);
            }
        }
    }

    // ==========================================
    // SHIPPING SECTION
    // ==========================================

    private Component createShippingSection() {
        Div card = new Div();
        card.addClassName("rw-checkout-card");

        Div header = new Div();
        header.addClassName("rw-checkout-card-header");
        header.getElement().setProperty("innerHTML",
            "<div style='display:flex;align-items:center;gap:8px;'>" +
            "<svg width='20' height='20' viewBox='0 0 24 24' fill='none' stroke='#001934' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><rect x='1' y='3' width='15' height='13'/><polygon points='16 8 20 8 23 11 23 16 16 16 16 8'/><circle cx='5.5' cy='18.5' r='2.5'/><circle cx='18.5' cy='18.5' r='2.5'/></svg>" +
            "<span class='rw-card-header-title'>Opsi Pengiriman</span>" +
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
            Div opt0 = new Div();
            opt0.addClassNames("rw-shipping-card", "selected");
            opt0.getElement().setProperty("innerHTML",
                "<div class='rw-shipping-title-row'>" +
                "<span class='rw-shipping-title'>Ambil Sendiri / COD Sekolah</span>" +
                "<span class='rw-shipping-badge-free'>Gratis</span>" +
                "</div>" +
                "<div class='rw-shipping-desc'>Titik temu langsung di Lobby SMKN 24 Jakarta. Estimasi 1 jam setelah konfirmasi penjual.</div>"
            );
            shippingSectionContainer.add(opt0);
        } else {
            Div opt0 = new Div();
            opt0.addClassName("rw-shipping-card");
            if (selectedShippingIndex == 0) opt0.addClassName("selected");
            opt0.getElement().setProperty("innerHTML",
                "<div class='rw-shipping-title-row'><span class='rw-shipping-title'>Ambil Sendiri (Gratis)</span><span class='rw-shipping-badge-free'>Gratis</span></div>" +
                "<div class='rw-shipping-desc'>Ambil barang di lokasi penjual.</div>"
            );
            opt0.addClickListener(e -> { selectedShippingIndex = 0; renderShippingOptions(); updateCalculations(); });

            Div opt1 = new Div();
            opt1.addClassName("rw-shipping-card");
            if (selectedShippingIndex == 1) opt1.addClassName("selected");
            opt1.getElement().setProperty("innerHTML",
                "<div class='rw-shipping-title-row'><span class='rw-shipping-title'>Instan (Gojek/Grab - Max 10km)</span><span class='rw-shipping-price'>Rp22.000</span></div>" +
                "<div class='rw-shipping-desc'>Pengiriman kurir instan cepat tiba (1-2 jam).</div>"
            );
            opt1.addClickListener(e -> { selectedShippingIndex = 1; renderShippingOptions(); updateCalculations(); });

            Div opt2 = new Div();
            opt2.addClassName("rw-shipping-card");
            if (selectedShippingIndex == 2) opt2.addClassName("selected");
            opt2.getElement().setProperty("innerHTML",
                "<div class='rw-shipping-title-row'><span class='rw-shipping-title'>Reguler (JNE/J&T Ekspedisi Nasional)</span><span class='rw-shipping-price'>Rp9.000</span></div>" +
                "<div class='rw-shipping-desc'>Pengiriman ekspedisi ke seluruh Indonesia (2-3 hari kerja).</div>"
            );
            opt2.addClickListener(e -> { selectedShippingIndex = 2; renderShippingOptions(); updateCalculations(); });

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

        Div noteBanner = new Div();
        noteBanner.addClassName("rw-payment-note-banner");
        noteBanner.getElement().setProperty("innerHTML",
            "<svg width='16' height='16' viewBox='0 0 24 24' fill='none' stroke='#001934' stroke-width='2' style='flex-shrink:0;'><circle cx='12' cy='12' r='10'/><line x1='12' y1='16' x2='12' y2='12'/><line x1='12' y1='8' x2='12.01' y2='8'/></svg>" +
            (isPasarSmkn24Mode
                ? "<span><strong>COD Sekolah:</strong> Bebas biaya pengiriman & layanan bagi komunitas SMKN 24.</span>"
                : "<span><strong>ReWear Escrow Protection:</strong> Dana ditahan sistem hingga barang diterima.</span>")
        );

        Div summaryRows = new Div();
        summaryRows.addClassName("rw-checkout-summary-rows");
        summaryRows.add(
            createRowSpan("Subtotal Barang", subtotalSpan),
            createRowSpan("Biaya Pengiriman", shippingFeeSpan),
            createRowSpan("Biaya Layanan", serviceFeeSpan)
        );

        Div rowTotal = new Div();
        rowTotal.addClassName("rw-checkout-total-row");
        Span totalLabel = new Span("Total Tagihan");
        totalLabel.addClassName("rw-checkout-total-label");
        totalTagihanSpan.addClassName("rw-checkout-total-val");
        rowTotal.add(totalLabel, totalTagihanSpan);

        Button btnConfirm = new Button("Konfirmasi Bayar");
        btnConfirm.addClassName("btn-confirm-pay");
        btnConfirm.addClickListener(e -> {
            List<CartItem> selected = getSelectedItems();
            if (selected.isEmpty()) {
                Notification.show("Tidak ada barang yang terpilih untuk di-checkout.", 2500, Notification.Position.TOP_CENTER);
                return;
            }
            if (!isPasarSmkn24Mode && selectedAddress == null) {
                Notification.show("Pilih atau tambahkan alamat pengiriman terlebih dahulu.", 2500, Notification.Position.TOP_CENTER);
                return;
            }
            openOrderValidationDialog(selected);
        });

        Paragraph finePrint = new Paragraph("Dengan menekan tombol di atas, Anda menyetujui Syarat & Ketentuan transaksi di ReWear.");
        finePrint.addClassName("rw-checkout-fine-print");

        payCard.add(payTitle, paymentSectionContainer, noteBanner, summaryRows, rowTotal, btnConfirm, finePrint);

        Div escrowBox = new Div();
        escrowBox.addClassName("rw-escrow-protected-box");
        escrowBox.getElement().setProperty("innerHTML",
            "<svg width='22' height='22' viewBox='0 0 24 24' fill='none' stroke='#001934' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><path d='M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z'/></svg>" +
            "<div><div style='font-size:12px;font-weight:800;color:#001934;letter-spacing:0.5px;'>ESCROW PROTECTED</div>" +
            "<div style='font-size:12px;color:#475569;'>Transaksi dilindungi oleh sistem keamanan SMKN 24.</div></div>"
        );

        rightDiv.add(payCard, escrowBox);
        return rightDiv;
    }

    private void renderPaymentOptions() {
        paymentSectionContainer.removeAll();

        Div grid = new Div();
        grid.getStyle().set("display", "grid").set("grid-template-columns", "repeat(auto-fit, minmax(220px, 1fr))").set("gap", "12px").set("margin-bottom", "14px");

        Div pay0 = buildPayCard(0, "blue-wrap",
            "<path d='M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z'/>",
            "#1E40AF", "ReWear Pay", "rw-pay-badge-blue", "Saldo Escrow Instan",
            "Bayar langsung dari saldo akun ReWear Anda.", 0);

        Div pay1 = buildPayCard(1, "gold-wrap",
            "<rect x='3' y='3' width='18' height='18' rx='2'/><path d='M7 7h3v3H7zM14 7h3v3h-3zM7 14h3v3H7z'/>",
            "#B45309", "QRIS Statis SMKN 24", "rw-pay-badge-gold", "GoPay/Dana/BCA",
            "Scan QRIS instan & upload struk bukti bayar.", 1);

        Div pay2 = buildPayCard(2, "purple-wrap",
            "<rect x='2' y='5' width='20' height='14' rx='2'/><line x1='2' y1='10' x2='22' y2='10'/>",
            "#7C3AED", "Transfer Bank & E-Wallet", "rw-pay-badge-blue", "GoPay / BCA / Mandiri",
            "Transfer ke rekening / e-wallet & upload bukti.", 2);

        grid.add(pay0, pay1, pay2);
        paymentSectionContainer.add(grid);

        // Sub-panel for QRIS or Transfer
        if (selectedPaymentIndex == 1) {
            // QRIS Section
            Div qrisPanel = new Div();
            qrisPanel.getStyle()
                .set("background", "#F8FAFC").set("border", "1.5px solid #CBD5E1")
                .set("border-radius", "12px").set("padding", "16px").set("margin-top", "12px");

            H5 qrisTitle = new H5("Scan QRIS ReWear SMKN 24");
            qrisTitle.getStyle().set("margin", "0 0 6px 0").set("color", "#001934").set("font-weight", "800");
            Paragraph qrisDesc = new Paragraph("Gunakan GoPay, OVO, DANA, ShopeePay, BCA Mobile, atau aplikasi QRIS lainnya. Setelah pembayaran berhasil, unggah foto bukti transfer di bawah agar admin dapat memverifikasi.");
            qrisDesc.getStyle().set("font-size", "13px").set("color", "#475569").set("margin", "0 0 14px 0");

            HorizontalLayout qrisBody = new HorizontalLayout();
            qrisBody.setSpacing(true);
            qrisBody.setAlignItems(FlexComponent.Alignment.CENTER);

            Image qrisImg = new Image("/images/qris.png", "QRIS ReWear");
            qrisImg.getStyle().set("width", "160px").set("height", "160px").set("object-fit", "contain")
                .set("border-radius", "8px").set("border", "1px solid #CBD5E1").set("background", "#FFFFFF").set("padding", "6px");

            VerticalLayout uploadLayout = new VerticalLayout();
            uploadLayout.setSpacing(true);
            uploadLayout.setPadding(false);

            Span uploadLabel = new Span("Unggah Foto Bukti Transfer / Struk QRIS:");
            uploadLabel.getStyle().set("font-size", "13px").set("font-weight", "700").set("color", "#001934");

            MemoryBuffer buffer = new MemoryBuffer();
            Upload upload = new Upload(buffer);
            upload.setAcceptedFileTypes("image/jpeg", "image/png", "image/webp");
            upload.setMaxFileSize(5 * 1024 * 1024);

            Image proofPreview = new Image();
            proofPreview.setVisible(false);
            proofPreview.getStyle().set("width", "80px").set("height", "80px").set("object-fit", "cover").set("border-radius", "6px").set("border", "1px solid #CBD5E1");

            upload.addSucceededListener(event -> {
                try {
                    // P1.5: Sanitasi ekstensi dari MIME type
                    String ext = sanitizeExtension(event.getMIMEType());
                    if (ext == null) {
                        Notification.show("Format file tidak didukung. Gunakan JPG, PNG, atau WEBP.", 3000, Notification.Position.TOP_CENTER);
                        return;
                    }
                    String fileName = "qris_proof_" + System.currentTimeMillis() + ext;
                    java.io.File uploadDir = new java.io.File(WebMvcConfig.PROOFS_BASE_DIR);
                    if (!uploadDir.exists()) uploadDir.mkdirs();
                    java.io.File destFile = new java.io.File(uploadDir, fileName);
                    try (java.io.InputStream in = buffer.getInputStream();
                         java.io.FileOutputStream out = new java.io.FileOutputStream(destFile)) {
                        in.transferTo(out);
                    }
                    uploadedPaymentProofPath = "api/payment-proofs/" + fileName;
                    proofPreview.setSrc("/" + uploadedPaymentProofPath);
                    proofPreview.setVisible(true);
                    Notification.show("Bukti pembayaran berhasil diunggah!", 2500, Notification.Position.TOP_CENTER);
                } catch (Exception ex) {
                    Notification.show("Gagal mengunggah foto: " + ex.getMessage(), 3000, Notification.Position.TOP_CENTER);
                }
            });

            uploadLayout.add(uploadLabel, upload, proofPreview);
            qrisBody.add(qrisImg, uploadLayout);
            qrisPanel.add(qrisTitle, qrisDesc, qrisBody);
            paymentSectionContainer.add(qrisPanel);

        } else if (selectedPaymentIndex == 2) {
            // Transfer Bank & E-Wallet Section
            Div tfPanel = new Div();
            tfPanel.getStyle()
                .set("background", "#F8FAFC").set("border", "1.5px solid #CBD5E1")
                .set("border-radius", "12px").set("padding", "16px").set("margin-top", "12px");

            H5 tfTitle = new H5("Pilih Channel Transfer (E-Wallet / Rekening Bank)");
            tfTitle.getStyle().set("margin", "0 0 6px 0").set("color", "#001934").set("font-weight", "800");

            ComboBox<String> channelCombo = new ComboBox<>("Pilih Rekening / E-Wallet Tujuan");
            List<String> channels = paymentService.getTransferChannels();
            channelCombo.setItems(channels);
            if (!channels.isEmpty()) {
                channelCombo.setValue(channels.get(0));
                selectedTransferChannel = channels.get(0).split(" - ")[0].trim();
            }
            channelCombo.setWidthFull();
            channelCombo.addValueChangeListener(e -> {
                if (e.getValue() != null) {
                    selectedTransferChannel = e.getValue().split(" - ")[0].trim();
                }
            });

            Div infoBox = new Div();
            infoBox.getStyle().set("background", "#EFF6FF").set("padding", "10px 14px").set("border-radius", "8px").set("margin", "12px 0")
                .set("font-size", "13px").set("color", "#1E40AF").set("border", "1px solid #BFDBFE");
            infoBox.setText("Silakan transfer sesuai nominal total pesanan ke nomor akun di atas, kemudian unggah foto bukti transfer struk di bawah:");

            VerticalLayout uploadLayout = new VerticalLayout();
            uploadLayout.setSpacing(true);
            uploadLayout.setPadding(false);

            Span uploadLabel = new Span("Unggah Foto Bukti Transfer Struk:");
            uploadLabel.getStyle().set("font-size", "13px").set("font-weight", "700").set("color", "#001934");

            MemoryBuffer buffer = new MemoryBuffer();
            Upload upload = new Upload(buffer);
            upload.setAcceptedFileTypes("image/jpeg", "image/png", "image/webp");
            upload.setMaxFileSize(5 * 1024 * 1024);

            Image proofPreview = new Image();
            proofPreview.setVisible(false);
            proofPreview.getStyle().set("width", "80px").set("height", "80px").set("object-fit", "cover").set("border-radius", "6px").set("border", "1px solid #CBD5E1");

            upload.addSucceededListener(event -> {
                try {
                    // P1.5: Sanitasi ekstensi dari MIME type
                    String ext = sanitizeExtension(event.getMIMEType());
                    if (ext == null) {
                        Notification.show("Format file tidak didukung. Gunakan JPG, PNG, atau WEBP.", 3000, Notification.Position.TOP_CENTER);
                        return;
                    }
                    String fileName = "tf_proof_" + System.currentTimeMillis() + ext;
                    java.io.File uploadDir = new java.io.File(WebMvcConfig.PROOFS_BASE_DIR);
                    if (!uploadDir.exists()) uploadDir.mkdirs();
                    java.io.File destFile = new java.io.File(uploadDir, fileName);
                    try (java.io.InputStream in = buffer.getInputStream();
                         java.io.FileOutputStream out = new java.io.FileOutputStream(destFile)) {
                        in.transferTo(out);
                    }
                    uploadedPaymentProofPath = "api/payment-proofs/" + fileName;
                    proofPreview.setSrc("/" + uploadedPaymentProofPath);
                    proofPreview.setVisible(true);
                    Notification.show("Bukti pembayaran berhasil diunggah!", 2500, Notification.Position.TOP_CENTER);
                } catch (Exception ex) {
                    Notification.show("Gagal mengunggah foto: " + ex.getMessage(), 3000, Notification.Position.TOP_CENTER);
                }
            });

            uploadLayout.add(uploadLabel, upload, proofPreview);
            tfPanel.add(tfTitle, channelCombo, infoBox, uploadLayout);
            paymentSectionContainer.add(tfPanel);
        }
    }

    private Div buildPayCard(int index, String wrapClass, String svgPath, String stroke,
                              String title, String badgeClass, String badgeText, String desc, int cardIndex) {
        Div card = new Div();
        card.addClassName("rw-pay-card");
        if (selectedPaymentIndex == cardIndex) card.addClassName("selected");
        card.getElement().setProperty("innerHTML",
            "<div class='rw-pay-icon-wrap " + wrapClass + "'>" +
            "<svg width='22' height='22' viewBox='0 0 24 24' fill='none' stroke='" + stroke + "' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'>" + svgPath + "</svg>" +
            "</div>" +
            "<div class='rw-pay-title'>" + title + "</div>" +
            "<span class='" + badgeClass + "'>" + badgeText + "</span>" +
            "<div class='rw-pay-subtext'>" + desc + "</div>"
        );
        card.addClickListener(e -> {
            selectedPaymentIndex = cardIndex;
            renderPaymentOptions();
        });
        return card;
    }

    // ==========================================
    // VALIDATION DIALOG + SAVE TO DB
    // ==========================================

    private void openOrderValidationDialog(List<CartItem> itemsToPay) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Verifikasi & Konfirmasi Pesanan");
        dialog.setWidth("520px");

        Div body = new Div();
        body.getStyle().set("padding", "8px 0");

        Paragraph subhead = new Paragraph("Pastikan rincian pesanan, alamat, dan pembayaran sudah sesuai:");
        subhead.getStyle().set("font-size", "14px").set("color", "#64748B").set("margin-bottom", "16px");
        body.add(subhead);

        // Ringkasan item
        Div itemsBox = new Div();
        itemsBox.getStyle()
            .set("background", "#F8FAFC").set("border", "1px solid #E2E8F0")
            .set("border-radius", "8px").set("padding", "12px 16px").set("margin-bottom", "14px");

        Div itemsHeader = new Div();
        itemsHeader.getStyle().set("display", "flex").set("align-items", "center").set("gap", "8px").set("margin-bottom", "8px");

        Icon pkgIcon = VaadinIcon.PACKAGE.create();
        pkgIcon.getElement().getStyle().set("width", "16px").set("height", "16px").set("color", "#001934");

        H5 itemsTitle = new H5("Barang yang Dibeli (" + itemsToPay.size() + " produk):");
        itemsTitle.getStyle().set("margin", "0").set("color", "#001934").set("font-weight", "700");
        itemsHeader.add(pkgIcon, itemsTitle);
        itemsBox.add(itemsHeader);

        for (CartItem item : itemsToPay) {
            Div itemRow = new Div();
            itemRow.getStyle().set("display", "flex").set("justify-content", "space-between")
                .set("font-size", "13px").set("margin-bottom", "6px");
            Span name = new Span("• " + item.getTitle() + " (x" + item.getQuantity() + ")");
            name.getStyle().set("color", "#1E293B").set("font-weight", "600");
            Span price = new Span("Rp " + String.format("%,.0f", item.getPrice() * item.getQuantity()));
            price.getStyle().set("color", "#001934").set("font-weight", "700");
            itemRow.add(name, price);
            itemsBox.add(itemRow);
        }
        body.add(itemsBox);

        // Alamat & pengiriman
        Div shipBox = new Div();
        shipBox.getStyle()
            .set("background", "#F8FAFC").set("border", "1px solid #E2E8F0")
            .set("border-radius", "8px").set("padding", "12px 16px").set("margin-bottom", "14px");

        String locText = isPasarSmkn24Mode
            ? "Lobby / Kantin Utama SMKN 24 Jakarta (COD Sekolah)"
            : (selectedAddress != null ? selectedAddress.getRecipientName() + " - " + selectedAddress.getFullAddress() : "Alamat belum dipilih");
        String shipText = isPasarSmkn24Mode ? "COD Ambil Sendiri (Bebas Ongkir)"
            : (selectedShippingIndex == 0 ? "Ambil Sendiri (Gratis)" : selectedShippingIndex == 1 ? "Instan Gojek/Grab (Rp22.000)" : "Reguler JNE/J&T (Rp9.000)");

        shipBox.add(
            buildDialogRow(VaadinIcon.MAP_MARKER, "Tujuan", locText),
            buildDialogRow(VaadinIcon.TRUCK, "Pengiriman", shipText)
        );
        body.add(shipBox);

        // Pembayaran & total
        Div payBox = new Div();
        payBox.getStyle()
            .set("background", "#EFF6FF").set("border", "1px solid #BFDBFE")
            .set("border-radius", "8px").set("padding", "12px 16px");

        String payText = selectedPaymentIndex == 0 ? "ReWear Pay (Saldo Otomatis)"
            : selectedPaymentIndex == 1 ? "QRIS Instan SMKN 24"
            : "Transfer (" + selectedTransferChannel + ")";

        payBox.add(
            buildDialogRow(VaadinIcon.CREDIT_CARD, "Pembayaran", payText),
            buildDialogRow(VaadinIcon.MONEY, "Total Tagihan", totalTagihanSpan.getText())
        );

        if (selectedPaymentIndex != 0) {
            String proofStatus = (uploadedPaymentProofPath != null && !uploadedPaymentProofPath.isBlank())
                ? "Foto Bukti Terlampir ✓" : "Belum Dilampirkan (Bisa diunggah di Riwayat Pesanan)";
            payBox.add(buildDialogRow(VaadinIcon.PICTURE, "Bukti Transfer", proofStatus));
        }

        body.add(payBox);

        dialog.add(body);

        Button btnBack = new Button("Periksa Kembali", e -> dialog.close());
        btnBack.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button btnProceed = new Button("Ya, Pesanan Sesuai & Konfirmasi", VaadinIcon.CHECK_CIRCLE.create(), e -> {
            dialog.close();
            saveOrderToDatabase(itemsToPay);
        });
        btnProceed.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnProceed.getStyle().set("background", "#001934").set("color", "#FFFFFF");

        dialog.getFooter().add(btnBack, btnProceed);
        dialog.open();
    }

    private Div buildDialogRow(VaadinIcon icon, String label, String value) {
        Div row = new Div();
        row.getStyle()
            .set("display", "flex")
            .set("align-items", "center")
            .set("gap", "8px")
            .set("font-size", "13px")
            .set("margin-bottom", "6px")
            .set("color", "#334155");

        Icon ic = icon.create();
        ic.getElement().getStyle()
            .set("width", "16px")
            .set("height", "16px")
            .set("color", "#001934")
            .set("flex-shrink", "0");

        Span lbl = new Span(label + ":");
        lbl.getStyle().set("font-weight", "600").set("color", "#64748B");

        Span val = new Span(value);
        val.getStyle().set("font-weight", "700").set("color", "#001934");

        row.add(ic, lbl, val);
        return row;
    }

    // ==========================================
    // SAVE ORDER TO DATABASE
    // ==========================================

    private void saveOrderToDatabase(List<CartItem> itemsToPay) {
        User buyer = AuthGuard.getCurrentUser();
        if (buyer == null) {
            Notification.show("Sesi login habis. Silakan login kembali.", 3000, Notification.Position.TOP_CENTER);
            return;
        }

        try {
            // P1.4: Validasi status verifikasi sekolah untuk mode Pasar SMKN 24
            if (isPasarSmkn24Mode) {
                boolean isVerified = userService.isSchoolVerified(buyer);
                if (!isVerified) {
                    Notification.show("Hanya warga SMKN 24 yang sudah terverifikasi dapat bertransaksi di Pasar SMKN 24. Silakan ajukan verifikasi di profil Anda.", 4500, Notification.Position.TOP_CENTER);
                    return;
                }
            }

            // P1.3: Cek saldo jika memilih ReWear Pay
            double grandTotalForBalance = 0;
            if (selectedPaymentIndex == 0) {
                for (CartItem item : itemsToPay) {
                    grandTotalForBalance += item.getPrice() * item.getQuantity();
                }
                if (!isPasarSmkn24Mode) {
                    grandTotalForBalance += (selectedShippingIndex == 1 ? 22000 : selectedShippingIndex == 2 ? 9000 : 0);
                    grandTotalForBalance += Math.max(2500, grandTotalForBalance * 0.01);
                }
                BigDecimal totalBD = BigDecimal.valueOf(grandTotalForBalance);
                BigDecimal currentBalance = buyer.getBalance() != null ? buyer.getBalance() : BigDecimal.ZERO;
                if (currentBalance.compareTo(totalBD) < 0) {
                    Notification.show(
                        "Saldo ReWear Pay tidak mencukupi. Saldo Anda: Rp " +
                        String.format("%,.0f", currentBalance.doubleValue()) +
                        ". Dibutuhkan: Rp " + String.format("%,.0f", grandTotalForBalance),
                        4500, Notification.Position.TOP_CENTER);
                    return;
                }
                // Potong saldo buyer
                buyer.setBalance(currentBalance.subtract(totalBD));
                userService.saveUser(buyer);
            }

            // Tentukan alamat pengiriman sebagai string
            String shippingAddressStr = isPasarSmkn24Mode
                ? "COD Sekolah - Lobby / Kantin Utama SMKN 24 Jakarta, Jl. Bambu Apus No. 24, Cipayung, Jakarta Timur"
                : (selectedAddress != null
                    ? selectedAddress.getRecipientName() + " | " + selectedAddress.getFullAddress()
                    + (selectedAddress.getKecamatanKotaProvinsi() != null ? ", " + selectedAddress.getKecamatanKotaProvinsi() : "")
                    + (selectedAddress.getKodePos() != null ? " " + selectedAddress.getKodePos() : "")
                    : "Alamat tidak tersedia");

            // Group items by seller so multi-seller checkouts create separate orders per seller
            var entities = cartService.getCartItems(buyer);
            Map<User, List<CartItem>> itemsBySeller = new HashMap<>();

            for (CartItem item : itemsToPay) {
                User seller = findSellerForCartItem(item, entities);
                if (seller == null) seller = buyer;
                itemsBySeller.computeIfAbsent(seller, k -> new ArrayList<>()).add(item);
            }

            int orderCount = 0;
            String lastOrderNumber = "";

            for (Map.Entry<User, List<CartItem>> entry : itemsBySeller.entrySet()) {
                User seller = entry.getKey();
                List<CartItem> sellerItems = entry.getValue();

                double subtotalVal = sellerItems.stream().mapToDouble(i -> i.getPrice() * i.getQuantity()).sum();
                double shippingCostVal = isPasarSmkn24Mode ? 0 : (selectedShippingIndex == 0 ? 0 : selectedShippingIndex == 1 ? 22000 : 9000);
                double serviceFeeVal = isPasarSmkn24Mode ? 0 : Math.max(2500, subtotalVal * 0.01);
                double totalVal = subtotalVal + shippingCostVal + serviceFeeVal;

                String paymentMethodStr = selectedPaymentIndex == 0 ? "REWEAR_PAY"
                    : selectedPaymentIndex == 1 ? "QRIS"
                    : "TRANSFER_" + selectedTransferChannel;

                ShippingMethod shippingMethod = isPasarSmkn24Mode ? ShippingMethod.COD_SEKOLAH
                    : (selectedShippingIndex == 0 ? ShippingMethod.LAINNYA : selectedShippingIndex == 1 ? ShippingMethod.GOSEND : ShippingMethod.EKSPEDISI);

                Order order = new Order();
                order.setBuyer(buyer);
                order.setSeller(seller);
                order.setOrderNumber("RW-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                order.setShippingAddress(shippingAddressStr);
                order.setSubtotal(BigDecimal.valueOf(subtotalVal));
                order.setShippingCost(BigDecimal.valueOf(shippingCostVal));
                order.setAdminFeeAmount(BigDecimal.valueOf(serviceFeeVal));
                order.setTotalAmount(BigDecimal.valueOf(totalVal));
                order.setPaymentMethod(paymentMethodStr);
                order.setShippingMethod(shippingMethod);

                if (selectedPaymentIndex == 0) {
                    // ReWear Pay Saldo Langsung
                    order.setStatus(OrderStatus.DIPROSES);
                } else {
                    // QRIS atau Transfer Bank & E-Wallet: Menunggu Pembayaran & Verifikasi Admin
                    order.setStatus(OrderStatus.MENUNGGU_PEMBAYARAN);
                }

                List<OrderItem> orderItems = new ArrayList<>();
                for (CartItem cartItem : sellerItems) {
                    OrderItem oi = new OrderItem();
                    oi.setProductNameSnapshot(cartItem.getTitle());
                    oi.setPriceSnapshot(BigDecimal.valueOf(cartItem.getPrice()));
                    oi.setQuantity(cartItem.getQuantity());

                    // Link Product entity precisely by Product ID
                    if (cartItem.getProductId() != null) {
                        productService.findById(cartItem.getProductId()).ifPresent(oi::setProduct);
                    }
                    if (oi.getProduct() == null) {
                        for (var entity : entities) {
                            if (entity.getProduct() != null && entity.getProduct().getId() != null
                                    && entity.getProduct().getId().equals(cartItem.getProductId())) {
                                oi.setProduct(entity.getProduct());
                                break;
                            }
                        }
                    }
                    // Fallback to name matching if product ID not matched
                    if (oi.getProduct() == null) {
                        for (var entity : entities) {
                            if (entity.getProduct() != null && entity.getProduct().getName() != null
                                    && entity.getProduct().getName().equals(cartItem.getTitle())) {
                                oi.setProduct(entity.getProduct());
                                break;
                            }
                        }
                    }

                    orderItems.add(oi);
                }

                orderService.createOrder(order, orderItems, buyer);

                // Create Payment Record
                String gateway = selectedPaymentIndex == 0 ? "SALDO" : selectedPaymentIndex == 1 ? "QRIS" : selectedTransferChannel;
                paymentService.createOrUpdatePayment(order, paymentMethodStr, gateway, uploadedPaymentProofPath, BigDecimal.valueOf(totalVal));

                orderCount++;
                lastOrderNumber = order.getOrderNumber();
            }

            // Hapus item yang sudah di-checkout dari cart DB jika bukan direct checkout
            for (CartItem cartItem : itemsToPay) {
                if (cartItem.getId() != null && !cartItem.getId().startsWith("direct-")) {
                    try {
                        cartService.removeFromCart(Long.parseLong(cartItem.getId()));
                    } catch (Exception ignored) {}
                }
            }

            VaadinSession session = VaadinSession.getCurrent();
            if (session != null) {
                session.setAttribute("DIRECT_CHECKOUT_ITEM", null);
            }

            // P2.2: Tampilkan sukses dan redirect
            String notifMsg;
            if (selectedPaymentIndex == 0) {
                notifMsg = "Pesanan Berhasil Dibuat! Saldo ReWear Pay Anda telah dipotong sebesar Rp "
                    + String.format("%,.0f", grandTotalForBalance) + ". Order #" + lastOrderNumber;
            } else if (uploadedPaymentProofPath != null && !uploadedPaymentProofPath.isBlank()) {
                notifMsg = "Pesanan Berhasil Dibuat! Bukti pembayaran telah terlampir dan sedang diverifikasi Admin.";
            } else {
                notifMsg = "Pesanan Berhasil Dibuat! Silakan transfer dan unggah bukti pembayaran di Riwayat Pesanan.";
            }

            Notification successNotif = Notification.show(notifMsg, 4000, Notification.Position.TOP_CENTER);
            successNotif.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            UI.getCurrent().navigate("orders");

        } catch (Exception ex) {
            Notification.show("Gagal membuat pesanan: " + ex.getMessage(), 4000, Notification.Position.TOP_CENTER);
        }
    }

    private User findSellerForCartItem(CartItem cartItem, List<CartItemEntity> entities) {
        if (cartItem == null) return null;

        if (cartItem.getProductId() != null) {
            Product p = productService.findById(cartItem.getProductId()).orElse(null);
            if (p != null && p.getSeller() != null) return p.getSeller();
        }

        if (cartItem.getId() == null) return null;

        try {
            long cartItemId = Long.parseLong(cartItem.getId());
            for (var entity : entities) {
                if (entity.getId() != null && entity.getId().equals(cartItemId)) {
                    if (entity.getProduct() != null && entity.getProduct().getSeller() != null) {
                        return entity.getProduct().getSeller();
                    }
                }
            }
        } catch (NumberFormatException ignored) {}

        // Fallback: match by product title
        for (var entity : entities) {
            if (entity.getProduct() != null
                    && entity.getProduct().getName() != null
                    && entity.getProduct().getName().equals(cartItem.getTitle())
                    && entity.getProduct().getSeller() != null) {
                return entity.getProduct().getSeller();
            }
        }
        return null;
    }

    @SuppressWarnings("unused")
    private User getSellerFromFirstItem(List<CartItem> items) {
        User currentUser = AuthGuard.getCurrentUser();
        if (currentUser == null) return null;
        var entities = cartService.getCartItems(currentUser);

        for (CartItem cartItem : items) {
            User s = findSellerForCartItem(cartItem, entities);
            if (s != null) return s;
        }
        return null;
    }

    // ==========================================
    // UTILITY
    // ==========================================

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
            shippingFeeSpan.setText("Rp0");
            serviceFeeSpan.setText("Rp0");
        } else if (isPasarSmkn24Mode) {
            shippingFeeSpan.setText("Gratis");
            shippingFeeSpan.getElement().getStyle().set("color", "#16A34A").set("font-weight", "700");
            serviceFeeSpan.setText("Gratis (SMKN 24)");
            serviceFeeSpan.getElement().getStyle().set("color", "#16A34A").set("font-weight", "700");
        } else {
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
            currentServiceFee = Math.max(2500, currentSubtotal * 0.01);
            serviceFeeSpan.setText("Rp" + String.format("%,.0f", currentServiceFee));
            serviceFeeSpan.getElement().getStyle().set("color", "#001934").set("font-weight", "700");
        }

        double total = currentSubtotal > 0 ? (currentSubtotal + currentShippingFee + currentServiceFee) : 0;
        totalTagihanSpan.setText("Rp" + String.format("%,.0f", total));
    }

    /**
     * P1.5: Memetakan MIME type ke ekstensi file yang aman.
     * Mengembalikan null jika tipe tidak didukung.
     */
    private String sanitizeExtension(String mimeType) {
        if (mimeType == null) return null;
        return switch (mimeType.toLowerCase().trim()) {
            case "image/jpeg", "image/jpg" -> ".jpg";
            case "image/png"               -> ".png";
            case "image/webp"              -> ".webp";
            default                        -> null;
        };
    }
}
