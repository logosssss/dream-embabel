package com.zhu.embabel.mcp;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 演示页查看 MCP 是否连上，并列出工具。
 */
@RestController
@RequestMapping({"/api/mcp", "/api/mcp/"})
public class McpStatusController {

    private final McpDemo mcp;

    public McpStatusController(McpDemo mcp) {
        this.mcp = mcp;
    }

    @GetMapping
    public McpDemo.McpStatus status() {
        return mcp.status();
    }

    @PostMapping({"/tools", "/tools/"})
    public List<McpDemo.McpTools> tools() {
        return mcp.tools();
    }
}
