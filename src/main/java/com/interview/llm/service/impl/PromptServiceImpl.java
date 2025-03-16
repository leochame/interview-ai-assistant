package com.interview.llm.service.impl;

import com.interview.llm.prompt.PromptTemplate;
import com.interview.llm.service.PromptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 提示词服务实现类
 */
@Slf4j
@Service
public class PromptServiceImpl implements PromptService {

    @Value("${prompt.template.path:classpath:templates}")
    private String templatePath;
    
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{([^}]+)\\}\\}");
    
    // 缓存已加载的模板
    private final Map<String, PromptTemplate> templateCache = new HashMap<>();

    @Override
    public PromptTemplate loadTemplate(String templateName) {
        // 先从缓存中查找
        if (templateCache.containsKey(templateName)) {
            return templateCache.get(templateName);
        }
        
        try {
            // 构造模板文件路径
            String fileName = templateName.endsWith(".template") ? templateName : templateName + ".template";
            Path path = Paths.get(templatePath, fileName);
            File file = path.toFile();
            
            if (!file.exists()) {
                log.error("模板文件不存在: {}", path);
                return null;
            }
            
            // 读取模板内容
            String content = Files.readString(path);
            
            // 解析变量列表
            StringBuilder variables = new StringBuilder();
            Matcher matcher = VARIABLE_PATTERN.matcher(content);
            while (matcher.find()) {
                if (variables.length() > 0) {
                    variables.append(",");
                }
                variables.append(matcher.group(1).trim());
            }
            
            // 创建模板对象
            PromptTemplate template = PromptTemplate.builder()
                    .name(templateName)
                    .content(content)
                    .variables(variables.toString())
                    .build();
            
            // 缓存模板
            templateCache.put(templateName, template);
            
            return template;
        } catch (IOException e) {
            log.error("加载模板失败: {}", templateName, e);
            return null;
        }
    }

    @Override
    public String formatPrompt(PromptTemplate template, Map<String, Object> variables) {
        if (template == null || template.getContent() == null) {
            return null;
        }
        
        String content = template.getContent();
        
        if (variables == null || variables.isEmpty()) {
            return content;
        }
        
        // 替换变量
        Matcher matcher = VARIABLE_PATTERN.matcher(content);
        StringBuffer sb = new StringBuffer();
        
        while (matcher.find()) {
            String variableName = matcher.group(1).trim();
            Object variableValue = variables.get(variableName);
            String replacement = variableValue != null ? variableValue.toString() : "";
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        
        matcher.appendTail(sb);
        return sb.toString();
    }

    @Override
    public String formatPrompt(String templateName, Map<String, Object> variables) {
        PromptTemplate template = loadTemplate(templateName);
        if (template == null) {
            return null;
        }
        
        return formatPrompt(template, variables);
    }

    @Override
    public void saveTemplate(PromptTemplate template) {
        if (template == null || template.getName() == null || template.getContent() == null) {
            log.error("无效的模板");
            return;
        }
        
        try {
            // 构造模板文件路径
            String fileName = template.getName().endsWith(".template") ? template.getName() : template.getName() + ".template";
            Path path = Paths.get(templatePath, fileName);
            
            // 创建父目录
            Files.createDirectories(path.getParent());
            
            // 写入模板内容
            Files.writeString(path, template.getContent());
            
            // 更新缓存
            templateCache.put(template.getName(), template);
            
            log.info("保存模板成功: {}", template.getName());
        } catch (IOException e) {
            log.error("保存模板失败: {}", template.getName(), e);
        }
    }
}