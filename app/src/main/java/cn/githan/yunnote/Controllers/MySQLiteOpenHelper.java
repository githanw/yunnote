package cn.githan.yunnote.Controllers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import cn.githan.yunnote.Constants.Constant;

/**
 * Created by BW on 16/8/22.
 *
 * 两个数据表的作用：
 * 1，note：客户端本地保存笔记信息
 * 2，queue：记录客户端本地对note的CUD操作，同步时发送到服务器进行服务端更新，数据每次成功同步后会清空
 *
 */
public class MySQLiteOpenHelper extends SQLiteOpenHelper {

    public static final String NOTE = "note";
    //    public static final String SQL_NOTE_ID = "id";
    public static final String SQL_NOTE_ID = "nid";
    public static final String SQL_NOTE_TITLE = "ntitle";
    public static final String SQL_NOTE_CONTENT = "ncontent";
    public static final String SQL_NOTE_TIME = "ntime";
    public static final String SQL_NOTE_SYNC = "nsync";
    public static final String QUEUE = "queue";
    public static final String QUEUE_REQUEST_TYPE = "action";
    public static final String QUEUE_REQUEST_TYPE_ADD = "add";
    public static final String QUEUE_REQUEST_TYPE_DEL = "delete";
    public static final String QUEUE_REQUEST_TYPE_UPDATE = "update";


    private static final String CREATE_NOTE = "create table " + Constant.NOTE + " (" +
            "id integer primary key autoincrement," +
            SQL_NOTE_ID + " integer not null," +
            SQL_NOTE_TITLE + " varchar(20)," +
            SQL_NOTE_CONTENT + " varchar(25)," +
            SQL_NOTE_TIME + " datetime not null," +
            SQL_NOTE_SYNC + " bool not null)";

    private static final String CREATE_UPDATE_QUEUE = "create table " + QUEUE + " (" +
            QUEUE_REQUEST_TYPE + " text not null," +
            SQL_NOTE_ID + " integer not null," +
            SQL_NOTE_TITLE + " varchar(20)," +
            SQL_NOTE_CONTENT + " varchar(25)," +
            SQL_NOTE_TIME + " datetime not null," +
            SQL_NOTE_SYNC + " bool not null)";

    public MySQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_NOTE);
        sqLiteDatabase.execSQL(CREATE_UPDATE_QUEUE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }

}
