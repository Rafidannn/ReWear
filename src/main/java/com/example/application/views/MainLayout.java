package com.example.application.views;

import com.example.application.model.user.User;
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
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.VaadinSession;

/**
 * MainLayout — Top Navbar Navy (Figma Frame 1 Exact) + Navigasi Keranjang
 */
@Layout
public final class MainLayout extends AppLayout implements BeforeEnterObserver {

    private Span linkKategori;
    private Span linkPasar;

    public MainLayout() {
        setPrimarySection(Section.NAVBAR);
        addToNavbar(true, createTopNavbar());
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

        // Tekan Enter di search → notification
        searchInput.getElement().addEventListener("keydown", e -> {})
            .setFilter("event.key === 'Enter'");
        searchInput.getElement().addEventListener("keydown", e -> {
            UI.getCurrent().getPage().executeJs(
                "var q = document.querySelector('.rw-nav-search-input');" +
                "if(q && q.value.trim()) { return q.value.trim(); } else { return ''; }"
            ).then(String.class, query -> {
                if (query != null && !query.isEmpty()) {
                    Notification.show("Mencari: \"" + query + "\"", 2000, Notification.Position.TOP_CENTER);
                }
            });
        }).setFilter("event.key === 'Enter'");

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

        // "Dashboard Penjual" → navigate ke dashboard toko penjual
        Span linkSeller = new Span("Dashboard Penjual");
        linkSeller.addClassName("rw-nav-link");
        linkSeller.addClickListener(e -> UI.getCurrent().navigate("seller"));

        HorizontalLayout navLinks = new HorizontalLayout(linkKategori, linkPasar, linkSeller);
        navLinks.setSpacing(false);
        navLinks.addClassName("rw-nav-links-group");

        // ---- Right Side Icons ----
        Span cartIcon = new Span();
        Icon cartSvgIcon = VaadinIcon.CART.create();
        cartSvgIcon.setSize("20px");
        cartIcon.add(cartSvgIcon);
        cartIcon.addClassNames("rw-nav-icon-btn", "rw-cart-icon-wrap");
        cartIcon.getElement().setAttribute("title", "Keranjang Belanja");

        Span cartBadge = new Span("3");
        cartBadge.addClassName("rw-cart-badge-count");
        cartIcon.add(cartBadge);

        cartIcon.addClickListener(e -> UI.getCurrent().navigate("cart"));

        Span chatIcon = buildNavIconBtn(VaadinIcon.COMMENT, "Pesan masuk");
        chatIcon.addClickListener(e -> UI.getCurrent().navigate("chat"));
        Span bellIcon = buildNavIconBtn(VaadinIcon.BELL, "Notifikasi");

        // ---- Avatar / Profile Button (Conditional on login state) ----
        User currentUser = VaadinSession.getCurrent() != null ? VaadinSession.getCurrent().getAttribute(User.class) : null;
        Component rightSideItem;

        if (currentUser == null) {
            Span masukLink = new Span("Masuk");
            masukLink.getElement().getStyle()
                .set("color", "#F5C45E")
                .set("font-weight", "700")
                .set("font-size", "14px")
                .set("cursor", "pointer")
                .set("padding", "6px 12px")
                .set("transition", "opacity 0.2s");
            masukLink.getElement().addEventListener("mouseover", e -> masukLink.getElement().getStyle().set("opacity", "0.85"));
            masukLink.getElement().addEventListener("mouseout", e -> masukLink.getElement().getStyle().set("opacity", "1.0"));
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

            ContextMenu menu = new ContextMenu(avatar);
            menu.setOpenOnClick(true);
            menu.addItem("Profil Saya", e -> UI.getCurrent().navigate("profile"));
            menu.addItem("Dashboard Penjual", e -> UI.getCurrent().navigate("seller"));
            menu.addItem("Pesanan Saya", e -> UI.getCurrent().navigate("profile?tab=orders"));
            menu.addItem("Keluar", e -> {
                VaadinSession.getCurrent().setAttribute(User.class, null);
                Notification.show("Berhasil keluar.");
                UI.getCurrent().navigate("");
                UI.getCurrent().getPage().reload();
            });
            rightSideItem = avatar;
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
        btnJualNav.addClickListener(e -> UI.getCurrent().navigate("sell"));

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

    private Span buildNavIconBtn(VaadinIcon iconType, String tooltip) {
        Span btn = new Span();
        Icon ico = iconType.create();
        ico.setSize("20px");
        btn.add(ico);
        btn.addClassName("rw-nav-icon-btn");
        btn.getElement().setAttribute("title", tooltip);
        btn.addClickListener(e ->
            Notification.show(tooltip, 1500, Notification.Position.TOP_CENTER)
        );
        return btn;
    }
}
