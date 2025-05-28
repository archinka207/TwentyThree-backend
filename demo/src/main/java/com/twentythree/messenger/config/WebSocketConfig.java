package com.twentythree.messenger.config; // Укажите ваш корректный пакет

import com.twentythree.messenger.security.JwtChannelInterceptor; // <--- ИМПОРТИРУЕМ НАШ ИНТЕРЦЕПТОР
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.simp.config.ChannelRegistration; // <--- НУЖЕН ДЛЯ РЕГИСТРАЦИИ ИНТЕРЦЕПТОРА
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
// @Order гарантирует, что эта конфигурация WebSocket будет обработана с определенным приоритетом.
// Spring Security для WebSocket также имеет свои конфигурации с порядком.
// Ordered.HIGHEST_PRECEDENCE + 99 - это распространенный способ убедиться,
// что ваша конфигурация применяется после базовых настроек безопасности, но до других кастомных.
// Возможно, это значение потребуется подобрать, если возникнут конфликты с другими конфигурациями.
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtChannelInterceptor jwtChannelInterceptor; // Инжектируем наш интерцептор

    @Autowired
    public WebSocketConfig(JwtChannelInterceptor jwtChannelInterceptor) {
        this.jwtChannelInterceptor = jwtChannelInterceptor;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Клиенты подписываются на топики, начинающиеся с /topic (для общих чатов)
        // или /queue (для персональных сообщений, если используются)
        config.enableSimpleBroker("/topic", "/queue");
        // Сообщения от клиентов к серверу направляются на эндпоинты с префиксом /app
        config.setApplicationDestinationPrefixes("/app");
        // Для использования @SendToUser и client.subscribe('/user/queue/errors')
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Эндпоинт, к которому клиенты будут подключаться для установления WebSocket соединения
        registry.addEndpoint("/ws") // Например, ws://localhost:8080/ws
                .setAllowedOriginPatterns("*") // В продакшене укажите конкретные домены фронтенда
                .withSockJS(); // Включаем SockJS как fallback для браузеров без нативной поддержки WebSocket
    }

    // --- ВОТ ЗДЕСЬ ПРОИСХОДИТ ПОДКЛЮЧЕНИЕ ИНТЕРЦЕПТОРА ---
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Добавляем наш JwtChannelInterceptor в цепочку обработки входящих сообщений от клиента.
        // Он будет срабатывать для каждого сообщения (CONNECT, SEND, SUBSCRIBE и т.д.),
        // но наша логика в preSend сфокусирована на команде CONNECT.
        registration.interceptors(jwtChannelInterceptor);
    }
    // --- КОНЕЦ ПОДКЛЮЧЕНИЯ ---
}