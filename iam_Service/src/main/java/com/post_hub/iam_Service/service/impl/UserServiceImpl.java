package com.post_hub.iam_Service.service.impl;

import com.post_hub.iam_Service.mapper.UserMapper;
import com.post_hub.iam_Service.model.constants.ApiErrorMessage;
import com.post_hub.iam_Service.model.dto.user.UserDTO;
import com.post_hub.iam_Service.model.dto.user.UserSearchDTO;
import com.post_hub.iam_Service.model.enteties.User;
import com.post_hub.iam_Service.model.exeption.DataExistException;
import com.post_hub.iam_Service.model.exeption.NotFoundException;
import com.post_hub.iam_Service.model.request.user.NewUserRequest;
import com.post_hub.iam_Service.model.request.user.UpdateUserRequest;
import com.post_hub.iam_Service.model.request.user.UserSearchRequest;
import com.post_hub.iam_Service.model.response.IamResponse;
import com.post_hub.iam_Service.model.response.PaginationResponse;
import com.post_hub.iam_Service.repositories.UserRepository;
import com.post_hub.iam_Service.repositories.criteria.UserSearchCriteria;
import com.post_hub.iam_Service.service.UserService;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    @Override
    public IamResponse<UserDTO> getById(@NonNull Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new NotFoundException(ApiErrorMessage.USER_NOT_FOUND_BY_ID.getMessage(userId)));
        UserDTO userDTO =  userMapper.toDto(user);
        return IamResponse.createSuccessful(userDTO);

    }

    @Override
    public IamResponse<UserDTO> createUser(@NotNull NewUserRequest newUserRequest) {
        if (userRepository.existsByUsername(newUserRequest.getUsername())) {
            throw new DataExistException(ApiErrorMessage.USERNAME_ALREADY_EXISTS.getMessage(newUserRequest.getUsername()));
        }

        if (userRepository.existsByEmail(newUserRequest.getEmail())) {
            throw new DataExistException(ApiErrorMessage.EMAIL_ALREADY_EXISTS.getMessage(newUserRequest.getEmail()));
        }

        User user = userMapper.createUser(newUserRequest);
        user.setPassword(passwordEncoder.encode(newUserRequest.getPassword()));
        User savedUser = userRepository.save(user);
        UserDTO userDTO = userMapper.toDto(savedUser);

        return IamResponse.createSuccessful(userDTO);
    }

    @Override
    public IamResponse<UserDTO> updateUser(@NotNull Integer userId, @NotNull UpdateUserRequest request) {
        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new NotFoundException(ApiErrorMessage.USER_NOT_FOUND_BY_ID.getMessage(userId)));

        userMapper.updateUser(user, request);
        user.setUpdated(LocalDateTime.now());
        user = userRepository.save(user);

        UserDTO userDTO = userMapper.toDto(user);
        return IamResponse.createSuccessful(userDTO);
    }

    @Override
    public void softDeleteUser(Integer userId) {
        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new NotFoundException(ApiErrorMessage.USER_NOT_FOUND_BY_ID.getMessage(userId)));

        user.setDeleted(true);
        userRepository.save(user);
    }

    @Override
    public IamResponse<PaginationResponse<UserSearchDTO>> findAllUsers(Pageable pageable) {
        Page<UserSearchDTO> users = userRepository.findAll(pageable)
                .map(userMapper::toUserSearchDTO);

        PaginationResponse<UserSearchDTO> paginationResponse = new PaginationResponse<>(
                users.getContent(),
                new PaginationResponse.Pagination(
                        users.getTotalElements(),
                        pageable.getPageSize(),
                        users.getNumber() + 1,
                        users.getTotalPages()
                )
        );

        return IamResponse.createSuccessful(paginationResponse);
    }

    @Override
    public IamResponse<PaginationResponse<UserSearchDTO>> searchUsers(UserSearchRequest request, Pageable pageable) {
        Specification<User> specification = new UserSearchCriteria(request);

        Page<UserSearchDTO> usersPage = userRepository.findAll(specification, pageable)
                .map(userMapper::toUserSearchDTO);

        PaginationResponse<UserSearchDTO> response = PaginationResponse.<UserSearchDTO>builder()
                .content(usersPage.getContent())
                .pagination(PaginationResponse.Pagination.builder()
                        .total(usersPage.getTotalElements())
                        .limit(pageable.getPageSize())
                        .page(usersPage.getNumber() + 1)
                        .pages(usersPage.getTotalPages())
                        .build())
                .build();

        return IamResponse.createSuccessful(response);
    }
}
