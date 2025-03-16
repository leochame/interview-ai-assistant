package com.interview.crawler.pipeline;

import com.interview.crawler.model.RawInterviewData;
import com.interview.entity.Company;
import com.interview.entity.InterviewExperience;
import com.interview.repository.CompanyRepository;
import com.interview.repository.InterviewExperienceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 牛客网面经处理管道
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NowcoderPipeline implements Pipeline {

    private final CompanyRepository companyRepository;
    private final InterviewExperienceRepository interviewExperienceRepository;

    @Override
    public void process(ResultItems resultItems, Task task) {
        RawInterviewData rawData = resultItems.get("interviewData");
        if (rawData == null) {
            return;
        }

        try {
            // 1. 查找或创建公司
            Company company = findOrCreateCompany(rawData.getCompany());

            // 2. 查找是否已存在相同URL的面经
            Optional<InterviewExperience> existingExperience = 
                interviewExperienceRepository.findBySourceUrl(rawData.getSourceUrl());
            
            if (existingExperience.isPresent()) {
                log.info("面经已存在，跳过: {}", rawData.getSourceUrl());
                return;
            }

            // 3. 创建新的面经记录
            InterviewExperience experience = new InterviewExperience();
            experience.setCompanyId(company.getId());
            experience.setPosition(rawData.getPosition());
            experience.setContent(rawData.getContent());
            if (rawData.getPublishDate() != null) {
                experience.setPublishDate(rawData.getPublishDate());
            }
            experience.setAuthor(rawData.getAuthor());
            experience.setSource(rawData.getSource());
            experience.setSourceUrl(rawData.getSourceUrl());
            experience.setStatus(1); // 默认有效

            // 4. 保存面经
            interviewExperienceRepository.save(experience);
            log.info("成功保存面经: {}", rawData.getTitle());

        } catch (Exception e) {
            log.error("处理面经数据出错", e);
        }
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
