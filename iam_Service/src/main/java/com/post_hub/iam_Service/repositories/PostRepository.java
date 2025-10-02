package com.post_hub.iam_Service.repositories;

import com.post_hub.iam_Service.model.enteties.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Integer> {

}
