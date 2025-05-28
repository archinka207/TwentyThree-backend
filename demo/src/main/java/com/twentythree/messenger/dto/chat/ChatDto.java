package com.twentythree.messenger.dto.chat;

import com.twentythree.messenger.dto.message.MessageDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatDto {
    private Long id;
    private String chatName;
    private Long primaryInterestId;
    private String primaryInterestName;
    private String creatorNickname;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private List<ChatParticipantDto> participants;
    private List<MessageDto> recentMessages; // Можно добавить для быстрой загрузки
}