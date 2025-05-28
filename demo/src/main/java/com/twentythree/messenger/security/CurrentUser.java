package com.twentythree.messenger.security; // <--- ВАЖНО: правильный пакет

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER, ElementType.TYPE}) // Указывает, что аннотацию можно применять к параметрам методов и типам
@Retention(RetentionPolicy.RUNTIME) // Аннотация будет доступна во время выполнения
@Documented // Аннотация будет включена в Javadoc
@AuthenticationPrincipal // <--- Это ключевая аннотация Spring Security, которая делает магию
public @interface CurrentUser {}