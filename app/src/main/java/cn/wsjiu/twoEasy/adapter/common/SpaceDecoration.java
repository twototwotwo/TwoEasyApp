package cn.wsjiu.twoEasy.adapter.common;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 间距装饰器，修饰RecyclerView的item间的间距
 */
public class SpaceDecoration extends RecyclerView.ItemDecoration {
    private final int PADDING = 20;

    public final static int VERTICAL = 1;
    public final static int HORIZONTAL = 2;
    public final static int ALL = 3;

    private int mode = HORIZONTAL;

    public SpaceDecoration(){};

    public SpaceDecoration(int mode) {
        this.mode = mode;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        if(outRect != null) {
            if((mode&HORIZONTAL) > 0) {
                outRect.left = PADDING;
                outRect.right = PADDING;
            }
            if((mode&VERTICAL) > 0) {
                outRect.top = PADDING;
                outRect.bottom = PADDING;
            }
        }
    }
}
