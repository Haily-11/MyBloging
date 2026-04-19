package com.blog.mylog.entity;


import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "project")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;
    private String url;
    private String techStack; // 逗号分隔，如 "Java,Spring Boot"
    private String status;    // 如 "进行中", "已完成"

    @Column(name = "create_time")
    private LocalDateTime createTime;
}