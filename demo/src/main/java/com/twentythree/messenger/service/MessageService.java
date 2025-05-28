package com.twentythree.messenger.service;

import com.twentythree.messenger.dto.message.MessageDto;
import com.twentythree.messenger.dto.message.MessageSendRequest;
import com.twentythree.messenger.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MessageService {
    MessageDto saveAndBroadcastMessage(Long chatId, User sender, MessageSendRequest messageRequest);
    MessageDto storeAndCreateImageMessage(Long chatId, User sender, MultipartFile file); // For HTTP upload
    List<MessageDto> getMessagesForChat(Long chatId, User currentUser); // For initial load or history
    Page<MessageDto> getMessagesForChatPaginated(Long chatId, User currentUser, Pageable pageable);
}