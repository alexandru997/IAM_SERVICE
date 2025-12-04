package com.post_hub.iam_Service.controller;

import com.post_hub.iam_Service.model.constants.ApiLogoMessage;
import com.post_hub.iam_Service.model.dto.comment.CommentDTO;
import com.post_hub.iam_Service.model.response.IamResponse;
import com.post_hub.iam_Service.service.CommentService;
import com.post_hub.iam_Service.utils.APIUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
