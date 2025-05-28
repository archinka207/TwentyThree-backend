package com.twentythree.messenger.dto.user;

import com.twentythree.messenger.dto.InterestDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
    private Long id;
    private String nickname;
    private BigDecimal reputation;
    private String profilePictureUrl;
    private LocalDateTime createdAt;
    private Set<InterestDto> interests; // Отображаем интересы пользователя
}