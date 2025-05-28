package com.twentythree.messenger.repository;

import com.twentythree.messenger.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByNickname(String nickname);
    Boolean existsByNickname(String nickname);
    // You might need methods to find users by participation in a chat
    // or if they have created a chat.
    Optional<User> findByCreatedChatId(Long chatId);
    Optional<User> findByParticipatingChatChatId(Long chatId);
}