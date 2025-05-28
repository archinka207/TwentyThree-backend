package com.twentythree.messenger.service;

import com.twentythree.messenger.dto.user.UserProfileDto;
import com.twentythree.messenger.dto.user.UserUpdateDto;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    UserProfileDto getUserProfile(Long userId);
    UserProfileDto updateUserProfile(Long userId, UserUpdateDto userUpdateDto);
    UserProfileDto updateUserAvatar(Long userId, MultipartFile file);
}