package cn.githan.yunnote.Utils;

import android.content.Context;
import android.content.SyncStatusObserver;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * Created by BW on 16/9/14.
 */
public class MyUtils {
    /**
     * get formatted of current system time
     * @return string of time
     */
    public static String getSystemTime(){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        return format.format(date);
    }

    /**
     * get random number of note'id
     * @return int of number
     */
    public static int getRandomNumber(){
        Random random = new Random(System.currentTimeMillis());
        int num = Math.abs(random.nextInt());
        MyLog.log(String.valueOf(num));
        return num;
    }

    public static boolean isNetworkAvaliable(Context context){
        if (isMobileNetworkConneted(context)||isWifiConnected(context)){
            return true;
        }
        return false;
    }

    public static boolean isMobileNetworkConneted(Context paramContext) {
        boolean b = false;
        if (paramContext != null) {
            NetworkInfo localNetworkInfo = ((ConnectivityManager) paramContext.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
            if (localNetworkInfo != null) {
                b = localNetworkInfo.isAvailable();
            }
        }
        return b;
    }

    public static boolean isWifiConnected(Context paramContext) {
        boolean b = false;
        NetworkInfo localNetworkInfo = ((ConnectivityManager) paramContext.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if ((localNetworkInfo != null) && (localNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI)) {
            b = true;
        }
        return b;
    }
}
