package cn.wsjiu.twoEasy.component;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import cn.wsjiu.twoEasy.R;
import cn.wsjiu.twoEasy.activity.ChatActivity;
import cn.wsjiu.twoEasy.entity.Goods;
import cn.wsjiu.twoEasy.entity.GoodsState;
import cn.wsjiu.twoEasy.entity.SubscribeRecord;
import cn.wsjiu.twoEasy.entity.User;
import cn.wsjiu.twoEasy.result.Result;
import cn.wsjiu.twoEasy.thread.HttpGetRunnable;
import cn.wsjiu.twoEasy.thread.HttpPostRunnable;
import cn.wsjiu.twoEasy.thread.threadPool.ThreadPoolUtils;
import cn.wsjiu.twoEasy.util.DataSourceUtils;
import cn.wsjiu.twoEasy.util.ImageUtils;
import cn.wsjiu.twoEasy.util.UserUtils;

/**
 * 订阅物品在个人主页的展示view
 * @author wsj
 */
public class GoodsSubcribedView extends LinearLayout {
    private AdaptionImageView coverImage;
    private TextView subscribePrice;
    private TextView currentPrice;
    private TextView goodsTitle;
    private TextView priceDownInfo;
    private TextView reSubscribeOrCancelButton;
    private TextView goodsStateView;
    private Button chatButton;

    private Goods goods;
    private SubscribeRecord record;
    private boolean isSubscribed = true;
    private boolean isRun = false;


    public GoodsSubcribedView(Context context) {
        super(context);
        init();
    }

    public GoodsSubcribedView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GoodsSubcribedView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public GoodsSubcribedView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setLayoutParams(layoutParams);
        inflate(getContext(), R.layout.view_goods_subscribed, this);
        coverImage = findViewById(R.id.goods_cover);
        subscribePrice = findViewById(R.id.subscribe_price);
        currentPrice = findViewById(R.id.current_price);
        goodsTitle = findViewById(R.id.goods_title);
        priceDownInfo = findViewById(R.id.price_down_info);
        goodsStateView = findViewById(R.id.goods_state_view);
        chatButton = findViewById(R.id.chat_button);
        chatButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int userSelfId = UserUtils.getUser().getUserId();
                User chatUser = DataSourceUtils.getUser(goods.getUserId());
                Intent intent = new Intent();
                intent.putExtra("user", chatUser);
                intent.putExtra("goods", goods);
                intent.setClass(getContext(), ChatActivity.class);
                getContext().startActivity(intent);
            }
        });

        reSubscribeOrCancelButton = findViewById(R.id.reSubscribe_or_cancel_button);
        reSubscribeOrCancelButton.setOnClickListener(new OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                reSubscribeOrCancel();
            }
        });
    }

    private void reSubscribeOrCancel() {
        if (isRun) return;
        else isRun = true;
        String url;
        if (isSubscribed) {
            url = getResources().getString(R.string.price_cancel_subscribe_url);
            url += "?userId=" + record.getUserId() + "&goodsId=" + record.getGoodsId();
            Handler handler = new Handler(getContext().getMainLooper(),
                    this::handle);
            HttpGetRunnable runnable = new HttpGetRunnable(url, handler);
            ThreadPoolUtils.asynExecute(runnable);
        }else {
            url = getResources().getString(R.string.price_subscribe_url);
            Handler handler = new Handler(getContext().getMainLooper(),
                    this::handle);
            HttpPostRunnable<SubscribeRecord, Result>
                    runnable = new HttpPostRunnable<>(url, handler, record);
            ThreadPoolUtils.asynExecute(runnable);
        }
    }

    public void bindData(SubscribeRecord record) {
        this.record = record;
        isSubscribed = DataSourceUtils.checkIsSubscribed(record.getGoodsId());
        if(isSubscribed) {
            reSubscribeOrCancelButton.setText("取消订阅");
        }else {
            reSubscribeOrCancelButton.setText("重新订阅");
        }
        goods = DataSourceUtils.getGoods(record.getGoodsId());
        String coverUrl = getResources().getString(R.string.image_get_url) + ImageUtils.getCoverByStr(goods.getImageUrl());
        coverImage.setImageFromUrl(coverUrl);
        Float subscribePriceValue = record.getSubscribePrice();
        Float currentPriceValue = goods.getSellPrice();
        currentPrice.setText(String.valueOf(currentPriceValue));
        subscribePrice.setText(String.valueOf(subscribePriceValue));
        int compareResult = subscribePriceValue.compareTo(currentPriceValue);
        if(compareResult > 0) {
            priceDownInfo.setText(String.format("已降价%s",
                    String.valueOf(subscribePriceValue - currentPriceValue)));
        }
        goodsTitle.setText(goods.getTitle());
        GoodsState goodsState = GoodsState.valueOf(goods.getState());
        goodsStateView.setText(goodsState.state);
    }

    public boolean handle(Message msg) {
        Object obj = msg.obj;
        Result result = (Result) obj;
        if(result.isSuccess()) {
            if(isSubscribed) {
                DataSourceUtils.cancelSubscribedGoods(record.getGoodsId());
                reSubscribeOrCancelButton.setText("重新订阅");
            }else {
                DataSourceUtils.subscribedGoods(record, goods);
                reSubscribeOrCancelButton.setText("取消订阅");
            }
            this.invalidate();
            isSubscribed = !isSubscribed;
        }else {
            Toast.makeText(getContext(), result.getMsg(), Toast.LENGTH_SHORT).show();
        }
        isRun = false;
        return true;
    }
}
