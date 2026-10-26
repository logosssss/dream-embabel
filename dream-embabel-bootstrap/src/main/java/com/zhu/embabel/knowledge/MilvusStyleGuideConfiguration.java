package com.zhu.embabel.knowledge;

import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.MutationResult;
import io.milvus.param.ConnectParam;
import io.milvus.param.MetricType;
import io.milvus.param.R;
import io.milvus.param.dml.DeleteParam;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 默认不创建客户端。打开后用本地字符哈希写入 Milvus，维度与 {@link HashEmbeddingModel} 一致。
 */
@Configuration
@ConditionalOnProperty(prefix = "dreamembabel.rag.milvus", name = "enabled", havingValue = "true")
public class MilvusStyleGuideConfiguration {

    @Bean(destroyMethod = "close")
    MilvusServiceClient styleGuideMilvusClient(
            @Value("${dreamembabel.rag.milvus.host:127.0.0.1}") String host,
            @Value("${dreamembabel.rag.milvus.port:19530}") int port,
            @Value("${dreamembabel.rag.milvus.username:}") String username,
            @Value("${dreamembabel.rag.milvus.password:}") String password) {
        ConnectParam.Builder connection = ConnectParam.newBuilder()
                .withHost(host)
                .withPort(port)
                .withConnectTimeout(10, TimeUnit.SECONDS);
        if (!username.isBlank() || !password.isBlank()) {
            if (username.isBlank() || password.isBlank()) {
                throw new IllegalStateException("Milvus 账号和密码需要一起配置");
            }
            connection.withAuthorization(username, password);
        }
        return new MilvusServiceClient(connection.build());
    }

    @Bean
    VectorStore styleGuideMilvus(
            MilvusServiceClient styleGuideMilvusClient,
            @Value("${dreamembabel.rag.milvus.database:default}") String database,
            @Value("${dreamembabel.rag.milvus.collection:style_guide}") String collection) {
        HashEmbeddingModel embedding = new HashEmbeddingModel();
        MilvusVectorStore store = MilvusVectorStore.builder(styleGuideMilvusClient, embedding)
                .databaseName(database)
                .collectionName(collection)
                .embeddingDimension(embedding.dimensions())
                .metricType(MetricType.COSINE)
                .initializeSchema(true)
                .build();
        return new RefreshingMilvusStore(store, styleGuideMilvusClient, database, collection);
    }

    /**
     * 每次启动先清空集合再写入。Lucene 片段 id 每次进程都不同，不清空会留下旧向量。
     */
    static final class RefreshingMilvusStore implements VectorStore, InitializingBean {

        private final MilvusVectorStore store;
        private final MilvusServiceClient client;
        private final String database;
        private final String collection;

        RefreshingMilvusStore(
                MilvusVectorStore store, MilvusServiceClient client, String database, String collection) {
            this.store = store;
            this.client = client;
            this.database = database;
            this.collection = collection;
        }

        @Override
        public void afterPropertiesSet() throws Exception {
            store.afterPropertiesSet();
            R<MutationResult> status = client.delete(DeleteParam.newBuilder()
                    .withDatabaseName(database)
                    .withCollectionName(collection)
                    .withExpr(MilvusVectorStore.DOC_ID_FIELD_NAME + " != \"\"")
                    .build());
            if (status.getException() != null) {
                throw new IllegalStateException("清空 Milvus 栏目规范失败", status.getException());
            }
        }

        @Override
        public void add(List<Document> documents) {
            store.add(documents);
        }

        @Override
        public void delete(List<String> idList) {
            store.delete(idList);
        }

        @Override
        public void delete(Filter.Expression filterExpression) {
            store.delete(filterExpression);
        }

        @Override
        public List<Document> similaritySearch(SearchRequest request) {
            return store.similaritySearch(request);
        }

        @Override
        public <T> Optional<T> getNativeClient() {
            return store.getNativeClient();
        }
    }
}
