package cn.wsjiu.twoEasy.adapter.valid;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import cn.wsjiu.twoEasy.adapter.common.RecyclerViewHolder;

/**
 * 需要进行数据校验的抽象adapter
 */
public abstract class BaseValidDataAdapter extends RecyclerView.Adapter<RecyclerViewHolder> {
    /**
     * 在adapter每一次被显示都需要调用这个方法进行数据合法性的检验
     */
    public abstract void checkDataValid();
}
