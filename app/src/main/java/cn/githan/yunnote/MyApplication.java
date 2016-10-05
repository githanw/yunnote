package cn.githan.yunnote;

import android.app.Application;

import cn.githan.yunnote.Managers.EditTextManager;
import cn.githan.yunnote.Managers.NoteManager;
import cn.githan.yunnote.Managers.NoteSyncManager;
import cn.githan.yunnote.Managers.SQLiteManager;
import cn.githan.yunnote.Managers.UserManager;
import cn.githan.yunnote.Constants.Constant;
import cn.githan.yunnote.Utils.MySharePref;

/**
 * Created by BW on 16/8/13.
 */
public class MyApplication extends Application {

    private NoteManager noteManager;
    private UserManager userManager;
    private NoteSyncManager noteSyncManager;
    private SQLiteManager sqLiteManager;
    private EditTextManager editTextManager;


    @Override
    public void onCreate() {
        super.onCreate();
        getNoteManager();
        getUserManager();

        // TODO: 16/9/5 check if there is network , start checking if user is is logged in,if it is logged in, set isUserLogin true
        // TODO: 16/9/5 if not, set isUserLogin false;
    }

    /**
     * NoteManager instance
     * @return NoteManager instance
     */
    public NoteManager getNoteManager() {
        if (noteManager == null) {
            noteManager = new NoteManager(this);
        }
        return noteManager;
    }

    /**
     * UserManager instance
     * @return UserManager instance
     */
    public UserManager getUserManager(){
        if (userManager==null){
            userManager = new UserManager(this);
        }
        return userManager;
    }

    /**
     * NoteSyncManager instance
     * @return NoteSyncManager instance
     */
    public NoteSyncManager getNoteSyncManager(){
        if (noteSyncManager==null){
            noteSyncManager = new NoteSyncManager(this,getUserManager());
        }
        return noteSyncManager;
    }

    /**
     * SQLiteManager instance
     * @return SQLiteManager instance
     */
    public SQLiteManager getSqLiteManager(){
        if (sqLiteManager==null){
            sqLiteManager = new SQLiteManager(this);
        }
        return sqLiteManager;
    }

    /**
     * clear user info when application destroyed.
     */
    public void clearUserInfo(){
        MySharePref sp = new MySharePref(this);
        boolean b = sp.getBoolean(Constant.SP_SAVEPASSWD);
        if (!b) {
            sp.putBoolean(Constant.SP_IS_USER_LOGIN, false);
            sp.putString(Constant.SP_USERNAME, null);
            sp.putString(Constant.SP_PASSWORD, null);
            sp.commit();
        }
    }

    public String getUsername() {
        return getUserManager().getUsername();
    }


    public String getPassword() {
        return getUserManager().getPassword();
    }

    @Override
    public void onTerminate() {
        clearUserInfo();
        super.onTerminate();
    }

    @Override
    public void onLowMemory() {
        clearUserInfo();
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        clearUserInfo();
        super.onTrimMemory(level);
    }


}
