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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import cn.wsjiu.twoEasy.entity.Goods;
import cn.wsjiu.twoEasy.entity.SearchRecord;

public class SearchRecordDAO extends SQLiteOpenHelper {
    public static SearchRecordDAO instance;

    public SearchRecordDAO(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public SearchRecordDAO(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version, @Nullable DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public SearchRecordDAO(@Nullable Context context, @Nullable String name, int version, @NonNull SQLiteDatabase.OpenParams openParams) {
        super(context, name, version, openParams);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createSql = "create table searchRecord(recordId int, record varchar(100), " +
                "time timeStamp default current_timestamp)";
        db.execSQL(createSql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String dropSql = "drop table searchRecord";
        db.execSQL(dropSql);
        onCreate(db);
    }

    /**
     * 查询数据库中的所有搜索记录
     * @return 返回搜索记录
     */
    public static List<SearchRecord> queryAllSearchRecord() {
        String tableName = instance.getDatabaseName();
        String[] columns = SearchRecord.columns;
        Cursor cursor = instance.getReadableDatabase().query(tableName, columns,
                null, null, null, null, "time desc");
        cursor.moveToFirst();
        int count = cursor.getCount();
        List<SearchRecord> recordList = new ArrayList<>(count);
        while(count-- > 0) {
            if(!cursor.isClosed()) {
                SearchRecord record = new SearchRecord();
                record.setId(cursor.getInt(0));
                record.setRecord(cursor.getString(1));
                record.setTime(cursor.getString(2));
                recordList.add(record);
            }
        }
        return recordList;
    }

    /**
     * 插入搜索记录
     * @param record 搜索记录
     * @return true 插入成功，false插入失败
     */
    public static boolean insertRecord(SearchRecord record) {
        ContentValues contentValues = record.toContentValues();
        String dbName = instance.getDatabaseName();
        long rowId = instance.getWritableDatabase().insert(dbName, null, contentValues);
        return rowId >= 0;
    }

    /**
     * 更新搜索记录的时间
     * @return 返回成功与否
     */
    public static boolean updateRecordTime(SearchRecord searchRecord) {
        String dbName = instance.getDatabaseName();
        ContentValues contentValues = searchRecord.toContentValues();
        String whereClause = "id=?";
        String[] whereArgs = {String.valueOf(searchRecord.getId())};
        long rowEffectId = instance.getWritableDatabase().update(dbName, contentValues, whereClause, whereArgs);
        return rowEffectId >= 0;
    }

    public static void create(Context context, String name,
                              SQLiteDatabase.CursorFactory factory, int version) {
        if(instance == null) {
            synchronized (SearchRecord.class) {
                if(instance == null) {
                    instance = new SearchRecordDAO(context, name, factory, version);
                }
            }
        }
    }
}
