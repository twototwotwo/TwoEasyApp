package cn.wsjiu.twoEasy.adapter;


import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.wsjiu.twoEasy.R;
import cn.wsjiu.twoEasy.adapter.common.RecyclerViewHolder;
import cn.wsjiu.twoEasy.component.GoodsRankView;
import cn.wsjiu.twoEasy.component.GoodsView;
import cn.wsjiu.twoEasy.component.LoadingBeanner;
import cn.wsjiu.twoEasy.entity.Goods;
import cn.wsjiu.twoEasy.entity.User;

/**
 * 图文{@link GoodsView}
 * 的{@link RecyclerView} 的自定义适配器
 */
public class RankGoodsViewRecyclerAdapter extends Adapter<RecyclerViewHolder> {
    private static final int TYPE_LOADING = 1;
    private static final int TYPE_COMMON= 1 << 1;
    private static final int TYPE_EMPTY = 1 << 2;

    private View defaultEmptyView;
    private View defaultLoadingView;
    private boolean isEnd = false;

    private List<Goods> goodsList;
    private Map<Integer, User> userMap;

    public RankGoodsViewRecyclerAdapter() {
        goodsList = new ArrayList<>(8);
        //goodsList.add(null);
        userMap = new HashMap<>(8);
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;
        if(viewType == TYPE_COMMON) {
            itemView = new GoodsRankView(parent.getContext());
        }else if(viewType == TYPE_EMPTY){
            if(defaultEmptyView == null) {
                TextView emptyTextView = new TextView(parent.getContext());
                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                emptyTextView.setLayoutParams(layoutParams);
                emptyTextView.setGravity(Gravity.CENTER);
                emptyTextView.setText("好物排行榜暂无符合条件的物品");
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
                Goods goods = goodsList.get(position);
                GoodsRankView goodsView = (GoodsRankView) holder.itemView;
                User user = userMap.get(goods.getUserId());
                goodsView.bindData(goods, user, position);
                break;
            case TYPE_LOADING:
                break;
        }
    }

    @Override
    public int getItemCount() {
        int size = goodsList != null ? goodsList.size() : 0;
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
        int goodsCount = goodsList == null ? 0 : goodsList.size();
        if(goodsCount == 0 && size == 1 && position == 0) {
            return TYPE_EMPTY;
        }else if(position == goodsCount){
            return TYPE_LOADING;
        }else {
            return TYPE_COMMON;
        }
    }

    public void addItem(Goods goods) {
        goodsList.add(goods);
        notifyDataSetChanged();
    }

    public void addItems(List<Goods> goodsList, Map<Integer, User> userMap) {
        int size = this.goodsList.size();
        if(size > 0 && this.goodsList.get(size - 1) == null) {
            this.goodsList.remove(size - 1);
        }
        this.goodsList.addAll(goodsList);
        this.userMap.putAll(userMap);
        notifyDataSetChanged();
    }

    public void clearData() {
        goodsList.clear();
        userMap.clear();
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
