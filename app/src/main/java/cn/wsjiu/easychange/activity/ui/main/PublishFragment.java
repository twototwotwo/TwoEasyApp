package cn.wsjiu.easychange.activity.ui.main;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.alibaba.fastjson.JSONObject;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import cn.wsjiu.easychange.R;
import cn.wsjiu.easychange.entity.Goods;
import cn.wsjiu.easychange.listener.PriceEditListener;
import cn.wsjiu.easychange.result.ResultCode;
import cn.wsjiu.easychange.thread.DataUploadCallable;
import cn.wsjiu.easychange.entity.User;
import cn.wsjiu.easychange.result.Result;
import cn.wsjiu.easychange.util.Imageutils;

/**
 * 商品发布页面
 * @author wsjiu
 * @date 2020/11/12
 */
public class PublishFragment extends Fragment {

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
     * bitmap转为base64后的前缀（需要手动加上）
     */
    private final String BASE64_PREFIX = "data:image/png;base64,";

    /**
     * 图片的recyclerView的适配器
     */
    private RecyclerAdapter recyclerAdapter;

    /**
     * fragement的根视图
     */
    private View rootView;

    /**
     * 用户信息对象
     */
    private final User user;

    public PublishFragment(User user) {
        this.user = user;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_publish, container, false);
        this.rootView = rootView;

        Button cancleButton = rootView.findViewById(R.id.cancel_button);
        cancleButton.setOnClickListener(v -> cancle());

        Button publishButton = rootView.findViewById(R.id.publish_button);
        publishButton.setOnClickListener(v -> publish());

        ImageView addPhotoButton = rootView.findViewById(R.id.add_button);
        addPhotoButton.setOnClickListener(v -> addImage());

        EditText sellPriceEditText = rootView.findViewById(R.id.sell_price);
        sellPriceEditText.addTextChangedListener(new PriceEditListener(sellPriceEditText));

        EditText buyPriceEditText = rootView.findViewById(R.id.buy_price);
        buyPriceEditText.addTextChangedListener(new PriceEditListener(buyPriceEditText));

        // 图片的recyclerView，设置适配器及item间距
        RecyclerView imageGroupView = rootView.findViewById(R.id.image_group);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        imageGroupView.setLayoutManager(layoutManager);
        layoutManager.setOrientation(RecyclerView.HORIZONTAL);
        recyclerAdapter = new RecyclerAdapter();
        imageGroupView.setAdapter(recyclerAdapter);
        imageGroupView.addItemDecoration(new SpaceDecoration());
        return rootView;
    }

    public void cancle() {

    }

    public void publish() {
        String text;
        Goods goods = new Goods();

        EditText titleEditText = rootView.findViewById(R.id.title_text);
        text = titleEditText.getText().toString();
        if(text == null || text.length() == 0) {
            Toast.makeText(getContext(), "标题不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        goods.setTitle(text);

        EditText contentEditText = rootView.findViewById(R.id.content_text);
        text = contentEditText.getText().toString();
        if(text == null || text.length() == 0) {
            Toast.makeText(getContext(), "物品详情不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        goods.setDetail(text);

        EditText sellPriceEditText = rootView.findViewById(R.id.sell_price);
        text = sellPriceEditText.getText().toString();
        goods.setSellPrice(Float.valueOf(text));

        EditText buyPriceEditText = rootView.findViewById(R.id.buy_price);
        text = buyPriceEditText.getText().toString();
        goods.setBuyPrice(Float.valueOf(text));

        //TODO label

        goods.setUserId(user.getUserId());

        Map<String, String> dataMap = new HashMap<>(8);
        dataMap.put(USER_ID, String.valueOf(user.getUserId()));
        Result<Map<String, String>> uploadResult = new Result<>(ResultCode.SUCCESS);
        List<RoundedBitmapDrawable> imageList = recyclerAdapter.getDrawableList();
        // 上传图片
        try {
            for(Integer key = 0; key < imageList.size(); key++) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                imageList.get(key).getBitmap().compress(Bitmap.CompressFormat.PNG, 100, out);
                out.flush();
                String base64Str = Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP);
                dataMap.put(String.valueOf(key), BASE64_PREFIX + base64Str);
                out.close();
            }
            String url = getResources().getString(R.string.image_upload_url);
            DataUploadCallable<Map<String, String>, Map<String, String>> dataUploadCallable = new DataUploadCallable<>(url, dataMap);
            FutureTask<Result<Map<String, String>>> task = new FutureTask<>(dataUploadCallable);
            if(dataMap.size() != 0) {
                new Thread(task).start();
                uploadResult = task.get();
                if(uploadResult.isSuccess()) {
                    Map<String, String> data = uploadResult.getData();
                    if(data != null && data.size() > 0) {
                        goods.setImageUrl(JSONObject.toJSONString(data));
                    }
                }else {
                    Toast.makeText(getContext(), "图片上传失败", Toast.LENGTH_SHORT);
                    return;
                }
            }
        } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "图片上传失败", Toast.LENGTH_SHORT);
                return;
        }
        // 发布物品
        String url = getResources().getString(R.string.goods_publish_url);
        DataUploadCallable<Goods, Void> callable = new DataUploadCallable<>(url, goods);
        FutureTask<Result<Void>> task = new FutureTask<>(callable);
        new Thread(task).start();
        try {
            Result<Void> result = task.get();
            if(result.isSuccess()) {
                Toast.makeText(getContext(), "发布成功", Toast.LENGTH_SHORT);
                cancle();
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void addImage() {
        if(ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PHOTO_PERMISSION_REQUEST_CODE);
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
            ContentResolver contentResolver = getContext().getContentResolver();
            Bitmap bitmap = Imageutils.getBitmapByUri(getContext(), uri);
            RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(getContext().getResources(), bitmap);
            recyclerAdapter.addItem(drawable);
        }
    }

    /**
     * recyclerView 的自定义适配器
     */
    class RecyclerAdapter extends RecyclerView.Adapter {
        /**
         * 固定每个item的高度为200
         */
        private final static int ITEM_HEIGHT = 200;

        private final static int ITEM_WIDTH = 200;

        /**
         * 图片的圆角
         */
        private final float CORNER_RADIUS = 20;

        /**
         * 每个item共用相同的layoutParams
         */
        private ViewGroup.LayoutParams layoutParams;

        /**
         * 图片数据
         */
        private List<RoundedBitmapDrawable> drawableList;

        public RecyclerAdapter() {
            drawableList = new ArrayList<>(4);
            this.layoutParams = new ViewGroup.LayoutParams(ITEM_WIDTH, ITEM_HEIGHT);
        }
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImageView imageView = new ImageView(parent.getContext());
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setLayoutParams(layoutParams);
            return new RecyclerViewHolder(imageView);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ImageView imageView = (ImageView) holder.itemView;
            imageView.setBackground(drawableList.get(position));
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public int getItemCount() {
            return drawableList.size();
        }
        public void addItem(RoundedBitmapDrawable drawable) {
            drawable.setCornerRadius(CORNER_RADIUS);
            drawableList.add(drawable);
            notifyDataSetChanged();
        }

        /**
         * 提供给外部获取图片数据用于上传服务器
         * @return 图片数据的list对象
         */
        public List<RoundedBitmapDrawable> getDrawableList() {
            return drawableList;
        }
    }

    /**
     * 视图持有对象，封装recyclerView的item
     */
    class RecyclerViewHolder extends RecyclerView.ViewHolder {

        public RecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    /**
     * 间距装饰器，修饰recyclerView的item间的间距
     */
    class SpaceDecoration extends RecyclerView.ItemDecoration {
        private final int PADDING = 10;

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            if(outRect != null) {
                outRect.left = PADDING;
                outRect.right = PADDING;
            }
        }
    }
}