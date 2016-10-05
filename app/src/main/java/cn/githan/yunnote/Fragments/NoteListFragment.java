package cn.githan.yunnote.Fragments;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ListView;

import cn.githan.yunnote.Utils.MyLog;
import cn.githan.yunnote.Utils.MyUtils;
import cn.githan.yunnote.Widgets.AppToolBar;
import cn.githan.yunnote.Activitys.LoginActivity;
import cn.githan.yunnote.Activitys.MainActivity;
import cn.githan.yunnote.Activitys.NoteContentActivity;
import cn.githan.yunnote.Activitys.UnLoginActivity;
import cn.githan.yunnote.MyApplication;
import cn.githan.yunnote.Adapters.NoteListViewAdapter;
import cn.githan.yunnote.Managers.NoteManager;
import cn.githan.yunnote.Managers.NoteSyncManager;
import cn.githan.yunnote.Constants.Constant;
import cn.githan.yunnote.Models.Note;
import cn.githan.yunnote.R;
import cn.githan.yunnote.Utils.MyToast;

import static cn.githan.yunnote.Managers.NoteSyncManager.SYNC_SUCCEED;


/**
 * Created by BW on 16/8/13.
 */
public class NoteListFragment extends Fragment implements Toolbar.OnMenuItemClickListener, View.OnClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, NoteSyncManager.OnSyncSucceedListener {


    private AppToolBar toolBar;
    private MyApplication myApp;
    private MainActivity mainActivity;
    private RotateAnim ra;
    private FloatingActionButton fab;
    private ListView listView;
    private NoteListViewAdapter adapter;
    private NoteManager noteManager;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //get MainActivity instance
        mainActivity = (MainActivity) getActivity();
        //get MyApplication instance
        myApp = mainActivity.getApp();
        //get NoteManager instance
        noteManager = myApp.getNoteManager();
        //inflate view
        View v = inflater.inflate(R.layout.fragment_note_list, null);
        //init custom toolbar
        initToolbar(v);
        //init floating action button
        fab = (FloatingActionButton) v.findViewById(R.id.fab);
        fab.setOnClickListener(this);
        //init list view
        listView = (ListView) v.findViewById(R.id.list_view);
        adapter = new NoteListViewAdapter(noteManager.getNoteList(), getActivity());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);
        ra = new RotateAnim();
        return v;

    }

    /**
     * init toolbar
     *
     * @param v view
     */
    private void initToolbar(View v) {
        toolBar = new AppToolBar(mainActivity, (Toolbar) v.findViewById(R.id.toolbar));
        toolBar.setTitle(R.string.app_name);
        toolBar.setNavigationIconAsLogin();
        toolBar.setDisplayRefreshButton(true);
        toolBar.setDisplayEditorButton(true);
        toolBar.setOnClickNavigationIconListener(this);
        toolBar.setOnClickRefreshButtonListener(this);
        toolBar.setOnClickEditorButtonListener(this);
        toolBar.setOnClickFinishButtonListener(this);
    }

    @Override
    public void onResume() {
        if (toolBar != null) {
            toolBar.setNavigationIconAsLogin();
        }
        super.onResume();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_lv_editor:
                //change layout to list view editor mode
                setEditorLayout(true);
                break;
        }
        return false;
    }

    private void setEditorLayout(boolean b) {
        if (b == true) {
            //reset list view to editor mode, which layout has a checkbox
            adapter.setEditorMode(true);
            listView.setAdapter(adapter);
            //change layout style
            ((MainActivity) getActivity()).setTabHostVisibility(View.GONE);
            toolBar.setDisplayEditorButton(false);
            fab.setVisibility(View.GONE);
            toolBar.setDisplayRefreshButton(false);
            toolBar.setDisplayFinishButton(true);
            toolBar.setNavigationIconAsTrash();
        } else {
            //clear all checkbox-item status
            noteManager.restoreNoteCheckStatus();
            //reset list view to normal mode

            adapter.setEditorMode(false);
            listView.setAdapter(adapter);
            //change layout style
            ((MainActivity) getActivity()).setTabHostVisibility(View.VISIBLE);
            toolBar.setDisplayEditorButton(true);
            fab.setVisibility(View.VISIBLE);
            toolBar.setDisplayRefreshButton(true);
            toolBar.setDisplayFinishButton(false);
            toolBar.setNavigationIconAsLogin();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.menu_item_refresh:
                if (MyUtils.isNetworkAvaliable(getContext())) {
                    if (myApp.getUserManager().isUserLogin()) {
                        ra.bindView(view);
                        ra.start();
                        //start sync data
                        myApp.getNoteSyncManager().startSyncData(myApp.getUsername(), this);
                    } else {
                        startUnLoginActivity();
                    }
                } else {
                    MyToast.show(getContext(), getString(R.string.toast_info_network_unavaliable));
                }

                // TODO: 16/8/13 check isLogin, if not, go to

                break;
            //this is navigation button
            case -1:
                switch (toolBar.getNavigationIcon()) {
                    case R.drawable.ic_menu_item_nav_delete:
                        //deleteNote list view item which checkbox is checked
                        showAlertDialog();
                        break;
                    case R.drawable.ic_menu_item_nav_login:
                        startLoginActivity();
                        break;
                    case R.drawable.ic_menu_item_nav_unlogin:
                        startUnLoginActivity();
                        break;
                }
                break;
            case R.id.menu_item_finish:
                setEditorLayout(false);
                break;
            case R.id.fab:
                Intent intent = new Intent(getActivity(), NoteContentActivity.class);
                startActivityForResult(intent, NoteContentActivity.REQUEST_CODE_ADD_NOTE);
                break;
        }
    }

    /**
     * start UnLoginActivity
     */
    private void startUnLoginActivity() {
        startActivity(new Intent(getActivity(), UnLoginActivity.class));
    }

    /**
     * start LoginActivity
     */
    private void startLoginActivity() {
        startActivity(new Intent(getActivity(), LoginActivity.class));
    }

    /**
     * show dialog
     */
    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setMessage(R.string.dialog_message)
                .setPositiveButton(R.string.dialog_sure, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        noteManager.deleteNote();
                        setEditorLayout(false);
                        adapter.notifyDataSetChanged();
                    }
                })
                .setTitle(R.string.dialog_tip)
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        builder.show();
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (adapter.isEditorMode()) {
            adapter.getItem(i).setChecked(!adapter.getItem(i).isChecked());
            adapter.notifyDataSetChanged();
        } else {
            //start note content activity for result
            Intent intent = new Intent(getActivity(), NoteContentActivity.class);
            intent.putExtra(Constant.NOTE, noteManager.getNoteList().get(i));
            startActivityForResult(intent, NoteContentActivity.REQUEST_CODE_EDIT_NOTE);
        }
    }


    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (adapter.isEditorMode()) {
            // TODO: 16/8/20 do nothing now, plan to show note menu dialog
        } else {
            setEditorLayout(true);
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == NoteContentActivity.RESULT_CODE_CANCEL) {
            return;
        }
        switch (requestCode) {
            case NoteContentActivity.REQUEST_CODE_ADD_NOTE:
                //add a new note
                noteManager.addNote((Note) data.getSerializableExtra(Constant.NOTE));
                adapter.notifyDataSetChanged();
                break;
            case NoteContentActivity.REQUEST_CODE_EDIT_NOTE:
                //modify note
                noteManager.modifyNote((Note) data.getSerializableExtra(Constant.NOTE));
                adapter.notifyDataSetChanged();
                break;
        }
    }

    @Override
    public void onPause() {
        ra.stop();
        super.onPause();
    }

    @Override
    public void onNoteSyncSucceed(String data) {
        if (data.equals(SYNC_SUCCEED)) {
            MyLog.log("onNoteSyncSucceed. data = " + data);
            Message m = new Message();
            m.what = 1;
            handler.sendMessage(m);
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                MyToast.show(getContext(), getContext().getString(R.string.info_sync_succeed));
                myApp.getNoteManager().refreshNoteList();
                adapter.notifyDataSetChanged();
                ra.stop();
            }
            super.handleMessage(msg);
        }
    };

    /**
     * Rotate Animation
     */
    public static class RotateAnim {

        private View view;
        private Animation a;
        private LinearInterpolator lir;
        private boolean isAnimating = false;

        public RotateAnim() {
            lir = new LinearInterpolator();
            a = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            a.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    isAnimating = true;
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    isAnimating = false;
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }

        /**
         * bind view
         *
         * @param view view
         */
        public void bindView(View view) {
            this.view = view;
        }

        /**
         * start animation
         */
        public void start() {
            if (!isAnimating) {
                a.setDuration(2000);
                a.setInterpolator(lir);
                a.setRepeatMode(Animation.RESTART);
                a.setRepeatCount(Animation.INFINITE);
                if (view != null) {
                    view.startAnimation(a);
                }
            }
        }

        /**
         * stop animation
         */
        public void stop() {
            if (isAnimating) {
                a.cancel();
            }
        }

    }
}
