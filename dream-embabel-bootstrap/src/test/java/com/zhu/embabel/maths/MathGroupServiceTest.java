package com.zhu.embabel.maths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.embabel.agent.tools.math.MathTools;
import org.junit.jupiter.api.Test;

class MathGroupServiceTest {

    @Test
    void catalogShowsTheEntryAndTheOperationsInside() {
        MathToolCatalog catalog = MathGroupService.catalog(new MathTools());

        assertEquals("math", catalog.group());
        assertEquals("math", catalog.visible());
        assertFalse(catalog.tools().isEmpty());
        assertTrue(catalog.tools().stream().anyMatch(tool -> "add".equals(tool.name())));
        assertTrue(catalog.tools().stream().allMatch(tool -> tool.description() != null && !tool.description().isBlank()));
    }
}
