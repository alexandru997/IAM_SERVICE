package com.post_hub.iam_Service.controller;

import com.post_hub.iam_Service.service.CommentService;
import com.post_hub.iam_Service.service.impl.SecondCommentServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/comments")
public class CommentController {
    private final CommentService defaultCommentService;
    private final CommentService advancedCommentService;

    @Autowired
    public CommentController(CommentService defaultCommentService,
                             @Qualifier("advancedCommentService") CommentService advancedCommentService) {
        this.defaultCommentService = defaultCommentService;
        this.advancedCommentService = advancedCommentService;
    }


    @PostMapping("/createDefaultComment")
    public ResponseEntity<String> createDefaultComment(@RequestBody Map<String, Object> requestBody){
        String content = (String) requestBody.get("content");
        defaultCommentService.createComment(content);
        System.out.println("Default Comment added: " + content + " - Status: " + HttpStatus.OK);
        return new ResponseEntity<>("Default Comment added:" + content, HttpStatus.OK);
    }

    @PostMapping("/createAdvancedComment")
    public ResponseEntity<String> createAdvancedComment(@RequestBody Map<String, Object> requestBody){
        String content = (String) requestBody.get("content");
        advancedCommentService.createComment(content);
        System.out.println("Advanced comment  added: " + content + " - Status: " + HttpStatus.OK);
        return new ResponseEntity<>("Advanced comment  added:" + content, HttpStatus.OK);
    }
}
