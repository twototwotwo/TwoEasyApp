package cn.wsjiu.twoEasy.activity.ui.main;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.wsjiu.twoEasy.R;
import cn.wsjiu.twoEasy.activity.PublishGoodsActivity;
import cn.wsjiu.twoEasy.activity.SearchGoodsActivity;
import cn.wsjiu.twoEasy.adapter.GoodsViewRecyclerAdapter;
import cn.wsjiu.twoEasy.adapter.common.SpaceDecoration;
import cn.wsjiu.twoEasy.entity.Goods;
import cn.wsjiu.twoEasy.entity.User;
import cn.wsjiu.twoEasy.entity.request.RecommendRequest;
import cn.wsjiu.twoEasy.result.Result;
import cn.wsjiu.twoEasy.thread.HttpGetRunnable;
import cn.wsjiu.twoEasy.thread.HttpPostRunnable;
import cn.wsjiu.twoEasy.thread.threadPool.ThreadPoolUtils;
import cn.wsjiu.twoEasy.util.UserUtils;

/**
 * A placeholder fragment containing a simple view.
 */
public class HomeFragment extends Fragment {
    private RecyclerView recommendRecyclerView;
    private RecyclerView latestRecyclerView;

    private View rootView;
    private GoodsViewRecyclerAdapter recommendAdapter;
    private GoodsViewRecyclerAdapter latestAdapter;

    private View latestButton;
    private View recommendButton;


    /**
     * 推荐物品请求的参数对象
     */
    private RecommendRequest recommendRequest;

    /**
     * 推荐最新物品请求的参数对象
     */
    private RecommendRequest latestRequest;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        rootView = root;
        init();
        return root;
    }

    private void init() {
        ImageButton publishButton = rootView.findViewById(R.id.open_publish_goods_activity_button);
        publishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getContext(), PublishGoodsActivity.class);
                startActivity(intent);
            }
        });

        // 初始化最新物品的recycler
        latestRecyclerView = rootView.findViewById(R.id.latest_goods_recycler_view);
        latestRecyclerView.addItemDecoration(new SpaceDecoration(SpaceDecoration.ALL));
        latestAdapter = new GoodsViewRecyclerAdapter();
        latestRecyclerView.setAdapter(latestAdapter);
        StaggeredGridLayoutManager gridLayoutManager =
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        latestRecyclerView.setLayoutManager(gridLayoutManager);
        latestRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState == RecyclerView.SCROLL_STATE_SETTLING &&
                        !recyclerView.canScrollVertically(1) && !latestAdapter.isLoading()) {
                    getLatestGoods();
                }
            }
        });
        if(!latestAdapter.isLoading()) getLatestGoods();

        // 初始化推荐物品的recycler
        recommendRecyclerView = rootView.findViewById(R.id.recommend_goods_recycler_view);
        recommendRecyclerView.addItemDecoration(new SpaceDecoration(SpaceDecoration.ALL));
        recommendAdapter = new GoodsViewRecyclerAdapter();
        recommendRecyclerView.setAdapter(recommendAdapter);
        StaggeredGridLayoutManager recommendLayoutManager =
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recommendRecyclerView.setLayoutManager(recommendLayoutManager);
        recommendRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState == RecyclerView.SCROLL_STATE_SETTLING &&
                        !recyclerView.canScrollVertically(1) && !recommendAdapter.isLoading()) {
                    recommendGoods();
                }
            }
        });
        if(!recommendAdapter.isLoading()) recommendGoods();

        latestButton = rootView.findViewById(R.id.latest_goods_button);
        latestButton.setOnClickListener(this::latestOrRecommend);
        recommendButton = rootView.findViewById(R.id.recommend_goods_button);
        recommendButton.setOnClickListener(this::latestOrRecommend);
    }

    /**
     * 异步分页获取最新物品
     */
    private void getLatestGoods() {
        if(latestAdapter.isEnd() || latestAdapter.isLoading()) return;
        latestAdapter.setLoading(true);
        if(latestRequest == null) {
            latestRequest = new RecommendRequest();
            latestRequest.setUserId(UserUtils.getUser().getUserId());
        }
        latestRequest.setPage(latestRequest.getPage() + 1);
        String url = getResources().getString(R.string.goods_recommend_new_url);
        Handler handler = new Handler(getActivity().getMainLooper(), this::handle);
        HttpPostRunnable<RecommendRequest, String> httpPostRunnable =
                new HttpPostRunnable<>(url, handler, latestRequest);
        ThreadPoolUtils.asynExecute(httpPostRunnable);
    }

    /**
     * 异步回调处理
     * @param msg 回调信息
     * @return
     */
    private boolean handle(Message msg) {
        latestAdapter.setLoading(false);
        Object obj = msg.obj;
        Result<JSONObject> result = null;
        List<Goods> list = new ArrayList<>();
        Map<Integer, User> userMap = new HashMap<>();
        if (obj instanceof Result) {
            result = (Result<JSONObject>) obj;
        }
        if(result != null && result.isSuccess()) {
            JSONObject dataJSONObject = result.getData();
            if(dataJSONObject != null) {
                String goodsKey = "goods";
                JSONArray goodsJSONArray = dataJSONObject.getJSONArray(goodsKey);
                if (goodsJSONArray == null || goodsJSONArray.size() == 0) {
                    //TODO 取完数据
                    latestAdapter.setEnd(true);
                    return false;
                }
                for(int i = 0; i < goodsJSONArray.size(); i++) {
                    list.add(goodsJSONArray.getObject(i, Goods.class));
                }
                if(list.size() < recommendRequest.getPageSize()) {
                    latestAdapter.setEnd(true);
                }
                String userKey = "user";
                JSONObject usersJSOBject = dataJSONObject.getJSONObject(userKey);
                for(String key : usersJSOBject.keySet()) {
                    User user = usersJSOBject.getObject(key, User.class);
                    if(user != null) {
                        userMap.put(Integer.parseInt(key), user);
                    }
                }
            }
            latestAdapter.addItems(list, userMap);
            return true;
        }
        return false;
    }

    /**
     * 异步分页获取推荐物品
     */
    private void recommendGoods() {
        if(recommendAdapter.isEnd() || recommendAdapter.isLoading()) return;
        recommendAdapter.setLoading(true);
        if(recommendRequest == null) {
            recommendRequest = new RecommendRequest();
            recommendRequest.setUserId(UserUtils.getUser().getUserId());
            recommendRequest.setUserProfile(UserUtils.getUserProfile());
        }
        recommendRequest.setPage(recommendRequest.getPage() + 1);
        String url = getResources().getString(R.string.goods_recommend_url);
        Handler handler = new Handler(getActivity().getMainLooper(), this::handleForRecommend);
        HttpPostRunnable<RecommendRequest, String> httpPostRunnable =
                new HttpPostRunnable<>(url, handler, recommendRequest);
        ThreadPoolUtils.asynExecute(httpPostRunnable);
    }

    /**
     * 异步回调处理
     * @param msg 回调信息
     * @return
     */
    private boolean handleForRecommend(Message msg) {
        recommendAdapter.setLoading(false);
        Object obj = msg.obj;
        Result<JSONObject> result = null;
        List<Goods> list = new ArrayList<>();
        Map<Integer, User> userMap = new HashMap<>();
        if (obj instanceof Result) {
            result = (Result<JSONObject>) obj;
        }
        if(result != null && result.isSuccess()) {
            JSONObject dataJSONObject = result.getData();
            if(dataJSONObject != null) {
                String goodsKey = "goods";
                JSONArray goodsJSONArray = dataJSONObject.getJSONArray(goodsKey);
                if (goodsJSONArray == null || goodsJSONArray.size() == 0) {
                    //TODO 取完数据
                    recommendAdapter.setEnd(true);
                    return false;
                }
                for(int i = 0; i < goodsJSONArray.size(); i++) {
                    list.add(goodsJSONArray.getObject(i, Goods.class));
                }
                String userKey = "user";
                JSONObject usersJSOBject = dataJSONObject.getJSONObject(userKey);
                for(String key : usersJSOBject.keySet()) {
                    User user = usersJSOBject.getObject(key, User.class);
                    if(user != null) {
                        userMap.put(Integer.parseInt(key), user);
                    }
                }
            }
            recommendAdapter.addItems(list, userMap);
            return true;
        }
        return false;
    }

    private void latestOrRecommend(View view) {
        if(view == recommendButton) {
            recommendButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.water_blue, null)));
            latestButton.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
            recommendRecyclerView.setVisibility(View.VISIBLE);
            latestRecyclerView.setVisibility(View.INVISIBLE);
        }else {
            latestButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.water_blue, null)));
            recommendButton.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
            recommendRecyclerView.setVisibility(View.INVISIBLE);
            latestRecyclerView.setVisibility(View.VISIBLE);
        }
    }
}