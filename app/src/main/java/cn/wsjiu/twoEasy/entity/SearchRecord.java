package cn.wsjiu.twoEasy.entity;

import android.content.ContentValues;

/**
 * 首页搜索记录
 * @author wsj
 */
public class SearchRecord {
    public final static String columns[] = {"id", "record", "time"};
    /**
     * 记录id
     */
    private int id;

    /**
     * 记录内容
     */
    private String record;

    private String time;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRecord() {
        return record;
    }

    public void setRecord(String record) {
        this.record = record;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public ContentValues toContentValues() {
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", id);
        contentValues.put("record", record);
        contentValues.put("time", time);
        return contentValues;
    }
}
