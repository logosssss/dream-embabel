package com.zhu.embabel.knowledge;

import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * 演示页读取栏目规范。全文检索走 Lucene，不访问模型。
 */
@Component
public class StyleGuideDemo {

    private final StyleGuide guide;
    private final boolean milvus;

    @Autowired
    public StyleGuideDemo(
            StyleGuide guide, @Qualifier("styleGuideMilvus") ObjectProvider<VectorStore> vectors) {
        this(guide, vectors.getIfAvailable() != null);
    }

    StyleGuideDemo(StyleGuide guide, boolean milvus) {
        this.guide = guide;
        this.milvus = milvus;
    }

    public StyleGuideStatus status() {
        return new StyleGuideStatus(milvus, guide.chunks().size(), StyleGuide.RESOURCE);
    }

    public String search(String query) {
        return guide.search(query);
    }

    public record StyleGuideStatus(boolean milvus, int chunks, String resource) {
    }
}
