package com.zhu.embabel.maths;

import java.util.List;

/**
 * math 工具组的名单。visible 是模型一开始看得到的入口，tools 是展开之后的运算。
 */
public record MathToolCatalog(String group, String visible, List<MathToolLine> tools) {

    public record MathToolLine(String name, String description) {
    }
}
