package com.zhu.embabel.knowledge;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class StyleGuideDemoTest {

    @Test
    void searchesLocalNotesWithoutMilvus() {
        StyleGuide guide = new StyleGuide();
        try {
            StyleGuideDemo demo = new StyleGuideDemo(guide, false);
            StyleGuideDemo.StyleGuideStatus status = demo.status();
            assertFalse(status.milvus());
            assertTrue(status.chunks() > 0);
            assertTrue(demo.search("春草").contains("春草发芽"));
        } finally {
            guide.close();
        }
    }
}
