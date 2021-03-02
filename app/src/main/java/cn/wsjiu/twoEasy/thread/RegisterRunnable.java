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

public class RegisterRunnable extends AbstractAsynRunnable {
    protected final String ACCESS_TOKEN = "accessToken";

    private final String accountName;
    private final String password;
    private final String accessToken;

    public RegisterRunnable(String url, Handler handler, String accountName, String password, String accessToken) {
        super(url, handler);
        this.accountName = accountName;
        this.password = password;
        this.accessToken = accessToken;
    }

    @Override
    public void run() {
        Result<User> result;
        try {
            JSONObject dataJSONObject = new JSONObject();
            String ACCOUNT_NAME = "accountName";
            dataJSONObject.put(ACCOUNT_NAME, accountName);
            String PASSWORD = "password";
            dataJSONObject.put(PASSWORD, password);
            dataJSONObject.put(ACCESS_TOKEN, accessToken);
            byte[] formData = dataJSONObject.toJSONString().getBytes();
            URL url = new URL(this.url);
            HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("content-type", "application/json");
            httpURLConnection.setRequestProperty("content-length", String.valueOf(formData.length));
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
            result = new Result<>(ResultCode.REGISTER_ERROR.getCode(), "注册失败，服务器异常");
        } catch (IOException e) {
            e.printStackTrace();
            result = new Result<>(ResultCode.REGISTER_ERROR.getCode(), "注册失败，网络异常");
        }
        handle(result);
    }
}
