package com.twentythree.messenger.security; // Укажите ваш корректный пакет

import com.twentythree.messenger.security.JwtTokenProvider; // Ваш провайдер JWT токенов
import com.twentythree.messenger.security.CustomUserDetailsService; // Ваш сервис для загрузки UserDetails
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy; // Используем Lazy, чтобы избежать циклической зависимости при инициализации SecurityConfig и WebSocketConfig
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

@Component // Делает этот класс бином Spring, чтобы его можно было инжектировать
public class JwtChannelInterceptor implements ChannelInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(JwtChannelInterceptor.class);

    // Используем @Lazy для зависимостей, чтобы разорвать возможные циклы при инициализации бинов,
    // особенно если JwtTokenProvider или CustomUserDetailsService как-то зависят от SecurityConfig,
    // а WebSocketConfig (где используется этот интерцептор) может инициализироваться раньше.
    // В большинстве случаев это не требуется, но это безопасная практика.
    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    @Autowired
    public JwtChannelInterceptor(@Lazy JwtTokenProvider tokenProvider,
                                 @Lazy CustomUserDetailsService customUserDetailsService) {
        this.tokenProvider = tokenProvider;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        // StompHeaderAccessor позволяет работать с заголовками STOMP сообщений
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // Проверяем, что accessor не null и что команда - это CONNECT
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // Пытаемся получить заголовок 'Authorization' (или другой, если вы его изменили на фронтенде)
            // Фронтенд передает его в client.connectHeaders: { Authorization: `Bearer ${token}` }
            List<String> authorizationHeaders = accessor.getNativeHeader("Authorization");
            String jwt = null;

            if (authorizationHeaders != null && !authorizationHeaders.isEmpty()) {
                String bearerToken = authorizationHeaders.get(0); // Берем первый заголовок Authorization
                if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
                    jwt = bearerToken.substring(7); // Удаляем префикс "Bearer "
                } else {
                    logger.warn("WebSocket CONNECT: Authorization header does not start with Bearer: {}", bearerToken);
                }
            } else {
                logger.warn("WebSocket CONNECT: No Authorization header found.");
            }

            if (jwt != null) {
                if (tokenProvider.validateToken(jwt)) { // Валидируем токен
                    try {
                        Long userId = tokenProvider.getUserIdFromJWT(jwt); // Получаем ID пользователя из токена
                        UserDetails userDetails = customUserDetailsService.loadUserById(userId); // Загружаем UserDetails

                        // Создаем объект аутентификации
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                        // Ключевой момент: устанавливаем аутентифицированного пользователя в StompHeaderAccessor.
                        // Spring Security для WebSocket затем будет использовать этого пользователя для авторизации
                        // и для заполнения Principal в ваших @MessageMapping методах.
                        accessor.setUser(authentication);
                        // SecurityContextHolder.getContext().setAuthentication(authentication); // Это обычно не нужно для WebSocket, accessor.setUser важнее

                        logger.info("WebSocket CONNECT: User '{}' authenticated successfully via JWT.", userDetails.getUsername());
                    } catch (Exception e) {
                        logger.error("WebSocket CONNECT: Failed to set user authentication from JWT. Token: [MASKED]", e);
                        // Если произошла ошибка при аутентификации (например, пользователь не найден),
                        // пользователь останется неаутентифицированным для этой WebSocket сессии.
                        // Сервер может разорвать соединение или обработать это как анонимное (если разрешено).
                        // Наш MessageController затем выдаст ошибку "User not authenticated for WebSocket action".
                    }
                } else {
                    logger.warn("WebSocket CONNECT: Invalid JWT token provided.");
                    // Токен не прошел валидацию.
                }
            } else {
                logger.warn("WebSocket CONNECT: JWT token is missing in Authorization header.");
                // Токен отсутствует.
            }
        }
        // Для команд SEND, SUBSCRIBE и т.д., которые приходят после CONNECT,
        // Spring должен использовать Principal, установленный через accessor.setUser() на этапе CONNECT.
        // Если accessor.getUser() не равен null, то SecurityContextHolder в потоке обработки сообщения
        // также будет заполнен, что позволит @PreAuthorize работать на @MessageMapping методах (если настроено).

        return message; // Возвращаем сообщение (возможно, с измененными заголовками) для дальнейшей обработки
    }

    // Остальные методы интерфейса ChannelInterceptor можно оставить с реализацией по умолчанию (return message; или пустые)
    // preSend - самый важный для аутентификации при подключении.

    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
        // Можно использовать для логирования или другой постобработки после отправки сообщения
    }

    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
        // Вызывается после того, как сообщение было отправлено (или не отправлено из-за ошибки)
    }

    @Override
    public boolean preReceive(MessageChannel channel) {
        // Для входящего канала (от клиента к брокеру), обычно true
        return true;
    }

    @Override
    public Message<?> postReceive(Message<?> message, MessageChannel channel) {
        // Для входящего канала, после получения сообщения, но до его обработки
        return message;
    }

    @Override
    public void afterReceiveCompletion(Message<?> message, MessageChannel channel, Exception ex) {
        // После завершения обработки полученного сообщения
    }
}