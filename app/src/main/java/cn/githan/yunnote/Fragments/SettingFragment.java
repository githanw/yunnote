package cn.githan.yunnote.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cn.githan.yunnote.Activitys.MainActivity;
import cn.githan.yunnote.Activitys.UnLoginActivity;
import cn.githan.yunnote.MyApplication;
import cn.githan.yunnote.R;
import cn.githan.yunnote.Widgets.AppToolBar;


/**
 * Created by BW on 16/8/13.
 */
public class SettingFragment extends Fragment implements View.OnClickListener {

    private MyApplication myApp;
    private MainActivity mainActivity;
    private AppToolBar toolbar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settting, null);
        mainActivity = (MainActivity) getActivity();
        myApp = mainActivity.getApp();
        //init custom toolbar
        initToolbar(v);
        return v;
    }

    /**
     * init tool bar
     *
     * @param v view
     */
    public void initToolbar(View v) {
        toolbar = new AppToolBar(mainActivity, (Toolbar) v.findViewById(R.id.toolbar));
        toolbar.setTitle(R.string.settings);
        toolbar.setNavigationIconAsLogin();
        toolbar.setDisplayEditorButton(false);
        toolbar.setDisplayRefreshButton(false);
        toolbar.setOnClickNavigationIconListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            //this is navigation button
            case -1:
                startActivity(new Intent(getActivity(), UnLoginActivity.class));
                break;
        }
    }

    @Override
    public void onResume() {
        toolbar.setNavigationIconAsLogin();
        super.onResume();
    }
}
