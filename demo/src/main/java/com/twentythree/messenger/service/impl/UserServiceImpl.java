package com.twentythree.messenger.service.impl;

import com.twentythree.messenger.dto.InterestDto;
import com.twentythree.messenger.dto.user.UserProfileDto;
import com.twentythree.messenger.dto.user.UserUpdateDto;
import com.twentythree.messenger.entity.Interest;
import com.twentythree.messenger.entity.User;
import com.twentythree.messenger.exception.BadRequestException;
import com.twentythree.messenger.exception.ResourceNotFoundException;
import com.twentythree.messenger.repository.InterestRepository;
import com.twentythree.messenger.repository.UserRepository;
import com.twentythree.messenger.service.FileStorageService;
import com.twentythree.messenger.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder; // If password change is added
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InterestRepository interestRepository;

    @Autowired
    private FileStorageService fileStorageService;

    // @Autowired
    // private PasswordEncoder passwordEncoder; // If password change is implemented

    @Override
    @Transactional(readOnly = true)
    public UserProfileDto getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return mapUserToProfileDto(user);
    }

    @Override
    @Transactional
    public UserProfileDto updateUserProfile(Long userId, UserUpdateDto userUpdateDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Update nickname if provided and different
        if (userUpdateDto.getNickname() != null && !userUpdateDto.getNickname().isBlank() && !user.getNickname().equals(userUpdateDto.getNickname())) {
            if (userRepository.existsByNickname(userUpdateDto.getNickname())) {
                throw new BadRequestException("Nickname '" + userUpdateDto.getNickname() + "' is already taken.");
            }
            user.setNickname(userUpdateDto.getNickname());
        }

        // Update interests if provided
        if (userUpdateDto.getInterestIds() != null) {
            Set<Interest> newInterests = new HashSet<>();
            if (!userUpdateDto.getInterestIds().isEmpty()) {
                newInterests.addAll(interestRepository.findByIdIn(userUpdateDto.getInterestIds()));
                if (newInterests.size() != userUpdateDto.getInterestIds().size()) {
                    throw new BadRequestException("One or more provided interest IDs are invalid.");
                }
            }
            user.setInterests(newInterests);
        }

        // Password update logic would go here if implemented

        User updatedUser = userRepository.save(user);
        return mapUserToProfileDto(updatedUser);
    }

    @Override
    @Transactional
    public UserProfileDto updateUserAvatar(Long userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (file.isEmpty()) {
            throw new BadRequestException("Cannot update avatar with an empty file.");
        }

        // Delete old avatar if exists and if storage service supports it
        if (user.getProfilePictureUrl() != null) {
            try {
                // Assuming fileStorageService.storeFile returns a relative path like "user_avatars/filename.jpg"
                // And the actual URL is constructed by prefixing it (e.g., /static/images/)
                // For deletion, we might need just the filename or the full path depending on implementation.
                // This is a simplified deletion attempt.
                String oldFileName = user.getProfilePictureUrl().substring(user.getProfilePictureUrl().lastIndexOf("/") + 1);
                fileStorageService.deleteFile(oldFileName, "user_avatars"); // Pass a subfolder if used
            } catch (Exception e) {
                // Log error but continue, as updating avatar is more important than deleting old one in case of error
                System.err.println("Could not delete old avatar: " + e.getMessage());
            }
        }

        String fileName = fileStorageService.storeFile(file, "user_avatars"); // Store in a subfolder
        // The fileStorageService should return a path or URL that can be used to access the file.
        // e.g., "user_avatars/timestamp_originalfilename.ext"
        // Construct the full URL for accessing it (e.g., /static/images/user_avatars/filename.ext)
        String fileAccessUrl = "/static/images/" + fileName; // Adjust based on your static resource handling

        user.setProfilePictureUrl(fileAccessUrl);
        User updatedUser = userRepository.save(user);
        return mapUserToProfileDto(updatedUser);
    }

    private UserProfileDto mapUserToProfileDto(User user) {
        Set<InterestDto> interestDtos = user.getInterests().stream()
                .map(interest -> new InterestDto(interest.getId(), interest.getName(), interest.getDescription()))
                .collect(Collectors.toSet());

        return new UserProfileDto(
                user.getId(),
                user.getNickname(),
                user.getReputation(),
                user.getProfilePictureUrl(),
                user.getCreatedAt(),
                interestDtos
        );
    }
}