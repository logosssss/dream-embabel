package com.zhu.embabel.review;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

/**
 * 框架自带的 x 仍可用。这条命令和 POST /api/reviews 走同一次调用。
 */
@ShellComponent
public class ContentReviewCommand {

    private final ContentReviewService reviews;

    public ContentReviewCommand(ContentReviewService reviews) {
        this.reviews = reviews;
    }

    @ShellMethod(key = "review", value = "编程式调用内容审校 Agent")
    public String review(@ShellOption(help = "文章主题") String topic) {
        ContentReviewResponse response = reviews.review(topic);
        if ("COMPLETED".equals(response.status())) {
            return response.content();
        }
        return response.message();
    }
}
