package cn.wsjiu.twoEasy.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.wsjiu.twoEasy.R;
import cn.wsjiu.twoEasy.adapter.CommentRecyclerAdapter;
import cn.wsjiu.twoEasy.adapter.GoodsViewRecyclerAdapter;
import cn.wsjiu.twoEasy.adapter.SimpleGoodsViewRecyclerAdapter;
import cn.wsjiu.twoEasy.adapter.common.SpaceDecoration;
import cn.wsjiu.twoEasy.component.AdaptionImageView;
import cn.wsjiu.twoEasy.entity.Goods;
import cn.wsjiu.twoEasy.entity.OrderComment;
import cn.wsjiu.twoEasy.entity.User;
import cn.wsjiu.twoEasy.entity.request.GoodsRequest;
import cn.wsjiu.twoEasy.result.Result;
import cn.wsjiu.twoEasy.thread.HttpGetRunnable;
import cn.wsjiu.twoEasy.thread.HttpPostRunnable;
import cn.wsjiu.twoEasy.thread.threadPool.ThreadPoolUtils;
import cn.wsjiu.twoEasy.util.DataSourceUtils;
import cn.wsjiu.twoEasy.util.UserUtils;

public class UserInfoActivity extends AppCompatActivity {
    /**
     * 数据实体
     */
    private User user;
    private GoodsRequest request;
    private boolean isRunForGoods = false;
    private boolean isRunForComment = false;
    private int commentPage = 0;
    private int commentPageSize = 10;

    /**
     *物品视图的适配器和列表
     */
    private SimpleGoodsViewRecyclerAdapter goodsAdapter;
    private RecyclerView goodsRecyclerView;

    /**
     * 评论视图的适配器和列表
     */
    private CommentRecyclerAdapter commentAdapter;
    private RecyclerView commentRecyclerView;

    /**
     * 查看发布物品或评论的tab选择按钮
     */
    private TextView publishedButton;
    private TextView commentedButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        Intent intent = getIntent();
        user = (User) intent.getSerializableExtra("user");
        init();
    }

    private void init() {
        AdaptionImageView head = findViewById(R.id.head_view);
        head.setImageFromUrl(user.getHeadUrl());
        TextView userNameView = findViewById(R.id.user_name_view);
        userNameView.setText(user.getUserNickName());
        TextView declarationView = findViewById(R.id.declaration_view);
        declarationView.setText(user.getDeclaration());
        TextView schoolView = findViewById(R.id.school_name_view);
        schoolView.setText("◉" + user.getSchoolName());
        Button followButton = findViewById(R.id.follow_button);
        if(DataSourceUtils.checkIsFollow(user.getUserId())) {
            followButton.setText("已关注");
        }else {
            followButton.setText("+关注");
        }
        followButton.setOnClickListener(this::followOrCancelFollow);

        goodsRecyclerView = findViewById(R.id.goods_recycler_view);
        goodsAdapter = new SimpleGoodsViewRecyclerAdapter(user);
        goodsRecyclerView.setAdapter(goodsAdapter);
        StaggeredGridLayoutManager gridLayoutManager =
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        goodsRecyclerView.setLayoutManager(gridLayoutManager);
        goodsRecyclerView.addItemDecoration(new SpaceDecoration(SpaceDecoration.ALL));
        goodsRecyclerView.setAdapter(goodsAdapter);
        goodsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState == RecyclerView.SCROLL_STATE_SETTLING &&
                        !recyclerView.canScrollVertically(1) && !isRunForGoods) {
                    getGoods();
                }
            }
        });

        commentRecyclerView = findViewById(R.id.comment_recycler_view);
        commentAdapter = new CommentRecyclerAdapter();
        commentRecyclerView.setAdapter(commentAdapter);
        LinearLayoutManager commentManager = new LinearLayoutManager(this);
        commentRecyclerView.setLayoutManager(commentManager);
        commentRecyclerView.addItemDecoration(new SpaceDecoration(SpaceDecoration.VERTICAL));
        commentRecyclerView.setAdapter(commentAdapter);
        commentRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState == RecyclerView.SCROLL_STATE_SETTLING &&
                        !recyclerView.canScrollVertically(1) && !isRunForComment) {
                    getComment();
                }
            }
        });

        publishedButton = findViewById(R.id.published_button);
        commentedButton = findViewById(R.id.commented_button);
        View.OnClickListener tabClickListener = v -> {
            if(v == publishedButton) {
                publishedButton.setTextColor(Color.WHITE);
                commentedButton.setTextColor(Color.GRAY);
                goodsRecyclerView.setVisibility(View.VISIBLE);
                commentRecyclerView.setVisibility(View.INVISIBLE);
            }else {
                publishedButton.setTextColor(Color.GRAY);
                commentedButton.setTextColor(Color.WHITE);
                goodsRecyclerView.setVisibility(View.INVISIBLE);
                commentRecyclerView.setVisibility(View.VISIBLE);
            }
        };
        publishedButton.setOnClickListener(tabClickListener);
        commentedButton.setOnClickListener(tabClickListener);
        tabClickListener.onClick(publishedButton);

        request = new GoodsRequest();
        request.setUserId(user.getUserId());
        request.setPage(0);
        request.setPageSize(10);
        getGoods();
        getComment();
    }

    private void getGoods() {
        if(isRunForGoods || goodsAdapter.isEnd()) return;
        String url = getString(R.string.goods_get_url);
        url += "?userId=" + request.getUserId() + "&page=" + request.getPage() + "&pageSize=" + request.getPageSize();
        Handler handler = new Handler(getMainLooper(), this::handleForGoods);
        HttpGetRunnable runnable = new HttpGetRunnable(url, handler);
        ThreadPoolUtils.synExecute(runnable);
        isRunForGoods = true;
    }

    private void getComment() {
        if(isRunForComment || commentAdapter.isEnd()) return;
        String url = getString(R.string.order_comment_get_url);
        url += "?userId=" + request.getUserId() + "&page=" + commentPage
                + "&pageSize=" + commentPageSize;
        Handler handler = new Handler(getMainLooper(), this::handlerForComment);
        HttpGetRunnable runnable = new HttpGetRunnable(url, handler);
        ThreadPoolUtils.synExecute(runnable);
        isRunForComment = true;
    }

    public void followOrCancelFollow(View view) {
        String url;
        if(DataSourceUtils.checkIsFollow(user.getUserId())) {
            url = getResources().getString(R.string.cancel_follow_user_url);
        }else {
            url = getResources().getString(R.string.follow_user_url);
        }
        Handler handler = new Handler(getMainLooper(), this::handleForFollowAction);
        int userSelfId = UserUtils.getUser().getUserId();
        url += "?fansId=" + userSelfId + "&followedId=" + user.getUserId();
        HttpGetRunnable runnable =new HttpGetRunnable(url, handler);
        ThreadPoolUtils.asynExecute(runnable);
    }

    private boolean handleForGoods(Message message) {
        Object obj = message.obj;
        Result<JSONArray> result = null;
        List<Goods> list = new ArrayList<>();
        if (obj instanceof Result) {
            result = (Result<JSONArray>) obj;
        }
        if(result != null && result.isSuccess()) {
            JSONArray goodsJSONArray = result.getData();
            if (goodsJSONArray == null || goodsJSONArray.size() == 0) {
                goodsAdapter.setEnd(true);
                return false;
            }
            for(int i = 0; i < goodsJSONArray.size(); i++) {
                list.add(goodsJSONArray.getObject(i, Goods.class));
            }
            if(list.size() < request.getPageSize()) {
                goodsAdapter.setEnd(true);
            }
            goodsAdapter.addItems(list);
            isRunForGoods = false;
            request.setPage(request.getPage() + 1);
            return true;
        }else {
            Toast.makeText(this, "获取物品失败", Toast.LENGTH_SHORT).show();
        }
        isRunForGoods = false;
        return false;
    }

    private boolean handleForFollowAction(Message message) {
        Object object = message.obj;
        if(object instanceof Result) {
            Result result = (Result) object;
            if(result.isSuccess()) {
                Button followButton = findViewById(R.id.follow_button);
                if(DataSourceUtils.checkIsFollow(user.getUserId())) {
                    DataSourceUtils.cancelFollow(user.getUserId());
                    followButton.setText("+关注");
                }else {
                    DataSourceUtils.addFollow(user.getUserId());
                    followButton.setText("已关注");
                }
            }
        }
        return true;
    }

    private boolean handlerForComment(Message message) {
        Object obj = message.obj;
        Result<JSONArray> result = null;
        List<OrderComment> list = new ArrayList<>();
        if (obj instanceof Result) {
            result = (Result<JSONArray>) obj;
        }
        if(result != null && result.isSuccess()) {
            JSONArray commentJSONArray = result.getData();
            if (commentJSONArray == null || commentJSONArray.size() == 0) {
                commentAdapter.setEnd(true);
                return false;
            }
            for(int i = 0; i < commentJSONArray.size(); i++) {
                list.add(commentJSONArray.getObject(i, OrderComment.class));
            }
            if(list.size() < request.getPageSize()) {
                commentAdapter.setEnd(true);
            }
            commentAdapter.addItems(list);
            isRunForComment = false;
            commentPage += 1;
            return true;
        }else {
            Toast.makeText(this, "获取评论失败", Toast.LENGTH_SHORT).show();
        }
        isRunForComment = false;
        return false;
    }

    public void back(View view) {
        onBackPressed();
        finish();
    }
}