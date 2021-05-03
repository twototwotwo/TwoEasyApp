package cn.wsjiu.twoEasy.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSONObject;

import java.util.Map;

import cn.wsjiu.twoEasy.R;
import cn.wsjiu.twoEasy.component.AdaptionImageView;
import cn.wsjiu.twoEasy.entity.Goods;
import cn.wsjiu.twoEasy.entity.SubscribeRecord;
import cn.wsjiu.twoEasy.entity.User;
import cn.wsjiu.twoEasy.result.Result;
import cn.wsjiu.twoEasy.thread.HttpGetRunnable;
import cn.wsjiu.twoEasy.thread.HttpPostRunnable;
import cn.wsjiu.twoEasy.thread.threadPool.ThreadPoolUtils;
import cn.wsjiu.twoEasy.util.DataSourceUtils;
import cn.wsjiu.twoEasy.util.DensityUtils;
import cn.wsjiu.twoEasy.util.UserUtils;

public class GoodsDetailActivity extends AppCompatActivity {

    /**
     * 数据
     */
    private User user;
    private Goods goods;

    /**
     * 是否正在进行请求
     */
    private boolean isRun = false;

    private Button followButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goods_detail);
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        Intent intent = getIntent();
        user = (User) intent.getSerializableExtra("user");
        goods = (Goods) intent.getSerializableExtra("goods");
        AdaptionImageView head = findViewById(R.id.head_image_view);
        head.setImageFromUrl(user.getHeadUrl());
        head.setOnClickListener((v -> {
            Intent jumpIntent = new Intent();
            jumpIntent.setClass(GoodsDetailActivity.this, UserInfoActivity.class);
            jumpIntent.putExtra("user", user);
            startActivity(jumpIntent);
        }));

        TextView userNameView = findViewById(R.id.user_name_text_view);
        userNameView.setText(user.getUserName());

        TextView sellPriceView = findViewById(R.id.sell_price_view);
        sellPriceView.setText( "￥" + goods.getSellPrice());

        TextView discountView = findViewById(R.id.discount_view);
        if(goods.getBuyPrice() > goods.getSellPrice()) {
            float discount = goods.getSellPrice() / goods.getBuyPrice();
            int discountInt = (int) (discount * 100);
            discount = discountInt / 10.0f;
            discountView.setText(discount + "折");
            discountView.setVisibility(View.VISIBLE);
        }

        TextView detailView = findViewById((R.id.detail_view));
        detailView.setText(goods.getDetail());

        TextView schoolNameView = findViewById(R.id.school_name_text_view);
        schoolNameView.setText("◉ " + user.getSchoolName());

        LinearLayout imageGroupView = findViewById(R.id.image_group_layout);
        String imageUrl = goods.getImageUrl();
        if(imageUrl != null && imageUrl.length() != 0) {
            String baseUrl = getResources().getString(R.string.image_get_url);
            JSONObject urlJSONObject = JSONObject.parseObject(imageUrl);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, 0, 0, DensityUtils.dpToPx(20));
            for(Map.Entry entry : urlJSONObject.entrySet()) {
                String url = (String) entry.getValue();
                if(url != null && url.length() != 0) {
                    String[] splits = url.split("&&|\\.|:");
                    int width = Integer.parseInt(splits[1]);
                    int height = Integer.parseInt(splits[2]);
                    AdaptionImageView adaptionImageView = new AdaptionImageView(getBaseContext());
                    adaptionImageView.setImageWidth(width);
                    adaptionImageView.setImageHeight(height);
                    adaptionImageView.setLayoutParams(layoutParams);
                    url = baseUrl + url;
                    adaptionImageView.setImageFromUrl(url);
                    imageGroupView.addView(adaptionImageView);
                }
            }
        }

        ImageView priceDownImage = findViewById(R.id.price_down);
        if(DataSourceUtils.checkIsSubscribed(goods.getGoodsId())) {
            priceDownImage.setBackgroundResource(R.drawable.notice_selected);
        }else {
            priceDownImage.setBackgroundResource(R.drawable.notice);
        }

        ImageView upImage = findViewById(R.id.click_up);
        if(DataSourceUtils.checkIsUp(goods.getGoodsId())) {
            upImage.setBackgroundResource(R.drawable.up);
        }

        followButton = findViewById(R.id.follow_button);
        if(DataSourceUtils.checkIsFollow(user.getUserId())) {
            followButton.setText("已关注");
        }else {
            followButton.setText("+关注");
        }

        // 物品发布人是自己，隐藏"我想要"按钮
        if(UserUtils.getUser().getUserId().equals(user.getUserId())) {
            Button wantButton = findViewById(R.id.want_button);
            wantButton.setVisibility(View.INVISIBLE);
        }
    }

    public void want(View view) {
        Intent intent = new Intent();
        intent.putExtra("user", user);
        intent.putExtra("goods", goods);
        intent.setClass(getBaseContext(), ChatActivity.class);
        startActivity(intent);
    }

    public void back(View view) {
        onBackPressed();
    }

    public void clickUp(View view) {
        if (isRun) return;
        else isRun = true;
        String url;
        Handler handler;
        if (DataSourceUtils.checkIsUp(goods.getGoodsId())) {
            url = getResources().getString(R.string.cancel_up_goods_url);
            handler = new Handler(getMainLooper(), this::handleForCancelUp);
        }else {
            url = getResources().getString(R.string.up_goods_url);
            handler = new Handler(getMainLooper(), this::handleForUp);
        }
        url += "?userId=" + user.getUserId() + "&goodsId=" + goods.getGoodsId();
        HttpGetRunnable runnable = new HttpGetRunnable(url, handler);
        ThreadPoolUtils.asynExecute(runnable);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void subscribeOrCancelPrice(View view) {
        if (isRun) return;
        else isRun = true;
        String url;
        if (DataSourceUtils.checkIsSubscribed(goods.getGoodsId())) {
            url = getResources().getString(R.string.price_cancel_subscribe_url);
            url += "?userId=" + user.getUserId() + "&goodsId=" + goods.getGoodsId();
            Handler handler = new Handler(getMainLooper(),  this::handleForSubscribeAction);
            HttpGetRunnable runnable = new HttpGetRunnable(url, handler);
            ThreadPoolUtils.asynExecute(runnable);
        }else {
            url = getResources().getString(R.string.price_subscribe_url);
            SubscribeRecord record = new SubscribeRecord();
            record.setGoodsId(goods.getGoodsId());
            record.setUserId(user.getUserId());
            record.setSubscribePrice(goods.getSellPrice());
            Handler handler = new Handler(getMainLooper(),  this::handleForSubscribeAction);
            HttpPostRunnable<SubscribeRecord, Result>
                    runnable = new HttpPostRunnable<>(url, handler, record);
            ThreadPoolUtils.asynExecute(runnable);
        }
    }

    public void followOrCancelFollow(View view) {
        String url;
        if(DataSourceUtils.checkIsFollow(user.getUserId())) {
            url = getString(R.string.cancel_follow_user_url);
        }else {
            url = getString(R.string.follow_user_url);
        }
        Handler handler = new Handler(getMainLooper(), this::handleForFollowAction);
        int userSelfId = UserUtils.getUser().getUserId();
        url += "?fansId=" + userSelfId + "&followedId=" + user.getUserId();
        HttpGetRunnable runnable =new HttpGetRunnable(url, handler);
        ThreadPoolUtils.asynExecute(runnable);
    }

    private boolean handleForFollowAction(Message message) {
        Object object = message.obj;
        if(object instanceof Result) {
            Result result = (Result) object;
            if(result.isSuccess()) {
                if(DataSourceUtils.checkIsFollow(user.getUserId())) {
                    DataSourceUtils.cancelFollow(user.getUserId());
                    followButton.setText("关注");
                }else {
                    DataSourceUtils.addFollow(user.getUserId());
                    followButton.setText("已关注");
                }
            }
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private boolean handleForSubscribeAction(Message message) {
        Object obj = message.obj;
        Result result = (Result) obj;
        if(result.isSuccess()) {
            if(DataSourceUtils.checkIsSubscribed(goods.getGoodsId())) {
                DataSourceUtils.cancelSubscribedGoods(goods.getGoodsId());
                ImageView priceDownImage = findViewById(R.id.price_down);
                priceDownImage.setBackgroundResource(R.drawable.notice);
            }else {
                SubscribeRecord record = new SubscribeRecord();
                record.setGoodsId(goods.getGoodsId());
                record.setUserId(UserUtils.getUser().getUserId());
                record.setSubscribePrice(goods.getSellPrice());
                DataSourceUtils.subscribedGoods(record, goods);
                ImageView priceDownImage = findViewById(R.id.price_down);
                priceDownImage.setBackgroundResource(R.drawable.notice_selected);
            }
        }else {
            Toast.makeText(getBaseContext(), result.getMsg(), Toast.LENGTH_SHORT).show();
        }
        isRun = false;
        return true;
    }

    private boolean handleForUp(Message message) {
        Object obj = message.obj;
        Result result = (Result) obj;
        if(result.isSuccess()) {
            DataSourceUtils.addUp(goods.getGoodsId());
            ImageView upImage = findViewById(R.id.click_up);
            upImage.setBackgroundResource(R.drawable.up);
        }else {
            Toast.makeText(getBaseContext(), "点赞失败", Toast.LENGTH_SHORT).show();
        }
        isRun = false;
        return true;
    }

    private boolean handleForCancelUp(Message message) {
        Object obj = message.obj;
        Result result = (Result) obj;
        if(result.isSuccess()) {
            DataSourceUtils.cancelUp(goods.getGoodsId());
            ImageView upImage = findViewById(R.id.click_up);
            upImage.setBackgroundResource(R.drawable.no_up);
        }else {
            Toast.makeText(getBaseContext(), "取消点赞失败", Toast.LENGTH_SHORT).show();
        }
        isRun = false;
        return true;
    }
}