package com.example.application.service.chat;

import com.example.application.model.chat.Conversation;
import com.example.application.model.chat.Message;
import com.example.application.model.product.Product;
import com.example.application.model.user.User;
import com.example.application.repository.chat.ConversationRepository;
import com.example.application.repository.chat.MessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    public ChatService(ConversationRepository conversationRepository, MessageRepository messageRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    /** Get or create conversation between two users (optionally scoped to a product) */
    public Conversation getOrCreateConversation(User initiator, User seller, Product product) {
        Optional<Conversation> existing;
        if (product != null && product.getId() != null) {
            existing = conversationRepository.findByUsersAndProductWithDetails(
                initiator.getId(), seller.getId(), product.getId());
            if (existing.isEmpty()) {
                existing = conversationRepository.findBetweenUsersWithDetails(
                    initiator.getId(), seller.getId()).stream().findFirst();
            }
        } else {
            existing = conversationRepository.findBetweenUsersWithDetails(
                initiator.getId(), seller.getId()).stream().findFirst();
        }

        if (existing.isPresent()) {
            Conversation conv = existing.get();
            if (product != null && (conv.getProduct() == null || !conv.getProduct().getId().equals(product.getId()))) {
                conv.setProduct(product);
                conversationRepository.save(conv);
            }
            touchConversation(conv);
            return conv;
        }

        Conversation conv = new Conversation();
        conv.setBuyer(initiator);
        conv.setSeller(seller);
        conv.setProduct(product);
        conv = conversationRepository.save(conv);

        Conversation result = conversationRepository.findByIdWithDetails(conv.getId()).orElse(conv);
        touchConversation(result);
        return result;
    }

    /** All conversations for a user (as buyer or seller), ordered by latest message */
    public List<Conversation> getUserConversations(User user) {
        if (user == null || user.getId() == null) return List.of();
        List<Conversation> list = conversationRepository.findByUserIdWithDetails(user.getId());
        list.forEach(this::touchConversation);
        return list;
    }

    /** All messages in a conversation, oldest first */
    public List<Message> getMessages(Conversation conversation) {
        if (conversation == null || conversation.getId() == null) return List.of();
        List<Message> list = messageRepository.findByConversationIdWithSender(conversation.getId());
        list.forEach(m -> {
            if (m.getSender() != null) {
                m.getSender().getFullName();
                m.getSender().getAvatarUrl();
            }
        });
        return list;
    }

    /** Save a new message and bump lastMessageAt on the conversation */
    public Message sendMessage(Conversation conversation, User sender, String body) {
        Message msg = new Message();
        msg.setConversation(conversation);
        msg.setSender(sender);
        msg.setBody(body.trim());
        messageRepository.save(msg);

        conversation.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conversation);
        return msg;
    }

    public Optional<Conversation> findConversationById(Long id) {
        Optional<Conversation> convOpt = conversationRepository.findByIdWithDetails(id);
        convOpt.ifPresent(this::touchConversation);
        return convOpt;
    }

    private void touchConversation(Conversation conv) {
        if (conv == null) return;
        if (conv.getBuyer() != null) {
            conv.getBuyer().getFullName();
            conv.getBuyer().getAvatarUrl();
        }
        if (conv.getSeller() != null) {
            conv.getSeller().getFullName();
            conv.getSeller().getAvatarUrl();
        }
        if (conv.getProduct() != null) {
            conv.getProduct().getName();
            conv.getProduct().getPrice();
            conv.getProduct().getImages();
        }
    }
}
