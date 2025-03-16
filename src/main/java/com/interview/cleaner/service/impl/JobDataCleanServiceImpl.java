
package com.interview.cleaner.service.impl;

import com.interview.cleaner.model.CleanedJobData;
import com.interview.cleaner.processor.ContentExtractor;
import com.interview.cleaner.processor.DuplicateRemover;
import com.interview.cleaner.processor.TextNormalizer;
import com.interview.cleaner.service.JobDataCleanService;
import com.interview.crawler.model.RawJobData;
import com.interview.entity.Company;
import com.interview.entity.JobDescription;
import com.interview.repository.CompanyRepository;
import com.interview.repository.JobDescriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 职位数据清洗服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobDataCleanServiceImpl implements JobDataCleanService {

    private final TextNormalizer textNormalizer;
    private final DuplicateRemover duplicateRemover;
    private final ContentExtractor contentExtractor;
    
    private final CompanyRepository companyRepository;
    private final JobDescriptionRepository jobDescriptionRepository;
    
    // 薪资范围正则表达式
    private static final Pattern SALARY_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)[Kk]?-(\\d+(?:\\.\\d+)?)[Kk]?");

    @Override
    public CleanedJobData cleanData(RawJobData rawData) {
        if (rawData == null) {
            return null;
        }
        
        try {
            CleanedJobData cleanedData = new CleanedJobData();
            
            // 标准化公司名称
            cleanedData.setCompanyName(textNormalizer.normalizeCompanyName(rawData.getCompanyName()));
            
            // 标准化职位标题
            cleanedData.setTitle(normalizeJobTitle(rawData.getTitle()));
            
            // 标准化薪资范围
            String normalizedSalary = rawData.getSalaryRange();
            Integer salaryMin = null;
            Integer salaryMax = null;
            
            if (StringUtils.isNotBlank(rawData.getSalaryRange())) {
                Matcher matcher = SALARY_PATTERN.matcher(rawData.getSalaryRange());
                if (matcher.find()) {
                    try {
                        salaryMin = (int) Math.round(Double.parseDouble(matcher.group(1)));
                        salaryMax = (int) Math.round(Double.parseDouble(matcher.group(2)));
                        normalizedSalary = salaryMin + "K-" + salaryMax + "K";
                    } catch (NumberFormatException e) {
                        log.warn("解析薪资范围出错: {}", rawData.getSalaryRange());
                    }
                }
            }
            
            cleanedData.setSalaryRange(normalizedSalary);
            cleanedData.setSalaryMin(salaryMin);
            cleanedData.setSalaryMax(salaryMax);
            
            // 标准化工作地点
            cleanedData.setLocation(normalizeLocation(rawData.getLocation()));
            
            // 清洗职位描述
            String cleanedDescription = textNormalizer.cleanHtml(rawData.getDescription());
            cleanedData.setDescription(cleanedDescription);
            
            // 标准化公司行业
            cleanedData.setIndustry(rawData.getIndustry());
            
            // 标准化公司规模
            cleanedData.setCompanySize(rawData.getCompanySize());
            
            // 标准化经验要求
            cleanedData.setExperienceRequired(rawData.getExperienceRequired());
            
            // 标准化学历要求
            cleanedData.setEducationRequired(rawData.getEducationRequired());
            
            // 清洗职位要求
            if (StringUtils.isNotBlank(rawData.getDescription())) {
                // 提取技能要求
                List<String> skills = extractSkills(cleanedDescription);
                cleanedData.setSkills(skills);
                
                // 提取工作职责
                List<String> responsibilities = extractResponsibilities(cleanedDescription);
                cleanedData.setResponsibilities(responsibilities);
                
                // 构建职位要求文本
                StringBuilder requirements = new StringBuilder();
                if (StringUtils.isNotBlank(rawData.getExperienceRequired())) {
                    requirements.append("经验要求: ").append(rawData.getExperienceRequired()).append("\n");
                }
                if (StringUtils.isNotBlank(rawData.getEducationRequired())) {
                    requirements.append("学历要求: ").append(rawData.getEducationRequired()).append("\n");
                }
                if (!skills.isEmpty()) {
                    requirements.append("技能要求: ").append(String.join(", ", skills)).append("\n");
                }
                
                cleanedData.setRequirements(requirements.toString());
            }
            
            // 标签列表
            List<String> tags = new ArrayList<>();
            if (rawData.getTags() != null) {
                tags.addAll(rawData.getTags());
            }
            // 将技能也作为标签
            if (cleanedData.getSkills() != null) {
                tags.addAll(cleanedData.getSkills());
            }
            // 去重
            cleanedData.setTags(tags.stream().distinct().collect(Collectors.toList()));
            
            // 设置其他基本属性
            cleanedData.setPublishDate(rawData.getPublishDate() != null ? rawData.getPublishDate() : LocalDate.now());
            cleanedData.setSource(rawData.getSource());
            cleanedData.setSourceUrl(rawData.getSourceUrl());
            
            // 元数据
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("recruiterName", rawData.getRecruiterName());
            metadata.put("recruiterTitle", rawData.getRecruiterTitle());
            metadata.put("businessDirection", rawData.getBusinessDirection());
            metadata.put("cleanTime", new Date());
            cleanedData.setMetadata(metadata);
            
            return cleanedData;
        } catch (Exception e) {
            log.error("清洗职位数据出错", e);
            return null;
        }
    }

    @Override
    public List<CleanedJobData> cleanBatch(List<RawJobData> rawDataList) {
        if (rawDataList == null || rawDataList.isEmpty()) {
            return new ArrayList<>();
        }
        
        return rawDataList.stream()
                .map(this::cleanData)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Long cleanAndSave(RawJobData rawData) {
        CleanedJobData cleanedData = cleanData(rawData);
        if (cleanedData == null) {
            return null;
        }
        
        try {
            // 检查是否重复
            if (duplicateRemover.isDuplicate(cleanedData.getDescription())) {
                log.info("检测到重复职位描述，跳过保存: {}", cleanedData.getSourceUrl());
                return null;
            }
            
            // 1. 查找或创建公司
            Company company = findOrCreateCompany(cleanedData);
            
            // 2. 创建职位记录
            JobDescription job = new JobDescription();
            job.setCompanyId(company.getId());
            job.setTitle(cleanedData.getTitle());
            job.setLocation(cleanedData.getLocation());
            job.setExperienceRequired(cleanedData.getExperienceRequired());
            job.setEducationRequired(cleanedData.getEducationRequired());
            job.setSalaryRange(cleanedData.getSalaryRange());
            job.setDescription(cleanedData.getDescription());
            job.setRequirements(cleanedData.getRequirements());
            job.setPublishDate(cleanedData.getPublishDate());
            job.setSource(cleanedData.getSource());
            job.setSourceUrl(cleanedData.getSourceUrl());
            job.setStatus(1); // 默认有效
            
            // 保存职位
            JobDescription savedJob = jobDescriptionRepository.save(job);
            
            log.info("成功保存清洗后的职位数据: ID={}, 公司={}, 职位={}",
                    savedJob.getId(), cleanedData.getCompanyName(), cleanedData.getTitle());
            
            return savedJob.getId();
        } catch (Exception e) {
            log.error("保存清洗后的职位数据出错", e);
            throw new RuntimeException("保存清洗后的职位数据出错", e);
        }
    }

    @Override
    @Transactional
    public List<Long> cleanAndSaveBatch(List<RawJobData> rawDataList) {
        if (rawDataList == null || rawDataList.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Long> savedIds = new ArrayList<>();
        for (RawJobData rawData : rawDataList) {
            try {
                Long id = cleanAndSave(rawData);
                if (id != null) {
                    savedIds.add(id);
                }
            } catch (Exception e) {
                log.error("批量保存职位数据出错: {}", rawData.getTitle(), e);
                // 继续处理下一条
            }
        }
        
        return savedIds;
    }

    @Override
    public List<String> extractSkills(String description) {
        if (StringUtils.isBlank(description)) {
            return new ArrayList<>();
        }
        
        return contentExtractor.extractSkills(description);
    }

    @Override
    public List<String> extractResponsibilities(String description) {
        if (StringUtils.isBlank(description)) {
            return new ArrayList<>();
        }
        
        return contentExtractor.extractResponsibilities(description);
    }

    @Override
    public CleanedJobData.SalaryRange normalizeSalaryRange(String salaryRange) {
        if (StringUtils.isBlank(salaryRange)) {
            return null;
        }
        
        CleanedJobData.SalaryRange result = new CleanedJobData.SalaryRange();
        
        Matcher matcher = SALARY_PATTERN.matcher(salaryRange);
        if (matcher.find()) {
            try {
                int min = (int) Math.round(Double.parseDouble(matcher.group(1)));
                int max = (int) Math.round(Double.parseDouble(matcher.group(2)));
                result.setMin(min);
                result.setMax(max);
                result.setDisplay(min + "K-" + max + "K");
            } catch (NumberFormatException e) {
                log.warn("解析薪资范围出错: {}", salaryRange);
            }
        } else {
            result.setDisplay(salaryRange);
        }
        
        return result;
    }

    @Override
    public String normalizeLocation(String location) {
        if (StringUtils.isBlank(location)) {
            return "";
        }
        
        // 移除不必要的前缀和后缀
        String normalized = location.replaceAll("(?i)(地点|工作地点|办公地点)：?", "");
        
        // 提取主要城市名称
        Pattern cityPattern = Pattern.compile("(北京|上海|广州|深圳|杭州|南京|成都|武汉|西安|苏州|天津|重庆|长沙|郑州|青岛|宁波|东莞|无锡|厦门|福州|大连|合肥|济南|哈尔滨|长春|沈阳|石家庄|南宁|贵阳|昆明|太原|南昌|兰州|海口|西宁|银川|呼和浩特|拉萨|乌鲁木齐)");
        Matcher matcher = cityPattern.matcher(normalized);
        if (matcher.find()) {
            normalized = matcher.group(1);
        }
        
        return normalized.trim();
    }

    @Override
    public String normalizeJobTitle(String title) {
        if (StringUtils.isBlank(title)) {
            return "";
        }
        
        // 移除括号及其内容
        String normalized = title.replaceAll("\\(.*?\\)|（.*?）", "");
        
        // 标准化职位级别
        normalized = normalized.replaceAll("(?i)初级|junior", "初级");
        normalized = normalized.replaceAll("(?i)中级|middle", "中级");
        normalized = normalized.replaceAll("(?i)高级|senior", "高级");
        normalized = normalized.replaceAll("(?i)专家|expert", "专家");
        normalized = normalized.replaceAll("(?i)资深", "高级");
        
        // 标准化职位名称
        normalized = normalized.replaceAll("(?i)java工程师|java开发工程师|java后端工程师|java后端开发", "Java工程师");
        normalized = normalized.replaceAll("(?i)python工程师|python开发工程师|python后端工程师|python后端开发", "Python工程师");
        normalized = normalized.replaceAll("(?i)golang工程师|go工程师|go开发工程师|go后端开发", "Go工程师");
        normalized = normalized.replaceAll("(?i)前端工程师|web前端|前端开发|h5前端", "前端工程师");
        normalized = normalized.replaceAll("(?i)后端工程师|后端开发工程师|服务端工程师", "后端工程师");
        normalized = normalized.replaceAll("(?i)算法工程师|机器学习工程师|人工智能工程师|AI工程师", "算法工程师");
        normalized = normalized.replaceAll("(?i)测试工程师|软件测试|测试开发工程师|自动化测试", "测试工程师");
        normalized = normalized.replaceAll("(?i)数据工程师|数据开发工程师|大数据工程师", "数据工程师");
        normalized = normalized.replaceAll("(?i)全栈工程师|全栈开发", "全栈工程师");
        normalized = normalized.replaceAll("(?i)架构师|技术架构师|软件架构师", "架构师");
        normalized = normalized.replaceAll("(?i)开发工程师|研发工程师|软件工程师|软件开发", "软件工程师");
        
        // 移除前后空白
        normalized = normalized.trim();
        
        return normalized;
    }
}