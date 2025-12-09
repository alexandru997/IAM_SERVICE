package com.post_hub.iam_Service.controller;

import com.post_hub.iam_Service.model.constants.ApiLogoMessage;
import com.post_hub.iam_Service.model.request.user.LoginRequest;
import com.post_hub.iam_Service.model.dto.user.UserProfileDTO;
import com.post_hub.iam_Service.model.request.user.RegistrationUserRequest;
import com.post_hub.iam_Service.model.response.IamResponse;
import com.post_hub.iam_Service.service.AuthService;
import com.post_hub.iam_Service.utils.APIUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("${end.points.auth}")
@Tag(name = "Auth", description = "Authorization methods")public class AuthController {
    private final AuthService authService;

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful authorization",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{ \"token\": \"eyJhbGcIoIJIuz...\" }")))
    })

    @PostMapping("${end.points.login}")
    @Operation(
            summary = "User login",
            description = "Authenticates the user and returns an access/refresh token"
    )
    public ResponseEntity<?> login(
            @RequestBody @Valid LoginRequest request,
            HttpServletResponse response) {
        log.trace(ApiLogoMessage.NAME_OF_CURRENT_METHOD.getValue(), APIUtils.getMethodName());

        IamResponse<UserProfileDTO> result = authService.login(request);
        Cookie authorizationCookie = APIUtils.createAuthCookie(result.getPayload().getToken());
        response.addCookie(authorizationCookie);

        return ResponseEntity.ok(result);
    }
    @GetMapping("${end.points.refresh.token}")
    @Operation(
            summary = "Refresh access token",
            description = "Generates new access token using provided refresh token"
    )
    public ResponseEntity<IamResponse<UserProfileDTO>> refreshToken(
            @RequestParam(name = "token") String refreshToken,
            HttpServletResponse response) {
        log.trace(ApiLogoMessage.NAME_OF_CURRENT_METHOD.getValue(), APIUtils.getMethodName());

        IamResponse<UserProfileDTO> result = authService.refreshAccessToken(refreshToken);
        Cookie authorizationCookie = APIUtils.createAuthCookie(result.getPayload().getToken());
        response.addCookie(authorizationCookie);

        return ResponseEntity.ok(result);
    }

    @PostMapping("${end.points.register}")
    @Operation(
            summary = "Register a new user",
            description = "Creates new user and returns authentication details"
    )
    public ResponseEntity<?> register(
            @RequestBody @Valid RegistrationUserRequest request,
            HttpServletResponse response) {
        log.trace(ApiLogoMessage.NAME_OF_CURRENT_METHOD.getValue(), APIUtils.getMethodName());

        IamResponse<UserProfileDTO> result = authService.registerUser(request);
        Cookie authorizationCookie = APIUtils.createAuthCookie(result.getPayload().getToken());
        response.addCookie(authorizationCookie);

        return ResponseEntity.ok(result);
    }
}
