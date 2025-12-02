package com.post_hub.iam_Service.security.validation;

import com.post_hub.iam_Service.model.constants.ApiErrorMessage;
import com.post_hub.iam_Service.model.exception.InvalidDataException;
import com.post_hub.iam_Service.model.exeption.DataExistException;
import com.post_hub.iam_Service.model.exeption.InvalidPasswordException;
import com.post_hub.iam_Service.repositories.UserRepository;
import com.post_hub.iam_Service.utils.PasswordUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessValidator {
    private final UserRepository userRepository;

    public void validateNewUser(String username, String email, String password, String confirmPassword) {
        userRepository.findByUsername(username).ifPresent(existingUser -> {
            throw new DataExistException(ApiErrorMessage.USERNAME_ALREADY_EXISTS.getMessage(username));
        });

        userRepository.findByEmail(email).ifPresent(existingUser -> {
            throw new DataExistException(ApiErrorMessage.EMAIL_ALREADY_EXISTS.getMessage(email));
        });

        if (!password.equals(confirmPassword)) {
            throw new InvalidDataException(ApiErrorMessage.MISMATCH_PASSWORDS.getMessage());
        }

        if (PasswordUtils.isNotValidPassword(password)) {
            throw new InvalidPasswordException(ApiErrorMessage.INVALID_PASSWORD.getMessage());
        }

    }
}
