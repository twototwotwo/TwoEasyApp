package cn.wsjiu.twoEasy.component;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.LinearLayout;

import androidx.cardview.widget.CardView;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import cn.wsjiu.twoEasy.R;
import cn.wsjiu.twoEasy.result.Result;
import cn.wsjiu.twoEasy.thread.LoadImageRunnable;
import cn.wsjiu.twoEasy.thread.threadPool.ThreadPoolUtils;
import cn.wsjiu.twoEasy.util.DensityUtils;
import cn.wsjiu.twoEasy.util.ImageUtils;

/**
 * 自适应图片。根据传入宽高提前占位（在网络图片未加载成功前）
 */
public class AdaptionImageView extends androidx.appcompat.widget.AppCompatImageView {
    private int imageWidth = 1;
    private int imageHeight = 1;
    private int cornerRadius = 10;

    public AdaptionImageView(Context context) {
        super(context);
        init();
    }

    public AdaptionImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AdaptionImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        ViewOutlineProvider outlineProvider = new ViewOutlineProvider(){
            @Override
            public void getOutline(View view, Outline outline) {
                int width = view.getWidth();
                int height = view.getHeight();
                if (cornerRadius < 0) {
                    int radius = Math.min(width, height) / 2;
                    Rect rect = new Rect(width/2-radius, height/2-radius, width/2+radius, height/2+radius);
                    outline.setOval(rect);  // API>=21
                } else {
                    Rect rect = new Rect(0, 0, width, height);
                    outline.setRoundRect(rect, cornerRadius);
                }
            }
        };
        setClipToOutline(true);
        setOutlineProvider(outlineProvider);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)   {
        Drawable background = this.getDrawable();
        if(background == null) background = this.getBackground();
        if(background != null) {
            imageWidth = background.getIntrinsicWidth();
            imageHeight = background.getIntrinsicHeight();
        }
        float ratio = imageHeight / (float) imageWidth;
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if(layoutParams.width == ViewGroup.LayoutParams.MATCH_PARENT
                || layoutParams.width > 0) {
            int width = layoutParams.width;
            if(layoutParams.width == ViewGroup.LayoutParams.MATCH_PARENT) {
                width = MeasureSpec.getSize(widthMeasureSpec);
            }
            int mode = MeasureSpec.EXACTLY;
            int height = (int) (width * ratio);
            if(layoutParams.height > 0) height = layoutParams.height;
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, mode);
        }else if(layoutParams.height == ViewGroup.LayoutParams.MATCH_PARENT
                || layoutParams.height > 0) {
            int height = layoutParams.height;
            if(layoutParams.height == ViewGroup.LayoutParams.MATCH_PARENT) {
                height = MeasureSpec.getSize(heightMeasureSpec);
            }
            int mode = MeasureSpec.EXACTLY;
            int width = (int) (height / ratio);
            if(layoutParams.width > 0) width = layoutParams.width;
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, mode);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }


    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }

    public void setImageFromUrl(String url) {
        if (!url.contains("http")) {
            url = getResources().getString(R.string.image_get_url) + url;
        }
        Bitmap coverBitmap = ImageUtils.getBitmapFromPool(url);
        if(coverBitmap != null) {
            // 图片缓存池获取到图片
            setImageBitmap(coverBitmap);
        }else {
            Handler handler = new Handler(getContext().getMainLooper(), this::handle);
            LoadImageRunnable loadImageRunnable = new LoadImageRunnable(url, handler);
            ThreadPoolUtils.asynExecute(loadImageRunnable);
        }
    }



    public void setCornerRadius(int cornerRadius) {
        this.cornerRadius = cornerRadius;
        ViewOutlineProvider outlineProvider = new ViewOutlineProvider(){
            @Override
            public void getOutline(View view, Outline outline) {
                int width = view.getWidth();
                int height = view.getHeight();
                if (cornerRadius < 0) {
                    int radius = Math.min(width, height) / 2;
                    Rect rect = new Rect(width/2-radius, height/2-radius, width/2+radius, height/2+radius);
                    outline.setOval(rect);  // API>=21
                } else {
                    Rect rect = new Rect(0, 0, width, height);
                    outline.setRoundRect(rect, cornerRadius);
                }
            }
        };
        setClipToOutline(true);
        setOutlineProvider(outlineProvider);
    }

    /**
     * 异步图片请求的回调处理
     * @param msg 回调信息
     * @return true 处理成功 false 请求失败
     */
    public boolean handle(Message msg) {
        Object obj = msg.obj;
        Result<Bitmap> result = null;
        if (obj instanceof Result) result = (Result<Bitmap>) obj;
        if(result == null || !result.isSuccess() || result.getData() == null) {
            return false;
        }else {
            Bitmap bitmap = result.getData();
            setImageBitmap(bitmap);
            return true;
        }
    }
}
