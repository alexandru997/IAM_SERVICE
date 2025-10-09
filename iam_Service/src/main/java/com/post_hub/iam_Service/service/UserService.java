package com.post_hub.iam_Service.service;

import com.post_hub.iam_Service.model.dto.user.UserDTO;
import com.post_hub.iam_Service.model.response.IamResponse;
import jakarta.validation.constraints.NotNull;

public interface UserService {

    IamResponse<UserDTO> getById(@NotNull Integer userId);
}
