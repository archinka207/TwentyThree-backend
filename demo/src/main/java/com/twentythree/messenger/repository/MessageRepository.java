package com.twentythree.messenger.repository;

import com.twentythree.messenger.entity.Chat;
import com.twentythree.messenger.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    // Найти все сообщения для конкретного чата, отсортированные по времени отправки
    List<Message> findByChatOrderBySentAtAsc(Chat chat);

    // Найти сообщения для конкретного чата с пагинацией (если сообщений много)
    Page<Message> findByChatOrderBySentAtDesc(Chat chat, Pageable pageable); // Desc for latest first

    // Можно добавить методы для поиска сообщений по отправителю, типу и т.д., если потребуется
}