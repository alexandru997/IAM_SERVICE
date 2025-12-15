package com.post_hub.iam_Service.service.impl;

import com.post_hub.iam_Service.kafka.service.KafkaMessageService;
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
import com.post_hub.iam_Service.security.validation.AccessValidator;
import com.post_hub.iam_Service.service.PostService;
import com.post_hub.iam_Service.utils.APIUtils;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostMapper postMapper;
    private final AccessValidator accessValidator;
    private final APIUtils apiUtils;
    private final KafkaMessageService kafkaMessageService;

    @Override
    @Transactional(readOnly = true)
    public IamResponse<PostDTO> getById(@NotNull Integer postId) {
        Post post = postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new NotFoundException(ApiErrorMessage.POST_NOT_FOUND_BY_ID.getMessage(postId)));
        return IamResponse.createSuccessful(postMapper.toPostDTO(post));
    }

    @Override
    @Transactional
    public IamResponse<PostDTO> createPost(@NotNull  PostRequest postRequest) {
        if (postRepository.existsByTitle(postRequest.getTitle())) {
            throw new DataExistException(ApiErrorMessage.POST_ALREADY_EXISTS.getMessage(postRequest.getTitle()));
        }
        Integer userId = apiUtils.getUserIdFromAuthentication();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException(ApiErrorMessage.USERNAME_NOT_FOUND.getMessage(userId)));

        Post post = postMapper.createPost(postRequest, user, user.getUsername());
        post = postRepository.save(post);
        kafkaMessageService.sendPostCreatedMessage(user.getId(), post.getId());


        return IamResponse.createSuccessful(postMapper.toPostDTO(post));

    }

    @Override
    @Transactional
    public IamResponse<PostDTO> updatePost(@NotNull Integer postId, @NotNull UpdatePostRequest request) {
        Post post = postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new NotFoundException(ApiErrorMessage.POST_NOT_FOUND_BY_ID.getMessage(postId)));
        accessValidator.validateAdminOrOwnerAccess(post.getUser().getId());

        if (!post.getTitle().equals(request.getTitle()) && postRepository.existsByTitle(request.getTitle())) {
            throw new DataExistException(ApiErrorMessage.POST_ALREADY_EXISTS.getMessage(request.getTitle()));
        }
        postMapper.updatePost(post, request);
        post = postRepository.save(post);
        kafkaMessageService.sendPostUpdatedMessage(post.getUser().getId(), post.getId());

        return IamResponse.createSuccessful(postMapper.toPostDTO(post));
    }

    @Override
    @Transactional
    public void softDeletePost(Integer postId) {
        Post post = postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new NotFoundException(ApiErrorMessage.POST_NOT_FOUND_BY_ID.getMessage(postId)));
        accessValidator.validateAdminOrOwnerAccess(post.getUser().getId());
        post.setDeleted(true);
        postRepository.save(post);
        kafkaMessageService.sendPostDeletedMessage(post.getUser().getId(), postId);

    }

    @Override
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
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
