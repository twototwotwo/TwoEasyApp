package cn.wsjiu.twoEasy.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import cn.wsjiu.twoEasy.R;
import cn.wsjiu.twoEasy.adapter.FollowedUserRecyclerAdapter;
import cn.wsjiu.twoEasy.adapter.common.SpaceDecoration;
import cn.wsjiu.twoEasy.entity.User;
import cn.wsjiu.twoEasy.result.Result;
import cn.wsjiu.twoEasy.thread.HttpPostRunnable;
import cn.wsjiu.twoEasy.thread.threadPool.ThreadPoolUtils;
import cn.wsjiu.twoEasy.util.DataSourceUtils;

public class FollowedUserActivity extends AppCompatActivity {
    private FollowedUserRecyclerAdapter adapter;
    private List<Integer> userIdList;
    private int currentIndex = 0;
    private int pageSize = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_user);
        init();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void init() {
        RecyclerView recyclerView = findViewById(R.id.user_recycler_view);
        adapter = new FollowedUserRecyclerAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new SpaceDecoration(SpaceDecoration.ALL));
        LinearLayoutManager manager = new LinearLayoutManager(getBaseContext());
        manager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(manager);
        userIdList = new ArrayList<>(DataSourceUtils.followedIdSet);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState == RecyclerView.SCROLL_STATE_SETTLING &&
                        !recyclerView.canScrollVertically(1) ) {
                    // TODO
                    loadMore();
                }
            }
        });
        loadMore();
    }

    private void loadMore() {
        Set<Integer> userIdSet = new HashSet<>();
        for(int i = 0; i < pageSize && currentIndex < userIdList.size(); i++) {
            int userId = userIdList.get(currentIndex++);
            userIdSet.add(userId);
        }
        if(userIdSet.size() > 0) {
            String url = getString(R.string.user_base_info_url);
            Handler handler = new Handler(getMainLooper(), this::handle);
            HttpPostRunnable<Set<Integer>, JSONObject> runnable = new HttpPostRunnable<>(url, handler, userIdSet);
            ThreadPoolUtils.asynExecute(runnable);
        }else if(currentIndex >= userIdList.size()){
            adapter.setEnd(true);
        }
    }

    private boolean handle(Message msg) {
        Object obj = msg.obj;
        if(obj instanceof Result) {
            Result result = (Result) obj;
            if(result.isSuccess()) {
                JSONObject dataJSONObject = (JSONObject) result.getData();
                List<User> userList = new ArrayList<>(dataJSONObject.size());
                for (String key : dataJSONObject.keySet()
                     ) {
                    User user = dataJSONObject.getObject(key, User.class);
                    userList.add(user);
                }
                if(userList.size() > 0) {
                    adapter.addAll(userList);
                }
                if(currentIndex >= userIdList.size()) {
                    adapter.setEnd(true);
                }
            }
        }
        return true;
    }

    public void back(View view) {
        onBackPressed();
    }
}