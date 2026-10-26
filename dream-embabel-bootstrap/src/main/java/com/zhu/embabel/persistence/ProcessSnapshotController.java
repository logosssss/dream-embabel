package com.zhu.embabel.persistence;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 演示页列出已保存的 Agent 进程。
 */
@RestController
@RequestMapping({"/api/processes", "/api/processes/"})
public class ProcessSnapshotController {

    private final ProcessSnapshotDemo snapshots;

    public ProcessSnapshotController(ProcessSnapshotDemo snapshots) {
        this.snapshots = snapshots;
    }

    @GetMapping
    public ProcessSnapshotDemo.ProcessList list() {
        return snapshots.list();
    }
}
