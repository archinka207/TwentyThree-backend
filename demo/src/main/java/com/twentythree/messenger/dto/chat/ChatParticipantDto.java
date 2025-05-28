package com.twentythree.messenger.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatParticipantDto {
    private Long userId;
    private String nickname;
    private String profilePictureUrl;
}