package cn.wsjiu.twoEasy.component;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import cn.wsjiu.twoEasy.R;
import cn.wsjiu.twoEasy.adapter.ImageRecyclerAdapter;
import cn.wsjiu.twoEasy.result.Result;
import cn.wsjiu.twoEasy.thread.LoadImageRunnable;
import cn.wsjiu.twoEasy.thread.threadPool.ThreadPoolUtils;
import cn.wsjiu.twoEasy.util.DensityUtils;
import cn.wsjiu.twoEasy.util.ImageUtils;

/**
 * 图片组件带取消按钮
 */
public class ImageSelectedView extends RelativeLayout {
    /**
     * 圆角半径
     */
    private static int DEFAULT_RADIUS = DensityUtils.dpToPx(5);

    /**
     * 固定高度为300
     */
    private final int ITEM_HEIGHT = 200;
    private final float CANCEL_BUTTON_PERCENT = 0.3f;

    private ImageButton deleteButton;
    private ImageView imageView;
    private ImageRecyclerAdapter parentAdapter;
    /**
     * 当前绑定的数据在adapter中的位置
     */
    private int position;

    public ImageSelectedView(Context context) {
        super(context);
        init(context);
    }

    public ImageSelectedView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ImageSelectedView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public ImageSelectedView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    public void init(Context context) {
        ViewGroup.LayoutParams viewGroupLayoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setLayoutParams(viewGroupLayoutParams);
        imageView = new ImageView(context);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        LayoutParams layoutParams = new LayoutParams(ITEM_HEIGHT, ITEM_HEIGHT);
        imageView.setLayoutParams(layoutParams);

        deleteButton = new ImageButton(context);
        layoutParams = new LayoutParams((int) (ITEM_HEIGHT * CANCEL_BUTTON_PERCENT),
                (int) (ITEM_HEIGHT * CANCEL_BUTTON_PERCENT));
        deleteButton.setLayoutParams(layoutParams);
        deleteButton.setBackground(getResources().getDrawable(R.drawable.delete_icon, null));
        deleteButton.setOnClickListener(new DeleteClickListener());
        this.addView(imageView);
        this.addView(deleteButton);
    }

    private void setImage(Drawable drawable) {
        if(drawable instanceof RoundedBitmapDrawable) {
            ((RoundedBitmapDrawable) drawable).setCornerRadius(DEFAULT_RADIUS);
        }
        imageView.setBackground((Drawable) drawable);
    }

    public void bindData(Object background, int position) {
        if(background instanceof Drawable) {
            setImage((Drawable) background);
        }else {
            String url = (String) background;
            setImageFromUrl(url);
        }
    }

    public void setImageFromUrl(String url) {
        url = getResources().getString(R.string.image_get_url) + url;
        Bitmap coverBitmap = ImageUtils.getBitmapFromPool(url);
        if(coverBitmap != null) {
            // 图片缓存池获取到图片
            RoundedBitmapDrawable roundedBitmapDrawable =
                    RoundedBitmapDrawableFactory.create(getResources(),
                            coverBitmap);
            setImage(roundedBitmapDrawable);
        }else {
            Handler handler = new Handler(getContext().getMainLooper(), this::handle);
            LoadImageRunnable loadImageRunnable = new LoadImageRunnable(url, handler);
            ThreadPoolUtils.asynExecute(loadImageRunnable);
        }
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
            RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory
                    .create(getResources(), bitmap);
            setImage(roundedBitmapDrawable);
            return true;
        }
    }

    public void setParentAdapter(ImageRecyclerAdapter parentAdapter) {
        this.parentAdapter = parentAdapter;
    }

    public void deleteImage() {
        if(parentAdapter != null) parentAdapter.deleteItem(position);
    }

    class DeleteClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            deleteImage();
        }
    }
}
