package com.example.application.views.seller;

import com.example.application.model.product.Category;
import com.example.application.model.product.ConditionType;
import com.example.application.model.product.Product;
import com.example.application.model.product.ProductStatus;
import com.example.application.model.user.User;
import com.example.application.service.product.CategoryService;
import com.example.application.service.product.ProductService;
import com.example.application.service.user.UserService;
import com.example.application.config.WebMvcConfig;
import com.example.application.util.AuthGuard;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Route(value = "sell", layout = MainLayout.class)
@PageTitle("Jual Barang | ReWear SMKN 24")
public class AddProductView extends VerticalLayout implements BeforeEnterObserver {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final UserService userService;

    // State: menyimpan path semua foto yang sudah diupload
    private final List<String> uploadedImagePaths = new ArrayList<>();

    public AddProductView(ProductService productService, CategoryService categoryService, UserService userService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.userService = userService;

        setSpacing(false);
        setPadding(false);
        setWidthFull();
        getElement().getStyle()
            .set("background-color", "#F8F9FF")
            .set("padding-bottom", "64px");

        Div container = new Div();
        container.getElement().getStyle()
            .set("max-width", "720px")
            .set("margin", "32px auto")
            .set("background", "#FFFFFF")
            .set("border-radius", "16px")
            .set("padding", "40px")
            .set("box-shadow", "0 4px 24px rgba(0, 25, 52, 0.06)")
            .set("box-sizing", "border-box");

        // Header
        H2 pageTitle = new H2("Jual Barang Preloved");
        pageTitle.getElement().getStyle()
            .set("font-size", "26px")
            .set("font-weight", "800")
            .set("color", "#001934")
            .set("margin", "0 0 6px 0");

        Paragraph pageSub = new Paragraph("Isi informasi barang yang ingin Anda jual di ReWear SMKN 24");
        pageSub.getElement().getStyle()
            .set("font-size", "14px")
            .set("color", "#64748B")
            .set("margin", "0 0 32px 0");

        // Error Box
        Div errorBox = new Div();
        errorBox.setVisible(false);
        errorBox.getElement().getStyle()
            .set("background-color", "#FEF2F2")
            .set("border", "1px solid #FCA5A5")
            .set("color", "#991B1B")
            .set("padding", "12px 16px")
            .set("border-radius", "8px")
            .set("font-size", "13px")
            .set("margin-bottom", "20px");

        // 1. Nama Barang
        TextField nameField = new TextField("Nama Barang");
        nameField.setPlaceholder("Contoh: Buku Pelajaran Pastry Kelas X");
        nameField.setWidthFull();
        nameField.setRequiredIndicatorVisible(true);
        nameField.getElement().getStyle().set("margin-bottom", "16px");

        // 2. Kategori
        ComboBox<Category> categoryCombo = new ComboBox<>("Kategori Barang");
        categoryCombo.setPlaceholder("Pilih kategori");
        categoryCombo.setItemLabelGenerator(Category::getName);
        categoryCombo.setWidthFull();
        categoryCombo.setRequiredIndicatorVisible(true);
        categoryCombo.getElement().getStyle().set("margin-bottom", "16px");
        categoryCombo.setItems(categoryService.findAllSorted());

        // 3. Harga & Stok
        NumberField priceField = new NumberField("Harga (Rp)");
        priceField.setPlaceholder("45000");
        priceField.setWidth("65%");
        priceField.setRequiredIndicatorVisible(true);

        IntegerField stockField = new IntegerField("Jumlah Stok");
        stockField.setValue(1);
        stockField.setMin(1);
        stockField.setWidth("32%");
        stockField.setRequiredIndicatorVisible(true);

        HorizontalLayout priceStockRow = new HorizontalLayout(priceField, stockField);
        priceStockRow.setWidthFull();
        priceStockRow.setJustifyContentMode(JustifyContentMode.BETWEEN);
        priceStockRow.getElement().getStyle().set("margin-bottom", "16px");

        // 4. Kondisi Barang
        RadioButtonGroup<ConditionType> conditionGroup = new RadioButtonGroup<>("Kondisi Barang");
        conditionGroup.setItems(ConditionType.BEKAS, ConditionType.BARU);
        conditionGroup.setValue(ConditionType.BEKAS);
        conditionGroup.setRequiredIndicatorVisible(true);
        conditionGroup.getElement().getStyle().set("margin-bottom", "16px");

        // 5. Checklist Warga SMKN 24
        Checkbox isSchoolMarketCheck = new Checkbox("Tampilkan di Pasar SMKN 24 (Khusus Warga Sekolah)");
        isSchoolMarketCheck.setValue(true);
        isSchoolMarketCheck.getElement().getStyle()
            .set("font-weight", "600")
            .set("color", "#001934")
            .set("margin-bottom", "20px");

        // 6. Upload Multi-Foto (maks 5 foto)
        Span uploadLabel = new Span("Foto Barang (Maks. 5 foto)");
        uploadLabel.getElement().getStyle()
            .set("font-size", "14px")
            .set("font-weight", "600")
            .set("color", "#001934")
            .set("display", "block")
            .set("margin-bottom", "4px");

        Span uploadHint = new Span("Format: JPG, PNG, WEBP · Maks. 5 MB per foto · Foto pertama = foto utama produk");
        uploadHint.getElement().getStyle()
            .set("font-size", "12px")
            .set("color", "#94A3B8")
            .set("display", "block")
            .set("margin-bottom", "10px");

        // Grid pratinjau foto
        Div previewGrid = new Div();
        previewGrid.getElement().getStyle()
            .set("display", "flex")
            .set("flex-wrap", "wrap")
            .set("gap", "10px")
            .set("margin-bottom", "12px");

        // Counter badge
        Span photoCount = new Span("0 / 5 foto");
        photoCount.getElement().getStyle()
            .set("font-size", "12px")
            .set("color", "#64748B")
            .set("display", "block")
            .set("margin-bottom", "8px");

        // Multi-file upload component
        MultiFileMemoryBuffer multiBuffer = new MultiFileMemoryBuffer();
        Upload multiUpload = new Upload(multiBuffer);
        multiUpload.setAcceptedFileTypes("image/jpeg", "image/png", "image/webp");
        multiUpload.setMaxFileSize(5 * 1024 * 1024);
        multiUpload.setMaxFiles(5);
        multiUpload.setWidthFull();

        multiUpload.addSucceededListener(event -> {
            if (uploadedImagePaths.size() >= 5) {
                Notification warn = Notification.show("Maksimal 5 foto sudah tercapai.", 2000, Notification.Position.TOP_CENTER);
                warn.addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            try {
                InputStream inputStream = multiBuffer.getInputStream(event.getFileName());
                String originalFileName = event.getFileName();
                String extension = "";
                int dotIdx = originalFileName.lastIndexOf('.');
                if (dotIdx > 0) extension = originalFileName.substring(dotIdx);

                String newFileName = "prod_" + System.currentTimeMillis() + extension;
                // URL path yang di-serve oleh WebMvcConfig → /images/uploads/{filename}
                String relativePath = "images/uploads/" + newFileName;

                byte[] bytes = inputStream.readAllBytes();

                // Tulis ke {project_root}/uploads/ — direktori yang didaftarkan
                // di WebMvcConfig sebagai static resource handler
                File uploadDir = new File(WebMvcConfig.UPLOAD_BASE_DIR);
                if (!uploadDir.exists()) uploadDir.mkdirs();
                try (FileOutputStream out = new FileOutputStream(new File(uploadDir, newFileName))) {
                    out.write(bytes);
                }

                boolean isFirst = uploadedImagePaths.isEmpty();
                uploadedImagePaths.add(relativePath);
                photoCount.setText(uploadedImagePaths.size() + " / 5 foto");

                // Kartu pratinjau per foto
                Div thumbCard = new Div();
                thumbCard.getElement().getStyle()
                    .set("position", "relative")
                    .set("width", "100px")
                    .set("height", "100px")
                    .set("border-radius", "10px")
                    .set("overflow", "hidden")
                    .set("border", isFirst ? "2px solid #001934" : "1px solid #CBD5E1")
                    .set("flex-shrink", "0");

                Image thumbImg = new Image(relativePath, originalFileName);
                thumbImg.getElement().getStyle()
                    .set("width", "100%")
                    .set("height", "100%")
                    .set("object-fit", "cover");

                // Tombol ✕ hapus
                Button btnRemove = new Button("✕");
                btnRemove.getElement().getStyle()
                    .set("position", "absolute")
                    .set("top", "4px")
                    .set("right", "4px")
                    .set("background", "rgba(0,0,0,0.55)")
                    .set("color", "#FFFFFF")
                    .set("border", "none")
                    .set("border-radius", "50%")
                    .set("width", "22px")
                    .set("height", "22px")
                    .set("font-size", "11px")
                    .set("cursor", "pointer")
                    .set("display", "flex")
                    .set("align-items", "center")
                    .set("justify-content", "center")
                    .set("padding", "0")
                    .set("min-width", "0")
                    .set("z-index", "2");

                btnRemove.addClickListener(ev -> {
                    uploadedImagePaths.remove(relativePath);
                    previewGrid.remove(thumbCard);
                    photoCount.setText(uploadedImagePaths.size() + " / 5 foto");
                    // Update badge "Utama" ke foto pertama berikutnya
                    refreshMainBadge(previewGrid);
                });

                thumbCard.add(thumbImg, btnRemove);

                // Badge "Utama" hanya di foto pertama
                if (isFirst) {
                    Div mainBadge = new Div(new Text("Utama"));
                    mainBadge.getElement().getStyle()
                        .set("position", "absolute")
                        .set("bottom", "0")
                        .set("left", "0")
                        .set("right", "0")
                        .set("background", "rgba(0, 25, 52, 0.75)")
                        .set("color", "#F5C45E")
                        .set("font-size", "10px")
                        .set("font-weight", "700")
                        .set("text-align", "center")
                        .set("padding", "3px 0")
                        .set("z-index", "1");
                    mainBadge.addClassName("main-photo-badge");
                    thumbCard.add(mainBadge);
                }

                previewGrid.add(thumbCard);

                Notification ok = Notification.show("Foto berhasil ditambahkan!", 1500, Notification.Position.TOP_CENTER);
                ok.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            } catch (Exception ex) {
                Notification err = Notification.show("Gagal mengunggah foto: " + ex.getMessage(), 3000, Notification.Position.TOP_CENTER);
                err.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        Div uploadWrapper = new Div(uploadLabel, uploadHint, photoCount, previewGrid, multiUpload);
        uploadWrapper.getElement().getStyle().set("margin-bottom", "20px");

        // 7. Deskripsi Barang
        TextArea descArea = new TextArea("Deskripsi & Kondisi Detail Barang");
        descArea.setPlaceholder("Jelaskan kondisi barang, kelengkapan, minus (jika ada), atau alasan dijual...");
        descArea.setWidthFull();
        descArea.setHeight("120px");
        descArea.getElement().getStyle().set("margin-bottom", "28px");

        // Submit Button
        Button btnSubmit = new Button("Tayangkan Barang Jualan");
        btnSubmit.setWidthFull();
        btnSubmit.getElement().getStyle()
            .set("background", "#001934")
            .set("color", "#FFFFFF")
            .set("font-weight", "700")
            .set("font-size", "15px")
            .set("padding", "14px")
            .set("border-radius", "8px")
            .set("border", "none")
            .set("cursor", "pointer");

        btnSubmit.addClickListener(e -> {
            String name = nameField.getValue();
            Category category = categoryCombo.getValue();
            Double priceVal = priceField.getValue();
            Integer stockVal = stockField.getValue();
            ConditionType condition = conditionGroup.getValue();
            boolean isSchoolMarket = isSchoolMarketCheck.getValue();
            String desc = descArea.getValue();

            if (name == null || name.isBlank() || category == null ||
                priceVal == null || priceVal <= 0 || stockVal == null || stockVal <= 0) {
                errorBox.setText("Mohon lengkapi Nama Barang, Kategori, Harga, dan Stok dengan benar.");
                errorBox.setVisible(true);
                return;
            }

            User currentUser = AuthGuard.getCurrentUser();
            if (currentUser == null) {
                errorBox.setText("Silakan login terlebih dahulu untuk menjual barang.");
                errorBox.setVisible(true);
                UI.getCurrent().navigate("login");
                return;
            }

            // Bangun JSON array dari semua foto
            String imagesJson;
            if (!uploadedImagePaths.isEmpty()) {
                StringBuilder sb = new StringBuilder("[");
                for (int idx = 0; idx < uploadedImagePaths.size(); idx++) {
                    sb.append("\"").append(uploadedImagePaths.get(idx)).append("\"");
                    if (idx < uploadedImagePaths.size() - 1) sb.append(",");
                }
                sb.append("]");
                imagesJson = sb.toString();
            } else {
                imagesJson = "[\"images/buku.jpeg\"]";
            }

            Product product = new Product();
            product.setName(name.trim());
            product.setCategory(category);
            product.setPrice(BigDecimal.valueOf(priceVal));
            product.setStock(stockVal);
            product.setConditionType(condition);
            product.setSchoolMarket(isSchoolMarket);
            product.setDescription(desc);
            product.setStatus(ProductStatus.ACTIVE);
            product.setSeller(currentUser);
            product.setImages(imagesJson);

            try {
                productService.saveProduct(product);
                Notification notif = Notification.show("Barang berhasil ditayangkan di ReWear.", 3000, Notification.Position.TOP_CENTER);
                notif.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                UI.getCurrent().navigate("");
            } catch (Exception ex) {
                errorBox.setText("Gagal menyimpan barang: " + ex.getMessage());
                errorBox.setVisible(true);
            }
        });

        container.add(pageTitle, pageSub, errorBox, nameField, categoryCombo,
            priceStockRow, conditionGroup, isSchoolMarketCheck,
            uploadWrapper, descArea, btnSubmit);
        add(container);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (AuthGuard.getCurrentUser() == null) {
            event.forwardTo("login");
        }
    }

    /**
     * Update badge "Utama" agar selalu tampil di kartu foto pertama
     * setelah pengguna menghapus salah satu foto.
     */
    private void refreshMainBadge(Div previewGrid) {
        previewGrid.getChildren().findFirst().ifPresent(firstCard -> {
            Div card = (Div) firstCard;
            card.getElement().getStyle().set("border", "2px solid #001934");
            boolean hasBadge = card.getChildren()
                .anyMatch(c -> c instanceof Div && c.getElement().getClassList().contains("main-photo-badge"));
            if (!hasBadge) {
                Div mainBadge = new Div(new Text("Utama"));
                mainBadge.addClassName("main-photo-badge");
                mainBadge.getElement().getStyle()
                    .set("position", "absolute")
                    .set("bottom", "0")
                    .set("left", "0")
                    .set("right", "0")
                    .set("background", "rgba(0, 25, 52, 0.75)")
                    .set("color", "#F5C45E")
                    .set("font-size", "10px")
                    .set("font-weight", "700")
                    .set("text-align", "center")
                    .set("padding", "3px 0")
                    .set("z-index", "1");
                card.add(mainBadge);
            }
        });
    }
}
