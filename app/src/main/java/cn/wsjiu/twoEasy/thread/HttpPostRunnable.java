package cn.wsjiu.twoEasy.thread;

import android.os.Handler;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import cn.wsjiu.twoEasy.result.Result;
import cn.wsjiu.twoEasy.result.ResultCode;

public class HttpPostRunnable<D, R> extends AbstractAsynRunnable{

    D data;
    public HttpPostRunnable(String url, Handler handler, D data) {
        super(url, handler);
        this.data = data;
    }
    @Override
    public  void run(){
        Result<R> result;
        try {
            URL httpUrl = new URL(this.url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) httpUrl.openConnection();
            String METHOD = "POST";
            httpURLConnection.setRequestMethod(METHOD);
            byte[] formData = JSONObject.toJSONString(data).getBytes();
            String CONTENT_TYPE_KEY = "content-type";
            String CONTENT_TYPE_JSON = "application/json";
            httpURLConnection.setRequestProperty(CONTENT_TYPE_KEY, CONTENT_TYPE_JSON);
            String CONTENT_LENGTH_KEY = "content-length";
            httpURLConnection.setRequestProperty(CONTENT_LENGTH_KEY, String.valueOf(formData.length));
            httpURLConnection.setConnectTimeout(TIME_OUT);
            OutputStream out = httpURLConnection.getOutputStream();
            out.write(formData);
            out.flush();
            out.close();
            int statusCode = httpURLConnection.getResponseCode();
            int HTTP_SUCCESS_CODE = 200;
            if (HTTP_SUCCESS_CODE != statusCode) {
                result = new Result<R>(statusCode, httpURLConnection.getResponseMessage());
            } else {
                InputStream in = httpURLConnection.getInputStream();
                OutputStream bufferOut = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    bufferOut.write(buffer, 0, len);
                }
                String responseData = bufferOut.toString();
                result = JSONObject.parseObject(responseData, new TypeReference<Result>() {
                });
            }
        }catch (IOException e) {
            e.printStackTrace();
            result = new Result<R>(ResultCode.NET__ERROR);
        }
        handle(result);
    }
}
