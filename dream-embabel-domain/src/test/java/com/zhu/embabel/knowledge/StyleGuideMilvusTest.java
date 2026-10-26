package com.zhu.embabel.knowledge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;

class StyleGuideMilvusTest {

    @Test
    void vectorSearchUsesTheStoreAndTextSearchStaysOnLucene() {
        RecordingStore store = new RecordingStore();
        StyleGuide guide = new StyleGuide(store);
        try {
            assertTrue(store.current.stream().anyMatch(document -> document.getText().contains("春草发芽")));
            assertTrue(store.current.stream().allMatch(document -> document.getId().length() <= StyleGuide.MILVUS_ID_LENGTH));
            assertEquals(0, store.searches);

            assertTrue(guide.search("春草").contains("春草发芽"));
            assertEquals(0, store.searches);

            String nearest = guide.nearest("春草").get(0).getMatch().getText();
            assertEquals("来自向量库", nearest);
            assertEquals(1, store.searches);
        } finally {
            guide.close();
        }
    }

    private static final class RecordingStore implements VectorStore {

        private final List<Document> current = new ArrayList<>();
        private int searches;

        @Override
        public void add(List<Document> documents) {
            current.addAll(documents);
        }

        @Override
        public void delete(List<String> idList) {
            current.removeIf(document -> idList.contains(document.getId()));
        }

        @Override
        public void delete(Filter.Expression filterExpression) {
        }

        @Override
        public List<Document> similaritySearch(SearchRequest request) {
            searches++;
            return List.of(Document.builder().id("milvus-hit").text("来自向量库").score(0.8).build());
        }

        @Override
        public <T> Optional<T> getNativeClient() {
            return Optional.empty();
        }
    }
}
