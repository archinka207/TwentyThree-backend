package com.twentythree.messenger.dto.user;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set; // Set of interest IDs

@Data
public class UserUpdateDto {
    @Size(min = 3, max = 50, message = "Nickname must be between 3 and 50 characters if provided")
    private String nickname; // Optional: allow changing nickname

    // private String currentPassword; // If password change is allowed
    // private String newPassword;

    private Set<Long> interestIds; // Пользователь выбирает ID интересов
}