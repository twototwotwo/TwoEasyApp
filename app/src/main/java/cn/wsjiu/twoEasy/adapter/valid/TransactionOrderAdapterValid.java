package cn.wsjiu.twoEasy.adapter.valid;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import cn.wsjiu.twoEasy.adapter.common.RecyclerViewHolder;
import cn.wsjiu.twoEasy.component.TransactionOrderView;
import cn.wsjiu.twoEasy.entity.Order;
import cn.wsjiu.twoEasy.entity.OrderState;
import cn.wsjiu.twoEasy.util.DataSourceUtils;
import cn.wsjiu.twoEasy.util.UserUtils;

public class TransactionOrderAdapterValid extends BaseValidDataAdapter {
    boolean isSoldOrder = true;
    List<Order> orderList ;
    Comparator<Order> comparator;

    public TransactionOrderAdapterValid() {
        orderList = new ArrayList<>();
        comparator = new Comparator<Order>() {
            @Override
            public int compare(Order o1, Order o2) {
                return -(o1.getTime().compareTo(o2.getTime()));
            }
        };
    }

    public TransactionOrderAdapterValid(boolean isSoldOrder) {
        this();
        this.isSoldOrder = isSoldOrder;
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TransactionOrderView view = new TransactionOrderView(parent.getContext());
        view.setAdapter(this);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
        View view = holder.itemView;
        if(view instanceof TransactionOrderView) {
            TransactionOrderView transactionOrderView = (TransactionOrderView) view;
            ((TransactionOrderView) view).bindData(orderList.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    @Override
    public void checkDataValid() {
        Map<Integer, Order> orderMap = DataSourceUtils.orderMap;
        int validCount = 0;
        for (Order order : orderList
             ) {
            validCount += orderMap.containsKey(order.getGoodsId()) ? 1 : 0;
        }
        int orderCount = 0;
        int userSelfId = UserUtils.getUser().getUserId();
        for(Order order : orderMap.values()) {
            if(isSoldOrder && order.getSellerId() == userSelfId) {
                orderCount++;
            }else if(!isSoldOrder && order.getBuyerId() == userSelfId) {
                orderCount++;
            }
        }
        if(validCount == orderCount && validCount == orderList.size()) {
            return;
        }
        orderList.clear();
        for(Order order : orderMap.values()) {
            if(isSoldOrder && order.getSellerId() == userSelfId) {
                orderList.add(order);
            }else if(!isSoldOrder && order.getBuyerId() == userSelfId) {
                orderList.add(order);
            }
        }
        orderList.sort(comparator);
        notifyDataSetChanged();
    }

    public void removeItem(Order order) {
        int position = orderList.indexOf(order);
        orderList.remove(position);
        notifyItemRemoved(position);
    }
}
