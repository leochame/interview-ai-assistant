package com.interview.rag.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 查询结果模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryResult {
    /**
     * 查询文本
     */
    private String query;
    
    /**
     * 检索到的文档列表
     */
    @Builder.Default
    private List<VectorDocument> documents = new ArrayList<>();
    
    /**
     * 检索上下文（用于传递给LLM）
     */
    private String context;
    
    /**
     * 执行时间（毫秒）
     */
    private long executionTime;
}

