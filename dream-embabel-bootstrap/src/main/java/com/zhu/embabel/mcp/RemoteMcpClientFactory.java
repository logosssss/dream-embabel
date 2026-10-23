package com.zhu.embabel.mcp;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapperSupplier;
import io.modelcontextprotocol.spec.McpSchema;
import java.time.Duration;

/**
 * 用 Streamable HTTP 连接调用方给出的 MCP 根地址。调用方负责 close。
 */
public final class RemoteMcpClientFactory {

    private RemoteMcpClientFactory() {
    }

    public static McpSyncClient start(String url, String endpoint) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("dreamembabel.mcp.remote.url 不能为空");
        }
        String path = endpoint == null || endpoint.isBlank() ? "/mcp" : endpoint;
        McpSyncClient client = McpClient.sync(HttpClientStreamableHttpTransport.builder(url.strip())
                        .endpoint(path)
                        .jsonMapper(new JacksonMcpJsonMapperSupplier().get())
                        .build())
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
