package com.post_hub.iam_Service.model.dto.comment;

import com.post_hub.iam_Service.model.dto.post.PostOwnerDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentDTO implements Serializable {

    private Integer id;
    private String message;
    private PostOwnerDTO owner;
    private Integer postId;
    private LocalDateTime created;
    private LocalDateTime updated;
}
