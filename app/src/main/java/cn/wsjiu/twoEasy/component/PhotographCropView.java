package cn.wsjiu.twoEasy.component;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.RequiresApi;

import java.util.function.Consumer;

import cn.wsjiu.twoEasy.R;
import cn.wsjiu.twoEasy.util.DensityUtils;

/**
 * 图片裁切组件
 */
public class PhotographCropView extends ViewGroup {
    /**
     * 需要裁切的位图
     */
    private Bitmap photograph;
    /**
     * 位图在canvas的x坐标
     */
    private int photographX;
    /**
     * 位图在canvas的y坐标
     */
    private int photographY;
    /**
     * 位图显示在canvas的宽度
     */
    private int photographWidth;
    /**
     * 位图显示在canvas的高度
     */
    private int photographHeight;
    /**
     * 位图被绘画的位置
     */
    private Rect photographRect;
    /**
     * 位图被裁切的位置
     */
    private Rect cropRect;
    /**
     * 画笔
     */
    private Paint paint;

    /**
     * 取消和确认按钮
     */
    private Button cancelButton;
    private Button confirmButton;

    /**
     * 位图裁切完成后的消费者
     */
    private Consumer<Bitmap> consumer;

    public PhotographCropView(Context context) {
        super(context);
        init();
    }

    public PhotographCropView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PhotographCropView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public PhotographCropView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        int padding = DensityUtils.dpToPx(10);
        this.setPadding(padding, padding, padding, padding);
        paint = new Paint();
        photographRect = new Rect();
        cropRect = new Rect();

        cancelButton = new Button(getContext(), null, 0);
        confirmButton = new Button(getContext(), null, 0);
        cancelButton.setText("取消");
        confirmButton.setText("确认");
        LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        cancelButton.setLayoutParams(layoutParams);
        confirmButton.setLayoutParams(layoutParams);

        confirmButton.setPadding(padding, padding, padding, padding);
        cancelButton.setPadding(padding, padding, padding, padding);

        confirmButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.water_blue, null)));
        confirmButton.setBackgroundResource(R.drawable.shape_radius_10dp);

        cancelButton.setTextColor(Color.WHITE);
        cancelButton.setBackgroundColor(Color.TRANSPARENT);

        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });
        confirmButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm();
            }
        });

        this.addView(cancelButton);
        this.addView(confirmButton);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        l += getPaddingStart();
        r -= getPaddingEnd();
        t += getPaddingTop();
        b -= getPaddingBottom();
        if(getMeasuredWidth() > 0 && photograph != null) {
            int width = r -l;
            int height = b - t;
            if(photograph.getWidth() > photograph.getHeight()) {
                photographY = t + (height - width) / 2;
                photographHeight = width;
                float tmpVal = photograph.getWidth() * photographHeight;
                photographWidth = (int) (tmpVal / photograph.getHeight());
                photographX = l - (photographWidth - photographHeight) / 2;
            }else {
                photographX = l;
                photographWidth = width;
                float tmpVal = photograph.getHeight() * photographWidth;
                photographHeight = (int) (tmpVal / photograph.getWidth());
                photographY = t + (height - photographHeight) / 2;
            }
            cropRect.set(l, t + (b - t - width) / 2, r, t + (b - t + width) / 2);
        }

        cancelButton.layout(l, b - cancelButton.getMeasuredHeight(), l + cancelButton.getMeasuredWidth(), b);
        confirmButton.layout(r - confirmButton.getMeasuredWidth(), b - confirmButton.getMeasuredHeight(), r, b);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onDraw(Canvas canvas) {
        if(photograph != null) {
            photographRect.set(photographX, photographY, photographX + photographWidth, photographY + photographHeight);
            canvas.drawBitmap(photograph, null, photographRect, paint);
        }
        canvas.clipOutRect(cropRect);
        canvas.drawColor(getResources().getColor(R.color.gray_transparent, null));
    }

    public void setCropPhotoGraph(Bitmap bitmap) {
        photograph = bitmap;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_MOVE){
            int historicalSize = event.getHistorySize();
            if(historicalSize == 0) return true;
            float lastX = event.getHistoricalX(historicalSize - 1);
            float lastY = event.getHistoricalY(historicalSize - 1);
            float offsetX = event.getX() - lastX;
            float offsetY = event.getY() - lastY;
            photographX += 2 * offsetX;
            photographY += 2 * offsetY;
            invalidate();
        }else if(event.getAction() == MotionEvent.ACTION_UP) {
            if(photographX > cropRect.left) photographX = cropRect.left;
            if(photographX + photographWidth < cropRect.right) photographX = cropRect.right - photographWidth;
            if(photographY > cropRect.top) photographY = cropRect.top;
            if(photographY + photographHeight < cropRect.bottom) photographY = cropRect.bottom - photographHeight;
            invalidate();
        }
        this.performClick();
        return true;
    }

    private void confirm() {
        int cropWidth = cropRect.right - cropRect.left;
        int x = cropRect.left - photographX;
        float ratio = (float)(photographWidth) / photograph.getWidth();
        x = (int) (x / ratio);

        int y = cropRect.top - photographY;
        y = (int) (y / ratio);

        cropWidth = (int) (cropWidth / ratio);
        Bitmap bitmap = Bitmap.createBitmap(photograph, x, y, cropWidth, cropWidth);
        photograph = bitmap;
        if(consumer != null) consumer.accept(bitmap);
        cancel();
    }

    private void cancel() {
        ViewGroup viewGroup = (ViewGroup) this.getParent();
        viewGroup.removeView(this);
    }

    public void setConsumer(Consumer<Bitmap> consumer) {
        this.consumer = consumer;
    }
}
