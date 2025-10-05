package com.post_hub.iam_Service.service;

import com.post_hub.iam_Service.model.dto.post.PostDTO;
import com.post_hub.iam_Service.model.request.post.PostRequest;
import com.post_hub.iam_Service.model.request.post.UpdatePostRequest;
import com.post_hub.iam_Service.model.response.IamResponse;
import jakarta.validation.constraints.NotNull;


public interface PostService {
    IamResponse<PostDTO> getById(@NotNull Integer postId);

    IamResponse<PostDTO> createPost(@NotNull PostRequest postRequest);

    IamResponse<PostDTO> updatePost(@NotNull Integer postId,  @NotNull UpdatePostRequest postRequest);

}
