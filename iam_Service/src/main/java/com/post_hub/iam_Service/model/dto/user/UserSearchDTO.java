package com.post_hub.iam_Service.model.dto.user;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class UserSearchDTO implements Serializable {

    private Integer id;
    private String username;
    private String email;
    private LocalDateTime created;
    private Boolean isDeleted;

}