package com.post_hub.iam_Service.service.impl;

import com.post_hub.iam_Service.service.CommentService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SecondCommentServiceImpl implements CommentService {
    private final List<String> comments =  new ArrayList<>();

    @Override
    public void createComment(String commentContent) {
        String advancedComment ="[" + LocalDateTime.now() + "]" + commentContent.toLowerCase();
        comments.add(commentContent);
        System.out.println("Advanced Comment created: " + advancedComment);
    }
}
