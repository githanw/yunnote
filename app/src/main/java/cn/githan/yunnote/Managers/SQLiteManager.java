package cn.githan.yunnote.Managers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import cn.githan.yunnote.Controllers.MySQLiteOpenHelper;
import cn.githan.yunnote.Models.Note;
import cn.githan.yunnote.Utils.MyLog;

/**
 * Created by BW on 16/8/22.
 * 管理note表，queue表的CRUD操作
 */
public class SQLiteManager {
    private Context context;
    private MySQLiteOpenHelper sqLiteOpenHelper;
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "YunNote.db";
    private SQLiteDatabase db;
    private ContentValues values;
    private List<Note> noteList = new ArrayList<>();

    public SQLiteManager(Context context) {
        this.context = context;
        sqLiteOpenHelper = new MySQLiteOpenHelper(this.context, DATABASE_NAME, null, DATABASE_VERSION);
        values = new ContentValues();
    }

    /**
     * open datebase
     */
    private void openDatabase() {
        if (db != null) {
            if (!db.isOpen()) {
                db = sqLiteOpenHelper.getWritableDatabase();
            }
        } else {
            db = sqLiteOpenHelper.getWritableDatabase();
        }

    }

    /**
     * close database
     */
    private void closeDatabase() {
        if (db != null) {
            if (db.isOpen()) {
                db.close();
            }
        }
    }

    /**
     * insert data to database
     *
     * @param table          table name
     * @param nullColumnHack nullColumnHack
     * @param values         values
     */
    public void insert(String table, String nullColumnHack, ContentValues values) {
        db.insert(table, nullColumnHack, values);
    }

    /**
     * query data from database
     *
     * @param table table name
     * @return Cursor
     */
    public Cursor query(String table) {
        Cursor c = db.query(table, null, null, null, null, null, null);
        return c;
    }

    /**
     * update data to database
     *
     * @param table       table name
     * @param values      values
     * @param whereClause where clause
     * @param whereArgs   where args
     */
    public void update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        db.update(table, values, whereClause, whereArgs);
    }

    /**
     * delete data from database
     *
     * @param table       table name
     * @param whereClause where clause
     * @param whereArgs   where args
     */
    public void delete(String table, String whereClause, String[] whereArgs) {
        db.delete(table, whereClause, whereArgs);
    }

    /**
     * put ContentValues to variable 'values'
     *
     * @param note note
     * @return ContentValues
     */
    public ContentValues putValues(Note note) {
        clearValues();
        values.put(MySQLiteOpenHelper.SQL_NOTE_ID, note.getnId());
        values.put(MySQLiteOpenHelper.SQL_NOTE_TITLE, note.getnTitle());
        values.put(MySQLiteOpenHelper.SQL_NOTE_CONTENT, note.getnContent());
        values.put(MySQLiteOpenHelper.SQL_NOTE_TIME, note.getnTime());
        values.put(MySQLiteOpenHelper.SQL_NOTE_SYNC, note.getnSync());
        return values;
    }

    /**
     * add note to table note
     *
     * @param note which note to be added
     */
    public void addNote(Note note) {
        values = putValues(note);
        openDatabase();
        insert(MySQLiteOpenHelper.NOTE, null, values);
        insertToQueue(MySQLiteOpenHelper.QUEUE_REQUEST_TYPE_ADD, values);
        closeDatabase();
        MyLog.log("SQLiteManager: Note added. nid: " + values.getAsString(MySQLiteOpenHelper.SQL_NOTE_ID));
    }

    /**
     * add new notes to table note
     *
     * @param notes list of notes to be added
     */
    public void addNote(List<Note> notes) {
        openDatabase();
        for (int i = 0; i < notes.size(); i++) {
            values = putValues(notes.get(i));
            insert(MySQLiteOpenHelper.NOTE, null, values);
            MyLog.log("SQLiteManager: Note added. nid: " + values.getAsString(MySQLiteOpenHelper.SQL_NOTE_ID));
        }
        closeDatabase();
    }

    /**
     * update table note
     *
     * @param note which to be updated
     */
    public void updateNote(Note note) {
        values = putValues(note);
        openDatabase();
        String Clause = MySQLiteOpenHelper.SQL_NOTE_ID + " = ?";
        String[] str = new String[]{values.getAsString(MySQLiteOpenHelper.SQL_NOTE_ID)};
        update(MySQLiteOpenHelper.NOTE, values, Clause, str);
        insertToQueue(MySQLiteOpenHelper.QUEUE_REQUEST_TYPE_UPDATE, values);
        closeDatabase();
        MyLog.log("SQLiteManager: Note updated. nid: " + values.getAsString(MySQLiteOpenHelper.SQL_NOTE_ID));
    }

    /**
     * delete list of notes
     *
     * @param notes which to be deleted
     */
    public void deleteNote(List<Note> notes) {
        String clause = MySQLiteOpenHelper.SQL_NOTE_ID + "=?";
        openDatabase();
        for (int i = 0; i < notes.size(); i++) {
            if (notes.get(i).isChecked()) {
                String[] str = new String[]{String.valueOf(notes.get(i).getnId())};
                values = putValues(notes.get(i));
                delete(MySQLiteOpenHelper.NOTE, clause, str);
                insertToQueue(MySQLiteOpenHelper.QUEUE_REQUEST_TYPE_DEL, values);
                MyLog.log("SQLiteManager: Note deleted. nid: " + values.getAsString(MySQLiteOpenHelper.SQL_NOTE_ID));
            }
        }
        closeDatabase();
    }

    /**
     * query table note
     *
     * @return list of notes
     */
    public List<Note> queryNote() {
        if (!noteList.isEmpty()) {
            noteList.clear();
        }
        openDatabase();
        Cursor c = query(MySQLiteOpenHelper.NOTE);
        if (c.moveToFirst()) {
            do {
                Note note = new Note();
                note.setnId(c.getInt(c.getColumnIndex(MySQLiteOpenHelper.SQL_NOTE_ID)));
                note.setnTitle(c.getString(c.getColumnIndex(MySQLiteOpenHelper.SQL_NOTE_TITLE)));
                note.setnContent(c.getString(c.getColumnIndex(MySQLiteOpenHelper.SQL_NOTE_CONTENT)));
                note.setnTime(c.getString(c.getColumnIndex(MySQLiteOpenHelper.SQL_NOTE_TIME)));
                note.setnSync(c.getInt(c.getColumnIndex(MySQLiteOpenHelper.SQL_NOTE_SYNC)));
                note.setChecked(false);
                noteList.add(note);
            } while (c.moveToNext());
        }
        c.close();
        closeDatabase();
        return noteList;
    }

    public void insertToQueue(String type, ContentValues contentValues) {
        contentValues.put(MySQLiteOpenHelper.QUEUE_REQUEST_TYPE, type);
        insert(MySQLiteOpenHelper.QUEUE, null, contentValues);
    }

    /**
     * insert list of note into table queue
     *
     * @param notes list of notes
     */
    public void insertToQueue(List<Note> notes) {
        clearValues();
        openDatabase();
        for (int i = 0; i < notes.size(); i++) {
            values.put(MySQLiteOpenHelper.SQL_NOTE_ID, notes.get(i).getnId());
            values.put(MySQLiteOpenHelper.SQL_NOTE_TITLE, notes.get(i).getnTitle());
            values.put(MySQLiteOpenHelper.SQL_NOTE_CONTENT, notes.get(i).getnContent());
            values.put(MySQLiteOpenHelper.SQL_NOTE_TIME, notes.get(i).getnTime());
            values.put(MySQLiteOpenHelper.SQL_NOTE_SYNC, notes.get(i).getnSync());
            values.put(MySQLiteOpenHelper.QUEUE_REQUEST_TYPE, MySQLiteOpenHelper.QUEUE_REQUEST_TYPE_ADD);
            insert(MySQLiteOpenHelper.QUEUE, null, values);
        }
        closeDatabase();
    }

    /**
     * query table queue
     *
     * @return List of notes
     */
    public List<Note> queryQueue() {
        // TODO: 16/9/20 return List<Note>
        List<Note> notes = new ArrayList<>();
        openDatabase();
        Cursor c = query(MySQLiteOpenHelper.QUEUE);
        if (c.moveToFirst()) {
            do {
                Note note = new Note(
                        c.getString(c.getColumnIndex(MySQLiteOpenHelper.QUEUE_REQUEST_TYPE)),
                        c.getInt(c.getColumnIndex(MySQLiteOpenHelper.SQL_NOTE_ID)),
                        c.getString(c.getColumnIndex(MySQLiteOpenHelper.SQL_NOTE_TITLE)),
                        c.getString(c.getColumnIndex(MySQLiteOpenHelper.SQL_NOTE_CONTENT)),
                        c.getString(c.getColumnIndex(MySQLiteOpenHelper.SQL_NOTE_TIME)),
                        c.getInt(c.getColumnIndex(MySQLiteOpenHelper.SQL_NOTE_SYNC))
                );
                notes.add(note);
            } while (c.moveToNext());
        }
        closeDatabase();
        return notes;
    }

    /**
     * clear ContentValues
     */
    private void clearValues() {
        if (values.size() != 0) {
            values.clear();
        }
    }

    /**
     * clear table queue;
     */
    public void clearQueue() {
        openDatabase();
        db.delete(MySQLiteOpenHelper.QUEUE, null, null);
        closeDatabase();
    }

    /**
     * change table column of note: 'nsync' to 1(means Synced)
     *
     * @param notes which to change
     */
    public void changeNoteSyncStatus(List<Note> notes) {
        openDatabase();
        clearValues();
        String clause = MySQLiteOpenHelper.SQL_NOTE_ID + "=?";
        for (int i = 0; i < notes.size(); i++) {
            String nId = String.valueOf(notes.get(i).getnId());
            values.put(MySQLiteOpenHelper.SQL_NOTE_SYNC, 1);
            update(MySQLiteOpenHelper.NOTE, values, clause, new String[]{nId});
        }
        closeDatabase();
    }
}
