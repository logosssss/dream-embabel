package com.zhu.embabel.mcp;

import com.embabel.agent.api.tool.Tool;
import com.embabel.agent.spi.support.springai.SpringAiMcpToolFactory;
import com.zhu.embabel.agent.EchoTool;
import io.modelcontextprotocol.client.McpSyncClient;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 默认关闭。打开后创作动作会多一个 echo 工具，进程是本机 stdio，不访问外网。
 */
@Configuration
@ConditionalOnProperty(prefix = "dreamembabel.mcp.echo", name = "enabled", havingValue = "true")
public class EchoMcpConfiguration {

    @Bean(destroyMethod = "close")
    @EchoTool
    McpSyncClient echoMcpClient() throws Exception {
        return EchoMcpClientFactory.start();
    }

    @Bean
    @EchoTool
    Tool echoMcpTool(@EchoTool McpSyncClient echoMcpClient) {
        return new SpringAiMcpToolFactory(List.of(echoMcpClient)).requiredToolByName(EchoMcpServer.TOOL_NAME);
    }
}
