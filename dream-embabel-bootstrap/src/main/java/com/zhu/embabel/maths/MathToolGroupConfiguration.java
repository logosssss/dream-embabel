package com.zhu.embabel.maths;

import com.embabel.agent.tools.math.MathTools;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 把框架自带的数学工具组交给平台。按名字 math 才能解析到。
 */
@Configuration
public class MathToolGroupConfiguration {

    @Bean
    MathTools mathTools() {
        return new MathTools();
    }
}
