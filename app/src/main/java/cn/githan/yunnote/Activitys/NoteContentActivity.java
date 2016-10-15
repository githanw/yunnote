package cn.githan.yunnote.Activitys;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import java.io.File;

import cn.githan.yunnote.Constants.Constant;
import cn.githan.yunnote.Managers.EditTextManager;
import cn.githan.yunnote.Models.Note;
import cn.githan.yunnote.R;
import cn.githan.yunnote.Utils.MyLog;
import cn.githan.yunnote.Utils.MyToast;
import cn.githan.yunnote.Utils.MyUtils;
import cn.githan.yunnote.Widgets.AppToolBar;
import cn.githan.yunnote.Widgets.SuperEditText;

/**
 * Created by BW on 16/8/12.
 */
public class NoteContentActivity extends BaseActivity implements View.OnClickListener, TextWatcher, EditTextManager.OnMediaTouchListener {

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
    private Button editBtn;


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
            setEditMode(false);
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
        etContent.getEditTextManager().setOnMediaTouchListener(this);
    }

    /**
     * init toolbar
     */
    public void initToolbar() {
        toolBar = new AppToolBar(this, (Toolbar) findViewById(R.id.toolbar));
        toolBar.setOnClickFinishButtonListener(this);
        toolBar.setNavigationIconAsBack();
        toolBar.setOnClickNavigationIconListener(this);
        editBtn = (Button) findViewById(R.id.menu_item_finish);
    }

    public void setEditMode(boolean b) {
        if (b) {
            editBtn.setText(getString(R.string.menu_item_finish));
            etTitle.setEnabled(true);
            etContent.setFocusableInTouchMode(true);
            etContent.setFocusable(true);
            findViewById(R.id.layout_media_button).setVisibility(View.VISIBLE);
        } else {
            editBtn.setText(getString(R.string.menu_item_edit));
            etTitle.setEnabled(false);
            etContent.setFocusable(false);
            etContent.setFocusableInTouchMode(false);
            findViewById(R.id.layout_media_button).setVisibility(View.GONE);
        }
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
                if (editBtn.getText().toString().equals(getString(R.string.menu_item_finish))) {
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
                    MyLog.log("etContent : " + etContent.getText().toString());
                    Intent intent = new Intent();
                    intent.putExtra(Constant.NOTE, note);
                    setResult(NoteContentActivity.RESULT_CODE_FINISH, intent);
                    finish();
                } else if (editBtn.getText().toString().equals(getString(R.string.menu_item_edit))) {
                    setEditMode(true);
                }

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
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                    MyToast.show(this, "请使用android 5.0 及以下的系统测试此功能");
                    return;
                }
                Intent intentVideoCapture = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                File outputVideoFile = etContent.getEditTextManager().createNewVideoFile(String.valueOf(noteId));
                resultVideoUri = Uri.fromFile(outputVideoFile);
                intentVideoCapture.putExtra(MediaStore.EXTRA_OUTPUT, resultVideoUri);
                startActivityForResult(intentVideoCapture, SuperEditText.VIDEO_CAPTURE);
                break;
        }
    }

    /**
     * Gets the content:// URI from the given corresponding path to a file
     *
     * @param context
     * @param imageFile
     * @return content Uri
     */
    public static Uri getVideoContentUri(Context context, java.io.File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Video.Media._ID}, MediaStore.Video.Media.DATA + "=? ",
                new String[]{filePath}, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/video/media/");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
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

    @Override
    public void onMediaTouch(Uri uri) {
        String type;
        String str = uri.toString();
        if (str.endsWith(".mp4")) {
            type = "video/mp4";
        } else {
            type = "image/*";
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, type);
        startActivity(intent);
    }


}
