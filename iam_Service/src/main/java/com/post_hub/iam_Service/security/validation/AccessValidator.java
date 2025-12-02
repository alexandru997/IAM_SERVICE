package com.post_hub.iam_Service.security.validation;

import com.post_hub.iam_Service.model.constants.ApiErrorMessage;
import com.post_hub.iam_Service.model.enteties.User;
import com.post_hub.iam_Service.model.exception.InvalidDataException;
import com.post_hub.iam_Service.model.exeption.DataExistException;
import com.post_hub.iam_Service.model.exeption.InvalidPasswordException;
import com.post_hub.iam_Service.model.exeption.NotFoundException;
import com.post_hub.iam_Service.repositories.UserRepository;
import com.post_hub.iam_Service.service.model.IamServiceUserRole;
import com.post_hub.iam_Service.utils.APIUtils;
import com.post_hub.iam_Service.utils.PasswordUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import org.springframework.security.access.AccessDeniedException;

@Component
@RequiredArgsConstructor
public class AccessValidator {
    private final UserRepository userRepository;
    private final APIUtils apiUtils;

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

    public boolean isAdminOrSuperAdmin(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ApiErrorMessage.USER_NOT_FOUND_BY_ID.getMessage(userId)));

        return user.getRoles().stream()
                .map(role -> IamServiceUserRole.fromName(role.getName()))
                .anyMatch(role -> role == IamServiceUserRole.ADMIN || role == IamServiceUserRole.SUPER_ADMIN);
    }
    @SneakyThrows
    public void validateAdminOrOwnerAccess(Integer ownerId) {
        Integer currentUserId = apiUtils.getUserIdFromAuthentication();

        if (!currentUserId.equals(ownerId) && !isAdminOrSuperAdmin(currentUserId)) {
            throw new AccessDeniedException(ApiErrorMessage.HAVE_NO_ACCESS.getMessage());
        }
    }
}
