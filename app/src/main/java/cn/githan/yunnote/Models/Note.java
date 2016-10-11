package cn.githan.yunnote.Models;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by BW on 16/8/15.
 */
public class Note implements Serializable {


    private int nId; //note id
    private String nTitle; //note title
    private String nContent; //note content
    private String nTime; //note create or modify time
    private int nSync; //sync status
    private boolean isChecked = false; //checkbox status
    private String updateAction; //update action
    private List<Map> medias;

    public Note(int nId, String nTitle, String nContent, String nTime, int nSync) {
        this.nId = nId;
        this.nTitle = nTitle;
        this.nContent = nContent;
        this.nTime = nTime;
        this.nSync = nSync;
    }

    public Note(String updateAction, int nId, String nTitle, String nContent, String nTime, int nSync) {
        this.nId = nId;
        this.nTitle = nTitle;
        this.nContent = nContent;
        this.nTime = nTime;
        this.nSync = nSync;
        this.updateAction = updateAction;
    }

    public Note() {
    }

    public List<Map> getMedias() {
        return medias;
    }

    public void setMedias(List<Map> medias) {
        this.medias = medias;
    }

    public String getUpdateAction() {
        return updateAction;
    }

    public void setUpdateAction(String updateAction) {
        this.updateAction = updateAction;
    }

    public int getnId() {
        return nId;
    }

    public void setnId(int nId) {
        this.nId = nId;
    }

    public String getnTitle() {
        return nTitle;
    }

    public void setnTitle(String nTitle) {
        this.nTitle = nTitle;
    }

    public String getnContent() {
        return nContent;
    }

    public void setnContent(String nContent) {
        this.nContent = nContent;
    }

    public String getnTime() {
        return nTime;
    }

    public void setnTime(String nTime) {
        this.nTime = nTime;
    }

    public int getnSync() {
        return nSync;
    }

    public void setnSync(int nSync) {
        this.nSync = nSync;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

}
