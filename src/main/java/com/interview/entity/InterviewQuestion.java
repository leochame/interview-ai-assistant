package com.interview.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 面试问题实体类
 */
@Data
@Entity
@Table(name = "interview_question")
public class InterviewQuestion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "experience_id")
    private Long experienceId;
    
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "answer", columnDefinition = "TEXT")
    private String answer;
    
    @Column(name = "category")
    private String category;
    
    @Column(name = "tags")
    private String tags;
    
    @Column(name = "difficulty")
    private Integer difficulty;
    
    @Column(name = "frequency")
    private Integer frequency;
    
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
