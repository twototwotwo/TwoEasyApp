package cn.wsjiu.twoEasy.thread;

import android.os.Handler;
import android.os.Message;
import cn.wsjiu.twoEasy.result.Result;

/**
 * 异步网络io线程任务接口
 */
public abstract class AbstractAsynRunnable implements Runnable {
    protected final int TIME_OUT = 1000;
    protected final String SUCCESS = "success";
    protected final String DATA = "data";
    protected final String MSG = "msg";
    protected final String CODE = "code";

    /**
     * 网络io的url
     */
    protected String url;
    /**
     * 异步线程执行结束后回调的handler处理器
     */
    protected Handler handler;

    public AbstractAsynRunnable(String url, Handler handler) {
        this.url = url;
        this.handler = handler;
    }

    /**
     * 回调handler
     * @param result 网络请求的结果
     */
    protected void handle(Result result) {
        Message message = new Message();
        message.obj = result;
        handler.sendMessage(message);
    }
}
