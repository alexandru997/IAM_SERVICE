package com.post_hub.iam_Service.service.impl;

import com.post_hub.iam_Service.mapper.UserMapper;
import com.post_hub.iam_Service.model.constants.ApiErrorMessage;
import com.post_hub.iam_Service.model.dto.user.UserDTO;
import com.post_hub.iam_Service.model.dto.user.UserSearchDTO;
import com.post_hub.iam_Service.model.enteties.Role;
import com.post_hub.iam_Service.model.enteties.User;
import com.post_hub.iam_Service.model.exeption.DataExistException;
import com.post_hub.iam_Service.model.exeption.NotFoundException;
import com.post_hub.iam_Service.model.request.user.NewUserRequest;
import com.post_hub.iam_Service.model.request.user.UpdateUserRequest;
import com.post_hub.iam_Service.model.request.user.UserSearchRequest;
import com.post_hub.iam_Service.model.response.IamResponse;
import com.post_hub.iam_Service.model.response.PaginationResponse;
import com.post_hub.iam_Service.repositories.RoleRepository;
import com.post_hub.iam_Service.repositories.UserRepository;
import com.post_hub.iam_Service.repositories.criteria.UserSearchCriteria;
import com.post_hub.iam_Service.security.validation.AccessValidator;
import com.post_hub.iam_Service.service.UserService;
import com.post_hub.iam_Service.service.model.IamServiceUserRole;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final AccessValidator accessValidator;
    @Override
    public IamResponse<UserDTO> getById(@NonNull Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new NotFoundException(ApiErrorMessage.USER_NOT_FOUND_BY_ID.getMessage(userId)));
        return IamResponse.createSuccessful(userMapper.toDto(user));

    }

    @Override
    public IamResponse<UserDTO> createUser(@NotNull NewUserRequest newUserRequest) {

        if (userRepository.existsByEmail(newUserRequest.getEmail())) {
            throw new DataExistException(ApiErrorMessage.EMAIL_ALREADY_EXISTS.getMessage(newUserRequest.getEmail()));
        }

        if (userRepository.existsByUsername(newUserRequest.getUsername())) {
            throw new DataExistException(ApiErrorMessage.USERNAME_ALREADY_EXISTS.getMessage(newUserRequest.getUsername()));

        }

        Role userRole =  roleRepository.findByName(IamServiceUserRole.USER.getRole())
                .orElseThrow(()-> new NotFoundException(ApiErrorMessage.USER_ROLE_NOT_FOUND.getMessage()));

        User user = userMapper.createUser(newUserRequest);
        user.setPassword(passwordEncoder.encode(newUserRequest.getPassword()));
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);
        User savedUser = userRepository.save(user);
        return IamResponse.createSuccessful(userMapper.toDto(savedUser));
    }

    @Override
    public IamResponse<UserDTO> updateUser(@NotNull Integer userId, UpdateUserRequest request) {
        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new NotFoundException(ApiErrorMessage.USER_NOT_FOUND_BY_ID.getMessage(userId)));
        accessValidator.validateAdminOrOwnerAccess(userId);

        if (!user.getUsername().equals(request.getUsername()) && userRepository.existsByUsername(request.getUsername())) {
            throw new DataExistException(ApiErrorMessage.USERNAME_ALREADY_EXISTS.getMessage(request.getUsername()));
        }

        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new DataExistException(ApiErrorMessage.EMAIL_ALREADY_EXISTS.getMessage(request.getEmail()));
        }

        userMapper.updateUser(user, request);
        user = userRepository.save(user);

        return IamResponse.createSuccessful(userMapper.toDto(user));
    }

    @Override
    public void softDeleteUser(Integer userId) {
        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new NotFoundException(ApiErrorMessage.USER_NOT_FOUND_BY_ID.getMessage(userId)));
        accessValidator.validateAdminOrOwnerAccess(userId);
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
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return getUserDetails(email, userRepository);
    }

    static UserDetails getUserDetails(String email, UserRepository userRepository) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ApiErrorMessage.EMAIL_NOT_FOUND.getMessage()));

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getName()))
                        .collect(Collectors.toList())
        );
    }
}
