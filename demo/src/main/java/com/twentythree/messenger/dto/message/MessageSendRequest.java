package com.twentythree.messenger.dto.message;

import com.twentythree.messenger.entity.enums.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MessageSendRequest {
    @NotNull(message = "Message type cannot be null")
    private MessageType messageType = MessageType.TEXT; // Default to text

    @Size(max = 2000, message = "Text content cannot exceed 2000 characters")
    private String contentText; // Required if type is TEXT

    @Size(max = 255, message = "Image URL cannot exceed 255 characters")
    private String contentImageUrl; // Required if type is IMAGE
}