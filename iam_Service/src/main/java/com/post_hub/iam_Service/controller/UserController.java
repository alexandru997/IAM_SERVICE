package com.post_hub.iam_Service.controller;

import com.post_hub.iam_Service.model.constants.ApiLogoMessage;
import com.post_hub.iam_Service.model.dto.user.UserDTO;
import com.post_hub.iam_Service.model.dto.user.UserSearchDTO;
import com.post_hub.iam_Service.model.request.user.NewUserRequest;
import com.post_hub.iam_Service.model.request.user.UpdateUserRequest;
import com.post_hub.iam_Service.model.request.user.UserSearchRequest;
import com.post_hub.iam_Service.model.response.IamResponse;
import com.post_hub.iam_Service.model.response.PaginationResponse;
import com.post_hub.iam_Service.service.UserService;
import com.post_hub.iam_Service.utils.APIUtils;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Slf4j
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("${end.point.users}")
public class UserController {
    private final UserService userService;

    @GetMapping("${end.point.id}")
    @Operation(summary = "Get User by ID", description = "Retrieves user details by their unique identifier")
    public ResponseEntity<IamResponse<UserDTO>> getUserById(
            @PathVariable(name = "id") Integer userId) {
        log.trace(ApiLogoMessage.NAME_OF_CURRENT_METHOD.getValue(), APIUtils.getMethodName());
        IamResponse<UserDTO> response = userService.getById(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("${end.points.create}")
    @Operation(summary = "Create a new User [only for Admins]", description = "Registers a new user in the system")
    public ResponseEntity<IamResponse<UserDTO>> createUser(
            @RequestBody @Valid NewUserRequest request) {
        log.trace(ApiLogoMessage.NAME_OF_CURRENT_METHOD.getValue(), APIUtils.getMethodName());

        IamResponse<UserDTO> createdUser = userService.createUser(request);
        return ResponseEntity.ok(createdUser);
    }

    @PutMapping("${end.point.id}")
    @Operation(summary = "Update User", description = "Updates an existing user by their ID")
    public ResponseEntity<IamResponse<UserDTO>> updateUserById(
            @PathVariable(name = "id") Integer userId,
            @RequestBody @Valid UpdateUserRequest request) {
        log.trace(ApiLogoMessage.NAME_OF_CURRENT_METHOD.getValue(), APIUtils.getMethodName());

        IamResponse<UserDTO> update = userService.updateUser(userId, request);
        return ResponseEntity.ok(update);
    }

    @DeleteMapping("${end.point.id}")
    @Operation(summary = "Delete User", description = "Marks a user as deleted without removing them from the database")
    public ResponseEntity<Void> softDeleteUser(
            @PathVariable(name = "id") Integer userId
    ) {
        log.trace(ApiLogoMessage.NAME_OF_CURRENT_METHOD.getValue(), APIUtils.getMethodName());

        userService.softDeleteUser(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("${end.points.all}")
    @Operation(summary = "Get all Users", description = "Retrieves a paginated list of all registered users")
    public ResponseEntity<IamResponse<PaginationResponse<UserSearchDTO>>> getAllUsers(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "limit", defaultValue = "10") int limit) {
        log.trace(ApiLogoMessage.NAME_OF_CURRENT_METHOD.getValue(), APIUtils.getMethodName());

        Pageable pageable = PageRequest.of(page, limit);
        IamResponse<PaginationResponse<UserSearchDTO>> response = userService.findAllUsers(pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping("${end.points.search}")
    @Operation(summary = "Search Users", description = "Filters users based on search criteria and pagination settings")
    public ResponseEntity<IamResponse<PaginationResponse<UserSearchDTO>>> searchUsers(
            @RequestBody @Valid UserSearchRequest request,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "limit", defaultValue = "10") int limit) {
        log.trace(ApiLogoMessage.NAME_OF_CURRENT_METHOD.getValue(), APIUtils.getMethodName());

        Pageable pageable = PageRequest.of(page, limit);
        IamResponse<PaginationResponse<UserSearchDTO>> response = userService.searchUsers(request, pageable);
        return ResponseEntity.ok(response);
    }
}
