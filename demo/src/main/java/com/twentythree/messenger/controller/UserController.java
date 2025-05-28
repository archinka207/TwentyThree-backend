package com.twentythree.messenger.controller;

import com.twentythree.messenger.dto.user.UserProfileDto;
import com.twentythree.messenger.dto.user.UserUpdateDto;
import com.twentythree.messenger.entity.User;
import com.twentythree.messenger.exception.ResourceNotFoundException;
import com.twentythree.messenger.repository.UserRepository;
import com.twentythree.messenger.security.CurrentUser;
import com.twentythree.messenger.security.UserPrincipal;
import com.twentythree.messenger.service.FileStorageService;
import com.twentythree.messenger.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("isAuthenticated()")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository; // For direct fetch if needed by controller

    @Autowired
    private FileStorageService fileStorageService;

    // Helper to get User entity from UserPrincipal
    private User getCurrentUserEntity(UserPrincipal currentUserPrincipal) {
        return userRepository.findById(currentUserPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserPrincipal.getId()));
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getCurrentUserProfile(@CurrentUser UserPrincipal currentUserPrincipal) {
        User user = getCurrentUserEntity(currentUserPrincipal);
        UserProfileDto userProfileDto = userService.getUserProfile(user.getId());
        return ResponseEntity.ok(userProfileDto);
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileDto> updateUserProfile(@CurrentUser UserPrincipal currentUserPrincipal,
                                                            @Valid @RequestBody UserUpdateDto userUpdateDto) {
        User user = getCurrentUserEntity(currentUserPrincipal);
        UserProfileDto updatedProfile = userService.updateUserProfile(user.getId(), userUpdateDto);
        return ResponseEntity.ok(updatedProfile);
    }

    @PostMapping("/me/avatar")
    public ResponseEntity<UserProfileDto> updateUserAvatar(@CurrentUser UserPrincipal currentUserPrincipal,
                                                           @RequestParam("file") MultipartFile file) {
        User user = getCurrentUserEntity(currentUserPrincipal);
        UserProfileDto updatedProfile = userService.updateUserAvatar(user.getId(), file);
        return ResponseEntity.ok(updatedProfile);
    }

    // Endpoint for other users to view a (limited) profile if needed, e.g., by ID or nickname
    // This might not be needed if users are anonymous in chats.
    // @GetMapping("/{userId}")
    // public ResponseEntity<UserProfileDto> getUserProfileById(@PathVariable Long userId) {
    //     UserProfileDto userProfileDto = userService.getUserProfile(userId); // Service should handle privacy
    //     return ResponseEntity.ok(userProfileDto);
    // }
}