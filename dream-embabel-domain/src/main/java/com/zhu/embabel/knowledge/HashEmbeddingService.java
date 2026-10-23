package com.zhu.embabel.knowledge;

import com.embabel.common.ai.model.EmbeddingService;
import com.embabel.common.ai.model.PricingModel;
import java.util.ArrayList;
import java.util.List;

/**
 * 把本地字符哈希交给 Embabel 的嵌入接口。不访问模型。
 */
public final class HashEmbeddingService implements EmbeddingService {

    @Override
    public float[] embed(String text) {
        return HashEmbeddingModel.vector(text);
    }

    @Override
    public List<float[]> embed(List<String> texts) {
        List<float[]> vectors = new ArrayList<>();
        for (String text : texts) {
            vectors.add(embed(text));
        }
        return vectors;
    }

    @Override
    public int getDimensions() {
        return HashEmbeddingModel.DIMENSIONS;
    }

    @Override
    public String getName() {
        return "hash-embedding";
    }

    @Override
    public String getProvider() {
        return "local";
    }

    @Override
    public PricingModel getPricingModel() {
        return PricingModel.getALL_YOU_CAN_EAT();
    }
}
