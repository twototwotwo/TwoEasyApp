package cn.wsjiu.twoEasy.thread;

import android.os.Handler;
import com.alibaba.fastjson.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import cn.wsjiu.twoEasy.result.Result;
import cn.wsjiu.twoEasy.result.ResultCode;

public class AuthorizeRunnable extends AbstractAsynRunnable {
    protected final String ACCESS_TOKEN = "access_token";

    public AuthorizeRunnable(String url, Handler handler) {
        super(url, handler);
    }

    @Override
    public void run(){
        Result<String> result;
        try {
            URL targetUrl = new URL(url);
            HttpURLConnection httpURLConnection =
                    (HttpURLConnection)targetUrl.openConnection();
            int TIME_OUT = 3000;
            httpURLConnection.setConnectTimeout(TIME_OUT);
            httpURLConnection.setRequestMethod("GET");
            InputStream in = httpURLConnection.getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            String responseStr = out.toString();
            JSONObject responseJSONObject = JSONObject.parseObject(responseStr);
            if(responseJSONObject.getBoolean(SUCCESS)) {
                String data = responseJSONObject.getString(DATA);
                JSONObject dataJSONObject = JSONObject.parseObject(data);
                String accessToken = dataJSONObject.getString(ACCESS_TOKEN);
                if(accessToken != null && accessToken.length() > 0) {
                    result = new Result<>(accessToken);
                }else {
                    result = new Result<>(ResultCode.AUTHORIZE_ERROR.getCode(), "授权失败, accessToken为空");
                }
            }else {
                result = new Result<>(responseJSONObject.getInteger(CODE), responseJSONObject.getString(MSG));
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            result = new Result<>(ResultCode.AUTHORIZE_ERROR.getCode(), "授权失败，服务器异常");
        } catch (IOException e) {
            e.printStackTrace();
            result = new Result<>(ResultCode.AUTHORIZE_ERROR.getCode(), "授权失败， 网络异常");
        }
        // 回调handler
        handle(result);
    }
}
