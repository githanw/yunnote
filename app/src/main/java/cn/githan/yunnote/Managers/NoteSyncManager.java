package cn.githan.yunnote.Managers;

import android.content.Context;
import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.githan.yunnote.Constants.Constant;
import cn.githan.yunnote.Controllers.MySQLiteOpenHelper;
import cn.githan.yunnote.Controllers.NetWorkAsyncTask;
import cn.githan.yunnote.Models.Note;
import cn.githan.yunnote.MyApplication;
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
    private OnSyncResultListener onSyncResultListener;
    private NoteManager noteManager;
    private SQLiteManager sqLiteManager;
    private List<Note> queueDataList;
    private String username;
    private List<Note> serverExtractNotes = new ArrayList<>();
    private List<Note> clientExtractNotes = new ArrayList<>();
    public static final String SYNC_SUCCEED = "Sync succeed";
    public static final String SYNC_FAILED = "Sync failed";
    public static final int RESULT_SUCCEED = 1;
    public static final int RESULT_FAILED = 0;


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
    public void startSync(String username, OnSyncResultListener listener) {
        /*
        set on sync succeed listener
         */
        onSyncResultListener = listener;
        /*
        set username
         */
        this.username = username;
        /*
        step 1 update queue
         */
        uploadQueueToServer(username);
    }

    /**
     * 1,select * from queue.
     * 2,encode them to json.
     * 3,send them to server.
     *
     * @param username
     */
    private void uploadQueueToServer(String username) {
        String uploadData = queryQueueToString();
        if (uploadData == null) {
            requestFullData();
        } else {
            String url = Constant.ADDRESS + "queue.php";
            MyLog.log(url);
            String msg = "msg=" + uploadData;
            MyLog.log("uploadQueueToServer >>> upload data : " + msg);
            MyLog.log("uploadQueueToServer >>> Start upload queue to server.");

            new NetWorkAsyncTask(new NetWorkAsyncTask.NetWorkResultListener() {
                @Override
                public void onResultData(String data) {
                    MyLog.log("uploadQueueToServer >>> onResultData: " + data);
                    try {
                        //update queue result
                        JSONObject object = new JSONObject(data);
                        if (object.getInt("result") == RESULT_SUCCEED) {
                            sqLiteManager.clearQueue();
                            sqLiteManager.changeNoteSyncStatus(queueDataList);
                            /*
                            step 2 request full data
                             */
                            requestFullData();
                        } else if (object.getInt("result") == RESULT_FAILED) {
                            onSyncResultListener.onNoteSyncResult(RESULT_FAILED, SYNC_FAILED);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }).execute(url, msg);
        }
    }


    /**
     * re upload queue data to server
     */
    private void reUploadQueueToServer() {
        String uploadData = queryQueueToString();
        if (uploadData == null) {
            onSyncResultListener.onNoteSyncResult(RESULT_SUCCEED, SYNC_SUCCEED);
        } else {
            String url = Constant.ADDRESS + "queue.php";
            String msg = "msg=" + uploadData;
            MyLog.log("reUpload data : " + msg);
            MyLog.log("Start upload queue to server.");

            new NetWorkAsyncTask(new NetWorkAsyncTask.NetWorkResultListener() {
                @Override
                public void onResultData(String data) {
                    MyLog.log("reUploadQueueToServer >>> onResultData: " + data);
                    try {
                        //update queue result
                        JSONObject object = new JSONObject(data);
                        if (object.getInt("result") == RESULT_SUCCEED) {
                            sqLiteManager.clearQueue();
                            sqLiteManager.changeNoteSyncStatus(queueDataList);
                            onSyncResultListener.onNoteSyncResult(RESULT_SUCCEED, SYNC_SUCCEED);

                        } else if (object.getInt("result") == RESULT_FAILED) {
                            onSyncResultListener.onNoteSyncResult(RESULT_FAILED, SYNC_FAILED);
                        }
                    } catch (JSONException e) {
                        MyLog.log("reUploadQueueToServer >>> parsing json failed");
                        e.printStackTrace();
                    }
                }
            }).execute(url, msg);
        }
    }

    /**
     * query table queue & return String of content;
     *
     * @return
     */
    private String queryQueueToString() {
        String returnStr = null;

        //get queue content
        if (queueDataList != null && queueDataList.size() != 0) {
            queueDataList.clear();
        }
        queueDataList = sqLiteManager.queryQueue();
        if (queueDataList.size() != 0) {
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

                    /*
                    analysing content of note & get file name of medias, put medias into json
                     */
                    JSONArray mediaArray = convertMediaDataToJsonArray(note.getnContent(), note.getnId());
                    if (mediaArray.length() > 0) {
                        object.put("media", mediaArray);
                    }

                    array.put(i, object);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            returnStr = array.toString();
        } else {
            return null;
        }
        return returnStr;
    }

    /**
     * convert media data which from note content to json array
     *
     * @param content note content
     * @return json array
     */
    private JSONArray convertMediaDataToJsonArray(String content, int nId) {
        List<File> mediaFiles = getFileObjects(getMediaResources(content, nId));
        JSONArray mediaArray = new JSONArray();
        for (int j = 0; j < mediaFiles.size(); j++) {
            File file = mediaFiles.get(j);
            byte[] bytes = readFile(file);
            if (bytes != null) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("file", file.getName());
                    jsonObject.put("data", Base64.encodeToString(bytes, Base64.NO_WRAP));
                    mediaArray.put(j, jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return mediaArray;
    }

    /**
     * find media uri from note content by regex
     *
     * @param content note content
     * @return list of media uri
     */
    private List<String> getMediaResources(String content, int nid) {
        MyLog.log(content);
        List<String> resourceStrs = new ArrayList<>();
        String regex = "/(storage|mnt)/.{0,15}/Android/data/cn\\.githan\\.yunnote/files/media/" + nid + "FFFF.{0,15}\\.(jpg|mp4)";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(content);
        while (m.find()) {
            String resourcePath = m.group();
            MyLog.log("getMediaResources >>> find matcher. nid= " + nid + " resourcePath= " + resourcePath);
            resourceStrs.add(resourcePath);
        }

        return resourceStrs;
    }

    /**
     * get file objects from list of media uri
     *
     * @param resourceStrs media paths
     * @return file objects
     */
    private List<File> getFileObjects(List<String> resourceStrs) {
        List<File> files = new ArrayList<>();
        for (int i = 0; i < resourceStrs.size(); i++) {
            String filePath = resourceStrs.get(i);
            File file = new File(filePath);
            if (file.exists()) {
                files.add(file);
            }
        }
        return files;
    }

    /**
     * read file as byte[]
     *
     * @param file file
     * @return byte[] of file
     */
    private byte[] readFile(File file) {
        byte[] resultData;
        if (file.exists()) {
            byte[] buff = new byte[1024];
            int len = -1;
            try {
                FileInputStream fis = new FileInputStream(file);
                ByteArrayOutputStream baos = new ByteArrayOutputStream(fis.available());
                while ((len = fis.read(buff)) != -1) {
                    baos.write(buff, 0, len);
                }
                resultData = baos.toByteArray();
                baos.close();
                fis.close();
                return resultData;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * send full data request to server
     * normally result data is Json Array.
     * if it is not Json Array, it proves there is no data on server, then send local full data to server.
     */
    private void requestFullData() {
        MyLog.log("requestFullData >>> Start request full data.");
        String url = Constant.ADDRESS + Constant.NOTE + ".php";
        JSONObject object = MyJsonParser.packageJsonObject(Constant.FULLDATA_REQUEST, username);
        String msg = MyJsonParser.jsonObjectToStr(object);

        new NetWorkAsyncTask(new NetWorkAsyncTask.NetWorkResultListener() {
            @Override
            public void onResultData(String data) {
                MyLog.log("requestFullData >>> onResultData: " + data);
                if (data == null) {
                    return;
                }

                try {
                    JSONObject resultJson = new JSONObject(data);
                    int result = resultJson.getInt("result");
                    if (result == RESULT_SUCCEED) {
                        List<Note> notesFromServer = new ArrayList<>();
                        JSONArray array = new JSONArray(resultJson.getString("msg"));
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject object = array.getJSONObject(i);
                            Note note = new Note(
                                    object.getInt(MySQLiteOpenHelper.SQL_NOTE_ID),
                                    object.getString(MySQLiteOpenHelper.SQL_NOTE_TITLE),
                                    object.getString(MySQLiteOpenHelper.SQL_NOTE_CONTENT),
                                    object.getString(MySQLiteOpenHelper.SQL_NOTE_TIME),
                                    object.getInt(MySQLiteOpenHelper.SQL_NOTE_SYNC));
                            /*
                            save media data into note.
                             */
                            if (object.has("media")) {
                                JSONArray mediaArray = object.getJSONArray("media");
                                List<Map> medias = new ArrayList<>();
                                for (int j = 0; j < mediaArray.length(); j++) {
                                    Map<String, String> media = new HashMap<>();
                                    JSONObject mediaObject = mediaArray.getJSONObject(j);
                                    media.put("file", mediaObject.getString("file"));
                                    media.put("data", mediaObject.getString("data"));
                                    medias.add(media);
                                }
                                if (medias.size() > 0) {
                                    note.setMedias(medias);
                                }
                            }

                            notesFromServer.add(note);
                        }

                        /*
                        step 3 start analyseData
                         */
                        analyseData(notesFromServer);

                    } else if (result == RESULT_FAILED) {
                        if (resultJson.getString("msg").equals("empty")) {
                            /*
                            no data on sever
                             */
                            List<Note> notes = sqLiteManager.queryNote();
                            if (notes.size() != 0) {
                                sqLiteManager.insertToQueue(notes);
                                reUploadQueueToServer();
                            } else {
                                onSyncResultListener.onNoteSyncResult(RESULT_SUCCEED, SYNC_SUCCEED);
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }).execute(url, msg);
    }

    /**
     * analysing data from server
     *
     * @param notesFromServer data from server
     */
    private void analyseData(List<Note> notesFromServer) {
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

            /*
            put extraData into table queue;
             */
            sqLiteManager.insertToQueue(clientExtractNotes);
             /*
            restart to upload queue to server
             */
            reUploadQueueToServer();

        } else if (notesFromClient.size() < notesFromServer.size()) {
            MyLog.log("Client data < server data");
            serverExtractNotes.clear();

            for (int i = 0; i < notesFromServer.size(); i++) {
                Note note = notesFromServer.get(i);
                if (!contains(note, notesFromClient)) {
                    serverExtractNotes.add(note);
                    /*
                    save media data to local files
                     */
                    if (note.getMedias() != null) {
                        List<Map> medias = note.getMedias();
                        for (int j = 0; j < medias.size(); j++) {
                            Map<String, String> media = medias.get(j);
                            String fileName = media.get("file");
                            String fileData = media.get("data");
                            /*
                            1，decode base64 to byte[]
                            2，write byte[] to local file
                             */
                            byte[] decodeData = Base64.decode(fileData, Base64.NO_WRAP);
                            writeMediaToFile(fileName, decodeData);
                        }
                    }
                }
            }

            /*
            update serverExtractData to client database;
             */
            sqLiteManager.addNote(serverExtractNotes);

            /*
            tell outside sync finished.
             */
            onSyncResultListener.onNoteSyncResult(RESULT_SUCCEED, SYNC_SUCCEED);

        } else {
            /*
            there is the same data on both client & server.
             */
            MyLog.log("Client data = server data");
            onSyncResultListener.onNoteSyncResult(RESULT_SUCCEED, SYNC_SUCCEED);
        }
    }

    /**
     * write media byte[] from server to local file
     *
     * @param fileName   file name
     * @param decodeData media data
     */
    private void writeMediaToFile(String fileName, byte[] decodeData) {
        try {
            File file = new File(context.getExternalFilesDir("media").toString() + "/" + fileName);
            if (file.exists()) {
                file.delete();
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(decodeData);
            fos.flush();
            fos.close();
            MyLog.log("writeMediaToFile >>> " + file.toString() + "  succeed");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * check if List<Note> contain note
     *
     * @param note
     * @param notes
     * @return
     */
    private boolean contains(Note note, List<Note> notes) {
        for (int i = 0; i < notes.size(); i++) {
            if (notes.get(i).getnId() == note.getnId()) {
                return true;
            }
        }
        return false;
    }


    public interface OnSyncResultListener {
        void onNoteSyncResult(int resultCode, String content);
    }
}
