package com.twentythree.messenger.entity;

import com.twentythree.messenger.entity.enums.MessageType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "chats")
@Data
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"creator", "primaryInterest", "participants", "messages"})
@EntityListeners(AuditingEntityListener.class)
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_id")
    private Long id;

    // Unique constraint in DB schema for creator_user_id ensures one chat per creator
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_user_id", referencedColumnName = "user_id", nullable = false, unique = true)
    private User creator;

    @Column(name = "chat_name", length = 100)
    private String chatName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_interest_id", nullable = false)
    private Interest primaryInterest;

    @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean active = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt; // For temporary chats

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<ChatParticipant> participants = new HashSet<>();

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("sentAt ASC")
    private List<Message> messages = new ArrayList<>();
}