package com.twentythree.messenger.controller;

import com.twentythree.messenger.dto.chat.ChatCreateRequest;
import com.twentythree.messenger.dto.chat.ChatDto;
import com.twentythree.messenger.entity.User; // Assuming you have a way to get current user
import com.twentythree.messenger.exception.ResourceNotFoundException;
import com.twentythree.messenger.repository.UserRepository;
import com.twentythree.messenger.security.CurrentUser; // Custom annotation to inject UserPrincipal
import com.twentythree.messenger.security.UserPrincipal; // Your UserDetails implementation
import com.twentythree.messenger.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chats")
@PreAuthorize("isAuthenticated()") // All methods here require authentication
public class ChatController {

    @Autowired
    private ChatService chatService;
    @Autowired
    private UserRepository userRepository; // To fetch full User entity

    // Helper to get User entity from UserPrincipal
    private User getCurrentUserEntity(UserPrincipal currentUserPrincipal) {
        return userRepository.findById(currentUserPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserPrincipal.getId()));
    }

    @PostMapping
    public ResponseEntity<ChatDto> createChat(@Valid @RequestBody ChatCreateRequest createRequest,
                                              @CurrentUser UserPrincipal currentUserPrincipal) {
        User user = getCurrentUserEntity(currentUserPrincipal);
        ChatDto chatDto = chatService.createChat(createRequest, user);
        return new ResponseEntity<>(chatDto, HttpStatus.CREATED);
    }

    // "Join" button logic - user provides an interest, backend finds or creates a chat.
    // For v1, let's assume user picks an interest and we try to find an existing chat for it.
    @PostMapping("/join/{interestId}")
    public ResponseEntity<ChatDto> joinChatByInterest(@PathVariable Long interestId,
                                                     @CurrentUser UserPrincipal currentUserPrincipal) {
        User user = getCurrentUserEntity(currentUserPrincipal);
        ChatDto chatDto = chatService.joinChatByInterest(interestId, user);
        return ResponseEntity.ok(chatDto);
    }
    
    @GetMapping("/current")
    public ResponseEntity<ChatDto> getCurrentActiveChat(@CurrentUser UserPrincipal currentUserPrincipal) {
        User user = getCurrentUserEntity(currentUserPrincipal);
        ChatDto chatDto = chatService.getCurrentChatForUser(user);
        if (chatDto == null) {
            return ResponseEntity.noContent().build(); // Or 404 if a chat is always expected
        }
        return ResponseEntity.ok(chatDto);
    }

    @GetMapping("/{chatId}")
    public ResponseEntity<ChatDto> getChatDetails(@PathVariable Long chatId,
                                                  @CurrentUser UserPrincipal currentUserPrincipal) {
        User user = getCurrentUserEntity(currentUserPrincipal);
        ChatDto chatDto = chatService.getChatDetails(chatId, user);
        return ResponseEntity.ok(chatDto);
    }

    @PostMapping("/{chatId}/leave")
    public ResponseEntity<?> leaveChat(@PathVariable Long chatId,
                                       @CurrentUser UserPrincipal currentUserPrincipal) {
        User user = getCurrentUserEntity(currentUserPrincipal);
        chatService.leaveChat(chatId, user);
        return ResponseEntity.ok("Successfully left the chat.");
    }
}
// You'll need the @CurrentUser annotation and UserPrincipal class:
// package com.twentythree.messenger.security;
// import org.springframework.security.core.annotation.AuthenticationPrincipal;
// import java.lang.annotation.*;
// @Target({ElementType.PARAMETER, ElementType.TYPE})
// @Retention(RetentionPolicy.RUNTIME)
// @Documented
// @AuthenticationPrincipal
// public @interface CurrentUser {}

// package com.twentythree.messenger.security;
// import com.fasterxml.jackson.annotation.JsonIgnore;
// import com.twentythree.messenger.entity.User;
// import org.springframework.security.core.GrantedAuthority;
// import org.springframework.security.core.authority.SimpleGrantedAuthority;
// import org.springframework.security.core.userdetails.UserDetails;
// import java.util.Collection;
// import java.util.List;
// import java.util.Objects;
// import java.util.stream.Collectors;

// public class UserPrincipal implements UserDetails {
//     private Long id;
//     private String nickname;
//     @JsonIgnore
//     private String password;
//     private Collection<? extends GrantedAuthority> authorities;

//     public UserPrincipal(Long id, String nickname, String password, Collection<? extends GrantedAuthority> authorities) {
//         this.id = id;
//         this.nickname = nickname;
//         this.password = password;
//         this.authorities = authorities;
//     }

//     public static UserPrincipal create(User user) {
//         // For now, no specific roles, just a general authenticated user
//         List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
//         return new UserPrincipal(
//                 user.getId(),
//                 user.getNickname(),
//                 user.getPassword(),
//                 authorities
//         );
//     }
//     public Long getId() { return id; }
//     @Override public String getUsername() { return nickname; } // Use nickname as username
//     @Override public String getPassword() { return password; }
//     @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
//     @Override public boolean isAccountNonExpired() { return true; }
//     @Override public boolean isAccountNonLocked() { return true; }
//     @Override public boolean isCredentialsNonExpired() { return true; }
//     @Override public boolean isEnabled() { return true; }
//     // equals and hashCode
// }