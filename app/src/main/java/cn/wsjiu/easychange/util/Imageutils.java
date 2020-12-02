package cn.wsjiu.easychange.util;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;

import java.io.FileNotFoundException;
import java.io.InputStream;

import cn.wsjiu.easychange.activity.ui.main.PublishFragment;

public class Imageutils {
    private static int DEFAULT_WIDTH = 1024;

    public static String getImagePathByUri(Uri uri) {
        return null;
    }


    public static Bitmap getBitmapByUri(Context context, Uri uri) {
        ContentResolver contentResolver = context.getContentResolver();
        Bitmap bitmap = null;
        try {
            InputStream in = contentResolver.openInputStream(uri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, options);
            options.inSampleSize = options.outWidth / DEFAULT_WIDTH;
            options.inJustDecodeBounds = false;
            in = contentResolver.openInputStream(uri);
            bitmap = BitmapFactory.decodeStream(in, null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bitmap;
    }
}
