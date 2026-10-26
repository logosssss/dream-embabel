package com.zhu.embabel.mcp;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapperSupplier;
import io.modelcontextprotocol.spec.McpSchema;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

/**
 * 用当前 JDK 拉起 {@link EchoMcpServer}，再建立 stdio 客户端。调用方负责 close。
 */
public final class EchoMcpClientFactory {

    private EchoMcpClientFactory() {
    }

    public static McpSyncClient start() throws IOException {
        Path argsFile = Files.createTempFile("dream-embabel-echo-", ".args");
        // Java 参数文件在引号里把 \t、\n 当转义。Windows 路径改成斜杠，避免 \target 被吃掉。
        String classpath = System.getProperty("java.class.path").replace('\\', '/').replace("\"", "\\\"");
        Files.writeString(argsFile, "-cp\n\"" + classpath + "\"\n" + EchoMcpServer.class.getName() + "\n");
        String executable = System.getProperty("os.name", "").toLowerCase().contains("win") ? "java.exe" : "java";
        String java = Path.of(System.getProperty("java.home"), "bin", executable).toString();
        ServerParameters parameters = ServerParameters.builder(java)
                .args("@" + argsFile.toAbsolutePath())
                .build();
        McpSyncClient client = McpClient.sync(new StdioClientTransport(parameters, new JacksonMcpJsonMapperSupplier().get()))
                .clientInfo(new McpSchema.Implementation("dream-embabel", "0.1.0"))
                .requestTimeout(Duration.ofSeconds(30))
                .initializationTimeout(Duration.ofSeconds(30))
                .build();
        try {
            client.initialize();
        } catch (RuntimeException error) {
            client.close();
            throw error;
        }
        return client;
    }
}
