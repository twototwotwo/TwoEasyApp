package cn.wsjiu.twoEasy.activity;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.wsjiu.twoEasy.R;
import cn.wsjiu.twoEasy.adapter.GoodsViewRecyclerAdapter;
import cn.wsjiu.twoEasy.adapter.common.SpaceDecoration;
import cn.wsjiu.twoEasy.entity.Goods;
import cn.wsjiu.twoEasy.entity.User;
import cn.wsjiu.twoEasy.result.Result;
import cn.wsjiu.twoEasy.thread.HttpGetRunnable;
import cn.wsjiu.twoEasy.thread.threadPool.ThreadPoolUtils;

public class SearchGoodsActivity extends Activity {
    /**
     * 分页的页号和页大小
     */
    private int page = -1;
    private int pageSize = 10;
    /**
     * 当前正在搜索的关键字
     */
    private String currentWord;

    private boolean isSearch = false;
    private GoodsViewRecyclerAdapter adapter;

    private RecyclerView goodsRecyclerView;
    private LinearLayout searchRecordView;
    private SearchView searchView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_goods);
        init();
    }

    @Override
    public void onBackPressed() {
        if(goodsRecyclerView.getVisibility() == View.VISIBLE) {
            goodsRecyclerView.setVisibility(View.INVISIBLE);
            searchRecordView.setVisibility(View.VISIBLE);
            searchView.setQuery(currentWord, false);
            currentWord = "";
        }else if(currentWord != null && currentWord.length() > 0){
            goodsRecyclerView.setVisibility(View.VISIBLE);
            searchRecordView.setVisibility(View.INVISIBLE);
        }else {
            super.onBackPressed();
        }
    }

    private void init() {
        goodsRecyclerView = findViewById(R.id.goods_recycler_view);
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

        searchRecordView = findViewById(R.id.search_record_view);
        // TODO 搜索记录布局

        goodsRecyclerView.setVisibility(View.INVISIBLE);
        searchRecordView.setVisibility(View.VISIBLE);

        searchView = findViewById(R.id.search_view);
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
        Handler handler = new Handler(getMainLooper(), this::handler);
        HttpGetRunnable runnable = new HttpGetRunnable(url, handler);
        ThreadPoolUtils.asynExecute(runnable);
    }

    private boolean handler(Message msg) {
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
}
