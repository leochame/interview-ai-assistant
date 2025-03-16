
package com.interview.rag.service.impl;

import com.defiy.mysql.MysqlVectorStore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interview.rag.model.VectorDocument;
import com.interview.rag.service.VectorStoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 面试知识库向量存储服务实现
 * 基于Defiy MySQL向量存储
 */
@Slf4j
@Service
@Qualifier("interviewVectorStoreService")
public class InterviewVectorStoreServiceImpl implements VectorStoreService {

    private final MysqlVectorStore vectorStore;
    private final ObjectMapper objectMapper;

    public InterviewVectorStoreServiceImpl(
            @Qualifier("interviewVectorStore") MysqlVectorStore vectorStore,
            ObjectMapper objectMapper) {
        this.vectorStore = vectorStore;
        this.objectMapper = objectMapper;
    }

    @Override
    public String addDocument(VectorDocument document) {
        try {
            String metadataJson = null;
            if (document.getMetadata() != null) {
                metadataJson = objectMapper.writeValueAsString(document.getMetadata());
            }

            String docId = vectorStore.addDocument(
                    document.getContent(),
                    document.getEmbedding(),
                    metadataJson
            );
            
            return docId;
        } catch (JsonProcessingException e) {
            log.error("元数据序列化失败", e);
            throw new RuntimeException("添加文档失败: 元数据序列化错误", e);
        } catch (Exception e) {
            log.error("添加文档失败", e);
            throw new RuntimeException("添加文档失败", e);
        }
    }

    @Override
    public List<String> addDocuments(List<VectorDocument> documents) {
        List<String> documentIds = new ArrayList<>();
        
        for (VectorDocument document : documents) {
            try {
                String docId = addDocument(document);
                documentIds.add(docId);
            } catch (Exception e) {
                log.error("批量添加文档失败: {}", document.getContent(), e);
                // 继续处理下一个文档
            }
        }
        
        return documentIds;
    }

    @Override
    public boolean deleteDocument(String docId) {
        try {
            return vectorStore.deleteDocument(docId);
        } catch (Exception e) {
            log.error("删除文档失败: {}", docId, e);
            return false;
        }
    }

    @Override
    public int deleteDocumentsByMetadata(Map<String, Object> metadataFilter) {
        try {
            String metadataFilterJson = objectMapper.writeValueAsString(metadataFilter);
            return vectorStore.deleteDocumentsByMetadata(metadataFilterJson);
        } catch (JsonProcessingException e) {
            log.error("元数据序列化失败", e);
            throw new RuntimeException("删除文档失败: 元数据序列化错误", e);
        } catch (Exception e) {
            log.error("根据元数据删除文档失败", e);
            throw new RuntimeException("删除文档失败", e);
        }
    }

    @Override
    public List<VectorDocument> similaritySearch(List<Float> embedding, int topK) {
        try {
            List<Map<String, Object>> results = vectorStore.similaritySearch(embedding, topK);
            
            return results.stream()
                    .map(this::mapToVectorDocument)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("相似度搜索失败", e);
            throw new RuntimeException("相似度搜索失败", e);
        }
    }

    @Override
    public List<VectorDocument> similaritySearchWithMetadata(List<Float> embedding, Map<String, Object> metadataFilter, int topK) {
        try {
            String metadataFilterJson = null;
            if (metadataFilter != null && !metadataFilter.isEmpty()) {
                metadataFilterJson = objectMapper.writeValueAsString(metadataFilter);
            }
            
            List<Map<String, Object>> results = vectorStore.similaritySearchWithMetadata(embedding, metadataFilterJson, topK);
            
            return results.stream()
                    .map(this::mapToVectorDocument)
                    .collect(Collectors.toList());
        } catch (JsonProcessingException e) {
            log.error("元数据序列化失败", e);
            throw new RuntimeException("相似度搜索失败: 元数据序列化错误", e);
        } catch (Exception e) {
            log.error("带元数据筛选的相似度搜索失败", e);
            throw new RuntimeException("相似度搜索失败", e);
        }
    }

    @Override
    public long getDocumentCount() {
        try {
            return vectorStore.getDocumentCount();
        } catch (Exception e) {
            log.error("获取文档数量失败", e);
            return 0;
        }
    }

    @Override
    public boolean clearCollection() {
        try {
            return vectorStore.clearCollection();
        } catch (Exception e) {
            log.error("清空集合失败", e);
            return false;
        }
    }
    
    /**
     * 将查询结果映射为向量文档对象
     */
    @SuppressWarnings("unchecked")
    private VectorDocument mapToVectorDocument(Map<String, Object> result) {
        try {
            VectorDocument.VectorDocumentBuilder builder = VectorDocument.builder();
            
            if (result.containsKey("id")) {
                builder.id(result.get("id").toString());
            }
            
            if (result.containsKey("content")) {
                builder.content((String) result.get("content"));
            }
            
            if (result.containsKey("embedding")) {
                Object embObj = result.get("embedding");
                if (embObj instanceof List) {
                    builder.embedding((List<Float>) embObj);
                }
            }
            
            if (result.containsKey("metadata")) {
                Object metaObj = result.get("metadata");
                if (metaObj instanceof String) {
                    Map<String, Object> metadata = objectMapper.readValue((String) metaObj, Map.class);
                    builder.metadata(metadata);
                } else if (metaObj instanceof Map) {
                    builder.metadata((Map<String, Object>) metaObj);
                }
            }
            
            if (result.containsKey("score")) {
                Object scoreObj = result.get("score");
                if (scoreObj instanceof Number) {
                    builder.score(((Number) scoreObj).floatValue());
                }
            }
            
            return builder.build();
        } catch (Exception e) {
            log.error("映射向量文档失败", e);
            throw new RuntimeException("映射向量文档失败", e);
        }
    }
}

