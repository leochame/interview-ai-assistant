package com.interview.crawler.processor;

import com.interview.common.constants.CrawlerConstants;
import com.interview.crawler.model.RawInterviewData;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 牛客网面经爬虫处理器
 */
@Slf4j
@Component
public class NowcoderProcessor implements PageProcessor {

    private final Site site;

    @Value("${crawler.nowcoder.list-url-pattern}")
    private String listUrlPattern;

    @Value("${crawler.nowcoder.detail-url-pattern}")
    private String detailUrlPattern;

    // 公司名称提取正则
    private static final Pattern COMPANY_PATTERN = Pattern.compile("(.*?)面经");
    // 职位名称提取正则
    private static final Pattern POSITION_PATTERN = Pattern.compile("【(.*?)】");
    // 日期格式化
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public NowcoderProcessor(@Value("${crawler.user-agent:}") String userAgent,
                          @Value("${crawler.domain.nowcoder:www.nowcoder.com}") String domain,
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
        
        // 提取所有面经详情页URL
        List<String> detailUrls = html.links().regex(detailUrlPattern).all();
        page.addTargetRequests(detailUrls);
        
        // 提取下一页URL
        String nextPageUrl = html.xpath("//a[@class='btn-next']/@href").toString();
        if (StringUtils.isNotBlank(nextPageUrl)) {
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
            // 创建原始面经数据对象
            RawInterviewData interviewData = new RawInterviewData();
            
            // 提取标题
            String title = doc.select(".discuss-post-title").text().trim();
            interviewData.setTitle(title);
            
            // 提取公司名称
            String company = "";
            Matcher companyMatcher = COMPANY_PATTERN.matcher(title);
            if (companyMatcher.find()) {
                company = companyMatcher.group(1).trim();
            }
            interviewData.setCompany(company);
            
            // 提取职位
            String position = "";
            Matcher positionMatcher = POSITION_PATTERN.matcher(title);
            if (positionMatcher.find()) {
                position = positionMatcher.group(1).trim();
            }
            interviewData.setPosition(position);
            
            // 提取作者
            String author = doc.select(".discuss-post-username").text().trim();
            interviewData.setAuthor(author);
            
            // 提取发布时间
            String publishDateStr = doc.select(".post-time").text().trim();
            LocalDate publishDate = null;
            try {
                if (StringUtils.isNotBlank(publishDateStr)) {
                    publishDate = LocalDate.parse(publishDateStr, DATE_FORMATTER);
                }
            } catch (Exception e) {
                log.warn("解析发布时间失败: {}", publishDateStr);
            }
            interviewData.setPublishDate(publishDate);
            
            // 提取正文内容
            String content = doc.select(".post-content").html().trim();
            interviewData.setContent(content);
            
            // 提取标签
            List<String> tags = new ArrayList<>();
            Elements tagElements = doc.select(".tag-list .tag");
            for (Element tagElement : tagElements) {
                tags.add(tagElement.text().trim());
            }
            interviewData.setTags(tags);
            
            // 设置数据来源
            interviewData.setSource(CrawlerConstants.SOURCE_NOWCODER);
            interviewData.setSourceUrl(url);
            
            // 保存结果
            page.putField("interviewData", interviewData);
            
        } catch (Exception e) {
            log.error("解析面经详情页出错: {}", url, e);
            page.setSkip(true);
        }
    }

    @Override
    public Site getSite() {
        return site;
    }
}