package com.twentythree.messenger.repository;
import com.twentythree.messenger.entity.Chat;
import com.twentythree.messenger.entity.Interest;
import com.twentythree.messenger.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    Optional<Chat> findByCreator(User creator);
    boolean existsByCreator(User creator);

    @Query("SELECT c FROM Chat c WHERE c.active = true AND c.primaryInterest = :interest AND c.id NOT IN (SELECT cp.chat.id FROM ChatParticipant cp WHERE cp.user = :user)")
    List<Chat> findActiveChatsByInterestNotParticipatedByUser(@Param("interest") Interest interest, @Param("user") User user);
    
    @Query("SELECT c FROM Chat c JOIN c.participants p WHERE p.user = :user AND c.active = true")
    Optional<Chat> findActiveChatByParticipant(@Param("user") User user);

    Optional<Chat> findByCreatorAndActiveTrue(User creator);

    List<Chat> findByActiveTrueAndPrimaryInterestIn(Set<Interest> interests);

    List<Chat> findAllByExpiresAtBeforeAndActiveTrue(LocalDateTime now);
}