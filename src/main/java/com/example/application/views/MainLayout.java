package com.example.application.views;

import com.example.application.model.user.Role;
import com.example.application.model.user.User;
import com.example.application.service.order.CartService;
import com.example.application.util.AuthGuard;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.server.VaadinSession;

import java.util.List;
import java.util.Map;

/**
 * MainLayout — Navy Top Navbar + Responsive Mobile Bottom Navigation
 */
@Layout
@StyleSheet("styles.css")
public final class MainLayout extends AppLayout implements BeforeEnterObserver {

    private Span linkKategori;
    private Span linkPasar;
    private final CartService cartService;
    private Span cartBadge;
    private Div mobNavHome;
    private Div mobNavCat;
    private Div mobNavChat;
    private Div mobNavProfile;

    public MainLayout(CartService cartService) {
        this.cartService = cartService;
        setPrimarySection(Section.NAVBAR);
        addToNavbar(false, createTopNavbar());
        addToNavbar(true, createMobileBottomBar());
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String path = event.getLocation().getPath();
        if (linkKategori != null && linkPasar != null) {
            if ("pasar-smkn24".equalsIgnoreCase(path)) {
                linkPasar.getStyle().set("color", "#F5C45E").set("font-weight", "800");
                linkKategori.getStyle().set("color", "rgba(255,255,255,0.85)").set("font-weight", "600");
            } else {
                linkKategori.getStyle().set("color", "#F5C45E").set("font-weight", "800");
                linkPasar.getStyle().set("color", "rgba(255,255,255,0.85)").set("font-weight", "600");
            }
        }
        if (mobNavHome != null) {
            mobNavHome.removeClassName("active");
            mobNavCat.removeClassName("active");
            mobNavChat.removeClassName("active");
            mobNavProfile.removeClassName("active");

            if (path.isEmpty() || "home".equalsIgnoreCase(path)) {
                mobNavHome.addClassName("active");
            } else if ("pasar-smkn24".equalsIgnoreCase(path)) {
                mobNavCat.addClassName("active");
            } else if ("chat".equalsIgnoreCase(path)) {
                mobNavChat.addClassName("active");
            } else if ("profile".equalsIgnoreCase(path)) {
                mobNavProfile.addClassName("active");
            }
        }
    }

    private Component createTopNavbar() {
        // Outer wrapper
        Div navbarOuter = new Div();
        navbarOuter.addClassName("rw-main-navbar");
        navbarOuter.setWidthFull();
        navbarOuter.getElement().getStyle()
            .set("background", "#001934")
            .set("width", "100%")
            .set("height", "64px")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center")
            .set("position", "sticky")
            .set("top", "0")
            .set("z-index", "1000")
            .set("box-shadow", "0 2px 10px rgba(0,0,0,0.15)")
            .set("box-sizing", "border-box");

        // Inner centered row
        Div navbarInner = new Div();
        navbarInner.addClassName("rw-main-navbar-inner");
        navbarInner.getElement().getStyle()
            .set("max-width", "1280px")
            .set("width", "100%")
            .set("height", "100%")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "space-between")
            .set("padding", "0 20px")
            .set("box-sizing", "border-box")
            .set("gap", "12px");

        // ---- 1. Logo ----
        Div logo = new Div();
        logo.addClassName("rw-logo");
        logo.getElement().getStyle()
            .set("display", "flex").set("align-items", "center")
            .set("cursor", "pointer").set("flex-shrink", "0");
        Span logoText = new Span("ReWear");
        logoText.addClassName("rw-logo-text");
        logoText.getElement().getStyle()
            .set("font-size", "22px").set("font-weight", "800")
            .set("color", "#FFFFFF").set("letter-spacing", "-0.5px");
        logo.add(logoText);
        logo.addClickListener(e -> UI.getCurrent().navigate(""));

        // ---- 2. Search Bar ----
        Div searchBar = new Div();
        searchBar.addClassName("rw-nav-searchbar");
        searchBar.getElement().getStyle()
            .set("display", "flex").set("align-items", "center")
            .set("background", "rgba(255, 255, 255, 0.12)")
            .set("border", "1px solid rgba(255, 255, 255, 0.2)")
            .set("border-radius", "8px")
            .set("padding", "6px 14px")
            .set("flex", "1")
            .set("max-width", "420px")
            .set("box-sizing", "border-box");

        Input searchInput = new Input();
        searchInput.setPlaceholder("Cari seragam, buku, barang thrift...");
        searchInput.addClassName("rw-nav-search-input");
        searchInput.getElement().getStyle()
            .set("border", "none").set("background", "transparent")
            .set("outline", "none").set("color", "#FFFFFF")
            .set("font-size", "13px").set("width", "100%");

        Icon searchIco = VaadinIcon.SEARCH.create();
        searchIco.setSize("16px");
        searchIco.getElement().getStyle()
            .set("color", "rgba(255,255,255,0.7)")
            .set("cursor", "pointer").set("margin-left", "8px");

        Runnable performSearch = () -> {
            UI.getCurrent().getPage().executeJs(
                "return document.querySelector('.rw-nav-search-input') ? document.querySelector('.rw-nav-search-input').value.trim() : ''"
            ).then(String.class, q -> {
                if (q != null && !q.isBlank()) {
                    UI.getCurrent().navigate("pasar-smkn24", new QueryParameters(Map.of("q", List.of(q))));
                } else {
                    UI.getCurrent().navigate("pasar-smkn24");
                }
            });
        };

        searchInput.getElement().addEventListener("keydown", e -> performSearch.run()).setFilter("event.key === 'Enter'");
        searchIco.addClickListener(e -> performSearch.run());
        searchBar.add(searchInput, searchIco);

        // ---- 3. Center Nav Links (Desktop) ----
        Div navLinks = new Div();
        navLinks.addClassName("rw-nav-links-group");
        navLinks.getElement().getStyle()
            .set("display", "flex").set("align-items", "center")
            .set("gap", "8px").set("flex-shrink", "0");

        linkKategori = new Span("Kategori");
        linkKategori.addClassName("rw-nav-link");
        styleNavLink(linkKategori);
        linkKategori.addClickListener(e -> {
            UI.getCurrent().navigate("");
            UI.getCurrent().getPage().executeJs(
                "setTimeout(function(){ var el = document.getElementById('category-section'); if(el) el.scrollIntoView({behavior:'smooth'}); }, 300)");
        });

        linkPasar = new Span("Pasar SMKN 24");
        linkPasar.addClassName("rw-nav-link");
        styleNavLink(linkPasar);
        linkPasar.addClickListener(e -> UI.getCurrent().navigate("pasar-smkn24"));

        navLinks.add(linkKategori, linkPasar);

        User currentUser = VaadinSession.getCurrent() != null ? VaadinSession.getCurrent().getAttribute(User.class) : null;
        if (currentUser != null && (currentUser.getRole() == Role.SUPER_ADMIN || currentUser.getRole() == Role.MODERATOR)) {
            Span linkAdmin = new Span("Panel Admin");
            linkAdmin.addClassName("rw-nav-link");
            styleNavLink(linkAdmin);
            linkAdmin.getStyle().set("color", "#F5C45E").set("font-weight", "800");
            linkAdmin.addClickListener(e -> UI.getCurrent().navigate("admin"));
            navLinks.add(linkAdmin);
        } else {
            Span linkSeller = new Span("Dashboard Penjual");
            linkSeller.addClassName("rw-nav-link");
            styleNavLink(linkSeller);
            linkSeller.addClickListener(e -> {
                if (AuthGuard.requireLogin(UI.getCurrent())) UI.getCurrent().navigate("seller");
            });
            navLinks.add(linkSeller);
        }

        // ---- 4. Right Side Items ----
        Div rightSide = new Div();
        rightSide.addClassName("rw-nav-right");
        rightSide.getElement().getStyle()
            .set("display", "flex").set("align-items", "center")
            .set("gap", "10px").set("flex-shrink", "0");

        // Cart Icon
        Span cartIcon = buildNavIconBtn(VaadinIcon.CART, "Keranjang Belanja", () -> {
            if (AuthGuard.requireLogin(UI.getCurrent())) UI.getCurrent().navigate("cart");
        });
        int cartCount = (currentUser != null && cartService != null) ? cartService.getCartCount(currentUser) : 0;
        this.cartBadge = new Span(String.valueOf(cartCount));
        this.cartBadge.addClassName("rw-cart-badge-count");
        this.cartBadge.getElement().getStyle()
            .set("position", "absolute").set("top", "-4px").set("right", "-4px")
            .set("background", "#DC2626").set("color", "#FFFFFF")
            .set("font-size", "10px").set("font-weight", "800")
            .set("padding", "2px 6px").set("border-radius", "10px")
            .set("line-height", "1");
        if (cartCount == 0) this.cartBadge.getStyle().set("display", "none");
        cartIcon.getStyle().set("position", "relative");
        cartIcon.add(this.cartBadge);

        // Chat Icon
        Span chatIcon = buildNavIconBtn(VaadinIcon.COMMENT, "Pesan Chat", () -> {
            if (AuthGuard.requireLogin(UI.getCurrent())) UI.getCurrent().navigate("chat");
        });

        // Bell Icon
        Span bellIcon = buildNavIconBtn(VaadinIcon.BELL, "Pusat Notifikasi", () -> {
            if (AuthGuard.requireLogin(UI.getCurrent())) UI.getCurrent().navigate("notifications");
        });

        // Jual Button
        Button btnJual = new Button("Jual", VaadinIcon.PLUS_CIRCLE.create());
        btnJual.getElement().getStyle()
            .set("background", "#F5C45E").set("color", "#001934")
            .set("font-weight", "800").set("font-size", "12px")
            .set("border-radius", "20px").set("border", "none")
            .set("padding", "6px 14px").set("cursor", "pointer");
        btnJual.addClickListener(e -> {
            if (AuthGuard.requireLogin(UI.getCurrent())) UI.getCurrent().navigate("sell");
        });

        // Login / Avatar
        Component profileOrLogin;
        if (currentUser == null) {
            Button btnMasuk = new Button("Masuk", e -> UI.getCurrent().navigate("login"));
            btnMasuk.getElement().getStyle()
                .set("background", "transparent").set("color", "#F5C45E")
                .set("font-weight", "700").set("font-size", "13px")
                .set("border", "1px solid #F5C45E").set("border-radius", "16px")
                .set("padding", "4px 12px").set("cursor", "pointer");
            profileOrLogin = btnMasuk;
        } else {
            String initial = (currentUser.getFullName() != null && !currentUser.getFullName().isBlank())
                ? String.valueOf(currentUser.getFullName().charAt(0)).toUpperCase()
                : "U";

            Div avatar = new Div();
            avatar.getElement().getStyle()
                .set("width", "34px").set("height", "34px").set("border-radius", "50%")
                .set("background", "#F5C45E").set("color", "#001934")
                .set("font-weight", "800").set("font-size", "14px")
                .set("display", "flex").set("align-items", "center").set("justify-content", "center")
                .set("cursor", "pointer").set("user-select", "none");
            avatar.setText(initial);

            ContextMenu menu = new ContextMenu(avatar);
            menu.setOpenOnClick(true);
            if (currentUser.getRole() == Role.SUPER_ADMIN || currentUser.getRole() == Role.MODERATOR) {
                menu.addItem("Panel Admin & Moderasi", e -> UI.getCurrent().navigate("admin"));
            }
            menu.addItem("Profil Saya", e -> UI.getCurrent().navigate("profile"));
            menu.addItem("Dashboard Penjual", e -> UI.getCurrent().navigate("seller"));
            menu.addItem("Pesanan Saya", e -> UI.getCurrent().navigate("orders"));
            menu.addItem("Keluar", e -> {
                VaadinSession.getCurrent().setAttribute(User.class, null);
                Notification.show("Berhasil keluar.");
                UI.getCurrent().navigate("");
                UI.getCurrent().getPage().reload();
            });
            profileOrLogin = avatar;
        }

        rightSide.add(cartIcon, chatIcon, bellIcon, btnJual, profileOrLogin);

        navbarInner.add(logo, searchBar, navLinks, rightSide);
        navbarOuter.add(navbarInner);
        return navbarOuter;
    }

    private void styleNavLink(Span link) {
        link.getElement().getStyle()
            .set("color", "rgba(255,255,255,0.85)")
            .set("font-size", "13px")
            .set("font-weight", "600")
            .set("padding", "6px 10px")
            .set("cursor", "pointer")
            .set("user-select", "none")
            .set("transition", "color 0.2s");
    }

    private Span buildNavIconBtn(VaadinIcon iconType, String tooltip, Runnable onClickAction) {
        Span btn = new Span();
        Icon ico = iconType.create();
        ico.setSize("18px");
        ico.getElement().getStyle().set("color", "#FFFFFF");
        btn.add(ico);
        btn.getElement().getStyle()
            .set("display", "flex").set("align-items", "center").set("justify-content", "center")
            .set("width", "34px").set("height", "34px").set("border-radius", "50%")
            .set("background", "rgba(255,255,255,0.08)").set("cursor", "pointer");
        btn.getElement().setAttribute("title", tooltip);
        if (onClickAction != null) {
            btn.addClickListener(e -> onClickAction.run());
        }
        return btn;
    }

    public void refreshCartBadge() {
        User navUser = VaadinSession.getCurrent() != null ? VaadinSession.getCurrent().getAttribute(User.class) : null;
        int cartCount = (navUser != null && cartService != null) ? cartService.getCartCount(navUser) : 0;
        if (this.cartBadge != null) {
            this.cartBadge.setText(String.valueOf(cartCount));
            if (cartCount == 0) {
                this.cartBadge.getElement().getStyle().set("display", "none");
            } else {
                this.cartBadge.getElement().getStyle().remove("display");
            }
        }
    }

    public static void reloadCartBadge(UI ui) {
        if (ui == null) return;
        ui.getChildren().forEach(c -> {
            if (c instanceof MainLayout mainLayout) {
                mainLayout.refreshCartBadge();
            }
        });
    }

    private Component createMobileBottomBar() {
        Div bottomBar = new Div();
        bottomBar.addClassName("rw-mobile-bottom-bar");
        bottomBar.getElement().getStyle()
            .set("position", "fixed").set("bottom", "0").set("left", "0").set("right", "0")
            .set("height", "60px").set("background", "#FFFFFF")
            .set("border-top", "1px solid #E2E8F0")
            .set("box-shadow", "0 -3px 12px rgba(0,25,52,0.06)")
            .set("z-index", "9999")
            .set("display", "flex").set("align-items", "center").set("justify-content", "space-around")
            .set("padding", "0 6px").set("box-sizing", "border-box");

        mobNavHome = createMobileNavItem("Home",
            "<svg width='20' height='20' viewBox='0 0 24 24' fill='none' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><path d='M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z'></path><polyline points='9 22 9 12 15 12 15 22'></polyline></svg>",
            "");

        mobNavCat = createMobileNavItem("Pasar",
            "<svg width='20' height='20' viewBox='0 0 24 24' fill='none' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><rect x='3' y='3' width='7' height='7'></rect><rect x='14' y='3' width='7' height='7'></rect><rect x='14' y='14' width='7' height='7'></rect><rect x='3' y='14' width='7' height='7'></rect></svg>",
            "pasar-smkn24");

        Div fabWrap = new Div();
        fabWrap.getElement().getStyle()
            .set("display", "flex").set("align-items", "center").set("justify-content", "center").set("flex", "1");

        Div fab = new Div();
        fab.getElement().getStyle()
            .set("width", "46px").set("height", "46px").set("border-radius", "50%")
            .set("background", "linear-gradient(135deg, #FFDEA2 0%, #F5C45E 100%)")
            .set("box-shadow", "0 4px 12px rgba(245, 196, 94, 0.4)")
            .set("display", "flex").set("align-items", "center").set("justify-content", "center")
            .set("cursor", "pointer").set("margin-top", "-20px").set("border", "3px solid #FFFFFF");
        fab.getElement().setProperty("innerHTML",
            "<svg width='22' height='22' viewBox='0 0 24 24' fill='none' stroke='#001934' stroke-width='3' stroke-linecap='round' stroke-linejoin='round'><line x1='12' y1='5' x2='12' y2='19'></line><line x1='5' y1='12' x2='19' y2='12'></line></svg>"
        );
        fab.addClickListener(e -> {
            if (AuthGuard.requireLogin(UI.getCurrent())) UI.getCurrent().navigate("sell");
        });
        fabWrap.add(fab);

        mobNavChat = createMobileNavItem("Chat",
            "<svg width='20' height='20' viewBox='0 0 24 24' fill='none' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><path d='M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z'></path></svg>",
            "chat");

        mobNavProfile = createMobileNavItem("Profil",
            "<svg width='20' height='20' viewBox='0 0 24 24' fill='none' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><path d='M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2'></path><circle cx='12' cy='7' r='4'></circle></svg>",
            "profile");

        bottomBar.add(mobNavHome, mobNavCat, fabWrap, mobNavChat, mobNavProfile);
        return bottomBar;
    }

    private Div createMobileNavItem(String label, String svgHtml, String routePath) {
        Div item = new Div();
        item.addClassName("rw-mobile-nav-item");
        item.getElement().getStyle()
            .set("display", "flex").set("flex-direction", "column")
            .set("align-items", "center").set("justify-content", "center")
            .set("gap", "2px").set("color", "#64748B").set("font-size", "11px")
            .set("font-weight", "600").set("cursor", "pointer").set("flex", "1");

        Div iconDiv = new Div();
        iconDiv.getElement().setProperty("innerHTML", svgHtml);
        Span textSpan = new Span(label);
        item.add(iconDiv, textSpan);
        item.addClickListener(e -> {
            if ("profile".equals(routePath) || "chat".equals(routePath) || "sell".equals(routePath)) {
                if (AuthGuard.requireLogin(UI.getCurrent())) UI.getCurrent().navigate(routePath);
            } else {
                UI.getCurrent().navigate(routePath);
            }
        });
        return item;
    }
}
