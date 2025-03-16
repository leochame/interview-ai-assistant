-- 创建数据库
CREATE DATABASE IF NOT EXISTS interview_assistant DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE interview_assistant;

-- 公司表
CREATE TABLE IF NOT EXISTS `company` (
                                         `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                         `name` VARCHAR(100) NOT NULL COMMENT '公司名称',
    `industry` VARCHAR(100) COMMENT '所属行业',
    `size` VARCHAR(50) COMMENT '公司规模',
    `description` TEXT COMMENT '公司描述',
    `website` VARCHAR(255) COMMENT '公司网站',
    `location` VARCHAR(255) COMMENT '公司地址',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公司信息表';

-- 职位描述表
CREATE TABLE IF NOT EXISTS `job_description` (
                                                 `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                                 `company_id` BIGINT NOT NULL COMMENT '公司ID',
                                                 `title` VARCHAR(100) NOT NULL COMMENT '职位标题',
    `department` VARCHAR(100) COMMENT '所属部门',
    `location` VARCHAR(255) COMMENT '工作地点',
    `job_type` VARCHAR(50) COMMENT '工作类型',
    `experience_required` VARCHAR(50) COMMENT '经验要求',
    `education_required` VARCHAR(50) COMMENT '学历要求',
    `salary_range` VARCHAR(50) COMMENT '薪资范围',
    `description` TEXT COMMENT '职位描述',
    `requirements` TEXT COMMENT '职位要求',
    `responsibilities` TEXT COMMENT '职责',
    `benefits` TEXT COMMENT '福利待遇',
    `publish_date` DATE COMMENT '发布日期',
    `source` VARCHAR(50) NOT NULL COMMENT '数据来源',
    `source_url` VARCHAR(255) COMMENT '源URL',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态(0:无效,1:有效)',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_company_id` (`company_id`),
    KEY `idx_title` (`title`),
    KEY `idx_source` (`source`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='职位描述表';

-- 面试经验表
CREATE TABLE IF NOT EXISTS `interview_experience` (
                                                      `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                                      `company_id` BIGINT NOT NULL COMMENT '公司ID',
                                                      `position` VARCHAR(100) NOT NULL COMMENT '面试职位',
    `content` TEXT NOT NULL COMMENT '面经内容',
    `interview_date` DATE COMMENT '面试日期',
    `difficulty` TINYINT COMMENT '难度(1-5)',
    `result` VARCHAR(20) COMMENT '面试结果',
    `publish_date` DATE COMMENT '发布日期',
    `author` VARCHAR(100) COMMENT '作者',
    `source` VARCHAR(50) NOT NULL COMMENT '数据来源',
    `source_url` VARCHAR(255) COMMENT '源URL',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态(0:无效,1:有效)',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_company_id` (`company_id`),
    KEY `idx_position` (`position`),
    KEY `idx_source` (`source`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='面试经验表';

-- 面试问题表
CREATE TABLE IF NOT EXISTS `interview_question` (
                                                    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                                    `experience_id` BIGINT COMMENT '所属面经ID',
                                                    `content` TEXT NOT NULL COMMENT '问题内容',
                                                    `answer` TEXT COMMENT '回答内容',
                                                    `category` VARCHAR(50) COMMENT '问题分类',
    `tags` VARCHAR(255) COMMENT '标签',
    `difficulty` TINYINT COMMENT '难度(1-5)',
    `frequency` INT DEFAULT 1 COMMENT '出现频率',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态(0:无效,1:有效)',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_experience_id` (`experience_id`),
    KEY `idx_category` (`category`),
    KEY `idx_difficulty` (`difficulty`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='面试问题表';

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
                                      `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                      `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `email` VARCHAR(100) COMMENT '邮箱',
    `phone` VARCHAR(20) COMMENT '手机号',
    `real_name` VARCHAR(50) COMMENT '真实姓名',
    `avatar` VARCHAR(255) COMMENT '头像',
    `gender` TINYINT COMMENT '性别(0:女,1:男)',
    `birthday` DATE COMMENT '生日',
    `introduction` TEXT COMMENT '自我介绍',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态(0:禁用,1:正常)',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    UNIQUE KEY `uk_phone` (`phone`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 用户简历表
CREATE TABLE IF NOT EXISTS `user_resume` (
                                             `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                             `user_id` BIGINT NOT NULL COMMENT '用户ID',
                                             `education` TEXT COMMENT '教育经历',
                                             `work_experience` TEXT COMMENT '工作经历',
                                             `skills` TEXT COMMENT '技能',
                                             `projects` TEXT COMMENT '项目经历',
                                             `certifications` TEXT COMMENT '证书',
                                             `languages` TEXT COMMENT '语言能力',
                                             `objective` TEXT COMMENT '求职目标',
                                             `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                             `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                             PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户简历表';

-- 面试模拟记录表
CREATE TABLE IF NOT EXISTS `interview_simulation` (
                                                      `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                                      `user_id` BIGINT NOT NULL COMMENT '用户ID',
                                                      `job_description_id` BIGINT COMMENT '职位ID',
                                                      `simulation_type` VARCHAR(50) NOT NULL COMMENT '模拟类型',
    `duration` INT COMMENT '时长(分钟)',
    `start_time` DATETIME NOT NULL COMMENT '开始时间',
    `end_time` DATETIME COMMENT '结束时间',
    `score` INT COMMENT '得分',
    `feedback` TEXT COMMENT '反馈',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态(0:未完成,1:已完成)',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_job_description_id` (`job_description_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='面试模拟记录表';

-- 模拟面试对话记录表
CREATE TABLE IF NOT EXISTS `simulation_message` (
                                                    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                                    `simulation_id` BIGINT NOT NULL COMMENT '模拟ID',
                                                    `role` VARCHAR(20) NOT NULL COMMENT '角色(system/user/assistant)',
    `content` TEXT NOT NULL COMMENT '内容',
    `sequence` INT NOT NULL COMMENT '顺序',
    `evaluation` TEXT COMMENT '评价',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_simulation_id` (`simulation_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模拟面试对话记录表';

-- 向量存储表 (适用于Defiy)
CREATE TABLE IF NOT EXISTS `vector_storage` (
                                                `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                                `collection` VARCHAR(100) NOT NULL COMMENT '集合名称',
    `content` TEXT NOT NULL COMMENT '文本内容',
    `embedding` LONGTEXT COMMENT '向量嵌入(JSON格式)',
    `metadata` TEXT COMMENT '元数据(JSON格式)',
    `source_id` BIGINT COMMENT '源数据ID',
    `source_type` VARCHAR(50) COMMENT '源数据类型',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_collection` (`collection`),
    KEY `idx_source` (`source_id`, `source_type`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='向量存储表';

-- 添加外键约束
ALTER TABLE `job_description` ADD CONSTRAINT `fk_job_company` FOREIGN KEY (`company_id`) REFERENCES `company` (`id`);
ALTER TABLE `interview_experience` ADD CONSTRAINT `fk_interview_company` FOREIGN KEY (`company_id`) REFERENCES `company` (`id`);
ALTER TABLE `interview_question` ADD CONSTRAINT `fk_question_experience` FOREIGN KEY (`experience_id`) REFERENCES `interview_experience` (`id`);
ALTER TABLE `user_resume` ADD CONSTRAINT `fk_resume_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);
ALTER TABLE `interview_simulation` ADD CONSTRAINT `fk_simulation_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);
ALTER TABLE `interview_simulation` ADD CONSTRAINT `fk_simulation_job` FOREIGN KEY (`job_description_id`) REFERENCES `job_description` (`id`);
ALTER TABLE `simulation_message` ADD CONSTRAINT `fk_message_simulation` FOREIGN KEY (`simulation_id`) REFERENCES `interview_simulation` (`id`);