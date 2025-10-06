package com.post_hub.iam_Service.model.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum PostSortField {
    @JsonProperty("Title")
    TITLE,
    @JsonProperty("Content")
    CONTENT,
    @JsonProperty("Likes")
    LIKES
}
