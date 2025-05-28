package com.twentythree.messenger.dto.message;

import com.twentythree.messenger.entity.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto {
    private Long id;
    private Long chatId;
    private Long senderId;
    private String senderNickname;
    private String senderProfilePictureUrl;
    private MessageType messageType;
    private String contentText;
    private String contentImageUrl;
    private LocalDateTime sentAt;
}