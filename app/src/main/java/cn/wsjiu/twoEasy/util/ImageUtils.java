package cn.wsjiu.twoEasy.util;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Base64;

import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public class ImageUtils {
    private final static BitmapPool bitmapPool = new BitmapPool();
    /**
     * 默认图片压缩后的宽度
     */
    private static final int DEFAULT_WIDTH = 512;
    /**
     * 默认头像压缩后的宽度
     */
    private static final int DEFAULT_HEAD_WIDTH = 100;

    /**
     * bitmap转为base64后的前缀（需要手动加上）
     */
    private final static String BASE64_PREFIX = "data:image/png;base64,";

    /**
     * 普通图片
     */
    public final static int COMMON_IMAGE_TYPE = 1;
    /**
     * 头像
     */
    public final static int HEAD_IMAGE_TYPE = 1 << 1;

    /**
     * 解析uri，获取bitmap
     * @param context 上下文
     * @param uri 图片uri
     * @return 位图格式的图片
     */
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
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            in = contentResolver.openInputStream(uri);
            bitmap = BitmapFactory.decodeStream(in, null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 获取图片缓冲池的图片
     * @param key 图片的唯一key
     * @return 返回图片缓冲池的图片
     */
    public static Bitmap getBitmapFromPool(String key) {
        return bitmapPool.get(key);
    }

    /**
     * 移除图片缓冲池的图片
     * @param key 图片的唯一key
     * @return
     */
    public static void removeBitmapFromPool(String key) {
        bitmapPool.remove(key);
    }

    /**
     * 解码位图
     * @param bitmap 位图
     * @return 返回位图解析后的base64编码
     */
    public static String  decodeBitmapToBase64(Bitmap bitmap) {
        return decodeBitmapToBase64(bitmap, COMMON_IMAGE_TYPE);
    }

    /**
     * 解码位图
     * @param bitmap 位图
     * @param type 图片类型
     * @return 返回位图解析后的base64编码
     */
    public static String  decodeBitmapToBase64(Bitmap bitmap, int type) {
        try {
            if(type == HEAD_IMAGE_TYPE && bitmap.getWidth() > DEFAULT_HEAD_WIDTH) {
                float ratio = (float)DEFAULT_HEAD_WIDTH / bitmap.getWidth();
                int compressHeight = (int)(ratio * bitmap.getHeight());
                bitmap = Bitmap.createScaledBitmap(bitmap, DEFAULT_HEAD_WIDTH, compressHeight, true);
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            String base64Str = BASE64_PREFIX + Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP);
            out.close();
            return base64Str;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 保存图片进入图片缓存池
     * @param key 图片的唯一key
     * @param bitmap 要保存的位图
     */
    public static void putBitmapToPool(String key, Bitmap bitmap) {
        bitmapPool.put(key, bitmap);
    }

    /**
     * 解析获得首张图片url
     * @param imageUrlMapStr  图片url字符串，包含多个图片url
     * @return 首张图片url
     */
    @Nullable
    public static String getCoverByStr(String imageUrlMapStr ) {
        String coverUrl = null;
        if(imageUrlMapStr != null && imageUrlMapStr.length() != 0) {
            JSONObject urlJSONObject = JSONObject.parseObject(imageUrlMapStr);
            if(urlJSONObject != null && urlJSONObject.size() != 0) {
                coverUrl = urlJSONObject.getString("0");
                if(coverUrl == null || coverUrl.length() == 0) {
                    for(Map.Entry<String, Object> e : urlJSONObject.entrySet()) {
                        coverUrl = e.getValue().toString();
                        if(coverUrl != null && coverUrl.length() != 0) {
                            break;
                        }
                    }
                }
            }
        }
        if (coverUrl == null || coverUrl.length() == 0) {
            coverUrl = "0/error&&404:404.png";
        }
        return coverUrl;
    }

    /**
     * 图片池，重复利用图片
     */
    static class BitmapPool {
        Map<String, SoftReference<Bitmap>> bitmapMap;
        public BitmapPool() {
            bitmapMap = new HashMap<>(8);
        }
        @Nullable
        public Bitmap get(String key) {
            SoftReference<Bitmap> softReference = bitmapMap.get(key);
            return softReference == null ? null : softReference.get();
        }

        public void put(String key, Bitmap bitmap) {
            SoftReference<Bitmap> softReference = new SoftReference<>(bitmap);
            bitmapMap.put(key, softReference);
        }

        public void remove(String key) {
            bitmapMap.remove(key);
        }
    }
}
