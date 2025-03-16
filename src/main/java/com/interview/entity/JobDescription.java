
package com.interview.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 职位描述实体类
 */
@Data
@Entity
@Table(name = "job_description")
public class JobDescription {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "company_id", nullable = false)
    private Long companyId;
    
    @Column(name = "title", nullable = false)
    private String title;
    
    @Column(name = "department")
    private String department;
    
    @Column(name = "location")
    private String location;
    
    @Column(name = "job_type")
    private String jobType;
    
    @Column(name = "experience_required")
    private String experienceRequired;
    
    @Column(name = "education_required")
    private String educationRequired;
    
    @Column(name = "salary_range")
    private String salaryRange;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "requirements", columnDefinition = "TEXT")
    private String requirements;
    
    @Column(name = "responsibilities", columnDefinition = "TEXT")
    private String responsibilities;
    
    @Column(name = "benefits", columnDefinition = "TEXT")
    private String benefits;
    
    @Column(name = "publish_date")
    private LocalDate publishDate;
    
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

