package com.interview.rag.service.impl;

import com.defiy.core.embedding.EmbeddingClient;
import com.defiy.mysql.MysqlVectorStore;
import com.interview.rag.model.QueryResult;
import com.interview.rag.model.RetrievalContext;
import com.interview.rag.model.VectorDocument;
import com.interview.rag.service.EmbeddingService;
import com.interview.rag.service.RagService;
import com.interview.rag.service.VectorStoreService;
import com.interview.rag.util.TextSplitter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 面试知识库 RAG 服务实现
 */
@Slf4j
@Service
public class InterviewRagServiceImpl implements RagService {

    private final VectorStoreService vectorStoreService;
    private final EmbeddingService embeddingService;
    private final TextSplitter textSplitter;

    public InterviewRagServiceImpl(
            @Qualifier("interviewVectorStoreService") VectorStoreService vectorStoreService,
            EmbeddingService embeddingService,
            TextSplitter textSplitter) {
        this.vectorStoreService = vectorStoreService;
        this.embeddingService = embeddingService;
        this.textSplitter = textSplitter;
    }

    @Override
    public String addDocument(String content, Map<String, Object> metadata) {
        if (content == null || content.isEmpty()) {
            throw new IllegalArgumentException("文档内容不能为空");
        }

        try {
            // 将长文本分割成块
            List<String> chunks = textSplitter.splitIntoChunks(content);
            if (chunks.isEmpty()) {
                chunks.add(content);
            }

            List<String> documentIds = new ArrayList<>();

            // 为每个块创建向量嵌入并存储
            for (int i = 0; i < chunks.size(); i++) {
                String chunk = chunks.get(i);
                List<Float> embedding = embeddingService.createEmbedding(chunk);

                // 为每个块添加额外元数据
                Map<String, Object> chunkMetadata = new HashMap<>(metadata);
                chunkMetadata.put("chunkIndex", i);
                chunkMetadata.put("chunkCount", chunks.size());
                if (chunks.size() > 1) {
                    chunkMetadata.put("isChunk", true);
                    chunkMetadata.put("originalContent", content.substring(0, Math.min(100, content.length())) + "...");
                }

                VectorDocument document = VectorDocument.builder()
                        .content(chunk)
                        .embedding(embedding)
                        .metadata(chunkMetadata)
                        .build();

                String docId = vectorStoreService.addDocument(document);
                documentIds.add(docId);
            }

            log.info("添加文档成功: 共{}个块", chunks.size());
            // 返回第一个块的ID作为文档ID
            return documentIds.get(0);
        } catch (Exception e) {
            log.error("添加文档失败", e);
            throw new RuntimeException("添加文档失败", e);
        }
    }

    @Override
    public List<String> addDocuments(List<String> contents, List<Map<String, Object>> metadataList) {
        if (contents == null || contents.isEmpty()) {
            throw new IllegalArgumentException("文档内容列表不能为空");
        }

        if (metadataList != null && contents.size() != metadataList.size()) {
            throw new IllegalArgumentException("文档内容列表和元数据列表长度不匹配");
        }

        List<String> documentIds = new ArrayList<>();
        for (int i = 0; i < contents.size(); i++) {
            Map<String, Object> metadata = metadataList != null ? metadataList.get(i) : new HashMap<>();
            String docId = addDocument(contents.get(i), metadata);
            documentIds.add(docId);
        }

        return documentIds;
    }

    @Override
    public boolean deleteDocument(String docId) {
        try {
            return vectorStoreService.deleteDocument(docId);
        } catch (Exception e) {
            log.error("删除文档失败", e);
            return false;
        }
    }

    @Override
    public QueryResult search(String query, int topK) {
        long startTime = System.currentTimeMillis();

        try {
            List<Float> queryEmbedding = embeddingService.createEmbedding(query);
            List<VectorDocument> documents = vectorStoreService.similaritySearch(queryEmbedding, topK);

            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;

            return QueryResult.builder()
                    .query(query)
                    .documents(documents)
                    .executionTime(executionTime)
                    .build();
        } catch (Exception e) {
            log.error("搜索失败", e);
            return QueryResult.builder()
                    .query(query)
                    .documents(new ArrayList<>())
                    .executionTime(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    @Override
    public RetrievalContext searchWithContext(String query, int topK) {
        QueryResult result = search(query, topK);
        List<VectorDocument> documents = result.getDocuments();

        List<String> documentContents = new ArrayList<>();
        List<Map<String, Object>> documentMetadata = new ArrayList<>();
        List<Float> similarityScores = new ArrayList<>();
        List<String> sources = new ArrayList<>();

        for (VectorDocument doc : documents) {
            documentContents.add(doc.getContent());
            documentMetadata.add(doc.getMetadata());
            similarityScores.add(doc.getScore());

            // 提取来源信息
            String source = "";
            if (doc.getMetadata() != null) {
                Object sourceObj = doc.getMetadata().get("source");
                if (sourceObj != null) {
                    source = sourceObj.toString();
                }
            }
            sources.add(source);
        }

        RetrievalContext context = RetrievalContext.builder()
                .query(query)
                .documentContents(documentContents)
                .documentMetadata(documentMetadata)
                .similarityScores(similarityScores)
                .sources(sources)
                .build();

        result.setContext(context.formatAsText());
        return context;
    }

    @Override
    public QueryResult searchWithMetadata(String query, Map<String, Object> metadataFilter, int topK) {
        long startTime = System.currentTimeMillis();

        try {
            List<Float> queryEmbedding = embeddingService.createEmbedding(query);
            List<VectorDocument> documents = vectorStoreService.similaritySearchWithMetadata(queryEmbedding, metadataFilter, topK);

            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;

            return QueryResult.builder()
                    .query(query)
                    .documents(documents)
                    .executionTime(executionTime)
                    .build();
        } catch (Exception e) {
            log.error("带元数据筛选的搜索失败", e);
            return QueryResult.builder()
                    .query(query)
                    .documents(new ArrayList<>())
                    .executionTime(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    @Override
    public int importExistingData(String sourceType, int batchSize) {
        // 这个方法需要连接到实体存储库，导入现有数据
        // 具体实现将在具体的业务服务中进行
        throw new UnsupportedOperationException("该方法需要在具体业务服务中实现");
    }
}
