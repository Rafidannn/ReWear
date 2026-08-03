package com.example.application.repository.chat;

import com.example.application.model.chat.Conversation;
import com.example.application.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    List<Conversation> findByBuyerOrSellerOrderByLastMessageAtDesc(User buyer, User seller);
}
