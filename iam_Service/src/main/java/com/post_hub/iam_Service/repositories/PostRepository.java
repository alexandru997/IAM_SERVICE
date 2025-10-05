package com.post_hub.iam_Service.repositories;

import com.post_hub.iam_Service.model.enteties.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface PostRepository extends JpaRepository<Post, Integer> {

    boolean existsByTitle(String title);

    Optional<Post> findByIdAndDeletedFalse(Integer id);
}
