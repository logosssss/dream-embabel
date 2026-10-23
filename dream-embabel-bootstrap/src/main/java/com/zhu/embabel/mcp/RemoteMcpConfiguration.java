package com.zhu.embabel.mcp;

import com.embabel.agent.core.ToolGroupDescription;
import com.embabel.agent.core.ToolGroupPermission;
import com.embabel.agent.tools.mcp.McpToolGroup;
import com.embabel.agent.tools.mcp.ToolCallContextMcpMetaConverter;
import io.modelcontextprotocol.client.McpSyncClient;
import java.util.List;
import java.util.Set;
import kotlin.jvm.functions.Function1;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 默认不设置地址，因此不会连接。地址是服务根路径，请求会发到该根路径下的 endpoint。
 */
@Configuration
@ConditionalOnProperty(prefix = "dreamembabel.mcp.remote", name = "url")
public class RemoteMcpConfiguration {

    @Bean(destroyMethod = "close")
    @RemoteMcp
    McpSyncClient remoteMcpClient(
            @Value("${dreamembabel.mcp.remote.url}") String url,
            @Value("${dreamembabel.mcp.remote.endpoint:/mcp}") String endpoint) {
        return RemoteMcpClientFactory.start(url, endpoint);
    }

    @Bean
    McpToolGroup remoteMcpTools(@RemoteMcp McpSyncClient remoteMcpClient) {
        return new McpToolGroup(
                ToolGroupDescription.create("按配置的地址读取外部资料", "remote-notes"),
                "remote",
                "remote-notes",
                Set.of(ToolGroupPermission.INTERNET_ACCESS),
                List.of(remoteMcpClient),
                new Function1<ToolCallback, Boolean>() {
                    @Override
                    public Boolean invoke(ToolCallback callback) {
                        return Boolean.TRUE;
                    }
                },
                ToolCallContextMcpMetaConverter.noOp());
    }
}
