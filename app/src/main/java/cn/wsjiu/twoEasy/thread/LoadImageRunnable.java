package cn.wsjiu.twoEasy.thread;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import cn.wsjiu.twoEasy.result.Result;
import cn.wsjiu.twoEasy.result.ResultCode;
import cn.wsjiu.twoEasy.util.ImageUtils;

public class LoadImageRunnable extends AbstractAsynRunnable {
    private static Map<String, List<Handler>> handlerMap = new ConcurrentHashMap<>(8);

    public LoadImageRunnable(String url, Handler handler) {
        super(url, handler);
    }
    @Override
    public void run() {
        Result<Bitmap> result;
        synchronized (LoadImageRunnable.class) {
            if(handlerMap.containsKey(url)) {
                  List<Handler> handlerList = handlerMap.get(url);
                  if(handlerList != null) {
                      handlerList.add(handler);
                      return;
                  }
            }
            List<Handler> handlerList = new ArrayList<>();
            handlerList.add(handler);
            handlerMap.put(url, handlerList);
        }
        try {
            Bitmap bitmap = ImageUtils.getBitmapFromPool(url);
            if(bitmap ==null) {
                URL url = new URL(this.url);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                String METHOD = "GET";
                httpURLConnection.setRequestMethod(METHOD);
                InputStream in = httpURLConnection.getInputStream();
                bitmap = BitmapFactory.decodeStream(in);
            }
            result = new Result<>(bitmap);
            ImageUtils.putBitmapToPool(this.url, bitmap);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            result = new Result<>(ResultCode.AUTHORIZE_ERROR.getCode(), "数据加载失败，服务器异常");
        } catch (IOException e) {
            e.printStackTrace();
            result = new Result<>(ResultCode.AUTHORIZE_ERROR.getCode(), "数据加载失败， 网络异常");
        } catch (Exception e) {
            e.printStackTrace();
            result = new Result<>(ResultCode.AUTHORIZE_ERROR.getCode(), "数据加载失败， " + e.toString());
        }
        List<Handler> handlerList;
        synchronized (LoadImageRunnable.class) {
            handlerList =  handlerMap.remove(url);
        }
        if(handlerList == null) {
            handle(result);
        }else {
            for(Handler handler : handlerList) {
                Message message = new Message();
                message.obj = result;
                handler.sendMessage(message);
            }
        }
    }
}
