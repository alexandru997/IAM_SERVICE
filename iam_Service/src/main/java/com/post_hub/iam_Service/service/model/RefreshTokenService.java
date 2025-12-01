package com.post_hub.iam_Service.service.model;

import com.post_hub.iam_Service.model.enteties.RefreshToken;
import com.post_hub.iam_Service.model.enteties.User;

public interface RefreshTokenService {
    RefreshToken generateOrUpdateRefreshToken(User user);

    RefreshToken validateAndRefreshToken(String refreshToken);
}
