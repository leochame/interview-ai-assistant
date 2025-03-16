
package com.interview.llm.service.impl;

import com.interview.entity.InterviewExperience;
import com.interview.entity.InterviewQuestion;
import com.interview.entity.InterviewSimulation;
import com.interview.entity.JobDescription;
import com.interview.llm.model.Conversation;
import com.interview.llm.model.InterviewFeedback;
import com.interview.llm.model.Message;
import com.interview.llm.prompt.InterviewerPrompt;
import com.interview.llm.service.InterviewSimulatorService;
import com.interview.llm.service.LLMService;
import com.interview.llm.service.PromptService;
import com.interview.repository.InterviewExperienceRepository;
import com.interview.repository.InterviewQuestionRepository;
import com.interview.repository.InterviewSimulationRepository;
import com.interview.repository.JobDescriptionRepository;
import com.interview.rag.model.RetrievalContext;
import com.interview.rag.service.RagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 面试模拟服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewSimulatorServiceImpl implements InterviewSimulatorService {

    private final LLMService llmService;
    private final PromptService promptService;
    private final RagService ragService;
    private final InterviewerPrompt interviewerPrompt;

    private final InterviewSimulationRepository simulationRepository;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final InterviewQuestionRepository questionRepository;
    private final InterviewExperienceRepository experienceRepository;

    // 模拟面试会话缓存
    private final Map<Long, Conversation> simulationCache = new HashMap<>();

    @Override
    @Transactional
    public Long createSimulation(Long userId, Long jobDescriptionId, String simulationType) {
        // 1. 获取职位信息
        JobDescription jobDescription = jobDescriptionRepository.findById(jobDescriptionId)
                .orElseThrow(() -> new IllegalArgumentException("职位不存在: " + jobDescriptionId));

        // 2. 创建模拟记录
        InterviewSimulation simulation = new InterviewSimulation();
        simulation.setUserId(userId);
        simulation.setJobDescriptionId(jobDescriptionId);
        simulation.setSimulationType(simulationType);
        simulation.setStartTime(LocalDateTime.now());
        simulation.setStatus(0); // 未完成

        InterviewSimulation savedSimulation = simulationRepository.save(simulation);

        // 3. 创建会话
        Conversation conversation = Conversation.builder()
                .id(savedSimulation.getId().toString())
                .userId(userId)
                .title(jobDescription.getTitle() + " 面试模拟")
                .type(simulationType)
                .createdAt(new Date())
                .build();

        // 4. 准备系统提示词
        String systemPrompt;
        if (jobDescription.getTitle().toLowerCase().contains("java")) {
            systemPrompt = interviewerPrompt.getJavaInterviewerPrompt();
        } else if (jobDescription.getTitle().toLowerCase().contains("前端") ||
                jobDescription.getTitle().toLowerCase().contains("frontend")) {
            systemPrompt = interviewerPrompt.getFrontendInterviewerPrompt();
        } else if (jobDescription.getTitle().toLowerCase().contains("算法") ||
                jobDescription.getTitle().toLowerCase().contains("algorithm")) {
            systemPrompt = interviewerPrompt.getAlgorithmEngineerPrompt();
        } else {
            systemPrompt = interviewerPrompt.getSystemPrompt();
        }

        // 5. 替换变量
        Map<String, Object> variables = new HashMap<>();
        variables.put("position", jobDescription.getTitle());
        variables.put("job_description", jobDescription.getDescription());
        variables.put("required_skills", jobDescription.getRequirements());

        String formattedPrompt = promptService.formatPrompt(
                PromptTemplate.builder().content(systemPrompt).build(),
                variables);

        // 6. 添加系统提示词到会话
        conversation.addSystemMessage(formattedPrompt);

        // 7. 缓存会话
        simulationCache.put(savedSimulation.getId(), conversation);

        return savedSimulation.getId();
    }

    @Override
    public String startSimulation(Long simulationId) {
        // 1. 获取会话
        Conversation conversation = getConversation(simulationId);

        // 2. 生成面试官开场白
        String response = llmService.generateChatResponse(conversation);

        // 3. 添加助手回复
        conversation.addAssistantMessage(response);

        // 4. 更新缓存
        simulationCache.put(simulationId, conversation);

        return response;
    }

    @Override
    public String sendMessage(Long simulationId, String userMessage) {
        // 1. 获取会话
        Conversation conversation = getConversation(simulationId);

        // 2. 添加用户消息
        conversation.addUserMessage(userMessage);

        // 3. 生成面试官回复
        String response = llmService.generateChatResponse(conversation);

        // 4. 添加助手回复
        conversation.addAssistantMessage(response);

        // 5. 更新缓存
        simulationCache.put(simulationId, conversation);

        return response;
    }

    @Override
    @Transactional
    public InterviewFeedback endSimulation(Long simulationId) {
        // 1. 获取会话
        Conversation conversation = getConversation(simulationId);

        // 2. 获取模拟记录
        InterviewSimulation simulation = simulationRepository.findById(simulationId)
                .orElseThrow(() -> new IllegalArgumentException("模拟记录不存在: " + simulationId));

        // 3. 生成评估提示词
        String feedbackPrompt = """
                你是一位专业的面试评估专家。请根据以下面试对话，对候选人的表现进行全面评估。
                
                请从以下几个方面进行评估：
                1. 技术能力（满分100分）
                2. 沟通能力（满分100分）
                3. 专业知识（满分100分）
                4. 问题解决能力（满分100分）
                5. 总体表现（满分100分）
                
                并提供以下内容：
                1. 优势（列出3-5个）
                2. 需要改进的地方（列出3-5个）
                3. 针对每个问题的具体反馈
                4. 改进建议（列出3-5个）
                5. 总体评价（100-200字）
                
                面试对话记录：
                {{interview_dialog}}
                
                请提供详细、客观、有建设性的反馈，帮助候选人提升面试表现。
                """;

        // 4. 提取对话内容
        StringBuilder dialogBuilder = new StringBuilder();
        for (Message message : conversation.getMessages()) {
            if (message.getRole() == Message.Role.SYSTEM) {
                continue; // 跳过系统消息
            }

            String role = message.getRole() == Message.Role.USER ? "候选人" : "面试官";
            dialogBuilder.append(role).append(": ").append(message.getContent()).append("\n\n");
        }

        // 5. 替换变量
        Map<String, Object> variables = new HashMap<>();
        variables.put("interview_dialog", dialogBuilder.toString());

        String formattedPrompt = promptService.formatPrompt(
                PromptTemplate.builder().content(feedbackPrompt).build(),
                variables);

        // 6. 生成评估
        String feedbackText = llmService.generateText(formattedPrompt);

        // 7. 解析评估结果
        InterviewFeedback feedback = parseFeedback(feedbackText);

        // 8. 更新模拟记录
        simulation.setEndTime(LocalDateTime.now());
        simulation.setScore(feedback.getOverallScore());
        simulation.setFeedback(feedbackText);
        simulation.setStatus(1); // 已完成

        simulationRepository.save(simulation);

        // 9. 从缓存中移除会话
        simulationCache.remove(simulationId);

        return feedback;
    }

    @Override
    public Conversation getSimulationHistory(Long simulationId) {
        return getConversation(simulationId);
    }

    @Override
    public List<String> generateInterviewQuestions(Long jobDescriptionId, int count) {
        // 1. 获取职位信息
        JobDescription jobDescription = jobDescriptionRepository.findById(jobDescriptionId)
                .orElseThrow(() -> new IllegalArgumentException("职位不存在: " + jobDescriptionId));

        // 2. 准备提示词
        String prompt = """
                你是一位资深的技术面试官。请根据以下职位描述，生成{{count}}个高质量的技术面试问题。
                
                这些问题应该：
                1. 覆盖职位所需的核心技术技能
                2. 包括基础知识、技术原理和实际应用
                3. 有不同的难度级别（简单、中等、困难）
                4. 能够有效评估候选人的技术能力和思维方式
                
                职位标题：{{job_title}}
                
                职位描述：
                {{job_description}}
                
                技能要求：
                {{requirements}}
                
                请直接列出问题，每个问题一行，不要包含编号或额外说明。
                """;

        // 3. 替换变量
        Map<String, Object> variables = new HashMap<>();
        variables.put("count", count);
        variables.put("job_title", jobDescription.getTitle());
        variables.put("job_description", jobDescription.getDescription());
        variables.put("requirements", jobDescription.getRequirements());

        String formattedPrompt = promptService.formatPrompt(
                PromptTemplate.builder().content(prompt).build(),
                variables);

        // 4. 生成问题
        String questionsText = llmService.generateText(formattedPrompt);

        // 5. 解析问题列表
        return Arrays.stream(questionsText.split("\n"))
                .map(String::trim)
                .filter(q -> !q.isEmpty())
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> evaluateAnswer(String question, String answer) {
        // 1. 使用RAG检索相关内容
        RetrievalContext retrievalContext = ragService.searchWithContext(question, 3);

        // 2. 准备评估提示词
        String prompt = """
                你是一位专业的技术面试评估专家。请评估候选人对以下问题的回答质量。
                
                问题：
                {{question}}
                
                候选人回答：
                {{answer}}
                
                参考资料：
                {{reference}}
                
                请从以下几个方面进行评估：
                1. 准确性（满分100分）：回答是否包含技术错误或误解
                2. 完整性（满分100分）：是否全面覆盖了问题的关键点
                3. 深度（满分100分）：是否展示了对技术原理的深入理解
                4. 表达清晰度（满分100分）：是否条理清晰、逻辑严密
                5. 总体得分（满分100分）
                
                并提供：
                1. 优点（列出2-3个）
                2. 不足（列出2-3个）
                3. 改进建议（100字以内）
                4. 参考答案要点（列出关键要点）
                
                请以JSON格式返回评估结果，包含上述所有字段。
                """;

        // 3. 替换变量
        Map<String, Object> variables = new HashMap<>();
        variables.put("question", question);
        variables.put("answer", answer);
        variables.put("reference", retrievalContext.formatAsText());

        String formattedPrompt = promptService.formatPrompt(
                PromptTemplate.builder().content(prompt).build(),
                variables);

        // 4. 生成评估
        String evaluationText = llmService.generateText(formattedPrompt);

        // 5. 解析评估结果
        try {
            // TODO: 实现JSON解析逻辑
            // 这里简化处理，返回文本结果
            Map<String, Object> result = new HashMap<>();
            result.put("evaluation", evaluationText);
            return result;
        } catch (Exception e) {
            log.error("解析评估结果失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("evaluation", evaluationText);
            return result;
        }
    }

    @Override
    public String provideGuidance(Long userId) {
        // 1. 使用RAG检索相关内容
        RetrievalContext retrievalContext = ragService.searchWithContext("面试技巧 简历优化 职业发展", 5);

        // 2. 准备指导提示词
        String prompt = """
                你是一位职业发展顾问和面试教练。请根据以下内容，为求职者提供全面的面试准备和职业发展指导。
                
                指导应包括：
                1. 简历优化建议
                2. 面试准备策略
                3. 常见面试问题及回答技巧
                4. 技术面试特别建议
                5. 沟通技巧
                6. 职业发展路径建议
                
                参考资料：
                {{reference}}
                
                请提供详细、实用且有针对性的建议，帮助求职者在面试和职业发展中取得成功。
                """;

        // 3. 替换变量
        Map<String, Object> variables = new HashMap<>();
        variables.put("reference", retrievalContext.formatAsText());

        String formattedPrompt = promptService.formatPrompt(
                PromptTemplate.builder().content(prompt).build(),
                variables);

        // 4. 生成指导
        return llmService.generateText(formattedPrompt);
    }

    /**
     * 获取会话（从缓存或创建新的）
     */
    private Conversation getConversation(Long simulationId) {
        // 从缓存中获取
        Conversation conversation = simulationCache.get(simulationId);

        if (conversation != null) {
            return conversation;
        }

        // 缓存中不存在，从数据库加载
        InterviewSimulation simulation = simulationRepository.findById(simulationId)
                .orElseThrow(() -> new IllegalArgumentException("模拟记录不存在: " + simulationId));

        // TODO: 从数据库加载消息历史
        // 这里简化处理，创建一个新的会话
        conversation = Conversation.builder()
                .id(simulation.getId().toString())
                .userId(simulation.getUserId())
                .title("面试模拟")
                .type(simulation.getSimulationType())
                .createdAt(Date.from(simulation.getStartTime().toInstant(java.time.ZoneOffset.UTC)))
                .build();

        // 缓存会话
        simulationCache.put(simulationId, conversation);

        return conversation;
    }

    /**
     * 解析评估文本为结构化反馈对象
     */
    private InterviewFeedback parseFeedback(String feedbackText) {
        // TODO: 实现更精确的文本解析
        // 这里简化处理，使用正则表达式提取关键信息

        // 构建一个基本的反馈对象
        InterviewFeedback.InterviewFeedbackBuilder builder = InterviewFeedback.builder();

        // 提取分数
        extractScore(feedbackText, "技术能力[：:](\\d+)", score -> builder.technicalScore(score));
        extractScore(feedbackText, "沟通能力[：:](\\d+)", score -> builder.communicationScore(score));
        extractScore(feedbackText, "专业知识[：:](\\d+)", score -> builder.knowledgeScore(score));
        extractScore(feedbackText, "问题解决能力[：:](\\d+)", score -> builder.problemSolvingScore(score));
        extractScore(feedbackText, "总体[表现|评分|得分][：:](\\d+)", score -> builder.overallScore(score));

        // 提取优势
        List<String> strengths = extractListItems(feedbackText, "优势[：:]([\\s\\S]*?)(?=需要改进|不足|改进建议|总体评价)");
        builder.strengths(strengths);

        // 提取改进点
        List<String> improvements = extractListItems(feedbackText, "(需要改进|不足)[：:]([\\s\\S]*?)(?=针对|改进建议|总体评价)");
        builder.areasToImprove(improvements);

        // 提取建议
        List<String> suggestions = extractListItems(feedbackText, "改进建议[：:]([\\s\\S]*?)(?=总体评价|$)");
        builder.suggestions(suggestions);

        // 提取总体评价
        String overallFeedback = extractText(feedbackText, "总体评价[：:]([\\s\\S]*?)$");
        builder.overallFeedback(overallFeedback);

        // 如果没有提取到总体分数，计算平均分
        if (builder.build().getOverallScore() == 0) {
            int avgScore = (builder.build().getTechnicalScore() +
                    builder.build().getCommunicationScore() +
                    builder.build().getKnowledgeScore() +
                    builder.build().getProblemSolvingScore()) / 4;
            builder.overallScore(avgScore);
        }

        return builder.build();
    }

    /**
     * 提取分数
     */
    private void extractScore(String text, String pattern, java.util.function.Consumer<Integer> setter) {
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(text);
        if (m.find()) {
            try {
                int score = Integer.parseInt(m.group(1));
                setter.accept(score);
            } catch (NumberFormatException e) {
                log.warn("解析分数失败: {}", m.group(1));
            }
        }
    }

    /**
     * 提取列表项
     */
    private List<String> extractListItems(String text, String pattern) {
        List<String> items = new ArrayList<>();

        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(text);

        if (m.find()) {
            String content = m.group(m.groupCount()); // 获取最后一个捕获组
            String[] lines = content.split("\n");

            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.startsWith("-") || trimmed.startsWith("•") || trimmed.matches("\\d+\\..*")) {
                    items.add(trimmed.replaceFirst("^[-•\\d\\.]+\\s*", "").trim());
                } else if (!trimmed.isEmpty() && !items.contains(trimmed)) {
                    items.add(trimmed);
                }
            }
        }

        return items;
    }

    /**
     * 提取文本内容
     */
    private String extractText(String text, String pattern) {
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(text);

        if (m.find()) {
            return m.group(1).trim();
        }

        return "";
    }
}
