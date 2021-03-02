package cn.wsjiu.twoEasy.activity;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.android.material.tabs.TabLayout;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cn.wsjiu.twoEasy.R;
import cn.wsjiu.twoEasy.activity.ui.main.ChatFragment;
import cn.wsjiu.twoEasy.activity.ui.main.TabPagerAdapter;
import cn.wsjiu.twoEasy.dao.MessageDAO;
import cn.wsjiu.twoEasy.entity.Goods;
import cn.wsjiu.twoEasy.entity.IM.Message;
import cn.wsjiu.twoEasy.entity.Order;
import cn.wsjiu.twoEasy.entity.SubscribeRecord;
import cn.wsjiu.twoEasy.entity.User;
import cn.wsjiu.twoEasy.result.Result;
import cn.wsjiu.twoEasy.thread.HttpGetRunnable;
import cn.wsjiu.twoEasy.thread.threadPool.ThreadPoolUtils;
import cn.wsjiu.twoEasy.util.DataSourceUtils;
import cn.wsjiu.twoEasy.util.UserUtils;
import cn.wsjiu.twoEasy.webSocket.IMWebSocket;

public class MainActivity extends AppCompatActivity {
    /**
     * 广播接收器
     */
    private BroadcastReceiver receiver;

    ChatFragment chatFragment;

    private int waterBlue;
    private int black;
    private int unReadCount = 0;

    /**
     *  tab button text
     */
    private int[] tabTexts = {
            R.string.tab_home,
            R.string.tab_search,
            R.string.tab_chat,
            R.string.tab_person
    };

    /**
     * tab button's icon id
     */
    private int[] tabIcons = {
            R.drawable.tab_home_icon,
            R.drawable.tab_question_icon,
            R.drawable.tab_chat_icon,
            R.drawable.tab_person_icon};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        waterBlue = getResources().getColor(R.color.water_blue, null);
        black = getResources().getColor(R.color.black, null);

        ViewPager viewPager = findViewById(R.id.view_pager);
        TabPagerAdapter tabPagerAdapter = new TabPagerAdapter(
                getApplicationContext(),
                getSupportFragmentManager());
        Fragment fragment = tabPagerAdapter.getItem(2);
        viewPager.setAdapter(tabPagerAdapter);
        viewPager.setOffscreenPageLimit(4);
        if(fragment instanceof ChatFragment) {
            chatFragment = (ChatFragment) fragment;
        }
        //设置viewpager和tablayout联动
        TabLayout tabLayout = findViewById(R.id.tab_bar);
        tabLayout.setupWithViewPager(viewPager);
        // reset tab's icon and text
        for(int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            tab.setCustomView(R.layout.view_tab);
            View customView = tab.getCustomView();
            ImageView tabIcon = customView.findViewById(R.id.tab_icon);
            TextView tabName = customView.findViewById(R.id.tab_name);
            tabIcon.setImageResource(tabIcons[i]);
            tabIcon.setBackgroundTintMode(PorterDuff.Mode.DST);
            tabName.setText(tabTexts[i]);
        }
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                View customView = tab.getCustomView();
                ImageView tabIcon = customView.findViewById(R.id.tab_icon);
                TextView tabName = customView.findViewById(R.id.tab_name);
                tabIcon.getDrawable().setTint(waterBlue);
                tabIcon.setBackgroundTintMode(PorterDuff.Mode.CLEAR);
                tabName.setTextColor(waterBlue);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                View customView = tab.getCustomView();
                ImageView tabIcon = customView.findViewById(R.id.tab_icon);
                TextView tabName = customView.findViewById(R.id.tab_name);
                tabIcon.getDrawable().setTint(black);
                tabName.setTextColor(black);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                onTabSelected(tab);
            }
        });
        tabLayout.getTabAt(0).select();

        // 广播接收器注册
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String payLoad = intent.getStringExtra("message");
                Message message = JSONObject.parseObject(payLoad, Message.class);
                receiveMessage(message);
            }
        };
        IntentFilter intentFilter = new IntentFilter(getResources().getString(R.string.chat_message_broadcast));
        registerReceiver(receiver, intentFilter);

        int unReadCount = MessageDAO.instance.queryUnReadCount();
        updateUnReadCount(unReadCount);

        // 初始化websocket
        User user = UserUtils.getUser();
        if(user != null && user.getUserId() != null) {
            //获取一些基础数据
            String url = getResources().getString(R.string.start_url);
            url += "?userId=" + user.getUserId();
            Handler handler = new Handler(getMainLooper(), this::handle);
            HttpGetRunnable runnable = new HttpGetRunnable(url, handler);
            ThreadPoolUtils.synExecute(runnable);
        }
        ConnectivityManager manager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        manager.registerNetworkCallback(builder.build(), new NetworkChangeCallback());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void finish() {
        super.finish();
        unregisterReceiver(receiver);
    }

    public void receiveMessage(Message message) {
        if(chatFragment != null) {
            chatFragment.addChatMessageByChatId(message.getChatId());
        }
        Integer receiveId = message.getReceiveId();
        if(receiveId != null && receiveId.equals(UserUtils.getUser().getUserId())) {
            updateUnReadCount(this.unReadCount + 1);
            notifyNewMessage(message);
            MessageDAO.instance.insertMessage(message);
        }
    }

    /**
     * 发通知有新消息
     */
    private void notifyNewMessage(Message message) {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, null);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    getString(R.string.chat_message_channel_ID),
                    getString(R.string.chat_message_channel_NAME),
                    NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }
        builder.setAutoCancel(true);
        builder.setChannelId(getString(R.string.chat_message_channel_ID));
        builder.setSmallIcon(R.mipmap.ic_launcher);

        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), ChatActivity.class);
        intent.putExtra("chatId", message.getChatId());
        if(Message.CONTENT_OPEN_EYE.equals(message.getContentType())) {
            builder.setContentTitle("开眼邀请");
            builder.setContentText(message.getContent());
        }else {
            builder.setContentTitle("新消息");
            builder.setContentText("您有未读消息,请查看");
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this,
                0, intent, PendingIntent.FLAG_ONE_SHOT);
        builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        manager.notify(0, notification);
    }

    public void updateUnReadCount(int unReadCount) {
        if(unReadCount != this.unReadCount) {
            this.unReadCount = unReadCount;
            TabLayout tabLayout = findViewById(R.id.tab_bar);
            TabLayout.Tab tab = tabLayout.getTabAt(2);
            View customView = tab.getCustomView();
            TextView unReadCountText = customView.findViewById(R.id.unread_text);
            if(unReadCount <= 0) {
                unReadCount = 0;
                unReadCountText.setVisibility(View.INVISIBLE);
            }else if (unReadCount <= 99){
                unReadCountText.setText(String.valueOf(unReadCount));
                unReadCountText.setVisibility(View.VISIBLE);
            }else {
                unReadCountText.setText("99");
                unReadCountText.setVisibility(View.VISIBLE);
            }
        }
    }

    public void reduceUnReadCount(int reduce) {
        updateUnReadCount(unReadCount - reduce);
    }

    public boolean handle(android.os.Message msg) {
        Object obj = msg.obj;
        Result result = (Result)obj;
        if(result.isSuccess()) {
            JSONObject dataJSONObject = (JSONObject) result.getData();
            JSONArray recordJSONArray = dataJSONObject.getJSONArray("subscribeRecord");
            Map<Integer, SubscribeRecord> subscribeRecordMap = new HashMap<>();
            if(recordJSONArray != null) {
                for(int i =0 ; i < recordJSONArray.size(); i++) {
                    SubscribeRecord record = recordJSONArray.getObject(i, SubscribeRecord.class);
                    subscribeRecordMap.put(record.getGoodsId(), record);
                }
            }

            JSONArray orderJSONArray = dataJSONObject.getJSONArray("order");
            Map<Integer, Order> orderMap = new HashMap<>();
            if(orderJSONArray != null) {
                for (int i = 0; i < orderJSONArray.size(); i++) {
                    Order order = orderJSONArray.getObject(i, Order.class);
                    orderMap.put(order.getGoodsId(), order);
                }
            }

            JSONObject userJSONObject = dataJSONObject.getJSONObject("user");
            Map<Integer, User> userMap = new HashMap<>();
            if(userJSONObject != null) {
                for (String userId : userJSONObject.keySet()
                ) {
                    User user = userJSONObject.getObject(userId, User.class);
                    userMap.put(user.getUserId(), user);
                }
            }

            JSONObject subscribeJSONObject = dataJSONObject.getJSONObject("commonGoods");
            Map<Integer, Goods> commonGoodsMap = new HashMap<>();
            if(subscribeJSONObject != null) {
                for(String key : subscribeJSONObject.keySet()) {
                    Goods goods = subscribeJSONObject.getObject(key, Goods.class);
                    commonGoodsMap.put(goods.getGoodsId(), goods);
                }
            }

            JSONArray publishJSONArray = dataJSONObject.getJSONArray("publishGoods");
            Map<Integer, Goods> publishMap = new HashMap<>();
            if(publishJSONArray != null) {
                for(int i = 0; i < publishJSONArray.size(); i++) {
                    Goods goods = publishJSONArray.getObject(i, Goods.class);
                    publishMap.put(goods.getGoodsId(), goods);
                }
            }

            JSONArray followedIdJSONArray = dataJSONObject.getJSONArray("followedId");
            Set<Integer> followedIdSet = new HashSet<>();
            if(followedIdJSONArray != null) {
                for(int i = 0; i < followedIdJSONArray.size(); i++) {
                    followedIdSet.add(followedIdJSONArray.getIntValue(i));
                }
            }
            DataSourceUtils.init(orderMap, subscribeRecordMap, userMap, commonGoodsMap, publishMap, followedIdSet);
        }
        return true;
    }

    static class NetworkChangeCallback extends ConnectivityManager.NetworkCallback {
        @Override
        public void onAvailable(@NonNull Network network) {
            IMWebSocket.singleInstance.reconnect();
        }
    }
}