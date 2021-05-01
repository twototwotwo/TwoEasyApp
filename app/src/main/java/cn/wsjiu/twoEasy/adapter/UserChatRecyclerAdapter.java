package cn.wsjiu.twoEasy.adapter;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import cn.wsjiu.twoEasy.adapter.common.RecyclerViewHolder;
import cn.wsjiu.twoEasy.component.UserChatView;
import cn.wsjiu.twoEasy.entity.Order;
import cn.wsjiu.twoEasy.entity.OrderState;
import cn.wsjiu.twoEasy.util.DataSourceUtils;

public class UserChatRecyclerAdapter extends RecyclerView.Adapter {
    List<String> chatIdList = new ArrayList<>();
    Integer userSelfId;

    public UserChatRecyclerAdapter(Integer userSelfId) {
        init(userSelfId);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = new UserChatView(parent.getContext());
        return new RecyclerViewHolder(view);
    }

    public void init(Integer userSelfId) {
        this.userSelfId = userSelfId;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        String chatId = chatIdList.get(position);
        UserChatView userChatView = (UserChatView) holder.itemView;
        userChatView.bindData(chatId);
    }

    @Override
    public int getItemCount() {
        return chatIdList.size();
    }

    public void add(String chatId) {
        chatIdList.add(chatId);
        notifyDataSetChanged();
    }

    public void addAll(List<String> chatIdList) {
        boolean haveNew = false;
        for (String chatId : chatIdList
             ) {
            if (!this.chatIdList.contains(chatId)) {
                this.chatIdList.add(chatId);
                haveNew = true;
            }
        }
        if (haveNew) {
            notifyDataSetChanged();
        }
    }

    public int getPosition(String chatId) {
        return chatIdList.indexOf(chatId);
    }

    public void remove(int position) {
        chatIdList.remove(position);
        notifyItemRemoved(position);
    }

    /**
     * 高8位为沟通中，低8位为交易中
     * @return 返回处于交易中和沟通中的聊天记录的计数
     */
    public int countChatClassification() {
        int count = 0;
        for(String chatId : chatIdList) {
            String[] splits = chatId.split(":");
            int goodsId = Integer.parseInt(splits[1]);
            Order order = DataSourceUtils.getOrder(goodsId);
            if(order != null && order.getState() == OrderState.TRANSACTION_IN.mask) {
                count++;
            }else {
                count += 1<<8;
            }
        }
        return count;
    }
}
