package cn.githan.yunnote.Activitys;

import android.support.v7.app.AppCompatActivity;

import cn.githan.yunnote.Managers.NoteSyncManager;
import cn.githan.yunnote.Managers.SQLiteManager;
import cn.githan.yunnote.Managers.UserManager;
import cn.githan.yunnote.MyApplication;

/**
 * Created by BW on 16/9/15.
 */
public class BaseActivity extends AppCompatActivity {

    public MyApplication getApp(){
        return (MyApplication)getApplicationContext();
    }

    public UserManager getUserManager(){
        return getApp().getUserManager();
    }

    public SQLiteManager getSQLiteManager(){
        return getApp().getSqLiteManager();
    }

    public NoteSyncManager getNoteSyncManager(){
        return getApp().getNoteSyncManager();
    }

}
