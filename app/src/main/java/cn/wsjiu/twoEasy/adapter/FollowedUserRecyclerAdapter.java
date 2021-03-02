package cn.wsjiu.twoEasy.adapter;

import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import cn.wsjiu.twoEasy.R;
import cn.wsjiu.twoEasy.adapter.common.RecyclerViewHolder;
import cn.wsjiu.twoEasy.component.FollowedUserView;
import cn.wsjiu.twoEasy.component.LoadingBeanner;
import cn.wsjiu.twoEasy.entity.User;

public class FollowedUserRecyclerAdapter extends RecyclerView.Adapter<RecyclerViewHolder> {
    private static final int TYPE_LOADING = 1;
    private static final int TYPE_COMMON= 1 << 1;
    private static final int TYPE_EMPTY = 1 << 2;

    private View defaultEmptyView;
    private View defaultLoadingView;
    private boolean isEnd = false;
    private List<User> userList;

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;
        if(viewType == TYPE_COMMON) {
            itemView = new FollowedUserView(parent.getContext());
        }else if(viewType == TYPE_EMPTY){
            if(defaultEmptyView == null) {
                TextView emptyTextView = new TextView(parent.getContext());
                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                emptyTextView.setLayoutParams(layoutParams);
                emptyTextView.setGravity(Gravity.CENTER);
                emptyTextView.setText("没有关注");
                emptyTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                defaultEmptyView = emptyTextView;
            }
            itemView = defaultEmptyView;
        }else {
            if(defaultLoadingView == null) defaultLoadingView = LoadingBeanner.make(parent.getContext(), 0, R.color.transparent);
            itemView = defaultLoadingView;
        }
        return new RecyclerViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
        View view = holder.itemView;
        switch (holder.getItemViewType()) {
            case TYPE_COMMON:
                FollowedUserView followedUserView = (FollowedUserView) view;
                followedUserView.bindData(userList.get(position));
                break;
            case TYPE_LOADING:
                break;
        }
    }

    @Override
    public int getItemCount() {
        int size = userList != null ? userList.size() : 0;
        if(isEnd && size != 0) return size;
        else return size + 1;
    }

    @Override
    public int getItemViewType(int position) {
        int size = getItemCount();
        int userCount = userList == null ? 0 : userList.size();
        if(userCount == 0 && size == 1 && position == 0) {
            return TYPE_EMPTY;
        }else if(position == userCount){
            return TYPE_LOADING;
        }else {
            return TYPE_COMMON;
        }
    }

    public void addAll(List<User> userList) {
        if(this.userList == null) this.userList = new ArrayList<>();
        this.userList.addAll(userList);
        notifyDataSetChanged();
    }

    public void setEnd(boolean isEnd) {
        if(this.isEnd == isEnd) return;
        if(isEnd) {
            notifyDataSetChanged();
        }
        this.isEnd = isEnd;
    }
}
