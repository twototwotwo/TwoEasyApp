package cn.wsjiu.twoEasy.activity.ui.main;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import cn.wsjiu.twoEasy.R;
import cn.wsjiu.twoEasy.adapter.UserChatRecyclerAdapter;
import cn.wsjiu.twoEasy.adapter.common.SpaceDecoration;
import cn.wsjiu.twoEasy.dao.MessageDAO;
import cn.wsjiu.twoEasy.entity.Goods;
import cn.wsjiu.twoEasy.entity.Order;
import cn.wsjiu.twoEasy.entity.User;
import cn.wsjiu.twoEasy.result.Result;
import cn.wsjiu.twoEasy.thread.HttpGetRunnable;
import cn.wsjiu.twoEasy.thread.HttpPostRunnable;
import cn.wsjiu.twoEasy.thread.threadPool.ThreadPoolUtils;
import cn.wsjiu.twoEasy.util.DataSourceUtils;
import cn.wsjiu.twoEasy.util.UserUtils;

/**
 * create an instance of this fragment.
 */
public class ChatFragment extends Fragment {

    private View rootView;

    private UserChatRecyclerAdapter adapter;

    private TextView communicateCountView;
    private TextView inTransactionCountView;

    /**
     * 数据准备就绪的标志位
     */
    private boolean dataIsReady = false;

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_chat, container, false);
        init();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        List<String> chatIdList;
        MessageDAO messageDAO = MessageDAO.instance;
        chatIdList = messageDAO.queryAllChatId();
        // TODO 获取交易中
        adapter.addAll(chatIdList);
        checkChatId(chatIdList);

        updateChatClassificationCount();
    }

    /**
     * 初始化
     */
    private void init() {
        RecyclerView userChatRecyclerView = rootView.findViewById(R.id.chat_recycler_view);
        adapter = new UserChatRecyclerAdapter(UserUtils.getUser().getUserId());
        userChatRecyclerView.setAdapter(adapter);
        userChatRecyclerView.addItemDecoration(new SpaceDecoration(SpaceDecoration.VERTICAL));
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        manager.setOrientation(RecyclerView.VERTICAL);
        userChatRecyclerView.setLayoutManager(manager);
        inTransactionCountView = rootView.findViewById(R.id.transaction_count_view);
        communicateCountView = rootView.findViewById(R.id.communicate_count_view);
    }

    private void checkChatId(List<String> chatIdList) {
        Set<Integer> userIdSet = new HashSet<>(chatIdList.size());
        Set<Integer> goodsIdSet = new HashSet<>(chatIdList.size());
        for(String chatId : chatIdList) {
            if(chatId != null) {
                String[] splits = chatId.split(":", 3);
                if(splits[0] != null && splits[0].length() > 0) {
                    int preUserId = Integer.parseInt(splits[0]);
                    userIdSet.add(preUserId);
                }
                if(splits[1] != null && splits[1].length() > 0) {
                    int goodsId = Integer.parseInt(splits[1]);
                    goodsIdSet.add(goodsId);
                }
                if(splits[2] != null && splits[2].length() > 0) {
                    int lastUserId = Integer.parseInt(splits[2]);
                    userIdSet.add(lastUserId);
                }
            }
        }
        String url = getString(R.string.goods_get_with_user_url);
        if(userIdSet.size() == 0 && goodsIdSet.size() == 0) return;
        try {
            url += "?userIdSet=" +
                    URLEncoder.encode(JSONObject.toJSONString(userIdSet),
                            StandardCharsets.UTF_8.name())
                    + "&goodsIdSet=" +
                    URLEncoder.encode(JSONObject.toJSONString(goodsIdSet),
                            StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Handler handler = new Handler(Objects.requireNonNull(getContext()).getMainLooper(), this::handleGoods);
        HttpGetRunnable runnable = new HttpGetRunnable(url, handler);
        ThreadPoolUtils.asynExecute(runnable);
    }

    public void updateChatClassificationCount() {
        int count = adapter.countChatClassification();
        int inTransactionCount = (count & 0xff);
        int communicateCount = count >>> 8;
        inTransactionCountView.setText(String.valueOf(inTransactionCount));
        communicateCountView.setText(String.valueOf(communicateCount));
    }

    public void addChatMessageByChatId(String chatId) {
        int position = adapter.getPosition(chatId);
        if(position >= 0) {
            adapter.notifyItemChanged(position);
        }else {
            List<String> chatIdList = new ArrayList<>(1);
            chatIdList.add(chatId);
            adapter.add(chatId);
            checkChatId(chatIdList);
        }
    }

    private boolean handleGoods(Message message) {
        Object obj = message.obj;
        if(obj instanceof Result) {
            Result result = (Result)obj;
            if(result.isSuccess()) {
                JSONObject dataJSONObject = (JSONObject) result.getData();
                if(dataJSONObject != null) {
                    JSONObject goodsMapJSONObject = dataJSONObject.getJSONObject("goods");
                    for(String key : goodsMapJSONObject.keySet()) {
                        Goods goods = goodsMapJSONObject.getObject(key, Goods.class);
                        if(goods != null) {
                            DataSourceUtils.commonGoodsMap.put(Integer.parseInt(key), goods);
                        }
                    }
                    JSONObject userMapJSONObject = dataJSONObject.getJSONObject("user");
                    for(String key : userMapJSONObject.keySet()) {
                        User user = userMapJSONObject.getObject(key, User.class);
                        if(user != null) {
                            DataSourceUtils.userMap.put(Integer.parseInt(key), user);
                        }
                    }
                    // 通知数据变动
                    adapter.notifyDataSetChanged();
                }
            }
        }
        return false;
    }


}