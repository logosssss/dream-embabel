package com.zhu.embabel.knowledge;

import java.util.ArrayList;
import java.util.List;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;

/**
 * 用字符和相邻字的哈希做向量。只在进程内比较相似度，不访问模型。
 */
public final class HashEmbeddingModel implements EmbeddingModel {

    static final int DIMENSIONS = 64;

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        List<Embedding> embeddings = new ArrayList<>();
        int index = 0;
        for (String text : request.getInstructions()) {
            embeddings.add(new Embedding(vector(text), index++));
        }
        return new EmbeddingResponse(embeddings);
    }

    @Override
    public float[] embed(Document document) {
        return vector(document.getText());
    }

    @Override
    public int dimensions() {
        return DIMENSIONS;
    }

    static float[] vector(String text) {
        float[] values = new float[DIMENSIONS];
        if (text == null || text.isBlank()) {
            values[0] = 1f;
            return values;
        }
        int previous = 0;
        boolean first = true;
        for (int codePoint : text.codePoints().filter(ch -> !Character.isWhitespace(ch)).toArray()) {
            values[Math.floorMod(codePoint, DIMENSIONS)] += 1f;
            if (!first) {
                values[Math.floorMod(previous * 31 + codePoint, DIMENSIONS)] += 1f;
            }
            previous = codePoint;
            first = false;
        }
        float norm = 0f;
        for (float value : values) {
            norm += value * value;
        }
        norm = (float) Math.sqrt(norm);
        if (norm == 0f) {
            values[0] = 1f;
            return values;
        }
        for (int i = 0; i < values.length; i++) {
            values[i] /= norm;
        }
        return values;
    }
}
