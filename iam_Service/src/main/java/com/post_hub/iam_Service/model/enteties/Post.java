package com.post_hub.iam_Service.model.enteties;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
@Getter
@Setter
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    @Column(name = "created", nullable = false, updatable = false)
    private LocalDateTime create = LocalDateTime.now();

    @Column(nullable = false, columnDefinition = "integer default 0")
    private String content;
}
