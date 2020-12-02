package cn.wsjiu.easychange.thread;

import com.alibaba.fastjson.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import cn.wsjiu.easychange.result.Result;

public class DataUploadCallable<D , R> extends AbstractCallable{
    private final String METHOD = "POST";
    private final String CONTENT_TYPE_KEY = "content-type";
    private final String CONTENT_TYPE_JSON = "application/json";
    private final String CONTENT_LENGTH_KEY = "content-length";
    private final int HTTP_SUCCESS_CODE = 200;

    D data;
    public DataUploadCallable(String url, D data) {
        this.url = url;
        this.data = data;
    }
    @Override
    public  Result<R> call() throws Exception {
        URL httpUrl = new URL(this.url);
        HttpURLConnection httpURLConnection = (HttpURLConnection) httpUrl.openConnection();
        httpURLConnection.setRequestMethod(METHOD);
        byte[] formData = JSONObject.toJSONString(data).getBytes();
        httpURLConnection.setRequestProperty(CONTENT_TYPE_KEY, CONTENT_TYPE_JSON);
        httpURLConnection.setRequestProperty(CONTENT_LENGTH_KEY, String.valueOf(formData.length));
        httpURLConnection.setConnectTimeout(TIME_OUT);
        OutputStream out = httpURLConnection.getOutputStream();
        out.write(formData);
        out.flush();
        out.close();
        int statusCode = httpURLConnection.getResponseCode();
        if(HTTP_SUCCESS_CODE != statusCode) {
            return new Result<R>(statusCode, httpURLConnection.getResponseMessage());
        }
        InputStream in = httpURLConnection.getInputStream();
        OutputStream bufferOut = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while((len = in.read(buffer)) != -1) {
            bufferOut.write(buffer, 0 , len);
        }
        String responseData = bufferOut.toString();
        Result<R> result = JSONObject.parseObject(responseData, Result.class);
        return result;
    }
}
