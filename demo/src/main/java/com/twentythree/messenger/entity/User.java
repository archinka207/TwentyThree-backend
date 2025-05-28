package com.twentythree.messenger.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(columnNames = "nickname")
})
@Data
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"interests", "createdChat", "participatingChat"}) // Avoid circular dependencies in hashCode/equals
@EntityListeners(AuditingEntityListener.class)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, unique = true)
    private String nickname;

    @NotBlank
    @Size(max = 255)
    @Column(name = "password_hash", nullable = false)
    private String password; // Store hashed password

    @DecimalMin(value = "1.0")
    @DecimalMax(value = "10.0")
    @Column(nullable = false, precision = 3, scale = 1, columnDefinition = "DECIMAL(3,1) DEFAULT 5.0")
    private BigDecimal reputation = BigDecimal.valueOf(5.0);

    @Column(name = "profile_picture_url", length = 255)
    private String profilePictureUrl;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Relationships
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_interests",
               joinColumns = @JoinColumn(name = "user_id"),
               inverseJoinColumns = @JoinColumn(name = "interest_id"))
    private Set<Interest> interests = new HashSet<>();

    @OneToOne(mappedBy = "creator", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    private Chat createdChat; // User can create only one chat

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    private ChatParticipant participatingChat; // User can participate in only one chat
}