package com.example.application.views;

import com.example.application.model.user.Role;
import com.example.application.model.user.User;
import com.example.application.service.order.CartService;
import com.example.application.util.AuthGuard;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.server.VaadinSession;

/**
 * MainLayout — Top Navbar Navy (Figma Frame 1 Exact) + Navigasi Keranjang
 */
@Layout
public final class MainLayout extends AppLayout implements BeforeEnterObserver {

    private Span linkKategori;
    private Span linkPasar;
    private final CartService cartService;
    private Span cartBadge;
    private Div mobNavHome;
    private Div mobNavCat;
    private Div mobNavChat;
    private Div mobNavProfile;
    private Div mobBottomBarDiv;

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
                linkPasar.addClassNames("rw-nav-link", "rw-nav-link-active");
                linkKategori.removeClassName("rw-nav-link-active");
                linkKategori.addClassName("rw-nav-link");
            } else {
                linkKategori.addClassNames("rw-nav-link", "rw-nav-link-active");
                linkPasar.removeClassName("rw-nav-link-active");
                linkPasar.addClassName("rw-nav-link");
            }
        }
        if (mobBottomBarDiv != null) {
            boolean isStandaloneMobileView = path.startsWith("product") || path.startsWith("checkout");
            if (isStandaloneMobileView) {
                mobBottomBarDiv.getStyle().set("display", "none");
            } else {
                mobBottomBarDiv.getStyle().remove("display");
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

        // ---- Brand / Logo (klik → home) ----
        Div logo = new Div();
        logo.addClassName("rw-logo");
        Span logoText = new Span("ReWear");
        logoText.addClassName("rw-logo-text");
        logo.add(logoText);
        logo.addClickListener(e -> UI.getCurrent().navigate(""));

        // ---- Search Bar ----
        Div searchBar = new Div();
        searchBar.addClassName("rw-nav-searchbar");
        Input searchInput = new Input();
        searchInput.setPlaceholder("Cari barang thrift impianmu...");
        searchInput.addClassName("rw-nav-search-input");

        // SVG search icon via innerHTML
        Div searchIcoDiv = new Div();
        searchIcoDiv.addClassName("rw-nav-search-icon");
        searchIcoDiv.getElement().setProperty("innerHTML",
            "<svg width='16' height='16' viewBox='0 0 24 24' fill='none' xmlns='http://www.w3.org/2000/svg' style='color:#74777F'>" +
            "<circle cx='11' cy='11' r='8' stroke='#74777F' stroke-width='1.8'/>" +
            "<path d='M21 21l-4.35-4.35' stroke='#74777F' stroke-width='1.8' stroke-linecap='round'/>" +
            "</svg>"
        );

        // Tekan Enter di search → navigate ke Pasar SMKN 24 dengan keyword
        searchInput.getElement().addEventListener("keydown", e -> {
            UI.getCurrent().getPage().executeJs(
                "return document.querySelector('.rw-nav-search-input') ? document.querySelector('.rw-nav-search-input').value.trim() : ''"
            ).then(String.class, query -> {
                if (query != null && !query.isBlank()) {
                    UI.getCurrent().navigate("pasar-smkn24",
                        new QueryParameters(java.util.Map.of("q", java.util.List.of(query))));
                } else {
                    UI.getCurrent().navigate("pasar-smkn24");
                }
            });
        }).setFilter("event.key === 'Enter'");

        // Klik ikon search → juga trigger pencarian
        searchIcoDiv.addClickListener(e -> {
            UI.getCurrent().getPage().executeJs(
                "return document.querySelector('.rw-nav-search-input') ? document.querySelector('.rw-nav-search-input').value.trim() : ''"
            ).then(String.class, query -> {
                if (query != null && !query.isBlank()) {
                    UI.getCurrent().navigate("pasar-smkn24",
                        new QueryParameters(java.util.Map.of("q", java.util.List.of(query))));
                } else {
                    UI.getCurrent().navigate("pasar-smkn24");
                }
            });
        });

        searchBar.add(searchInput, searchIcoDiv);

        // "Kategori" → scroll ke section kategori di home
        linkKategori = new Span("Kategori");
        linkKategori.addClassNames("rw-nav-link", "rw-nav-link-active");
        linkKategori.addClickListener(e -> {
            UI.getCurrent().navigate("");
            UI.getCurrent().getPage().executeJs(
                "setTimeout(function(){ var el = document.getElementById('category-section'); if(el) el.scrollIntoView({behavior:'smooth'}); }, 300)"
            );
        });

        // "Pasar SMKN 24" → navigate ke halaman khusus marketplace Pasar SMKN 24
        linkPasar = new Span("Pasar SMKN 24");
        linkPasar.addClassName("rw-nav-link");
        linkPasar.addClickListener(e -> UI.getCurrent().navigate("pasar-smkn24"));

        // User session
        User currentUser = VaadinSession.getCurrent() != null ? VaadinSession.getCurrent().getAttribute(User.class) : null;

        HorizontalLayout navLinks = new HorizontalLayout(linkKategori, linkPasar);
        if (currentUser != null && (currentUser.getRole() == Role.SUPER_ADMIN || currentUser.getRole() == Role.MODERATOR)) {
            Span linkAdmin = new Span("Panel Admin");
            linkAdmin.addClassName("rw-nav-link");
            linkAdmin.getStyle().set("color", "#F5C45E").set("font-weight", "800");
            linkAdmin.addClickListener(e -> UI.getCurrent().navigate("admin"));
            navLinks.add(linkAdmin);
        } else {
            Span linkSeller = new Span("Dashboard Penjual");
            linkSeller.addClassName("rw-nav-link");
            linkSeller.addClickListener(e -> UI.getCurrent().navigate("seller"));
            navLinks.add(linkSeller);
        }
        navLinks.setSpacing(false);
        navLinks.addClassName("rw-nav-links-group");

        // ---- Right Side Icons ----
        Span cartIcon = new Span();
        Icon cartSvgIcon = VaadinIcon.CART.create();
        cartSvgIcon.setSize("20px");
        cartIcon.add(cartSvgIcon);
        cartIcon.addClassNames("rw-nav-icon-btn", "rw-cart-icon-wrap");
        cartIcon.getElement().setAttribute("title", "Keranjang Belanja");

        User navUser = VaadinSession.getCurrent() != null ? VaadinSession.getCurrent().getAttribute(User.class) : null;
        int cartCount = (navUser != null && cartService != null) ? cartService.getCartCount(navUser) : 0;
        this.cartBadge = new Span(String.valueOf(cartCount));
        this.cartBadge.addClassName("rw-cart-badge-count");
        if (cartCount == 0) {
            this.cartBadge.getElement().getStyle().set("display", "none");
        }
        cartIcon.add(this.cartBadge);

        cartIcon.addClickListener(e -> {
            if (AuthGuard.requireLogin(UI.getCurrent())) UI.getCurrent().navigate("cart");
        });

        Span chatIcon = buildNavIconBtn(VaadinIcon.COMMENT, "Pesan masuk",
            () -> { if (AuthGuard.requireLogin(UI.getCurrent())) UI.getCurrent().navigate("chat"); });
        Span bellIcon = buildNavIconBtn(VaadinIcon.BELL, "Notifikasi",
            () -> { if (AuthGuard.requireLogin(UI.getCurrent())) UI.getCurrent().navigate("notifications"); });

        // ---- Avatar / Profile Button (Conditional on login state) ----
        Component rightSideItem;

        if (currentUser == null) {
            Span masukLink = new Span("Masuk");
            masukLink.getElement().getStyle()
                .set("color", "#F5C45E")
                .set("font-weight", "700")
                .set("font-size", "14px")
                .set("cursor", "pointer")
                .set("padding", "6px 12px");
            masukLink.addClickListener(e -> UI.getCurrent().navigate("login"));
            rightSideItem = masukLink;
        } else {
            String initial = (currentUser.getFullName() != null && !currentUser.getFullName().isEmpty())
                ? String.valueOf(currentUser.getFullName().charAt(0)).toUpperCase()
                : "R";

            Div avatar = new Div();
            avatar.addClassName("rw-nav-avatar-wrap");
            avatar.getElement().setProperty("innerHTML",
                "<div class='rw-avatar-circle' title='" + currentUser.getFullName() + "'>" + initial + "</div>"
            );

            MenuBar userMenuBar = new MenuBar();
            userMenuBar.addClassName("rw-nav-user-menubar");
            userMenuBar.getElement().getStyle()
                .set("background", "transparent")
                .set("border", "none")
                .set("cursor", "pointer")
                .set("padding", "0")
                .set("margin", "0");

            MenuItem userItem = userMenuBar.addItem(avatar);
            if (currentUser.getRole() == Role.SUPER_ADMIN || currentUser.getRole() == Role.MODERATOR) {
                userItem.getSubMenu().addItem("Panel Admin & Moderasi", e -> UI.getCurrent().navigate("admin"));
            }
            userItem.getSubMenu().addItem("Profil Saya", e -> UI.getCurrent().navigate("profile"));
            userItem.getSubMenu().addItem("Dashboard Penjual", e -> UI.getCurrent().navigate("seller"));
            userItem.getSubMenu().addItem("Pesanan Saya", e -> UI.getCurrent().navigate("orders"));
            userItem.getSubMenu().addItem("Keluar", e -> {
                VaadinSession.getCurrent().setAttribute(User.class, null);
                Notification.show("Berhasil keluar.");
                UI.getCurrent().navigate("");
                UI.getCurrent().getPage().reload();
            });
            rightSideItem = userMenuBar;
        }

        // ---- Tombol "Jual" (Gold Pill Button) ----
        Button btnJualNav = new Button("Jual", VaadinIcon.PLUS_CIRCLE.create());
        btnJualNav.getElement().getStyle()
            .set("background", "#F5C45E")
            .set("color", "#001934")
            .set("font-weight", "800")
            .set("font-size", "13px")
            .set("border-radius", "9999px")
            .set("border", "none")
            .set("padding", "6px 16px")
            .set("cursor", "pointer")
            .set("margin-right", "8px")
            .set("box-shadow", "0 2px 8px rgba(245, 196, 94, 0.3)");
        btnJualNav.addClickListener(e -> {
            if (AuthGuard.requireLogin(UI.getCurrent())) UI.getCurrent().navigate("sell");
        });

        HorizontalLayout rightSide = new HorizontalLayout(cartIcon, chatIcon, bellIcon, btnJualNav, rightSideItem);
        rightSide.setAlignItems(FlexComponent.Alignment.CENTER);
        rightSide.setSpacing(false);
        rightSide.addClassName("rw-nav-right");

        // ---- Assemble Navbar ----
        HorizontalLayout navbarInner = new HorizontalLayout(logo, searchBar, navLinks, rightSide);
        navbarInner.setWidthFull();
        navbarInner.setAlignItems(FlexComponent.Alignment.CENTER);
        navbarInner.addClassName("rw-main-navbar-inner");
        navbarInner.expand(searchBar);

        Div navbarOuter = new Div(navbarInner);
        navbarOuter.setWidthFull();
        navbarOuter.addClassName("rw-main-navbar");

        return navbarOuter;
    }

    private Span buildNavIconBtn(VaadinIcon iconType, String tooltip, Runnable onClickAction) {
        Span btn = new Span();
        Icon ico = iconType.create();
        ico.setSize("20px");
        btn.add(ico);
        btn.addClassName("rw-nav-icon-btn");
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
        mobBottomBarDiv = new Div();
        mobBottomBarDiv.addClassName("rw-mobile-bottom-bar");
        mobBottomBarDiv.getElement().getStyle()
            .set("position", "fixed")
            .set("bottom", "0")
            .set("left", "0")
            .set("right", "0")
            .set("width", "100%")
            .set("height", "60px")
            .set("background", "#FFFFFF")
            .set("border-top", "1px solid #E2E8F0")
            .set("z-index", "999")
            .set("box-shadow", "0 -2px 10px rgba(0, 25, 52, 0.05)")
            .set("box-sizing", "border-box")
            .set("align-items", "center")
            .set("justify-content", "space-around");

        // Home
        mobNavHome = createMobileNavItem("Home",
            "<svg width='22' height='22' viewBox='0 0 24 24' fill='none' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><path d='M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z'></path><polyline points='9 22 9 12 15 12 15 22'></polyline></svg>",
            "Home", "");

        // Pasar SMKN 24
        mobNavCat = createMobileNavItem("Pasar 24",
            "<svg width='22' height='22' viewBox='0 0 24 24' fill='none' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><rect x='3' y='3' width='7' height='7'></rect><rect x='14' y='3' width='7' height='7'></rect><rect x='14' y='14' width='7' height='7'></rect><rect x='3' y='14' width='7' height='7'></rect></svg>",
            "Pasar SMKN 24", "pasar-smkn24");

        // Plus FAB
        Div fabWrap = new Div();
        fabWrap.addClassName("rw-mobile-fab-wrap");
        fabWrap.getElement().getStyle()
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center")
            .set("flex", "1");
        Div fab = new Div();
        fab.addClassName("rw-mobile-fab");
        fab.getElement().getStyle()
            .set("cursor", "pointer")
            .set("width", "42px")
            .set("height", "42px")
            .set("border-radius", "50%")
            .set("background", "#F5C45E")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center")
            .set("margin-top", "-14px")
            .set("box-shadow", "0 3px 8px rgba(0, 25, 52, 0.18)");
        fab.getElement().setProperty("innerHTML",
            "<svg width='22' height='22' viewBox='0 0 24 24' fill='none' stroke='#001934' stroke-width='2.5' stroke-linecap='round' stroke-linejoin='round' style='pointer-events:none;'><line x1='12' y1='5' x2='12' y2='19'></line><line x1='5' y1='12' x2='19' y2='12'></line></svg>"
        );
        fab.addClickListener(e -> {
            if (AuthGuard.requireLogin(UI.getCurrent())) UI.getCurrent().navigate("sell");
        });
        fabWrap.add(fab);

        // Chat
        mobNavChat = createMobileNavItem("Chat",
            "<svg width='22' height='22' viewBox='0 0 24 24' fill='none' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><path d='M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z'></path></svg>",
            "Chat", "chat");

        // Profil
        mobNavProfile = createMobileNavItem("Profil",
            "<svg width='22' height='22' viewBox='0 0 24 24' fill='none' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><path d='M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2'></path><circle cx='12' cy='7' r='4'></circle></svg>",
            "Profil", "profile");

        mobBottomBarDiv.add(mobNavHome, mobNavCat, fabWrap, mobNavChat, mobNavProfile);
        return mobBottomBarDiv;
    }

    private Div createMobileNavItem(String label, String svgHtml, String title, String routePath) {
        Div item = new Div();
        item.addClassName("rw-mobile-nav-item");
        item.getElement().getStyle().set("cursor", "pointer");

        Div iconDiv = new Div();
        iconDiv.addClassName("rw-mob-nav-icon");
        iconDiv.getElement().getStyle().set("pointer-events", "none");
        iconDiv.getElement().setProperty("innerHTML", svgHtml);

        Span textSpan = new Span(label);
        textSpan.addClassName("rw-mob-nav-label");
        textSpan.getElement().getStyle().set("pointer-events", "none");

        item.add(iconDiv, textSpan);
        item.addClickListener(e -> UI.getCurrent().navigate(routePath));
        return item;
    }
}
