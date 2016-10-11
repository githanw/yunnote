package cn.githan.yunnote.Widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.net.Uri;
import android.text.Editable;
import android.util.AttributeSet;
import android.widget.EditText;

import cn.githan.yunnote.Managers.EditTextManager;

/**
 * Created by BW on 2016/9/27.
 * 这是一个可以插入，显示图片和视频的EditText
 */

public class SuperEditText extends EditText {
    public static final int IMAGE_SELECTOR = 1;
    public static final int IMAGE_CAPTURE = 2;
    public static final int VIDEO_CAPTURE = 3;
    private Context context;
    private EditTextManager editTextManager;

    //4.0
    ///mnt/sdcard/Android/data/cn.githan.yunnote/files/media/1691161538FFFF30985358.jpg
    //4.0++
    ///storage/emulated/0/Android/data/cn.githan.yunnote/files/media/0FFFF1017373148.jpg

    public SuperEditText(Context context) {
        super(context);
        this.context = context;
        getEditTextManager();
    }

    public SuperEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        getEditTextManager();
    }

    public SuperEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        getEditTextManager();
    }

    /**
     * get instance of EditTextManager
     *
     * @return instance of EditTextManager
     */
    public EditTextManager getEditTextManager() {
        if (editTextManager == null) {
            editTextManager = new EditTextManager(context, this);
        }
        return editTextManager;
    }

    /**
     * insert media into text
     *
     * @param uri  media uri
     * @param type type
     */
    public void insertMedia(Uri uri, int type, int noteId) {
        switch (type) {
            case IMAGE_SELECTOR:
                /*
                insert image into edittext from image selector
                 */
                editTextManager.insertFromImageSelector(this, uri, noteId);
                break;
            case IMAGE_CAPTURE:
                /*
                insert image into edittext from image capture;
                 */
                editTextManager.insertFromImageCapture(this, uri);
                break;
            case VIDEO_CAPTURE:
                /*
                insert video into edittext
                 */
                editTextManager.insertFromVideoCapture(this, uri);
                break;
        }
    }

    /**
     * update media files
     *
     * @param noteId note id
     */
    public void updateMediaFiles(int noteId) {
        editTextManager.updateMediaFiles(noteId, getText().toString());
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
    }

    @Override
    public Editable getText() {
        return super.getText();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        editTextManager.setEtWidth(getWidth());
        editTextManager.setEtHeight(getHeight());
        editTextManager.matchMediaResources(getText().toString());
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }
}
