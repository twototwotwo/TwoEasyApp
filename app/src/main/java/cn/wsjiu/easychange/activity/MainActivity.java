package cn.wsjiu.easychange.activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSONObject;
import com.google.android.material.tabs.TabLayout;

import cn.wsjiu.easychange.R;
import cn.wsjiu.easychange.activity.ui.main.TabPagerAdapter;

public class MainActivity extends AppCompatActivity {

    /**
     *  tab button text
     */
    private int[] tabTexts = {
            R.string.tab_home,
            R.string.tab_question,
            R.string.tab_chat,
            R.string.tab_person
    };

    /**
     * tab button's icon id
     */
    private int[] tabIcons = {
            R.drawable.tab_home_icon_selector,
            R.drawable.tab_question_icon_selector,
            R.drawable.tab_chat_icon_selector,
            R.drawable.tab_person_icon_selector};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewPager viewPager = findViewById(R.id.view_pager);
        TabPagerAdapter tabPagerAdapter = new TabPagerAdapter(
                getApplicationContext(),
                getSupportFragmentManager());
        viewPager.setAdapter(tabPagerAdapter);
        //设置viewpager和tablayout联动
        TabLayout tabLayout = findViewById(R.id.tab_bar);
        tabLayout.setupWithViewPager(viewPager);
        // reset tab's icon and text
        for(int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            tab.setText(tabTexts[i]);
            tab.setIcon(tabIcons[i]);
        }

    }
}