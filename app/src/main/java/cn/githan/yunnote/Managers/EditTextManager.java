package cn.githan.yunnote.Managers;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.view.WindowManager;
import android.widget.EditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.githan.yunnote.Controllers.MyLinkMovementMethod;
import cn.githan.yunnote.R;
import cn.githan.yunnote.Utils.MyLog;
import cn.githan.yunnote.Utils.MyToast;
import cn.githan.yunnote.Utils.MyUtils;
import cn.githan.yunnote.Widgets.MyImageSpan;
import cn.githan.yunnote.Widgets.SuperEditText;

import static cn.githan.yunnote.Widgets.SuperEditText.IMAGE_CAPTURE;
import static cn.githan.yunnote.Widgets.SuperEditText.IMAGE_SELECTOR;
import static cn.githan.yunnote.Widgets.SuperEditText.VIDEO_CAPTURE;

/**
 * Created by BW on 2016/9/28.
 */

public class EditTextManager {

    private Context context;
    private String mediaRootPath;
    private static final String FILE_TAG = "FFFF";
    private EditText editText;
    private int etWidth, etHeight;
    private OnMediaTouchListener onMediaTouchListener;

    /*
    media文件格式：
    noteIdFFFFrandomId.jpg
    noteIdFFFFrandomId.mp4
     */

    public EditTextManager(Context context, EditText editText) {
        this.context = context;
        this.editText = editText;
        /*
        get media root path
         */
        mediaRootPath = context.getExternalFilesDir("media").toString();
    }

    /**
     * 将Uri解析成文件路径
     *
     * @param uri
     * @param type
     * @return
     */
    public String decodeUriToFilePath(Uri uri, int type) {
        String filePath;
        switch (type) {
            case IMAGE_SELECTOR:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    if (DocumentsContract.isDocumentUri(context, uri)) {
                        String docId = DocumentsContract.getDocumentId(uri);
                        if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                            String id = docId.split(":")[1];
                            String selection = MediaStore.Images.Media._ID + "=" + id;
                            filePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
                            MyLog.log("media path = " + filePath);
                            return filePath;
                        } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                            Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                            filePath = getImagePath(contentUri, null);
                            MyLog.log("media path = " + filePath);
                            return filePath;
                        }
                    } else if ("content".equalsIgnoreCase(uri.getScheme())) {
                        filePath = getImagePath(uri, null);
                        MyLog.log("media path = " + filePath);
                        return filePath;
                    }
                } else {
                    filePath = getImagePath(uri, null);
                    MyLog.log("query result = " + filePath);
                    MyLog.log("media path = " + filePath);
                    return filePath;
                }
                break;
            case IMAGE_CAPTURE:
                String s = uri.toString();
                filePath = s.substring(7, s.length());
                MyLog.log("media path = " + filePath.toString());
                return filePath;

            case VIDEO_CAPTURE:
                String str = uri.toString();
                filePath = str.substring(7, str.length());
                MyLog.log("media path = " + filePath);
                return filePath;
        }
        filePath = uri.toString();
        return filePath;
    }

    /**
     * 从文件路径中获取Bitmap
     *
     * @param filePath bitmap file path
     * @param type     flag
     * @return bitmap
     */
    public Bitmap getBitmapFromFilePath(String filePath, int type) {
        Bitmap bitmap = null;
        switch (type) {
            case SuperEditText.IMAGE_SELECTOR:
            case SuperEditText.IMAGE_CAPTURE:
                bitmap = BitmapFactory.decodeFile(filePath);
                break;
            case SuperEditText.VIDEO_CAPTURE:
                WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

                int videoThumbnailWidth = wm.getDefaultDisplay().getWidth() - 60;
                int videoThumbnailHeight = wm.getDefaultDisplay().getHeight();
                int kind = MediaStore.Images.Thumbnails.MINI_KIND;
                bitmap = getVideoThumbnail(filePath, videoThumbnailWidth, videoThumbnailHeight, kind);
                break;
        }
        return bitmap;

    }


    /**
     * 获取视频的缩略图
     * 先通过ThumbnailUtils来创建一个视频的缩略图，然后再利用ThumbnailUtils来生成指定大小的缩略图。
     * 如果想要的缩略图的宽和高都小于MICRO_KIND，则类型要使用MICRO_KIND作为kind的值，这样会节省内存。
     *
     * @param videoPath 视频的路径
     * @param width     指定输出视频缩略图的宽度
     * @param height    指定输出视频缩略图的高度度
     * @param kind      参照MediaStore.Images.Thumbnails类中的常量MINI_KIND和MICRO_KIND。
     *                  其中，MINI_KIND: 512 x 384，MICRO_KIND: 96 x 96
     * @return 指定大小的视频缩略图
     */
    public Bitmap getVideoThumbnail(String videoPath, int width, int height, int kind) {
        Bitmap bitmap;
        bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
        MyLog.log("Video Thumbnail is created: w->" + bitmap.getWidth() + " h->" + bitmap.getHeight());

        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, bitmap.getHeight(), ThumbnailUtils.OPTIONS_RECYCLE_INPUT);

        //加上播放的水印
        BitmapDrawable bd = (BitmapDrawable) context.getResources().getDrawable(R.drawable.water_mark_play);
        Bitmap waterBitmap = bd.getBitmap();
        bitmap = createBitmap(bitmap, waterBitmap);
        return bitmap;
    }

    /**
     * create bitmap which has water_mark
     *
     * @param src       bitmap resource
     * @param watermark watermark
     * @return bitmap with watermark
     */
    public Bitmap createBitmap(Bitmap src, Bitmap watermark) {
        if (src == null) {
            return null;
        }
        int w = src.getWidth();
        int h = src.getHeight();
        int ww = watermark.getWidth();
        int wh = watermark.getHeight();
        //create the new blank bitmap
        Bitmap newb = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(newb);
        //draw src into
        cv.drawBitmap(src, 0, 0, null);
        //draw watermark into
        cv.drawBitmap(watermark, (w - ww) / 2, (h - wh) / 2, null);
        //save all clip
        cv.save(Canvas.ALL_SAVE_FLAG);
        //store
        cv.restore();
        return newb;
    }

    /**
     * get image file path through ContentResolver
     *
     * @param uri
     * @param selection
     * @return
     */
    public String getImagePath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = context.getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    /**
     * resize Bitmap
     *
     * @param b bitmap
     * @return new bitmap
     */
    private Bitmap resizeBitmap(Bitmap b) {
         /*
        resize image's size;
        */
        int width = getEtWidth();
        int height = b.getHeight();
        if (height > getEtHeight()) {
            height = getEtHeight();
        }

        b = ThumbnailUtils.extractThumbnail(b, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        MyLog.log("Bitmap resize : w->" + b.getWidth() + " h->" + b.getHeight());
        return b;
    }

    /**
     * create new Image file
     *
     * @param noteId this file belongs to a note
     * @return file
     */
    public File createNewImageFile(String noteId) {
        File imageFile = new File(mediaRootPath, noteId + FILE_TAG + MyUtils.getRandomNumber() + ".jpg");
        return imageFile;
    }

    /**
     * create new video file
     *
     * @param noteId this file belongs to a note
     * @return file
     */
    public File createNewVideoFile(String noteId) {
        File videoFile = new File(mediaRootPath, noteId + FILE_TAG + MyUtils.getRandomNumber() + ".mp4");
        return videoFile;
    }


    /**
     * insert Image
     *
     * @param uri image uri
     */
    public void insertFromImageSelector(EditText et, Uri uri, int noteId) {
        /*
        1，从uri中解析出bitmap
        2，然后将bitmap输出到app制定目录
        3，从制定目录重新解析出bitmap；
         */
        String path = decodeUriToFilePath(uri, IMAGE_SELECTOR);
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
        File newImagePath = createNewImageFile(String.valueOf(noteId));
        try {
            if (!newImagePath.exists()) {
                newImagePath.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(newImagePath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        MyLog.log("newImagePath = " + newImagePath.toString());
        Bitmap newBitmap = getBitmapFromFilePath(newImagePath.toString(), IMAGE_SELECTOR);
        insertImageToEditText(et, newBitmap, Uri.parse(newImagePath.toString()));

    }


    /**
     * 插入拍摄的图片
     *
     * @param uri
     */
    public void insertFromImageCapture(EditText et, Uri uri) {
        String path = decodeUriToFilePath(uri, IMAGE_CAPTURE);
        Bitmap bitmap = getBitmapFromFilePath(path, IMAGE_CAPTURE);
        insertImageToEditText(et, bitmap, Uri.parse(path));
    }

    /**
     * 插入拍摄的video
     *
     * @param uri
     */
    public void insertFromVideoCapture(EditText et, Uri uri) {
        String path = decodeUriToFilePath(uri, VIDEO_CAPTURE);
        Bitmap bitmap = getBitmapFromFilePath(path, VIDEO_CAPTURE);
        insertImageToEditText(et, bitmap, Uri.parse(path));
    }

    /**
     * insert Bitmap into EditText
     *
     * @param et     EditText
     * @param bitmap bitmap resource
     * @param uri    bitmap uri
     */
    public void insertImageToEditText(EditText et, Bitmap bitmap, Uri uri) {
        /*
        resize Bitmap
         */
        bitmap = resizeBitmap(bitmap);
        /*
        insert image into EditText
         */
        SpannableString ss = new SpannableString(uri.toString());
        MyImageSpan is = new MyImageSpan(context, bitmap, uri);
        ss.setSpan(is, 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        Editable editable = et.getEditableText();
        int index = et.getSelectionStart();
        editable.insert(index, ss);
        editable.insert(index + ss.length(), "\n");
        et.setMovementMethod(MyLinkMovementMethod.getInstance(handler, MyImageSpan.class));
    }

    /*
    make links and image work
     */
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            int what = msg.what;
            if (what == 200) {
                MyLinkMovementMethod.MessageSpan ms = (MyLinkMovementMethod.MessageSpan) msg.obj;
                Object[] spans = (Object[]) ms.getObj();
                for (Object span : spans) {
                    if (span instanceof MyImageSpan) {
                        MyLog.log("点击了图片：" + ((MyImageSpan) span).getUri());
                        String filePath = String.valueOf(((MyImageSpan) span).getUri());
                        File file = new File(filePath);
                        Uri uri = Uri.fromFile(file);
                        onMediaTouchListener.onMediaTouch(uri);
                        MyLog.log("uri = " + uri.toString());
                    }
                }
            }
        }
    };

    /**
     * 从传入的内容中解析出图像／视频文件
     *
     * @param str note content
     * @return resource Strings
     */
    public List<String> matchMediaResources(String str) {
        List<String> resourceStrs = new ArrayList<>();
        String regex = "/(storage|mnt)/.{0,15}/Android/data/cn\\.githan\\.yunnote/files/media/.{0,25}\\.(jpg|mp4)";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(str);
        while (m.find()) {
            String resourcePath = m.group();
            resourceStrs.add(resourcePath);
            MyLog.log("matcher str : " + resourcePath + " ; start = " + m.start() + "; end = " + m.end());
            if (checkFileExist(resourcePath)) {
                displayResource(resourcePath, m.start(), m.end());
            } else {
                MyLog.log("resource show failed : " + resourcePath);
                MyToast.show(context, context.getString(R.string.info_show_media_failed));
            }
        }
        return resourceStrs;
    }

    /**
     * check if file existed
     *
     * @param filePath file path
     * @return boolean
     */
    public boolean checkFileExist(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            return true;
        }
        return false;
    }

    /**
     * 处理editText的内容，使显示图片／视频
     *
     * @param resource 需要替换为SpannableString的str
     * @param start    start index
     * @param end      end index
     */
    public void displayResource(String resource, int start, int end) {
        SpannableString ss = new SpannableString(resource);
        Editable editable = editText.getEditableText();
        Bitmap bitmap = null;
        if (resource.endsWith(".jpg")) {
            bitmap = BitmapFactory.decodeFile(resource);
        } else if (resource.endsWith(".mp4")) {
            bitmap = getBitmapFromFilePath(resource, VIDEO_CAPTURE);
        }
        if (bitmap != null) {
            bitmap = resizeBitmap(bitmap);
            MyImageSpan mis = new MyImageSpan(context, bitmap, Uri.parse(resource));
            ss.setSpan(mis, 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            editable.replace(start, end, ss);
            editText.setMovementMethod(MyLinkMovementMethod.getInstance(handler, MyImageSpan.class));
        } else {
            editable.replace(start, end, "");
            MyLog.log("can't show image");
        }
    }

    /**
     * 返回media文件夹目录下该note的所有media资源文件
     *
     * @param noteId note id
     * @return files of medias
     */
    public List<File> getMediaFiles(int noteId) {
        MyLog.log("get Media Files from note : " + noteId);
        File mediaRoot = new File(mediaRootPath);
        File[] files = mediaRoot.listFiles();
        List<File> fileList = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            String fileName = files[i].getName();
            System.out.println(files[i].getName());
            if (fileName.startsWith(String.valueOf(noteId))) {
                fileList.add(files[i]);
                MyLog.log("matched file name = " + files[i].getName());
            }
        }
        return fileList;
    }

    /**
     * 对比检查note content & media文件夹目录下的资源，删除多余的media文件
     *
     * @param noteId  note id
     * @param content note content
     */
    public void updateMediaFiles(int noteId, String content) {
        //1,获取media文件夹里的所有文件名
        //2,获取edittext 里的内容，找到media的uri?定义一个List？
        //3,遍历对比两者，找出编辑过程中被删除的media
        //4,删除对应的文件
        List<File> mediaFiles = getMediaFiles(noteId);
        List<String> uris = getMatchResources(content);
        List<File> toRemoveFiles = new ArrayList<>();
        for (int i = 0; i < mediaFiles.size(); i++) {
            File file = mediaFiles.get(i);
            if (!contains(file, uris)) {
                toRemoveFiles.add(file);
            }
        }
        deleteMediaFiles(toRemoveFiles);
    }

    /**
     * 从传入的内容中解析出media文件列表
     *
     * @param str String
     * @return List<String>
     */
    public List<String> getMatchResources(String str) {
        List<String> resourceStrs = new ArrayList<>();
        String regex = "/(storage|mnt)/.{0,15}/Android/data/cn\\.githan\\.yunnote/files/media/.{0,25}\\.(jpg|mp4)";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(str);
        while (m.find()) {
            String resourcePath = m.group();
            resourceStrs.add(resourcePath);
            MyLog.log("resource path : " + resourcePath);
        }
        return resourceStrs;
    }

    /**
     * 删除media文件
     *
     * @param mediaFiles list of media files
     */
    private void deleteMediaFiles(List<File> mediaFiles) {
        for (int i = 0; i < mediaFiles.size(); i++) {
            File file = mediaFiles.get(i);
            String name = file.getName();
            if (file.delete()) {
                MyLog.log("File deleted. file name = " + name);
            }
        }
    }

    /**
     * 检查file 是否包含在List<String>里面
     *
     * @param file   file
     * @param uriStr List<String>
     * @return boolean
     */
    public boolean contains(File file, List<String> uriStr) {
        for (int i = 0; i < uriStr.size(); i++) {
            if (uriStr.get(i).equals(file.toString())) {
                return true;
            }
        }
        return false;
    }


    public int getEtWidth() {
        return etWidth;
    }

    public void setEtWidth(int etWidth) {
        this.etWidth = etWidth;
    }

    public int getEtHeight() {
        return etHeight;
    }

    public void setEtHeight(int etHeight) {
        this.etHeight = etHeight;
    }

    public void setOnMediaTouchListener(OnMediaTouchListener onMediaTouchListener) {
        this.onMediaTouchListener = onMediaTouchListener;
    }

    public interface OnMediaTouchListener {
        void onMediaTouch(Uri uri);
    }
}
