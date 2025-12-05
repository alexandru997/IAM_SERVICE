package com.post_hub.iam_Service.repositories;

import com.post_hub.iam_Service.model.enteties.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Integer>, JpaSpecificationExecutor<Comment> {
    Optional<Comment> findByIdAndDeletedFalse(Integer commentId);
}
