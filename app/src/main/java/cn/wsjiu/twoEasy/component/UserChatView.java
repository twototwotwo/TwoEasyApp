package cn.wsjiu.twoEasy.component;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.List;

import cn.wsjiu.twoEasy.R;
import cn.wsjiu.twoEasy.activity.ChatActivity;
import cn.wsjiu.twoEasy.activity.MainActivity;
import cn.wsjiu.twoEasy.dao.MessageDAO;
import cn.wsjiu.twoEasy.entity.IM.Message;
import cn.wsjiu.twoEasy.entity.Goods;
import cn.wsjiu.twoEasy.entity.User;
import cn.wsjiu.twoEasy.util.DataSourceUtils;
import cn.wsjiu.twoEasy.util.DensityUtils;
import cn.wsjiu.twoEasy.util.ImageUtils;
import cn.wsjiu.twoEasy.util.TimeUtils;
import cn.wsjiu.twoEasy.util.UserUtils;

public class UserChatView  extends LinearLayout {

    /**
     * 组件view
     */
    private AdaptionImageView head;
    private TextView unReadCountText;
    private AdaptionImageView goodsCover;
    private TextView userName;
    private TextView firstMessage;
    private TextView latestTime;

    /**
     * 聊天记录的个人数据
     */
    private String chatId;
    private User chatUser;
    private Goods goods;
    private int unReadCount;

    private static OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v instanceof UserChatView) {
                UserChatView userChatView = (UserChatView)v;
                Intent intent = new Intent();
                intent.putExtra("user", userChatView.getUser());
                intent.putExtra("goods", userChatView.getGoods());
                intent.putExtra("chatId", userChatView.getChatId());
                intent.setClass(userChatView.getContext(), ChatActivity.class);
                userChatView.getContext().startActivity(intent);
                userChatView.updateUnReadCount(0);
                Context context = userChatView.getContext();
                if(context instanceof MainActivity) {
                    MainActivity activity = (MainActivity) context;
                    activity.reduceUnReadCount(userChatView.getUnReadCount());
                }
            }
        }
    };

    public UserChatView(Context context) {
        super(context);
        init();
    }

    public UserChatView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public UserChatView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public UserChatView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        this.setOrientation(HORIZONTAL);
        this.setOnClickListener(onClickListener); 
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                DensityUtils.dpToPx(60));
        this.setLayoutParams(layoutParams);

        head = new AdaptionImageView(getContext());
        LayoutParams headLayoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        head.setLayoutParams(headLayoutParams);
        head.setCornerRadius(DensityUtils.dpToPx(10));
        this.addView(head);

        LayoutParams unReadCountTextLayoutParams = new LayoutParams(DensityUtils.dpToPx(20),
                DensityUtils.dpToPx(20));
        unReadCountTextLayoutParams.setMargins(DensityUtils.dpToPx(-10), 0, 0, 0);
        unReadCountText = new TextView(getContext());
        unReadCountText.setLayoutParams(unReadCountTextLayoutParams);
        unReadCountText.setBackgroundResource(R.drawable.shape_circle);
        unReadCountText.setTextColor(Color.WHITE);
        unReadCountText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
        unReadCountText.setGravity(Gravity.CENTER);
        int padding = DensityUtils.dpToPx(3);
        unReadCountText.setPadding(padding, padding, padding, padding);
        this.addView(unReadCountText);

        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(VERTICAL);
        userName = new TextView(getContext());
        userName.setPadding(DensityUtils.dpToPx(10), 0, 0, 0);
        userName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        userName.setTextColor(Color.BLACK);
        linearLayout.addView(userName);

        firstMessage = new TextView(getContext());
        firstMessage.setPadding(DensityUtils.dpToPx(10), 0, 0, 0);
        firstMessage.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        firstMessage.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        firstMessage.setMaxLines(1);
        linearLayout.addView(firstMessage);

        latestTime = new TextView(getContext());
        latestTime.setPadding(DensityUtils.dpToPx(10), 0, 0, 0);
        latestTime.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
        linearLayout.addView(latestTime);
        linearLayout.setPadding(0, 0, DensityUtils.dpToPx(10) , 0);

        LayoutParams layoutparams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.weight = 1;
        linearLayout.setLayoutParams(layoutParams);
        this.addView(linearLayout);

        goodsCover = new AdaptionImageView(getContext());
        LayoutParams coverLayoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        goodsCover.setLayoutParams(coverLayoutParams);
        this.addView(goodsCover);
    }

    public void bindData(String chatId) {
        Goods goods = null;
        User chatUser = null;
        int userSelfId = UserUtils.getUser().getUserId();
        if(chatId != null && chatId.length() > 0) {
            String[] splits = chatId.split(":", 3);
            int preUserId = Integer.parseInt(splits[0]);
            int goodsId = Integer.parseInt(splits[1]);
            int afterUserId = Integer.parseInt(splits[2]);
            if(preUserId != userSelfId) {
                chatUser = DataSourceUtils.getUser(preUserId);
            }else if(afterUserId != userSelfId){
                chatUser = DataSourceUtils.getUser(afterUserId);
            }else {
                chatUser = UserUtils.getUser();
            }
            goods = DataSourceUtils.getGoods(goodsId);
        }

        this.chatId = chatId;
        this.chatUser = chatUser;
        this.goods = goods;

        if(goods != null) {
            String coverUrl = ImageUtils.getCoverByStr(goods.getImageUrl());
            coverUrl = getResources().getString(R.string.image_get_url) + coverUrl;
            goodsCover.setImageFromUrl(coverUrl);
        }else {

        }

        if(chatUser != null) {
            head.setImageFromUrl(chatUser.getHeadUrl());
            userName.setText(chatUser.getUserName());
            List<Message> messageList = MessageDAO.instance.queryMessage(chatId, 0, 1);
            if(messageList != null && messageList.size() > 0) {
                Message message = messageList.get(0);
                if(Message.CONTENT_TYPE_IMAGE.equals(message.getContentType())) {
                    firstMessage.setText(Message.CONTENT_TYPE_IMAGE);
                }else {
                    firstMessage.setText(message.getContent());
                }
                latestTime.setText(TimeUtils.covertWithNow(message.getTimeStamp()));
            }
        }

        unReadCount = MessageDAO.instance.queryUnReadCountByChatId(chatId);
        updateUnReadCount(unReadCount);
        if(unReadCount == 0) {
            unReadCountText.setVisibility(INVISIBLE);
        }else if(unReadCount <= 99) {
            unReadCountText.setText(String.valueOf(unReadCount));
            unReadCountText.setVisibility(VISIBLE);
        }else {
            unReadCountText.setText("99+");
            unReadCountText.setVisibility(VISIBLE);
        }
    }

    public void updateUnReadCount(int unReadCount) {
        if(unReadCount == 0) {
            unReadCountText.setVisibility(View.INVISIBLE);
        }else if (unReadCount <= 99){
            unReadCountText.setText(String.valueOf(unReadCount));
            unReadCountText.setVisibility(View.VISIBLE);
        }else {
            unReadCountText.setText("99");
            unReadCountText.setVisibility(View.VISIBLE);
        }
    }

    public String getChatId() {
        return chatId;
    }

    public User getUser() {
        return chatUser;
    }

    public Goods getGoods() {
        return goods;
    }

    public int getUnReadCount() {
        return unReadCount;
    }
}
