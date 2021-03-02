package cn.wsjiu.twoEasy.adapter.valid;

import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import cn.wsjiu.twoEasy.adapter.common.RecyclerViewHolder;
import cn.wsjiu.twoEasy.component.GoodsSubcribedView;
import cn.wsjiu.twoEasy.entity.SubscribeRecord;
import cn.wsjiu.twoEasy.util.DataSourceUtils;

public class ValidDataSubscribedRecyclerAdapter extends BaseValidDataAdapter {
    private List<SubscribeRecord> subscribeRecordList;
    private Comparator<SubscribeRecord> comparator;

    public ValidDataSubscribedRecyclerAdapter() {
        subscribeRecordList = new ArrayList<>();
        comparator = new Comparator<SubscribeRecord>() {
            @Override
            public int compare(SubscribeRecord o1, SubscribeRecord o2) {
                return -(o1.getTime().compareTo(o2.getTime()));
            }
        };
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = new GoodsSubcribedView(parent.getContext());
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
        GoodsSubcribedView view = (GoodsSubcribedView) holder.itemView;
        SubscribeRecord record = subscribeRecordList.get(position);
        view.bindData(record);
    }

    @Override
    public int getItemCount() {
        return subscribeRecordList == null ? 0 : subscribeRecordList.size();
    }

    @Override
    public void checkDataValid() {
        Map<Integer, SubscribeRecord> subscribeRecordMap = DataSourceUtils.subscribeRecordMap;
        int validCount = 0;
        for(int i = 0; i < subscribeRecordList.size(); i++) {
            boolean isContained = subscribeRecordMap.containsKey(subscribeRecordList.get(i).getGoodsId());
            if(isContained) {
                validCount++;
            }
        }
        if(validCount != subscribeRecordMap.size() || validCount != subscribeRecordList.size()) {
            subscribeRecordList = new ArrayList<>(DataSourceUtils.subscribeRecordMap.values());
            subscribeRecordList.sort(comparator);
            notifyDataSetChanged();
        }
    }
}
