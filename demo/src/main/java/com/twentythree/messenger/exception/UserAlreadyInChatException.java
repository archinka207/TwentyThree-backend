package com.twentythree.messenger.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT) // 409 Conflict is suitable here
public class UserAlreadyInChatException extends RuntimeException {
    public UserAlreadyInChatException(String message) {
        super(message);
    }
}