package com.post_hub.iam_Service.integration.user_controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.post_hub.iam_Service.IamServiceApplication;
import com.post_hub.iam_Service.model.enteties.User;
import com.post_hub.iam_Service.model.exception.InvalidDataException;
import com.post_hub.iam_Service.model.request.user.NewUserRequest;
import com.post_hub.iam_Service.repositories.UserRepository;
import com.post_hub.iam_Service.security.JwtTokenProvider;
import lombok.Setter;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SpringBootTest(classes = {IamServiceApplication.class})
@AutoConfigureMockMvc
@ExtendWith({MockitoExtension.class, SpringExtension.class})
public class AccessToEndPointTest {

    @Autowired
    @Setter
    private MockMvc mvc;

    @Autowired @Setter
    private JwtTokenProvider jwtTokenProvider;

    @Autowired @Setter
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private String superAdminJwt;
    private String userJwt;

    @BeforeAll
    void setUp() {
        User superAdmin = userRepository.findById(1)
                .orElseThrow(() -> new InvalidDataException("Super-admin with ID 1 not found"));
        Hibernate.initialize(superAdmin.getRoles());
        this.superAdminJwt = "Bearer " + jwtTokenProvider.generateToken(superAdmin);

        User user = userRepository.findById(3)
                .orElseThrow(() -> new InvalidDataException("User ID 3 not found"));
        Hibernate.initialize(user.getRoles());
        this.userJwt = "Bearer " + jwtTokenProvider.generateToken(user);
    }

    @Test
    @Transactional
    void createUserBySuperAdmin_OK_200() throws Exception {
        NewUserRequest request = new NewUserRequest("new_admin", "securePassword123!", "newadmin@example.com");

        mvc.perform(MockMvcRequestBuilders
                        .post("/users/create")
                        .header(HttpHeaders.AUTHORIZATION, superAdminJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void createUserByUser_FORBIDDEN_403() throws Exception {
        NewUserRequest request = new NewUserRequest("regular_user", "UserPass123!", "user@example.com");

        mvc.perform(MockMvcRequestBuilders
                        .post("/users/create")
                        .header(HttpHeaders.AUTHORIZATION, userJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    void accessWithoutToken_NOT_AUTHORIZED_401() throws Exception {
        MvcResult requestResult = mvc.perform(MockMvcRequestBuilders
                        .post("/users/create"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andReturn();

        MockHttpServletResponse response = requestResult.getResponse();
        Assertions.assertNull(response.getErrorMessage());
    }

    @Test
    void accessWithInvalidToken_NOT_AUTHORIZED_401() throws Exception {
        MvcResult requestResult = mvc.perform(MockMvcRequestBuilders
                        .post("/users/create")
                        .header(HttpHeaders.AUTHORIZATION, "INVALID_TOKEN"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andReturn();

        MockHttpServletResponse response = requestResult.getResponse();
        Assertions.assertNull(response.getErrorMessage());
    }

    @Test
    void userCannotDeleteOtherUser_FORBIDDEN_403() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .delete("/users/1")
                        .header(HttpHeaders.AUTHORIZATION, userJwt))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @Transactional
    void superAdminCanDeleteUser_OK_200() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .delete("/users/3")
                        .header(HttpHeaders.AUTHORIZATION, superAdminJwt))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void unauthorizedAccessToProtectedEndpoint_NOT_AUTHORIZED_401() throws Exception {
        MvcResult requestResult = mvc.perform(MockMvcRequestBuilders
                        .get("/users/all"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andReturn();

        MockHttpServletResponse response = requestResult.getResponse();
        Assertions.assertNull(response.getErrorMessage());
    }
}
