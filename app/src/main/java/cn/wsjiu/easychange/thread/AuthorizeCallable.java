package cn.wsjiu.easychange.thread;

import com.alibaba.fastjson.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import cn.wsjiu.easychange.result.Result;
import cn.wsjiu.easychange.result.ResultCode;

public class AuthorizeCallable extends AbstractCallable {
    protected final String ACCESS_TOKEN = "access_token";

    private final int TIME_OUT = 3000;
    public AuthorizeCallable(String url) {
        super();
        this.url = url;
    }

    @Override
    public Result<String> call() throws Exception {
        try {
            URL targetUrl = new URL(url);
            HttpURLConnection httpURLConnection =
                    (HttpURLConnection)targetUrl.openConnection();
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
                    return new Result<String>(accessToken);
                }else {
                    return new Result<String>(ResultCode.AUTHORIZE_ERROR.getCode(), "授权失败, accessToken为空");
                }
            }else {
                return new Result<String>(responseJSONObject.getInteger(CODE), responseJSONObject.getString(MSG));
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return new Result<String>(ResultCode.AUTHORIZE_ERROR.getCode(), "授权失败，服务器异常");
        } catch (IOException e) {
            e.printStackTrace();
            return new Result<String>(ResultCode.AUTHORIZE_ERROR.getCode(), "授权失败， 网络异常");
        }
    }
}
