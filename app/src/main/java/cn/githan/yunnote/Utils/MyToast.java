package cn.githan.yunnote.Utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by BW on 16/9/14.
 */
public class MyToast {

    /**
     * show toast
     *
     * @param context context
     * @param string  string of toast
     */
    public static void show(Context context, String string) {
        Toast toast = Toast.makeText(context, string, Toast.LENGTH_SHORT);
        toast.show();
    }
}
