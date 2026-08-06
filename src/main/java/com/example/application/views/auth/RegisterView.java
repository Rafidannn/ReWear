package com.example.application.views.auth;

import com.example.application.model.user.School;
import com.example.application.model.user.User;
import com.example.application.service.user.UserService;
import com.example.application.views.BlankLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;
import java.util.Optional;

@Route(value = "register", layout = BlankLayout.class)
@PageTitle("Daftar Akun | ReWear SMKN 24")
public class RegisterView extends HorizontalLayout {

    private final UserService userService;

    public RegisterView(UserService userService) {
        this.userService = userService;

        setSizeFull();
        setMargin(false);
        setPadding(false);
        setSpacing(false);
        getElement().getStyle()
            .set("height", "100vh")
            .set("width", "100%")
            .set("overflow", "hidden")
            .set("background-color", "#FFFFFF")
            .set("font-family", "Inter, sans-serif");

        // ---- LEFT CONTAINER (Form Side - 45% width) ----
        Div leftSide = new Div();
        leftSide.getElement().getStyle()
            .set("width", "45%")
            .set("min-width", "400px")
            .set("height", "100vh")
            .set("padding", "32px 64px")
            .set("box-sizing", "border-box")
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("justify-content", "center")
            .set("background", "#FFFFFF")
            .set("overflow-y", "auto");

        // Center Form Box
        Div formBox = new Div();
        formBox.getElement().getStyle()
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("width", "100%")
            .set("max-width", "380px")
            .set("margin", "0 auto");

        H2 title = new H2("Daftar Akun Baru");
        title.getElement().getStyle()
            .set("font-size", "28px")
            .set("font-weight", "800")
            .set("color", "#001934")
            .set("margin", "0 0 6px 0");

        Paragraph subtitle = new Paragraph("Bergabung dengan ekosistem preloved SMKN 24");
        subtitle.getElement().getStyle()
            .set("font-size", "14px")
            .set("color", "#64748B")
            .set("margin", "0 0 24px 0");

        // Inline Error Message Box
        Div errorBox = new Div();
        errorBox.setVisible(false);
        errorBox.getElement().getStyle()
            .set("background-color", "#FEF2F2")
            .set("border", "1px solid #FCA5A5")
            .set("color", "#991B1B")
            .set("padding", "12px 16px")
            .set("border-radius", "8px")
            .set("font-size", "13px")
            .set("font-weight", "500")
            .set("margin-bottom", "16px");

        // Input Nama Lengkap
        TextField nameField = new TextField("Nama Lengkap");
        nameField.setPlaceholder("Masukkan nama lengkap kamu");
        nameField.setWidthFull();
        nameField.setRequiredIndicatorVisible(true);
        nameField.getElement().getStyle().set("margin-bottom", "12px");

        // Input Email
        EmailField emailField = new EmailField("Email Sekolah / Umum");
        emailField.setPlaceholder("nama@smkn24.sch.id");
        emailField.setWidthFull();
        emailField.setRequiredIndicatorVisible(true);
        emailField.getElement().getStyle().set("margin-bottom", "12px");

        // Input Nomor HP
        TextField phoneField = new TextField("Nomor Telepon (WhatsApp)");
        phoneField.setPlaceholder("08xxxxxxxxxx");
        phoneField.setWidthFull();
        phoneField.setRequiredIndicatorVisible(true);
        phoneField.getElement().getStyle().set("margin-bottom", "12px");

        // Input Sekolah (ComboBox dinamis dari database)
        ComboBox<School> schoolComboBox = new ComboBox<>("Asal Sekolah");
        schoolComboBox.setPlaceholder("Pilih sekolah kamu");
        schoolComboBox.setItemLabelGenerator(School::getName);
        schoolComboBox.setWidthFull();
        schoolComboBox.setRequiredIndicatorVisible(true);
        schoolComboBox.getElement().getStyle().set("margin-bottom", "12px");

        List<School> schools = userService.findAllSchools();
        schoolComboBox.setItems(schools);
        if (!schools.isEmpty()) {
            schoolComboBox.setValue(schools.get(0)); // Auto-select the first school (SMKN 24 Jakarta)
        }

        // Input Password
        PasswordField passwordField = new PasswordField("Password");
        passwordField.setPlaceholder("Buat password baru");
        passwordField.setWidthFull();
        passwordField.setRequiredIndicatorVisible(true);
        passwordField.getElement().getStyle().set("margin-bottom", "20px");

        // Submit Button
        Button btnSubmit = new Button("Daftar Sekarang");
        btnSubmit.setWidthFull();
        btnSubmit.getElement().getStyle()
            .set("background", "#001934")
            .set("color", "#FFFFFF")
            .set("font-weight", "700")
            .set("font-size", "15px")
            .set("padding", "14px")
            .set("border-radius", "8px")
            .set("border", "none")
            .set("cursor", "pointer")
            .set("transition", "all 0.2s ease");

        btnSubmit.addClickListener(e -> {
            String name = nameField.getValue();
            String email = emailField.getValue();
            String phone = phoneField.getValue();
            String password = passwordField.getValue();
            School school = schoolComboBox.getValue();

            if (name == null || name.isBlank() ||
                email == null || email.isBlank() ||
                phone == null || phone.isBlank() ||
                password == null || password.isBlank() ||
                school == null) {
                errorBox.setText("Semua kolom harus diisi.");
                errorBox.setVisible(true);
                return;
            }

            // Check if email already exists
            Optional<User> existingUser = userService.findByEmail(email);
            if (existingUser.isPresent()) {
                errorBox.setText("Email tersebut sudah terdaftar. Silakan masuk.");
                errorBox.setVisible(true);
                return;
            }

            try {
                userService.registerUser(name, email, phone, password, school);

                Notification notif = Notification.show("Registrasi berhasil! Silakan masuk dengan akun baru Anda.", 3000, Notification.Position.TOP_CENTER);
                notif.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                UI.getCurrent().navigate("login");
            } catch (Exception ex) {
                errorBox.setText("Gagal melakukan registrasi: " + ex.getMessage());
                errorBox.setVisible(true);
            }
        });

        // Form Footer Links
        Div formFooter = new Div();
        formFooter.getElement().getStyle()
            .set("text-align", "center")
            .set("margin-top", "16px")
            .set("font-size", "14px")
            .set("color", "#64748B");

        Span hasAccount = new Span("Sudah punya akun? ");
        Anchor loginLink = new Anchor("login", "Masuk di sini");
        loginLink.getElement().getStyle()
            .set("color", "#001934")
            .set("font-weight", "700")
            .set("text-decoration", "none");

        formFooter.add(hasAccount, loginLink);

        formBox.add(title, subtitle, errorBox, nameField, emailField, phoneField, schoolComboBox, passwordField, btnSubmit, formFooter);

        // Left Bottom Copyright / Info
        Paragraph copyright = new Paragraph("© 2026 ReWear SMKN 24 Jakarta");
        copyright.getElement().getStyle()
            .set("font-size", "12px")
            .set("color", "#94A3B8")
            .set("margin-top", "24px")
            .set("text-align", "center");

        leftSide.add(formBox, copyright);

        // ---- RIGHT CONTAINER (Hero Image Side - 55% width) ----
        Div rightSide = new Div();
        rightSide.getElement().getStyle()
            .set("width", "55%")
            .set("height", "100vh")
            .set("position", "relative")
            .set("background-image", "linear-gradient(135deg, rgba(0, 25, 52, 0.85) 0%, rgba(0, 25, 52, 0.50) 100%), url('images/hero-banner2.jpg')")
            .set("background-size", "cover")
            .set("background-position", "right center")
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("justify-content", "space-between")
            .set("padding", "48px 64px")
            .set("box-sizing", "border-box");

        // Top Right Logo in Split Container
        Div logoWrapper = new Div();
        logoWrapper.getElement().getStyle()
            .set("display", "flex")
            .set("justify-content", "flex-end")
            .set("width", "100%");

        Anchor logoAnchor = new Anchor("", "");
        Image logoImg = new Image("images/logo.png", "ReWear Logo");
        logoImg.setHeight("48px");
        logoImg.getElement().getStyle().set("object-fit", "contain");
        logoAnchor.add(logoImg);
        logoWrapper.add(logoAnchor);

        // Hero Content at Bottom Right
        Div heroContent = new Div();
        heroContent.getElement().getStyle()
            .set("max-width", "520px")
            .set("color", "#FFFFFF");

        Span heroBadge = new Span("SMKN 24 MARKETPLACE");
        heroBadge.getElement().getStyle()
            .set("background", "#F5C45E")
            .set("color", "#001934")
            .set("font-weight", "800")
            .set("font-size", "11px")
            .set("padding", "4px 12px")
            .set("border-radius", "9999px")
            .set("letter-spacing", "0.5px")
            .set("display", "inline-block")
            .set("margin-bottom", "16px");

        H2 heroHeadline = new H2("Platform Preloved Terpercaya Warga SMKN 24");
        heroHeadline.getElement().getStyle()
            .set("font-size", "36px")
            .set("font-weight", "800")
            .set("line-height", "1.25")
            .set("color", "#FFFFFF")
            .set("margin", "0 0 16px 0");

        Paragraph heroDesc = new Paragraph("Beli dan jual barang preloved berkualitas di lingkungan sekolah. Hemat biaya, kurangi limbah, dan bangun gaya hidup berkelanjutan bersama.");
        heroDesc.getElement().getStyle()
            .set("font-size", "15px")
            .set("color", "rgba(255, 255, 255, 0.85)")
            .set("line-height", "1.6")
            .set("margin", "0");

        heroContent.add(heroBadge, heroHeadline, heroDesc);
        rightSide.add(logoWrapper, heroContent);

        add(leftSide, rightSide);
    }
}
