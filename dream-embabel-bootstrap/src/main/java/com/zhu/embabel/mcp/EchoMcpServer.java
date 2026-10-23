package com.zhu.embabel.mcp;

import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapperSupplier;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import java.util.List;
import java.util.Map;

/**
 * 本机 stdio MCP 服务。只提供 echo，把 text 原样返回。标准输出只给协议，日志走错误输出。
 */
public final class EchoMcpServer {

    public static final String TOOL_NAME = "echo";

    private EchoMcpServer() {
    }

    public static void main(String[] args) throws InterruptedException {
        McpSyncServer server = McpServer.sync(new StdioServerTransportProvider(new JacksonMcpJsonMapperSupplier().get()))
                .serverInfo("dream-embabel-echo", "0.1.0")
                .toolCall(echoTool(), EchoMcpServer::echo)
                .build();
        Runtime.getRuntime().addShutdownHook(new Thread(server::close));
        Thread.currentThread().join();
    }

    private static McpSchema.Tool echoTool() {
        return McpSchema.Tool.builder()
                .name(TOOL_NAME)
                .description("把 text 原样返回")
                .inputSchema(Map.of(
                        "type", "object",
                        "properties", Map.of("text", Map.of("type", "string", "description", "要原样返回的文本")),
                        "required", List.of("text")))
                .build();
    }

    private static McpSchema.CallToolResult echo(
            io.modelcontextprotocol.server.McpSyncServerExchange exchange, McpSchema.CallToolRequest request) {
        Object text = request.arguments() == null ? null : request.arguments().get("text");
        return McpSchema.CallToolResult.builder()
                .addTextContent(text == null ? "" : text.toString())
                .isError(false)
                .build();
    }
}
