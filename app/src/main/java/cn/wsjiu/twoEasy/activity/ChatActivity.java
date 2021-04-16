package cn.wsjiu.twoEasy.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;

import java.util.List;

import cn.wsjiu.twoEasy.R;
import cn.wsjiu.twoEasy.component.LoadingBeanner;
import cn.wsjiu.twoEasy.component.TransactionView;
import cn.wsjiu.twoEasy.entity.GoodsState;
import cn.wsjiu.twoEasy.adapter.MessageRecyclerAdapter;
import cn.wsjiu.twoEasy.component.AdaptionImageView;
import cn.wsjiu.twoEasy.dao.MessageDAO;
import cn.wsjiu.twoEasy.entity.IM.Message;
import cn.wsjiu.twoEasy.entity.Goods;
import cn.wsjiu.twoEasy.entity.User;
import cn.wsjiu.twoEasy.result.Result;
import cn.wsjiu.twoEasy.thread.HttpGetRunnable;
import cn.wsjiu.twoEasy.thread.HttpPostRunnable;
import cn.wsjiu.twoEasy.thread.threadPool.ThreadPoolUtils;
import cn.wsjiu.twoEasy.util.DataSourceUtils;
import cn.wsjiu.twoEasy.util.DensityUtils;
import cn.wsjiu.twoEasy.util.ImageUtils;
import cn.wsjiu.twoEasy.util.UserUtils;
import cn.wsjiu.twoEasy.webSocket.IMWebSocket;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;

public class ChatActivity extends AppCompatActivity {
    private LoadingBeanner loadingBeanner;
    private User chatUser;
    private Goods goods;
    private String channelName;
    private String token;
    private boolean isOpenEye = false;
    private Integer userSelfId;
    private String chatId;
    private int offset = 0;
    private int size = 10;

    private EditText inputView;
    private ImageButton openEyeButton;

    private MessageRecyclerAdapter adapter;
    private RecyclerView messageRecyclerView;
    private BroadcastReceiver receiver;

    public TransactionView transactionView;

    // Java
    private static final int PERMISSION_REQ_ID = 22;

    // App 运行时确认麦克风和摄像头设备的使用权限。
    private static final String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
    };

    private static RtcEngine rtcEngine;
    private static IRtcEngineEventHandler iRtcEngineEventHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        if(intent == null) return;
        chatUser = (User) intent.getSerializableExtra("user");
        goods = (Goods) intent.getSerializableExtra("goods");
        String chatId = intent.getStringExtra("chatId");
        userSelfId = UserUtils.getUser().getUserId();
        String action = intent.getAction();
        if(chatId == null || chatId.length() == 0) {
            if(goods == null || chatUser == null) return;
            if(userSelfId > chatUser.getUserId()) {
                chatId = chatUser.getUserId() + ":" + goods.getGoodsId() + ":" + userSelfId;
            }else {
                chatId = userSelfId + ":" + goods.getGoodsId() + ":" + chatUser.getUserId();
            }
        }
        if(chatId.equals(this.chatId)) {
            offset = 0;
            loadLocalMessage();
            return;
        }else {
            this.chatId = chatId;
        }
        String[] splits = chatId.split(":");
        if(chatUser == null) {
            int userId = Integer.parseInt(splits[0]);
            if(userSelfId.equals(userId)) {
                userId = Integer.parseInt(splits[2]);
            }
            chatUser = DataSourceUtils.getUser(userId);
        }
        if(goods == null) {
            int goodsId = Integer.parseInt(splits[1]);
            goods = DataSourceUtils.getGoods(goodsId);
        }
        if(chatUser == null || goods == null) {
            finish();
            return;
        }
        init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(receiver != null) unregisterReceiver(receiver);
        closeEye();
        ThreadPoolUtils.asynExecute(new Runnable() {
            @Override
            public void run() {
                RtcEngine.destroy();
            }
        });
    }

    /**
     * 初始化
     */
    private void init() {
        MessageDAO.instance.updateChatMessageToRead(chatId);
        TextView topNameView = findViewById(R.id.top_name_view);
        topNameView.setText(chatUser.getUserName());

        AdaptionImageView goodsCover = findViewById(R.id.goods_cover);
        String coverUrl = ImageUtils.getCoverByStr(goods.getImageUrl());
        coverUrl = getResources().getString(R.string.image_get_url) + coverUrl;
        goodsCover.setImageFromUrl(coverUrl);
        goodsCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User user;
                if(goods == null) return;
                if(goods.getUserId().equals(chatUser.getUserId())) user = chatUser;
                else user = UserUtils.getUser();
                Intent intent = new Intent();
                intent.setClass(getBaseContext(), GoodsDetailActivity.class);
                intent.putExtra("goods", goods);
                intent.putExtra("user", user);
                startActivity(intent);
            }
        });
        TextView sellPrice = findViewById(R.id.sell_price);
        sellPrice.setText("¥" + goods.getSellPrice()  );
        TextView goodsTitle = findViewById(R.id.goods_title);
        goodsTitle.setText(goods.getTitle());
        Button buyButton = findViewById(R.id.buy);
        buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(transactionView == null) {
                    transactionView = new TransactionView(getBaseContext());
                    transactionView.init(goods, chatUser, ChatActivity.this);
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT);
                    layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                    addContentView(transactionView, layoutParams);
                }else {
                    transactionView.setVisibility(View.VISIBLE);
                }
            }
        });

        openEyeButton = findViewById(R.id.open_eye_button);
        openEyeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) ||
                        !checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID)) {
                    //缺少权限
                    return;
                }
                if(userSelfId.equals(chatUser.getUserId())) {
                    return;
                }
                if(isOpenEye) {
                    closeEye();
                    return;
                }
                loadingBeanner = LoadingBeanner.make(ChatActivity.this,
                        DensityUtils.dpToPx(50), R.color.transparent);
                try {
                    loadingBeanner.loading();
                }catch (Exception e) {
                    e.printStackTrace();
                }
                if(userSelfId.equals(goods.getUserId())) {
                    openEyeCall();
                }else {
                    openEyeConnect();
                }
            }
        });

        inputView = findViewById(R.id.input_text_view);

        if(adapter == null) {
            messageRecyclerView = findViewById(R.id.message_recycler_view);
            LinearLayoutManager manager = new LinearLayoutManager(this);
            manager.setStackFromEnd(true);
            manager.setReverseLayout(true);
            manager.setOrientation(RecyclerView.VERTICAL);
            messageRecyclerView.setLayoutManager(manager);
            adapter = new MessageRecyclerAdapter(chatUser);
            messageRecyclerView.setAdapter(adapter);
            messageRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    if(newState == RecyclerView.SCROLL_STATE_SETTLING &&
                            !recyclerView.canScrollVertically(-1 )) {
                        loadLocalMessage();
                    }
                }
            });
        }else {
            adapter.clearItems();
        }
        messageRecyclerView.scrollToPosition(adapter.getItemCount() - 1);

        // 广播接收器注册
        if(receiver == null) {
            receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String payLoad = intent.getStringExtra("message");
                    Message message = JSONObject.parseObject(payLoad, Message.class);
                    if(chatId != null && chatId.equals(message.getChatId())) {
                        adapter.addFirst(message);
                        MessageDAO.instance.insertMessage(message);
                        messageRecyclerView.scrollToPosition(0);
                        abortBroadcast();
                    }
                }
            };
            IntentFilter intentFilter = new IntentFilter("cn.wsjiu.twoEasy.webSocket.chatMessage");
            // 设置高优先级阻断广播
            intentFilter.setPriority(100);
            registerReceiver(receiver, intentFilter);
        }
        offset = 0;
        loadLocalMessage();
        checkGoodsState();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for(int result : grantResults) {
            if(result == 1) {
                Toast.makeText(getBaseContext(),
                        "缺少相机音频权限，请在设置中授予，否则无法使用开眼功能", Toast.LENGTH_LONG).show();
                return;
            }
        }
    }

    public void loadLocalMessage() {
        MessageDAO messageDAO = MessageDAO.instance;
        List<Message> messageList = messageDAO.queryMessage(chatId, offset, size);
        adapter.addItems(messageList);
        offset += size;
    }

    public void sendMessage(View view) {
        if(inputView.getText().toString().length() == 0) return;
        Message message = new Message();
        message.setContent(inputView.getText().toString());
        message.setSendId(userSelfId);
        message.setReceiveId(chatUser.getUserId());
        message.setGoodsId(goods.getGoodsId());
        message.setChatId(chatId);
        message.setContentType(Message.CONTENT_TYPE_TEXT);
        message.setRead(false);

        if(!userSelfId.equals(message.getReceiveId())) {
            boolean isSuccess = IMWebSocket.sendChatMessage(message);
            if(!isSuccess) {
                Toast.makeText(getBaseContext(), "网络异常", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        message.setRead(true);
        MessageDAO.instance.insertMessage(message);
        adapter.addFirst(message);
        messageRecyclerView.scrollToPosition(0);

        inputView.setText("");
        InputMethodManager manager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
    }


    /**
     * 检查物品状态是否为可购买
     */
    public void checkGoodsState() {
        Button buyButton = findViewById(R.id.buy);
        Goods sourceGoods = DataSourceUtils.getGoods(goods.getGoodsId());
        if(sourceGoods != null) goods = sourceGoods;
        int userSelfId = UserUtils.getUser().getUserId();
        if(userSelfId == goods.getUserId()) {
            buyButton.setVisibility(View.INVISIBLE);
            return;
        }else {
            buyButton.setVisibility(View.VISIBLE);
        }
        if(goods.getState() == GoodsState.UNSOLD.mask) {
            buyButton.setText("立即购买");
            buyButton.setClickable(true);
        }else if(goods.getState() == GoodsState.TRANSACTION.mask){
            buyButton.setText(GoodsState.TRANSACTION.state);
            buyButton.setClickable(false);
        }else if(goods.getState() == GoodsState.SOLD.mask) {
            buyButton.setText(GoodsState.SOLD.state);
            buyButton.setClickable(false);
        }
    }

    public void back(View view) {
        onBackPressed();
    }

    /**
     * 卖家发起开眼邀请
     */
    public void openEyeCall() {
        String url = getResources().getString(R.string.open_eye_call_url);
        if(userSelfId.equals(chatUser.getUserId())) return;
        Message message = new Message();
        message.setSendId(userSelfId);
        message.setReceiveId(chatUser.getUserId());
        message.setGoodsId(goods.getGoodsId());
        message.setChatId(chatId);
        // 存在漏洞，应该再加一个随机数防止channelName固化
        message.setContent("卖家邀请你进行开眼");
        message.setContentType(Message.CONTENT_OPEN_EYE);
        message.setRead(false);
        Handler handler = new Handler(getMainLooper(), this::handleForOpenEye);
        HttpPostRunnable<Message, String> runnable = new HttpPostRunnable<>(url, handler, message);
        ThreadPoolUtils.asynExecute(runnable);
    }

    /**
     * 买家进行开眼
     */
    public void openEyeConnect() {
        String url = getResources().getString(R.string.open_eye_connect_url);
        url += "?chatId=" + chatId + "&userId=" + userSelfId;
        if(userSelfId.equals(chatUser.getUserId())) return;
        Handler handler = new Handler(getMainLooper(), this::handleForOpenEye);
        HttpGetRunnable runnable = new HttpGetRunnable(url, handler);
        ThreadPoolUtils.asynExecute(runnable);
    }


    /**
     * 开眼请求回调
     * @param message 回调信息
     * @return
     */
    public boolean handleForOpenEye(android.os.Message message){
        Object object = message.obj;
        loadingBeanner.cancel();
        if(object instanceof Result) {
            Result result = (Result) object;
            if(result.isSuccess() && result.getData() instanceof JSONObject) {
                JSONObject dataJSONObject = (JSONObject) result.getData();
                token = dataJSONObject.getString("token");
                channelName = dataJSONObject.getString("channelName");
                openEye();
                openEyeButton.setImageDrawable(getDrawable(R.drawable.close_eye));
            }else {
                Toast.makeText(getBaseContext(), result.getMsg(), Toast.LENGTH_LONG).show();
                isOpenEye =false;
            }
        }
        return true;
    }

    private void openEye() {
        try {
            View openEyeVideoView = findViewById(R.id.open_eye_video_view);
            openEyeVideoView.getLayoutParams().height = openEyeVideoView.getMeasuredWidth();
            initEngineAndJoinChannel();
            isOpenEye = true;
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 闭眼（结束视频）
     */
    private void closeEye() {
        if(rtcEngine != null) {
            ViewGroup openEyeVideoView = findViewById(R.id.open_eye_video_view);
            openEyeVideoView.getLayoutParams().height = 0;
            ViewGroup openEyeContainer = findViewById(R.id.open_eye_video_container);
            openEyeContainer.removeAllViews();
            rtcEngine.leaveChannel();
        }
        isOpenEye = false;
        openEyeButton.setImageDrawable(getDrawable(R.drawable.open_eye));
    }

    private boolean checkSelfPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, requestCode);
            return false;
        }
        return true;
    }

    private void initEngineAndJoinChannel() {
        initializeEngine();
        setupLocalVideo();
        joinChannel();
    }

    private void initializeEngine() {
        try {
            if(iRtcEngineEventHandler == null) {
                iRtcEngineEventHandler = new MIRtcEngineEventHandler();
            }
            rtcEngine = RtcEngine.create(getBaseContext(), getString(R.string.agora_app_id), iRtcEngineEventHandler);
        } catch (Exception e) {
            Log.e("video error", Log.getStackTraceString(e));
            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
    }

    private void setupLocalVideo() {
        // 启用视频模块。
        rtcEngine.enableVideo();
        // 创建 SurfaceView 对象。
//        SurfaceView mLocalView = RtcEngine.CreateRendererView(getBaseContext());
//        ViewGroup containerView = findViewById(R.id.local_video_container);
//        containerView.addView(mLocalView);
//        mLocalView.setZOrderMediaOverlay(true);
//        // 设置本地视图。
//        VideoCanvas localVideoCanvas = new VideoCanvas(mLocalView, VideoCanvas.RENDER_MODE_HIDDEN, 0);
//        rtcEngine.setupLocalVideo(localVideoCanvas);
        int userSelfId = UserUtils.getUser().getUserId();
        View switchCameraView = findViewById(R.id.switch_camera_button);
        if(goods.getUserId().equals(userSelfId)) {
            SurfaceView mLocalView = RtcEngine.CreateRendererView(getBaseContext());
            ViewGroup containerView = findViewById(R.id.open_eye_video_container);
            containerView.addView(mLocalView);
            mLocalView.setZOrderMediaOverlay(true);
            // 设置本地视图。
            VideoCanvas localVideoCanvas = new VideoCanvas(mLocalView, VideoCanvas.RENDER_MODE_HIDDEN, 0);
            rtcEngine.setupLocalVideo(localVideoCanvas);

            rtcEngine.enableLocalVideo(true);
            rtcEngine.muteLocalVideoStream(false);
            switchCameraView.setVisibility(View.VISIBLE);
            switchCameraView.setOnClickListener(this::switchCamera);
        }else {
            rtcEngine.enableLocalVideo(false);
            rtcEngine.muteLocalVideoStream(true);
            switchCameraView.setVisibility(View.INVISIBLE);
        }
        rtcEngine.muteLocalAudioStream(false);
    }

    // Java
    private void joinChannel() {

        // 调用 joinChannel 方法 加入频道。
        // TODO Token
        int userSelfId = UserUtils.getUser().getUserId();
        int res = rtcEngine.joinChannel(token, channelName, "Extra Optional Data", userSelfId);
    }

    private void setupRemoteVideo(int uid) {
        // 创建一个 SurfaceView 对象。

        try {
            if(goods.getUserId().equals(uid)) {
                SurfaceView mRemoteView = RtcEngine.CreateRendererView(getBaseContext());
                ViewGroup containerView = findViewById(R.id.open_eye_video_container);
                containerView.addView(mRemoteView);
                // 设置远端视图。
                rtcEngine.setupRemoteVideo(new VideoCanvas(mRemoteView, VideoCanvas.RENDER_MODE_HIDDEN, uid));
                rtcEngine.muteRemoteVideoStream(uid, false);
            }
        }catch (Exception e) {
            e.printStackTrace();
            finish();
        }
    }

    private void onRemoteUserLeft() {
        // TODO
        closeEye();
    }

    private void switchCamera(View view) {
        rtcEngine.switchCamera();
    }
    
    private class MIRtcEngineEventHandler extends IRtcEngineEventHandler {
        @Override
        public void onWarning(int warn) {
            super.onWarning(warn);
        }

        @Override
        public void onUserJoined(int uid, int elapsed) {
            super.onUserJoined(uid, elapsed);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setupRemoteVideo(uid);
                }
            });
        }

        @Override
        // 注册 onJoinChannelSuccess 回调。
        // 本地用户成功加入频道时，会触发该回调。
        public void onJoinChannelSuccess(String channel, final int uid, int elapsed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i("agora", "Join channel success, uid: " + (uid & 0xFFFFFFFFL));
                }
            });
        }

        @Override
        public void onFirstRemoteVideoFrame(int uid, int width, int height, int elapsed) {
            super.onFirstRemoteVideoFrame(uid, width, height, elapsed);
            //setupRemoteVideo(uid);
        }

        @Override
        public void onError(int err) {
            super.onError(err);
        }

        @Override
        // 注册 onUserOffline 回调。
        // 远端用户离开频道或掉线时，会触发该回调。
        public void onUserOffline(final int uid, int reason) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i("agora", "User offline, uid: " + (uid & 0xFFFFFFFFL));
                    onRemoteUserLeft();
                }
            });
        }
    }
}