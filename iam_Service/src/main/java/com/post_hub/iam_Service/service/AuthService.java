package com.post_hub.iam_Service.service;

import com.post_hub.iam_Service.model.dto.user.LoginRequest;
import com.post_hub.iam_Service.model.dto.user.UserProfileDTO;
import com.post_hub.iam_Service.model.response.IamResponse;

public interface AuthService {
    IamResponse<UserProfileDTO> login(LoginRequest request);

    IamResponse<UserProfileDTO> refreshAccessToken(String refreshToken);
}
