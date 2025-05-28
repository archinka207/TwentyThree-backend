package com.twentythree.messenger.controller;

import com.twentythree.messenger.dto.message.MessageDto;
import com.twentythree.messenger.dto.message.MessageSendRequest;
import com.twentythree.messenger.entity.User;
import com.twentythree.messenger.exception.ResourceNotFoundException;
import com.twentythree.messenger.repository.UserRepository;
import com.twentythree.messenger.security.CurrentUser; // Используется для HTTP эндпоинта
import com.twentythree.messenger.security.UserPrincipal;
import com.twentythree.messenger.service.MessageService;

import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler; // Для обработки исключений в WebSocket
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
// import org.springframework.messaging.handler.annotation.SendTo; // SendTo используется, если нет SimpMessagingTemplate
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate; // Для отправки сообщений конкретным пользователям или темам
import org.springframework.messaging.simp.annotation.SendToUser; // Если нужно отправить ответ только текущему пользователю
import org.springframework.security.access.AccessDeniedException; // Для явной проверки
import org.springframework.security.access.prepost.PreAuthorize; // Для HTTP эндпоинтов
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller; // Для WebSocket
import org.springframework.web.bind.annotation.PathVariable; // Используйте это для HTTP
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController; // Для HTTP
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Controller // Основная аннотация для WebSocket-хендлеров
@RestController // Добавляем для HTTP эндпоинтов в этом же классе
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate; // Для гибкой отправки сообщений

    // Helper для получения User из UserPrincipal (для HTTP эндпоинтов)
    private User getUserFromPrincipal(UserPrincipal principal) {
        if (principal == null) {
            throw new AccessDeniedException("User principal is null. Authentication required.");
        }
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));
    }


    // WebSocket endpoint for sending messages
    // Клиент отправляет на /app/chat/{chatId}/send
    // Сообщения будут разосланы сервисом через SimpMessagingTemplate на /topic/chat/{chatId}
    @MessageMapping("/chat/{chatId}/send")
    public void sendMessage(@DestinationVariable Long chatId, // @DestinationVariable для частей пути в @MessageMapping
                            @Payload MessageSendRequest messageRequest,
                            SimpMessageHeaderAccessor headerAccessor) { // SimpMessageHeaderAccessor для доступа к Principal

        Authentication authentication = (Authentication) headerAccessor.getUser();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            log.warn("Attempt to send message from unauthenticated WebSocket session to chat {}", chatId);
            // Можно отправить ошибку обратно пользователю, если это настроено
            // messagingTemplate.convertAndSendToUser(headerAccessor.getSessionId(), "/queue/errors", "Authentication required", headerAccessor.getMessageHeaders());
            throw new AccessDeniedException("User not authenticated for WebSocket action."); // Это будет обработано @MessageExceptionHandler
        }

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User sender = userRepository.findById(principal.getId())
                .orElseThrow(() -> {
                    log.error("User with ID {} not found from principal in WebSocket context.", principal.getId());
                    return new ResourceNotFoundException("User", "id", principal.getId());
                });

        log.info("User {} sending message to chat {}", sender.getNickname(), chatId);
        // Сервис сохранит сообщение и сам вызовет messagingTemplate.convertAndSend("/topic/chat/" + chatId, messageDto);
        messageService.saveAndBroadcastMessage(chatId, sender, messageRequest);
    }

    // HTTP endpoint for uploading images for chat messages
    // Клиент сначала загружает изображение сюда, получает URL,
    // затем отправляет WebSocket сообщение типа IMAGE с этим URL.
    @PostMapping("/api/chats/{chatId}/messages/image")
    @PreAuthorize("isAuthenticated()") // Эта аннотация должна работать для HTTP эндпоинтов
    public ResponseEntity<MessageDto> uploadChatMessageImage(@PathVariable Long chatId, // @PathVariable для HTTP
                                                             @RequestParam("file") MultipartFile file,
                                                             @CurrentUser UserPrincipal currentUserPrincipal) { // @CurrentUser для HTTP
        User sender = getUserFromPrincipal(currentUserPrincipal);
        log.info("User {} uploading image to chat {}", sender.getNickname(), chatId);

        // Сервис сохранит файл, создаст сообщение в БД и вернет DTO с URL.
        // Он также может сразу разослать это сообщение через WebSocket.
        MessageDto imageMessageDto = messageService.storeAndCreateImageMessage(chatId, sender, file);

        return ResponseEntity.ok(imageMessageDto);
    }

    // Обработчик исключений для WebSocket сообщений в этом контроллере
    @MessageExceptionHandler
    @SendToUser("/queue/errors") // Отправляет сообщение об ошибке обратно пользователю, который ее вызвал
    public String handleWebSocketException(Throwable exception) {
        log.error("Error handling WebSocket message: {}", exception.getMessage());
        if (exception instanceof AccessDeniedException) {
            return "Access Denied: " + exception.getMessage();
        }
        // Можно добавить обработку других специфичных исключений
        return "An error occurred: " + exception.getMessage();
    }
}