package cn.githan.yunnote.Managers;

import android.content.Context;

import org.json.JSONObject;

import cn.githan.yunnote.Controllers.NetWorkAsyncTask;
import cn.githan.yunnote.Constants.Constant;
import cn.githan.yunnote.Utils.MyJsonParser;
import cn.githan.yunnote.Utils.MySharePref;


/**
 * Created by BW on 16/9/15.
 * 管理用户登录及注册的功能
 */
public class UserManager implements NetWorkAsyncTask.NetWorkResultListener {


    private Context context;
    private JsonDataReceivedListener jsonDataReceivedListener;
    private String username, password;
    private MySharePref sp;

    public UserManager(Context context) {
        this.context = context;
        sp = new MySharePref(context);
    }

    /**
     * start to connect server
     *
     * @param requestCode switch type to identify login or register
     * @param user        username
     * @param passwd      password
     * @param listener    on data result listener
     */
    public void startConnectServer(String requestCode, String user, String passwd, JsonDataReceivedListener listener) {
        jsonDataReceivedListener = listener;
        String url = Constant.ADDRESS + requestCode + ".php";
        JSONObject object = MyJsonParser.packageJsonObject(requestCode, user, passwd);
        String msg = MyJsonParser.jsonObjectToStr(object);
        new NetWorkAsyncTask(this).execute(url, msg);
    }

    @Override
    public void onResultData(String data) {
        jsonDataReceivedListener.onReceived(data);
    }

    /**
     * if login succeed set login user information
     *
     * @param username username
     * @param password password
     */
    public void setUserLoginInfo(String username, String password, boolean savePWD) {
        this.username = username;
        this.password = password;
        sp.putBoolean(Constant.SP_SAVEPASSWD, savePWD);
        sp.putString(Constant.SP_USERNAME, username);
        sp.putString(Constant.SP_PASSWORD, password);
        sp.putBoolean(Constant.SP_IS_USER_LOGIN, true);
        sp.commit();
    }

    public boolean isUserLogin() {
        return sp.getBoolean(Constant.SP_IS_USER_LOGIN);
    }

    /**
     * set user log out
     */
    public void setUserLogOut() {
        sp.putBoolean(Constant.SP_IS_USER_LOGIN, false);
        sp.putString(Constant.SP_USERNAME, null);
        sp.putString(Constant.SP_PASSWORD, null);
        sp.commit();
    }

    public String getUsername() {
        return sp.getString(Constant.SP_USERNAME);
    }

    public String getPassword() {
        return sp.getString(Constant.SP_PASSWORD);
    }

    public interface JsonDataReceivedListener {
        void onReceived(String data);
    }
}
