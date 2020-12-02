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

public class RegisterCallable extends AbstractCallable {
    private final String ACCOUNT_NAME = "accountName";
    private final String PASSWORD = "password";
    protected final String ACCESS_TOKEN = "accessToken";

    private String accountName;
    private String password;
    private String accessToken;

    public RegisterCallable(String url, String accountName, String password, String accessToken) {
        super();
        this.url = url;
        this.accountName = accountName;
        this.password = password;
        this.accessToken = accessToken;
    }

    @Override
    public Result<User> call() throws Exception {
        try {
            JSONObject dataJSONObject = new JSONObject();
            dataJSONObject.put(ACCOUNT_NAME, accountName);
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
                return new Result<User>(user);
            }else {
                return new Result<User>(responseJSONObject.getInteger(CODE), responseJSONObject.getString(MSG));
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return new Result<User>(ResultCode.REGISTER_ERROR.getCode(), "注册失败，服务器异常");
        } catch (IOException e) {
            e.printStackTrace();
            return new Result<User>(ResultCode.REGISTER_ERROR.getCode(), "注册失败，服务器异常");
        }
    }
}
