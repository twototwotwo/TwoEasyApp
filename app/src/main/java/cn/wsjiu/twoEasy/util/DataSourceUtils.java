package cn.wsjiu.twoEasy.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import cn.wsjiu.twoEasy.dao.MessageDAO;
import cn.wsjiu.twoEasy.entity.Goods;
import cn.wsjiu.twoEasy.entity.Order;
import cn.wsjiu.twoEasy.entity.OrderState;
import cn.wsjiu.twoEasy.entity.SubscribeRecord;
import cn.wsjiu.twoEasy.entity.User;

/**
 * 数据源工具
 * 作用是应用内数据的统一缓存，包括用户信息和物品信息，订阅信息等
 * @author wsj
 */
public class DataSourceUtils {
    /**
     * 普通物品
     * key = goodsId
     * value = Goods
     */
    public static Map<Integer, Goods> commonGoodsMap = new ConcurrentHashMap<>(8);
    /**
     * 用户发布的物品
     * key = goodsId
     * value = Goods
     */
    public static Map<Integer, Goods> publishGoodsMap = new ConcurrentHashMap<>(8);

    /**
     * 订阅记录
     * key = goodsId
     * value = SubscribeRecord
     */
    public static Map<Integer, SubscribeRecord> subscribeRecordMap = new ConcurrentHashMap<>(8);
    /**
     * 用户信息
     * key =userId
     * value = User
     */
    public static Map<Integer, User> userMap = new ConcurrentHashMap<>(8);
    /**
     * 订单
     * key = goodsId
     * value = Order
     */
    public static Map<Integer, Order> orderMap = new ConcurrentHashMap<>();

    /**
     * 粉丝id集合
     */
    public static Set<Integer> fansIdSet = new HashSet<>();

    /**
     * 已关注用户的id集合
     */
    public static Set<Integer> followedIdSet = new HashSet<>();

    public static int unreadMessageCount = 0;


    public static void init(Map<Integer, Order> odMap,
                            Map<Integer, SubscribeRecord> srMap,
                            Map<Integer, User> uMap,
                            Map<Integer, Goods> cgMap,
                            Map<Integer, Goods> pgMap,
                            Set<Integer> fdSet) {
        commonGoodsMap = cgMap;
        subscribeRecordMap = srMap;
        publishGoodsMap = pgMap;
        orderMap = odMap;
        userMap = uMap;
        followedIdSet = fdSet;

        unreadMessageCount = MessageDAO.instance.queryUnReadCount();
    }


    public static Goods getGoods(Integer goodsId) {
        Goods goods = commonGoodsMap.get(goodsId);
        if(goods == null) goods = publishGoodsMap.get(goodsId);
        return goods;
    }

    public static void addCommonGoods(Goods goods) {
        commonGoodsMap.put(goods.getGoodsId(), goods);
    }

    public static void addPublishGoods(Goods goods) {
        commonGoodsMap.put(goods.getGoodsId(), goods);
    }

    public static boolean checkIsSubscribed(Integer goodsId) {
        if(subscribeRecordMap.containsKey(goodsId)) {
            return true;
        }
        return false;
    }

    public static void subscribedGoods(SubscribeRecord record, Goods goods) {
        subscribeRecordMap.put(record.getGoodsId(), record);
        commonGoodsMap.put(goods.getGoodsId(), goods);
    }

    public static void cancelSubscribedGoods(Integer goodsId) {
        subscribeRecordMap.remove(goodsId);
    }

    public static SubscribeRecord getSubscribeRecord(Integer goodsId) {
        return subscribeRecordMap.get(goodsId);
    }

    public static Set<Integer> getAllSubscribeGoodId() {
        return subscribeRecordMap.keySet();
    }

    public static User getUser(Integer userId) {
        return userMap.get(userId);
    }

    public static void addUser(User user) {
        userMap.put(user.getUserId(), user);
    }

    public static Order getOrder(Integer goodsId) {
        return orderMap.get(goodsId);
    }

    public static void addOrder(Order order) {
        orderMap.put(order.getGoodsId(), order);
    }

    public static boolean checkIsFollow(int userId) {
        return followedIdSet != null && followedIdSet.contains(userId);
    }

    public static void addFollow(int userId) {
        followedIdSet.add(userId);
    }

    public static void cancelFollow(int userId) {
        followedIdSet.remove(userId);
    }
}
