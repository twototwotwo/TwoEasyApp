package cn.wsjiu.easychange.activity.ui.main;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import cn.wsjiu.easychange.R;
import cn.wsjiu.easychange.thread.LoadImageCallable;
import cn.wsjiu.easychange.entity.User;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class PersonFragment extends Fragment {
    private User user;
    private View rootView;
    private RoundedBitmapDrawable headDrawable;

    public PersonFragment(User user) {
        this.user = user;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void init() {
        ImageView headImageView = rootView.findViewById(R.id.head_image_view);
        if(headDrawable == null) {
            LoadImageCallable callable = new LoadImageCallable(user.getHeadUrl());
            FutureTask<Bitmap> task = new FutureTask<>(callable);
            new Thread(task).start();
            try {
                Bitmap headBitmap = task.get();
                headDrawable = RoundedBitmapDrawableFactory.create(null, headBitmap);
                headDrawable.setCornerRadius(30);
                headImageView.setBackground(headDrawable);
                headImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                headImageView.invalidate();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        TextView nameView = rootView.findViewById(R.id.user_name_text_view);
        nameView.setText(user.getUserNickName());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        this.rootView = inflater.inflate(R.layout.fragment_person, container, false);
        return this.rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        init();
    }
}