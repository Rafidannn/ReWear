package com.example.application.repository.chat;

import com.example.application.model.chat.Conversation;
import com.example.application.model.product.Product;
import com.example.application.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("SELECT DISTINCT c FROM Conversation c " +
           "LEFT JOIN FETCH c.buyer " +
           "LEFT JOIN FETCH c.seller " +
           "LEFT JOIN FETCH c.product " +
           "WHERE c.buyer.id = :userId OR c.seller.id = :userId " +
           "ORDER BY c.lastMessageAt DESC")
    List<Conversation> findByUserIdWithDetails(@Param("userId") Long userId);

    @Query("SELECT c FROM Conversation c " +
           "LEFT JOIN FETCH c.buyer " +
           "LEFT JOIN FETCH c.seller " +
           "LEFT JOIN FETCH c.product " +
           "WHERE (c.buyer.id = :user1Id AND c.seller.id = :user2Id) OR " +
           "(c.buyer.id = :user2Id AND c.seller.id = :user1Id) " +
           "ORDER BY c.lastMessageAt DESC")
    List<Conversation> findBetweenUsersWithDetails(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);

    @Query("SELECT c FROM Conversation c " +
           "LEFT JOIN FETCH c.buyer " +
           "LEFT JOIN FETCH c.seller " +
           "LEFT JOIN FETCH c.product " +
           "WHERE ((c.buyer.id = :user1Id AND c.seller.id = :user2Id) OR " +
           "(c.buyer.id = :user2Id AND c.seller.id = :user1Id)) AND c.product.id = :productId")
    Optional<Conversation> findByUsersAndProductWithDetails(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id, @Param("productId") Long productId);

    @Query("SELECT c FROM Conversation c " +
           "LEFT JOIN FETCH c.buyer " +
           "LEFT JOIN FETCH c.seller " +
           "LEFT JOIN FETCH c.product " +
           "WHERE c.id = :id")
    Optional<Conversation> findByIdWithDetails(@Param("id") Long id);
}
