package cn.wsjiu.twoEasy.component;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import cn.wsjiu.twoEasy.R;
import cn.wsjiu.twoEasy.activity.ChatActivity;
import cn.wsjiu.twoEasy.adapter.valid.TransactionOrderAdapterValid;
import cn.wsjiu.twoEasy.entity.Goods;
import cn.wsjiu.twoEasy.entity.GoodsState;
import cn.wsjiu.twoEasy.entity.Order;
import cn.wsjiu.twoEasy.entity.OrderComment;
import cn.wsjiu.twoEasy.entity.OrderState;
import cn.wsjiu.twoEasy.entity.TransactionMode;
import cn.wsjiu.twoEasy.entity.User;
import cn.wsjiu.twoEasy.result.Result;
import cn.wsjiu.twoEasy.thread.HttpGetRunnable;
import cn.wsjiu.twoEasy.thread.HttpPostRunnable;
import cn.wsjiu.twoEasy.thread.threadPool.ThreadPoolUtils;
import cn.wsjiu.twoEasy.util.DataSourceUtils;
import cn.wsjiu.twoEasy.util.ImageUtils;
import cn.wsjiu.twoEasy.util.UserUtils;

public class TransactionOrderView extends LinearLayout {
    private Order order;
    private TransactionOrderAdapterValid adapter;
    private AlertDialog commentDialog;

    public TransactionOrderView(Context context) {
        super(context);
        init();
    }

    public TransactionOrderView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TransactionOrderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public TransactionOrderView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public void setAdapter(TransactionOrderAdapterValid adapter) {
        this.adapter = adapter;
    }

    private void init() {
        inflate(getContext(), R.layout.view_order_transaction, this);
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setLayoutParams(layoutParams);
    }

    public void bindData(Order order) {
        this.order = order;
        Goods goods = DataSourceUtils.getGoods(order.getGoodsId());
        User user = DataSourceUtils.getUser(order.getSellerId());

        AdaptionImageView headView = findViewById(R.id.head_image_view);
        headView.setImageFromUrl(user.getHeadUrl());

        AdaptionImageView coverView = findViewById(R.id.goods_cover_view);
        String url = getResources().getString(R.string.image_get_url);
        String imageName = ImageUtils.getCoverByStr(goods.getImageUrl());
        url += imageName;
        coverView.setImageFromUrl(url);

        TextView userNameView = findViewById(R.id.user_name_view);
        userNameView.setText(user.getUserName());

        TextView titleView = findViewById(R.id.title_view);
        titleView.setText(goods.getTitle());

        TextView orderStateView = findViewById(R.id.order_state_view);
        for (OrderState orderState : OrderState.values()
             ) {
            if((orderState.mask & order.getState()) > 0) {
                orderStateView.setText(orderState.state);
            }
        }

        TextView priceView = findViewById(R.id.price_view);
        priceView.setText("¥" + goods.getSellPrice().toString());

        TextView transactionModeView = findViewById(R.id.transaction_mode_view);
        for(TransactionMode transactionMode : TransactionMode.values()) {
            if((transactionMode.mode & order.getTransactionMode()) > 0) {
                if(transactionMode == TransactionMode.TRANSACTION_EXPRESS_MODE) {
                    initExpressView(true);
                }else {
                    initExpressView(false);
                }
                transactionModeView.setText("交易模式: " + transactionMode.modeStr);
            }
        }

        TextView phoneNumberView = findViewById(R.id.phone_number_view);
        phoneNumberView.setText("联系电话: " + order.getPhoneNumber());

        TextView siteView = findViewById(R.id.site_view);
        if(order.getSite() != null && order.getSite().length() != 0) {
            siteView.setText("交易地址: " + order.getSite());
        }else {
            siteView.setText("交易地址：无");
        }

        TextView orderTimeView = findViewById(R.id.order_time_view);
        orderTimeView.setText("订单时间: " + order.getTime().toString());

        Button cancelButton = findViewById(R.id.cancel_button);
        Button finishButton = findViewById(R.id.finish_button);
        Button chatButton = findViewById(R.id.chat_button);
        Button commentButton = findViewById(R.id.comment_button);

        int userSelfId = UserUtils.getUser().getUserId();

        if(OrderState.TRANSACTION_IN.mask == order.getState()) {
            finishButton.setVisibility(VISIBLE);
            cancelButton.setVisibility(VISIBLE);
            if(userSelfId == order.getSellerId()) {
                finishButton.setVisibility(INVISIBLE);
            }else {
                cancelButton.setVisibility(INVISIBLE);
            }
        }else {
            finishButton.setVisibility(INVISIBLE);
            cancelButton.setVisibility(INVISIBLE);
        }
        if(OrderState.TRANSACTION_FINISH.mask == order.getState()) {
            commentButton.setVisibility(VISIBLE);
        }else {
            commentButton.setVisibility(INVISIBLE);
        }

        finishButton.setOnClickListener(this::cancelOrFinish);
        cancelButton.setOnClickListener(this::cancelOrFinish);
        chatButton.setOnClickListener(this::chatWithSellerOrBuyer);
        commentButton.setOnClickListener(this::comment);
    }

    private void initExpressView(boolean isExpress) {
        LinearLayout expressView = findViewById(R.id.express_view);
        if(!isExpress) {
            expressView.setVisibility(INVISIBLE);
            expressView.getLayoutParams().height = 0;
            return;
        }
        expressView.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
        EditText expressNumberEditText = findViewById(R.id.express_number_edit_view);
        expressNumberEditText.setEnabled(false);
        TextView expressNumberEditButton = findViewById(R.id.express_number_edit_button);
        if(order.getSellerId().equals(UserUtils.getUser().getUserId())) {
            expressNumberEditButton.setOnClickListener(this::editExpressNumber);
            expressNumberEditButton.setVisibility(VISIBLE);
            if(order.getExpressNumber() == null || order.getExpressNumber().length() == 0 ) {
                expressNumberEditText.setHint("请填写");
            }else {
                expressNumberEditText.setText(order.getExpressNumber());
            }
        }else {
            expressNumberEditButton.setVisibility(INVISIBLE);
            if(order.getExpressNumber() == null || order.getExpressNumber().length() == 0 ) {
                expressNumberEditText.setHint("卖家还未填写");
            }else {
                expressNumberEditText.setHint(order.getExpressNumber());
            }
        }
    }

    public void cancelOrFinish(View view) {
        String url;
        Handler handler;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(R.layout.dialog_confirm);
        AlertDialog dialog = builder.create();
        dialog.show();
        //dialog.getWindow().setBackgroundDrawableResource(R.color.transparent);
        TextView content = dialog.findViewById(R.id.dialog_confirm_content);
        Button confirmButton = dialog.findViewById(R.id.dialog_confirm_button);
        Button cancelButton = dialog.findViewById(R.id.dialog_cancel_button);
        if(view.getId() == R.id.cancel_button) {
            content.setText("确认取消吗");
            url = getResources().getString(R.string.order_cancel_url);
            handler = new Handler(getContext().getMainLooper(), this::handlerCancelEvent);
        }else if(view.getId() == R.id.finish_button) {
            content.setText("确认交易完成吗");
            url = getResources().getString(R.string.order_finish_url);
            handler = new Handler(getContext().getMainLooper(), this::handlerFinishEvent);
        }else {
            content.setText("确认交易完成吗");
            url = getResources().getString(R.string.order_finish_url);
            handler = new Handler(getContext().getMainLooper(), this::handlerFinishEvent);
        }
        url += "?orderId=" + order.getOrderId() + "&goodsId=" + order.getGoodsId();
        String finalUrl = url;
        OnClickListener listener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId() == R.id.dialog_confirm_button) {
                    HttpGetRunnable httpGetRunnable = new HttpGetRunnable(finalUrl, handler);
                    ThreadPoolUtils.asynExecute(httpGetRunnable);
                }
                dialog.cancel();
            }
        };
        confirmButton.setOnClickListener(listener);
        cancelButton.setOnClickListener(listener);
    }

    public void editExpressNumber(View view) {
        EditText expressNumberEditText = findViewById(R.id.express_number_edit_view);
        TextView expressNumberEditButton = findViewById(R.id.express_number_edit_button);
        if(expressNumberEditText.isEnabled()) {
            // TODO 修改快递单号
            expressNumberEditText.setEnabled(false);
            expressNumberEditButton.setText("编辑");
            String expressNumber = expressNumberEditText.getText().toString();
            if(expressNumber != null && expressNumber.length() > 0) {
                String url = getResources().getString(R.string.order_update_express_number_url);
                url += "?orderId=" + order.getOrderId() + "&expressNumber=" + expressNumber;
                Handler handler = new Handler(getContext().getMainLooper(), this::handlerUpdateEvent);
                HttpGetRunnable runnable = new HttpGetRunnable(url, handler);
                ThreadPoolUtils.asynExecute(runnable);
            }
        }else {
            expressNumberEditText.setEnabled(true);
            expressNumberEditButton.setText("确认");
        }
    }

    public void comment(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(R.layout.dialog_comment);
        commentDialog = builder.create();
        commentDialog.show();
        TextView submitButton = commentDialog.findViewById(R.id.dialog_submit_button);
        TextView cancelButton = commentDialog.findViewById(R.id.dialog_cancel_button);
        OnClickListener listener = v -> {
            if(v.getId() == R.id.dialog_submit_button) {
                EditText commentContentView = commentDialog.findViewById(R.id.comment_content_view);
                String commentContent = commentContentView.getText().toString();
                if(commentContent.length() > 0) {
                    String url = getResources().getString(R.string.order_comment_publish_url);
                    Handler handler = new Handler(getContext().getMainLooper(), this::handlerCommentEvent);
                    OrderComment comment = new OrderComment();
                    comment.setOrderId(order.getOrderId());
                    comment.setContent(commentContent);
                    comment.setBuyerId(order.getBuyerId());
                    comment.setSellerId(order.getSellerId());
                    HttpPostRunnable<OrderComment, Void> runnable = new HttpPostRunnable<>(url, handler, comment);
                    ThreadPoolUtils.synExecute(runnable);
                }else {
                    Toast.makeText(getContext(), "评论不能为空", Toast.LENGTH_SHORT).show();
                }
            }else {
                commentDialog.cancel();
            }
        };
        submitButton.setOnClickListener(listener);
        cancelButton.setOnClickListener(listener);
    }

    public void chatWithSellerOrBuyer(View view) {
        int userSelfId = UserUtils.getUser().getUserId();
        User  chatUser = null;
        if(order.getBuyerId() == userSelfId) {
            chatUser = DataSourceUtils.getUser(order.getSellerId());
        }else {
            chatUser = DataSourceUtils.getUser(order.getBuyerId());
        }
        Goods goods = DataSourceUtils.getGoods(order.getGoodsId());
        Intent intent = new Intent();
        intent.putExtra("user", chatUser);
        intent.putExtra("goods", goods);
        intent.setClass(getContext(), ChatActivity.class);
        getContext().startActivity(intent);
    }

    public boolean handlerCancelEvent(Message msg) {
        Object obj = msg.obj;
        if(obj instanceof Result) {
            Result result = (Result) obj;
            if(result.isSuccess()) {
                order.setState(OrderState.TRANSACTION_CANCEL.mask);
                Goods goods = DataSourceUtils.getGoods(order.getGoodsId());
                goods.setState(GoodsState.UNSOLD.mask);
                DataSourceUtils.addOrder(order);
                adapter.removeItem(order);
                return true;
            }else {
                Toast.makeText(getContext(), result.getMsg(), Toast.LENGTH_SHORT).show();
            }
        }
        return true;
    }

    public boolean handlerFinishEvent(Message msg) {
        Object obj = msg.obj;
        if(obj instanceof Result) {
            Result result = (Result) obj;
            if(result.isSuccess()) {
                order.setState(OrderState.TRANSACTION_FINISH.mask);
                Goods goods = DataSourceUtils.getGoods(order.getGoodsId());
                goods.setState(GoodsState.SOLD.mask);
                DataSourceUtils.addOrder(order);
                adapter.removeItem(order);
                return true;
            }else {
                Toast.makeText(getContext(), result.getMsg(), Toast.LENGTH_SHORT).show();
            }
        }
        return true;
    }

    public boolean handlerUpdateEvent(Message msg) {
        Object obj = msg.obj;
        if(obj instanceof Result) {
            Result result = (Result) obj;
            if(result.isSuccess()) {
                return true;
            }else {
                EditText expressNumberEditText = findViewById(R.id.express_number_edit_view);
                expressNumberEditText.setText("");
                Toast.makeText(getContext(), result.getMsg(), Toast.LENGTH_SHORT).show();
            }
        }
        return true;
    }

    public boolean handlerCommentEvent(Message msg) {
        Object obj = msg.obj;
        if(obj instanceof Result) {
            Result result = (Result) obj;
            if(result.isSuccess()) {
                order.setState(OrderState.TRANSACTION_COMMENTED.mask);
                Button commentButton = findViewById(R.id.comment_button);
                TextView orderStateView = findViewById(R.id.order_state_view);
                orderStateView.setText(OrderState.TRANSACTION_COMMENTED.state);
                commentButton.setVisibility(INVISIBLE);
                if(commentDialog != null) commentDialog.cancel();
            }
        }
        return true;
    }
}
