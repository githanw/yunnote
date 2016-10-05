package cn.githan.yunnote.Widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.style.ImageSpan;

/**
 * Created by BW on 2016/9/30.
 */
public class MyImageSpan extends ImageSpan {
    private Uri uri;

    public MyImageSpan(Context context, Bitmap b) {
        super(context, b);
    }

    public MyImageSpan(Context context, Bitmap b, Uri uri) {
        super(context, b);
        this.uri = uri;
    }

    public Uri getUri() {
        return uri;
    }


}
