package cn.githan.yunnote.Activitys;

import android.support.v7.app.AppCompatActivity;

import cn.githan.yunnote.Managers.EditTextManager;
import cn.githan.yunnote.MyApplication;
import cn.githan.yunnote.Managers.NoteSyncManager;
import cn.githan.yunnote.Managers.UserManager;

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

    public NoteSyncManager getNoteSyncManager(){
        return getApp().getNoteSyncManager();
    }

}
