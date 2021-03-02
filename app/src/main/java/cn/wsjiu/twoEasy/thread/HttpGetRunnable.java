package cn.wsjiu.twoEasy.thread;

import android.os.Handler;

import com.alibaba.fastjson.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import cn.wsjiu.twoEasy.result.Result;
import cn.wsjiu.twoEasy.result.ResultCode;

public class HttpGetRunnable extends AbstractAsynRunnable{
    public HttpGetRunnable(String url, Handler handler) {
        super(url, handler);
    }

    @Override
    public void run() {
        Result result;
        try {
            URL httpUrl = new URL(url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) httpUrl.openConnection();
            httpURLConnection.setConnectTimeout(TIME_OUT);
            InputStream in = httpURLConnection.getInputStream();
            ByteArrayOutputStream dataOutStream = new ByteArrayOutputStream();
            int len = 0;
            byte[] buffer = new byte[1024];
            while((len = in.read(buffer)) != -1) {
                dataOutStream.write(buffer, 0 , len);
            }
            String data = dataOutStream.toString();
            result = JSONObject.parseObject(data, Result.class);
        } catch (IOException e) {
            e.printStackTrace();
            result = new Result<>(ResultCode.NET__ERROR);
        }
        handle(result);
    }
}
