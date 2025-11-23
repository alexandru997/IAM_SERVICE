package com.post_hub.iam_Service.service;

import com.post_hub.iam_Service.model.dto.user.UserDTO;
import com.post_hub.iam_Service.model.dto.user.UserSearchDTO;
import com.post_hub.iam_Service.model.request.user.NewUserRequest;
import com.post_hub.iam_Service.model.request.user.UpdateUserRequest;
import com.post_hub.iam_Service.model.request.user.UserSearchRequest;
import com.post_hub.iam_Service.model.response.IamResponse;
import com.post_hub.iam_Service.model.response.PaginationResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface UserService extends UserDetailsService {

    IamResponse<UserDTO> getById(@NotNull Integer userId);
    IamResponse<UserDTO> createUser(@NotNull NewUserRequest newUserRequest);
    IamResponse<UserDTO> updateUser(@NotNull Integer postId, @NotNull UpdateUserRequest request);

    void softDeleteUser(Integer userId);

    IamResponse<PaginationResponse<UserSearchDTO>> findAllUsers(Pageable pageable);

    IamResponse<PaginationResponse<UserSearchDTO>> searchUsers(UserSearchRequest request, Pageable pageable);

    UserDetails loadUserByUsername(String email) throws UsernameNotFoundException;
}
