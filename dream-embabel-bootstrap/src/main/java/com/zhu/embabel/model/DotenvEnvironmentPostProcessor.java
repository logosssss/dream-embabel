package com.zhu.embabel.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * 读取工作目录或上一级目录的 {@code .env}。进程里已经存在的环境变量优先。
 * 必须早于配置文件加载，{@code application.properties} 里的占位符才能读到这些值。
 */
public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Path file = find();
        if (file == null) {
            return;
        }
        Map<String, Object> values = read(file);
        if (values.isEmpty()) {
            return;
        }
        environment.getPropertySources().addLast(new MapPropertySource("dreamembabelDotenv", values));
    }

    @Override
    public int getOrder() {
        return ConfigDataEnvironmentPostProcessor.ORDER - 2;
    }

    private static Path find() {
        Path start = Path.of(System.getProperty("user.dir", ".")).toAbsolutePath();
        Path here = start.resolve(".env");
        if (Files.isRegularFile(here)) {
            return here;
        }
        Path parent = start.getParent();
        if (parent == null) {
            return null;
        }
        Path above = parent.resolve(".env");
        return Files.isRegularFile(above) ? above : null;
    }

    private static Map<String, Object> read(Path file) {
        Map<String, Object> values = new LinkedHashMap<>();
        List<String> lines;
        try {
            lines = Files.readAllLines(file);
        } catch (IOException ex) {
            return values;
        }
        for (String line : lines) {
            String text = line.trim();
            if (text.isEmpty() || text.startsWith("#")) {
                continue;
            }
            if (text.startsWith("export ")) {
                text = text.substring("export ".length()).trim();
            }
            int split = text.indexOf('=');
            if (split <= 0) {
                continue;
            }
            String key = text.substring(0, split).trim();
            String value = unquote(text.substring(split + 1).trim());
            if (!key.isEmpty() && !value.isEmpty()) {
                values.put(key, value);
            }
        }
        return values;
    }

    private static String unquote(String value) {
        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }
}
