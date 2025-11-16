package com.post_hub.iam_Service.service.impl;

import com.post_hub.iam_Service.mapper.PostMapper;
import com.post_hub.iam_Service.model.constants.ApiErrorMessage;
import com.post_hub.iam_Service.model.dto.post.PostDTO;
import com.post_hub.iam_Service.model.dto.post.PostSearchDTO;
import com.post_hub.iam_Service.model.enteties.Post;
import com.post_hub.iam_Service.model.enteties.User;
import com.post_hub.iam_Service.model.exeption.DataExistException;
import com.post_hub.iam_Service.model.exeption.NotFoundException;
import com.post_hub.iam_Service.model.request.post.PostRequest;
import com.post_hub.iam_Service.model.request.post.PostSearchRequest;
import com.post_hub.iam_Service.model.request.post.UpdatePostRequest;
import com.post_hub.iam_Service.model.response.IamResponse;
import com.post_hub.iam_Service.model.response.PaginationResponse;
import com.post_hub.iam_Service.repositories.PostRepository;
import com.post_hub.iam_Service.repositories.UserRepository;
import com.post_hub.iam_Service.repositories.criteria.PostSearchCriteria;
import com.post_hub.iam_Service.service.PostService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostMapper postMapper;

    @Override
    public IamResponse<PostDTO> getById(@NotNull Integer postId) {
        Post post = postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new NotFoundException(ApiErrorMessage.POST_NOT_FOUND_BY_ID.getMessage(postId)));
        PostDTO postDTO = postMapper.toPostDTO(post);
        return IamResponse.createSuccessful(postDTO);
    }

    @Override
    public IamResponse<PostDTO> createPost(@NotNull Integer userId, PostRequest postRequest) {

        if (postRepository.existsByTitle(postRequest.getTitle())) {
            throw new DataExistException(ApiErrorMessage.POST_ALREADY_EXISTS.getMessage(postRequest.getTitle()));
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ApiErrorMessage.POST_NOT_FOUND_BY_ID.getMessage(userId)));

        Post post = postMapper.createPost(postRequest, user);
        Post savedPost = postRepository.save(post);
        PostDTO postDTO = postMapper.toPostDTO(savedPost);
        return IamResponse.createSuccessful(postDTO);

    }

    @Override
    public IamResponse<PostDTO> updatePost(@NotNull Integer postId, @NotNull UpdatePostRequest postRequest) {
        Post post = postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new NotFoundException(ApiErrorMessage.POST_NOT_FOUND_BY_ID.getMessage(postId)));

        postMapper.updatePost(post, postRequest);
        post.setUpdated(LocalDateTime.now());
        post = postRepository.save(post);
        PostDTO postDTO = postMapper.toPostDTO(post);
        return IamResponse.createSuccessful(postDTO);
    }

    @Override
    public void softDeletePost(Integer postId) {
        Post post = postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new NotFoundException(ApiErrorMessage.POST_NOT_FOUND_BY_ID.getMessage(postId)));

        post.setDeleted(true);
        postRepository.save(post);
    }

    @Override
    public IamResponse<PaginationResponse<PostSearchDTO>> findAllPosts(Pageable pageable) {
        Page<PostSearchDTO> posts = postRepository.findAll(pageable)
                .map(postMapper::toPostSearchDTO);

        PaginationResponse<PostSearchDTO> paginationResponse = new PaginationResponse<>(
                posts.getContent(),
                new PaginationResponse.Pagination(
                        posts.getTotalElements(),
                        pageable.getPageSize(),
                        posts.getNumber() + 1,
                        posts.getTotalPages()
                )
        );

        return IamResponse.createSuccessful(paginationResponse);
    }

    @Override
    public IamResponse<PaginationResponse<PostSearchDTO>> searchPosts(PostSearchRequest request, Pageable pageable) {
        Specification<Post> specification = new PostSearchCriteria(request);
        Page<PostSearchDTO> posts = postRepository.findAll(specification, pageable)
                .map(postMapper::toPostSearchDTO);

        PaginationResponse<PostSearchDTO> response = PaginationResponse.<PostSearchDTO>builder()
                .content(posts.getContent())
                .pagination(PaginationResponse.Pagination.builder()
                        .total(posts.getTotalElements())
                        .limit(pageable.getPageSize())
                        .page(posts.getNumber()+1)
                        .pages(posts.getTotalPages())
                        .build())
                .build();

        return IamResponse.createSuccessful(response);
    }
}
