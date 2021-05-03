 package cn.wsjiu.twoEasy.webSocket;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cn.wsjiu.twoEasy.R;
import cn.wsjiu.twoEasy.entity.IM.Message;
import cn.wsjiu.twoEasy.entity.User;
import cn.wsjiu.twoEasy.result.Result;
import cn.wsjiu.twoEasy.thread.HttpGetRunnable;
import cn.wsjiu.twoEasy.thread.threadPool.ThreadPoolUtils;
import cn.wsjiu.twoEasy.util.UserUtils;

public class IMWebSocket extends WebSocketClient {

    public static IMWebSocket singleInstance;
    /**
     * 用户id
     * websocket握手阶段作为用户session的唯一标识
     */
    private static final String USER_ID = "userId";

    private String url;
    private Integer userId;
    private Context context;
    private boolean isReconnect = false;

    public IMWebSocket(URI serverUri) {
        super(serverUri);
    }

    public IMWebSocket(URI serverUri, Draft protocolDraft) {
        super(serverUri, protocolDraft);
    }

    public IMWebSocket(URI serverUri, Map<String, String> httpHeaders) {
        super(serverUri, httpHeaders);
    }

    public IMWebSocket(URI serverUri, Draft protocolDraft, Map<String, String> httpHeaders) {
        super(serverUri, protocolDraft, httpHeaders);
    }

    public IMWebSocket(URI serverUri, Draft protocolDraft, Map<String, String> httpHeaders, int connectTimeout) {
        super(serverUri, protocolDraft, httpHeaders, connectTimeout);
    }

    public static void init(String url, Integer userId, Context context) {
        try {
            if(singleInstance == null) {
                URI uri = new URI(url);
                Map<String, String> headerMap = new HashMap<>(1);
                headerMap.put(USER_ID, String.valueOf(userId));
                singleInstance = new IMWebSocket(uri, headerMap);
                singleInstance.userId = userId;
                singleInstance.context = context;
                singleInstance.url = url;
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
            Toast.makeText(context, "url异常: " + e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void reconnect() {
        if(isReconnect) {
            super.reconnect();
        }else {
            isReconnect = true;
            connect();
        }
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        singleInstance.getOfflineMessage();
    }

    @Override
    public void onMessage(String messageStr) {
        if(messageStr != null && messageStr.length() > 0) {
            Message message = JSONObject.parseObject(messageStr, Message.class);
            receiveChatMessage(message);
        }
    }


    @Override
    public void onClose(int code, String reason, boolean remote) {

    }

    @Override
    public void onError(Exception ex) {

    }

    /**
     * 接收聊天消息
     * @param message 消息
     */
    public void receiveChatMessage(Message message) {
        Intent intent = new Intent(context.getResources().getString(R.string.chat_message_broadcast));
        intent.putExtra("message", JSONObject.toJSONString(message));
        context.sendOrderedBroadcast(intent, null);
    }

    /**
     * 降价通知
     * @param message 消息
     */
    private void receivePriceDownMessage(Message message) {

    }

    private void getOfflineMessage() {
        User user = UserUtils.getUser();
        String url = context.getString(R.string.message_get_url);
        url += "?userId=" + user.getUserId();
        Handler handler = new android.os.Handler(context.getMainLooper(), this::handleForGetMessage);
        HttpGetRunnable runnable = new HttpGetRunnable(url, handler);
        ThreadPoolUtils.synExecute(runnable);
    }

    private boolean handleForGetMessage(android.os.Message msg) {
        Object object = msg.obj;
        if(object instanceof Result) {
            Result result = (Result) object;
            if(result.isSuccess()) {
                JSONArray messageArray = (JSONArray) result.getData();
                for(int i = 0; i < messageArray.size(); i++) {
                    Message message = messageArray.getObject(i, Message.class);
                    receiveChatMessage(message);
                }
            }else {
                Toast.makeText(context, "发生了一点点异常", Toast.LENGTH_SHORT).show();
            }
        }
        return true;
    }

    public static boolean sendChatMessage(Message chatMessage) {
        if(singleInstance.isOpen()) {
            singleInstance.send(JSONObject.toJSONString(chatMessage));
        }else {
            return false;
        }
        return true;
    }

}
