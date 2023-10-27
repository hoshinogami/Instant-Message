package sdx.talk.net;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;

import sdx.talk.entity.Constants;

public class WebSocketService extends Service {
    public static final String RecvMsgAction = "talkit.net.talkit.WebSocketService.ReceiveMessage";
    public static final String SendMsgAction = "talkit.net.talkit.WebSocketService.SendMessage";
    public static final String TAG = "WebSocketService";

    private final WebSocketServiceBinder binder = new WebSocketServiceBinder();
//    WebSocket 客户端初始化
    private final WebSocketClient client = new WebSocketClient(URI.create(Constants.MESSAGE_IP)){
        //重写方法
        @Override
        public void onOpen(ServerHandshake handShakeData) {
            Log.i(TAG, "onOpen");
        }
        @Override//从服务端消息（聊天时对方发过来的消息）
        public void onMessage(String msgStr) {
            Intent intent = new Intent();
            intent.setAction(RecvMsgAction);
            intent.putExtra("message",msgStr);
            sendBroadcast(intent);
            Log.i(TAG, "onMessage: transfer " + msgStr);
        }
        @Override
        public void onClose(int code, String reason, boolean remote) {
            Log.i(TAG, "onClose: " + reason);
        }
        @Override
        public void onError(Exception e) {
            Log.e(TAG, "onError: ",e);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate ");
    }
//  messageReceiver 广播接收器：
//定义了一个广播接收器，用于接收来自应用其他部分的消息发送请求。
//当收到消息发送请求广播时，会调用 WebSocket 客户端的 send 方法将消息发送给服务器。
    private final BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (SendMsgAction.equals(intent.getAction())){
                String msgStr = intent.getStringExtra("message");
                Log.i("WebSocketService", "BroadcastReceiver onReceive: " + msgStr);
                WebSocketService.this.client.send(msgStr);
            }
        }
    };
//  这是一个公开方法，用于启动 WebSocket 连接。在该方法中，客户端会尝试连接到 WebSocket 服务器，并发送身份验证信息。
//通过注册 messageReceiver 来监听发送消息请求广播。
    public void connect(Integer id,String pswd){
        try {
            client.connectBlocking();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        client.send("{\"id\":" + id + ",\"pswd\":\"" + pswd + "\"}");
        this.registerReceiver(messageReceiver,new IntentFilter(WebSocketService.SendMsgAction));
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class WebSocketServiceBinder extends Binder{
        public WebSocketService getService(){
            return WebSocketService.this;
        }
    }
}
