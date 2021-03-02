package cn.wsjiu.twoEasy.activity.ui.main;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class TabPagerAdapter extends FragmentPagerAdapter {

    private List<Fragment> tabItemList;
    private HomeFragment homeFragment;
    private SearchGoodsFragment searchGoodsFragment;
    private ChatFragment chatFragment;
    private PersonFragment personFragment;

    public TabPagerAdapter(Context context, FragmentManager fm) {
        super(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

        tabItemList = new ArrayList<>(4);
        homeFragment = new HomeFragment();
        searchGoodsFragment = new SearchGoodsFragment();
        chatFragment = new ChatFragment();
        personFragment = new PersonFragment();

        tabItemList.add(homeFragment);
        tabItemList.add(searchGoodsFragment);
        tabItemList.add(chatFragment);
        tabItemList.add(personFragment);
    }

    @Override
    public Fragment getItem(int position) {
        return tabItemList.get(position);
    }

    @Override
    public int getCount() {
        return tabItemList == null ? 0 : tabItemList.size();
    }
}