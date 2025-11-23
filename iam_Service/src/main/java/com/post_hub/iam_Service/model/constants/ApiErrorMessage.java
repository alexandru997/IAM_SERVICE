package com.post_hub.iam_Service.model.constants;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ApiErrorMessage {
    POST_NOT_FOUND_BY_ID("Post with ID: %s not found"),
    POST_ALREADY_EXISTS("Post with title: %s already exists"),
    USER_NOT_FOUND_BY_ID("User with ID: %s not found"),
    USERNAME_ALREADY_EXISTS("Username: %s already exists"),
    EMAIL_ALREADY_EXISTS("Email: %s already exists"),
    EMAIL_NOT_FOUND("Email: %s not found"),
    USER_ROLE_NOT_FOUND("User role not found"),

    INVALID_TOKEN_SIGNATURE("Invalid token signature"),
    ERROR_DURING_JWT_PROCESSING("An unexpected error occurred during JWT processing"),
    TOKEN_EXPIRED("Token expired."),
    UNEXPECTED_ERROR_OCCURRED("An unexpected error occurred. Please try again later."),

    AUTHENTICATION_FAILED_FOR_USER("Authentication failed for user: {}. "),
    INVALID_USER_OR_PASSWORD("Invalid email or password. Try again"),
    INVALID_USER_REGISTRATION_STATUS("Invalid user registration status: %s. "),
    NOT_FOUND_REFRESH_TOKEN("Refresh token not found."),

    ;

    private final String message;

    public String getMessage(Object... args) {
        return String.format(message, args);
    }
}
