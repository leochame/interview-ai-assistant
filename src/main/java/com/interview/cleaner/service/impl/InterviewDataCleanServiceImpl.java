package com.interview.cleaner.service.impl;

import com.interview.cleaner.model.CleanedInterviewData;
import com.interview.cleaner.model.CleanedQuestionData;
import com.interview.cleaner.processor.ContentExtractor;
import com.interview.cleaner.processor.DuplicateRemover;
import com.interview.cleaner.processor.TextNormalizer;
import com.interview.cleaner.service.InterviewDataCleanService;
import com.interview.crawler.model.RawInterviewData;
import com.interview.entity.Company;
import com.interview.entity.InterviewExperience;
import com.interview.entity.InterviewQuestion;
import com.interview.repository.CompanyRepository;
import com.interview.repository.InterviewExperienceRepository;
import com.interview.repository.InterviewQuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 面经数据清洗服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewDataCleanServiceImpl implements InterviewDataCleanService {

    private final TextNormalizer textNormalizer;
    private final DuplicateRemover duplicateRemover;
    private final ContentExtractor contentExtractor;
    
    private final CompanyRepository companyRepository;
    private final InterviewExperienceRepository interviewExperienceRepository;
    private final InterviewQuestionRepository interviewQuestionRepository;

    @Override
    public CleanedInterviewData cleanData(RawInterviewData rawData) {
        if (rawData == null) {
            return null;
        }
        
        try {
            CleanedInterviewData cleanedData = new CleanedInterviewData();
            
            // 标准化公司名称
            cleanedData.setCompany(textNormalizer.normalizeCompanyName(rawData.getCompany()));
            
            // 标准化职位名称
            cleanedData.setPosition(textNormalizer.normalizePosition(rawData.getPosition()));
            
            // 清洗面经内容
            String cleanedContent = textNormalizer.cleanHtml(rawData.getContent());
            cleanedData.setContent(cleanedContent);
            
            // 提取问题列表
            List<CleanedQuestionData> questions = contentExtractor.extractQuestions(cleanedContent);
            cleanedData.setQuestions(questions);
            
            // 提取标签
            List<String> tags = extractTags(cleanedContent);
            if (rawData.getTags() != null && !rawData.getTags().isEmpty()) {
                // 合并原始标签和提取的标签
                Set<String> tagSet = new HashSet<>(tags);
                tagSet.addAll(rawData.getTags());
                tags = new ArrayList<>(tagSet);
            }
            cleanedData.setTags(tags);
            
            // 设置其他基本属性
            cleanedData.setAuthor(rawData.getAuthor());
            cleanedData.setPublishDate(rawData.getPublishDate());
            cleanedData.setSource(rawData.getSource());
            cleanedData.setSourceUrl(rawData.getSourceUrl());
            
            // 元数据
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("originalTitle", rawData.getTitle());
            metadata.put("cleanTime", new Date());
            cleanedData.setMetadata(metadata);
            
            return cleanedData;
        } catch (Exception e) {
            log.error("清洗面经数据出错", e);
            return null;
        }
    }

    @Override
    public List<CleanedInterviewData> cleanBatch(List<RawInterviewData> rawDataList) {
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
    public Long cleanAndSave(RawInterviewData rawData) {
        CleanedInterviewData cleanedData = cleanData(rawData);
        if (cleanedData == null) {
            return null;
        }
        
        try {
            // 检查是否重复
            if (duplicateRemover.isDuplicate(cleanedData.getContent())) {
                log.info("检测到重复面经内容，跳过保存: {}", cleanedData.getSourceUrl());
                return null;
            }
            
            // 1. 查找或创建公司
            Company company = findOrCreateCompany(cleanedData.getCompany());
            
            // 2. 创建面经记录
            InterviewExperience experience = new InterviewExperience();
            experience.setCompanyId(company.getId());
            experience.setPosition(cleanedData.getPosition());
            experience.setContent(cleanedData.getContent());
            experience.setAuthor(cleanedData.getAuthor());
            experience.setPublishDate(cleanedData.getPublishDate());
            experience.setSource(cleanedData.getSource());
            experience.setSourceUrl(cleanedData.getSourceUrl());
            experience.setStatus(1); // 默认有效
            
            // 保存面经
            InterviewExperience savedExperience = interviewExperienceRepository.save(experience);
            
            // 3. 保存提取的问题
            if (cleanedData.getQuestions() != null && !cleanedData.getQuestions().isEmpty()) {
                for (CleanedQuestionData questionData : cleanedData.getQuestions()) {
                    InterviewQuestion question = new InterviewQuestion();
                    question.setExperienceId(savedExperience.getId());
                    question.setContent(questionData.getQuestion());
                    question.setAnswer(questionData.getAnswer());
                    question.setCategory(questionData.getCategory());
                    question.setDifficulty(questionData.getDifficulty());
                    question.setStatus(1); // 默认有效
                    
                    interviewQuestionRepository.save(question);
                }
            }
            
            log.info("成功保存清洗后的面经数据: ID={}, 公司={}, 职位={}", 
                    savedExperience.getId(), cleanedData.getCompany(), cleanedData.getPosition());
            
            return savedExperience.getId();
        } catch (Exception e) {
            log.error("保存清洗后的面经数据出错", e);
            throw new RuntimeException("保存清洗后的面经数据出错", e);
        }
    }

    @Override
    @Transactional
    public List<Long> cleanAndSaveBatch(List<RawInterviewData> rawDataList) {
        if (rawDataList == null || rawDataList.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Long> savedIds = new ArrayList<>();
        for (RawInterviewData rawData : rawDataList) {
            try {
                Long id = cleanAndSave(rawData);
                if (id != null) {
                    savedIds.add(id);
                }
            } catch (Exception e) {
                log.error("批量保存面经数据出错: {}", rawData.getTitle(), e);
                // 继续处理下一条
            }
        }
        
        return savedIds;
    }

    @Override
    public CleanedInterviewData extractQuestionsFromContent(String content) {
        if (StringUtils.isBlank(content)) {
            return null;
        }
        
        CleanedInterviewData cleanedData = new CleanedInterviewData();
        
        // 清洗内容
        String cleanedContent = textNormalizer.cleanHtml(content);
        cleanedData.setContent(cleanedContent);
        
        // 提取问题列表
        List<CleanedQuestionData> questions = contentExtractor.extractQuestions(cleanedContent);
        cleanedData.setQuestions(questions);
        
        // 提取标签
        List<String> tags = extractTags(cleanedContent);
        cleanedData.setTags(tags);
        
        return cleanedData;
    }

    @Override
    public String normalizeContent(String content) {
        return textNormalizer.cleanHtml(content);
    }

    @Override
    public List<String> extractTags(String content) {
        if (StringUtils.isBlank(content)) {
            return new ArrayList<>();
        }
        
        // 提取技能作为标签
        return contentExtractor.extractSkills(content);
    }

    @Override
    @Transactional
    public int cleanCompanyInterviews(String companyName) {
        if (StringUtils.isBlank(companyName)) {
            return 0;
        }
        
        // 标准化公司名称
        String normalizedCompanyName = textNormalizer.normalizeCompanyName(companyName);
        
        // 查找公司
        Optional<Company> companyOpt = companyRepository.findByName(normalizedCompanyName);
        if (!companyOpt.isPresent()) {
            log.warn("未找到公司: {}", normalizedCompanyName);
            return 0;
        }
        
        Company company = companyOpt.get();
        
        // 查找该公司的所有面经
        List<InterviewExperience> experiences = interviewExperienceRepository.findByCompanyId(company.getId());
        if (experiences.isEmpty()) {
            log.info("未找到公司[{}]的面经数据", normalizedCompanyName);
            return 0;
        }
        
        int processedCount = 0;
        
        for (InterviewExperience experience : experiences) {
            try {
                // 清洗内容
                String cleanedContent = textNormalizer.cleanHtml(experience.getContent());
                experience.setContent(cleanedContent);
                
                // 更新面经
                interviewExperienceRepository.save(experience);
                
                // 提取问题
                List<CleanedQuestionData> questions = contentExtractor.extractQuestions(cleanedContent);
                
                // 删除已有问题
                interviewQuestionRepository.deleteByExperienceId(experience.getId());
                
                // 保存新问题
                for (CleanedQuestionData questionData : questions) {
                    InterviewQuestion question = new InterviewQuestion();
                    question.setExperienceId(experience.getId());
                    question.setContent(questionData.getQuestion());
                    question.setAnswer(questionData.getAnswer());
                    question.setCategory(questionData.getCategory());
                    question.setDifficulty(questionData.getDifficulty());
                    question.setStatus(1); // 默认有效
                    
                    interviewQuestionRepository.save(question);
                }
                
                processedCount++;
                log.info("成功清洗面经: ID={}, 公司={}, 职位={}, 提取问题数量={}",
                        experience.getId(), normalizedCompanyName, experience.getPosition(), questions.size());
                
            } catch (Exception e) {
                log.error("清洗面经出错: ID={}", experience.getId(), e);
                // 继续处理下一条
            }
        }
        
        return processedCount;
    }
    
    /**
     * 查找或创建公司
     */
    private Company findOrCreateCompany(String companyName) {
        if (StringUtils.isBlank(companyName)) {
            companyName = "未知公司";
        }

        // 查找公司
        Optional<Company> existingCompany = companyRepository.findByName(companyName);
        
        if (existingCompany.isPresent()) {
            return existingCompany.get();
        }

        // 创建新公司
        Company company = new Company();
        company.setName(companyName);
        return companyRepository.save(company);
    }
}