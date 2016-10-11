package cn.githan.yunnote.Controllers;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by BW on 16/9/15.
 * 连接网络传送数据的异步任务，
 * host地址及请求数据的字符串由使用者提供
 * 接口NetWorkResultListener 监听服务器传回数据
 */
public class NetWorkAsyncTask extends AsyncTask<String, Void, String> {

    private NetWorkResultListener listener;

    public NetWorkAsyncTask(NetWorkResultListener listener) {
        this.listener = listener;
    }

    @Override
    protected String doInBackground(String... strings) {

        try {
            URL url = new URL(strings[0]);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            //write
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream(), "utf-8");
            BufferedWriter bw = new BufferedWriter(osw);
            bw.write(strings[1]);
            bw.flush();
            bw.close();
            osw.close();

            //read
            InputStreamReader isr = new InputStreamReader(connection.getInputStream(), "utf-8");
            BufferedReader br = new BufferedReader(isr);
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            isr.close();
            return sb.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        listener.onResultData(s);
        super.onPostExecute(s);

    }

    public interface NetWorkResultListener {
        void onResultData(String data);
    }
}
