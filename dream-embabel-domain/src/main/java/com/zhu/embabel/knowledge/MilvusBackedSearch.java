package com.zhu.embabel.knowledge;

import com.embabel.agent.rag.lucene.LuceneSearchOperations;
import com.embabel.agent.rag.model.ContentElement;
import com.embabel.agent.rag.model.Retrievable;
import com.embabel.agent.rag.service.CoreSearchOperations;
import com.embabel.agent.rag.service.ResultExpander;
import com.embabel.agent.rag.service.spring.SpringVectorStoreVectorSearch;
import com.embabel.common.core.types.SimilarityResult;
import com.embabel.common.core.types.TextSimilaritySearchRequest;
import java.util.List;

/**
 * 向量检索交给 Spring AI 的向量库，全文和相邻片段仍交给 Lucene。
 * {@code ToolishRag} 按接口暴露工具，所以两边都要实现。
 */
final class MilvusBackedSearch implements CoreSearchOperations, ResultExpander {

    private final LuceneSearchOperations lucene;
    private final SpringVectorStoreVectorSearch vectors;

    MilvusBackedSearch(LuceneSearchOperations lucene, SpringVectorStoreVectorSearch vectors) {
        this.lucene = lucene;
        this.vectors = vectors;
    }

    @Override
    public boolean supportsType(String type) {
        return lucene.supportsType(type);
    }

    @Override
    public <T extends Retrievable> List<SimilarityResult<T>> vectorSearch(
            TextSimilaritySearchRequest request, Class<T> type) {
        return vectors.vectorSearch(request, type);
    }

    @Override
    public <T extends Retrievable> List<SimilarityResult<T>> textSearch(
            TextSimilaritySearchRequest request, Class<T> type) {
        return lucene.textSearch(request, type);
    }

    @Override
    public List<ContentElement> expandResult(String id, ResultExpander.Method method, int count) {
        return lucene.expandResult(id, method, count);
    }
}
