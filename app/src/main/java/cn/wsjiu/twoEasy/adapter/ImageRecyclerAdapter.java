package cn.wsjiu.twoEasy.adapter;

import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import cn.wsjiu.twoEasy.adapter.common.RecyclerViewHolder;
import cn.wsjiu.twoEasy.component.ImageSelectedView;

/**
 * 图片类型recyclerView 的自定义适配器
 */
public class ImageRecyclerAdapter extends Adapter {
    /**
     * 固定每个item的高度为300
     */
    private final static int ITEM_HEIGHT = 300;

    /**
     * 图片的圆角
     */
    private final float CORNER_RADIUS = 20;

    /**
     * 每个item共用相同的layoutParams
     */
    private ViewGroup.LayoutParams layoutParams;

    /**
     * 图片数据
     * 可能是url —— string,编辑时会传入url
     * 也可能是drawable —— RoundedBitmapDrawable
     */
    private List<Object> drawableList;

    public ImageRecyclerAdapter() {
        init();
    }

    private void init() {
        drawableList = new ArrayList<>(4);
        this.layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ITEM_HEIGHT);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageSelectedView imageSelectedView = new ImageSelectedView(parent.getContext());
        imageSelectedView.setParentAdapter(this);
        return new RecyclerViewHolder(imageSelectedView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ImageSelectedView imageView = (ImageSelectedView) holder.itemView;
        imageView.bindData(drawableList.get(position), position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return drawableList.size();
    }

    public void addItem(RoundedBitmapDrawable drawable) {
        drawable.setCornerRadius(CORNER_RADIUS);
        drawableList.add(drawable);
        notifyItemInserted(drawableList.size());
    }

    public void addItem(String drawableUrl) {
        drawableList.add(drawableUrl);
        notifyItemInserted(drawableList.size());
    }

    public void addItems(String urlMapStr) {
        JSONObject urlMap = JSONObject.parseObject(urlMapStr);
        List<String> keyList = new ArrayList<>(urlMap.keySet());
        keyList.sort(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1 == null ? -1 : o1.compareTo(o2);
            }
        });
        List<String> urlList = new ArrayList<>(keyList.size());
        for (String key : keyList
             ) {
            urlList.add(urlMap.getString(key));
        }
        drawableList.addAll(urlList);
        notifyDataSetChanged();
    }

    public void deleteItem(int position) {
        drawableList.remove(position);
        notifyItemRemoved(position);
    }

    /**
     * 提供给外部获取图片数据用于上传服务器
     * @return 图片数据的list对象
     */
    public List<Object> getDrawableList() {
        return drawableList;
    }
}

