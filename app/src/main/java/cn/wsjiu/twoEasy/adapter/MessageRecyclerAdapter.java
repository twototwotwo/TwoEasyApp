package cn.wsjiu.twoEasy.adapter;

import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import cn.wsjiu.twoEasy.adapter.common.RecyclerViewHolder;
import cn.wsjiu.twoEasy.component.ChatMessageView;
import cn.wsjiu.twoEasy.entity.IM.Message;
import cn.wsjiu.twoEasy.entity.User;

public class MessageRecyclerAdapter extends RecyclerView.Adapter {
    /**
     * 消息列表
     */
    private ArrayList<Message> messageList;
    /**
     * 用于排序
     */
    private Comparator<Message> comparator;
    /**
     * 聊天对象
     */
    private User targetUser;

    public MessageRecyclerAdapter(User user) {
        super();
        comparator = (m1, m2)-> {
            return m1.getTimeStamp().compareTo(m2.getTimeStamp());
        };
        targetUser = user;
        messageList = new ArrayList<>();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ChatMessageView chatMessageView = new ChatMessageView(parent.getContext());
        chatMessageView.init();
        return new RecyclerViewHolder(chatMessageView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);
        if(message != null) {
            ChatMessageView view = (ChatMessageView) holder.itemView;
            view.bindData(message);
        }
    }


    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public void addFirst(Message message) {
        messageList.add(0, message);
        notifyDataSetChanged();
    }

    public void addItems(List<Message> messageList) {
        this.messageList.addAll(messageList);
        notifyItemRangeInserted(getItemCount(), messageList.size());
    }

    public void clearItems() {
        messageList.clear();
        notifyDataSetChanged();
    }
}
