package cn.wsjiu.twoEasy.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import cn.wsjiu.twoEasy.R;
import cn.wsjiu.twoEasy.entity.OrderComment;

public class CommentView extends FrameLayout {
    private TextView commentContentView;
    private TextView commentTimeView;

    public CommentView(@NonNull Context context) {
        super(context);
        init();
    }

    public CommentView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CommentView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public CommentView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_comment, this);
        this.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        commentContentView = findViewById(R.id.comment_content_view);
        commentTimeView = findViewById(R.id.time_view);
    }

    public void bindData(OrderComment orderComment) {
        commentContentView.setText(orderComment.getContent());
        commentTimeView.setText(orderComment.getTime());
    }
}
