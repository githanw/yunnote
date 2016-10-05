package cn.githan.yunnote.Managers;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.githan.yunnote.MyApplication;
import cn.githan.yunnote.Controllers.MySQLiteOpenHelper;
import cn.githan.yunnote.Controllers.NetWorkAsyncTask;
import cn.githan.yunnote.Constants.Constant;
import cn.githan.yunnote.Models.Note;
import cn.githan.yunnote.Utils.MyJsonParser;
import cn.githan.yunnote.Utils.MyLog;


/**
 * Created by BW on 16/9/19.
 * 这个类用于同步客户端与服务器的数据
 * 同步逻辑：
 * 1，把queue表的数据全部推送到服务器
 * 2，向服务器获取当前用户的全部数据
 * 3，接收到的数据与服务器数据进行对比分析：（对比nid）
 * >>>>1 客户端数据 > 服务器数据
 * >>>>>>>> 找出服务端没有的数据，封装成Json，通过queue的方式发送给服务器进行更新
 * >>>>>>>> 修改本地数据的同步状态
 * >>>>2 客户端数据 = 服务器数据
 * >>>>>>>> 修改本地数据的同步状态
 * >>>>3 客户端数据 < 服务端数据
 * >>>>>>>> 找出本地没有的数据，插入note表。
 * >>>>>>>> 修改本地数据的同步状态
 * 4，通过接口告诉外界同步已经完成
 */
public class NoteSyncManager {

    private Context context;
    private UserManager userManager;
    private OnSyncSucceedListener onSyncSucceedListener;
    private NoteManager noteManager;
    private SQLiteManager sqLiteManager;
    private List<Note> queueDataList;
    private String username;
    private List<Note> serverExtractNotes = new ArrayList<>();
    private List<Note> clientExtractNotes = new ArrayList<>();
    public static final String SYNC_SUCCEED = "Sync succeed";


    public NoteSyncManager(Context context, UserManager userManager) {
        this.context = context;
        this.userManager = userManager;
        noteManager = ((MyApplication) context).getNoteManager();
        sqLiteManager = ((MyApplication) context).getSqLiteManager();
    }

    /**
     * start sync data
     *
     * @param username username
     * @param listener on sync succeed listener
     */
    public void startSyncData(String username, OnSyncSucceedListener listener) {
        //set on sync succeed listener
        onSyncSucceedListener = listener;

        //set username
        this.username = username;

        //step 1 update queue
        updateQueueToServer(username);

        //step 2 request full data
        requestFullData();

    }

    /**
     * 1,select * from queue.
     * 2,encode them to json.
     * 3,send them to server.
     *
     * @param username
     */
    public void updateQueueToServer(String username) {

        //get queue content
        if (queueDataList != null && queueDataList.size() != 0) {
            queueDataList.clear();
        }
        queueDataList = sqLiteManager.queryQueue();
        if (!(queueDataList.size() == 0)) {
            JSONArray array = new JSONArray();
            for (int i = 0; i < queueDataList.size(); i++) {
                JSONObject object = new JSONObject();
                Note note = queueDataList.get(i);
                try {
                    object.put(MySQLiteOpenHelper.QUEUE_REQUEST_TYPE, note.getUpdateAction());
                    object.put(MySQLiteOpenHelper.SQL_NOTE_ID, note.getnId());
                    object.put(MySQLiteOpenHelper.SQL_NOTE_TITLE, note.getnTitle());
                    object.put(MySQLiteOpenHelper.SQL_NOTE_CONTENT, note.getnContent());
                    object.put(MySQLiteOpenHelper.SQL_NOTE_TIME, note.getnTime());
                    object.put(MySQLiteOpenHelper.SQL_NOTE_SYNC, 1);
                    object.put(Constant.SP_USERNAME, username);
                    array.put(i, object);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            String url = Constant.ADDRESS + "queue.php";
            String msg = "msg=" + array.toString();
            new NetWorkAsyncTask(new NetWorkAsyncTask.NetWorkResultListener() {
                @Override
                public void onResultData(String data) {
                    MyLog.log("onResultData: " + data);
                    try {
                        //update queue result
                        JSONObject object = new JSONObject(data);
                        if (object.getInt("result") == 1) {
                            sqLiteManager.clearQueue();
                            sqLiteManager.changeNoteSyncStatus(queueDataList);

                        } else if (object.getInt("result") == 0) {
                            //queue update failed
                            MyLog.log("queue update failed");
                            onSyncSucceedListener.onNoteSyncSucceed("queue update failed");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }).execute(url, msg);
        } else {
            MyLog.log("table queue is empty.");
        }


    }

    /**
     * send full data request to server
     * normally result data is Json Array.
     * if it is not Json Array, it proves there is no data on server, then send local full data to server.
     */
    public void requestFullData() {
        MyLog.log("Start request full data.");
        String url = Constant.ADDRESS + Constant.NOTE + ".php";
        JSONObject object = MyJsonParser.packageJsonObject(Constant.FULLDATA_REQUEST, username);
        String msg = MyJsonParser.jsonObjectToStr(object);
        new NetWorkAsyncTask(new NetWorkAsyncTask.NetWorkResultListener() {
            @Override
            public void onResultData(String data) {
                MyLog.log("onResultData: " + data);
                //request full data
                try {
                    List<Note> notesFromServer = new ArrayList<>();
                    JSONArray array = new JSONArray(data);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject object = array.getJSONObject(i);
                        Note note = new Note(
                                object.getInt(MySQLiteOpenHelper.SQL_NOTE_ID),
                                object.getString(MySQLiteOpenHelper.SQL_NOTE_TITLE),
                                object.getString(MySQLiteOpenHelper.SQL_NOTE_CONTENT),
                                object.getString(MySQLiteOpenHelper.SQL_NOTE_TIME),
                                object.getInt(MySQLiteOpenHelper.SQL_NOTE_SYNC));
                        notesFromServer.add(note);
                    }

                    //start analyseData
                    analyseData(notesFromServer);

                } catch (JSONException e1) {
                    //server data is null, push local data to server
                    clientExtractNotes.clear();
                    clientExtractNotes = sqLiteManager.queryNote();
                    sqLiteManager.insertToQueue(clientExtractNotes);
                    updateQueueToServer(username);
                    onSyncSucceedListener.onNoteSyncSucceed(SYNC_SUCCEED);
                }
            }
        }).execute(url, msg);
    }

    public void analyseData(List<Note> notesFromServer) {
        MyLog.log("Start analyse data from server");
        List<Note> notesFromClient = sqLiteManager.queryNote();

        if (notesFromClient.size() > notesFromServer.size()) {
            MyLog.log("Client data > server data");
            clientExtractNotes.clear();
            for (int i = 0; i < notesFromClient.size(); i++) {
                Note note = notesFromClient.get(i);
                if (!contains(note, notesFromServer)) {
                    clientExtractNotes.add(note);
                    MyLog.log("clientExtractNotes added: " + note.toString());
                }
            }

            //send clientExtractData to server
            sqLiteManager.insertToQueue(clientExtractNotes);
            updateQueueToServer(username);
            onSyncSucceedListener.onNoteSyncSucceed(SYNC_SUCCEED);

        } else if (notesFromClient.size() < notesFromServer.size()) {
            MyLog.log("Client data < server data");
            serverExtractNotes.clear();
            for (int i = 0; i < notesFromServer.size(); i++) {
                Note note = notesFromServer.get(i);
                if (!contains(note, notesFromClient)) {
                    serverExtractNotes.add(note);
                    MyLog.log("serverExtractNotes added: " + note.getnId());
                }
            }

            //update serverExtractData to client database;
            sqLiteManager.addNote(serverExtractNotes);
            onSyncSucceedListener.onNoteSyncSucceed(SYNC_SUCCEED);

        }else {
            //Compare result is equal. Sync done.
            onSyncSucceedListener.onNoteSyncSucceed(SYNC_SUCCEED);
        }
    }

    /**
     * check if List<Note> contain note
     *
     * @param note
     * @param notes
     * @return
     */
    public boolean contains(Note note, List<Note> notes) {
        for (int i = 0; i < notes.size(); i++) {
            if (notes.get(i).getnId() == note.getnId()) {
                return true;
            }
        }
        return false;
    }


    public interface OnSyncSucceedListener {
        void onNoteSyncSucceed(String data);
    }
}
