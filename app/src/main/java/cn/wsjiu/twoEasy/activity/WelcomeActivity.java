package cn.wsjiu.twoEasy.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import cn.wsjiu.twoEasy.R;
import cn.wsjiu.twoEasy.thread.threadPool.ThreadPoolUtils;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class WelcomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Handler handler = new Handler(getMainLooper(), this::handle);
        ThreadPoolUtils.asynExecute(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Message message = new Message();
            handler.sendMessage(message);
        });
    }

    private boolean handle(Message message) {
        Intent intent = new Intent();
        intent.setClass(this, LoginActivity.class);
        startActivity(intent);
        finish();
        return true;
    }
}