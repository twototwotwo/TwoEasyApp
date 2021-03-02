package cn.wsjiu.twoEasy.entity.IM;

import android.content.ContentValues;
import android.database.Cursor;

import androidx.annotation.NonNull;

import java.sql.Timestamp;
import java.util.Date;

/**
 * 聊天消息
 */
public class Message {
    /**
     * 消息类型为文本
     */
    public  final static String CONTENT_TYPE_TEXT = "文字";
    /**
     * 消息类型为图片
     */
    public  final static String CONTENT_TYPE_IMAGE = "图片";

    /**
     * 消息类型为开眼邀请
     */
    public final static String CONTENT_OPEN_EYE = "开眼";

    /**
     * 与数据库内对应的字段名
     */
    public final static String[] columnNames = {"chatId", "sendId", "receiveId", "goodsId",
            "content", "contentType", "timeStamp", "isRead"};

    /**
     * 消息唯一id
     */
    String msgId;

    /**
     * 通信消息id，对于每一个会话记录是唯一
     * 会话记录指 卖家和买家在一件商品下的会话
     * 组成为 userId ： goodsId ： userId,用户id小在前，大的在后
     * 例如用户A（userId = 5），用户B（UserId = 10）， 物品c（goodsId = 20）
     * chatId = 5:20:10
     */
    String chatId;
    /**
     * 发送者的id
     */
    Integer sendId;
    /**
     * 接收者的id
     */
    Integer receiveId;

    /**
     * 物品id
     */
    Integer goodsId;
    /**
     * 内容
     */
    String content;
    /**
     * 内容类型
     */
    String contentType;
    /**
     * 发送的时间戳
     */
    Timestamp timeStamp;
    /**
     * 是否已读
     */
    Boolean isRead;

    public Message() {
        timeStamp = new Timestamp(new Date().getTime());
        isRead = false;
    }

    /**
     * @param cursor sqlite记录指针
     */
    public Message(@NonNull Cursor cursor) {
        chatId = cursor.getString(0);
        sendId = cursor.getInt(1);
        receiveId = cursor.getInt(2);
        goodsId = cursor.getInt(3);
        content = cursor.getString(4);
        contentType = cursor.getString(5);
        String str = cursor.getString(6);
        timeStamp = Timestamp.valueOf(cursor.getString(6));
        isRead = Boolean.getBoolean(cursor.getString(7));
    }

    public Integer getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Integer goodsId) {
        this.goodsId = goodsId;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public Integer getSendId() {
        return sendId;
    }

    public void setSendId(Integer sendId) {
        this.sendId = sendId;
    }

    public Integer getReceiveId() {
        return receiveId;
    }

    public void setReceiveId(Integer receiveId) {
        this.receiveId = receiveId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Timestamp getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = new Timestamp(timeStamp);
    }

    public Boolean getRead() {
        return isRead;
    }

    public void setRead(Boolean read) {
        isRead = read;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }


    /**
     * @return 返回为一个包含所有属性的contentValues
     */
    public ContentValues toContentValues() {
        ContentValues contentValues = new ContentValues();
        contentValues.put("chatId", chatId);
        contentValues.put("sendId", sendId);
        contentValues.put("goodsId", goodsId);
        contentValues.put("receiveId", receiveId);
        contentValues.put("content", content);
        contentValues.put("contentType", contentType);
        contentValues.put("timeStamp", timeStamp.toString());
        contentValues.put("isRead", isRead);
        return  contentValues;
    }

}
