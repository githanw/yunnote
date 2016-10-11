package cn.githan.yunnote.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

import cn.githan.yunnote.Models.Note;
import cn.githan.yunnote.R;

/**
 * Created by BW on 16/8/20.
 * list view adapter
 */
public class NoteListViewAdapter extends BaseAdapter {

    private List<Note> notelist;
    private Context context;
    private boolean editorMode = false;

    public NoteListViewAdapter(List<Note> notelist, Context context) {
        this.notelist = notelist;
        this.context = context;
        notifyDataSetChanged();
    }

    public boolean isEditorMode() {
        return editorMode;
    }

    public void setEditorMode(boolean editorMode) {
        this.editorMode = editorMode;
    }

    @Override
    public int getCount() {
        return notelist.size();
    }

    @Override
    public Note getItem(int i) {
        return notelist.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        return initView(view, i);

    }

    /**
     * base on boolean isEditorMode, return different layout of view
     *
     * @param view index of view
     * @param i    index
     * @return view
     */
    public View initView(View view, int i) {
        ViewHolder vh;
        if (isEditorMode()) {
            if (view == null) {
                view = LayoutInflater.from(context).inflate(R.layout.list_view_item_with_check_box, null);
                vh = new ViewHolder(
                        (TextView) view.findViewById(R.id.list_view_item_title),
                        (TextView) view.findViewById(R.id.list_view_item_content),
                        (TextView) view.findViewById(R.id.list_view_item_sync_status),
                        (TextView) view.findViewById(R.id.list_view_item_time),
                        (CheckBox) view.findViewById(R.id.list_view_item_checkbox));
                view.setTag(vh);
            }
            vh = (ViewHolder) view.getTag();
            vh.getTitle().setText(notelist.get(i).getnTitle());
            vh.getContent().setText(notelist.get(i).getnContent());
            vh.getSyncStatus().setText(setSyncStatus(i));
            vh.getTime().setText(notelist.get(i).getnTime());
            vh.getCheckBox().setChecked(getItem(i).isChecked());
        } else {
            if (view == null) {
                view = LayoutInflater.from(context).inflate(R.layout.list_view_item_without_check_box, null);
                vh = new ViewHolder(
                        (TextView) view.findViewById(R.id.list_view_item_title),
                        (TextView) view.findViewById(R.id.list_view_item_content),
                        (TextView) view.findViewById(R.id.list_view_item_sync_status),
                        (TextView) view.findViewById(R.id.list_view_item_time));
                view.setTag(vh);
            }
            vh = (ViewHolder) view.getTag();
            vh.getTitle().setText(notelist.get(i).getnTitle());
            vh.getContent().setText(notelist.get(i).getnContent());
            vh.getSyncStatus().setText(setSyncStatus(i));
            vh.getTime().setText(notelist.get(i).getnTime());
        }
        return view;
    }

    /**
     * if data was uploaded to server, return text "已同步"
     * otherwise, return text "未同步"
     *
     * @param i note list index
     * @return String
     */
    private String setSyncStatus(int i) {
        switch (notelist.get(i).getnSync()) {
            case 0:
                return context.getString(R.string.note_status_not_sync);
            case 1:
                return context.getString(R.string.note_status_is_sync);
        }
        return context.getString(R.string.note_status_not_sync);
    }

    class ViewHolder {
        private TextView title;
        private TextView content;
        private TextView syncStatus;
        private TextView time;
        private CheckBox checkBox;

        public ViewHolder(TextView title, TextView content, TextView syncStatus, TextView time) {
            this.title = title;
            this.content = content;
            this.syncStatus = syncStatus;
            this.time = time;
        }

        public ViewHolder(TextView title, TextView content, TextView syncStatus, TextView time, CheckBox checkBox) {
            this.title = title;
            this.content = content;
            this.syncStatus = syncStatus;
            this.time = time;
            this.checkBox = checkBox;
        }

        public CheckBox getCheckBox() {
            return checkBox;
        }

        public TextView getTitle() {
            return title;
        }

        public TextView getContent() {
            return content;
        }

        public TextView getSyncStatus() {
            return syncStatus;
        }

        public TextView getTime() {
            return time;
        }
    }

}
