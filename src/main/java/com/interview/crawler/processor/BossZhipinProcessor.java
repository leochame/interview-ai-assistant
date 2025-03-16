package com.interview.crawler.processor;

import com.interview.common.constants.CrawlerConstants;
import com.interview.crawler.model.RawJobData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Html;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Boss直聘职位爬虫处理器
 */
@Slf4j
@Component
public class BossZhipinProcessor implements PageProcessor {

    private final Site site;

    @Value("${crawler.boss.list-url-pattern}")
    private String listUrlPattern;

    @Value("${crawler.boss.detail-url-pattern}")
    private String detailUrlPattern;

    // 薪资范围提取正则
    private static final Pattern SALARY_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)[Kk]?-(\\d+(?:\\.\\d+)?)[Kk]?");
    // 经验和学历提取正则
    private static final Pattern EXPERIENCE_EDUCATION_PATTERN = Pattern.compile("(\\d+-\\d+年|\\d+年以上|经验不限|应届生)\\s*[·]\\s*(.*)");

    public BossZhipinProcessor(@Value("${crawler.user-agent:}") String userAgent,
                            @Value("${crawler.domain.boss:www.zhipin.com}") String domain,
                            @Value("${crawler.sleep-time:1000}") int sleepTime,
                            @Value("${crawler.retry-times:3}") int retryTimes) {
        this.site = Site.me()
                .setDomain(domain)
                .setSleepTime(sleepTime)
                .setRetryTimes(retryTimes)
                .setTimeOut(10000)
                .setUserAgent(StringUtils.isNotBlank(userAgent) ? userAgent :
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                .addHeader("Accept-Language", "zh-CN,zh;q=0.8,en;q=0.6");
    }

    @Override
    public void process(Page page) {
        String url = page.getUrl().toString();
        
        try {
            // 列表页处理
            if (url.matches(listUrlPattern)) {
                processListPage(page);
            } 
            // 详情页处理
            else if (url.matches(detailUrlPattern)) {
                processDetailPage(page);
            } 
            // 其他页面跳过
            else {
                page.setSkip(true);
            }
        } catch (Exception e) {
            log.error("处理页面出错: {}", url, e);
            page.setSkip(true);
        }
    }

    /**
     * 处理列表页
     */
    private void processListPage(Page page) {
        Html html = page.getHtml();
        
        // 提取所有职位详情页URL
        List<String> detailUrls = html.links().regex(detailUrlPattern).all();
        page.addTargetRequests(detailUrls);
        
        // 提取下一页URL
        String nextPageUrl = html.xpath("//a[@class='next']/@href").toString();
        if (StringUtils.isNotBlank(nextPageUrl)) {
            // 确保是完整URL
            if (!nextPageUrl.startsWith("http")) {
                nextPageUrl = "https://www.zhipin.com" + nextPageUrl;
            }
            page.addTargetRequest(nextPageUrl);
        }
        
        // 列表页不需要保存
        page.setSkip(true);
    }

    /**
     * 处理详情页
     */
    private void processDetailPage(Page page) {
        String url = page.getUrl().toString();
        Document doc = page.getHtml().getDocument();
        
        try {
            // 创建原始职位数据对象
            RawJobData jobData = new RawJobData();
            
            // 提取职位标题
            String title = doc.select(".name").text().trim();
            jobData.setTitle(title);
            
            // 提取薪资范围
            String salaryText = doc.select(".salary").text().trim();
            Matcher salaryMatcher = SALARY_PATTERN.matcher(salaryText);
            String salaryMin = "";
            String salaryMax = "";
            if (salaryMatcher.find()) {
                salaryMin = salaryMatcher.group(1);
                salaryMax = salaryMatcher.group(2);
            }
            jobData.setSalaryRange(salaryText);
            jobData.setSalaryMin(salaryMin);
            jobData.setSalaryMax(salaryMax);
            
            // 提取公司信息
            String companyName = doc.select(".company-info .name").text().trim();
            jobData.setCompanyName(companyName);
            
            // 提取公司行业
            String industry = doc.select(".sider-company p:contains(行业) + p").text().trim();
            jobData.setIndustry(industry);
            
            // 提取公司规模
            String size = doc.select(".sider-company p:contains(规模) + p").text().trim();
            jobData.setCompanySize(size);
            
            // 提取经验和学历要求
            String experienceEducation = doc.select(".job-banner .requirement span:eq(0)").text().trim();
            Matcher expEduMatcher = EXPERIENCE_EDUCATION_PATTERN.matcher(experienceEducation);
            String experience = "";
            String education = "";
            if (expEduMatcher.find()) {
                experience = expEduMatcher.group(1).trim();
                education = expEduMatcher.group(2).trim();
            }
            jobData.setExperienceRequired(experience);
            jobData.setEducationRequired(education);
            
            // 提取工作地点
            String location = doc.select(".job-banner .requirement span:eq(1)").text().trim();
            jobData.setLocation(location);
            
            // 提取职位描述
            String description = doc.select(".job-detail .text").html().trim();
            jobData.setDescription(description);
            
            // 提取公司介绍
            String companyDescription = doc.select(".job-sec.company-info .text").html().trim();
            jobData.setCompanyDescription(companyDescription);
            
            // 提取业务方向/产品
            List<String> businessList = new ArrayList<>();
            Elements businessElements = doc.select(".job-sec.company-products .product-item");
            for (Element element : businessElements) {
                businessList.add(element.text().trim());
            }
            jobData.setBusinessDirection(StringUtils.join(businessList, "、"));
            
            // 提取技能标签
            List<String> tagList = new ArrayList<>();
            Elements tagElements = doc.select(".job-tags span");
            for (Element element : tagElements) {
                tagList.add(element.text().trim());
            }
            jobData.setTags(tagList);
            
            // 提取招聘者信息
            String recruiterName = doc.select(".job-boss-info .name").text().trim();
            String recruiterTitle = doc.select(".job-boss-info .label").text().trim();
            jobData.setRecruiterName(recruiterName);
            jobData.setRecruiterTitle(recruiterTitle);
            
            // 设置数据来源
            jobData.setSource(CrawlerConstants.SOURCE_BOSS_ZHIPIN);
            jobData.setSourceUrl(url);
            
            // 保存结果
            page.putField("jobData", jobData);
            
        } catch (Exception e) {
            log.error("解析职位详情页出错: {}", url, e);
            page.setSkip(true);
        }
    }

    @Override
    public Site getSite() {
        return site;
    }
}