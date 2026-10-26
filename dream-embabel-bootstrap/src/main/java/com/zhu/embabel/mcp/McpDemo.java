package com.zhu.embabel.mcp;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 演示页查看已经连上的 MCP 客户端，并列出它们的工具。
 */
@Component
public class McpDemo {

    private final List<ToolSource> sources;

    @Autowired
    public McpDemo(ObjectProvider<McpSyncClient> clients) {
        this(clients.orderedStream().map(McpDemo::source).toList());
    }

    McpDemo(List<ToolSource> sources) {
        this.sources = sources;
    }

    public McpStatus status() {
        if (sources.isEmpty()) {
            return new McpStatus(List.of(), "没有 MCP 客户端。echo 默认关闭，也没有配置远程地址。");
        }
        List<McpClientStatus> clients = new ArrayList<>();
        for (ToolSource source : sources) {
            clients.add(new McpClientStatus(source.name(), true));
        }
        return new McpStatus(clients, null);
    }

    public List<McpTools> tools() {
        if (sources.isEmpty()) {
            return List.of(new McpTools(null, List.of(), "没有 MCP 客户端。"));
        }
        List<McpTools> listed = new ArrayList<>();
        for (ToolSource source : sources) {
            try {
                listed.add(new McpTools(source.name(), source.toolNames(), null));
            } catch (RuntimeException ex) {
                listed.add(new McpTools(source.name(), List.of(), ex.getMessage()));
            }
        }
        return listed;
    }

    private static ToolSource source(McpSyncClient client) {
        return new ToolSource() {
            @Override
            public String name() {
                McpSchema.InitializeResult initialized = client.getCurrentInitializationResult();
                if (initialized == null || initialized.serverInfo() == null || initialized.serverInfo().name() == null) {
                    return "mcp";
                }
                return initialized.serverInfo().name();
            }

            @Override
            public List<String> toolNames() {
                List<String> names = new ArrayList<>();
                for (McpSchema.Tool tool : client.listTools().tools()) {
                    names.add(tool.name());
                }
                return names;
            }
        };
    }

    interface ToolSource {
        String name();

        List<String> toolNames();
    }

    public record McpStatus(List<McpClientStatus> clients, String message) {
    }

    public record McpClientStatus(String name, boolean connected) {
    }

    public record McpTools(String name, List<String> tools, String message) {
    }
}
