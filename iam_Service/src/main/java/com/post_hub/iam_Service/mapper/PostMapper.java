package com.post_hub.iam_Service.mapper;

import com.post_hub.iam_Service.model.dto.post.PostDTO;
import com.post_hub.iam_Service.model.dto.post.PostSearchDTO;
import com.post_hub.iam_Service.model.enteties.Post;
import com.post_hub.iam_Service.model.enteties.User;
import com.post_hub.iam_Service.model.request.post.PostRequest;
import com.post_hub.iam_Service.model.request.post.UpdatePostRequest;
import org.hibernate.type.descriptor.DateTimeUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.Objects;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        imports = {DateTimeUtils .class, Objects .class}
)
public interface PostMapper {

    PostDTO toPostDTO(Post post);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target ="user", source="user")
    @Mapping(target="createdBy", source="user.username")
    Post createPost(PostRequest postRequest, User user);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "created", ignore = true)
    void updatePost(@MappingTarget Post post, UpdatePostRequest postRequest);

    @Mapping(source = "deleted", target = "isDeleted")
    @Mapping(source = "user.username", target = "createdBy")
    PostSearchDTO toPostSearchDTO(Post post);
}
