package com.zhu.embabel.mcp;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * 空地址在建立连接之前就被拒绝。不访问网络。
 */
class RemoteMcpClientFactoryTest {

    @Test
    void blankUrlDoesNotOpenAConnection() {
        assertThrows(IllegalArgumentException.class, () -> RemoteMcpClientFactory.start("  ", "/mcp"));
    }
}
