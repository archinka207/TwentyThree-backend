package com.twentythree.messenger.dto.chat;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class JoinChatRequestDto {
    @NotNull(message = "Interest ID cannot be null")
    private Long interestId;
    // Можно добавить другие параметры, если логика поиска чата усложнится
}