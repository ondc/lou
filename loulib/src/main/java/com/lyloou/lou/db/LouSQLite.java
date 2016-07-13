package com.lyloou.lou.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * 类描述：一个通用的SQLite，通过简单的配置快速搭建一个数据库存储的方案；
 * 创建人： Lou
 * 创建时间： 2016/7/13 10:10
 * 修改人： Lou
 * 修改时间：2016/7/13 10:10
 * 修改备注：
 */
public class LouSQLite extends SQLiteOpenHelper {

    public interface ICallBack {
        String getName();

        int getVersion();

        void doUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);

        List<String> createTablesSQL();

        <T> void assignValuesByInstance(String tableName, T t, ContentValues values);

        <T> T newInstanceByCursor(String tableName, Cursor cursor);
    }

    private static final String ILLEGAL_OPREATION = "非法操作，请先进行初始化操作：LouSQLite.init()";

    private static LouSQLite INSTANCE;

    public static void init(@NonNull Context context, @NonNull ICallBack callBack) {
        if (INSTANCE == null) {
            INSTANCE = new LouSQLite(context, callBack);
        }
    }


    public static <T> void insert(String tableName, T t) {
        if (INSTANCE == null) {
            throw new IllegalStateException(ILLEGAL_OPREATION);
        }

        SQLiteDatabase db = INSTANCE.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            INSTANCE.mCallBack.assignValuesByInstance(tableName, t, values);
            db.insert(tableName, null, values);
            values.clear();
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public static <T> void insert(String tableName, List<T> ts) {
        if (INSTANCE == null) {
            throw new IllegalStateException(ILLEGAL_OPREATION);
        }

        SQLiteDatabase db = INSTANCE.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            for (T t : ts) {
                INSTANCE.mCallBack.assignValuesByInstance(tableName, t, values);
                db.insert(tableName, null, values);
                values.clear();
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public static <T> void update(String tableName, T t, String whereClause, String[] whereArgs) {
        if (INSTANCE == null) {
            throw new IllegalStateException(ILLEGAL_OPREATION);
        }

        SQLiteDatabase db = INSTANCE.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            INSTANCE.mCallBack.assignValuesByInstance(tableName, t, values);
            db.update(tableName, values, whereClause, whereArgs);
            values.clear();
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
    }


    public static <T> void update(String tableName, List<T> ts, String whereClause, String[] whereArgs) {
        if (INSTANCE == null) {
            throw new IllegalStateException(ILLEGAL_OPREATION);
        }

        SQLiteDatabase db = INSTANCE.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            for (T t : ts) {
                INSTANCE.mCallBack.assignValuesByInstance(tableName, t, values);
                db.update(tableName, values, whereClause, whereArgs);
                values.clear();
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public static void delete(String tableName, String whereClause, String[] whereArgs) {
        if (INSTANCE == null) {
            throw new IllegalStateException(ILLEGAL_OPREATION);
        }

        SQLiteDatabase db = INSTANCE.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(tableName, whereClause, whereArgs);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public static <T> List<T> query(String tableName, @NonNull String queryStr, @Nullable String[] whereArgs) {
        if (INSTANCE == null) {
            throw new IllegalStateException(ILLEGAL_OPREATION);
        }

        List<T> lists = new ArrayList<>();
        SQLiteDatabase db = INSTANCE.getReadableDatabase();
        db.beginTransaction();
        try {
            db.setTransactionSuccessful();
            Cursor cursor = db.rawQuery(queryStr, whereArgs);
            if (cursor.moveToFirst()) {
                do {
                    T tt = INSTANCE.mCallBack.newInstanceByCursor(tableName, cursor);
                    if (tt != null) {
                        lists.add(tt);
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        } finally {
            db.endTransaction();
            db.close();
        }

        return lists;
    }


    ///////////////////////////////////////////////////////////////////////////
    // Self
    ///////////////////////////////////////////////////////////////////////////
    private final ICallBack mCallBack;

    private static final String TAG = "LouSQLite";


    private LouSQLite(Context context, ICallBack callBack) {
        super(context, callBack.getName(), null, callBack.getVersion());
        mCallBack = callBack;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (String create_table : mCallBack.createTablesSQL()) {
            db.execSQL(create_table);
            Log.d(TAG, "create table " + "[ \n" + create_table + "\n ]" + " successful! ");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        mCallBack.doUpgrade(sqLiteDatabase, oldVersion, newVersion);
    }
}
