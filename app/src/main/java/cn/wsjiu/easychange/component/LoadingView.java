package cn.wsjiu.easychange.component;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.solver.widgets.Rectangle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import cn.wsjiu.easychange.R;

public class LoadingView extends View {
    private final float WIDTH_RATIO = 0.3f;
    private final float HEIGHT_RATIO = 0.05f;

    private Context context;
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

    public LoadingView(Context context) {
        super(context);
        this.context = context;
        int sdkVersion = Build.VERSION.SDK_INT;
        if(sdkVersion >= Build.VERSION_CODES.R) {
            WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics metrics = new DisplayMetrics();
            WindowMetrics windowMetrics = wm.getCurrentWindowMetrics();
            windowWidth = windowMetrics.getBounds().width();
            windowHeight = windowMetrics.getBounds().height();
        }else {
            WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
            windowWidth = wm.getDefaultDisplay().getWidth();
            windowHeight =wm.getDefaultDisplay().getHeight();
        }
        init();
    }

    public LoadingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LoadingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init() {
        width = (int) (windowWidth * WIDTH_RATIO);
        height = (int)(windowHeight * HEIGHT_RATIO);
        this.setX(windowWidth / 2 - width /2 );
        this.setY(windowHeight / 2 - height / 2);
        paint = new Paint();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), beanerEatIcon, options);
        options.inSampleSize = options.outHeight / height;
        options.inJustDecodeBounds = false;
        beaner[0] = BitmapFactory.decodeResource(getResources(), beanerEatIcon, options);
        beaner[1] = BitmapFactory.decodeResource(getResources(), beanerDownIcon, options);
        matrix = new Matrix();
        matrix.postScale(-1, 1);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setColorFilter(new PorterDuffColorFilter(beanColor, PorterDuff.Mode.SRC_IN));
        int beanX = beanerX / height * height;
        while (beanX >= 0 && beanX + height/2 < width) {
            if((beanX - beanerX) * beanerMoveDirection > 0) {
                canvas.drawCircle(beanX + height/4, height/2, height/6, paint);
            }
            beanX += height / 2 * beanerMoveDirection;
        }
        Rect dst = new Rect();
        dst.set(beanerX, beanerY, beanerX + height, beanerY + height);
        paint.setColorFilter(new PorterDuffColorFilter(beanerColor, PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(beaner[bitmapIndex >> 5], null, dst, paint);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    private void initAnimator() {
        ObjectAnimator animator = new ObjectAnimator();
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
                LoadingView view = (LoadingView)objectAnimator.getTarget();
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
    }

    public void loading() {
        this.setVisibility(VISIBLE);
        Activity activity = (Activity)context;
        ViewGroup rootView = (ViewGroup) activity.findViewById(android.R.id.content).getRootView();
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        rootView.addView(this, layoutParams);
        initAnimator();
    }

    public void cancle() {
        this.setVisibility(GONE);
    }

    public static LoadingView  make(Context context) {
        return new LoadingView(context);
    }

    public int getBeanerX() {
        return beanerX;
    }

    public void setBeanerX(int beanerX) {
        this.beanerX = beanerX;
    }
}
