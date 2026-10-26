package com.zhu.embabel.http;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class RunStatusTest {

    @Test
    void completedIsOkAndWaitingIsAccepted() {
        assertEquals(HttpStatus.OK, RunStatus.of("COMPLETED"));
        assertEquals(HttpStatus.ACCEPTED, RunStatus.of("WAITING"));
    }

    @Test
    void anythingElseConflicts() {
        assertEquals(HttpStatus.CONFLICT, RunStatus.of("FAILED"));
        assertEquals(HttpStatus.CONFLICT, RunStatus.of(null));
    }
}
