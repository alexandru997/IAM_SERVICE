package com.post_hub.iam_Service.model.enteties;

import com.post_hub.iam_Service.service.model.IamServiceUserRole;
import com.post_hub.iam_Service.utils.enum_converter.UserRoleTypeConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(name = "user_system_role", nullable = false, updatable = false)
    @Convert(converter = UserRoleTypeConverter.class)
    private IamServiceUserRole userSystemRole;

    @Column()
    private boolean active;

    @Column(name = "created_by")
    private String createdBy;

    @ManyToMany(fetch =  FetchType.LAZY, mappedBy = "roles", cascade = CascadeType.MERGE)
    private Set<User> users = new HashSet<>();
}
