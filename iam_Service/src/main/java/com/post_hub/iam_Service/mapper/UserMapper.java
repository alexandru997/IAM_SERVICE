package com.post_hub.iam_Service.mapper;


import com.post_hub.iam_Service.model.dto.user.UserDTO;
import com.post_hub.iam_Service.model.enteties.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserMapper {
    @Mapping(source="last_login", target="lastLogin")
    UserDTO toDto(User user);
}
