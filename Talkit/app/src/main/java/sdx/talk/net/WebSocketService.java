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

public class WebSocketService extends Service {
    public static final String RecvMsgAction = "talkit.net.talkit.WebSocketService.ReceiveMessage";
    public static final String SendMsgAction = "talkit.net.talkit.WebSocketService.SendMessage";
    public static final String TAG = "WebSocketService";

    private final WebSocketServiceBinder binder = new WebSocketServiceBinder();
    private final WebSocketClient client = new WebSocketClient(URI.create("ws://10.21.204.243:3000/ws")){
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
