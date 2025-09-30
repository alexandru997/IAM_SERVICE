package com.post_hub.iam_Service.controller;

import com.post_hub.iam_Service.service.impl.PostServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping( "/posts")
public class PostController {

    private final PostServiceImpl postServiceImpl;

    @Autowired
    public PostController(PostServiceImpl postServiceImpl) {
        this.postServiceImpl = postServiceImpl;
    }

    @PostMapping("/create")
    public ResponseEntity<String> createPost(@RequestBody Map<String, Object> requestBody){
        String title =  (String) requestBody.get("title");
        String content = (String) requestBody.get("content");

        String postContent =  "Title: " + title + "\nContent: " + content+ "\n";

        postServiceImpl.CreatePost(postContent);

        return  new ResponseEntity<>("Post created with title: " + title, HttpStatus.OK);
    }

    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint(){
        return new ResponseEntity<>("API is working!", HttpStatus.OK);
    }

    @GetMapping("/create")
    public ResponseEntity<String> createPostDemo(){
        String title = "Demo Post";
        String content = "This is a demo post created via GET request";
        String postContent = "Title: " + title + "\nContent: " + content + "\n";
        
        postServiceImpl.CreatePost(postContent);
        
        return new ResponseEntity<>("Post created with title: " + title + " (via GET)", HttpStatus.OK);
    }
}
