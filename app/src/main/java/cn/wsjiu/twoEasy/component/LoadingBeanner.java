package cn.wsjiu.twoEasy.component;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import cn.wsjiu.twoEasy.R;
import cn.wsjiu.twoEasy.util.DensityUtils;

public class LoadingBeanner extends View {

    private int beanColor = Color.BLACK;
    private int beanerColor = Color.BLUE;
    private int beanerMoveDirection = 1;
    private int windowWidth = 0;
    private int windowHeight = 0;
    private int width = 0;
    private int height = 0;
    private int beanerEatIcon = R.drawable.beaner_eat;
    private int beanerDownIcon = R.drawable.beaner_down;
    private int beanerX = 0;
    private int beanerY = 0;
    private int duration = 3000;
    private Bitmap[] beaner = new Bitmap[2];
    private int bitmapIndex = 0;
    private Paint paint = new Paint();
    private Matrix matrix;

    private RelativeLayout backgroundMask;
    private ObjectAnimator animator;
    private PorterDuffColorFilter beanColorFilter;
    private PorterDuffColorFilter beanerColorFilter;
    private Rect dst;

    public LoadingBeanner(Context context) {
        super(context);
        init();
    }

    public LoadingBeanner(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LoadingBeanner(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        this.setBackgroundResource(R.drawable.shape_radius_10dp);
        this.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        paint = new Paint();
        beanColorFilter  = new PorterDuffColorFilter(beanColor, PorterDuff.Mode.SRC_IN);
        beanerColorFilter = new PorterDuffColorFilter(beanerColor, PorterDuff.Mode.SRC_IN);
        dst = new Rect();
        BitmapFactory.Options options = new BitmapFactory.Options();
        beaner[0] = BitmapFactory.decodeResource(getResources(), beanerEatIcon, options);
        beaner[1] = BitmapFactory.decodeResource(getResources(), beanerDownIcon, options);
        matrix = new Matrix();
        matrix.postScale(-1, 1);
        initAnimator();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if(width != getMeasuredWidth() || height != getMeasuredHeight()) {
            width = getMeasuredWidth();
            height = getMeasuredHeight();
            initAnimator();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setColorFilter(beanColorFilter);
        int beanX = 0;
        while (beanX >= 0 && beanX + height/2 < width) {
            if((beanX - beanerX) * beanerMoveDirection > 0) {
                canvas.drawCircle(beanX + (height >> 2), height >> 1, height/6, paint);
            }
            beanX += height / 2;
        }
        dst.set(beanerX, beanerY, beanerX + height, beanerY + height);
        paint.setColorFilter(beanerColorFilter);
        canvas.drawBitmap(beaner[bitmapIndex >> 5], null, dst, paint);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    public void initAnimator() {
        if(animator == null) {
            animator = new ObjectAnimator();
            animator.setDuration(duration);
            animator.setPropertyName("beanerX");
            animator.setIntValues(0, width - height);
            animator.setRepeatCount(Animation.INFINITE);
            animator.setRepeatMode(ValueAnimator.REVERSE);
            animator.setTarget(this);
            animator.setInterpolator(new LinearInterpolator());
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    ObjectAnimator objectAnimator = (ObjectAnimator)animation;
                    LoadingBeanner view = (LoadingBeanner)objectAnimator.getTarget();
                    view.bitmapIndex = view.bitmapIndex + 1;
                    view.bitmapIndex = view.bitmapIndex & 63;
                    view.invalidate();
                }
            });
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {

                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                    beaner[0] = Bitmap.createBitmap(beaner[0], 0, 0, beaner[0].getWidth(), beaner[0].getHeight(), matrix, true);
                    beaner[1] = Bitmap.createBitmap(beaner[1], 0, 0, beaner[1].getWidth(), beaner[1].getHeight(), matrix, true);
                    beanerMoveDirection = -beanerMoveDirection;
                }
            });
            animator.start();
        }else {
            animator.setIntValues(0, width - height);
        }
    }

    public boolean loading() {
        Context context = getContext();
        if(context instanceof Activity) {
            Activity activity = (Activity) context;
            backgroundMask = new RelativeLayout(getContext());
            ViewGroup.LayoutParams maskLayoutParams = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            backgroundMask.setLayoutParams(maskLayoutParams);
            RelativeLayout.LayoutParams beannerLayoutParams = (RelativeLayout.LayoutParams) this.getLayoutParams();
            beannerLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            this.setLayoutParams(beannerLayoutParams);
            backgroundMask.addView(this);
            activity.addContentView(backgroundMask, backgroundMask.getLayoutParams());
            return true;
        }
        return false;
    }

    public boolean cancel() {
        ViewGroup viewGroup = (ViewGroup) backgroundMask.getParent();
        viewGroup.removeView(backgroundMask);
        return true;
    }

    /**
     * @param context 上下文
     * @param horizontalMargin 水平外边距
     * @param backgroundResId 背景
     * @return
     */
    public static LoadingBeanner make(Context context, int horizontalMargin, int backgroundResId) {
        LoadingBeanner instance = new LoadingBeanner(context);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DensityUtils.dpToPx(50));
        layoutParams.setMargins(horizontalMargin, 0, horizontalMargin, 0);
        instance.setLayoutParams(layoutParams);
        instance.setBackgroundResource(backgroundResId);
        return instance;
    }

    public int getBeanerX() {
        return beanerX;
    }

    public void setBeanerX(int beanerX) {
        this.beanerX = beanerX;
    }
}
