package com.twentythree.messenger.repository;

import com.twentythree.messenger.entity.Chat;
import com.twentythree.messenger.entity.ChatParticipant;
import com.twentythree.messenger.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {
    Optional<ChatParticipant> findByUser(User user);
    boolean existsByUser(User user);
    Optional<ChatParticipant> findByUserAndChat(User user, Chat chat);
    void deleteByUserAndChat(User user, Chat chat);
}