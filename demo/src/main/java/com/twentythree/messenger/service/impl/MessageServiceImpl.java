package com.twentythree.messenger.service.impl;

import com.twentythree.messenger.dto.message.MessageDto;
import com.twentythree.messenger.dto.message.MessageSendRequest;
import com.twentythree.messenger.entity.Chat;
import com.twentythree.messenger.entity.Message;
import com.twentythree.messenger.entity.User;
import com.twentythree.messenger.entity.enums.MessageType;
import com.twentythree.messenger.exception.BadRequestException;
import com.twentythree.messenger.exception.ResourceNotFoundException;
import com.twentythree.messenger.repository.ChatRepository;
import com.twentythree.messenger.repository.MessageRepository;
import com.twentythree.messenger.repository.UserRepository;
import com.twentythree.messenger.service.FileStorageService;
import com.twentythree.messenger.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate; // For broadcasting
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private UserRepository userRepository; // Not strictly needed if User object is passed

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate; // For broadcasting WebSocket messages

    @Override
    @Transactional
    public MessageDto saveAndBroadcastMessage(Long chatId, User sender, MessageSendRequest messageRequest) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat", "id", chatId));

        if (!chat.isActive()) {
            throw new BadRequestException("Cannot send message to an inactive chat.");
        }

        // Authorization: Check if sender is a participant (implicitly handled if they can connect to WebSocket path)
        // For extra safety:
        // boolean isParticipant = chat.getParticipants().stream().anyMatch(p -> p.getUser().getId().equals(sender.getId()));
        // if (!isParticipant) {
        //     throw new AccessDeniedException("Sender is not a participant of this chat.");
        // }


        Message message = new Message();
        message.setChat(chat);
        message.setSender(sender);
        message.setMessageType(messageRequest.getMessageType());

        if (messageRequest.getMessageType() == MessageType.TEXT) {
            if (messageRequest.getContentText() == null || messageRequest.getContentText().isBlank()) {
                throw new BadRequestException("Text content cannot be empty for a text message.");
            }
            message.setContentText(messageRequest.getContentText());
        } else if (messageRequest.getMessageType() == MessageType.IMAGE) {
            if (messageRequest.getContentImageUrl() == null || messageRequest.getContentImageUrl().isBlank()) {
                throw new BadRequestException("Image URL cannot be empty for an image message.");
            }
            message.setContentImageUrl(messageRequest.getContentImageUrl());
        } else {
            throw new BadRequestException("Unsupported message type.");
        }

        Message savedMessage = messageRepository.save(message);
        MessageDto messageDto = mapMessageToDto(savedMessage);

        // Broadcast the message to all subscribers of this chat's topic
        messagingTemplate.convertAndSend("/topic/chat/" + chatId, messageDto);

        return messageDto;
    }

    @Override
    @Transactional
    public MessageDto storeAndCreateImageMessage(Long chatId, User sender, MultipartFile file) {
        Chat chat = chatRepository.findById(chatId)
            .orElseThrow(() -> new ResourceNotFoundException("Chat", "id", chatId));
        if (!chat.isActive()) {
            throw new BadRequestException("Cannot send message to an inactive chat.");
        }
        // Authorization check similar to above

        if (file.isEmpty()) {
            throw new BadRequestException("Cannot send an empty image file.");
        }

        String fileName = fileStorageService.storeFile(file, "chat_images/" + chatId); // Store in a subfolder per chat
        String fileAccessUrl = "/static/images/" + fileName; // Adjust path

        Message message = new Message();
        message.setChat(chat);
        message.setSender(sender);
        message.setMessageType(MessageType.IMAGE);
        message.setContentImageUrl(fileAccessUrl);

        Message savedMessage = messageRepository.save(message);
        MessageDto messageDto = mapMessageToDto(savedMessage);

        // Broadcast after successful save and URL generation
        messagingTemplate.convertAndSend("/topic/chat/" + chatId, messageDto);

        return messageDto; // Return DTO so client knows the URL and message ID
    }


    @Override
    @Transactional(readOnly = true)
    public List<MessageDto> getMessagesForChat(Long chatId, User currentUser) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat", "id", chatId));

        // Authorization: Ensure currentUser is part of this chat
        boolean isParticipant = chat.getParticipants().stream().anyMatch(p -> p.getUser().getId().equals(currentUser.getId()));
        if (!isParticipant && !chat.getCreator().getId().equals(currentUser.getId())) {
             throw new BadRequestException("User is not authorized to view messages for this chat.");
        }

        List<Message> messages = messageRepository.findByChatOrderBySentAtAsc(chat);
        return messages.stream().map(this::mapMessageToDto).collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<MessageDto> getMessagesForChatPaginated(Long chatId, User currentUser, Pageable pageable) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat", "id", chatId));

        // Authorization (similar to above)
        boolean isParticipant = chat.getParticipants().stream().anyMatch(p -> p.getUser().getId().equals(currentUser.getId()));
         if (!isParticipant && !chat.getCreator().getId().equals(currentUser.getId())) {
             throw new BadRequestException("User is not authorized to view messages for this chat.");
        }

        Page<Message> messagesPage = messageRepository.findByChatOrderBySentAtDesc(chat, pageable); // Desc for recent
        return messagesPage.map(this::mapMessageToDto);
    }


    private MessageDto mapMessageToDto(Message message) {
        return new MessageDto(
                message.getId(),
                message.getChat().getId(),
                message.getSender().getId(),
                message.getSender().getNickname(),
                message.getSender().getProfilePictureUrl(),
                message.getMessageType(),
                message.getContentText(),
                message.getContentImageUrl(),
                message.getSentAt()
        );
    }
}