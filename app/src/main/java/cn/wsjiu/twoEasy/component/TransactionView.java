package cn.wsjiu.twoEasy.component;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;

import cn.wsjiu.twoEasy.R;
import cn.wsjiu.twoEasy.activity.ChatActivity;
import cn.wsjiu.twoEasy.entity.Goods;
import cn.wsjiu.twoEasy.entity.GoodsState;
import cn.wsjiu.twoEasy.entity.Order;
import cn.wsjiu.twoEasy.entity.OrderState;
import cn.wsjiu.twoEasy.entity.TransactionMode;
import cn.wsjiu.twoEasy.entity.User;
import cn.wsjiu.twoEasy.result.Result;
import cn.wsjiu.twoEasy.thread.HttpPostRunnable;
import cn.wsjiu.twoEasy.thread.threadPool.ThreadPoolUtils;
import cn.wsjiu.twoEasy.util.DataSourceUtils;
import cn.wsjiu.twoEasy.util.DensityUtils;
import cn.wsjiu.twoEasy.util.UserUtils;

public class TransactionView extends FrameLayout {
    private Goods goods;
    private User user;
    private Order order;
    private ChatActivity chatActivity;
    private ArrayAdapter<String> adapter;

    public TransactionView(Context context) {
        super(context);
    }

    public TransactionView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TransactionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TransactionView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void init(Goods goods, User user, ChatActivity chatActivity) {
        this.goods = goods;
        this.user = user;
        this.chatActivity = chatActivity;
        inflate(getContext(), R.layout.view_transaction, this);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        this.setLayoutParams(layoutParams);
        TextView sellPriceView = findViewById(R.id.sell_price_view);
        sellPriceView.setText(String.format("¥%s", goods.getSellPrice()));
        TextView titleView = findViewById(R.id.title_view);
        titleView.setText(goods.getTitle());
        TextView detailView = findViewById(R.id.detail_view);
        detailView.setText(goods.getDetail());
        TextView sellerNameView = findViewById(R.id.seller_name_view);
        sellerNameView.setText(user.getUserName());

        Spinner transactionModeSpinner = findViewById(R.id.transaction_mode_spinner);
        transactionModeSpinner.setDropDownVerticalOffset(DensityUtils.dpToPx(20));
        adapter = new ArrayAdapter<>(getContext(), R.layout.support_simple_spinner_dropdown_item);
        int mode = goods.getTransactionMode();
        for (TransactionMode transactionMode : TransactionMode.values()
        ) {
            if((mode & transactionMode.mode) == 0) {
                adapter.add(transactionMode.modeStr);
            }
        }

        transactionModeSpinner.setAdapter(adapter);
        int currentPosition = transactionModeSpinner.getSelectedItemPosition();
        changeSiteView(currentPosition);
        transactionModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String str = (String) parent.getAdapter().getItem(position);
                changeSiteView(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



        Button confirmButton = findViewById(R.id.confirm_button);
        confirmButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmBuy();
            }
        });

        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setVisibility(INVISIBLE);
            }
        });
    }

    public void confirmBuy() {
        order = new Order();
        order.setGoodsId(goods.getGoodsId());
        order.setSellerId(goods.getUserId());
        order.setBuyerId(UserUtils.getUser().getUserId());
        order.setState(OrderState.TRANSACTION_IN.mask);
        order.setPrice(goods.getSellPrice());
        EditText phoneNumberView = findViewById(R.id.phone_number_edit_text);
        String phoneNumber = phoneNumberView.getText().toString();
        if(phoneNumber.length() == 0) {
            Toast.makeText(getContext(), "请输入电话号码", Toast.LENGTH_SHORT).show();
            return;
        }
        order.setPhoneNumber(phoneNumberView.getText().toString());
        Spinner transactionModeSpinner = findViewById(R.id.transaction_mode_spinner);
        int selectedPosition = transactionModeSpinner.getSelectedItemPosition();
        String transactionModeStr = adapter.getItem(selectedPosition);
        for (TransactionMode transactionMode : TransactionMode.values()
        ) {
            if(transactionMode.modeStr.equals(transactionModeStr)) {
                order.setTransactionMode(transactionMode.mode);
                break;
            }
        }
        if(order.getTransactionMode() == null) {
            order.setTransactionMode(TransactionMode.TRANSACTION_CUSTOM_MODE.mode);
        }
        EditText siteView = findViewById(R.id.site_edit_text);
        if(TransactionMode.TRANSACTION_CUSTOM_MODE.mode != order.getTransactionMode()) {
            String site = siteView.getText().toString();
            if(site.length() == 0) {
                Toast.makeText(getContext(), "请填写地址", Toast.LENGTH_SHORT).show();
                return;
            }
            order.setSite(site);
        }
        EditText payPasswordEditText = findViewById(R.id.pay_password_edit_text);
        String payPassword = payPasswordEditText.getText().toString();
        if(payPassword.length() < 6) {
            Toast.makeText(getContext(), "请输入完整的支付密码", Toast.LENGTH_SHORT).show();
        }else {
            order.setPayPassword(payPassword);
            String url = getResources().getString(R.string.order_create_url);
            Handler handler = new Handler(getContext().getMainLooper(), this::handlerForCreateOrder);
            HttpPostRunnable<Order, Void> runnable = new HttpPostRunnable<>(url, handler, order);
            ThreadPoolUtils.asynExecute(runnable);
        }
    }

    private void changeSiteView(int position) {
        String modeStr = adapter.getItem(position);
        LinearLayout siteView = findViewById(R.id.site_view);
        if(TransactionMode.TRANSACTION_CUSTOM_MODE.modeStr.equals(modeStr)) {
            siteView.setVisibility(GONE);
        }else {
            siteView.setVisibility(VISIBLE);
        }
    }

    public boolean handlerForCreateOrder(Message message) {
        Object object = message.obj;
        if(object instanceof Result) {
            Result<Void> result = (Result) object;
            if(result.isSuccess()) {
                Toast.makeText(getContext(), "订单已生成", Toast.LENGTH_SHORT).show();
                goods.setState(GoodsState.TRANSACTION.mask);
                DataSourceUtils.addCommonGoods(goods);
                DataSourceUtils.addOrder(order);
                chatActivity.checkGoodsState();
                this.setVisibility(GONE);
            }else {
                Toast.makeText(getContext(), "交易失败，" + result.getMsg(), Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return true;
    }
}
