package com.post_hub.iam_Service.repositories;

import com.post_hub.iam_Service.model.enteties.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Integer> {
    Optional<Comment> findByIdAndDeletedFalse(Integer commentId);
}
