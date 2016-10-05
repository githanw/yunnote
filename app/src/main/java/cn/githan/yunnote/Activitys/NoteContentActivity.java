package cn.githan.yunnote.Activitys;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import java.io.File;

import cn.githan.yunnote.Constants.Constant;
import cn.githan.yunnote.Models.Note;
import cn.githan.yunnote.R;
import cn.githan.yunnote.Utils.MyLog;
import cn.githan.yunnote.Utils.MyUtils;
import cn.githan.yunnote.Widgets.AppToolBar;
import cn.githan.yunnote.Widgets.SuperEditText;

/**
 * Created by BW on 16/8/12.
 */
public class NoteContentActivity extends BaseActivity implements View.OnClickListener, TextWatcher {

    public static final int REQUEST_CODE_ADD_NOTE = 1;
    public static final int REQUEST_CODE_EDIT_NOTE = 2;
    public static final int RESULT_CODE_FINISH = 3;
    public static final int RESULT_CODE_CANCEL = 4;

    private AppToolBar toolBar;
    private EditText etTitle;
    private SuperEditText etContent;
    private Note note;
    private ImageButton ibInsertPic, ibTakePic, ibRecordVideo;
    private Uri resultImageUri, resultVideoUri;
    private int noteId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_content);
        init();
        Intent intent = getIntent();
        note = (Note) intent.getSerializableExtra(Constant.NOTE);
        if (note != null) {
            noteId = note.getnId();
            etTitle.setText(note.getnTitle());
            etContent.setText(note.getnContent());
        } else {
            noteId = MyUtils.getRandomNumber();
        }
    }

    /**
     * init views
     */
    public void init() {
        initToolbar();
        etTitle = (EditText) findViewById(R.id.activity_note_content_et_title);
        etContent = (SuperEditText) findViewById(R.id.activity_note_content_et_content);
        ibInsertPic = (ImageButton) findViewById(R.id.image_button_insert_pic);
        ibTakePic = (ImageButton) findViewById(R.id.image_button_take_pic);
        ibRecordVideo = (ImageButton) findViewById(R.id.image_button_record_video);
        etTitle.addTextChangedListener(this);
        etContent.addTextChangedListener(this);
        ibInsertPic.setOnClickListener(this);
        ibTakePic.setOnClickListener(this);
        ibRecordVideo.setOnClickListener(this);
    }

    /**
     * init toolbar
     */
    public void initToolbar() {
        toolBar = new AppToolBar(this, (Toolbar) findViewById(R.id.toolbar));
        toolBar.setOnClickFinishButtonListener(this);
        toolBar.setNavigationIconAsBack();
        toolBar.setOnClickNavigationIconListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case -1:
                //navigation button
                setResult(NoteContentActivity.RESULT_CODE_CANCEL);
                finish();
                break;
            case R.id.menu_item_finish:
                if (note == null) {
                    note = new Note(
                            noteId,
                            etTitle.getText().toString(),
                            etContent.getText().toString(),
                            MyUtils.getSystemTime(),
                            0);
                } else {
                    note.setnTitle(etTitle.getText().toString());
                    note.setnContent(etContent.getText().toString());
                    note.setnSync(0);
                    note.setnTime(MyUtils.getSystemTime());
                }
                etContent.updateMediaFiles(noteId);
                MyLog.log("etContent : "+ etContent.getText().toString());
                Intent intent = new Intent();
                intent.putExtra(Constant.NOTE, note);
                setResult(NoteContentActivity.RESULT_CODE_FINISH, intent);
                finish();
                break;
            case R.id.image_button_insert_pic:
                /*
                insert picture
                 */
                Intent i = new Intent("android.intent.action.GET_CONTENT");
                i.setType("image/*");
                startActivityForResult(i, SuperEditText.IMAGE_SELECTOR);
                break;

            case R.id.image_button_take_pic:
                /*
                start image capture
                 */
                File outputImageFile = etContent.getEditTextManager().createNewImageFile(String.valueOf(noteId));
                resultImageUri = Uri.fromFile(outputImageFile);
                Intent intentImageCapture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intentImageCapture.putExtra(MediaStore.EXTRA_OUTPUT, resultImageUri);
                startActivityForResult(intentImageCapture, SuperEditText.IMAGE_CAPTURE);

                break;
            case R.id.image_button_record_video:
                /*
                start video capture
                 */
                Intent intentVideoCapture = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                File outputVideoFile = etContent.getEditTextManager().createNewVideoFile(String.valueOf(noteId));
                resultVideoUri = Uri.fromFile(outputVideoFile);
                intentVideoCapture.putExtra(MediaStore.EXTRA_OUTPUT, resultVideoUri);
                startActivityForResult(intentVideoCapture, SuperEditText.VIDEO_CAPTURE);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case SuperEditText.IMAGE_SELECTOR:
                    MyLog.log("requestCode = " + SuperEditText.IMAGE_SELECTOR + "; result uri = " + data.getData().toString());
                    etContent.insertMedia(data.getData(), SuperEditText.IMAGE_SELECTOR, noteId);
                    break;
                case SuperEditText.IMAGE_CAPTURE:
                    MyLog.log("requestCode = " + SuperEditText.IMAGE_CAPTURE + "; result uri = " + resultImageUri);
                    etContent.insertMedia(resultImageUri, SuperEditText.IMAGE_CAPTURE, noteId);
                    break;
                case SuperEditText.VIDEO_CAPTURE:
                    MyLog.log("requestCode = " + SuperEditText.VIDEO_CAPTURE + "; result uri = " + resultVideoUri);
                    etContent.insertMedia(resultVideoUri, SuperEditText.VIDEO_CAPTURE, noteId);
                    break;
            }
        } else {
            MyLog.log("onActivity Result failed");
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void afterTextChanged(Editable editable) {
        toolBar.setDisplayFinishButton(true);
    }

    @Override
    public void onBackPressed() {
        setResult(NoteContentActivity.RESULT_CODE_CANCEL);
        super.onBackPressed();
    }
}
