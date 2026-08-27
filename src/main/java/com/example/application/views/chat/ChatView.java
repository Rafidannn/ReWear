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

import com.vaadin.flow.dom.Element;
import com.vaadin.flow.data.value.ValueChangeMode;

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

    private final Div wrapper = new Div();

    public ChatView(ChatService chatService, UserService userService,
                    ProductService productService, ModerationService moderationService) {
        this.chatService = chatService;
        this.userService = userService;
        this.productService = productService;
        this.moderationService = moderationService;

        addClassName("rw-chat-page");

        Element styleElem = new Element("style");
        styleElem.setText(
            ".rw-chat-page { width: 100%; height: calc(100vh - 84px); box-sizing: border-box; padding: 16px 20px; display: flex; flex-direction: column; background: transparent; }" +
            ".rw-chat-wrapper { display: flex; width: 100%; max-width: 1200px; margin: 0 auto; height: 100%; background: #FFFFFF; border-radius: 16px; border: 1px solid #E2E8F0; box-shadow: 0 4px 20px rgba(0, 25, 52, 0.08); overflow: hidden; }" +
            ".rw-chat-left-sidebar { width: 360px; min-width: 320px; border-right: 1px solid #E2E8F0; display: flex; flex-direction: column; background: #FFFFFF; height: 100%; flex-shrink: 0; }" +
            ".rw-chat-sidebar-header { padding: 16px 18px 12px 18px; border-bottom: 1px solid #F1F5F9; display: flex; flex-direction: column; gap: 12px; }" +
            ".rw-chat-sidebar-title { font-size: 20px; font-weight: 800; color: #001934; margin: 0; }" +
            ".rw-chat-search-wrap { width: 100%; }" +
            ".rw-chat-search-input { width: 100%; }" +
            ".rw-chat-search-input::part(input-field) { background: #F8FAFC; border: 1px solid #E2E8F0; border-radius: 10px; font-size: 13.5px; padding: 2px 10px; }" +
            ".rw-chat-conv-list { flex: 1; overflow-y: auto; display: flex; flex-direction: column; }" +
            ".rw-chat-conv-item { display: flex; align-items: center; gap: 14px; padding: 14px 18px; border-bottom: 1px solid #F8FAFC; cursor: pointer; transition: all 0.15s ease; background: #FFFFFF; }" +
            ".rw-chat-conv-item:hover { background: #F8FAFC; }" +
            ".rw-chat-conv-item.active { background: #EFF6FF; border-left: 4px solid #001934; }" +
            ".rw-chat-avatar-wrap { position: relative; width: 48px; height: 48px; flex-shrink: 0; }" +
            ".rw-chat-avatar-img { width: 48px; height: 48px; border-radius: 50%; object-fit: cover; border: 1.5px solid #E2E8F0; }" +
            ".rw-chat-avatar-initials { width: 48px; height: 48px; border-radius: 50%; background: linear-gradient(135deg, #001934 0%, #1E3A8A 100%); color: #F5C45E; display: flex; align-items: center; justify-content: center; font-weight: 800; font-size: 16px; }" +
            ".rw-chat-online-dot { position: absolute; bottom: 1px; right: 1px; width: 12px; height: 12px; border-radius: 50%; background: #10B981; border: 2px solid #FFFFFF; }" +
            ".rw-chat-conv-meta { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 3px; }" +
            ".rw-chat-conv-top { display: flex; align-items: center; justify-content: space-between; }" +
            ".rw-chat-conv-name { font-size: 14.5px; font-weight: 700; color: #001934; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }" +
            ".rw-chat-conv-time { font-size: 11.5px; font-weight: 600; color: #94A3B8; flex-shrink: 0; margin-left: 8px; }" +
            ".rw-chat-conv-lastmsg { font-size: 13px; color: #64748B; margin: 0; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }" +
            ".rw-chat-right-area { flex: 1; display: flex; flex-direction: column; background: #F8FAFC; height: 100%; position: relative; overflow: hidden; }" +
            ".rw-chat-empty-state { display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100%; padding: 32px; color: #64748B; text-align: center; }" +
            ".rw-chat-header-bar { height: 64px; min-height: 64px; background: #FFFFFF; border-bottom: 1px solid #E2E8F0; display: flex; align-items: center; justify-content: space-between; padding: 0 18px; z-index: 10; }" +
            ".rw-chat-header-left { display: flex; align-items: center; gap: 12px; min-width: 0; }" +
            ".rw-chat-header-info { display: flex; flex-direction: column; gap: 1px; }" +
            ".rw-chat-header-name { font-size: 15px; font-weight: 800; color: #001934; margin: 0; }" +
            ".rw-chat-header-status { font-size: 11.5px; font-weight: 600; color: #10B981; display: flex; align-items: center; gap: 4px; }" +
            ".rw-chat-mob-back-btn { display: none; margin-right: 4px; color: #001934 !important; cursor: pointer; }" +
            ".rw-chat-product-banner { background: #FFFFFF; border-bottom: 1px solid #E2E8F0; padding: 10px 18px; display: flex; align-items: center; gap: 12px; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02); }" +
            ".rw-chat-p-img { width: 44px; height: 44px; border-radius: 8px; object-fit: cover; border: 1px solid #E2E8F0; flex-shrink: 0; }" +
            ".rw-chat-p-meta { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 2px; }" +
            ".rw-chat-p-title { font-size: 13.5px; font-weight: 700; color: #001934; margin: 0; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }" +
            ".rw-chat-p-price { font-size: 12.5px; font-weight: 800; color: #D97706; }" +
            ".rw-chat-btn-buy { background: #F5C45E !important; color: #001934 !important; font-weight: 800 !important; font-size: 12px !important; border-radius: 9999px !important; padding: 6px 14px !important; border: none !important; cursor: pointer; flex-shrink: 0; }" +
            ".rw-chat-stream { flex: 1; overflow-y: auto; padding: 16px 20px; display: flex; flex-direction: column; gap: 12px; background: #F8FAFC; }" +
            ".rw-chat-date-pill { align-self: center; background: #E2E8F0; color: #64748B; font-size: 10.5px; font-weight: 700; letter-spacing: 0.5px; padding: 3px 12px; border-radius: 9999px; margin-bottom: 4px; }" +
            ".rw-chat-bubble-row { display: flex; max-width: 75%; gap: 8px; }" +
            ".rw-chat-bubble-row.me { align-self: flex-end; flex-direction: column; align-items: flex-end; }" +
            ".rw-chat-bubble-row.other { align-self: flex-start; align-items: flex-end; }" +
            ".rw-chat-bubble-row.me .rw-chat-bubble { background: #001934; color: #FFFFFF; border-radius: 18px 18px 4px 18px; padding: 10px 14px; box-shadow: 0 2px 6px rgba(0, 25, 52, 0.12); }" +
            ".rw-chat-bubble-row.me .rw-chat-msg-txt { margin: 0; font-size: 13.5px; line-height: 1.45; color: #FFFFFF; word-break: break-word; }" +
            ".rw-chat-bubble-row.me .rw-chat-msg-time { font-size: 10px; color: #94A3B8; margin-top: 4px; display: block; text-align: right; font-weight: 600; }" +
            ".rw-chat-bubble-row.other .rw-chat-bubble { background: #FFFFFF; color: #001934; border-radius: 18px 18px 18px 4px; padding: 10px 14px; border: 1px solid #E2E8F0; box-shadow: 0 2px 6px rgba(0, 0, 0, 0.03); }" +
            ".rw-chat-bubble-row.other .rw-chat-msg-txt { margin: 0; font-size: 13.5px; line-height: 1.45; color: #0F172A; word-break: break-word; }" +
            ".rw-chat-bubble-row.other .rw-chat-msg-time { font-size: 10px; color: #94A3B8; margin-top: 4px; display: block; text-align: right; font-weight: 600; }" +
            ".rw-chat-input-footer { background: #FFFFFF; border-top: 1px solid #E2E8F0; padding: 12px 18px; display: flex; align-items: center; gap: 10px; min-height: 64px; z-index: 10; }" +
            ".rw-chat-input-field { flex: 1; }" +
            ".rw-chat-input-field::part(input-field) { background: #F8FAFC; border: 1px solid #E2E8F0; border-radius: 24px; padding: 2px 16px; font-size: 13.5px; }" +
            ".rw-chat-btn-send { width: 42px !important; height: 42px !important; min-width: 42px !important; border-radius: 50% !important; background: #001934 !important; color: #F5C45E !important; border: none !important; cursor: pointer; display: flex; align-items: center; justify-content: center; box-shadow: 0 2px 8px rgba(0, 25, 52, 0.25); transition: transform 0.15s ease; }" +
            "@media (max-width: 768px) {" +
            "  .rw-chat-page { padding: 0 !important; margin: 0 !important; max-width: 100% !important; width: 100% !important; height: calc(100vh - 64px - 60px) !important; background: #FFFFFF !important; }" +
            "  .rw-chat-wrapper { border-radius: 0 !important; border: none !important; box-shadow: none !important; height: 100% !important; width: 100% !important; max-width: 100% !important; }" +
            "  .rw-chat-wrapper.no-active-conv .rw-chat-left-sidebar { display: flex !important; width: 100% !important; height: 100% !important; border-right: none !important; }" +
            "  .rw-chat-wrapper.no-active-conv .rw-chat-right-area { display: none !important; }" +
            "  .rw-chat-wrapper.has-active-conv { position: fixed !important; top: 0 !important; left: 0 !important; right: 0 !important; bottom: 0 !important; z-index: 9999 !important; height: 100vh !important; }" +
            "  .rw-chat-wrapper.has-active-conv .rw-chat-left-sidebar { display: none !important; }" +
            "  .rw-chat-wrapper.has-active-conv .rw-chat-right-area { display: flex !important; width: 100% !important; height: 100% !important; }" +
            "  .rw-chat-mob-back-btn { display: inline-flex !important; }" +
            "  .rw-chat-bubble-row { max-width: 88% !important; }" +
            "  .rw-chat-sidebar-header { padding: 14px 16px 10px 16px !important; }" +
            "  .rw-chat-conv-item { padding: 12px 16px !important; }" +
            "  .rw-chat-header-bar { height: 56px !important; min-height: 56px !important; padding: 0 12px !important; }" +
            "  .rw-chat-input-footer { padding: 8px 12px !important; min-height: 56px !important; }" +
            "}"
        );
        getElement().appendChild(styleElem);

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
        renderSidebarWithFilter("");
    }

    private void renderSidebarWithFilter(String filterQuery) {
        leftSidebar.removeAll();

        Div sidebarHeader = new Div();
        sidebarHeader.addClassName("rw-chat-sidebar-header");

        Div topRow = new Div();
        topRow.addClassName("rw-chat-sidebar-top-row");

        H2 title = new H2("Pesan");
        title.addClassName("rw-chat-sidebar-title");
        topRow.add(title);

        Div searchWrap = new Div();
        searchWrap.addClassName("rw-chat-search-wrap");
        TextField searchInput = new TextField();
        searchInput.setPlaceholder("Cari percakapan atau nama...");
        searchInput.addClassName("rw-chat-search-input");
        searchInput.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchInput.setValueChangeMode(ValueChangeMode.EAGER);
        searchInput.setValue(filterQuery != null ? filterQuery : "");
        searchInput.addValueChangeListener(e -> renderSidebarWithFilter(e.getValue()));
        searchWrap.add(searchInput);

        sidebarHeader.add(topRow, searchWrap);

        Div listDiv = new Div();
        listDiv.addClassName("rw-chat-conv-list");

        if (currentUser == null) {
            leftSidebar.add(sidebarHeader, listDiv);
            return;
        }

        List<Conversation> conversations = chatService.getUserConversations(currentUser);

        String query = filterQuery != null ? filterQuery.toLowerCase().trim() : "";
        List<Conversation> filtered = conversations.stream()
            .filter(c -> {
                if (query.isEmpty()) return true;
                User other = (c.getBuyer() != null && c.getBuyer().getId().equals(currentUser.getId()))
                    ? c.getSeller() : c.getBuyer();
                String name = (other != null && other.getFullName() != null) ? other.getFullName().toLowerCase() : "";
                return name.contains(query);
            })
            .toList();

        if (filtered.isEmpty()) {
            Div emptyWrap = new Div();
            emptyWrap.getElement().getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("padding", "48px 16px")
                .set("color", "#94A3B8")
                .set("text-align", "center");

            Icon emptyIcon = VaadinIcon.CHAT.create();
            emptyIcon.setSize("36px");
            emptyIcon.getElement().getStyle().set("color", "#CBD5E1").set("margin-bottom", "10px");

            Paragraph empty = new Paragraph(query.isEmpty() ? "Belum ada percakapan." : "Tidak ada percakapan yang cocok.");
            empty.getElement().getStyle().set("font-size", "13.5px").set("font-weight", "600").set("margin", "0");
            emptyWrap.add(emptyIcon, empty);
            listDiv.add(emptyWrap);
        }

        for (Conversation conv : filtered) {
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

            Div itemTopRow = new Div();
            itemTopRow.addClassName("rw-chat-conv-top");

            Span nameSpan = new Span(otherName);
            nameSpan.addClassName("rw-chat-conv-name");

            String timeStr = conv.getLastMessageAt() != null
                ? conv.getLastMessageAt().format(TIME_FMT) : "";
            Span timeSpan = new Span(timeStr);
            timeSpan.addClassName("rw-chat-conv-time");
            itemTopRow.add(nameSpan, timeSpan);

            // Last message preview
            List<Message> msgs = chatService.getMessages(conv);
            String lastMsg = msgs.isEmpty() ? "Percakapan baru" : msgs.get(msgs.size() - 1).getBody();
            if (lastMsg != null && lastMsg.length() > 45) lastMsg = lastMsg.substring(0, 45) + "...";
            Paragraph lastMsgSpan = new Paragraph(lastMsg != null ? lastMsg : "");
            lastMsgSpan.addClassName("rw-chat-conv-lastmsg");
            metaDiv.add(itemTopRow, lastMsgSpan);

            item.add(avatarWrap, metaDiv);
            final Conversation convRef = conv;
            item.addClickListener(e -> {
                activeConversation = convRef;
                renderSidebarWithFilter(query);
                renderChatArea();
            });
            listDiv.add(item);
        }

        leftSidebar.add(sidebarHeader, listDiv);
    }

    // ==========================================
    // RIGHT PANEL: ACTIVE CHAT ROOM
    // ==========================================

    private void renderChatArea() {
        rightChatArea.removeAll();

        if (activeConversation != null) {
            wrapper.addClassName("has-active-conv");
            wrapper.removeClassName("no-active-conv");
        } else {
            wrapper.addClassName("no-active-conv");
            wrapper.removeClassName("has-active-conv");

            Div emptyArea = new Div();
            emptyArea.addClassName("rw-chat-empty-state");
            emptyArea.getElement().setProperty("innerHTML",
                "<div style='text-align:center;padding:80px 20px;'>" +
                "<svg width='64' height='64' viewBox='0 0 24 24' fill='none' stroke='#CBD5E1' stroke-width='1.5' style='margin-bottom:16px;'><path d='M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z'/></svg>" +
                "<h3 style='color:#001934;margin-bottom:8px;font-size:18px;font-weight:800;'>Pilih Percakapan</h3>" +
                "<p style='color:#64748B;font-size:14px;max-width:320px;margin:0 auto;'>Pilih percakapan dari daftar atau klik Chat Penjual di halaman produk.</p>" +
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

        Div headerLeft = new Div();
        headerLeft.addClassName("rw-chat-header-left");

        // Mobile back button to list
        Button btnBackToList = new Button(VaadinIcon.ARROW_LEFT.create(), e -> {
            activeConversation = null;
            renderSidebar();
            renderChatArea();
        });
        btnBackToList.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnBackToList.addClassName("rw-chat-mob-back-btn");

        // Header Avatar
        Div headerAvatarWrap = new Div();
        headerAvatarWrap.getElement().getStyle()
            .set("width", "36px")
            .set("height", "36px")
            .set("border-radius", "50%")
            .set("position", "relative")
            .set("flex-shrink", "0")
            .set("cursor", "pointer");
        String headerAvatarUrl = (other != null) ? other.getAvatarUrl() : null;
        if (headerAvatarUrl != null && !headerAvatarUrl.isBlank() && !headerAvatarUrl.contains("buku.jpeg")) {
            Image hav = new Image(headerAvatarUrl, otherName);
            hav.getElement().getStyle().set("width", "36px").set("height", "36px").set("border-radius", "50%").set("object-fit", "cover");
            headerAvatarWrap.add(hav);
        } else {
            Span hinit = new Span(getInitials(otherName));
            hinit.getElement().getStyle()
                .set("width", "36px").set("height", "36px").set("border-radius", "50%")
                .set("background", "#001934").set("color", "#F5C45E")
                .set("display", "flex").set("align-items", "center").set("justify-content", "center")
                .set("font-weight", "800").set("font-size", "12px");
            headerAvatarWrap.add(hinit);
        }
        if (other != null && other.getId() != null) {
            headerAvatarWrap.addClickListener(e -> UI.getCurrent().navigate("profile/" + other.getId()));
        }

        Div headerInfo = new Div();
        headerInfo.addClassName("rw-chat-header-info");

        H4 headerName = new H4(otherName);
        headerName.addClassName("rw-chat-header-name");
        headerName.getElement().getStyle().set("cursor", "pointer");
        if (other != null && other.getId() != null) {
            headerName.addClickListener(e -> UI.getCurrent().navigate("profile/" + other.getId()));
        }

        Span statusSpan = new Span();
        statusSpan.addClassName("rw-chat-header-status");
        statusSpan.getElement().setProperty("innerHTML", "<span>●</span> Online");
        headerInfo.add(headerName, statusSpan);

        headerLeft.add(btnBackToList, headerAvatarWrap, headerInfo);

        Div headerRightActions = new Div();
        headerRightActions.getElement().getStyle().set("display", "flex").set("align-items", "center").set("gap", "8px");

        Button btnReport = new Button(VaadinIcon.FLAG_O.create());
        btnReport.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnReport.getElement().getStyle()
            .set("color", "#64748B")
            .set("cursor", "pointer")
            .set("font-size", "16px")
            .set("padding", "6px 10px")
            .set("border-radius", "8px");
        btnReport.getElement().setAttribute("title", "Laporkan Pengguna");
        final User otherUserRef = other;
        final Conversation convRef = activeConversation;
        btnReport.addClickListener(e -> openReportUserDialog(otherUserRef, convRef));

        Button btnUserIcon = new Button(VaadinIcon.USER.create());
        btnUserIcon.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnUserIcon.getElement().getStyle()
            .set("background", "#001934").set("color", "#FFFFFF")
            .set("border-radius", "50%").set("width", "34px").set("height", "34px").set("min-width", "34px")
            .set("cursor", "pointer");
        btnUserIcon.getElement().setAttribute("title", "Lihat Profil");
        btnUserIcon.addClickListener(e -> {
            if (other != null && other.getId() != null) {
                UI.getCurrent().navigate("profile/" + other.getId());
            }
        });

        headerRightActions.add(btnReport, btnUserIcon);
        chatHeaderBar.add(headerLeft, headerRightActions);

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

            Button btnBuyNow = new Button("Lihat Barang");
            btnBuyNow.addClassName("rw-chat-btn-buy");
            Long prodId = product.getId();
            btnBuyNow.addClickListener(e -> UI.getCurrent().navigate("product?id=" + prodId));

            productBannerCard.add(pImg, pMeta, btnBuyNow);
        }

        // 3. Messages Stream
        Div messagesStream = new Div();
        messagesStream.addClassName("rw-chat-stream");

        Div datePill = new Div(new Span("HARI INI"));
        datePill.addClassName("rw-chat-date-pill");
        messagesStream.add(datePill);

        List<Message> messages = chatService.getMessages(activeConversation);
        for (Message msg : messages) {
            Div bubbleRow = new Div();
            bubbleRow.addClassName("rw-chat-bubble-row");
            boolean isMe = msg.getSender() != null && msg.getSender().getId().equals(currentUser.getId());
            bubbleRow.addClassName(isMe ? "me" : "other");

            if (!isMe) {
                Div avatarDiv = new Div();
                avatarDiv.addClassName("rw-chat-msg-avatar");
                String avatarUrl = msg.getSender() != null ? msg.getSender().getAvatarUrl() : null;
                if (avatarUrl != null && !avatarUrl.isBlank() && !avatarUrl.contains("buku.jpeg")) {
                    Image avImg = new Image(avatarUrl, "Avatar");
                    avImg.getElement().getStyle().set("width", "30px").set("height", "30px").set("border-radius", "50%").set("object-fit", "cover");
                    avatarDiv.add(avImg);
                } else {
                    Span init = new Span(getInitials(msg.getSender() != null ? msg.getSender().getFullName() : "U"));
                    init.getElement().getStyle()
                        .set("width", "30px").set("height", "30px").set("border-radius", "50%")
                        .set("background", "#001934").set("color", "#F5C45E")
                        .set("display", "flex").set("align-items", "center").set("justify-content", "center")
                        .set("font-size", "10.5px").set("font-weight", "800");
                    avatarDiv.add(init);
                }
                bubbleRow.add(avatarDiv);
            }

            Div bubbleCard = new Div();
            bubbleCard.addClassName("rw-chat-bubble");

            Paragraph txt = new Paragraph(msg.getBody());
            txt.addClassName("rw-chat-msg-txt");

            String timeFormatted = msg.getCreatedAt() != null ? msg.getCreatedAt().format(TIME_FMT) : "09:15";
            Span time = new Span(timeFormatted);
            time.addClassName("rw-chat-msg-time");

            bubbleCard.add(txt, time);
            bubbleRow.add(bubbleCard);
            messagesStream.add(bubbleRow);
        }

        // 4. Input Footer
        Div inputFooter = new Div();
        inputFooter.addClassName("rw-chat-input-footer");

        messageInput.setPlaceholder("Ketik pesan...");
        messageInput.addClassName("rw-chat-input-field");

        Button btnSend = new Button(VaadinIcon.PAPERPLANE.create());
        btnSend.addClassName("rw-chat-btn-send");
        btnSend.addClickListener(e -> sendMessage());

        messageInput.getElement().addEventListener("keydown", e -> sendMessage())
            .setFilter("event.key === 'Enter'");

        inputFooter.add(messageInput, btnSend);

        if (product != null) {
            rightChatArea.add(chatHeaderBar, productBannerCard, messagesStream, inputFooter);
        } else {
            rightChatArea.add(chatHeaderBar, messagesStream, inputFooter);
        }

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
        String clean = imagesJson.replace("[", "").replace("]", "").replace("\"", "").replace("'", "").replace("\\", "").trim();
        if (clean.isEmpty()) return "images/buku.jpeg";
        String first = clean.split(",")[0].trim();
        if (first.isEmpty()) return "images/buku.jpeg";
        return first.startsWith("/") ? first : "/" + first;
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
