package com.post_hub.iam_Service.service.impl;

import com.post_hub.iam_Service.service.PostService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PostServiceImpl implements PostService {

    private final List<String> posts =  new ArrayList<>();

    @Override
    public void CreatePost(String postContent){
        posts.add(postContent);
    }
}
