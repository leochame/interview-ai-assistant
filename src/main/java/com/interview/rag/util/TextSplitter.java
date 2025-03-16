package com.interview.rag.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文本分割工具
 * 负责将长文本分割成适合嵌入的短文本段落
 */
@Component
public class TextSplitter {

    @Value("${rag.text-splitter.chunk-size:1000}")
    private int chunkSize;
    
    @Value("${rag.text-splitter.chunk-overlap:200}")
    private int chunkOverlap;
    
    private static final Pattern SENTENCE_BOUNDARY = Pattern.compile("[.!?。！？]+\\s*");
    private static final Pattern PARAGRAPH_BOUNDARY = Pattern.compile("\\n\\s*\\n");
    
    /**
     * 按句子分割文本
     *
     * @param text 输入文本
     * @return 句子列表
     */
    public List<String> splitIntoSentences(String text) {
        if (StringUtils.isBlank(text)) {
            return new ArrayList<>();
        }
        
        List<String> sentences = new ArrayList<>();
        Matcher matcher = SENTENCE_BOUNDARY.matcher(text);
        
        int start = 0;
        while (matcher.find()) {
            String sentence = text.substring(start, matcher.end()).trim();
            if (StringUtils.isNotBlank(sentence)) {
                sentences.add(sentence);
            }
            start = matcher.end();
        }
        
        // 处理最后可能没有标点的文本
        if (start < text.length()) {
            String lastSentence = text.substring(start).trim();
            if (StringUtils.isNotBlank(lastSentence)) {
                sentences.add(lastSentence);
            }
        }
        
        return sentences;
    }
    
    /**
     * 按段落分割文本
     *
     * @param text 输入文本
     * @return 段落列表
     */
    public List<String> splitIntoParagraphs(String text) {
        if (StringUtils.isBlank(text)) {
            return new ArrayList<>();
        }
        
        List<String> paragraphs = new ArrayList<>();
        Matcher matcher = PARAGRAPH_BOUNDARY.matcher(text);
        
        int start = 0;
        while (matcher.find()) {
            String paragraph = text.substring(start, matcher.start()).trim();
            if (StringUtils.isNotBlank(paragraph)) {
                paragraphs.add(paragraph);
            }
            start = matcher.end();
        }
        
        // 处理最后一段
        if (start < text.length()) {
            String lastParagraph = text.substring(start).trim();
            if (StringUtils.isNotBlank(lastParagraph)) {
                paragraphs.add(lastParagraph);
            }
        }
        
        return paragraphs;
    }
    
    /**
     * 将文本分割成重叠的块
     *
     * @param text 输入文本
     * @return 文本块列表
     */
    public List<String> splitIntoChunks(String text) {
        if (StringUtils.isBlank(text)) {
            return new ArrayList<>();
        }
        
        // 首先按段落分割
        List<String> paragraphs = splitIntoParagraphs(text);
        
        List<String> chunks = new ArrayList<>();
        StringBuilder currentChunk = new StringBuilder();
        
        for (String paragraph : paragraphs) {
            // 如果当前段落加上已有内容超过块大小，则创建一个新块
            if (currentChunk.length() + paragraph.length() > chunkSize) {
                if (currentChunk.length() > 0) {
                    chunks.add(currentChunk.toString().trim());
                    
                    // 保留重叠部分
                    if (chunkOverlap > 0 && currentChunk.length() > chunkOverlap) {
                        String overlap = currentChunk.substring(Math.max(0, currentChunk.length() - chunkOverlap));
                        currentChunk = new StringBuilder(overlap);
                    } else {
                        currentChunk = new StringBuilder();
                    }
                }
                
                // 如果单个段落就超过了块大小，则需要进一步分割
                if (paragraph.length() > chunkSize) {
                    List<String> sentences = splitIntoSentences(paragraph);
                    StringBuilder sentenceChunk = new StringBuilder();
                    
                    for (String sentence : sentences) {
                        if (sentenceChunk.length() + sentence.length() > chunkSize) {
                            if (sentenceChunk.length() > 0) {
                                chunks.add(sentenceChunk.toString().trim());
                                
                                // 保留重叠部分
                                if (chunkOverlap > 0 && sentenceChunk.length() > chunkOverlap) {
                                    String overlap = sentenceChunk.substring(Math.max(0, sentenceChunk.length() - chunkOverlap));
                                    sentenceChunk = new StringBuilder(overlap);
                                } else {
                                    sentenceChunk = new StringBuilder();
                                }
                            }
                        }
                        
                        // 如果单个句子就超过块大小，则按字符截断
                        if (sentence.length() > chunkSize) {
                            for (int i = 0; i < sentence.length(); i += chunkSize - chunkOverlap) {
                                int end = Math.min(i + chunkSize, sentence.length());
                                chunks.add(sentence.substring(i, end).trim());
                            }
                        } else {
                            sentenceChunk.append(sentence).append(" ");
                        }
                    }
                    
                    if (sentenceChunk.length() > 0) {
                        currentChunk.append(sentenceChunk);
                    }
                } else {
                    currentChunk.append(paragraph).append("\n\n");
                }
            } else {
                currentChunk.append(paragraph).append("\n\n");
            }
        }
        
        // 添加最后一个块
        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }
        
        return chunks;
    }
    
    /**
     * 设置块大小
     */
    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }
    
    /**
     * 设置块重叠大小
     */
    public void setChunkOverlap(int chunkOverlap) {
        this.chunkOverlap = chunkOverlap;
    }
    
    /**
     * 获取当前块大小
     */
    public int getChunkSize() {
        return chunkSize;
    }
    
    /**
     * 获取当前块重叠大小
     */
    public int getChunkOverlap() {
        return chunkOverlap;
    }
}