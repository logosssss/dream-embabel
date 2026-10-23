package com.zhu.embabel.knowledge;

import com.embabel.agent.rag.ingestion.ContentChunker;
import com.embabel.agent.rag.ingestion.TikaHierarchicalContentReader;
import com.embabel.agent.rag.ingestion.policy.NeverRefreshExistingDocumentContentPolicy;
import com.embabel.agent.rag.ingestion.transform.AddTitlesChunkTransformer;
import com.embabel.agent.rag.lucene.LuceneSearchOperations;
import com.embabel.agent.rag.model.Chunk;
import com.embabel.agent.rag.tools.ResultExpanderTools;
import com.embabel.agent.rag.tools.SearchDefaults;
import com.embabel.agent.rag.tools.ToolishRag;
import com.embabel.common.core.types.SimilarityResult;
import com.embabel.common.core.types.TextSimilaritySearchRequest;
import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * 栏目规范放在进程内 Lucene。Markdown 由 Tika 解析成章节，
 * 标题经 {@link AddTitlesChunkTransformer} 写进 chunk。检索阈值用 {@link SearchDefaults}。
 */
@Component
public class StyleGuide {

    public static final String NAME = "styleGuide";
    /** 不是 http(s)，{@code parseUrl} 会改走 classpath，测试不会出网。 */
    public static final String RESOURCE = "classpath:style-guide.md";
    /**
     * 默认 1500 字会把一整节收成一个 chunk。收成 20 字，专栏才能拆出相邻片段。
     */
    private static final int CHUNK_CHARS = 20;

    private final LuceneSearchOperations operations;
    private final ToolishRag reference;

    public StyleGuide() {
        this.operations = LuceneSearchOperations.withName("style-guide")
                .withEmbeddingService(new HashEmbeddingService())
                .withChunkTransformer(AddTitlesChunkTransformer.INSTANCE)
                .withChunkerConfig(new ContentChunker.Config(CHUNK_CHARS, 0, 8))
                .build();
        NeverRefreshExistingDocumentContentPolicy.INSTANCE.ingestUriIfNeeded(
                operations, new TikaHierarchicalContentReader(), RESOURCE);
        this.reference = new ToolishRag(NAME, "栏目规范与资料", operations)
                .withSearchDefaults(SearchDefaults.DEFAULT);
    }

    public ToolishRag reference() {
        return reference;
    }

    public List<Chunk> chunks() {
        return operations.findAll();
    }

    public String broaden(String chunkId) {
        return new ResultExpanderTools(operations).broadenChunk(chunkId, 1);
    }

    public String zoom(String chunkId) {
        return new ResultExpanderTools(operations).zoomOut(chunkId);
    }

    public String search(String query) {
        if (query == null || query.isBlank()) {
            return "没有可检索的资料。";
        }
        List<SimilarityResult<Chunk>> hits = operations.textSearch(
                TextSimilaritySearchRequest.create(
                        query, SearchDefaults.DEFAULT.getTextSimilarityThreshold(), 2),
                Chunk.class);
        if (hits.isEmpty()) {
            return "没有可检索的资料。";
        }
        return hits.stream().map(hit -> hit.getMatch().getText()).collect(Collectors.joining("\n"));
    }

    @PreDestroy
    public void close() {
        operations.close();
    }
}
