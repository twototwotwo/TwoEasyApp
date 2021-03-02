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
import cn.wsjiu.twoEasy.activity.PublishGoodsActivity;
import cn.wsjiu.twoEasy.entity.Goods;
import cn.wsjiu.twoEasy.entity.GoodsState;
import cn.wsjiu.twoEasy.entity.OrderState;
import cn.wsjiu.twoEasy.result.Result;
import cn.wsjiu.twoEasy.thread.HttpGetRunnable;
import cn.wsjiu.twoEasy.thread.threadPool.ThreadPoolUtils;
import cn.wsjiu.twoEasy.util.DataSourceUtils;
import cn.wsjiu.twoEasy.util.ImageUtils;

public class GoodsPublishedView extends LinearLayout {
    private AdaptionImageView cover;
    private TextView title;
    private TextView sellPrice;
    private TextView wantInfo;
    private TextView goodsStateView;
    private Button offOrOnButton;

    private Goods goods;

    public GoodsPublishedView(Context context) {
        super(context);
        init();
    }

    public GoodsPublishedView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GoodsPublishedView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setLayoutParams(layoutParams);
        inflate(getContext(), R.layout.view_goods_published, this);
        cover = findViewById(R.id.goods_cover);
        title = findViewById(R.id.goods_title);
        sellPrice = findViewById(R.id.sell_price);
        wantInfo = findViewById(R.id.goods_want_info);
        goodsStateView = findViewById(R.id.goods_state_view);

        Button editButton = findViewById(R.id.edit_button);
        editButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("goods", goods);
                intent.setClass(getContext(), PublishGoodsActivity.class);
                getContext().startActivity(intent);
            }
        });

        offOrOnButton = findViewById(R.id.off_or_on_button);
        offOrOnButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setView(R.layout.dialog_confirm);
                AlertDialog dialog = builder.create();
                dialog.show();
                dialog.getWindow().setBackgroundDrawableResource(R.color.transparent);
                TextView content = dialog.findViewById(R.id.dialog_confirm_content);
                GoodsState goodsState = GoodsState.valueOf(goods.getState());
                if(goodsState == GoodsState.OFFLINE) {
                    content.setText("确认上架吗");
                }else if(goodsState == GoodsState.UNSOLD){
                    content.setText("确认下架吗");
                }
                Button confirmButton = dialog.findViewById(R.id.dialog_confirm_button);
                Button cancelButton = dialog.findViewById(R.id.dialog_cancel_button);
                OnClickListener listener = new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(v.getId() == R.id.dialog_confirm_button) {
                            offOrOnGoods();
                        }
                        dialog.cancel();
                    }
                };
                confirmButton.setOnClickListener(listener);
                cancelButton.setOnClickListener(listener);
            }
        });
    }
    public void bindData(Goods goods) {
        this.goods = goods;
        GoodsState goodsState = GoodsState.valueOf(goods.getState());
        offOrOnButton.setVisibility(VISIBLE);
        if(goodsState == GoodsState.UNSOLD) {
            offOrOnButton.setText("下架");
        }else if(goodsState == GoodsState.OFFLINE){
            offOrOnButton.setText("重新上架");
        }else {
            offOrOnButton.setVisibility(INVISIBLE);
        }
        String coverUrl = getResources().getString(R.string.image_get_url)
                + ImageUtils.getCoverByStr(goods.getImageUrl());
        cover.setImageFromUrl(coverUrl);
        title.setText(goods.getTitle());
        sellPrice.setText(String.format("¥ %s", goods.getSellPrice()));
        if(goods.getWants() != null && goods.getWants() > 0) {
            wantInfo.setText("有" + goods.getWants() + "人感兴趣");
        }else {
            wantInfo.setText("没有人感兴趣");
        }
        goodsStateView.setText(goodsState.state);
    }

    private void offOrOnGoods() {
        String url;
        Handler handler;
        GoodsState goodsState = GoodsState.valueOf(goods.getState());
        if(goodsState == GoodsState.OFFLINE) {
            url = getResources().getString(R.string.goods_online_url);
            handler = new Handler(getContext().getMainLooper(), this::handlerOnlineGoods);
        }else if(goodsState == GoodsState.UNSOLD) {
            url = getResources().getString(R.string.goods_offline_url);
            handler = new Handler(getContext().getMainLooper(), this::handlerOfflineGoods);
        }else {
            return;
        }
        url +="?goodsId=" + goods.getGoodsId();
        HttpGetRunnable runnable = new HttpGetRunnable(url, handler);
        ThreadPoolUtils.asynExecute(runnable);
    }

    public boolean handlerOfflineGoods(Message msg) {
        Object obj = msg.obj;
        if(obj instanceof Result) {
            Result result = (Result) obj;
            if(result.isSuccess()) {
                goods.setState(GoodsState.OFFLINE.mask);
                goodsStateView.setText(GoodsState.OFFLINE.state);
                offOrOnButton.setText("重新上架");
                return true;
            }else {
                Toast.makeText(getContext(), result.getMsg(), Toast.LENGTH_SHORT).show();
            }
        }
        return true;
    }

    public boolean handlerOnlineGoods(Message msg) {
        Object obj = msg.obj;
        if(obj instanceof Result) {
            Result result = (Result) obj;
            if(result.isSuccess()) {
                goods.setState(GoodsState.UNSOLD.mask);
                goodsStateView.setText(GoodsState.UNSOLD.state);
                offOrOnButton.setText("下架");
                return true;
            }else {
                Toast.makeText(getContext(), result.getMsg(), Toast.LENGTH_SHORT).show();
            }
        }
        return true;
    }
}
