package com.twentythree.messenger.service;
import com.twentythree.messenger.dto.chat.ChatCreateRequest;
import com.twentythree.messenger.dto.chat.ChatDto;
import com.twentythree.messenger.dto.chat.JoinChatRequestDto; // Or just interest ID
import com.twentythree.messenger.entity.User;

public interface ChatService {
    ChatDto createChat(ChatCreateRequest createRequest, User currentUser);
    ChatDto joinChatByInterest(Long interestId, User currentUser); // Or JoinChatRequestDto
    ChatDto getCurrentChatForUser(User currentUser);
    ChatDto getChatDetails(Long chatId, User currentUser); // To ensure user is part of it or for admin
    void leaveChat(Long chatId, User currentUser);
    void processExpiredChats(); // For scheduled task
}