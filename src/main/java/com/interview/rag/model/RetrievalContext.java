package com.interview.rag.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 检索上下文模型
 * 用于整合检索结果，传递给LLM进行回答生成
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetrievalContext {
    /**
     * 原始查询
     */
    private String query;
    
    /**
     * 检索到的文档内容列表
     */
    @Builder.Default
    private List<String> documentContents = new ArrayList<>();
    
    /**
     * 文档元数据列表
     */
    @Builder.Default
    private List<Map<String, Object>> documentMetadata = new ArrayList<>();
    
    /**
     * 相似度分数列表
     */
    @Builder.Default
    private List<Float> similarityScores = new ArrayList<>();
    
    /**
     * 引用来源列表（用于引用标注）
     */
    @Builder.Default
    private List<String> sources = new ArrayList<>();
    
    /**
     * 格式化为文本上下文
     */
    public String formatAsText() {
        StringBuilder context = new StringBuilder();
        context.append("以下是与问题相关的参考信息：\n\n");
        
        for (int i = 0; i < documentContents.size(); i++) {
            context.append("参考文档 ").append(i + 1).append("：\n");
            context.append(documentContents.get(i)).append("\n\n");
            
            if (sources != null && i < sources.size()) {
                context.append("来源：").append(sources.get(i)).append("\n\n");
            }
        }
        
        return context.toString();
    }
}