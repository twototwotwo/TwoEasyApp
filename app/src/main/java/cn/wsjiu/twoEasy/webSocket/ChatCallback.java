package cn.wsjiu.twoEasy.webSocket;

import android.util.Log;

import org.reactivestreams.Subscription;

import io.reactivex.FlowableSubscriber;
import io.reactivex.annotations.NonNull;

/**
 * @author wsjiu
 * @date 2020/12/27
 */
public class ChatCallback implements FlowableSubscriber {

    @Override
    public void onSubscribe(@NonNull Subscription s) {
        Log.d("webSokcet", "onSubscribe");
    }

    @Override
    public void onNext(Object o) {
        Log.d("webSokcet", "onNext");
    }

    @Override
    public void onError(Throwable t) {
        Log.d("webSokcet", "oneRROR");
    }

    @Override
    public void onComplete() {
        Log.d("webSokcet", "oncOMPLETE");
    }
}
