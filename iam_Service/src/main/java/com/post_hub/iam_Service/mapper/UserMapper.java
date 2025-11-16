package com.post_hub.iam_Service.mapper;


import com.post_hub.iam_Service.model.dto.user.UserDTO;
import com.post_hub.iam_Service.model.enteties.User;
import com.post_hub.iam_Service.model.enums.RegistrationStatus;
import com.post_hub.iam_Service.model.request.user.NewUserRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        imports = {RegistrationStatus.class, Object.class}
)
public interface UserMapper {
    @Mapping(source = "last_login", target = "lastLogin")
    UserDTO toDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "registrationStatus", expression = "java(RegistrationStatus.ACTIVE)")
    User createUser(NewUserRequest newUserRequest);
}
