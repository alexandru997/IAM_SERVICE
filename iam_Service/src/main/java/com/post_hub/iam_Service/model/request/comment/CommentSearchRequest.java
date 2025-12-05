package com.post_hub.iam_Service.model.request.comment;

import com.post_hub.iam_Service.model.enums.CommentSortField;
import lombok.Data;

@Data
public class CommentSearchRequest {
    private String message;
    private String createdBy;
    private Integer postId;

    private Boolean deleted;
    private String keyword;
    private CommentSortField sortField;
}
