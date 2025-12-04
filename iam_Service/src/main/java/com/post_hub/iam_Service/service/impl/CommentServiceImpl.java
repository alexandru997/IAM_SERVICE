package com.post_hub.iam_Service.service.impl;

import com.post_hub.iam_Service.mapper.CommentMapper;
import com.post_hub.iam_Service.model.constants.ApiErrorMessage;
import com.post_hub.iam_Service.model.dto.comment.CommentDTO;
import com.post_hub.iam_Service.model.enteties.Comment;
import com.post_hub.iam_Service.model.exeption.NotFoundException;
import com.post_hub.iam_Service.model.response.IamResponse;
import com.post_hub.iam_Service.repositories.CommentRepository;
import com.post_hub.iam_Service.service.CommentService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    @Override
    public IamResponse<CommentDTO> getCommentById(@NotNull Integer commentId) {
        Comment comment = commentRepository.findByIdAndDeletedFalse(commentId)
                .orElseThrow(() -> new NotFoundException(ApiErrorMessage.COMMENT_NOT_FOUND_BY_ID.getMessage(commentId)));

        return IamResponse.createSuccessful(commentMapper.toDto(comment));
    }
}
