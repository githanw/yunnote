package cn.githan.yunnote.Utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by BW on 16/9/19.
 */
public class MyJsonParser {
    /**
     * package json object
     *
     * @param type
     * @param username
     * @param password
     * @return json object
     */
    public static JSONObject packageJsonObject(String type, String username, String password) {
        JSONObject object = new JSONObject();
        try {
            object.put("requestcode", type);
            object.put("username", username);
            object.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }

    /**
     * package json object
     *
     * @param type
     * @param username
     * @return json object
     */
    public static JSONObject packageJsonObject(String type, String username) {
        JSONObject object = new JSONObject();
        try {
            object.put("requestcode", type);
            object.put("username", username);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }

    /**
     * package json object to string
     *
     * @param object json object
     * @return string
     */
    public static String jsonObjectToStr(JSONObject object) {
        String message = "msg=" + object.toString();
        return message;
    }

}
