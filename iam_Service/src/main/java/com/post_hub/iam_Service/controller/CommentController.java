package com.post_hub.iam_Service.controller;

import com.post_hub.iam_Service.model.constants.ApiLogoMessage;
import com.post_hub.iam_Service.model.dto.comment.CommentDTO;
import com.post_hub.iam_Service.model.dto.comment.CommentSearchDTO;
import com.post_hub.iam_Service.model.request.comment.CommentRequest;
import com.post_hub.iam_Service.model.request.comment.CommentSearchRequest;
import com.post_hub.iam_Service.model.request.comment.UpdateCommentRequest;
import com.post_hub.iam_Service.model.response.IamResponse;
import com.post_hub.iam_Service.model.response.PaginationResponse;
import com.post_hub.iam_Service.service.CommentService;
import com.post_hub.iam_Service.utils.APIUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("${end.points.comments}")
public class CommentController {
    private final CommentService commentService;

    @GetMapping("${end.points.id}")
    public ResponseEntity<IamResponse<CommentDTO>> getCommentById(
            @PathVariable(name = "id") Integer commentId) {
        log.trace(ApiLogoMessage.NAME_OF_CURRENT_METHOD.getValue(), APIUtils.getMethodName());

        IamResponse<CommentDTO> response = commentService.getCommentById(commentId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("${end.points.create}")
    public ResponseEntity<IamResponse<CommentDTO>> createComment(
            @RequestBody @Valid CommentRequest commentRequest) {
        log.trace(ApiLogoMessage.NAME_OF_CURRENT_METHOD.getValue(), APIUtils.getMethodName());

        IamResponse<CommentDTO> response = commentService.createComment(commentRequest);
        return ResponseEntity.ok(response);
    }

    @PutMapping("${end.points.id}")
    public ResponseEntity<IamResponse<CommentDTO>> updateComment(
            @PathVariable(name = "id") Integer commentId,
            @RequestBody @Valid UpdateCommentRequest commentRequest) {
        log.trace(ApiLogoMessage.NAME_OF_CURRENT_METHOD.getValue(), APIUtils.getMethodName());

        IamResponse<CommentDTO> response = commentService.updateComment(commentId, commentRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("${end.points.id}")
    public ResponseEntity<Void> softDeleteComment(
            @PathVariable(name = "id") Integer commentId) {
        log.trace(ApiLogoMessage.NAME_OF_CURRENT_METHOD.getValue(), APIUtils.getMethodName());

        commentService.softDelete(commentId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("${end.points.all}")
    public ResponseEntity<IamResponse<PaginationResponse<CommentSearchDTO>>> getAllComments(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "limit", defaultValue = "10") int limit) {
        log.trace(ApiLogoMessage.NAME_OF_CURRENT_METHOD.getValue(), APIUtils.getMethodName());

        Pageable pageable = PageRequest.of(page, limit);

        IamResponse<PaginationResponse<CommentSearchDTO>> response = commentService.findAllComments(pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping("${end.points.search}")
    public ResponseEntity<IamResponse<PaginationResponse<CommentSearchDTO>>> searchComments(
            @RequestBody @Valid CommentSearchRequest request,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "limit", defaultValue = "10") int limit) {
        log.trace(ApiLogoMessage.NAME_OF_CURRENT_METHOD.getValue(), APIUtils.getMethodName());

        Pageable pageable = PageRequest.of(page, limit);
        IamResponse<PaginationResponse<CommentSearchDTO>> response = commentService.searchComments(request, pageable);

        return ResponseEntity.ok(response);
    }

}
