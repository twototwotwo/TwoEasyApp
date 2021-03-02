package cn.wsjiu.twoEasy.activity.ui.main;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import cn.wsjiu.twoEasy.R;
import cn.wsjiu.twoEasy.activity.FollowedUserActivity;
import cn.wsjiu.twoEasy.activity.UserInfoEditActivity;
import cn.wsjiu.twoEasy.adapter.valid.BaseValidDataAdapter;
import cn.wsjiu.twoEasy.adapter.valid.ValidDataPublishedRecyclerAdapter;
import cn.wsjiu.twoEasy.adapter.valid.ValidDataSubscribedRecyclerAdapter;
import cn.wsjiu.twoEasy.adapter.valid.TransactionOrderAdapterValid;
import cn.wsjiu.twoEasy.adapter.common.SpaceDecoration;
import cn.wsjiu.twoEasy.component.AdaptionImageView;
import cn.wsjiu.twoEasy.entity.User;
import cn.wsjiu.twoEasy.util.DataSourceUtils;
import cn.wsjiu.twoEasy.util.UserUtils;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class  PersonFragment extends Fragment {
    private User user;
    private View rootView;
    private Map<Integer, BaseValidDataAdapter> adapterMap;
    private Map<Integer, TextView> tabTextViewMap;

    private AdaptionImageView head;
    private TextView nameView;
    private View currentTabView;
    private TextView followCountView;
    private TextView declarationView;
    
    RecyclerView recyclerView;

    public PersonFragment() {
        this.user = UserUtils.getUser();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        this.rootView = inflater.inflate(R.layout.fragment_person, container, false);
        init();
        return this.rootView;
    }

    public void init() {
        head =rootView.findViewById(R.id.head_image_view);
        head.setImageFromUrl(user.getHeadUrl());
        nameView = rootView.findViewById(R.id.user_name_text_view);
        nameView.setText(user.getUserNickName());

        recyclerView = rootView.findViewById(R.id.goods_recycler_view);
        ValidDataSubscribedRecyclerAdapter subscribedRecyclerAdapter = new ValidDataSubscribedRecyclerAdapter();
        ValidDataPublishedRecyclerAdapter publishedRecyclerAdapter = new ValidDataPublishedRecyclerAdapter();
        TransactionOrderAdapterValid soldOrderAdapter = new TransactionOrderAdapterValid();
        TransactionOrderAdapterValid buyInOrderAdapter = new TransactionOrderAdapterValid(false);

        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        manager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(manager);
        recyclerView.addItemDecoration(new SpaceDecoration(SpaceDecoration.VERTICAL));

        tabTextViewMap = new HashMap<>(4);
        adapterMap = new HashMap<>(4);
        View subscribedButton = rootView.findViewById(R.id.subscribed_button);
        subscribedButton.setOnClickListener(this::show);
        TextView subscribedTextView = rootView.findViewById(R.id.subscribed_text_view);
        tabTextViewMap.put(subscribedButton.getId(), subscribedTextView);
        adapterMap.put(subscribedButton.getId(), subscribedRecyclerAdapter);

        View publishedButton = rootView.findViewById(R.id.published_button);
        publishedButton.setOnClickListener(this::show);
        TextView publishedTextView = rootView.findViewById(R.id.published_text_view);
        tabTextViewMap.put(publishedButton.getId(), publishedTextView);
        adapterMap.put(publishedButton.getId(), publishedRecyclerAdapter);

        View soldButton = rootView.findViewById(R.id.sold_button);
        soldButton.setOnClickListener(this::show);
        TextView soldTextView = rootView.findViewById(R.id.sold_text_view);
        tabTextViewMap.put(soldButton.getId(), soldTextView);
        adapterMap.put(soldButton.getId(), soldOrderAdapter);

        View buyInButton = rootView.findViewById(R.id.buy_in_button);
        buyInButton.setOnClickListener(this::show);
        TextView buyInTextView = rootView.findViewById(R.id.buy_in_text_view);
        tabTextViewMap.put(buyInButton.getId(), buyInTextView);
        adapterMap.put(buyInButton.getId(), buyInOrderAdapter);

        show(publishedButton);

        View followButton = rootView.findViewById(R.id.follow_button);
        followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(PersonFragment.this.getContext(), FollowedUserActivity.class);
                startActivity(intent);
            }
        });

        followCountView = rootView.findViewById(R.id.follow_count_view);
        declarationView = rootView.findViewById(R.id.declaration_view);

        Button editButton = rootView.findViewById(R.id.edit_button);
        editButton.setOnClickListener(this::edit);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(currentTabView != null) {
            BaseValidDataAdapter adapter = adapterMap.get(currentTabView.getId());
            if(adapter != null) adapter.checkDataValid();
        }
        int count = DataSourceUtils.followedIdSet.size();
        followCountView.setText(String.valueOf(count));
        declarationView.setText(user.getDeclaration());
        head.setImageFromUrl(user.getHeadUrl());
        nameView.setText(user.getUserNickName());
    }

    /**
     * 展示物品
     */
    public void show(View view) {
        if(currentTabView != null && view.getId() == currentTabView.getId()) return;
        currentTabView = view;
        for(TextView textView : tabTextViewMap.values()) {
            textView.setTextColor(getResources().getColor(R.color.gray_white, null));
            textView.setBackgroundColor(Color.TRANSPARENT);
        }
        TextView clickTextView = tabTextViewMap.get(view.getId());
        if(clickTextView != null) {
            clickTextView.setTextColor(Color.BLACK);
            clickTextView.setBackgroundResource(R.drawable.border_radius_10dp);
        }
        BaseValidDataAdapter adapter = adapterMap.get(view.getId());
        if(adapter != null) {
            adapter.checkDataValid();
        }
        recyclerView.setAdapter(adapter);
    }

    private void edit(View view) {
        Intent intent = new Intent();
        intent.setClass(getContext(), UserInfoEditActivity.class);
        startActivity(intent);
    }
}