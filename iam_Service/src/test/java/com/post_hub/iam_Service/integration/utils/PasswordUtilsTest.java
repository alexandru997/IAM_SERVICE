package com.post_hub.iam_Service.integration.utils;

import com.post_hub.iam_Service.model.constants.ApiConstants;
import com.post_hub.iam_Service.utils.PasswordUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PasswordUtilsTest {

    @Test
    void isNotValid_FALSE() {
        Assertions.assertFalse(PasswordUtils.isNotValidPassword("Newpassword/8"), "Valid password should return FALSE");
    }

    @Test
    void isNotValid_newGeneratedPassword_8times_FALSE() {
        for (int i = 0; i < 8; i++) {
            String generatedPassword = PasswordUtils.generatePassword();
            Assertions.assertFalse(PasswordUtils.isNotValidPassword(generatedPassword),
                    "Generated password should always be valid: " + generatedPassword);
        }
    }

    @Test
    void isNotValid_PasswordLengthLess8_TRUE() {
        Assertions.assertTrue(PasswordUtils.isNotValidPassword("1234567"),
                "Password shorter than 8 should return TRUE");
    }

    @ParameterizedTest
    @ValueSource(strings = {"1234567a", "aaaaaaaa", "a54aaaaa", "aaa45+*aaaaa"})
    void isNotValid_LengthValid_NoUpperLetter_TRUE(String password) {
        runTestIsNotValid_TRUE(password, "Missing uppercase letter");
    }

    @ParameterizedTest
    @ValueSource(strings = {"1234567A", "AAAAAAAA", "A54AAAAA", "AAA45+*AAAAA"})
    void isNotValid_LengthValid_NoLowerLetter_TRUE(String password) {
        runTestIsNotValid_TRUE(password, "Missing lowercase letter");
    }

    @ParameterizedTest
    @ValueSource(strings = {"aaaaaa%A", "aaa%%AAA", "aaa+$#@#AAAA"})
    void isNotValid_LengthValid_NoDigit_TRUE(String password) {
        runTestIsNotValid_TRUE(password, "Missing digit");
    }

    @ParameterizedTest
    @ValueSource(strings = {"Aaaaaaaa1", "AAaaaaaa1", "AAaaaaa111"})
    void isNotValid_LengthValid_NoCharacter_TRUE(String password) {
        runTestIsNotValid_TRUE(password, "Missing special character");
    }

    private void runTestIsNotValid_TRUE(String password, String reason) {
        Assertions.assertTrue(password.length() >= ApiConstants.REQUIRED_MIN_PASSWORD_LENGTH,
                "Password should be at least " + ApiConstants.REQUIRED_MIN_PASSWORD_LENGTH + " characters long");
        Assertions.assertTrue(PasswordUtils.isNotValidPassword(password),
                "Invalid password case failed: " + reason + " -> " + password);
    }
}
