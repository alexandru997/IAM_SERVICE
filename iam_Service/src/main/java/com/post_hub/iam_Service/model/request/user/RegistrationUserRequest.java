package com.post_hub.iam_Service.model.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
public class RegistrationUserRequest implements Serializable {

    @NotBlank
    private String username;

    @Email
    @NotNull
    private String email;

    @NotEmpty
    private String password;

    @NotEmpty
    private String confirmPassword;
}
