package cn.wsjiu.twoEasy.component;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import cn.wsjiu.twoEasy.R;

public class ConfirmDialog extends AlertDialog {
    public ConfirmDialog(@NonNull Context context) {
        super(context);
    }

    public ConfirmDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected ConfirmDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setBackgroundDrawableResource(R.drawable.shape_radius_20dp);
    }
}
