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
    USER_ROLE_NOT_FOUND("User role not found"),
    ;

    private final String message;

    public String getMessage(Object... args){
        return String.format(message, args);
    }
}
