package cn.wsjiu.twoEasy.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.alibaba.fastjson.JSONObject;

import cn.wsjiu.twoEasy.R;
import cn.wsjiu.twoEasy.component.AdaptionImageView;
import cn.wsjiu.twoEasy.component.LoadingBeanner;
import cn.wsjiu.twoEasy.component.PhotographCropView;
import cn.wsjiu.twoEasy.entity.User;
import cn.wsjiu.twoEasy.result.Result;
import cn.wsjiu.twoEasy.thread.HttpPostRunnable;
import cn.wsjiu.twoEasy.thread.threadPool.ThreadPoolUtils;
import cn.wsjiu.twoEasy.util.DensityUtils;
import cn.wsjiu.twoEasy.util.ImageUtils;
import cn.wsjiu.twoEasy.util.UserUtils;

public class UserInfoEditActivity extends AppCompatActivity {
    private EditText nickNameEditView;
    private EditText declarationEditView;
    private AdaptionImageView headView;
    private Bitmap headImage;
    private User user;
    private LoadingBeanner beanner;

    /**
     * 相册权限请求后的回调请求码
     */
    private final int PHOTO_PERMISSION_REQUEST_CODE = 1;
    /**
     * 图片选择完成后的回调码
     */
    private final int CHOOSE_PHOTO_CODE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info_edit);
        init();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(PHOTO_PERMISSION_REQUEST_CODE == requestCode && grantResults != null && grantResults.length > 0) {
            for(int res : grantResults) {
                if(res == PackageManager.PERMISSION_DENIED) return;;
            }
            choosePhoto();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(CHOOSE_PHOTO_CODE == requestCode && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            Bitmap bitmap = ImageUtils.getBitmapByUri(getApplicationContext(), uri);
            // TODO
            PhotographCropView view = new PhotographCropView(getBaseContext());
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            view.setCropPhotoGraph(bitmap);
            view.setBackgroundColor(Color.BLACK);
            view.setConsumer(this::acceptNewHeadImage);
            this.addContentView(view, layoutParams);
        }
    }

    private void  init() {
        User localUser = UserUtils.getUser();
        user = new User();
        user.setUserId(localUser.getUserId());
        user.setUserNickName(localUser.getUserNickName());
        user.setHeadUrl(localUser.getHeadUrl());

        headView = findViewById(R.id.head_image_view);
        headView.setImageFromUrl(user.getHeadUrl());

        nickNameEditView = findViewById(R.id.nick_name_edit_view);
        nickNameEditView.setText(user.getUserNickName());

        declarationEditView = findViewById(R.id.declaration_edit_view);
        declarationEditView.setText(user.getDeclaration());
    }

    public void back(View view) {
        onBackPressed();
    }


    public void editHead(View view) {
        if(ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PHOTO_PERMISSION_REQUEST_CODE);
        }else {
            choosePhoto();
        }
    }

    private void choosePhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, CHOOSE_PHOTO_CODE);
    }

    public void editFinish(View view) {
        user.setUserNickName(nickNameEditView.getText().toString());
        user.setDeclaration(declarationEditView.getText().toString());
        if(headImage != null) {
            String base64Str = ImageUtils.decodeBitmapToBase64(headImage, ImageUtils.HEAD_IMAGE_TYPE);
            if(base64Str != null) {
                user.setHeadUrl(base64Str);
            }
        }
        Handler handler = new Handler(this::handleForUpdate);
        String url = getResources().getString(R.string.update_user_url);
        HttpPostRunnable<User, Void> runnable = new HttpPostRunnable<>(url, handler, user);
        ThreadPoolUtils.asynExecute(runnable);
        beanner = LoadingBeanner.make(this, DensityUtils.dpToPx(20), R.color.transparent);
        beanner.loading();
    }

    private void acceptNewHeadImage(Bitmap bitmap) {
        headImage = bitmap;
        headView.setImageBitmap(bitmap);
    }

    private boolean handleForUpdate(Message message) {
        Object object = message.obj;
        beanner.cancel();
        if(object instanceof Result) {
            Result result = (Result) object;
            if(result.isSuccess()) {
                JSONObject userJSOnObject = (JSONObject) result.getData();
                User user = userJSOnObject.toJavaObject(User.class);
                UserUtils.updateUser(user, getBaseContext());
                back(null);
            }
        }
        return true;
    }
}