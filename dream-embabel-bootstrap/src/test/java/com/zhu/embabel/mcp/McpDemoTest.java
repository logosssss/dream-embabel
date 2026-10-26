package com.zhu.embabel.mcp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class McpDemoTest {

    @Test
    void reportsNoClientUntilOneIsConfigured() {
        McpDemo demo = new McpDemo(List.of());
        assertTrue(demo.status().clients().isEmpty());
        assertTrue(demo.status().message().contains("没有 MCP"));
        assertEquals("没有 MCP 客户端。", demo.tools().get(0).message());
    }

    @Test
    void listsToolsFromAConnectedClient() {
        McpDemo demo = new McpDemo(List.of(new McpDemo.ToolSource() {
            @Override
            public String name() {
                return "echo";
            }

            @Override
            public List<String> toolNames() {
                return List.of("echo");
            }
        }));
        assertEquals("echo", demo.status().clients().get(0).name());
        assertEquals(List.of("echo"), demo.tools().get(0).tools());
    }
}
