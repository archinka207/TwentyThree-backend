package com.twentythree.messenger.dto.chat;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChatCreateRequest {
    @Size(max = 100, message = "Chat name cannot exceed 100 characters")
    private String chatName; // Optional, can be auto-generated

    @NotNull(message = "Primary interest ID cannot be null")
    private Long primaryInterestId;
}