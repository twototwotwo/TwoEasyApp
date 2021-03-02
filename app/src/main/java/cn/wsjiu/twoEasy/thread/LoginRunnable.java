package cn.wsjiu.twoEasy.thread;

import android.os.Handler;

import com.alibaba.fastjson.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import cn.wsjiu.twoEasy.entity.User;
import cn.wsjiu.twoEasy.result.Result;
import cn.wsjiu.twoEasy.result.ResultCode;

public class LoginRunnable extends AbstractAsynRunnable {

    private final String accountName;
    private final String password;

    public LoginRunnable(String url, Handler handler, String accountName, String password) {
        super(url, handler);
        this.url = url;
        this.accountName = accountName;
        this.password = password;
    }

    @Override
    public void run(){
        Result<User> result;
        try {
            JSONObject dataJSONObject = new JSONObject();
            String ACCOUNT_NAME = "accountName";
            dataJSONObject.put(ACCOUNT_NAME, accountName);
            String PASSWORD = "password";
            dataJSONObject.put(PASSWORD, password);
            byte[] formData = dataJSONObject.toJSONString().getBytes();
            URL url = new URL(this.url);
            HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.setRequestMethod("POST");
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
            InputStream in = httpURLConnection.getInputStream();
            byte[] response = new byte[1024];
            OutputStream responseOut = new ByteArrayOutputStream();
            int len;
            while((len = in.read(response)) != -1) {
                responseOut.write(response, 0, len);
            }
            in.close();
            JSONObject responseJSONObject = JSONObject.parseObject(responseOut.toString());
            if(responseJSONObject.getBoolean(SUCCESS)) {
                User user = responseJSONObject.getObject(DATA, User.class);
                result = new Result<>(user);
            }else {
                result = new Result<>(responseJSONObject.getInteger(CODE), responseJSONObject.getString(MSG));
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            result = new Result<>(ResultCode.LOGIN_ERROR.getCode(), "登录失败，服务器异常");
        } catch (IOException e) {
            e.printStackTrace();
            result = new Result<>(ResultCode.LOGIN_ERROR.getCode(), "登录失败，网络异常");
        }
        handle(result);
    }
}
