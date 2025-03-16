
package com.interview.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@Entity
@Table(name = "user")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "username", nullable = false, unique = true)
    private String username;
    
    @Column(name = "password", nullable = false)
    private String password;
    
    @Column(name = "email", unique = true)
    private String email;
    
    @Column(name = "phone", unique = true)
    private String phone;
    
    @Column(name = "real_name")
    private String realName;
    
    @Column(name = "avatar")
    private String avatar;
    
    @Column(name = "gender")
    private Integer gender;
    
    @Column(name = "birthday")
    private LocalDate birthday;
    
    @Column(name = "introduction", columnDefinition = "TEXT")
    private String introduction;
    
    @Column(name = "status", nullable = false)
    private Integer status;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}