package cn.wsjiu.twoEasy.activity.ui.main;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SearchView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.wsjiu.twoEasy.R;
import cn.wsjiu.twoEasy.adapter.GoodsViewRecyclerAdapter;
import cn.wsjiu.twoEasy.adapter.RankGoodsViewRecyclerAdapter;
import cn.wsjiu.twoEasy.adapter.common.SpaceDecoration;
import cn.wsjiu.twoEasy.entity.Goods;
import cn.wsjiu.twoEasy.entity.User;
import cn.wsjiu.twoEasy.result.Result;
import cn.wsjiu.twoEasy.thread.HttpGetRunnable;
import cn.wsjiu.twoEasy.thread.threadPool.ThreadPoolUtils;

/**
 * 搜索物品
 */
public class SearchGoodsFragment extends Fragment {
    /**
     * 搜索分页的页号和页大小
     */
    private int page = -1;
    private int pageSize = 10;

    /**
     * 排行榜分页的页号和页大小
     */
    private int rankPage = 1;
    private int rankPageSize = 10;

    /**
     * 当前正在搜索的关键字
     */
    private String currentWord;

    private boolean isSearch = false;
    private GoodsViewRecyclerAdapter adapter;
    private RankGoodsViewRecyclerAdapter rankAdapter;

    private View rootView;
    private RecyclerView goodsRecyclerView;
    private RecyclerView rankGoodsRecyclerView;
    private LinearLayout searchRecordView;
    private SearchView searchView;

    public SearchGoodsFragment() {
        // Required empty public constructor
    }

    public static SearchGoodsFragment newInstance(String param1, String param2) {
        SearchGoodsFragment fragment = new SearchGoodsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_search_goods, container, false);
        init();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        goodsRecyclerView.setVisibility(View.INVISIBLE);
        searchRecordView.setVisibility(View.VISIBLE);
    }

    private void init() {
        // 初始化搜索物品的recyclerView
        goodsRecyclerView = rootView.findViewById(R.id.goods_recycler_view);
        adapter = new GoodsViewRecyclerAdapter();
        goodsRecyclerView.addItemDecoration(new SpaceDecoration(SpaceDecoration.ALL));
        adapter = new GoodsViewRecyclerAdapter();
        goodsRecyclerView.setAdapter(adapter);
        StaggeredGridLayoutManager gridLayoutManager =
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        goodsRecyclerView.setLayoutManager(gridLayoutManager);
        goodsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState == RecyclerView.SCROLL_STATE_SETTLING &&
                        !recyclerView.canScrollVertically(1) && !isSearch) {
                    searchGoods();
                }
            }
        });

        searchRecordView = rootView.findViewById(R.id.search_record_view);

        // 初始化 好物排行榜
        rankGoodsRecyclerView = rootView.findViewById(R.id.goods_rank_view);
        rankAdapter = new RankGoodsViewRecyclerAdapter();
        rankGoodsRecyclerView.setAdapter(rankAdapter);
        rankGoodsRecyclerView.addItemDecoration(new SpaceDecoration(SpaceDecoration.VERTICAL));
        LinearLayoutManager rankManager = new LinearLayoutManager(getContext());
        rankManager.setOrientation(RecyclerView.VERTICAL);
        rankGoodsRecyclerView.setLayoutManager(rankManager);
        getRankGoods();
        // TODO 搜索记录布局

        // 设置搜索物品和搜索记录两个页面的可见性
        goodsRecyclerView.setVisibility(View.INVISIBLE);
        searchRecordView.setVisibility(View.VISIBLE);

        // 设置搜索的一些回调事件，例如点击搜索和提交搜索
        searchView = rootView.findViewById(R.id.search_view);
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goodsRecyclerView.setVisibility(View.INVISIBLE);
                searchRecordView.setVisibility(View.VISIBLE);
            }
        });
        searchView.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                goodsRecyclerView.setVisibility(View.INVISIBLE);
                searchRecordView.setVisibility(View.VISIBLE);
                searchView.setIconified(false);
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentWord = searchView.getQuery().toString();
                page = -1;
                adapter.clearData();
                searchGoods();
                goodsRecyclerView.setVisibility(View.VISIBLE);
                searchRecordView.setVisibility(View.INVISIBLE);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    public void searchGoods() {
        isSearch = true;
        String url = getResources().getString(R.string.goods_search_url);
        url += "?word=" + currentWord + "&page=" + (++page) + "&pageSize=" + pageSize;
        Handler handler = new Handler(getActivity().getMainLooper(), this::handlerForSearch);
        HttpGetRunnable runnable = new HttpGetRunnable(url, handler);
        ThreadPoolUtils.asynExecute(runnable);
    }

    private boolean handlerForSearch(Message msg) {
        isSearch =false;
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
                    adapter.setEnd(true);
                    return true;
                }
                for(int i = 0; i < goodsJSONArray.size(); i++) {
                    list.add(goodsJSONArray.getObject(i, Goods.class));
                }
                String userKey = "user";
                JSONObject usersJSONObject = dataJSONObject.getJSONObject(userKey);
                for(String key : usersJSONObject.keySet()) {
                    User user = usersJSONObject.getObject(key, User.class);
                    if(user != null) {
                        userMap.put(Integer.parseInt(key), user);
                    }
                }
            }
            adapter.addItems(list, userMap);
            return true;
        }
        return true;
    }

    public void getRankGoods() {
        String url = getResources().getString(R.string.rank_goods_url);
        url += "?page=" + (rankPage) + "&pageSize=" + rankPageSize;
        Handler handler = new Handler(getActivity().getMainLooper(), this::handlerForRank);
        HttpGetRunnable runnable = new HttpGetRunnable(url, handler);
        ThreadPoolUtils.asynExecute(runnable);
    }

    private boolean handlerForRank(Message msg) {
        isSearch =false;
        Object obj = msg.obj;
        Result<JSONArray> result = null;
        List<Goods> list = new ArrayList<>();
        Map<Integer, User> userMap = new HashMap<>();
        if (obj instanceof Result) {
            result = (Result<JSONArray>) obj;
        }
        if(result != null && result.isSuccess()) {
//            JSONObject dataJSONObject = result.getData();
//            if(dataJSONObject != null) {
//                String goodsKey = "goods";
//                JSONArray goodsJSONArray = dataJSONObject.getJSONArray(goodsKey);
//                rankAdapter.setEnd(true);
//                for(int i = 0; i < goodsJSONArray.size(); i++) {
//                    list.add(goodsJSONArray.getObject(i, Goods.class));
//                }
//                String userKey = "user";
//                JSONObject usersJSONObject = dataJSONObject.getJSONObject(userKey);
//                for(String key : usersJSONObject.keySet()) {
//                    User user = usersJSONObject.getObject(key, User.class);
//                    if(user != null) {
//                        userMap.put(Integer.parseInt(key), user);
//                    }
//                }
//            }
            JSONArray goodsJSONArray = result.getData();
            rankAdapter.setEnd(true);
            for(int i = 0; i < goodsJSONArray.size(); i++) {
                list.add(goodsJSONArray.getObject(i, Goods.class));
            }
            rankAdapter.addItems(list, userMap);
            return true;
        }
        return true;
    }
}