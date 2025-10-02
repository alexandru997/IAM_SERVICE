package com.post_hub.iam_Service.model.constants;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ApiErrorMessage {
    POST_NOT_FOUND_BY_ID("Post with ID: %s not found");

    private final String message;

    public String getMessage(Object... args){
        return String.format(message, args);
    }
}
