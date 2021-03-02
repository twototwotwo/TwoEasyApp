package cn.wsjiu.twoEasy.component;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import cn.wsjiu.twoEasy.R;
import cn.wsjiu.twoEasy.activity.UserInfoActivity;
import cn.wsjiu.twoEasy.entity.User;
import cn.wsjiu.twoEasy.result.Result;
import cn.wsjiu.twoEasy.thread.HttpGetRunnable;
import cn.wsjiu.twoEasy.thread.threadPool.ThreadPoolUtils;
import cn.wsjiu.twoEasy.util.DataSourceUtils;
import cn.wsjiu.twoEasy.util.UserUtils;

public class FollowedUserView extends FrameLayout {
    private User user;

    public FollowedUserView(@NonNull Context context) {
        super(context);
        init();
    }

    public FollowedUserView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FollowedUserView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public FollowedUserView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_followed_user, this);
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setLayoutParams(layoutParams);

        this.setOnClickListener((v -> {
            Intent intent = new Intent();
            intent.setClass(getContext(), UserInfoActivity.class);
            intent.putExtra("user", user);
            getContext().startActivity(intent);
        }));
    }

    public void bindData(User user) {
        this.user = user;
        AdaptionImageView headView = findViewById(R.id.user_head_view);
        headView.setImageFromUrl(user.getHeadUrl());
        TextView userNameView = findViewById(R.id.user_name_view);
        userNameView.setText(user.getUserName());
        Button followButton = findViewById(R.id.follow_button);
        if(DataSourceUtils.checkIsFollow(user.getUserId())) {
            followButton.setText("已关注");
        }else {
            followButton.setText("关注");
        }
        followButton.setOnClickListener(this::followOrCancelFollow);
    }

    public void followOrCancelFollow(View view) {
        String url;
        if(DataSourceUtils.checkIsFollow(user.getUserId())) {
            url = getResources().getString(R.string.cancel_follow_user_url);
        }else {
            url = getResources().getString(R.string.follow_user_url);
        }
        Handler handler = new Handler(getContext().getMainLooper(), this::handleForFollowAction);
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
                Button followButton = findViewById(R.id.follow_button);
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
}
