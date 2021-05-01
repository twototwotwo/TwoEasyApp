package cn.wsjiu.twoEasy.component;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
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

import cn.wsjiu.twoEasy.R;
import cn.wsjiu.twoEasy.activity.GoodsDetailActivity;
import cn.wsjiu.twoEasy.activity.PublishGoodsActivity;
import cn.wsjiu.twoEasy.entity.Goods;
import cn.wsjiu.twoEasy.entity.GoodsState;
import cn.wsjiu.twoEasy.entity.User;
import cn.wsjiu.twoEasy.result.Result;
import cn.wsjiu.twoEasy.thread.HttpGetRunnable;
import cn.wsjiu.twoEasy.thread.threadPool.ThreadPoolUtils;
import cn.wsjiu.twoEasy.util.ImageUtils;

public class GoodsRankView extends LinearLayout {
    private AdaptionImageView cover;
    private TextView title;
    private TextView sellPrice;
    // 在排行榜中的序号
    private TextView orderNumber;

    private TextView goodsStateView;
    private Goods goods;
    private User user;

    public GoodsRankView(Context context) {
        super(context);
        init();
    }

    public GoodsRankView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GoodsRankView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setLayoutParams(layoutParams);
        inflate(getContext(), R.layout.view_goods_rank, this);

        orderNumber = findViewById(R.id.order_number);
        cover = findViewById(R.id.goods_cover);
        title = findViewById(R.id.goods_title);
        sellPrice = findViewById(R.id.sell_price);
        goodsStateView = findViewById(R.id.goods_state_view);

        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(goods == null || user == null) {
                    Toast.makeText(getContext(), "数据异常", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent();
                intent.putExtra("user", user);
                intent.putExtra("goods", goods);
                intent.setClass(getContext(), GoodsDetailActivity.class);
                getContext().startActivity(intent);
            }
        });
    }

    public void bindData(Goods goods, User user, int orderNumber) {
        this.goods = goods;
        this.user = user;
        GoodsState goodsState = GoodsState.valueOf(goods.getState());

        String coverUrl = getResources().getString(R.string.image_get_url)
                + ImageUtils.getCoverByStr(goods.getImageUrl());
        this.orderNumber.setText(String.valueOf(orderNumber));
        cover.setImageFromUrl(coverUrl);
        title.setText(goods.getTitle());
        sellPrice.setText(String.format("¥ %s", goods.getSellPrice()));

        goodsStateView.setText(goodsState.state);
    }


}
