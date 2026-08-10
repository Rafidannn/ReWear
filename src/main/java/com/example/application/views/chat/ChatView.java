package com.example.application.views.chat;

import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.*;

@PageTitle("Chat - ReWear")
@Route(value = "chat", layout = MainLayout.class)
public class ChatView extends Div implements BeforeEnterObserver {

    // Data model for conversation
    public static class ChatConversation {
        private String id;
        private String name;
        private String avatarUrl;
        private String initials;
        private String lastMessage;
        private String time;
        private boolean online;
        private String activeProductTitle;
        private String activeProductPrice;
        private String activeProductImg;
        private List<ChatMessage> messages;

        public ChatConversation(String id, String name, String avatarUrl, String initials, String lastMessage, String time, boolean online, String activeProductTitle, String activeProductPrice, String activeProductImg) {
            this.id = id;
            this.name = name;
            this.avatarUrl = avatarUrl;
            this.initials = initials;
            this.lastMessage = lastMessage;
            this.time = time;
            this.online = online;
            this.activeProductTitle = activeProductTitle;
            this.activeProductPrice = activeProductPrice;
            this.activeProductImg = activeProductImg;
            this.messages = new ArrayList<>();
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getAvatarUrl() { return avatarUrl; }
        public String getInitials() { return initials; }
        public String getLastMessage() { return lastMessage; }
        public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
        public String getTime() { return time; }
        public void setTime(String time) { this.time = time; }
        public boolean isOnline() { return online; }
        public String getActiveProductTitle() { return activeProductTitle; }
        public void setActiveProductTitle(String t) { this.activeProductTitle = t; }
        public String getActiveProductPrice() { return activeProductPrice; }
        public void setActiveProductPrice(String p) { this.activeProductPrice = p; }
        public String getActiveProductImg() { return activeProductImg; }
        public void setActiveProductImg(String img) { this.activeProductImg = img; }
        public List<ChatMessage> getMessages() { return messages; }
    }

    public static class ChatMessage {
        private String sender; // "me" or "other"
        private String text;
        private String time;

        public ChatMessage(String sender, String text, String time) {
            this.sender = sender;
            this.text = text;
            this.time = time;
        }

        public String getSender() { return sender; }
        public String getText() { return text; }
        public String getTime() { return time; }
    }

    private final List<ChatConversation> conversations = new ArrayList<>();
    private ChatConversation activeConversation = null;

    private final Div leftSidebar = new Div();
    private final Div rightChatArea = new Div();

    private final Div chatHeaderBar = new Div();
    private final Div productBannerCard = new Div();
    private final Div messagesStream = new Div();
    private final TextField messageInput = new TextField();

    public ChatView() {
        addClassName("rw-chat-page");

        // Initialize sample conversations matching the exact screenshot design
        initSampleData();

        Div wrapper = new Div();
        wrapper.addClassName("rw-chat-wrapper");

        // Left Panel: Conversations List
        leftSidebar.addClassName("rw-chat-left-sidebar");

        // Right Panel: Active Chat Room
        rightChatArea.addClassName("rw-chat-right-area");

        wrapper.add(leftSidebar, rightChatArea);
        add(wrapper);
    }

    private void initSampleData() {
        conversations.clear();

        // Conversation 1: Budi Setiawan
        ChatConversation c1 = new ChatConversation(
            "budi", "Budi Setiawan", "images/buku.jpeg", "BS",
            "Apakah harganya bisa kurang sedikit?", "14:20", true,
            "Totebag Denim Recycled - Limited Edition", "Rp 85.000", "images/buku.jpeg"
        );
        c1.getMessages().add(new ChatMessage("other", "Halo kak, saya tertarik dengan Totebag Denimnya. Apakah harganya bisa kurang sedikit?", "14:15"));
        conversations.add(c1);

        // Conversation 2: Siti Aminah
        ChatConversation c2 = new ChatConversation(
            "siti", "Siti Aminah", "images/colokan.webp", "SA",
            "Barangnya masih ada kak?", "10:45", true,
            "Minimalist Graphic Tee", "Rp 45.000", "images/colokan.webp"
        );
        c2.getMessages().add(new ChatMessage("other", "Halo kak, barangnya masih ada kak?", "10:45"));
        conversations.add(c2);

        // Conversation 3: Rizky Kurniawan
        ChatConversation c3 = new ChatConversation(
            "rizky", "Rizky Kurniawan", null, "RK",
            "Oke, saya pesan sekarang ya.", "Kemarin", false,
            "Vans Old Skool Classic", "Rp 350.000", "images/kipas.jpg"
        );
        c3.getMessages().add(new ChatMessage("other", "Oke, saya pesan sekarang ya.", "Kemarin"));
        conversations.add(c3);

        // Default active conversation
        activeConversation = c1;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Map<String, List<String>> params = event.getLocation().getQueryParameters().getParameters();

        String sellerName = params.containsKey("seller") ? params.get("seller").get(0) : null;
        String productTitle = params.containsKey("product") ? params.get("product").get(0) : null;
        String productPrice = params.containsKey("price") ? params.get("price").get(0) : null;
        String productImg = params.containsKey("img") ? params.get("img").get(0) : null;

        if (sellerName != null && !sellerName.trim().isEmpty()) {
            // Find existing or create new conversation with seller
            Optional<ChatConversation> match = conversations.stream()
                .filter(c -> c.getName().equalsIgnoreCase(sellerName.trim()))
                .findFirst();

            if (match.isPresent()) {
                activeConversation = match.get();
                if (productTitle != null) activeConversation.setActiveProductTitle(productTitle);
                if (productPrice != null) activeConversation.setActiveProductPrice(productPrice);
                if (productImg != null) activeConversation.setActiveProductImg(productImg);
            } else {
                ChatConversation newConv = new ChatConversation(
                    "custom_" + System.currentTimeMillis(), sellerName, "images/buku.jpeg", getInitials(sellerName),
                    "Halo kak, saya berminat dengan produk ini.", "Sekarang", true,
                    productTitle != null ? productTitle : "Produk Thrift ReWear",
                    productPrice != null ? productPrice : "Rp 100.000",
                    productImg != null ? productImg : "images/buku.jpeg"
                );
                newConv.getMessages().add(new ChatMessage("me", "Halo kak, saya berminat dengan produk ini. Apakah masih tersedia?", "Sekarang"));
                conversations.add(0, newConv);
                activeConversation = newConv;
            }
        }

        renderSidebar();
        renderChatArea();
    }

    private String getInitials(String name) {
        if (name == null || name.isEmpty()) return "U";
        String[] parts = name.split(" ");
        if (parts.length >= 2) {
            return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
        }
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }

    // ==========================================
    // LEFT SIDEBAR: CONVERSATION LIST
    // ==========================================

    private void renderSidebar() {
        leftSidebar.removeAll();

        // Header: Pesan
        H3 title = new H3("Pesan");
        title.addClassName("rw-chat-sidebar-title");

        // Search Bar
        Div searchWrap = new Div();
        searchWrap.addClassName("rw-chat-search-wrap");

        TextField searchInput = new TextField();
        searchInput.setPlaceholder("Cari percakapan...");
        searchInput.addClassName("rw-chat-search-input");
        searchInput.setPrefixComponent(VaadinIcon.SEARCH.create());

        searchWrap.add(searchInput);

        // List Container
        Div listDiv = new Div();
        listDiv.addClassName("rw-chat-conv-list");

        for (ChatConversation conv : conversations) {
            Div item = new Div();
            item.addClassName("rw-chat-conv-item");
            if (activeConversation != null && conv.getId().equals(activeConversation.getId())) {
                item.addClassName("active");
            }

            // Avatar / Circle
            Div avatarWrap = new Div();
            avatarWrap.addClassName("rw-chat-avatar-wrap");

            if (conv.getAvatarUrl() != null && !conv.getAvatarUrl().isEmpty()) {
                Image img = new Image(conv.getAvatarUrl(), conv.getName());
                img.addClassName("rw-chat-avatar-img");
                avatarWrap.add(img);
            } else {
                Span initials = new Span(conv.getInitials());
                initials.addClassName("rw-chat-avatar-initials");
                avatarWrap.add(initials);
            }

            if (conv.isOnline()) {
                Span onlineDot = new Span();
                onlineDot.addClassName("rw-chat-online-dot");
                avatarWrap.add(onlineDot);
            }

            // Text Meta
            Div metaDiv = new Div();
            metaDiv.addClassName("rw-chat-conv-meta");

            Div topRow = new Div();
            topRow.addClassName("rw-chat-conv-top");

            Span nameSpan = new Span(conv.getName());
            nameSpan.addClassName("rw-chat-conv-name");

            Span timeSpan = new Span(conv.getTime());
            timeSpan.addClassName("rw-chat-conv-time");

            topRow.add(nameSpan, timeSpan);

            Paragraph lastMsg = new Paragraph(conv.getLastMessage());
            lastMsg.addClassName("rw-chat-conv-lastmsg");

            metaDiv.add(topRow, lastMsg);

            item.add(avatarWrap, metaDiv);

            // Select conversation on click
            item.addClickListener(e -> {
                activeConversation = conv;
                renderSidebar();
                renderChatArea();
            });

            listDiv.add(item);
        }

        leftSidebar.add(title, searchWrap, listDiv);
    }

    // ==========================================
    // RIGHT PANEL: ACTIVE ROOM CHAT
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
                "<p style='color:#64748B;font-size:14px;'>Pilih salah satu pesan di sebelah kiri untuk mulai mengobrol.</p>" +
                "</div>"
            );
            rightChatArea.add(emptyArea);
            return;
        }

        // 1. Chat Header Bar
        chatHeaderBar.removeAll();
        chatHeaderBar.addClassName("rw-chat-header-bar");

        Div headerInfo = new Div();
        headerInfo.addClassName("rw-chat-header-info");

        H4 headerName = new H4(activeConversation.getName());
        headerName.addClassName("rw-chat-header-name");

        Span statusSpan = new Span();
        statusSpan.addClassName("rw-chat-header-status");
        if (activeConversation.isOnline()) {
            statusSpan.getElement().setProperty("innerHTML", "<span class='dot-online'>●</span> Online");
        } else {
            statusSpan.setText("Offline");
        }

        headerInfo.add(headerName, statusSpan);

        // Tombol Laporkan Pengguna (Red outline button)
        Button btnReport = new Button("Laporkan Pengguna", VaadinIcon.WARNING.create());
        btnReport.addClassName("rw-btn-report-user");
        btnReport.addClickListener(e -> Notification.show("Laporan pengguna dikirim ke tim Moderator ReWear.", 2500, Notification.Position.TOP_CENTER));

        chatHeaderBar.add(headerInfo, btnReport);

        // 2. Product Info Banner Box (Attached at top of chat)
        productBannerCard.removeAll();
        productBannerCard.addClassName("rw-chat-product-banner");

        if (activeConversation.getActiveProductTitle() != null) {
            Image pImg = new Image(
                activeConversation.getActiveProductImg() != null ? activeConversation.getActiveProductImg() : "images/buku.jpeg",
                activeConversation.getActiveProductTitle()
            );
            pImg.addClassName("rw-chat-p-img");

            Div pMeta = new Div();
            pMeta.addClassName("rw-chat-p-meta");

            H5 pTitle = new H5(activeConversation.getActiveProductTitle());
            pTitle.addClassName("rw-chat-p-title");

            Span pPrice = new Span(activeConversation.getActiveProductPrice());
            pPrice.addClassName("rw-chat-p-price");

            pMeta.add(pTitle, pPrice);

            Button btnBuyNow = new Button("Beli Sekarang");
            btnBuyNow.addClassName("rw-chat-btn-buy");
            btnBuyNow.addClickListener(e -> UI.getCurrent().navigate("cart"));

            productBannerCard.add(pImg, pMeta, btnBuyNow);
        }

        // 3. Chat Messages Stream Container
        messagesStream.removeAll();
        messagesStream.addClassName("rw-chat-stream");

        // Date separator pill
        Div datePill = new Div(new Span("Hari ini"));
        datePill.addClassName("rw-chat-date-pill");
        messagesStream.add(datePill);

        for (ChatMessage msg : activeConversation.getMessages()) {
            Div bubbleRow = new Div();
            bubbleRow.addClassName("rw-chat-bubble-row");
            if ("me".equals(msg.getSender())) {
                bubbleRow.addClassName("me");
            } else {
                bubbleRow.addClassName("other");
            }

            Div bubbleCard = new Div();
            bubbleCard.addClassName("rw-chat-bubble");

            Paragraph txt = new Paragraph(msg.getText());
            txt.addClassName("rw-chat-msg-txt");

            Span time = new Span(msg.getTime());
            time.addClassName("rw-chat-msg-time");

            bubbleCard.add(txt, time);
            bubbleRow.add(bubbleCard);
            messagesStream.add(bubbleRow);
        }

        // 4. Chat Input Bar Footer
        Div inputFooter = new Div();
        inputFooter.addClassName("rw-chat-input-footer");

        Button btnAttach = new Button(VaadinIcon.PLUS.create());
        btnAttach.addClassName("rw-chat-icon-action");
        btnAttach.addClickListener(e -> Notification.show("Lampiran file", 1500, Notification.Position.TOP_CENTER));

        Button btnImgUpload = new Button(VaadinIcon.PICTURE.create());
        btnImgUpload.addClassName("rw-chat-icon-action");
        btnImgUpload.addClickListener(e -> Notification.show("Kirim Gambar", 1500, Notification.Position.TOP_CENTER));

        messageInput.setPlaceholder("Ketik pesan di sini...");
        messageInput.addClassName("rw-chat-input-field");
        Span emojiIcon = new Span("😊");
        emojiIcon.getElement().getStyle().set("cursor", "pointer").set("font-size", "18px").set("padding-right", "8px");
        messageInput.setSuffixComponent(emojiIcon);

        Button btnSend = new Button(VaadinIcon.PAPERPLANE.create());
        btnSend.addClassName("rw-chat-btn-send");
        btnSend.addClickListener(e -> sendMessage());

        // Enable sending on Enter keypress
        messageInput.getElement().addEventListener("keydown", e -> sendMessage())
            .setFilter("event.key === 'Enter'");

        inputFooter.add(btnAttach, btnImgUpload, messageInput, btnSend);

        rightChatArea.add(chatHeaderBar, productBannerCard, messagesStream, inputFooter);
    }

    private void sendMessage() {
        String text = messageInput.getValue();
        if (text != null && !text.trim().isEmpty() && activeConversation != null) {
            String currentTime = String.format("%02d:%02d", java.time.LocalTime.now().getHour(), java.time.LocalTime.now().getMinute());
            activeConversation.getMessages().add(new ChatMessage("me", text.trim(), currentTime));
            activeConversation.setLastMessage("Anda: " + text.trim());
            activeConversation.setTime(currentTime);

            messageInput.setValue("");

            renderSidebar();
            renderChatArea();
        }
    }
}
