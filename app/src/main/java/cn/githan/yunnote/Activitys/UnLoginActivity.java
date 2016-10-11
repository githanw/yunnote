package cn.githan.yunnote.Activitys;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import cn.githan.yunnote.Constants.Constant;
import cn.githan.yunnote.Managers.UserManager;
import cn.githan.yunnote.R;
import cn.githan.yunnote.Utils.MyLog;
import cn.githan.yunnote.Utils.MyToast;
import cn.githan.yunnote.Utils.MyUtils;
import cn.githan.yunnote.Widgets.AppToolBar;

/**
 * Created by BW on 16/8/12.
 */
public class UnLoginActivity extends BaseActivity implements View.OnClickListener, UserManager.JsonDataReceivedListener {

    private Button btnUserLogin;
    private Button btnUserRegister;
    private EditText etUserName, etUserPasswd;
    private AppToolBar toolBar;
    private ShakeAnim sa;
    private AlertDialog dialog;
    private TextView tvLoginInfo;
    private CheckBox cbSavePasswd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();

        /*
        check network
         */
        if (!MyUtils.isNetworkAvaliable(this)) {
            MyToast.show(this, getString(R.string.toast_info_network_unavaliable));
        }
    }

    /**
     * init views
     */
    private void init() {
        setContentView(R.layout.activity_un_login);
        toolBar = new AppToolBar(this, (Toolbar) findViewById(R.id.toolbar));
        toolBar.setTitle(R.string.please_login);
        etUserName = (EditText) findViewById(R.id.et_login_name);
        etUserPasswd = (EditText) findViewById(R.id.et_login_pwd);
        btnUserLogin = (Button) findViewById(R.id.btn_user_login);
        tvLoginInfo = (TextView) findViewById(R.id.tv_login_info);
        cbSavePasswd = (CheckBox) findViewById(R.id.cb_save_passwd);
        btnUserRegister = (Button) findViewById(R.id.btn_user_register);
        btnUserLogin.setOnClickListener(this);
        btnUserRegister.setOnClickListener(this);
        sa = new ShakeAnim();
        sa.setDuration(500);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle b = msg.getData();
            String resultData = b.getString("message");
            try {
                JSONObject object = new JSONObject(resultData);
                MyLog.log(object.toString());
                String message = object.getString("message");
                String resultcode = object.getString("resultcode");
                String username = object.getString("username");
                String password = object.getString("password");

                if (resultcode.equals(Constant.RESULT_FAILED)) {
                    //failed to login
                    MyToast.show(UnLoginActivity.this, message);
                } else if (resultcode.equals(Constant.RESULT_SUCCEED)) {
                    MyToast.show(UnLoginActivity.this, message);
                    //log in succeed, save username
                    getUserManager().setUserLoginInfo(username, password, cbSavePasswd.isChecked());
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            super.handleMessage(msg);
        }
    };


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_user_login:
                if (checkTextFormat()) {
                    if (MyUtils.isNetworkAvaliable(this)) {
                        tvLoginInfo.setVisibility(View.GONE);
                        //start connecting network to login
                        showDialog(getString(R.string.dialog_message_login));
                        getUserManager().startConnectServer(
                                Constant.LOGIN_REQUEST,
                                etUserName.getText().toString(),
                                etUserPasswd.getText().toString(),
                                this);
                    } else {
                        MyToast.show(this, getString(R.string.toast_info_network_unavaliable));
                    }
                } else {
                    btnUserLogin.startAnimation(sa);
                }
                break;
            case R.id.btn_user_register:
                if (checkTextFormat()) {
                    if (MyUtils.isNetworkAvaliable(this)) {
                        tvLoginInfo.setVisibility(View.GONE);
                        // start connecting network to register
                        showDialog(getString(R.string.dialog_message_register));
                        getUserManager().startConnectServer(
                                Constant.REGSITER_REQUEST,
                                etUserName.getText().toString(),
                                etUserPasswd.getText().toString(),
                                this);
                    } else {
                        MyToast.show(this, getString(R.string.toast_info_network_unavaliable));
                    }
                } else {
                    btnUserRegister.startAnimation(sa);
                }
                break;
        }
    }

    /**
     * show dialog
     *
     * @param message string of dialog message
     */
    private void showDialog(String message) {
        dialog = new AlertDialog.Builder(this)
                .setMessage(message)
                .setCancelable(false)
                .show();
    }

    /**
     * check text format of username & password,
     * basically not null
     *
     * @return boolean
     */
    private boolean checkTextFormat() {
        if (etUserName.getText().toString().equals("")) {
            tvLoginInfo.setText("用户名不能为空");
            tvLoginInfo.setVisibility(View.VISIBLE);
            return false;
        } else if (etUserPasswd.getText().toString().equals("")) {
            tvLoginInfo.setText("密码不能为空");
            tvLoginInfo.setVisibility(View.VISIBLE);
            return false;
        }
        return true;
    }

    @Override
    public void onReceived(String data) {
        MyLog.log("Login message onReceived: " + data);
        Message msg = new Message();
        Bundle b = new Bundle();
        b.putString("message", data);
        msg.setData(b);
        handler.sendMessage(msg);
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    /**
     * Shake animation
     */
    public static class ShakeAnim extends Animation {

        @Override
        public void initialize(int width, int height, int parentWidth, int parentHeight) {
            super.initialize(width, height, parentWidth, parentHeight);
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            t.getMatrix().setTranslate((float) (Math.sin(interpolatedTime * 10) * 10), 0);
            super.applyTransformation(interpolatedTime, t);
        }

    }
}
