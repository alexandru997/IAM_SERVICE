package com.post_hub.iam_Service.service.impl;

import com.post_hub.iam_Service.model.constants.ApiErrorMessage;
import com.post_hub.iam_Service.model.dto.post.PostDTO;
import com.post_hub.iam_Service.model.enteties.Post;
import com.post_hub.iam_Service.model.exeption.NotFoundException;
import com.post_hub.iam_Service.model.response.IamResponse;
import com.post_hub.iam_Service.repositories.PostRepository;
import com.post_hub.iam_Service.service.PostService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;
    @Override
    public IamResponse<PostDTO> getById(@NotNull Integer postId) {
       Post post =  postRepository.findById(postId)
               .orElseThrow(() -> new NotFoundException(ApiErrorMessage.POST_NOT_FOUND_BY_ID.getMessage(postId)));
       PostDTO postDTO = PostDTO.builder()
               .id(post.getId())
               .title(post.getTitle())
               .content(post.getContent())
               .likes(post.getLikes())
               .created(post.getCreated())
               .build();
       return IamResponse.createSuccessful(postDTO);
    }
}
