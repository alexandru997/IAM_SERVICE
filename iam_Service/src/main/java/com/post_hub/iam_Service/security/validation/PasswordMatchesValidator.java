package com.post_hub.iam_Service.security.validation;

import com.post_hub.iam_Service.model.request.user.RegistrationUserRequest;
import com.post_hub.iam_Service.utils.PasswordMatches;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, RegistrationUserRequest> {

    @Override
    public boolean isValid(RegistrationUserRequest request, ConstraintValidatorContext constraintValidatorContext) {
        return request.getPassword().equals(request.getConfirmPassword());
    }
}
