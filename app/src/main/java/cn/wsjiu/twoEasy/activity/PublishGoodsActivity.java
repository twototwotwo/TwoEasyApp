package cn.wsjiu.twoEasy.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.wsjiu.twoEasy.R;
import cn.wsjiu.twoEasy.entity.TransactionMode;
import cn.wsjiu.twoEasy.adapter.ImageRecyclerAdapter;
import cn.wsjiu.twoEasy.adapter.common.SpaceDecoration;
import cn.wsjiu.twoEasy.entity.Goods;
import cn.wsjiu.twoEasy.entity.User;
import cn.wsjiu.twoEasy.listener.PriceEditListener;
import cn.wsjiu.twoEasy.result.Result;
import cn.wsjiu.twoEasy.thread.HttpPostRunnable;
import cn.wsjiu.twoEasy.thread.threadPool.ThreadPoolUtils;
import cn.wsjiu.twoEasy.util.DataSourceUtils;
import cn.wsjiu.twoEasy.util.ImageUtils;
import cn.wsjiu.twoEasy.util.UserUtils;

public class PublishGoodsActivity extends AppCompatActivity {

    /**
     * 相册权限请求后的回调请求码
     */
    private final int PHOTO_PERMISSION_REQUEST_CODE = 1;
    /**
     * 相机权限请求后的回调请求码
     */
    private final int CAMERA_PERMISSION_REQUEST_CODE = 2;

    private final int CHOOSE_PHOTO_CODE = 3;

    /**
     * 用户id的key名
     */
    private final String USER_ID = "userId";

    /**
     * 图片的recyclerView的适配器
     */
    private ImageRecyclerAdapter imageRecyclerAdapter;


    /**
     * 物品和用户信息对象
     */
    private User user;
    private Goods goods;

    /**
     * 图片url集合
     */
    private Map<String, String> imageUrlMap;

    private Map<Integer, CheckBox> checkBoxMap;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_goods);
        init();
    }

    /**
     * 初始化
     */
    public void init() {
        user = UserUtils.getUser();
        Button cancelButton = this.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(v -> cancel());

        Button publishButton = this.findViewById(R.id.publish_button);
        publishButton.setOnClickListener(v -> publish());

        ImageView addPhotoButton = this.findViewById(R.id.add_button);
        addPhotoButton.setOnClickListener(v -> addImage());

        EditText sellPriceEditText = this.findViewById(R.id.sell_price);
        sellPriceEditText.addTextChangedListener(new PriceEditListener(sellPriceEditText));

        EditText buyPriceEditText = this.findViewById(R.id.buy_price);
        buyPriceEditText.addTextChangedListener(new PriceEditListener(buyPriceEditText));

        // 图片的recyclerView，设置适配器及item间距
        RecyclerView imageGroupView = this.findViewById(R.id.image_group);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getApplicationContext());
        imageGroupView.setLayoutManager(layoutManager);
        layoutManager.setOrientation(RecyclerView.HORIZONTAL);
        imageRecyclerAdapter = new ImageRecyclerAdapter();
        imageGroupView.setAdapter(imageRecyclerAdapter);
        imageGroupView.addItemDecoration(new SpaceDecoration());

        LinearLayout transactionModeGroup = findViewById(R.id.transaction_mode_group_view);
        LinearLayout.LayoutParams checkBoxLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        checkBoxMap = new HashMap<>(4);
        for (TransactionMode transactionMode : TransactionMode.values()
             ) {
            CheckBox checkBox = new CheckBox(getBaseContext());
            checkBox.setLayoutParams(checkBoxLayoutParams);
            checkBox.setText(transactionMode.modeStr);
            transactionModeGroup.addView(checkBox);
            checkBoxMap.put(transactionMode.mode, checkBox);
        }

        Goods goods = (Goods) getIntent().getSerializableExtra("goods");
        if(goods != null) {
            setGoodsInfo(goods);
        }
    }

    public void setGoodsInfo(Goods goods) {
        this.goods = goods;
        imageRecyclerAdapter.addItems(goods.getImageUrl());
        EditText titleEditText = this.findViewById(R.id.title_text);
        titleEditText.setText(goods.getTitle());
        EditText contentEditText = this.findViewById(R.id.content_text);
        contentEditText.setText(goods.getDetail());
        EditText sellPriceEditText = this.findViewById(R.id.sell_price);
        sellPriceEditText.setText(String.valueOf(goods.getSellPrice()));
        EditText buyPriceEditText = this.findViewById(R.id.buy_price);
        buyPriceEditText.setText(String.valueOf(goods.getBuyPrice()));

        Integer mode = goods.getTransactionMode();
        mode = mode == null ? 0 : mode;
        for (TransactionMode transactionMode : TransactionMode.values()
             ) {
            if( (mode & transactionMode.mode) != 0) {
                CheckBox checkBox = checkBoxMap.get(transactionMode.mode);
                checkBox.setSelected(true);
            }
        }
    }

    /**
     * 调用父类的返回退出
     */
    public void cancel() {
        super.onBackPressed();
    }

    /**
     * 重写覆盖返回操作
     */
    @Override
    public void onBackPressed() {
    }

    /**
     * 物品发布
     */
    public void publish() {
        String text;
        if(goods == null) goods = new Goods();

        EditText titleEditText = this.findViewById(R.id.title_text);
        text = titleEditText.getText().toString();
        if(text == null || text.length() == 0) {
            Toast.makeText(getApplicationContext(), "标题不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        goods.setTitle(text);

        EditText contentEditText = this.findViewById(R.id.content_text);
        text = contentEditText.getText().toString();
        if(text == null || text.length() == 0) {
            Toast.makeText(getApplicationContext(), "物品详情不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        goods.setDetail(text);
        EditText sellPriceEditText = this.findViewById(R.id.sell_price);
        text = sellPriceEditText.getText().toString();
        goods.setSellPrice(Float.valueOf(text));

        EditText buyPriceEditText = this.findViewById(R.id.buy_price);
        text = buyPriceEditText.getText().toString();
        goods.setBuyPrice(Float.valueOf(text));

        //TODO label

        goods.setUserId(user.getUserId());

        int transactionMode = 0;
        for (int mode : checkBoxMap.keySet()
             ) {
            CheckBox checkBox = checkBoxMap.get(mode);
            if(checkBox.isSelected()) {
                transactionMode |= mode;
            }
        }
        goods.setTransactionMode(transactionMode);

        Map<String, String> dataMap = new HashMap<>(8);
        dataMap.put(USER_ID, String.valueOf(user.getUserId()));
        List<Object> imageList = imageRecyclerAdapter.getDrawableList();
        if(imageList.size() == 0) {
            Toast.makeText(getApplicationContext(), "请至少添加一张物品详情图", Toast.LENGTH_SHORT).show();
            return;
        }
        //上传图片
        try {
            imageUrlMap = new HashMap<>(imageList.size());
            for(int key = 0; key < imageList.size(); key++) {
                Object imageObject = imageList.get(key);
                if(imageObject instanceof RoundedBitmapDrawable) {
                    RoundedBitmapDrawable drawable = (RoundedBitmapDrawable) imageObject;
                    Bitmap bitmap = drawable.getBitmap();
                    if(bitmap == null) continue;
                    String base64Str = ImageUtils.decodeBitmapToBase64(bitmap);
                    if(base64Str == null) continue;
                    dataMap.put(String.valueOf(key), base64Str);
                }else if(imageObject instanceof String){
                    String url = (String) imageObject;
                    imageUrlMap.put(String.valueOf(key), url);
                }
            }
            if(dataMap.size() > 0) {
                String url = getResources().getString(R.string.image_upload_url);
                Handler handler = new Handler(getMainLooper(), this::handleForImage);
                HttpPostRunnable<Map<String, String>, Map<String, String>> uploadRunnable =
                        new HttpPostRunnable<>(url, handler, dataMap);
                ThreadPoolUtils.asynExecute(uploadRunnable);
            }else {
                publishGoods(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "图片上传失败", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    private void publishGoods(Map<String, String> imageUrlMap) {
        if(imageUrlMap != null && imageUrlMap.size() > 0) {
            this.imageUrlMap.putAll(imageUrlMap);
        }
        goods.setImageUrl(JSONObject.toJSONString(this.imageUrlMap));
        String url = getResources().getString(R.string.goods_publish_url);
        Handler handler = new Handler(getMainLooper(), this::handleForGoods);
        HttpPostRunnable<Goods, Void> httpPostRunnable = new HttpPostRunnable<>(url, handler, goods);
        ThreadPoolUtils.asynExecute(httpPostRunnable);
    }

    private boolean handleForImage(Message msg) {
        Object obj = msg.obj;
        Result<Map<String, String>> result = null;
        if (obj instanceof Result) {
            result = (Result<Map<String, String>>) obj;
        }
        if(result.isSuccess()) {
            Map<String, String> data = result.getData();
            publishGoods(data);
            return true;
        }else {
            Toast.makeText(getApplicationContext(), "图片上传失败", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private boolean handleForGoods(Message msg) {
        Object obj = msg.obj;
        Result<Void> result = null;
        if (obj instanceof Result) {
            result = (Result<Void>) obj;
        }
        if(result.isSuccess()) {
            Toast.makeText(getApplicationContext(), "发布成功", Toast.LENGTH_SHORT).show();
            cancel();
            DataSourceUtils.addPublishGoods(goods);
            return true;
        }else {
            Toast.makeText(getApplicationContext(), "物品发布失败", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
    /**
     * 添加图片
     */
    public void addImage() {
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

    private void takePhoto() {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(PHOTO_PERMISSION_REQUEST_CODE == requestCode) {
            if(grantResults != null && grantResults.length > 0 &&
                    PackageManager.PERMISSION_GRANTED == grantResults[0]) {
                choosePhoto();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != Activity.RESULT_OK) {
            return;
        }
        if(requestCode == CHOOSE_PHOTO_CODE) {
            Uri uri = data.getData();
            Bitmap bitmap = ImageUtils.getBitmapByUri(getApplicationContext(), uri);
            RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(getApplicationContext().getResources(), bitmap);
            imageRecyclerAdapter.addItem(drawable);
        }
    }
}