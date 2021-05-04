package cn.wsjiu.twoEasy.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cn.wsjiu.twoEasy.R;
import cn.wsjiu.twoEasy.entity.Goods;
import cn.wsjiu.twoEasy.entity.SubscribeRecord;
import cn.wsjiu.twoEasy.entity.User;
import cn.wsjiu.twoEasy.entity.UserProfile;
import cn.wsjiu.twoEasy.result.Result;
import cn.wsjiu.twoEasy.thread.HttpGetRunnable;
import cn.wsjiu.twoEasy.thread.threadPool.ThreadPoolUtils;

import static android.content.Context.MODE_PRIVATE;

public class UserUtils {

    /**
     * 用户信息持久化的key
     */
    private final static String USER = "user";

    /**
     * 用户画像信息持久化的key
     */
    private final static String USER_PROFILE = "userProfile";

    /**
     * 用户信息
     */
    private static User user;

    private static UserProfile userProfile;

    public static void init(Context context) {
        if(user == null) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(USER, MODE_PRIVATE);
            String userStr = sharedPreferences.getString(USER, null);
            user = JSONObject.parseObject(userStr, User.class);
        }
        if(userProfile == null) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(USER, MODE_PRIVATE);
            String userProfileStr = sharedPreferences.getString(USER_PROFILE, null);
            JSONObject keyWordJSONObject = JSONObject.parseObject(userProfileStr);
            userProfile = new UserProfile();
            userProfile.init(keyWordJSONObject);
        }
    }

    public static void destroy(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER, MODE_PRIVATE);
        String userStr = sharedPreferences.getString(USER, null);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        user = JSONObject.parseObject(userStr, User.class);
    }

    public static User getUser() {
        return user;
    }

    public static void setUser(User me) {
        user = me;
    }

    public static void saveUser(User user, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER, MODE_PRIVATE);
        sharedPreferences.edit().putString(USER, JSONObject.toJSONString(user)).apply();
    }

    public static void updateUser(User newUser, Context context) {
        if(newUser.getHeadUrl() != null) {
            user.setHeadUrl(newUser.getHeadUrl());
            String completedUrl = context.getString(R.string.image_get_url) + newUser.getHeadUrl();
            ImageUtils.removeBitmapFromPool(completedUrl);
        }
        if(newUser.getUserNickName() != null) {
            user.setUserNickName(newUser.getUserNickName());
        }
        if(newUser.getDeclaration() != null) {
            user.setDeclaration(newUser.getDeclaration());
        }
        if(newUser.getHeadUrl() != null) {
            user.setHeadUrl(newUser.getHeadUrl());
        }
        saveUser(user, context);
    }

    public static void addKeyWord(String word) {
        userProfile.addKeyWord(word, UserProfile.DEFAULT_WEIGHT);
    }

    public static String getUserProfile() {
        return userProfile.toString();
    }

    public static void persistUserProfile(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER, MODE_PRIVATE);
        sharedPreferences.edit().putString(USER_PROFILE, JSONObject.toJSONString(userProfile.getKeyWordMap())).apply();
    }
}
