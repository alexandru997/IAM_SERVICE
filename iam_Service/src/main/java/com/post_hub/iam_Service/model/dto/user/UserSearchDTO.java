package com.post_hub.iam_Service.model.dto.user;

import com.post_hub.iam_Service.model.dto.role.RoleDTO;
import com.post_hub.iam_Service.model.enums.RegistrationStatus;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserSearchDTO implements Serializable {

    private Integer id;
    private String username;
    private String email;
    private LocalDateTime created;
    private Boolean isDeleted;

    private RegistrationStatus registrationStatus;
    private List<RoleDTO> roles;

}