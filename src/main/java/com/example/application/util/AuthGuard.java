package com.example.application.util;

import com.example.application.model.user.User;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.VaadinSession;

/**
 * AuthGuard — helper terpusat untuk memproteksi halaman yang memerlukan login.
 * Cara pakai di constructor View:
 *   if (!AuthGuard.requireLogin(UI.getCurrent())) return;
 */
public final class AuthGuard {

    private AuthGuard() {}

    /**
     * Ambil user yang sedang login dari session.
     * @return User jika sudah login, null jika belum.
     */
    public static User getCurrentUser() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session == null) return null;
        return session.getAttribute(User.class);
    }

    /**
     * Cek apakah user sudah login. Jika belum, tampilkan popup dialog login yang elegan.
     * @param ui UI instance saat ini (UI.getCurrent())
     * @return true jika sudah login, false jika belum (popup akan muncul).
     */
    public static boolean requireLogin(UI ui) {
        if (getCurrentUser() != null) return true;

        // Buat dialog popup login
        Dialog dialog = new Dialog();
        dialog.setCloseOnOutsideClick(true);
        dialog.setCloseOnEsc(true);
        dialog.addClassName("rw-auth-guard-dialog");

        // Container utama
        VerticalLayout content = new VerticalLayout();
        content.setAlignItems(FlexComponent.Alignment.CENTER);
        content.setPadding(false);
        content.setSpacing(false);
        content.getElement().getStyle()
            .set("padding", "32px 28px 24px 28px")
            .set("text-align", "center")
            .set("min-width", "320px")
            .set("max-width", "380px");

        // Icon kunci
        Div iconWrap = new Div();
        iconWrap.getElement().getStyle()
            .set("width", "64px")
            .set("height", "64px")
            .set("border-radius", "50%")
            .set("background", "linear-gradient(135deg, #001934, #002d5c)")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center")
            .set("margin", "0 auto 20px auto")
            .set("box-shadow", "0 8px 24px rgba(0,25,52,0.25)");
        var lockIcon = VaadinIcon.LOCK.create();
        lockIcon.setSize("28px");
        lockIcon.getElement().getStyle().set("color", "#F5C45E");
        iconWrap.add(lockIcon);

        // Judul
        H3 title = new H3("Login Diperlukan");
        title.getElement().getStyle()
            .set("color", "#001934")
            .set("font-size", "20px")
            .set("font-weight", "700")
            .set("margin", "0 0 8px 0");

        // Deskripsi
        Paragraph desc = new Paragraph("Kamu perlu login terlebih dahulu untuk mengakses fitur ini.");
        desc.getElement().getStyle()
            .set("color", "#6B7280")
            .set("font-size", "14px")
            .set("line-height", "1.5")
            .set("margin", "0 0 28px 0");

        // Tombol Login (primary)
        Button btnLogin = new Button("Masuk Sekarang");
        btnLogin.getElement().getStyle()
            .set("background", "linear-gradient(135deg, #001934, #002d5c)")
            .set("color", "#F5C45E")
            .set("font-weight", "700")
            .set("font-size", "14px")
            .set("border", "none")
            .set("border-radius", "10px")
            .set("padding", "12px 24px")
            .set("cursor", "pointer")
            .set("width", "100%")
            .set("margin-bottom", "10px")
            .set("box-shadow", "0 4px 12px rgba(0,25,52,0.2)");
        btnLogin.addClickListener(e -> {
            dialog.close();
            if (ui != null) ui.navigate("login");
        });

        // Tombol Daftar (secondary)
        Button btnRegister = new Button("Belum punya akun? Daftar");
        btnRegister.getElement().getStyle()
            .set("background", "transparent")
            .set("color", "#001934")
            .set("font-weight", "600")
            .set("font-size", "13px")
            .set("border", "1.5px solid #E5E7EB")
            .set("border-radius", "10px")
            .set("padding", "10px 24px")
            .set("cursor", "pointer")
            .set("width", "100%");
        btnRegister.addClickListener(e -> {
            dialog.close();
            if (ui != null) ui.navigate("register");
        });

        // Separator & label OR
        HorizontalLayout orRow = new HorizontalLayout();
        orRow.setWidthFull();
        orRow.setAlignItems(FlexComponent.Alignment.CENTER);
        orRow.setSpacing(false);
        orRow.getElement().getStyle().set("margin", "4px 0");

        Div line1 = new Div(); line1.getElement().getStyle().set("flex","1").set("height","1px").set("background","#E5E7EB");
        Span orLabel = new Span("atau");
        orLabel.getElement().getStyle()
            .set("padding","0 12px")
            .set("font-size","12px")
            .set("color","#9CA3AF");
        Div line2 = new Div(); line2.getElement().getStyle().set("flex","1").set("height","1px").set("background","#E5E7EB");
        orRow.add(line1, orLabel, line2);

        content.add(iconWrap, title, desc, btnLogin, orRow, btnRegister);
        dialog.add(content);
        dialog.open();

        return false;
    }
}
