   package cn.wsjiu.easychange.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
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

import cn.wsjiu.easychange.R;
import cn.wsjiu.easychange.thread.AuthorizeCallable;
import cn.wsjiu.easychange.thread.LoginCallable;
import cn.wsjiu.easychange.thread.RegisterCallable;
import cn.wsjiu.easychange.entity.User;
import cn.wsjiu.easychange.result.Result;

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
            Intent intent = getIntent();
            intent.setClass(this.getApplicationContext(), MainActivity.class);
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
        LoginCallable loginCallable = new LoginCallable(getResources().getString(R.string.login_url), accountName, password);
        FutureTask<Result<User>> task = new FutureTask<Result<User>>(loginCallable);
        new Thread(task).start();
        try {
            Result<User> result = task.get();
            if(result.isSuccess()) {
                loginSuccess(result.getData());
            }else {
                Toast.makeText(this, result.getMsg(), Toast.LENGTH_SHORT).show();
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
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

        RegisterCallable registerCallable = new RegisterCallable(getResources().getString(R.string.register_url), accountName, password, accessToken);
        FutureTask<Result<User>> task = new FutureTask<Result<User>>(registerCallable);
        new Thread(task).start();
        try {
            Result<User> result = task.get();
            if(result.isSuccess()) {
                loginSuccess(result.getData());
            }else {
                Toast.makeText(this, result.getMsg(), Toast.LENGTH_SHORT).show();
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * @param user
     */
    private void loginSuccess(User user) {
        // TODO server delete password
        Intent intent = new Intent();
        intent.setClass(this, MainActivity.class);
        SharedPreferences sharedPreferences = getSharedPreferences(USER, MODE_PRIVATE);
        sharedPreferences.edit().putString(USER, JSONObject.toJSONString(user)).commit();
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
                    AuthorizeCallable callable = new AuthorizeCallable(url);
                    try {
                        FutureTask<Result<String>> futureTask = new FutureTask<Result<String>>(callable);
                        Thread thread = new Thread(futureTask);
                        thread.start();
                        Result<String> result = futureTask.get();
                        if(result.isSuccess()) {
                            accessToken = result.getData();
                            webView.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), "授权成功", Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(getApplicationContext(), result.getMsg(), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
                return true;
            }
        });
        webView.loadUrl(getResources().getString(R.string.yiban_authorize_url));
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
            loginTextButton.setBackgroundColor(Color.GRAY);
            registerTextButton.setBackgroundColor(Color.WHITE);
        }else if(view.getId() == R.id.login_select_button) {
            loginLayout.setVisibility(View.VISIBLE);
            registerLayout.setVisibility(View.INVISIBLE);
            loginTextButton.setBackgroundColor(Color.WHITE);
            registerTextButton.setBackgroundColor(Color.GRAY);
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