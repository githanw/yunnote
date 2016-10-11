package cn.githan.yunnote.Activitys;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import cn.githan.yunnote.R;
import cn.githan.yunnote.Widgets.AppToolBar;

/**
 * Created by BW on 16/9/16.
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener {

    private AppToolBar toolBar;
    private TextView tv_user_name;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();
    }

    /**
     * init views
     */
    public void init() {
        toolBar = new AppToolBar(this, (Toolbar) findViewById(R.id.toolbar));
        toolBar.setNavigationIconAsBack();
        toolBar.setOnClickNavigationIconListener(this);
        tv_user_name = (TextView) findViewById(R.id.tv_user_name);
        tv_user_name.setText(getUserManager().getUsername());
        findViewById(R.id.btn_user_log_out).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_user_log_out:
                getUserManager().setUserLogOut();
                getSQLiteManager().clearQueue();
                finish();
                break;
            case -1:
                finish();
                break;
        }
    }
}
