package com.zhu.embabel.guide;

import java.util.List;

/**
 * 栏目规范引用暴露给模型的工具。
 */
public record GuideToolCatalog(String reference, List<GuideToolLine> tools) {

    public record GuideToolLine(String name, String description) {
    }
}
