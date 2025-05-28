package com.twentythree.messenger.util;

public final class AppConstants {

    // Default pagination settings (can be overridden by request params)
    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_PAGE_SIZE = "10"; // e.g., for messages
    public static final int MAX_PAGE_SIZE = 50;

    // Chat related constants
    public static final long DEFAULT_CHAT_DURATION_MINUTES = 60; // 1 hour

    // Other constants if needed, e.g., default reputation, specific role names if any.
    // public static final String ROLE_USER = "ROLE_USER";
    // public static final String ROLE_ADMIN = "ROLE_ADMIN";

    private AppConstants() {
        // This utility class is not publicly instantiable
    }
}