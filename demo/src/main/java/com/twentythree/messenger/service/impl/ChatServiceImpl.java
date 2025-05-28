package com.twentythree.messenger.service.impl;
// ... imports ...
import com.twentythree.messenger.dto.chat.ChatCreateRequest;
import com.twentythree.messenger.dto.chat.ChatDto;
import com.twentythree.messenger.dto.chat.ChatParticipantDto;
import com.twentythree.messenger.entity.*;
import com.twentythree.messenger.exception.BadRequestException;
import com.twentythree.messenger.exception.ResourceNotFoundException;
import com.twentythree.messenger.exception.UserAlreadyInChatException;
import com.twentythree.messenger.repository.*;
import com.twentythree.messenger.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService {
    private static final Logger logger = LoggerFactory.getLogger(ChatServiceImpl.class);

    @Autowired private ChatRepository chatRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private InterestRepository interestRepository;
    @Autowired private ChatParticipantRepository chatParticipantRepository;
    // @Autowired private MessageRepository messageRepository; // If needed directly

    private static final long CHAT_DURATION_MINUTES = 60; // Example: 1 hour

    @Override
    @Transactional
    public ChatDto createChat(ChatCreateRequest createRequest, User currentUser) {
        // 1. Check if user already created a chat
        if (chatRepository.existsByCreator(currentUser)) {
            throw new UserAlreadyInChatException("User has already created a chat.");
        }
        // 2. Check if user is already participating in another chat
        if (chatParticipantRepository.existsByUser(currentUser)) {
            throw new UserAlreadyInChatException("User is already participating in another chat.");
        }

        Interest primaryInterest = interestRepository.findById(createRequest.getPrimaryInterestId())
                .orElseThrow(() -> new ResourceNotFoundException("Interest", "id", createRequest.getPrimaryInterestId()));

        Chat chat = new Chat();
        chat.setCreator(currentUser);
        chat.setChatName(createRequest.getChatName() != null ? createRequest.getChatName() : "Chat about " + primaryInterest.getName());
        chat.setPrimaryInterest(primaryInterest);
        chat.setActive(true);
        chat.setExpiresAt(LocalDateTime.now().plusMinutes(CHAT_DURATION_MINUTES)); // Set expiration

        Chat savedChat = chatRepository.save(chat);

        // Creator automatically joins the chat they created
        ChatParticipant participant = new ChatParticipant();
        participant.setUser(currentUser);
        participant.setChat(savedChat);
        chatParticipantRepository.save(participant);
        
        savedChat.getParticipants().add(participant); // Add to in-memory set for DTO mapping

        return mapChatToDto(savedChat);
    }

    @Override
    @Transactional
    public ChatDto joinChatByInterest(Long interestId, User currentUser) {
        // 1. Check if user already created a chat
        if (chatRepository.existsByCreator(currentUser)) {
            throw new UserAlreadyInChatException("Cannot join a chat, user has already created one.");
        }
        // 2. Check if user is already participating in another chat
        if (chatParticipantRepository.existsByUser(currentUser)) {
            throw new UserAlreadyInChatException("User is already participating in another chat.");
        }

        Interest targetInterest = interestRepository.findById(interestId)
                .orElseThrow(() -> new ResourceNotFoundException("Interest", "id", interestId));
        
        // Find an active chat for this interest that the user is not already in (implicitly handled by above checks)
        // and that is not full (add capacity logic if needed).
        // For v1, just find any active chat with this interest.
        List<Chat> availableChats = chatRepository.findActiveChatsByInterestNotParticipatedByUser(targetInterest, currentUser);

        if (availableChats.isEmpty()) {
            throw new ResourceNotFoundException("Chat", "interest", targetInterest.getName() + " (no suitable active chat found)");
        }

        Chat chatToJoin = availableChats.get(0); // Simplistic: join the first one found. Add more logic if needed.

        ChatParticipant participant = new ChatParticipant();
        participant.setUser(currentUser);
        participant.setChat(chatToJoin);
        chatParticipantRepository.save(participant);

        return mapChatToDto(chatToJoin); // Return DTO of the joined chat
    }
    
    @Override
    @Transactional(readOnly = true)
    public ChatDto getCurrentChatForUser(User currentUser) {
        // Check if user is a creator of an active chat
        Optional<Chat> createdChatOpt = chatRepository.findByCreatorAndActiveTrue(currentUser);
        if (createdChatOpt.isPresent()) {
            return mapChatToDto(createdChatOpt.get());
        }

        // Check if user is a participant in an active chat
        Optional<ChatParticipant> participantOpt = chatParticipantRepository.findByUser(currentUser);
        if (participantOpt.isPresent() && participantOpt.get().getChat().isActive()) {
            return mapChatToDto(participantOpt.get().getChat());
        }
        
        return null; // Or throw ResourceNotFoundException if a chat is expected
    }


    @Override
    @Transactional(readOnly = true)
    public ChatDto getChatDetails(Long chatId, User currentUser) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat", "id", chatId));

        // Basic authorization: check if user is participant or creator
        boolean isParticipant = chat.getParticipants().stream().anyMatch(p -> p.getUser().getId().equals(currentUser.getId()));
        boolean isCreator = chat.getCreator().getId().equals(currentUser.getId());

        if (!isParticipant && !isCreator) {
             throw new BadRequestException("User is not authorized to view this chat."); // Or AccessDeniedException
        }
        return mapChatToDto(chat);
    }

    @Override
    @Transactional
    public void leaveChat(Long chatId, User currentUser) {
        Chat chat = chatRepository.findById(chatId)
            .orElseThrow(() -> new ResourceNotFoundException("Chat", "id", chatId));

        ChatParticipant participant = chatParticipantRepository.findByUserAndChat(currentUser, chat)
            .orElseThrow(() -> new BadRequestException("User is not a participant of this chat."));
        
        chatParticipantRepository.delete(participant);

        // Optional: If chat becomes empty (except creator leaving their own chat), deactivate it?
        // Or handle this via scheduled task.
        // If creator leaves their own chat, the chat should probably be deactivated/deleted.
        if (chat.getCreator().getId().equals(currentUser.getId())) {
            chat.setActive(false);
            // Potentially remove all other participants
            // chat.getParticipants().clear(); // This would trigger orphanRemoval if set on ChatParticipant
            chatRepository.save(chat);
        }
    }

    @Scheduled(fixedRate = 60000) // Run every minute
    @Transactional
    public void processExpiredChats() {
        logger.info("Running scheduled task to process expired chats at {}", LocalDateTime.now());
        List<Chat> expiredChats = chatRepository.findAllByExpiresAtBeforeAndActiveTrue(LocalDateTime.now());
        for (Chat chat : expiredChats) {
            logger.info("Deactivating chat ID: {}", chat.getId());
            chat.setActive(false);
            // Optionally: Notify participants via WebSocket that chat has ended
            // For simplicity, just deactivating. Frontend would then not show it / show as ended.
        }
        if (!expiredChats.isEmpty()) {
            chatRepository.saveAll(expiredChats);
        }
    }

    // Helper to map Entity to DTO
    private ChatDto mapChatToDto(Chat chat) {
        ChatDto dto = new ChatDto();
        dto.setId(chat.getId());
        dto.setChatName(chat.getChatName());
        dto.setPrimaryInterestId(chat.getPrimaryInterest().getId());
        dto.setPrimaryInterestName(chat.getPrimaryInterest().getName());
        dto.setCreatorNickname(chat.getCreator().getNickname());
        dto.setActive(chat.isActive());
        dto.setCreatedAt(chat.getCreatedAt());
        dto.setExpiresAt(chat.getExpiresAt());
        
        dto.setParticipants(chat.getParticipants().stream()
                                .map(p -> new ChatParticipantDto(p.getUser().getId(), p.getUser().getNickname(), p.getUser().getProfilePictureUrl()))
                                .collect(Collectors.toList()));
        // Load messages if needed for this DTO, or have a separate endpoint/DTO for messages
        // For now, ChatDto can include recent messages or this can be fetched separately.
        return dto;
    }
}