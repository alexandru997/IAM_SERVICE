package com.post_hub.iam_Service.service;

import com.post_hub.iam_Service.model.dto.post.PostDTO;
import com.post_hub.iam_Service.model.dto.post.PostSearchDTO;
import com.post_hub.iam_Service.model.request.post.PostRequest;
import com.post_hub.iam_Service.model.request.post.PostSearchRequest;
import com.post_hub.iam_Service.model.request.post.UpdatePostRequest;
import com.post_hub.iam_Service.model.response.IamResponse;
import com.post_hub.iam_Service.model.response.PaginationResponse;
import jakarta.validation.constraints.NotNull;

import org.springframework.data.domain.Pageable;


public interface PostService {
    IamResponse<PostDTO> getById(@NotNull Integer postId);

    IamResponse<PostDTO> createPost(@NotNull  PostRequest postRequest, String username);

    IamResponse<PostDTO> updatePost(@NotNull Integer postId,  @NotNull UpdatePostRequest postRequest);

    void softDeletePost(@NotNull Integer postId);

    IamResponse<PaginationResponse<PostSearchDTO>> findAllPosts(Pageable pageable) ;
    IamResponse<PaginationResponse<PostSearchDTO>> searchPosts(@NotNull PostSearchRequest request, Pageable pageable);

}
