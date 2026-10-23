package com.zhu.embabel.agent;

import com.zhu.embabel.domain.ReviewNote;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Component;

/**
 * 进程之间保留最近一次审校。Embabel 1.5.3 没有单独的领域记忆类型，这一格由应用自己持有。
 */
@Component
public class ReviewMemory {

    private final AtomicReference<ReviewNote> latest = new AtomicReference<>();

    public ReviewNote latest() {
        return latest.get();
    }

    public void remember(ReviewNote note) {
        latest.set(note);
    }
}
