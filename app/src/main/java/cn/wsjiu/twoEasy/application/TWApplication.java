package cn.wsjiu.twoEasy.application;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import cn.wsjiu.twoEasy.R;
import cn.wsjiu.twoEasy.dao.MessageDAO;
import cn.wsjiu.twoEasy.dao.SearchRecordDAO;
import cn.wsjiu.twoEasy.entity.Goods;
import cn.wsjiu.twoEasy.entity.Order;
import cn.wsjiu.twoEasy.entity.SearchRecord;
import cn.wsjiu.twoEasy.entity.SubscribeRecord;
import cn.wsjiu.twoEasy.entity.User;
import cn.wsjiu.twoEasy.result.Result;
import cn.wsjiu.twoEasy.thread.HttpGetRunnable;
import cn.wsjiu.twoEasy.thread.threadPool.ThreadPoolUtils;
import cn.wsjiu.twoEasy.util.DataSourceUtils;
import cn.wsjiu.twoEasy.util.DensityUtils;
import cn.wsjiu.twoEasy.util.UserUtils;
import cn.wsjiu.twoEasy.webSocket.IMWebSocket;

public class TWApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    private void init() {
        // 本地数据库初始化
        MessageDAO.create(this.getBaseContext(), "message",
                null, 3);
        SearchRecordDAO.create(this.getBaseContext(), "searchRecord",
                null, 1);
        // 初始化dp和px的转化
        DensityUtils.scale = getApplicationContext().getResources().getDisplayMetrics().density;
        // 初始化用户信息
        UserUtils.init(this);
        User user = UserUtils.getUser();
        if(user != null && user.getUserId() != null) {
            //初始化webSocket连接
            String url = getResources().getString(R.string.webSocket_url);
            IMWebSocket.init(url, user.getUserId(), getApplicationContext());
        }
    }
}
