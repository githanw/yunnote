package cn.githan.yunnote.Managers;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import cn.githan.yunnote.MyApplication;
import cn.githan.yunnote.Models.Note;

/**
 * Created by BW on 16/8/22.
 * 作用：管理本地note的CRUD操作
 * 注：全局变量List<Note>保存在这里
 */
public class NoteManager {

    private List<Note> noteList;
    private SQLiteManager sqLiteManager;

    public NoteManager(Context context) {
        if (noteList ==null){
            noteList = new ArrayList<>();
        }
        sqLiteManager = ((MyApplication)context).getSqLiteManager();
        refreshNoteList();
    }

    /**
     * add note
     * @param note which to be added
     */
    public void addNote(Note note){
        sqLiteManager.addNote(note);
        refreshNoteList();
    }

    /**
     * delete notes
     * switch notes which checkbox is checked & delete;
     */
    public void deleteNote(){
        sqLiteManager.deleteNote(getNoteList());
        refreshNoteList();
    }

    /**
     * modify note
     * @param note which to be modified
     */
    public void modifyNote(Note note){
        sqLiteManager.updateNote(note);
        refreshNoteList();
    }

    public List<Note> getNoteList() {
        return noteList;
    }

    public void setNoteList(List<Note> noteList) {
        this.noteList = noteList;
    }

    /**
     * refresh note list
     */
    public void refreshNoteList(){
        setNoteList(sqLiteManager.queryNote());
    }

    /**
     * restore checkbox status of notelist
     */
    public void restoreNoteCheckStatus(){
        for (int i = 0; i < noteList.size(); i++) {
            noteList.get(i).setChecked(false);
        }
    }


}
