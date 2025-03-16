
package com.interview.cleaner.processor;

import com.interview.cleaner.model.CleanedQuestionData;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 内容提取处理器
 * 负责从原始文本中提取结构化信息
 */
@Component
public class ContentExtractor {

    // 问题匹配模式
    private static final Pattern QUESTION_PATTERN = Pattern.compile(
            "(问题\\s*\\d+|Q\\s*\\d+|\\d+\\s*[.．、]|问：|Q：|面试官：|面试官问：|请问|怎么|如何|是什么|为什么)(.*?)(\\?|？|。|\\n|$)");

    // 回答匹配模式
    private static final Pattern ANSWER_PATTERN = Pattern.compile(
            "(答案\\s*\\d+|A\\s*\\d+|答：|A：|我：|我答：|我的回答：)(.*?)(?=(问题\\s*\\d+|Q\\s*\\d+|\\d+\\s*[.．、]|问：|Q：|面试官：|面试官问：|答案\\s*\\d+|A\\s*\\d+|答：|A：|我：|我答：|我的回答：|$))");

    // 问答对匹配模式
    private static final Pattern QA_PATTERN = Pattern.compile(
            "(问题\\s*\\d+|Q\\s*\\d+|\\d+\\s*[.．、]|问：|Q：|面试官：|面试官问：)(.*?)(答案\\s*\\d+|A\\s*\\d+|答：|A：|我：|我答：|我的回答：)(.*?)(?=(问题\\s*\\d+|Q\\s*\\d+|\\d+\\s*[.．、]|问：|Q：|面试官：|面试官问：|$))");

    // 技能关键词匹配模式
    private static final Pattern SKILL_PATTERN = Pattern.compile(
            "(?i)(Java|Python|Go|C\\+\\+|JavaScript|TypeScript|React|Vue|Angular|Spring|SpringBoot|MyBatis|Hibernate|Docker|Kubernetes|MySQL|Redis|MongoDB|ElasticSearch|Linux|Git|AWS|Kafka|RabbitMQ|微服务|分布式|并发|多线程|算法|数据结构|设计模式|网络|安全|性能优化|前端|后端|全栈|AI|机器学习|大数据)(\\s|,|，|.|。|;|；|:|：|\\)|）|\\]|」|\\}|$)");

    /**
     * 从面经内容中提取问题列表
     *
     * @param content 面经内容
     * @return 提取的问题列表
     */
    public List<CleanedQuestionData> extractQuestions(String content) {
        if (StringUtils.isBlank(content)) {
            return new ArrayList<>();
        }

        List<CleanedQuestionData> questions = new ArrayList<>();

        // 首先尝试匹配完整的问答对
        Matcher qaMatcher = QA_PATTERN.matcher(content);
        while (qaMatcher.find()) {
            CleanedQuestionData question = new CleanedQuestionData();
            question.setQuestion(qaMatcher.group(2).trim());
            question.setAnswer(qaMatcher.group(4).trim());
            question.setDifficulty(estimateDifficulty(question.getQuestion(), question.getAnswer()));
            question.setCategory(categorizeQuestion(question.getQuestion()));
            questions.add(question);
        }

        // 如果没有找到完整的问答对，则尝试分别匹配问题和答案
        if (questions.isEmpty()) {
            List<String> extractedQuestions = new ArrayList<>();
            Matcher questionMatcher = QUESTION_PATTERN.matcher(content);
            while (questionMatcher.find()) {
                extractedQuestions.add(questionMatcher.group(2).trim());
            }

            List<String> extractedAnswers = new ArrayList<>();
            Matcher answerMatcher = ANSWER_PATTERN.matcher(content);
            while (answerMatcher.find()) {
                extractedAnswers.add(answerMatcher.group(2).trim());
            }

            // 将问题和答案配对
            int minSize = Math.min(extractedQuestions.size(), extractedAnswers.size());
            for (int i = 0; i < minSize; i++) {
                CleanedQuestionData question = new CleanedQuestionData();
                question.setQuestion(extractedQuestions.get(i));
                question.setAnswer(extractedAnswers.get(i));
                question.setDifficulty(estimateDifficulty(question.getQuestion(), question.getAnswer()));
                question.setCategory(categorizeQuestion(question.getQuestion()));
                questions.add(question);
            }
        }

        return questions;
    }

    /**
     * 从职位描述中提取技能要求
     *
     * @param description 职位描述
     * @return 技能列表
     */
    public List<String> extractSkills(String description) {
        if (StringUtils.isBlank(description)) {
            return new ArrayList<>();
        }

        List<String> skills = new ArrayList<>();
        Matcher matcher = SKILL_PATTERN.matcher(description);

        while (matcher.find()) {
            String skill = matcher.group(1).trim();
            if (!skills.contains(skill)) {
                skills.add(skill);
            }
        }

        return skills;
    }

    /**
     * 从职位描述中提取工作职责
     *
     * @param description 职位描述
     * @return 工作职责列表
     */
    public List<String> extractResponsibilities(String description) {
        if (StringUtils.isBlank(description)) {
            return new ArrayList<>();
        }

        List<String> responsibilities = new ArrayList<>();

        // 寻找职责相关的段落
        Pattern responsibilitySection = Pattern.compile(
                "(?i)(工作职责|岗位职责|工作内容|主要职责|职位描述|工作职能|职位职责|你将负责)(.*?)(任职要求|岗位要求|职位要求|任职资格|能力要求|我们期望|我们希望|我们要求|福利待遇|薪资福利|工作地点|其他福利|加入我们的理由|加入我们|为什么加入|$)",
                Pattern.DOTALL);

        Matcher sectionMatcher = responsibilitySection.matcher(description);
        if (sectionMatcher.find()) {
            String responsibilityText = sectionMatcher.group(2).trim();

            // 提取列表项
            Pattern listItemPattern = Pattern.compile("(\\d+[.．、]|[·•◦]|[-—]|\\*)\\s*(.*?)(?=(\\d+[.．、]|[·•◦]|[-—]|\\*|$))");
            Matcher itemMatcher = listItemPattern.matcher(responsibilityText);

            while (itemMatcher.find()) {
                String item = itemMatcher.group(2).trim();
                if (StringUtils.isNotBlank(item) && item.length() > 5) {
                    responsibilities.add(item);
                }
            }

            // 如果没有找到列表项，则尝试按句子分割
            if (responsibilities.isEmpty()) {
                String[] sentences = responsibilityText.split("[。；;]");
                for (String sentence : sentences) {
                    String trimmed = sentence.trim();
                    if (StringUtils.isNotBlank(trimmed) && trimmed.length() > 5) {
                        responsibilities.add(trimmed);
                    }
                }
            }
        }

        return responsibilities;
    }

    /**
     * 估算问题难度
     *
     * @param question 问题内容
     * @param answer   回答内容
     * @return 难度估计（1-5）
     */
    private int estimateDifficulty(String question, String answer) {
        // 基于问题和答案的长度、复杂性等因素估算难度
        int difficulty = 1;

        // 问题长度
        if (question.length() > 50) difficulty++;
        if (question.length() > 100) difficulty++;

        // 答案长度
        if (answer != null) {
            if (answer.length() > 200) difficulty++;
            if (answer.length() > 500) difficulty++;
        }

        // 问题中包含高级关键词
        if (question.matches("(?i).*(设计模式|架构|优化|原理|底层|源码|分布式|并发|多线程|算法复杂度).*")) {
            difficulty++;
        }

        // 限制在1-5之间
        return Math.max(1, Math.min(5, difficulty));
    }

    /**
     * 对问题进行分类
     *
     * @param question 问题内容
     * @return 问题分类
     */
    private String categorizeQuestion(String question) {
        String lowerQuestion = question.toLowerCase();

        if (lowerQuestion.matches(".*(算法|排序|搜索|复杂度|数据结构|链表|树|图|堆|栈|队列).*")) {
            return "算法与数据结构";
        }

        if (lowerQuestion.matches(".*(java|jvm|gc|内存|垃圾回收|类加载|线程|锁|并发|多线程|spring|springboot|mybatis|hibernate).*")) {
            return "Java";
        }

        if (lowerQuestion.matches(".*(python|django|flask|pandas|numpy|机器学习|深度学习|神经网络).*")) {
            return "Python";
        }

        if (lowerQuestion.matches(".*(javascript|typescript|html|css|react|vue|angular|node|webpack|前端).*")) {
            return "前端";
        }

        if (lowerQuestion.matches(".*(go|golang|goroutine|channel).*")) {
            return "Go";
        }

        if (lowerQuestion.matches(".*(数据库|sql|mysql|oracle|mongodb|redis|nosql).*")) {
            return "数据库";
        }

        if (lowerQuestion.matches(".*(分布式|微服务|服务治理|rpc|服务发现|注册中心|配置中心|负载均衡|容错|熔断|限流|降级).*")) {
            return "分布式系统";
        }

        if (lowerQuestion.matches(".*(linux|操作系统|进程|线程|内存管理|文件系统|io|网络编程|shell).*")) {
            return "操作系统";
        }

        if (lowerQuestion.matches(".*(网络|http|https|tcp|ip|udp|socket|协议|restful|api).*")) {
            return "计算机网络";
        }

        if (lowerQuestion.matches(".*(设计模式|架构|架构设计|系统设计|重构|代码质量).*")) {
            return "架构设计";
        }

        if (lowerQuestion.matches(".*(docker|kubernetes|k8s|容器|虚拟化|云原生|devops|ci|cd).*")) {
            return "DevOps";
        }

        if (lowerQuestion.matches(".*(测试|单元测试|集成测试|自动化测试|tdd|测试驱动|junit|mock).*")) {
            return "测试";
        }

        if (lowerQuestion.matches(".*(安全|加密|授权|认证|漏洞|攻击|xss|csrf|sql注入).*")) {
            return "安全";
        }

        if (lowerQuestion.matches(".*(大数据|hadoop|spark|flink|流处理|批处理|数据仓库|etl|hive).*")) {
            return "大数据";
        }

        if (lowerQuestion.matches(".*(项目经验|自我介绍|职业规划|简历|加班|离职|薪资|期望|为什么|优缺点).*")) {
            return "行为面试";
        }

        return "其他";
    }
}