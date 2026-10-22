package com.zhu.embabel;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.domain.io.UserInput;
import com.zhu.embabel.domain.FinalArticle;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

/**
 * 框架自带的 x 仍可用。这条命令演示编程式 AgentInvocation。
 */
@ShellComponent
public class ContentReviewCommand {

    private final AgentPlatform agentPlatform;

    public ContentReviewCommand(AgentPlatform agentPlatform) {
        this.agentPlatform = agentPlatform;
    }

    @ShellMethod(key = "review", value = "编程式调用内容审校 Agent")
    public String review(@ShellOption(help = "文章主题") String topic) {
        FinalArticle article = AgentInvocation.create(agentPlatform, FinalArticle.class)
                .invoke(new UserInput(topic));
        return article.getContent();
    }
}
