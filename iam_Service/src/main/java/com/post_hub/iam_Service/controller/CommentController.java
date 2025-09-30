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
    private CommentService commentService;

    @Autowired
    public void  setCommentService(@Qualifier("commentServiceImpl") CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/create")
    public ResponseEntity<String> addComment(@RequestBody Map<String, Object> requestBody){
        String content = (String) requestBody.get("content");
        commentService.createComment(content);
        System.out.println("Comment added: " + content + " - Status: " + HttpStatus.OK);
        return new ResponseEntity<>("Comment added:" + content, HttpStatus.OK);
    }

    @PostMapping("/switchService")
    public ResponseEntity<String> switchToSecondService(@RequestBody Map<String, Object> requestBody){
        this.commentService =  new SecondCommentServiceImpl();
        String content = (String) requestBody.get("content");
        commentService.createComment(content);
        System.out.println("Switch to second comment service and added: " + content + " - Status: " + HttpStatus.OK);
        return new ResponseEntity<>("Switch to second comment service and added:" + content, HttpStatus.OK);
    }
}
