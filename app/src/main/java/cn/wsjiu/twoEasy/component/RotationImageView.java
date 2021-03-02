package cn.wsjiu.twoEasy.component;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import java.util.LinkedList;

/**
 * @author wsjiu
 * @date 2020.12.11
 * 自定义图片轮播组件
 */
public class RotationImageView extends View {

    /**
     * 轮播的图片
     */
    private LinkedList<Drawable> drawablesList;

    /**
     * 图片数量
     */
    private int size = 0;

    /**
     * 当前轮播的图片
     */
    private int currentPosition = 0;




    public RotationImageView(Context context) {
        super(context);
    }

    public RotationImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RotationImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public RotationImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
