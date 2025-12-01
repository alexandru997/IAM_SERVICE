package com.post_hub.iam_Service.service.impl;

import com.post_hub.iam_Service.mapper.UserMapper;
import com.post_hub.iam_Service.model.constants.ApiErrorMessage;
import com.post_hub.iam_Service.model.enteties.Role;
import com.post_hub.iam_Service.model.exeption.DataExistException;
import com.post_hub.iam_Service.model.exeption.InvalidPasswordException;
import com.post_hub.iam_Service.model.exeption.NotFoundException;
import com.post_hub.iam_Service.model.request.user.LoginRequest;
import com.post_hub.iam_Service.model.dto.user.UserProfileDTO;
import com.post_hub.iam_Service.model.enteties.RefreshToken;
import com.post_hub.iam_Service.model.enteties.User;
import com.post_hub.iam_Service.model.exception.InvalidDataException;
import com.post_hub.iam_Service.model.request.user.RegistrationUserRequest;
import com.post_hub.iam_Service.model.response.IamResponse;
import com.post_hub.iam_Service.repositories.RoleRepository;
import com.post_hub.iam_Service.repositories.UserRepository;
import com.post_hub.iam_Service.security.JwtTokenProvider;
import com.post_hub.iam_Service.service.AuthService;
import com.post_hub.iam_Service.service.model.IamServiceUserRole;
import com.post_hub.iam_Service.service.model.RefreshTokenService;
import com.post_hub.iam_Service.utils.PasswordUtils;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;


@Slf4j
@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public IamResponse<UserProfileDTO> login(@NotNull LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new InvalidDataException(ApiErrorMessage.INVALID_USER_OR_PASSWORD.getMessage());
        }

        User user = userRepository.findUserByEmailAndDeletedFalse(request.getEmail())
                .orElseThrow(() -> new InvalidDataException(ApiErrorMessage.INVALID_USER_OR_PASSWORD.getMessage()));
        RefreshToken refreshToken =  refreshTokenService.generateOrUpdateRefreshToken(user);
        String token = jwtTokenProvider.generateToken(user);
        UserProfileDTO userProfileDTO = userMapper.toUserProfileDto(user, token, refreshToken.getToken());
        userProfileDTO.setToken(token);

        return IamResponse.createSuccessfulWithNewToken(userProfileDTO);
    }

    @Override
    public IamResponse<UserProfileDTO> refreshAccessToken(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenService.validateAndRefreshToken(refreshTokenValue);
        User user = refreshToken.getUser();

        String accessToken =  jwtTokenProvider.generateToken(user);
        return IamResponse.createSuccessfulWithNewToken(
                userMapper.toUserProfileDto(user, accessToken, refreshToken.getToken())
        );
    }

    @Override
    public IamResponse<UserProfileDTO> registerUser(@NotNull  RegistrationUserRequest request) {
        userRepository.findByUsername(request.getUsername()).ifPresent(existingUser -> {
            throw new DataExistException(ApiErrorMessage.USERNAME_ALREADY_EXISTS.getMessage(request.getUsername()));
        });

        userRepository.findByEmail(request.getEmail()).ifPresent(existingUser -> {
            throw new DataExistException(ApiErrorMessage.EMAIL_ALREADY_EXISTS.getMessage(request.getEmail()));
        });

        String password = request.getPassword();
        String confirmPassword = request.getConfirmPassword();

        if (!password.equals(confirmPassword)) {
            throw new InvalidDataException(ApiErrorMessage.MISMATCH_PASSWORDS.getMessage());
        }

        if (PasswordUtils.isNotValidPassword(password)) {
            throw new InvalidPasswordException(ApiErrorMessage.INVALID_PASSWORD.getMessage());
        }

        Role userRole = roleRepository.findByName(IamServiceUserRole.USER.getRole())
                .orElseThrow(() -> new NotFoundException(ApiErrorMessage.USER_ROLE_NOT_FOUND.getMessage()));

        User newUser = userMapper.fromDto(request);
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        newUser.setRoles(roles);
        userRepository.save(newUser);

        RefreshToken refreshToken  = refreshTokenService.generateOrUpdateRefreshToken(newUser);
        String token = jwtTokenProvider.generateToken(newUser);
        UserProfileDTO userProfileDTO = userMapper.toUserProfileDto(newUser, token, refreshToken.getToken());
        userProfileDTO.setToken(token);

        return IamResponse.createSuccessfulWithNewToken(userProfileDTO);
    }

}
