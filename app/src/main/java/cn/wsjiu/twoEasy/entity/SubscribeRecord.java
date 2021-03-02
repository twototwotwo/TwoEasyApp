package cn.wsjiu.twoEasy.entity;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.SimpleFormatter;

/**
 * 订阅记录
 * @author wsj
 */
public class SubscribeRecord {
    /**
     * 订阅记录的用户
     */
    private Integer userId;
    /**
     * 用户订阅的物品的id
     */
    private Integer goodsId;
    /**
     * 订阅时的价格
     */
    private Float subscribePrice;

    /**
     * 订阅时的时间戳
     */
    private String time;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public SubscribeRecord() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        time = LocalDateTime.now().format(formatter);
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Integer goodsId) {
        this.goodsId = goodsId;
    }

    public Float getSubscribePrice() {
        return subscribePrice;
    }

    public void setSubscribePrice(Float subscribePrice) {
        this.subscribePrice = subscribePrice;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
