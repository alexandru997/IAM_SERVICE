package com.post_hub.iam_Service.model.constants;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)

public enum ApiLogoMessage {
    POST_INFO_BY_ID("Receiving post with ID: %s");
    
    private final String message;
    
    public String getMessage(Object... args){
        return String.format(message, args);
    }
}
