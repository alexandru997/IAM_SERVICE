package com.post_hub.iam_Service.service;

import com.post_hub.iam_Service.model.dto.comment.CommentDTO;
import com.post_hub.iam_Service.model.response.IamResponse;
import jakarta.validation.constraints.NotNull;

public interface CommentService {

    IamResponse<CommentDTO> getCommentById(@NotNull Integer commentId);
}
