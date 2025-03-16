package com.interview.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 面试经验实体类
 */
@Data
@Entity
@Table(name = "interview_experience")
public class InterviewExperience {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "company_id", nullable = false)
    private Long companyId;
    
    @Column(name = "position", nullable = false)
    private String position;
    
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "interview_date")
    private LocalDate interviewDate;
    
    @Column(name = "difficulty")
    private Integer difficulty;
    
    @Column(name = "result")
    private String result;
    
    @Column(name = "publish_date")
    private LocalDate publishDate;
    
    @Column(name = "author")
    private String author;
    
    @Column(name = "source", nullable = false)
    private String source;
    
    @Column(name = "source_url")
    private String sourceUrl;
    
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
