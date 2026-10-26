package com.zhu.embabel.persistence;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

class ProcessSnapshotDemoTest {

    @Test
    void staysInMemoryWhenJdbcIsAbsent() {
        ProcessSnapshotDemo.ProcessList list = new ProcessSnapshotDemo((JdbcTemplate) null).list();
        assertFalse(list.persisted());
        assertTrue(list.processes().isEmpty());
        assertTrue(list.message().contains("内存"));
    }
}
