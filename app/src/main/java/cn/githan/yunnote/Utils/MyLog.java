package cn.githan.yunnote.Utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.util.Random;

/**
 * Created by BW on 16/8/13.
 */
public class MyLog {

    private static final boolean showLog = true;
    private static final String TAG = "YNote";

    /**
     * show log
     * @param string string of log
     */
    public static void log(String string){
        if (showLog){
            Log.d(TAG,string);
        }
    }




}
