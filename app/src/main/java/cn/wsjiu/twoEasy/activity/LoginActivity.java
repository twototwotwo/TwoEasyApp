   package cn.wsjiu.twoEasy.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import cn.wsjiu.twoEasy.R;
import cn.wsjiu.twoEasy.component.LoadingBeanner;
import cn.wsjiu.twoEasy.thread.AuthorizeRunnable;
import cn.wsjiu.twoEasy.thread.LoginRunnable;
import cn.wsjiu.twoEasy.thread.RegisterRunnable;
import cn.wsjiu.twoEasy.entity.User;
import cn.wsjiu.twoEasy.result.Result;
import cn.wsjiu.twoEasy.thread.threadPool.ThreadPoolUtils;
import cn.wsjiu.twoEasy.util.UserUtils;

public class LoginActivity extends AppCompatActivity {

    private final String USER = "user";
    private String accessToken = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getSharedPreferences(USER, MODE_PRIVATE);
        if(sharedPreferences.getString(USER, null) != null) {
            Intent intent = new Intent();
            intent.setClass(this.getBaseContext(), MainActivity.class);
            startActivity(intent);
            this.finish();
        }
    }

    /**
     * login envent
     */
    public void login(View view) {
        EditText loginAccountView = findViewById(R.id.login_account_Edit_Text);
        EditText loginPasswordView = findViewById(R.id.login_password_edit_text);
        String accountName = loginAccountView.getText().toString();
        String password = loginPasswordView.getText().toString();
        if(password.length() == 0) {
            Toast.makeText(getApplicationContext(), "密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        Handler handler = new Handler(getMainLooper(), this::handleMessageForLogin);
        LoginRunnable loginRunnable = new LoginRunnable(getResources().getString(R.string.login_url), handler, accountName, password);
        ThreadPoolUtils.asynExecute(loginRunnable);
    }

    /**
     * register event
     */
    public void register(View mview) {
        if(accessToken == null || accessToken.length() <= 0) {
            Toast.makeText(getApplicationContext(), "请先验证", Toast.LENGTH_SHORT).show();
            return;
        }
        EditText registerAccountView = findViewById(R.id.register_account_edit_text);
        EditText registerPasswordView = findViewById(R.id.register_password_edit_text);
        EditText registerRePasswordView = findViewById(R.id.register_repassword_edit_text);
        String accountName = registerAccountView.getText().toString();
        String password = registerPasswordView.getText().toString();
        String rePassword = registerRePasswordView.getText().toString();
        if(accountName.length() == 0) {
            Toast.makeText(getApplicationContext(), "账户不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if(password.length() == 0) {
            Toast.makeText(getApplicationContext(), "密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!password.equals(rePassword)) {
            Toast.makeText(getApplicationContext(), "两次密码不同", Toast.LENGTH_SHORT).show();
            return;
        }
        Handler handler = new Handler(getMainLooper(), this::handleMessageForLogin);
        RegisterRunnable registerRunnable = new RegisterRunnable(getResources().getString(R.string.register_url), handler, accountName, password, accessToken);
        ThreadPoolUtils.asynExecute(registerRunnable);
    }

       /**
        * 处理登录或者注册请求的回调，完成登录进入
        * @param msg 回调的数据
        * @return true 请求成功 false 请求失败
        */
    private boolean handleMessageForLogin(Message msg) {
        Object obj = msg.obj;
        Result<User> result = null;
        if (obj instanceof Result) {
            result = (Result<User>) obj;
        }
        if(result != null && result.isSuccess()) {
            loginSuccess(result.getData());
            return true;
        }else {
            Toast.makeText(this, result.getMsg(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }
    /**
     * @param user
     */
    private void loginSuccess(User user) {
        UserUtils.saveUser(user, this);
        UserUtils.setUser(user);
        Intent intent = new Intent();
        intent.setClass(this, MainActivity.class);
        startActivity(intent);
        this.finish();
    }

    /**
     * authorize event
     */
    public void authorize(View mview) {
        WebView webView = findViewById(R.id.authorize_webview);
        webView.setVisibility(View.VISIBLE);
        WebSettings webSettings = webView.getSettings();
        webSettings.setDomStorageEnabled(true);//主要是这句
        webSettings.setJavaScriptEnabled(true);//启用js
        webSettings.setBlockNetworkImage(false);//解决图片不显示
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setLoadsImagesAutomatically(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if(url.equals(getResources().getString(R.string.yiban_authorize_html_url))) {
                    // this url is the authorize url
                    view.loadUrl(url);
                }else {
                    Handler handler = new Handler(getMainLooper(), (msg) -> {
                       return handleMessageForAuthorize(msg);
                    });
                    AuthorizeRunnable callable = new AuthorizeRunnable(url, handler);
                    ThreadPoolUtils.asynExecute(callable);
                }
                return true;
            }
        });
        webView.loadUrl(getResources().getString(R.string.yiban_authorize_url));
    }

       /**
        * 处理授权请求的回调，完成授权
        * @param msg 授权页面回调的数据
        * @return true 请求成功 false 请求失败
        */
       private boolean handleMessageForAuthorize(Message msg) {
           Object obj = msg.obj;
           Result<String> result = null;
           if (obj instanceof Result) {
               result = (Result<String>) obj;
           }
           if(result != null && result.isSuccess()) {
               accessToken = result.getData();
               Toast.makeText(getApplicationContext(), "授权成功", Toast.LENGTH_SHORT).show();
               return true;
           }else {
               Toast.makeText(this, result.getMsg(), Toast.LENGTH_SHORT).show();
               return false;
           }
       }

    /**
     * change login layout to register layout, or contrarily
     * @param view  which view click by user
     */
    public void loginOrRegister(View view) {
        LinearLayout loginLayout = findViewById(R.id.login_layout);
        LinearLayout registerLayout = findViewById(R.id.register_layout);
        TextView loginTextButton = findViewById(R.id.login_select_button);
        TextView registerTextButton = findViewById(R.id.register_select_button);
        if(view.getId() == R.id.register_select_button) {
            loginLayout.setVisibility(View.INVISIBLE);
            registerLayout.setVisibility(View.VISIBLE);
            loginTextButton.setBackgroundColor(Color.TRANSPARENT);
            registerTextButton.setBackgroundColor(Color.WHITE);
        }else if(view.getId() == R.id.login_select_button) {
            loginLayout.setVisibility(View.VISIBLE);
            registerLayout.setVisibility(View.INVISIBLE);
            loginTextButton.setBackgroundColor(Color.WHITE);
            registerTextButton.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    /**
     * check the user accont and password are correct format
     * @param account user acount(now only phone number)
     * @param password user password(least 8 characters, up to 10 )
     * @param repassword it is null when login
     * @return true or false
     */
    private boolean checkPasswordAndAccount(String account, String password, String repassword) {
        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            WebView webView = findViewById(R.id.authorize_webview);
            webView.setVisibility(View.GONE);
        }
        return true;
    }
}