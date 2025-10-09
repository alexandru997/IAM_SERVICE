package com.post_hub.iam_Service.service.impl;

import com.post_hub.iam_Service.mapper.UserMapper;
import com.post_hub.iam_Service.model.constants.ApiErrorMessage;
import com.post_hub.iam_Service.model.dto.user.UserDTO;
import com.post_hub.iam_Service.model.enteties.User;
import com.post_hub.iam_Service.model.exeption.NotFoundException;
import com.post_hub.iam_Service.model.response.IamResponse;
import com.post_hub.iam_Service.repositories.UserRepository;
import com.post_hub.iam_Service.service.UserService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    @Override
    public IamResponse<UserDTO> getById(@NonNull Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new NotFoundException(ApiErrorMessage.USER_NOT_FOUND_BY_ID.getMessage(userId)));
        UserDTO userDTO =  userMapper.toDto(user);
        return IamResponse.createSuccessful(userDTO);

    }
}
