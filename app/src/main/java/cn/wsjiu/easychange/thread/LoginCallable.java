package cn.wsjiu.easychange.thread;

import com.alibaba.fastjson.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import cn.wsjiu.easychange.entity.User;
import cn.wsjiu.easychange.result.Result;
import cn.wsjiu.easychange.result.ResultCode;

public class LoginCallable extends AbstractCallable {
    private final String ACCOUNT_NAME = "accountName";
    private final String PASSWORD = "password";
    private final String CONTENT_TYPE_KEY = "content-type";
    private final String CONTENT_TYPE_JSON = "application/json";
    private final String CONTENT_LENGTH_KEY = "content-length";

    private String accountName;
    private String password;

    public LoginCallable(String url, String accountName, String password) {
        super();
        this.url = url;
        this.accountName = accountName;
        this.password = password;
    }

    @Override
    public Result<User> call() throws Exception {
        try {
            JSONObject dataJSONObject = new JSONObject();
            dataJSONObject.put(ACCOUNT_NAME, accountName);
            dataJSONObject.put(PASSWORD, password);
            byte[] formData = dataJSONObject.toJSONString().getBytes();
            URL url = new URL(this.url);
            HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty(CONTENT_TYPE_KEY, CONTENT_TYPE_JSON);
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
                return new Result<User>(user);
            }else {
                return new Result<User>(responseJSONObject.getInteger(CODE), responseJSONObject.getString(MSG));
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return new Result<User>(ResultCode.LOGIN_ERROR.getCode(), "登录失败，服务器异常");
        } catch (IOException e) {
            e.printStackTrace();
            return new Result<User>(ResultCode.LOGIN_ERROR.getCode(), "登录失败，网络异常");
        }
    }
}
