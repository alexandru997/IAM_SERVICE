package com.post_hub.iam_Service.service;

import com.post_hub.iam_Service.model.dto.post.PostDTO;
import com.post_hub.iam_Service.model.response.IamResponse;
import jakarta.validation.constraints.NotNull;


public interface PostService {
    IamResponse<PostDTO> getById(@NotNull Integer postId);

}
