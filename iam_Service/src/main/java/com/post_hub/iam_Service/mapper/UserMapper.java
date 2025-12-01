package com.post_hub.iam_Service.mapper;


import com.post_hub.iam_Service.model.dto.role.RoleDTO;
import com.post_hub.iam_Service.model.dto.user.UserDTO;
import com.post_hub.iam_Service.model.dto.user.UserProfileDTO;
import com.post_hub.iam_Service.model.dto.user.UserSearchDTO;
import com.post_hub.iam_Service.model.enteties.Role;
import com.post_hub.iam_Service.model.enteties.User;
import com.post_hub.iam_Service.model.enums.RegistrationStatus;
import com.post_hub.iam_Service.model.request.user.NewUserRequest;
import com.post_hub.iam_Service.model.request.user.UpdateUserRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.Collection;
import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        imports = {RegistrationStatus.class, Object.class}
)
public interface UserMapper {
    @Mapping(target="roles", expression ="java(mapRoles(user.getRoles()))")
    UserDTO toDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "registrationStatus", expression = "java(RegistrationStatus.ACTIVE)")
    User createUser(NewUserRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "registrationStatus", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "posts", ignore = true)
    void updateUser(@MappingTarget User user, UpdateUserRequest request);

    @Mapping(source = "deleted", target ="isDeleted")
    @Mapping(target = "roles", expression ="java(mapRoles(user.getRoles()))")
    UserSearchDTO toUserSearchDTO(User user);

    @Mapping(target = "roles", expression = "java(mapRoles(user.getRoles()))")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "token", source = "token")
    @Mapping(target ="refreshToken", source = "refreshToken")
    UserProfileDTO toUserProfileDto(User user, String token, String refreshToken);

    default List<RoleDTO> mapRoles(Collection<Role> roles){
        return roles.stream()
                .map(role -> new RoleDTO(role.getId(), role.getName()))
                .toList();
    }
}
