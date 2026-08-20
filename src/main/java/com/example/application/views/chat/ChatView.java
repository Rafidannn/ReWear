package com.example.application.views.chat;

import com.example.application.model.chat.Conversation;
import com.example.application.model.chat.Message;
import com.example.application.model.product.Product;
import com.example.application.model.user.User;
import com.example.application.service.chat.ChatService;
import com.example.application.service.product.ProductService;
import com.example.application.service.user.UserService;
import com.example.application.service.moderation.ModerationService;
import com.example.application.util.AuthGuard;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@PageTitle("Chat - ReWear")
@Route(value = "chat", layout = MainLayout.class)
public class ChatView extends Div implements BeforeEnterObserver {

    private final ChatService chatService;
    private final UserService userService;
    private final ProductService productService;
    private final ModerationService moderationService;

    private User currentUser;
    private Conversation activeConversation;

    private final Div leftSidebar = new Div();
    private final Div rightChatArea = new Div();
    private final TextField messageInput = new TextField();

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    public ChatView(ChatService chatService, UserService userService,
                    ProductService productService, ModerationService moderationService) {
        this.chatService = chatService;
        this.userService = userService;
        this.productService = productService;
        this.moderationService = moderationService;

        addClassName("rw-chat-page");

        Div wrapper = new Div();
        wrapper.addClassName("rw-chat-wrapper");
        leftSidebar.addClassName("rw-chat-left-sidebar");
        rightChatArea.addClassName("rw-chat-right-area");
        wrapper.add(leftSidebar, rightChatArea);
        add(wrapper);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!AuthGuard.requireLogin(UI.getCurrent())) return;

        currentUser = AuthGuard.getCurrentUser();
        if (currentUser == null) return;

        Map<String, List<String>> params = event.getLocation().getQueryParameters().getParameters();
        String sellerIdStr = params.containsKey("sellerId") ? params.get("sellerId").get(0) : null;
        String productIdStr = params.containsKey("productId") ? params.get("productId").get(0) : null;

        if (sellerIdStr != null && !sellerIdStr.isBlank()) {
            try {
                Long sellerId = Long.parseLong(sellerIdStr);

                // Block self-chat
                if (currentUser.getId() != null && currentUser.getId().equals(sellerId)) {
                    Notification.show("Anda tidak bisa chat dengan diri sendiri.", 3000, Notification.Position.TOP_CENTER);
                    renderSidebar();
                    renderChatArea();
                    return;
                }

                Optional<User> sellerOpt = userService.findById(sellerId);
                if (sellerOpt.isPresent()) {
                    User seller = sellerOpt.get();
                    Product product = null;

                    if (productIdStr != null && !productIdStr.isBlank()) {
                        try {
                            Long productId = Long.parseLong(productIdStr);
                            product = productService.findById(productId).orElse(null);
                        } catch (NumberFormatException ignored) {}
                    }

                    // Get or create DB-backed conversation
                    activeConversation = chatService.getOrCreateConversation(currentUser, seller, product);

                    // Send auto-greeting if this is a brand new conversation (no messages yet)
                    List<Message> existing = chatService.getMessages(activeConversation);
                    if (existing.isEmpty() && product != null) {
                        String greeting = "Halo kak, saya tertarik dengan produk \""
                            + product.getName() + "\". Apakah masih tersedia?";
                        chatService.sendMessage(activeConversation, currentUser, greeting);
                    }
                }
            } catch (NumberFormatException ignored) {}
        }

        renderSidebar();
        renderChatArea();
    }

    // ==========================================
    // LEFT SIDEBAR: CONVERSATION LIST
    // ==========================================

    private void renderSidebar() {
        leftSidebar.removeAll();

        HorizontalLayout titleRow = new HorizontalLayout();
        titleRow.setWidthFull();
        titleRow.setAlignItems(FlexComponent.Alignment.CENTER);
        titleRow.setSpacing(true);
        titleRow.getElement().getStyle().set("margin-bottom", "12px");

        Button btnBack = new Button(VaadinIcon.ARROW_LEFT.create());
        btnBack.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnBack.getElement().getStyle()
            .set("color", "#001934")
            .set("cursor", "pointer")
            .set("padding", "4px 8px")
            .set("background", "#FFFFFF")
            .set("border", "1px solid #E2E8F0")
            .set("border-radius", "8px");
        btnBack.addClickListener(e -> UI.getCurrent().getPage().getHistory().back());

        H3 title = new H3("Pesan");
        title.addClassName("rw-chat-sidebar-title");
        title.getElement().getStyle().set("margin", "0");
        titleRow.add(btnBack, title);

        Div searchWrap = new Div();
        searchWrap.addClassName("rw-chat-search-wrap");
        TextField searchInput = new TextField();
        searchInput.setPlaceholder("Cari percakapan...");
        searchInput.addClassName("rw-chat-search-input");
        searchInput.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchWrap.add(searchInput);

        Div listDiv = new Div();
        listDiv.addClassName("rw-chat-conv-list");

        if (currentUser == null) {
            leftSidebar.add(titleRow, searchWrap, listDiv);
            return;
        }

        List<Conversation> conversations = chatService.getUserConversations(currentUser);

        // If no conversation was specifically selected and we have conversations, default to first
        if (activeConversation == null && !conversations.isEmpty()) {
            activeConversation = conversations.get(0);
        }

        if (conversations.isEmpty()) {
            Paragraph empty = new Paragraph("Belum ada percakapan.");
            empty.getElement().getStyle().set("color", "#94A3B8").set("font-size", "13px")
                .set("text-align", "center").set("padding", "24px 0");
            listDiv.add(empty);
        }

        for (Conversation conv : conversations) {
            // Determine the other party safely
            User other = (conv.getBuyer() != null && conv.getBuyer().getId().equals(currentUser.getId()))
                ? conv.getSeller() : conv.getBuyer();

            Div item = new Div();
            item.addClassName("rw-chat-conv-item");
            if (activeConversation != null && conv.getId().equals(activeConversation.getId())) {
                item.addClassName("active");
            }

            // Avatar
            Div avatarWrap = new Div();
            avatarWrap.addClassName("rw-chat-avatar-wrap");

            String otherName = (other != null && other.getFullName() != null) ? other.getFullName() : "Pengguna";
            String avatarUrl = (other != null) ? other.getAvatarUrl() : null;

            if (avatarUrl != null && !avatarUrl.isBlank() && !avatarUrl.contains("buku.jpeg")) {
                Image img = new Image(avatarUrl, otherName);
                img.addClassName("rw-chat-avatar-img");
                avatarWrap.add(img);
            } else {
                Span initials = new Span(getInitials(otherName));
                initials.addClassName("rw-chat-avatar-initials");
                avatarWrap.add(initials);
            }

            Span onlineDot = new Span();
            onlineDot.addClassName("rw-chat-online-dot");
            avatarWrap.add(onlineDot);

            // Text meta
            Div metaDiv = new Div();
            metaDiv.addClassName("rw-chat-conv-meta");

            Div topRow = new Div();
            topRow.addClassName("rw-chat-conv-top");

            Span nameSpan = new Span(otherName);
            nameSpan.addClassName("rw-chat-conv-name");

            String timeStr = conv.getLastMessageAt() != null
                ? conv.getLastMessageAt().format(TIME_FMT) : "";
            Span timeSpan = new Span(timeStr);
            timeSpan.addClassName("rw-chat-conv-time");
            topRow.add(nameSpan, timeSpan);

            // Last message preview
            List<Message> msgs = chatService.getMessages(conv);
            String lastMsg = msgs.isEmpty() ? "Percakapan baru" : msgs.get(msgs.size() - 1).getBody();
            if (lastMsg != null && lastMsg.length() > 45) lastMsg = lastMsg.substring(0, 45) + "...";
            Paragraph lastMsgSpan = new Paragraph(lastMsg != null ? lastMsg : "");
            lastMsgSpan.addClassName("rw-chat-conv-lastmsg");
            metaDiv.add(topRow, lastMsgSpan);

            item.add(avatarWrap, metaDiv);
            final Conversation convRef = conv;
            item.addClickListener(e -> {
                activeConversation = convRef;
                renderSidebar();
                renderChatArea();
            });
            listDiv.add(item);
        }

        leftSidebar.add(title, searchWrap, listDiv);
    }

    // ==========================================
    // RIGHT PANEL: ACTIVE CHAT ROOM
    // ==========================================

    private void renderChatArea() {
        rightChatArea.removeAll();

        if (activeConversation == null) {
            Div emptyArea = new Div();
            emptyArea.addClassName("rw-chat-empty-state");
            emptyArea.getElement().setProperty("innerHTML",
                "<div style='text-align:center;padding:100px 20px;'>" +
                "<svg width='64' height='64' viewBox='0 0 24 24' fill='none' stroke='#CBD5E1' stroke-width='1.5' style='margin-bottom:16px;'><path d='M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z'/></svg>" +
                "<h3 style='color:#001934;margin-bottom:8px;'>Pilih Percakapan</h3>" +
                "<p style='color:#64748B;font-size:14px;'>Pilih percakapan di sebelah kiri atau buka detail produk dan klik Chat Penjual.</p>" +
                "</div>"
            );
            rightChatArea.add(emptyArea);
            return;
        }

        User other = (activeConversation.getBuyer() != null && activeConversation.getBuyer().getId().equals(currentUser.getId()))
            ? activeConversation.getSeller() : activeConversation.getBuyer();

        String otherName = (other != null && other.getFullName() != null) ? other.getFullName() : "Pengguna";

        // 1. Header Bar
        Div chatHeaderBar = new Div();
        chatHeaderBar.addClassName("rw-chat-header-bar");

        Div headerInfo = new Div();
        headerInfo.addClassName("rw-chat-header-info");

        H4 headerName = new H4(otherName);
        headerName.addClassName("rw-chat-header-name");

        Span statusSpan = new Span();
        statusSpan.addClassName("rw-chat-header-status");
        statusSpan.getElement().setProperty("innerHTML", "<span class='dot-online'>●</span> Online");
        headerInfo.add(headerName, statusSpan);

        Button btnReport = new Button("Laporkan Pengguna", VaadinIcon.WARNING.create());
        btnReport.addClassName("rw-btn-report-user");
        btnReport.addClickListener(e -> openReportUserDialog(other, activeConversation));

        chatHeaderBar.add(headerInfo, btnReport);

        // 2. Product Banner (if conversation has a product context)
        Div productBannerCard = new Div();
        productBannerCard.addClassName("rw-chat-product-banner");

        Product product = activeConversation.getProduct();
        if (product != null) {
            String imgUrl = extractImgUrl(product.getImages());
            Image pImg = new Image(imgUrl, product.getName());
            pImg.addClassName("rw-chat-p-img");

            Div pMeta = new Div();
            pMeta.addClassName("rw-chat-p-meta");

            H5 pTitle = new H5(product.getName());
            pTitle.addClassName("rw-chat-p-title");

            String priceStr = product.getPrice() != null
                ? "Rp " + String.format("%,.0f", product.getPrice()) : "";
            Span pPrice = new Span(priceStr);
            pPrice.addClassName("rw-chat-p-price");

            pMeta.add(pTitle, pPrice);

            Button btnBuyNow = new Button("Beli Sekarang");
            btnBuyNow.addClassName("rw-chat-btn-buy");
            Long prodId = product.getId();
            btnBuyNow.addClickListener(e -> UI.getCurrent().navigate("product/" + prodId));

            productBannerCard.add(pImg, pMeta, btnBuyNow);
        }

        // 3. Messages Stream
        Div messagesStream = new Div();
        messagesStream.addClassName("rw-chat-stream");

        Div datePill = new Div(new Span("Hari ini"));
        datePill.addClassName("rw-chat-date-pill");
        messagesStream.add(datePill);

        List<Message> messages = chatService.getMessages(activeConversation);
        for (Message msg : messages) {
            Div bubbleRow = new Div();
            bubbleRow.addClassName("rw-chat-bubble-row");
            boolean isMe = msg.getSender() != null && msg.getSender().getId().equals(currentUser.getId());
            bubbleRow.addClassName(isMe ? "me" : "other");

            Div bubbleCard = new Div();
            bubbleCard.addClassName("rw-chat-bubble");

            Paragraph txt = new Paragraph(msg.getBody());
            txt.addClassName("rw-chat-msg-txt");

            Span time = new Span(msg.getCreatedAt() != null ? msg.getCreatedAt().format(TIME_FMT) : "");
            time.addClassName("rw-chat-msg-time");

            bubbleCard.add(txt, time);
            bubbleRow.add(bubbleCard);
            messagesStream.add(bubbleRow);
        }

        // 4. Input Footer
        Div inputFooter = new Div();
        inputFooter.addClassName("rw-chat-input-footer");

        messageInput.setPlaceholder("Ketik pesan di sini...");
        messageInput.addClassName("rw-chat-input-field");
        Span emojiIcon = new Span("😊");
        emojiIcon.getElement().getStyle().set("cursor", "pointer").set("font-size", "18px").set("padding-right", "8px");
        messageInput.setSuffixComponent(emojiIcon);

        Button btnSend = new Button(VaadinIcon.PAPERPLANE.create());
        btnSend.addClassName("rw-chat-btn-send");
        btnSend.addClickListener(e -> sendMessage());

        messageInput.getElement().addEventListener("keydown", e -> sendMessage())
            .setFilter("event.key === 'Enter'");

        Button btnAttach = new Button(VaadinIcon.PLUS.create());
        btnAttach.addClassName("rw-chat-icon-action");
        Button btnImgUpload = new Button(VaadinIcon.PICTURE.create());
        btnImgUpload.addClassName("rw-chat-icon-action");

        inputFooter.add(btnAttach, btnImgUpload, messageInput, btnSend);

        rightChatArea.add(chatHeaderBar, productBannerCard, messagesStream, inputFooter);

        // Auto-scroll to bottom after render
        UI.getCurrent().getPage().executeJs(
            "setTimeout(function(){ var el = document.querySelector('.rw-chat-stream'); if(el) el.scrollTop = el.scrollHeight; }, 100);"
        );
    }

    private void sendMessage() {
        if (currentUser == null || activeConversation == null) return;
        String text = messageInput.getValue();
        if (text == null || text.isBlank()) return;

        chatService.sendMessage(activeConversation, currentUser, text.trim());
        messageInput.setValue("");
        renderSidebar();
        renderChatArea();
    }

    private String getInitials(String name) {
        if (name == null || name.isEmpty()) return "U";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
        }
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }

    private String extractImgUrl(String imagesJson) {
        if (imagesJson == null || imagesJson.isBlank()) return "images/buku.jpeg";
        String s = imagesJson.trim();
        if (s.startsWith("[")) {
            s = s.replace("[", "").replace("]", "").replace("\"", "").replace("'", "").trim();
            String[] parts = s.split(",");
            if (parts.length > 0 && !parts[0].trim().isEmpty()) return parts[0].trim();
        }
        if (s.startsWith("http") || s.startsWith("images/") || s.startsWith("uploads/")) return s;
        return "images/buku.jpeg";
    }

    private void openReportUserDialog(User targetUser, Conversation conversation) {
        if (!AuthGuard.requireLogin(UI.getCurrent())) return;
        User user = AuthGuard.getCurrentUser();

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Laporkan Pengguna");
        dialog.setWidth("460px");

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(false);

        String targetName = targetUser != null && targetUser.getFullName() != null ? targetUser.getFullName() : "Pengguna ini";
        Paragraph info = new Paragraph("Laporkan aktivitas atau pesan yang melanggar ketentuan dari " + targetName + ".");
        info.getElement().getStyle().set("font-size", "13px").set("color", "#64748B").set("margin", "0");

        ComboBox<String> reasonCombo = new ComboBox<>("Alasan Pelaporan");
        reasonCombo.setItems(
            "Spam / Iklan Mengganggu",
            "Kata-kata Kasar / Pelecehan / Ancaman",
            "Indikasi Penipuan / Mengajak Transaksi di Luar ReWear",
            "Akun Palsu / Bukan Warga SMKN 24",
            "Lainnya"
        );
        reasonCombo.setValue("Indikasi Penipuan / Mengajak Transaksi di Luar ReWear");
        reasonCombo.setWidthFull();

        TextArea descField = new TextArea("Rincian Laporan (Opsional)");
        descField.setPlaceholder("Jelaskan pesan atau tindakan yang melanggar...");
        descField.setWidthFull();
        descField.setMaxLength(500);

        layout.add(info, reasonCombo, descField);

        Button btnCancel = new Button("Batal", e -> dialog.close());
        btnCancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button btnSubmit = new Button("Kirim Laporan", e -> {
            String reason = reasonCombo.getValue();
            String desc = descField.getValue();
            if (reason == null || reason.isBlank()) {
                Notification.show("Harap pilih alasan pelaporan.", 3000, Notification.Position.TOP_CENTER);
                return;
            }

            try {
                moderationService.reportUser(user, targetUser, conversation, reason, desc);
                Notification.show("Laporan pengguna telah diteruskan ke tim Moderator ReWear.", 3500, Notification.Position.TOP_CENTER);
                dialog.close();
            } catch (Exception ex) {
                Notification.show("Gagal mengirim laporan: " + ex.getMessage(), 3000, Notification.Position.TOP_CENTER);
            }
        });
        btnSubmit.getElement().getStyle()
            .set("background", "#DC2626").set("color", "#FFFFFF").set("font-weight", "700")
            .set("border-radius", "8px").set("border", "none").set("cursor", "pointer");

        dialog.getFooter().add(btnCancel, btnSubmit);
        dialog.add(layout);
        dialog.open();
    }
}
