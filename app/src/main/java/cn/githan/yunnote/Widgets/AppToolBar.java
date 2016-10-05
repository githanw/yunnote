package cn.githan.yunnote.Widgets;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import cn.githan.yunnote.MyApplication;
import cn.githan.yunnote.R;

/**
 * Created by BW on 16/8/15.
 */
public class AppToolBar {

    private MyApplication myApplication;
    private Toolbar toolbar;
    private ImageView refreshButton;
    private TextView title;
    private TextView finishButton;
    private Activity activity;
    private MenuItem editorButton;
    private int navigationIcon;

    public AppToolBar(Context context, Toolbar toolbar) {
        this.toolbar = toolbar;
        activity = (Activity) context;
        myApplication = (MyApplication) activity.getApplicationContext();
        refreshButton = (ImageView) toolbar.findViewById(R.id.menu_item_refresh);
        title = (TextView) toolbar.findViewById(R.id.menu_item_title);
        finishButton = (TextView) toolbar.findViewById(R.id.menu_item_finish);
        toolbar.inflateMenu(R.menu.menu);
        editorButton = toolbar.getMenu().findItem(R.id.menu_item_lv_editor);
        setDisplayEditorButton(false);
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    public void setNavigationIcon(int resId) {
        toolbar.setNavigationIcon(resId);
        navigationIcon = resId;
    }

    /**
     * set navigation on click listener
     *
     * @param listener on click listener
     */
    public void setOnClickNavigationIconListener(View.OnClickListener listener) {
        toolbar.setNavigationOnClickListener(listener);
    }

    /**
     * set toolbar title
     * @param str string of title
     */
    public void setTitle(String str) {
        title.setText(str);
    }

    /**
     * set toolbar title
     * @param resId resId of title
     */
    public void setTitle(int resId) {
        title.setText(activity.getResources().getString(resId));
    }

    /**
     * get toolbar title text
     * @return string of title
     */
    public String getTitle() {
        return title.getText().toString();
    }

    /**
     * display refresh button or not
     * @param b boolean
     */
    public void setDisplayRefreshButton(boolean b) {
        if (b) {
            refreshButton.setVisibility(View.VISIBLE);
        } else {
            refreshButton.setVisibility(View.GONE);
        }
    }

    /**
     * set refresh button on click listener
     * @param listener view.onclicklistener
     */
    public void setOnClickRefreshButtonListener(View.OnClickListener listener) {
        refreshButton.setOnClickListener(listener);

    }

    /**
     * set editor button display or not
     * @param b boolean
     */
    public void setDisplayEditorButton(boolean b) {
        if (editorButton != null) {
            if (b) {
                    editorButton.setVisible(true);
            }else {
                    editorButton.setVisible(false);
            }
        }
    }

    /**
     * set editor button on click listener
     * @param listener view.onclicklistener
     */
    public void setOnClickEditorButtonListener(Toolbar.OnMenuItemClickListener listener) {
        toolbar.setOnMenuItemClickListener(listener);
    }

    /**
     * set navigation icon as back icon
     */
    public void setNavigationIconAsBack() {
        setNavigationIcon(R.drawable.ic_menu_item_nav_back);
    }

    /**
     * get navigation icon resId
     * @return icon resId
     */
    public int getNavigationIcon() {
        return navigationIcon;
    }

    /**
     * set navigation icon as login icon
     */
    public void setNavigationIconAsLogin() {
        if (myApplication.getUserManager().isUserLogin()) {
            setNavigationIcon(R.drawable.ic_menu_item_nav_login);
        } else {
            setNavigationIcon(R.drawable.ic_menu_item_nav_unlogin);
        }
    }

    /**
     * set navigationi icon as trash icon
     */
    public void setNavigationIconAsTrash() {
        setNavigationIcon(R.drawable.ic_menu_item_nav_delete);
    }

    /**
     * display finish button or not
     * @param b
     */
    public void setDisplayFinishButton(boolean b) {
        if (b) {
            finishButton.setVisibility(View.VISIBLE);
        } else {
            finishButton.setVisibility(View.GONE);
        }
    }

    /**
     * set finish button onclick listener
     * @param listener view.onclicklistener
     */
    public void setOnClickFinishButtonListener(View.OnClickListener listener) {
        finishButton.setOnClickListener(listener);
    }

}
