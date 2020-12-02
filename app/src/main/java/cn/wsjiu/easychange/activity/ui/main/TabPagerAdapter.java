package cn.wsjiu.easychange.activity.ui.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.wsjiu.easychange.R;
import cn.wsjiu.easychange.activity.MainActivity;
import cn.wsjiu.easychange.entity.User;

import static android.content.Context.MODE_PRIVATE;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class TabPagerAdapter extends FragmentPagerAdapter {
    private final Context mContext;
    private final String USER = "user";

    private List<Fragment> tabItemList;
    public TabPagerAdapter(Context context, FragmentManager fm) {
        super(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        mContext = context;
        // 初始化每一个fragment
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(USER, MODE_PRIVATE);
        String userStr = sharedPreferences.getString(USER, null);
        User user = JSONObject.parseObject(userStr, User.class);
        tabItemList = new ArrayList<>(4);
        tabItemList.add(new HomeFragment());
        tabItemList.add(new QuestionFragment());
        tabItemList.add(new PublishFragment(user));
        tabItemList.add(new PersonFragment(user));
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        return tabItemList.get(position);
    }





    @Override
    public int getCount() {
        // Show 2 total pages.
        return tabItemList == null ? 0 : tabItemList.size();
    }
}