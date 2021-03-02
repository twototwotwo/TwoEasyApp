package cn.wsjiu.twoEasy.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import cn.wsjiu.twoEasy.R;
import cn.wsjiu.twoEasy.entity.Goods;
import cn.wsjiu.twoEasy.entity.User;
import cn.wsjiu.twoEasy.thread.threadPool.ThreadPoolUtils;
import cn.wsjiu.twoEasy.util.UserUtils;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.models.UserInfo;
import io.agora.rtc.video.VideoCanvas;

public class VideoCallActivity extends AppCompatActivity {
    private User chatUser;
    private Goods goods;
    private String channelName;
    private String token;

    // Java
    private static final int PERMISSION_REQ_ID = 22;

    // App 运行时确认麦克风和摄像头设备的使用权限。
    private static final String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
    };

    private RtcEngine mRtcEngine;
    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
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
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);
        init();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        hangUp(null);
    }

    private void init() {
        // 获取权限后，初始化 RtcEngine，并加入频道。
        Intent intent = getIntent();
        chatUser = (User) intent.getSerializableExtra("chatUser");
        channelName = intent.getStringExtra("channelName");
        token = intent.getStringExtra("token");
        goods = (Goods) intent.getSerializableExtra("goods");
        if (checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID)) {
            initEngineAndJoinChannel();
        }


    }

    private void initEngineAndJoinChannel() {
        initializeEngine();
        setupLocalVideo();
        joinChannel();
    }

    private boolean checkSelfPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, requestCode);
            return false;
        }

        return true;
    }

    private void initializeEngine() {
        try {
            mRtcEngine = RtcEngine.create(getBaseContext(), getString(R.string.agora_app_id), mRtcEventHandler);
        } catch (Exception e) {
            Log.e("video error", Log.getStackTraceString(e));
            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
    }

    private void setupLocalVideo() {
        // 启用视频模块。
        mRtcEngine.enableVideo();
        // 创建 SurfaceView 对象。
//        SurfaceView mLocalView = RtcEngine.CreateRendererView(getBaseContext());
//        ViewGroup containerView = findViewById(R.id.local_video_container);
//        containerView.addView(mLocalView);
//        mLocalView.setZOrderMediaOverlay(true);
//        // 设置本地视图。
//        VideoCanvas localVideoCanvas = new VideoCanvas(mLocalView, VideoCanvas.RENDER_MODE_HIDDEN, 0);
//        mRtcEngine.setupLocalVideo(localVideoCanvas);
        int userSelfId = UserUtils.getUser().getUserId();
        View switchCameraView = findViewById(R.id.switch_camera_view);
        if(goods.getUserId().equals(userSelfId)) {
            SurfaceView mLocalView = RtcEngine.CreateRendererView(getBaseContext());
            ViewGroup containerView = findViewById(R.id.open_eye_video_view);
            containerView.addView(mLocalView);
            mLocalView.setZOrderMediaOverlay(true);
            // 设置本地视图。
            VideoCanvas localVideoCanvas = new VideoCanvas(mLocalView, VideoCanvas.RENDER_MODE_HIDDEN, 0);
            mRtcEngine.setupLocalVideo(localVideoCanvas);

            mRtcEngine.enableLocalVideo(true);
            mRtcEngine.muteLocalVideoStream(false);
            switchCameraView.setVisibility(View.VISIBLE);
        }else {
            mRtcEngine.enableLocalVideo(false);
            mRtcEngine.muteLocalVideoStream(true);
            switchCameraView.setVisibility(View.INVISIBLE);
        }
        mRtcEngine.muteLocalAudioStream(false);
    }

    // Java
    private void joinChannel() {

        // 调用 joinChannel 方法 加入频道。
        // TODO Token
        int userSelfId = UserUtils.getUser().getUserId();
        mRtcEngine.joinChannel(token, channelName, "Extra Optional Data", userSelfId);
    }

    private void setupRemoteVideo(int uid) {
        // 创建一个 SurfaceView 对象。

        try {
            if(goods.getUserId().equals(uid)) {
                SurfaceView mRemoteView = RtcEngine.CreateRendererView(getBaseContext());
                ViewGroup containerView = findViewById(R.id.open_eye_video_view);
                containerView.addView(mRemoteView);
                // 设置远端视图。
                mRtcEngine.setupRemoteVideo(new VideoCanvas(mRemoteView, VideoCanvas.RENDER_MODE_HIDDEN, uid));
                mRtcEngine.muteRemoteVideoStream(uid, false);
            }
        }catch (Exception e) {
            e.printStackTrace();
            finish();
        }
    }

    private void onRemoteUserLeft() {
        // TODO
        hangUp(null);
    }

    public void hangUp(View view) {
        ThreadPoolUtils.asynExecute(new Runnable() {
            @Override
            public void run() {
                if(mRtcEngine != null) {
                    mRtcEngine.enableLocalVideo(false);
                    mRtcEngine.muteLocalVideoStream(true);
                    mRtcEngine.muteAllRemoteVideoStreams(true);
                    mRtcEngine.enableLocalAudio(false);
                    mRtcEngine.muteAllRemoteVideoStreams(true);
                    mRtcEngine.muteLocalAudioStream(true);
                    mRtcEngine.leaveChannel();
                }
                RtcEngine.destroy();
            }
        });
        finish();
    }

    public void switchCamera(View view) {
        mRtcEngine.switchCamera();
    }
}