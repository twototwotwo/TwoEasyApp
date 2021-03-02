package cn.wsjiu.twoEasy.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.List;

import cn.wsjiu.twoEasy.entity.IM.Message;

public class MessageDAO extends SQLiteOpenHelper {

    public static MessageDAO instance;

    
    public MessageDAO(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public MessageDAO(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version, @Nullable DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public MessageDAO(@Nullable Context context, @Nullable String name, int version, @NonNull SQLiteDatabase.OpenParams openParams) {
        super(context, name, version, openParams);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createSql = "create table message(chatId varchar(33), " +
                "sendId int(11), " +
                "receiveId int(11), " +
                "goodsId int(11), " +
                "content varchar(50), " +
                "contentType varchar(8), " +
                "timeStamp varchar(30), " +
                "isRead boolean)";
        db.execSQL(createSql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String dropSql = "drop table message";
        db.execSQL(dropSql);
        onCreate(db);
    }

    /**
     * 插入消息
     * @param message 消息
     * @return 插入操作受影响的记录条数
     */
    public long insertMessage(Message message) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues contentValues = message.toContentValues();
        return db.insert(getDatabaseName(), null, contentValues);
    }

    /**
     * 查询聊天记录
     * @param chatId 聊天id
     * @param offset 偏移量
     * @param size 数量
     * @return 聊天消息
     */
    public List<Message> queryMessage(String chatId, int offset, int size) {
        SQLiteDatabase db = this.getReadableDatabase();
        String tableName = getDatabaseName();
        String[] columns = Message.columnNames;
        int i = 0;
        String selection = "chatId = ?";
        String[] selectionArgs = {chatId};
        String limit = null;
        if(offset >= 0 && size > 0) {
            limit = offset + "," + size;
        }
        Cursor cursor = db.query(tableName, columns, selection, selectionArgs,
                null, null, "timeStamp desc", limit);
        int count = cursor.getCount();
        cursor.moveToFirst();
        List<Message> messageList = new ArrayList<>(count);
        while(count-- > 0) {
            Message message = new Message(cursor);
            messageList.add(message);
            cursor.moveToNext();
        }
        return messageList;
    }

    /**
     * @return 本地所有聊天记录id
     */
    public List<String> queryAllChatId() {
        SQLiteDatabase db = this.getReadableDatabase();
        String tableName = getDatabaseName();
        String[] columns = {"chatId"};
        Cursor cursor = db.query(true, tableName, columns, null, null,
                null, null, null, null);
        cursor.moveToFirst();
        int count = cursor.getCount();
        List<String> chatIdList = new ArrayList<>(count);
        while (count-- > 0) {
            String chatId = cursor.getString(0);
            if(chatId != null && chatId.length() > 0) {
                chatIdList.add(chatId);
            }
            cursor.moveToNext();
        }
        return chatIdList;
    }

    public int queryUnReadCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        String tableName = getDatabaseName();
        String[] columns = {"count(chatId)"};
        String selection = "isRead = ?";
        String[] selectionArgs = {"0"};
        Cursor cursor = db.query(false, tableName, columns, selection, selectionArgs,
                null, null, null, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        return Math.max(count, 0);
    }

    public int queryUnReadCountByChatId(String chatId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String tableName = getDatabaseName();
        String[] columns = {"count(chatId)"};
        String selection = "isRead = ? and chatId = ?";
        String[] selectionArgs = {"0", chatId};
        Cursor cursor = db.query(false, tableName, columns, selection, selectionArgs,
                null, null, null, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        return Math.max(count, 0);
    }

    public void updateChatMessageToRead(String chatId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String tableName = getDatabaseName();
        String sql = "update " + tableName + " set isRead = 1 where chatId = '" + chatId + "'";
        db.execSQL(sql);
    }

    public static void create(Context context, String name,
                              SQLiteDatabase.CursorFactory factory, int version) {
        if(instance == null) {
            synchronized (MessageDAO.class) {
                if(instance == null) {
                    instance = new MessageDAO(context, name, factory, version);
                }
            }
        }
    }
}
