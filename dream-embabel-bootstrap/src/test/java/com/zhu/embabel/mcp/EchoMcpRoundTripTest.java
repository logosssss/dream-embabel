package com.zhu.embabel.mcp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.embabel.agent.api.tool.Tool;
import com.embabel.agent.spi.support.springai.SpringAiMcpToolFactory;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * 拉起本机 echo 服务，经 MCP 客户端和 Embabel 的工具包装各走一次。不调用模型。
 */
class EchoMcpRoundTripTest {

    @Test
    void echoReturnsTheSameText() throws Exception {
        try (McpSyncClient client = EchoMcpClientFactory.start()) {
            McpSchema.CallToolResult raw = client.callTool(
                    new McpSchema.CallToolRequest(EchoMcpServer.TOOL_NAME, Map.of("text", "春草")));
            McpSchema.TextContent content = assertInstanceOf(McpSchema.TextContent.class, raw.content().get(0));
            assertEquals("春草", content.text());

            Tool tool = new SpringAiMcpToolFactory(List.of(client)).requiredToolByName(EchoMcpServer.TOOL_NAME);
            Tool.Result.Text wrapped = assertInstanceOf(
                    Tool.Result.Text.class, tool.call("{\"text\":\"春草\"}"));
            assertTrue(wrapped.getContent().contains("春草"));
        }
    }
}
