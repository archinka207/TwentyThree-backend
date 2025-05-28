package com.twentythree.messenger.config;

import com.twentythree.messenger.security.CustomUserDetailsService;
import com.twentythree.messenger.security.JwtAuthenticationEntryPoint;
import com.twentythree.messenger.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity; // Важный импорт
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration; // Для настройки CORS
import org.springframework.web.cors.CorsConfigurationSource; // Для настройки CORS
import org.springframework.web.cors.UrlBasedCorsConfigurationSource; // Для настройки CORS

import java.util.Arrays; // Для настройки CORS

@Configuration
@EnableWebSecurity // Включает веб-безопасность Spring
@EnableMethodSecurity(
    prePostEnabled = true,  // Включает @PreAuthorize и @PostAuthorize
    securedEnabled = true,  // Включает @Secured
    jsr250Enabled = true    // Включает @RolesAllowed
)
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService; // Для загрузки информации о пользователе

    @Autowired
    private JwtAuthenticationEntryPoint unauthorizedHandler; // Обработчик ошибок аутентификации (401)

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        // Фильтр для обработки JWT токенов при каждом запросе
        return new JwtAuthenticationFilter();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Используем BCrypt для хеширования паролей
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        // Менеджер аутентификации, используемый для проверки учетных данных пользователя
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Включаем и настраиваем CORS
            .csrf(csrf -> csrf.disable()) // Отключаем CSRF, так как используем JWT (stateless)
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(unauthorizedHandler) // Указываем обработчик для неавторизованных запросов
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Сессии не создаются, каждый запрос аутентифицируется по токену
            )
            .authorizeHttpRequests(authorize -> authorize
                // Публичные эндпоинты, доступные без аутентификации
                .requestMatchers(HttpMethod.POST, "/api/auth/**").permitAll() // Регистрация и логин
                .requestMatchers(HttpMethod.GET, "/api/interests").permitAll() // Получение списка интересов
                .requestMatchers("/ws/**").permitAll() // WebSocket эндпоинты
                .requestMatchers("/static/images/**").permitAll() // Доступ к загруженным изображениям (аватары, картинки в чатах)
                // Можно добавить другие публичные эндпоинты для главной страницы и т.д.
                // .requestMatchers(HttpMethod.GET, "/api/public/**").permitAll()

                // Все остальные запросы требуют аутентификации
                .anyRequest().authenticated()
            );

        // Добавляем наш JWT фильтр перед стандартным фильтром аутентификации по имени пользователя и паролю
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Конфигурация CORS, чтобы разрешить запросы с фронтенда (например, с другого порта)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:8081")); // Укажите URL вашего фронтенда
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type", "X-Requested-With", "Accept"));
        configuration.setAllowCredentials(true); // Разрешить отправку cookies и заголовков авторизации
        configuration.setMaxAge(3600L); // Время кеширования pre-flight запроса

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Применяем конфигурацию ко всем путям
        return source;
    }
}