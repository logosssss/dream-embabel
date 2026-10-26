package com.zhu.embabel.knowledge;

import com.embabel.agent.rag.ingestion.ContentChunker;
import com.embabel.agent.rag.ingestion.TikaHierarchicalContentReader;
import com.embabel.agent.rag.ingestion.policy.NeverRefreshExistingDocumentContentPolicy;
import com.embabel.agent.rag.ingestion.transform.AddTitlesChunkTransformer;
import com.embabel.agent.rag.lucene.LuceneSearchOperations;
import com.embabel.agent.rag.model.Chunk;
import com.embabel.agent.rag.service.CoreSearchOperations;
import com.embabel.agent.rag.service.spring.SpringVectorStoreVectorSearch;
import com.embabel.agent.rag.tools.ResultExpanderTools;
import com.embabel.agent.rag.tools.SearchDefaults;
import com.embabel.agent.rag.tools.ToolishRag;
import com.embabel.common.core.types.SimilarityResult;
import com.embabel.common.core.types.TextSimilaritySearchRequest;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * 栏目规范先放进进程内 Lucene。Markdown 由 Tika 解析成章节，
 * 标题经 {@link AddTitlesChunkTransformer} 写进 chunk。检索阈值用 {@link SearchDefaults}。
 * 没有名为 styleGuideMilvus 的向量库时，向量检索也走 Lucene。有的话，只把向量检索换过去。
 */
@Component
public class StyleGuide {

    /** Spring AI Milvus 建表时主键最长 36，与 UUID 字符串长度相同。 */
    static final int MILVUS_ID_LENGTH = 36;

    private static final Logger log = LoggerFactory.getLogger(StyleGuide.class);

    public static final String NAME = "styleGuide";
    /** 不是 http(s)，{@code parseUrl} 会改走 classpath，测试不会出网。 */
    public static final String RESOURCE = "classpath:style-guide.md";
    /**
     * 默认 1500 字会把一整节收成一个 chunk。收成 20 字，专栏才能拆出相邻片段。
     */
    private static final int CHUNK_CHARS = 20;

    private final LuceneSearchOperations lucene;
    private final CoreSearchOperations search;
    private final ToolishRag reference;

    public StyleGuide() {
        this((VectorStore) null);
    }

    @Autowired
    public StyleGuide(@Qualifier("styleGuideMilvus") ObjectProvider<VectorStore> vectors) {
        this(vectors.getIfAvailable());
    }

    StyleGuide(VectorStore vectors) {
        this.lucene = LuceneSearchOperations.withName("style-guide")
                .withEmbeddingService(new HashEmbeddingService())
                .withChunkTransformer(AddTitlesChunkTransformer.INSTANCE)
                .withChunkerConfig(new ContentChunker.Config(CHUNK_CHARS, 0, 8))
                .build();
        NeverRefreshExistingDocumentContentPolicy.INSTANCE.ingestUriIfNeeded(
                lucene, new TikaHierarchicalContentReader(), RESOURCE);
        if (vectors == null) {
            this.search = lucene;
        } else {
            write(vectors);
            this.search = new MilvusBackedSearch(lucene, new SpringVectorStoreVectorSearch(vectors));
        }
        this.reference = new ToolishRag(NAME, "栏目规范与资料", search)
                .withSearchDefaults(SearchDefaults.DEFAULT);
    }

    public ToolishRag reference() {
        return reference;
    }

    public List<Chunk> chunks() {
        return lucene.findAll();
    }

    public String broaden(String chunkId) {
        return new ResultExpanderTools(lucene).broadenChunk(chunkId, 1);
    }

    public String zoom(String chunkId) {
        return new ResultExpanderTools(lucene).zoomOut(chunkId);
    }

    public String search(String query) {
        if (query == null || query.isBlank()) {
            return "没有可检索的资料。";
        }
        List<SimilarityResult<Chunk>> hits = lucene.textSearch(
                TextSimilaritySearchRequest.create(
                        query, SearchDefaults.DEFAULT.getTextSimilarityThreshold(), 2),
                Chunk.class);
        if (hits.isEmpty()) {
            return "没有可检索的资料。";
        }
        return hits.stream().map(hit -> hit.getMatch().getText()).collect(Collectors.joining("\n"));
    }

    List<SimilarityResult<Chunk>> nearest(String query) {
        return search.vectorSearch(
                TextSimilaritySearchRequest.create(query, 0.0, 2), Chunk.class);
    }

    private void write(VectorStore vectors) {
        List<Document> documents = new ArrayList<>();
        List<String> ids = new ArrayList<>();
        for (Chunk chunk : lucene.findAll()) {
            String text = chunk.getText();
            if (text == null || text.isBlank()) {
                continue;
            }
            if (chunk.getId().length() > MILVUS_ID_LENGTH) {
                throw new IllegalStateException("Milvus 主键最长 36，当前片段 id 超长: " + chunk.getId());
            }
            ids.add(chunk.getId());
            documents.add(new Document(chunk.getId(), text, Map.of("source", RESOURCE)));
        }
        if (documents.isEmpty()) {
            return;
        }
        vectors.delete(ids);
        vectors.add(documents);
        log.info("栏目规范已写入向量库，共 {} 段", documents.size());
    }

    @PreDestroy
    public void close() {
        lucene.close();
    }
}
