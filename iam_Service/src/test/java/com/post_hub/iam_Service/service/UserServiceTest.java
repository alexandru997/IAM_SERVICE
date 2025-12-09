package com.post_hub.iam_Service.service;

import com.post_hub.iam_Service.mapper.UserMapper;
import com.post_hub.iam_Service.model.dto.user.UserDTO;
import com.post_hub.iam_Service.model.enteties.Role;
import com.post_hub.iam_Service.model.enteties.User;
import com.post_hub.iam_Service.model.exeption.DataExistException;
import com.post_hub.iam_Service.model.exeption.NotFoundException;
import com.post_hub.iam_Service.model.request.user.NewUserRequest;
import com.post_hub.iam_Service.model.request.user.UpdateUserRequest;
import com.post_hub.iam_Service.model.response.IamResponse;
import com.post_hub.iam_Service.repositories.RoleRepository;
import com.post_hub.iam_Service.repositories.UserRepository;
import com.post_hub.iam_Service.security.validation.AccessValidator;
import com.post_hub.iam_Service.service.impl.UserServiceImpl;
import com.post_hub.iam_Service.service.model.IamServiceUserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private AccessValidator accessValidator;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserDTO testUserDTO;
    private Role userRole;
    private Role superAdminRole;

    @BeforeEach
    void setUp() {
        userRole = new Role();
        userRole.setName(IamServiceUserRole.USER.getRole());

        superAdminRole = new Role();
        superAdminRole.setName(IamServiceUserRole.SUPER_ADMIN.getRole());

        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("TestUser");
        testUser.setEmail("testuser@gmail.com");
        testUser.setPassword("encodedPassword");
        testUser.setRoles(Set.of(userRole));

        testUserDTO = new UserDTO();
        testUserDTO.setId(1);
        testUserDTO.setUsername("TestUser");
        testUserDTO.setEmail("testuser@gmail.com");
    }

    @Test
    void getById_WhenUserExists_ReturnsUserDTO() {
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userMapper.toDto(testUser)).thenReturn(testUserDTO);

        IamResponse<UserDTO> response = userService.getById(1);

        assertNotNull(response);
        assertNotNull(response.getPayload());
        assertEquals("TestUser", response.getPayload().getUsername());
        assertEquals("testuser@gmail.com", response.getPayload().getEmail());
        verify(userRepository).findById(1);
        verify(userMapper).toDto(testUser);
    }

    @Test
    void getById_WhenUserNotFound_ThrowsNotFoundException() {
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getById(999));
        verify(userRepository).findById(999);
    }

    @Test
    void createUser_WhenValidRequest_ReturnsCreatedUser() {
        NewUserRequest request = new NewUserRequest();
        request.setUsername("NewUser");
        request.setEmail("newuser@gmail.com");
        request.setPassword("password123");

        User newUser = new User();
        newUser.setUsername("NewUser");
        newUser.setEmail("newuser@gmail.com");

        User savedUser = new User();
        savedUser.setId(2);
        savedUser.setUsername("NewUser");
        savedUser.setEmail("newuser@gmail.com");

        UserDTO savedUserDTO = new UserDTO();
        savedUserDTO.setId(2);
        savedUserDTO.setUsername("NewUser");
        savedUserDTO.setEmail("newuser@gmail.com");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(roleRepository.findByName(IamServiceUserRole.USER.getRole())).thenReturn(Optional.of(userRole));
        when(userMapper.createUser(request)).thenReturn(newUser);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toDto(savedUser)).thenReturn(savedUserDTO);

        IamResponse<UserDTO> response = userService.createUser(request);

        assertNotNull(response);
        assertNotNull(response.getPayload());
        assertEquals("NewUser", response.getPayload().getUsername());
        assertEquals("newuser@gmail.com", response.getPayload().getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_WhenEmailExists_ThrowsDataExistException() {
        NewUserRequest request = new NewUserRequest();
        request.setUsername("NewUser");
        request.setEmail("existing@gmail.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(DataExistException.class, () -> userService.createUser(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_WhenUsernameExists_ThrowsDataExistException() {
        NewUserRequest request = new NewUserRequest();
        request.setUsername("ExistingUser");
        request.setEmail("newuser@gmail.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(true);

        assertThrows(DataExistException.class, () -> userService.createUser(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_WhenValidRequest_ReturnsUpdatedUser() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setUsername("UpdatedUser");
        request.setEmail("testuser@gmail.com");

        User updatedUser = new User();
        updatedUser.setId(1);
        updatedUser.setUsername("UpdatedUser");
        updatedUser.setEmail("testuser@gmail.com");

        UserDTO updatedUserDTO = new UserDTO();
        updatedUserDTO.setId(1);
        updatedUserDTO.setUsername("UpdatedUser");
        updatedUserDTO.setEmail("testuser@gmail.com");

        when(userRepository.findByIdAndDeletedFalse(1)).thenReturn(Optional.of(testUser));
        doNothing().when(accessValidator).validateAdminOrOwnerAccess(1);
        when(userRepository.existsByUsername("UpdatedUser")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toDto(updatedUser)).thenReturn(updatedUserDTO);

        IamResponse<UserDTO> response = userService.updateUser(1, request);

        assertNotNull(response);
        assertNotNull(response.getPayload());
        assertEquals("UpdatedUser", response.getPayload().getUsername());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_WhenUserNotFound_ThrowsNotFoundException() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setUsername("UpdatedUser");
        request.setEmail("updated@gmail.com");

        when(userRepository.findByIdAndDeletedFalse(999)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.updateUser(999, request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void softDeleteUser_WhenUserExists_DeletesUser() {
        when(userRepository.findByIdAndDeletedFalse(1)).thenReturn(Optional.of(testUser));
        doNothing().when(accessValidator).validateAdminOrOwnerAccess(1);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.softDeleteUser(1);

        assertTrue(testUser.getDeleted());
        verify(userRepository).save(testUser);
    }

    @Test
    void softDeleteUser_WhenUserNotFound_ThrowsNotFoundException() {
        when(userRepository.findByIdAndDeletedFalse(999)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.softDeleteUser(999));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_AsSuperAdmin_CreatesUserSuccessfully() {
        NewUserRequest request = new NewUserRequest("NewUser", "password123!", "newuser@gmail.com");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(roleRepository.findByName(IamServiceUserRole.USER.getRole())).thenReturn(Optional.of(superAdminRole));

        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setEmail(request.getEmail());
        newUser.setPassword("encodedPassword");
        newUser.setRoles(Collections.singleton(superAdminRole));

        when(userMapper.createUser(request)).thenReturn(newUser);

        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(userMapper.toDto(newUser)).thenReturn(testUserDTO);

        UserDTO result = userService.createUser(request).getPayload();

        assertNotNull(result);
        assertEquals(testUserDTO.getId(), result.getId());
        assertEquals(testUserDTO.getUsername(), result.getUsername());

        verify(userRepository, times(1)).existsByEmail(request.getEmail());
        verify(userRepository, times(1)).existsByUsername(request.getUsername());
        verify(userRepository, times(1)).save(any(User.class));
        verify(userMapper, times(1)).toDto(newUser);
    }

    @Test
    void createUser_EmailAlreadyExists_ThrowsException() {
        NewUserRequest request = new NewUserRequest("NewUser", "password123!", "newuser@gmail.com");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(DataExistException.class)
                .hasMessageContaining("already exists");

        verify(userRepository, times(1)).existsByEmail(request.getEmail());
        verify(userRepository, never()).existsByUsername(anyString());
        verify(userRepository, never()).save(any(User.class));
    }
}
