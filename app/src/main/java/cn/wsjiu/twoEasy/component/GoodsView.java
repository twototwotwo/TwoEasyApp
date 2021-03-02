package cn.wsjiu.twoEasy.component;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import com.alibaba.fastjson.JSONObject;
import java.util.Map;

import cn.wsjiu.twoEasy.R;
import cn.wsjiu.twoEasy.activity.GoodsDetailActivity;
import cn.wsjiu.twoEasy.entity.Goods;
import cn.wsjiu.twoEasy.entity.User;
import cn.wsjiu.twoEasy.result.Result;
import cn.wsjiu.twoEasy.thread.LoadImageRunnable;
import cn.wsjiu.twoEasy.thread.threadPool.ThreadPoolUtils;
import cn.wsjiu.twoEasy.util.DensityUtils;
import cn.wsjiu.twoEasy.util.ImageUtils;

/**
 * @author wsjiu
 * @date 2020/12/15
 * 图文自定义view
 */
public class GoodsView extends LinearLayout {
    /**
     * 图片view
     */
    private AdaptionImageView image;
    /**
     * 物品标题view
     */
    private TextView title;

    /**
     * 价格view
     */
    private TextView sellPrice;

    private LinearLayout userInfoLinearLayout;

    /**
     * 物品发布者的名称
     */
    private TextView userName;

    /**
     * 物品发布者的头像
     */
    private AdaptionImageView headImage;


    /**
     * 控件当前绑定的数据
     */
    private Goods goods;
    private User user;

    private static final int USER_INFO_LINEAR_LAYOUT_HEIGHT = 30;

    public GoodsView(Context context) {
        super(context);
        init();
    }

    public GoodsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GoodsView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public GoodsView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }


    private void init() {
        this.setOrientation(VERTICAL);
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setLayoutParams(layoutParams);
        this.setBackgroundResource(R.drawable.shape_radius_10dp);
        this.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));

        image = new AdaptionImageView(getContext());
        LayoutParams imageLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        image.setLayoutParams(imageLayoutParams);
        this.addView(image);

        title = new TextView(getContext());
        LayoutParams titleLayoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        title.setLayoutParams(titleLayoutParams);
        title.setTextColor(Color.BLACK);
        title.setPadding(10, 10, 10 ,10);
        title.setMaxLines(2);
        this.addView(title);

        sellPrice = new TextView(getContext());
        LayoutParams sellPriceLayoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sellPrice.setLayoutParams(sellPriceLayoutParams);
        sellPrice.setTextColor(Color.RED);
        sellPrice.setPadding(10, 0, 10, 10);
        this.addView(sellPrice);

        userName = new TextView(getContext());
        LayoutParams userNameLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        userName.setLayoutParams(userNameLayoutParams);
        userName.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        userName.setTextSize(10);
        headImage = new AdaptionImageView(getContext());
        LayoutParams headImageLayoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        headImage.setLayoutParams(headImageLayoutParams);
        headImage.setImageHeight(DensityUtils.dpToPx(USER_INFO_LINEAR_LAYOUT_HEIGHT));
        headImage.setImageWidth(DensityUtils.dpToPx(USER_INFO_LINEAR_LAYOUT_HEIGHT));
        headImage.setBackgroundColor(Color.LTGRAY);

        userInfoLinearLayout = new LinearLayout(getContext());
        userInfoLinearLayout.setOrientation(HORIZONTAL);
        LayoutParams userInfoLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DensityUtils.dpToPx(USER_INFO_LINEAR_LAYOUT_HEIGHT));
        userInfoLinearLayout.setLayoutParams(userInfoLayoutParams);
        userInfoLinearLayout.setPadding(20, 0, 50, 20);
        userInfoLinearLayout.addView(headImage);
        userInfoLinearLayout.addView(userName);
        this.addView(userInfoLinearLayout);

        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(goods == null || user == null) return;
                Intent intent = new Intent();
                intent.setClass(getContext(), GoodsDetailActivity.class);
                intent.putExtra("goods", goods);
                intent.putExtra("user", user);
                Activity activity = (Activity) getContext();
                activity.startActivity(intent);
            }
        });
    }

    /**
     * 绑定物品信息与用户信息到当前view上
     * @param goods 物品信息
     * @param user 物品拥有者
     */
    public void bindData(Goods goods, User user) {
        this.goods = goods;
        this.user = user;
        String imageUrlMapStr = goods.getImageUrl();
        String coverUrl = ImageUtils.getCoverByStr(imageUrlMapStr);
        if(coverUrl != null && coverUrl.length() != 0) {
            coverUrl = getResources().getString(R.string.image_get_url) + coverUrl;
            image.setImageFromUrl(coverUrl);
        }
        String headUrl = user.getHeadUrl();
        if(headUrl != null && headUrl.length() > 0) {
            headImage.setImageFromUrl(headUrl);
        }
        userName.setText(user.getUserName());
        title.setText(goods.getTitle());
        sellPrice.setText(String.format("¥%s", goods.getSellPrice()));
    }

    /**
     * 绑定物品信息到当前view上
     * @param goods 物品信息
     */
    public void bindData(Goods goods) {
        this.goods = goods;
        String imageUrlMapStr = goods.getImageUrl();
        String coverUrl = ImageUtils.getCoverByStr(imageUrlMapStr);
        if(coverUrl != null && coverUrl.length() != 0) {
            coverUrl = getResources().getString(R.string.image_get_url) + coverUrl;
            image.setImageFromUrl(coverUrl);
        }
        title.setText(goods.getTitle());
        sellPrice.setText(String.format("¥%s", goods.getSellPrice()));
        userInfoLinearLayout.getLayoutParams().height = 0;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
