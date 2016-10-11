package cn.githan.yunnote.Activitys;

import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

import cn.githan.yunnote.Fragments.NoteListFragment;
import cn.githan.yunnote.Fragments.SettingFragment;
import cn.githan.yunnote.R;


public class MainActivity extends BaseActivity implements TabHost.OnTabChangeListener {

    private FragmentTabHost tabHost;
    private int tabSpecTitle[] = {R.string.notes, R.string.settings};
    private int tabSpecImg[] = {R.drawable.docker_tab_note_selector, R.drawable.docker_tab_setting_selector};
    private Class fragments[] = {NoteListFragment.class, SettingFragment.class};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    /**
     * init views
     */
    public void init() {
        //initial FragmentTabHost
        tabHost = (FragmentTabHost) findViewById(R.id.tab_host);
        tabHost.setup(this, getSupportFragmentManager(), R.id.fragment_container);
        for (int i = 0; i < fragments.length; i++) {
            tabHost.addTab(tabHost.newTabSpec(getString(tabSpecTitle[i])).setIndicator(getTabSpecView(i)), fragments[i], null);
        }
        tabHost.setOnTabChangedListener(this);
        changeTabTextColor(0);
    }

    /**
     * get every view of tab
     * it is a view group contains ImageView & TextView which is from item_tab_spec.xml
     *
     * @param i view's current index
     * @return view of tab
     */
    private View getTabSpecView(int i) {
        View v = LayoutInflater.from(this).inflate(R.layout.item_tab_spec, null);
        ImageView iv = (ImageView) v.findViewById(R.id.tab_spec_imageview);
        TextView tv = (TextView) v.findViewById(R.id.tab_spec_textview);
        iv.setBackgroundResource(tabSpecImg[i]);
        tv.setText(getString(tabSpecTitle[i]));
        return v;
    }

    @Override
    public void onTabChanged(String s) {
        //get title-text of current tab and change the color to selected.
        int position = tabHost.getCurrentTab();
        changeTabTextColor(position);
    }

    /**
     * change the color of tab's title-text which is been selected.
     *
     * @param position index
     */
    public void changeTabTextColor(int position) {
        clearTabTextcolor();
        View v = tabHost.getTabWidget().getChildAt(position);
        TextView tv = (TextView) v.findViewById(R.id.tab_spec_textview);
        tv.setTextColor(getResources().getColor(R.color.colorTabTitleTextSelected));
    }

    /**
     * set all tab's title-text to default color.
     */
    public void clearTabTextcolor() {
        for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
            View v = tabHost.getTabWidget().getChildAt(i);
            TextView tv = (TextView) v.findViewById(R.id.tab_spec_textview);
            tv.setTextColor(getResources().getColor(R.color.colorTabTitleTextNormal));

        }
    }

    /**
     * set tab host visibility
     *
     * @param visibility boolean
     */
    public void setTabHostVisibility(int visibility) {
        if (tabHost != null) {
            tabHost.setVisibility(visibility);
        }
    }

}
