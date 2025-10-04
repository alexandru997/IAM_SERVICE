package com.post_hub.iam_Service.model.request.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostRequest implements Serializable {

    @NotBlank(message = "Title can not be empty")
    private String title;
    @NotBlank(message = "Content can not be empty")
    private String content;
    @NotNull(message = "Likes can not be empty")
    private Integer likes;
}
