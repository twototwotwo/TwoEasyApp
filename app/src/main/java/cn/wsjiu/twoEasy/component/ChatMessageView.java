package cn.wsjiu.twoEasy.component;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import cn.wsjiu.twoEasy.R;
import cn.wsjiu.twoEasy.activity.UserInfoActivity;
import cn.wsjiu.twoEasy.entity.IM.Message;
import cn.wsjiu.twoEasy.entity.User;
import cn.wsjiu.twoEasy.util.DataSourceUtils;
import cn.wsjiu.twoEasy.util.DensityUtils;
import cn.wsjiu.twoEasy.util.UserUtils;

public class ChatMessageView extends LinearLayout {

    /**
     * 消息的内容排列方向
     * @LTR 从左到右
     * @RTL 从右到左
     */
    public static final int LTR = 0;
    public static final int RTL = 1;

    private Message message;

    private AdaptionImageView headView;
    private TextView messageView;
    private LinearLayout messageLinearLayout;

    public ChatMessageView(Context context) {
        super(context);
    }

    public ChatMessageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ChatMessageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ChatMessageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void init() {
        int imageHeight = DensityUtils.dpToPx(35);
        int padding = DensityUtils.dpToPx(10);
        messageView = new TextView(getContext());
        headView = new AdaptionImageView(getContext());
        ViewGroup.LayoutParams headLayoutParams = new ViewGroup.LayoutParams(imageHeight, ViewGroup.LayoutParams.WRAP_CONTENT);
        headView.setLayoutParams(headLayoutParams);
        headView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(message != null) {
                    User userSelf = UserUtils.getUser();
                    User user;
                    if(message.getSendId().equals(userSelf.getUserId())) {
                        user = userSelf;
                    }else {
                        user = DataSourceUtils.getUser(message.getSendId());
                    }
                    if(user != null) {
                        Intent intent = new Intent();
                        intent.setClass(getContext(), UserInfoActivity.class);
                        intent.putExtra("user", user);
                        getContext().startActivity(intent);
                    }
                }
            }
        });
        messageView.setPadding(padding, 0, padding, 0);
        messageView.setGravity(Gravity.CENTER);
        messageView.setBackgroundResource(R.drawable.shape_radius_5dp);
        messageView.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        ViewGroup.LayoutParams messageViewLayoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        messageView.setLayoutParams(messageViewLayoutParams);
        LayoutParams messageLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        messageLayoutParams.weight = 1;
        messageLinearLayout = new LinearLayout(getContext());
        messageLinearLayout.addView(messageView);
        messageLinearLayout.setLayoutParams(messageLayoutParams);
        messageLinearLayout.setPadding(padding, 0, padding, 0);

        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setLayoutParams(layoutParams);
        this.setOrientation(HORIZONTAL);
        this.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
    }

    public void bindData(Message message) {
        this.message = message;
        User userSelf = UserUtils.getUser();
        int orientation;
        String headUrl;
        if(userSelf.getUserId().equals(message.getSendId())) {
            orientation = ChatMessageView.RTL;
            headUrl = userSelf.getHeadUrl();
        }else {
            User sendUser = DataSourceUtils.getUser(message.getSendId());
            if(sendUser != null) {
                headUrl = sendUser.getHeadUrl();
            }else {
                headUrl = "";
            }
            orientation = ChatMessageView.LTR;
        }
        String messageText = Message.CONTENT_TYPE_TEXT.equals(message.getContentType())
                ? message.getContent()
                : "[ " + message.getContent() + "]";

        messageView.setText(messageText);
        headView.setImageFromUrl(headUrl);

        removeAllViews();
        if(orientation == LTR) {
            this.setPadding(0, DensityUtils.dpToPx(10), DensityUtils.dpToPx(50), DensityUtils.dpToPx(10));
            this.addView(headView);
            this.addView(messageLinearLayout);
            messageLinearLayout.setGravity(Gravity.START);
        }else {
            this.setPadding(DensityUtils.dpToPx(50), DensityUtils.dpToPx(10), 0, DensityUtils.dpToPx(10));
            this.addView(messageLinearLayout);
            this.addView(headView);
            messageLinearLayout.setGravity(Gravity.END);
        }
    }
}
