package cn.wsjiu.twoEasy.adapter;

import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.util.ArrayList;
import java.util.List;

import cn.wsjiu.twoEasy.R;
import cn.wsjiu.twoEasy.adapter.common.RecyclerViewHolder;
import cn.wsjiu.twoEasy.component.CommentView;
import cn.wsjiu.twoEasy.component.GoodsView;
import cn.wsjiu.twoEasy.component.LoadingBeanner;
import cn.wsjiu.twoEasy.entity.Goods;
import cn.wsjiu.twoEasy.entity.OrderComment;
import cn.wsjiu.twoEasy.entity.User;

public class CommentRecyclerAdapter extends RecyclerView.Adapter<RecyclerViewHolder> {
    private static final int TYPE_LOADING = 1;
    private static final int TYPE_COMMON= 1 << 1;
    private static final int TYPE_EMPTY = 1 << 2;

    private View defaultEmptyView;
    private View defaultLoadingView;
    private boolean isEnd = false;

    private final List<OrderComment> orderCommentList;

    public CommentRecyclerAdapter() {
        orderCommentList = new ArrayList<>(8);
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;
        if(viewType == TYPE_COMMON) {
            itemView = new CommentView(parent.getContext());
        }else if(viewType == TYPE_EMPTY){
            if(defaultEmptyView == null) {
                TextView emptyTextView = new TextView(parent.getContext());
                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                emptyTextView.setLayoutParams(layoutParams);
                emptyTextView.setGravity(Gravity.CENTER);
                emptyTextView.setText("空空如也");
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
    public void onViewRecycled(@NonNull RecyclerViewHolder holder) {
        super.onViewRecycled(holder);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case TYPE_COMMON:
                OrderComment orderComment = orderCommentList.get(position);
                CommentView commentView = (CommentView) holder.itemView;
                commentView.bindData(orderComment);
                break;
            case TYPE_LOADING:
                break;
        }
    }

    @Override
    public int getItemCount() {
        int size = orderCommentList != null ? orderCommentList.size() : 0;
        if(isEnd && size != 0) return size;
        else return size + 1;
    }

    @Override
    public void onViewAttachedToWindow(@NonNull RecyclerViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
        int itemVIewType = holder.getItemViewType();
        if(itemVIewType != TYPE_COMMON &&
                layoutParams instanceof StaggeredGridLayoutManager.LayoutParams) {
            StaggeredGridLayoutManager.LayoutParams lp = (StaggeredGridLayoutManager.LayoutParams)layoutParams;
            lp.setFullSpan(true);
            holder.itemView.setLayoutParams(lp);
        }
    }

    @Override
    public int getItemViewType(int position) {
        int size = getItemCount();
        int count = orderCommentList == null ? 0 : orderCommentList.size();
        if(count == 0 && size == 1 && position == 0) {
            return TYPE_EMPTY;
        }else if(position == count){
            return TYPE_LOADING;
        }else {
            return TYPE_COMMON;
        }
    }

    public void addItem(OrderComment orderComment) {
        orderCommentList.add(orderComment);
        notifyDataSetChanged();
    }

    public void addItems(List<OrderComment> orderCommentList) {
        this.orderCommentList.addAll(orderCommentList);
        notifyDataSetChanged();
    }

    public void clearData() {
        orderCommentList.clear();
        notifyDataSetChanged();
    }

    public void setEnd(boolean isEnd) {
        if(this.isEnd == isEnd) return;
        if(isEnd) {
            notifyDataSetChanged();
        }
        this.isEnd = isEnd;
    }

    public boolean isEnd() {
        return isEnd;
    }
}
