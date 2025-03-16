
package com.interview.crawler.pipeline;

import com.interview.crawler.model.RawJobData;
import com.interview.entity.Company;
import com.interview.entity.JobDescription;
import com.interview.repository.CompanyRepository;
import com.interview.repository.JobDescriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Boss直聘职位处理管道
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BossZhipinPipeline implements Pipeline {

    private final CompanyRepository companyRepository;
    private final JobDescriptionRepository jobDescriptionRepository;

    @Override
    public void process(ResultItems resultItems, Task task) {
        RawJobData rawData = resultItems.get("jobData");
        if (rawData == null) {
            return;
        }

        try {
            // 1. 查找或创建公司
            Company company = findOrCreateCompany(rawData);

            // 2. 查找是否已存在相同URL的职位
            Optional<JobDescription> existingJob = 
                jobDescriptionRepository.findBySourceUrl(rawData.getSourceUrl());
            
            if (existingJob.isPresent()) {
                log.info("职位已存在，跳过: {}", rawData.getSourceUrl());
                return;
            }

            // 3. 创建新的职位记录
            JobDescription job = new JobDescription();
            job.setCompanyId(company.getId());
            job.setTitle(rawData.getTitle());
            job.setLocation(rawData.getLocation());
            job.setExperienceRequired(rawData.getExperienceRequired());
            job.setEducationRequired(rawData.getEducationRequired());
            job.setSalaryRange(rawData.getSalaryRange());
            job.setDescription(rawData.getDescription());
            
            // 构建职位要求
            StringBuilder requirements = new StringBuilder();
            if (StringUtils.isNotBlank(rawData.getExperienceRequired())) {
                requirements.append("经验要求: ").append(rawData.getExperienceRequired()).append("\n");
            }
            if (StringUtils.isNotBlank(rawData.getEducationRequired())) {
                requirements.append("学历要求: ").append(rawData.getEducationRequired()).append("\n");
            }
            if (rawData.getTags() != null && !rawData.getTags().isEmpty()) {
                requirements.append("技能要求: ").append(String.join(", ", rawData.getTags())).append("\n");
            }
            job.setRequirements(requirements.toString());
            
            // 设置发布日期
            job.setPublishDate(rawData.getPublishDate() != null ? rawData.getPublishDate() : LocalDate.now());
            
            // 设置来源信息
            job.setSource(rawData.getSource());
            job.setSourceUrl(rawData.getSourceUrl());
            job.setStatus(1); // 默认有效

            // 4. 保存职位
            jobDescriptionRepository.save(job);
            log.info("成功保存职位: {}", rawData.getTitle());

        } catch (Exception e) {
            log.error("处理职位数据出错", e);
        }
    }

    /**
     * 根据职位数据查找或创建公司
     */
    private Company findOrCreateCompany(RawJobData rawData) {
        String companyName = rawData.getCompanyName();
        if (StringUtils.isBlank(companyName)) {
            companyName = "未知公司";
        }

        // 查找公司
        Optional<Company> existingCompany = companyRepository.findByName(companyName);
        
        if (existingCompany.isPresent()) {
            Company company = existingCompany.get();
            
            // 如果有新的信息，更新公司信息
            boolean updated = false;
            
            if (StringUtils.isBlank(company.getIndustry()) && StringUtils.isNotBlank(rawData.getIndustry())) {
                company.setIndustry(rawData.getIndustry());
                updated = true;
            }
            
            if (StringUtils.isBlank(company.getSize()) && StringUtils.isNotBlank(rawData.getCompanySize())) {
                company.setSize(rawData.getCompanySize());
                updated = true;
            }
            
            if (StringUtils.isBlank(company.getDescription()) && StringUtils.isNotBlank(rawData.getCompanyDescription())) {
                company.setDescription(rawData.getCompanyDescription());
                updated = true;
            }
            
            if (updated) {
                companyRepository.save(company);
            }
            
            return company;
        }

        // 创建新公司
        Company company = new Company();
        company.setName(companyName);
        company.setIndustry(rawData.getIndustry());
        company.setSize(rawData.getCompanySize());
        company.setDescription(rawData.getCompanyDescription());
        return companyRepository.save(company);
    }
}