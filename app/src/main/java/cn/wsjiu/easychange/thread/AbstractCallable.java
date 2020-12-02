package cn.wsjiu.easychange.thread;

import java.util.concurrent.Callable;

public abstract class AbstractCallable<T> implements Callable<T> {
    protected final int TIME_OUT = 1000;
    protected final String SUCCESS = "success";
    protected final String DATA = "data";
    protected final String MSG = "msg";
    protected final String CODE = "code";

    protected String url;
}
