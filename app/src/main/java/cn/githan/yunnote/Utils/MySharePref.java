package cn.githan.yunnote.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by BW on 16/9/16.
 */
public class MySharePref {

    private Context context;
    private SharedPreferences sp;
    private static SharedPreferences.Editor editor;

    public MySharePref(Context context) {
        this.context = context;
        sp = context.getSharedPreferences("note",context.MODE_PRIVATE);
        editor = sp.edit();
    }

    /**
     * put string to share preference
     * @param key string key
     * @param value string content
     */
    public static void putString(String key,String value){
        editor.putString(key,value);

    }

    /**
     * get string from share preference
     * @param key string key
     * @return string content
     */
    public String getString(String key){
        return sp.getString(key,null);
    }

    public void putBoolean(String key,boolean value){
        editor.putBoolean(key,value);

    }

    /**
     * get boolean from share preference
     * @param key string key
     * @return boolean content
     */
    public boolean getBoolean(String key){
        return sp.getBoolean(key,false);
    }

    /**
     * commit share preference
     * @return boolean
     */
    public boolean commit(){
        if (editor.commit()){
            editor.clear();
            return true;
        }
        return false;
    }

}
