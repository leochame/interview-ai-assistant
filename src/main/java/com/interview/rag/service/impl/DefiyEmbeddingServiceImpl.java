package com.interview.rag.service.impl;

import com.defiy.core.embedding.EmbeddingClient;
import com.interview.rag.service.EmbeddingService;
import com.interview.rag.util.SimilarityCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 嵌入服务实现
 * 基于Defiy的OpenAI嵌入客户端
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DefiyEmbeddingServiceImpl implements EmbeddingService {

    private final EmbeddingClient embeddingClient;
    private final SimilarityCalculator similarityCalculator;

    @Override
    public List<Float> createEmbedding(String text) {
        try {
            return embeddingClient.embed(text);
        } catch (Exception e) {
            log.error("创建文本嵌入失败", e);
            throw new RuntimeException("创建文本嵌入失败", e);
        }
    }

    @Override
    public List<List<Float>> createEmbeddings(List<String> texts) {
        try {
            return embeddingClient.embedBatch(texts);
        } catch (Exception e) {
            log.error("批量创建文本嵌入失败", e);
            throw new RuntimeException("批量创建文本嵌入失败", e);
        }
    }

    @Override
    public float calculateSimilarity(List<Float> embedding1, List<Float> embedding2) {
        return similarityCalculator.cosineSimilarity(embedding1, embedding2);
    }
}