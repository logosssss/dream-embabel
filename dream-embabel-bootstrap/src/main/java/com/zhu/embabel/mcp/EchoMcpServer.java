package com.zhu.embabel.mcp;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapperSupplier;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import java.util.List;
import java.util.Map;
import org.slf4j.LoggerFactory;

/**
 * 本机 stdio MCP 服务。只提供 echo，把 text 原样返回。标准输出只给协议，日志走错误输出。
 */
public final class EchoMcpServer {

    public static final String TOOL_NAME = "echo";

    private EchoMcpServer() {
    }

    public static void main(String[] args) throws InterruptedException {
        logsToStderr();
        McpSyncServer server = McpServer.sync(new StdioServerTransportProvider(new JacksonMcpJsonMapperSupplier().get()))
                .serverInfo("dream-embabel-echo", "0.1.0")
                .toolCall(echoTool(), EchoMcpServer::echo)
                .build();
        Runtime.getRuntime().addShutdownHook(new Thread(server::close));
        Thread.currentThread().join();
    }

    /**
     * 这个进程的标准输出只留给 MCP。Logback 默认打到标准输出，会把协议行弄坏。
     */
    private static void logsToStderr() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.reset();
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("%d{HH:mm:ss.SSS} %-5level %logger{30} - %msg%n");
        encoder.start();
        ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<>();
        appender.setContext(context);
        appender.setTarget("System.err");
        appender.setEncoder(encoder);
        appender.start();
        Logger root = context.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);
        root.addAppender(appender);
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
