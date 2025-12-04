package com.post_hub.iam_Service.service;

import com.post_hub.iam_Service.model.dto.comment.CommentDTO;
import com.post_hub.iam_Service.model.request.comment.CommentRequest;
import com.post_hub.iam_Service.model.request.comment.UpdateCommentRequest;
import com.post_hub.iam_Service.model.response.IamResponse;
import jakarta.validation.constraints.NotNull;

public interface CommentService {

    IamResponse<CommentDTO> getCommentById(@NotNull Integer commentId);
    IamResponse<CommentDTO> createComment(@NotNull CommentRequest request);
    IamResponse<CommentDTO> updateComment(@NotNull Integer commentId, @NotNull UpdateCommentRequest request);

    void softDelete(@NotNull Integer commentId);

}
