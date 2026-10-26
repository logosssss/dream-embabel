package com.zhu.embabel.agent;

/**
 * 本机 echo 工具没有注册。页面应返回 503，而不是把应用启动卡住。
 */
public class EchoUnavailableException extends IllegalStateException {

    public static final String MESSAGE = "echo 工具没有打开。";

    public EchoUnavailableException() {
        super(MESSAGE);
    }
}
