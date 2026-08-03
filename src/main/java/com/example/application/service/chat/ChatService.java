package com.example.application.service.chat;

import com.example.application.model.chat.Conversation;
import com.example.application.model.chat.Message;
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

    public List<Conversation> getUserConversations(User user) {
        return conversationRepository.findByBuyerOrSellerOrderByLastMessageAtDesc(user, user);
    }

    public Optional<Conversation> findById(Long id) {
        return conversationRepository.findById(id);
    }

    public Conversation startConversation(Conversation conversation) {
        return conversationRepository.save(conversation);
    }

    public List<Message> getMessages(Conversation conversation) {
        return messageRepository.findByConversationOrderByCreatedAtAsc(conversation);
    }

    public Message sendMessage(Message message) {
        Message saved = messageRepository.save(message);
        Conversation conversation = message.getConversation();
        conversation.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conversation);
        return saved;
    }
}
