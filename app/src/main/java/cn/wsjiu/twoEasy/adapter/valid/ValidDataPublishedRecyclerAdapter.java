package cn.wsjiu.twoEasy.adapter.valid;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import cn.wsjiu.twoEasy.adapter.common.RecyclerViewHolder;
import cn.wsjiu.twoEasy.component.GoodsPublishedView;
import cn.wsjiu.twoEasy.entity.Goods;
import cn.wsjiu.twoEasy.util.DataSourceUtils;

public class ValidDataPublishedRecyclerAdapter extends BaseValidDataAdapter {
    private List<Goods> goodsList;

    public ValidDataPublishedRecyclerAdapter() {
        goodsList = new ArrayList<>();
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = new GoodsPublishedView(parent.getContext());
        RecyclerViewHolder viewHolder = new RecyclerViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
        View view = holder.itemView;
        if(view instanceof GoodsPublishedView) {
            GoodsPublishedView goodsPublishedView = (GoodsPublishedView) view;
            goodsPublishedView.bindData(goodsList.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return goodsList.size();
    }

    @Override
    public void checkDataValid() {
        Map<Integer, Goods> goodsMap = DataSourceUtils.publishGoodsMap;
        int validCount = 0;
        for(int i = 0; i < goodsList.size(); i++) {
            if(goodsMap.containsKey(goodsList.get(i).getGoodsId())) {
                validCount++;
            }
        }

        if(validCount != goodsList.size() || validCount != goodsMap.size()) {
            goodsList.clear();
            for (Integer goodsId : goodsMap.keySet()
            ) {
                goodsList.add(goodsMap.get(goodsId));
            }
            goodsList.sort(new Comparator<Goods>() {
                @Override
                public int compare(Goods o1, Goods o2) {
                    return -(o1.getTime().compareTo(o2.getTime()));
                }
            });
            notifyDataSetChanged();
        }
    }
}
