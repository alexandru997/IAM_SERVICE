package com.post_hub.iam_Service.model.request.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
public class UpdateUserRequest  implements Serializable {

    private String username;


    private String email;

    private String password;
}
