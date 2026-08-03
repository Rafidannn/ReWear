package com.example.application.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Layout;

/**
 * MainLayout — Top Navbar Navy (Figma Frame 1 Exact) + Navigasi Berfungsi
 */
@Layout
public final class MainLayout extends AppLayout {

    public MainLayout() {
        setPrimarySection(Section.NAVBAR);
        addToNavbar(true, createTopNavbar());
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
        Span linkKategori = new Span("Kategori");
        linkKategori.addClassNames("rw-nav-link", "rw-nav-link-active");
        linkKategori.addClickListener(e -> {
            UI.getCurrent().navigate("");
            UI.getCurrent().getPage().executeJs(
                "setTimeout(function(){ var el = document.getElementById('category-section'); if(el) el.scrollIntoView({behavior:'smooth'}); }, 300)"
            );
        });

        // "Pasar SMKN 24" → scroll ke section pasar di home
        Span linkPasar = new Span("Pasar SMKN 24");
        linkPasar.addClassName("rw-nav-link");
        linkPasar.addClickListener(e -> {
            UI.getCurrent().navigate("");
            UI.getCurrent().getPage().executeJs(
                "setTimeout(function(){ var el = document.getElementById('pasar-section'); if(el) el.scrollIntoView({behavior:'smooth'}); }, 300)"
            );
        });

        // "Jual Barang" → navigate ke halaman seller
        Span linkJual = new Span("Jual Barang");
        linkJual.addClassName("rw-nav-link");
        linkJual.addClickListener(e -> UI.getCurrent().navigate("seller"));

        HorizontalLayout navLinks = new HorizontalLayout(linkKategori, linkPasar, linkJual);
        navLinks.setSpacing(false);
        navLinks.addClassName("rw-nav-links-group");

        // ---- Right Side Icons ----
        Span cartIcon = buildNavIconBtn(VaadinIcon.CART, "Keranjang belanjamu");
        Span chatIcon = buildNavIconBtn(VaadinIcon.COMMENT, "Pesan masuk");
        Span bellIcon = buildNavIconBtn(VaadinIcon.BELL, "Notifikasi");

        // ---- Beli / Jual Toggle Pill (visual + JS toggle) ----
        Div toggleGroup = new Div();
        toggleGroup.addClassName("rw-mode-toggle");
        toggleGroup.setId("rw-mode-toggle");
        toggleGroup.getElement().setProperty("innerHTML",
            "<span id='pill-beli' class='rw-toggle-btn-active' onclick=\"" +
            "document.getElementById('pill-beli').className='rw-toggle-btn-active';" +
            "document.getElementById('pill-jual').className='rw-toggle-btn';\">Beli</span>" +
            "<span id='pill-jual' class='rw-toggle-btn' onclick=\"" +
            "document.getElementById('pill-jual').className='rw-toggle-btn-active';" +
            "document.getElementById('pill-beli').className='rw-toggle-btn';" +
            "window.location.href='/seller';\">Jual</span>"
        );

        // ---- Avatar ----
        Div avatar = new Div();
        avatar.addClassName("rw-nav-avatar-wrap");
        avatar.getElement().setProperty("innerHTML",
            "<div class='rw-avatar-circle' title='Profil saya'>R</div>"
        );
        avatar.addClickListener(e ->
            Notification.show("Login diperlukan untuk mengakses profil.", 2000, Notification.Position.TOP_CENTER)
        );

        HorizontalLayout rightSide = new HorizontalLayout(cartIcon, chatIcon, bellIcon, toggleGroup, avatar);
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
