package com.zhu.embabel.persistence;

import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 演示页读取 Embabel 写入的进程快照表。没有 JDBC 时不连数据库。
 */
@Component
public class ProcessSnapshotDemo {

    private static final String LIST = """
            select process_id, parent_id, agent_name, status, version, updated_at
            from agent_process_snapshots
            order by updated_at desc
            limit 20
            """;

    private final JdbcTemplate jdbc;

    @Autowired
    public ProcessSnapshotDemo(ObjectProvider<JdbcTemplate> jdbc) {
        this(jdbc.getIfAvailable());
    }

    ProcessSnapshotDemo(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public ProcessList list() {
        if (jdbc == null) {
            return new ProcessList(false, List.of(), "没有 JDBC 数据源，进程快照留在内存。");
        }
        try {
            List<ProcessRow> rows = jdbc.query(LIST, (rs, row) -> new ProcessRow(
                    rs.getString("process_id"),
                    rs.getString("parent_id"),
                    rs.getString("agent_name"),
                    rs.getString("status"),
                    rs.getLong("version"),
                    rs.getTimestamp("updated_at").toInstant().toString()));
            return new ProcessList(true, rows, rows.isEmpty() ? "还没有已保存的进程。" : null);
        } catch (DataAccessException ex) {
            String detail = ex.getMostSpecificCause().getMessage();
            return new ProcessList(true, List.of(), "读取进程快照失败：" + detail);
        }
    }

    public record ProcessList(boolean persisted, List<ProcessRow> processes, String message) {
    }

    public record ProcessRow(
            String processId, String parentId, String agentName, String status, long version, String updatedAt) {
    }
}
