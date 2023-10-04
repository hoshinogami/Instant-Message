package sdx.talkit.page;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import sdx.talkit.component.ChatListFragment;
import sdx.talkit.component.FriendsFragment;
import sdx.talkit.component.InfoFragment;
import sdx.talkit.component.InputDialog;
import sdx.talkit.dao.DB;
import sdx.talkit.entity.ChatItem;
import sdx.talkit.entity.Message;
import sdx.talkit.entity.User;
import sdx.talkit.net.WebSocketService;
import sdx.talkit.util.DateFormatter;
import sdx.talkit.R;
import sdx.talkit.net.Http;

public class HomeActivity extends AppCompatActivity {
    private ImageButton chat_btn;
    private ImageButton friends_btn;
    private ImageButton info_btn;
    private ImageButton friend_add;
    private TextView tab_title;

    private int id;
    private String name;
    private String pswd;
    private ArrayList<ChatItem> chatItemList = new ArrayList<>();
    private ArrayList<User> friends = new ArrayList<>();

    private CurrentFragment currFragment = CurrentFragment.NONE;
    private ChatListFragment chatListFragment;
    private FriendsFragment friendsFragment;
    private InfoFragment infoFragment;

    private WebSocketService.WebSocketServiceBinder serviceBinder;
    //连接服务器
    private final ServiceConnection serviceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            HomeActivity.this.serviceBinder = (WebSocketService.WebSocketServiceBinder) iBinder;
            Toast.makeText(HomeActivity.this,"正在连接服务器",Toast.LENGTH_SHORT).show();
            HomeActivity.this.serviceBinder.getService().connect(HomeActivity.this.id,HomeActivity.this.pswd);
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Toast.makeText(HomeActivity.this,"与服务器连接断开",Toast.LENGTH_LONG).show();
        }
    };

    private final BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Message msg = Message.fromJSONStr(intent.getStringExtra("message"));

            boolean added = false;
            for (ChatItem item : chatItemList) {
                if (item.getId().equals(msg.getFrom())){
                    added = true;
                    boolean contains = false;
                    for (Message existMsg : item.getMsgList()) {
                        if (existMsg.equals(msg)){
                            contains = true;
                            break;
                        }
                    }
                    // 如果该消息未被添加，则添加
                    if (!contains){
                        //如果是图片
                        if(msg.getType()==1){
                            Integer index=msg.getText().indexOf("MyAlbums");
                            downloadFile(msg.getText().substring(index+9));
                        }
                        item.getMsgList().add(msg);
                        DB.insertMessage(msg);
                    }
                    break;
                }
            }
            // 如果该消息没有对应的聊天条目，则新建
            if (!added){
                // 在本地寻找过往的消息
                ArrayList<Message> list = DB.fetchMessage(HomeActivity.this.id, msg.getFrom());
                list.add(msg);
                // 在内存中寻找该ID对应的名字
                String name = "未知用户";
                for (User friend : HomeActivity.this.friends) {
                    if (msg.getFrom().equals(friend.getId())){
                        name = friend.getName();
                        break;
                    }
                }
                // 新建新的聊天条目
                ChatItem newItem = new ChatItem(msg.getFrom(), name,
                        new DateFormatter().fromTimeStamp(msg.getTime()),
                        true, list);
                DB.insertChatItem(id,msg.getFrom(),name);
                chatItemList.add(newItem);
                HomeActivity.this.chatListFragment.update();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        DB.init(HomeActivity.this);

        id = getIntent().getIntExtra("id",-1);
        name = getIntent().getStringExtra("name");
        pswd = getIntent().getStringExtra("pswd");

        this.getFriends();

        // 启动一个 WebSocketService，并绑定相应的BroadcastReceiver

        Intent intent = new Intent(HomeActivity.this,WebSocketService.class);
        this.bindService(intent,serviceConn,BIND_AUTO_CREATE);
        this.registerReceiver(this.messageReceiver, new IntentFilter(WebSocketService.RecvMsgAction));
         //获取页面元素
        chat_btn = this.findViewById(R.id.chat_btn);
        friends_btn = this.findViewById(R.id.friends_btn);
        info_btn = this.findViewById(R.id.info_btn);
        tab_title = this.findViewById(R.id.tab_title);
        friend_add=this.findViewById(R.id.friend_add);
        // fetch data from sqlite
        this.chatItemList.addAll(DB.fetchChatItem(id));
        chatListFragment = ChatListFragment.newInstance(this.chatItemList,id);
        friendsFragment = FriendsFragment.newInstance(this.friends);
        infoFragment = InfoFragment.newInstance(this.id,this.name);

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container,chatListFragment)
                .add(R.id.fragment_container,friendsFragment)
                .hide(friendsFragment)
                .add(R.id.fragment_container,infoFragment)
                .hide(infoFragment)
                .addToBackStack(null)
                .show(chatListFragment)
                .commit();
        //点击事件
        friend_add.setOnClickListener(new View.OnClickListener() {
                                          @Override
                                          public void onClick(View view) {
                                              showDialog();
                                          }
                                      });

        chat_btn.setOnClickListener(view -> switchFragment(CurrentFragment.CHAT));
        friends_btn.setOnClickListener(view -> switchFragment(CurrentFragment.FRIEND));
        info_btn.setOnClickListener(view -> switchFragment(CurrentFragment.INFO));
        chat_btn.callOnClick();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(this.messageReceiver);
        this.unbindService(this.serviceConn);
    }
    //获取朋友
    private void getFriends() {
        Http.get("/friend/get",new Http.KV[]{Http.KV.pair("id", String.valueOf(this.id))},result -> {
            if (result.getInt("code") == 0){
                JSONArray array = result.getJSONArray("data");
                int length = array.length();
                for (int i = 0; i < length; i++) {
                    JSONObject obj = array.getJSONObject(i);
                    User user = User.fromJSONObj(obj);
                    System.out.println(user);
                    this.friends.add(user);
                }
                this.friendsFragment.update();
            }
        });
    }

    private void showDialog(){
        InputDialog inputDialog=new InputDialog();
        Bundle setargs = new Bundle();
        setargs.putInt("id", this.id);
        inputDialog.setArguments(setargs);

        inputDialog.setmActivity(HomeActivity.this);
        inputDialog.show();
    }
    public void addChatItem(ChatItem item){
        for (ChatItem existItem : this.chatItemList) {
            if (existItem.equals(item)){
                return;
            }
        }
        this.chatItemList.add(item);
        DB.insertChatItem(this.id,item.getId(),item.getName());
        this.chatListFragment.update();
        Intent intent = new Intent(HomeActivity.this,TalkActivity.class);
        intent.putExtra("selfId",this.id);
        intent.putExtra("item",item);
        startActivity(intent);
    }

    public static final String CHAT_TITLE = "消息";
    public static final String FRIENDS_TITLE = "好友";
    public static final String INFO_TITLE = "信息";
    //切换页面
    private void switchFragment(CurrentFragment target){
        // 切换Fragment
        if (target == this.currFragment){
            return;
        }
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        switch (this.currFragment){
            case CHAT:{
                ft.hide(this.chatListFragment);
                break;
            }
            case FRIEND:{
                ft.hide(this.friendsFragment);
                break;
            }
            case INFO:{
                ft.hide(this.infoFragment);
                break;
            }
            default:{}
        }
        switch (target){
            case CHAT:{
                tab_title.setText(CHAT_TITLE);
                friend_add.setVisibility(View.GONE);
                ft.show(this.chatListFragment);
                break;
            }
            case FRIEND:{
                tab_title.setText(FRIENDS_TITLE);
                friend_add.setVisibility(View.VISIBLE);
                ft.show(this.friendsFragment);
                break;
            }
            case INFO:{
                tab_title.setText(INFO_TITLE);
                friend_add.setVisibility(View.GONE);
                ft.show(this.infoFragment);
                break;
            }
            default:{}
        }
        this.currFragment = target;
        ft.commit();
    }

    private static enum CurrentFragment {
        CHAT,FRIEND,INFO,NONE
    }

    //下载图片
    private void downloadFile(String fileName){
        OkHttpClient client = new OkHttpClient();
        HttpUrl.Builder urlBuilder = HttpUrl.parse("http://10.21.204.243:3000/image/download").newBuilder();
        urlBuilder.addQueryParameter("filename",fileName);

        Request request = new Request
                .Builder()
                .url(urlBuilder.build())
                .get()
                .build();

        // 发送请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(WebSocketService.TAG, "Download failed: " + e.getMessage());
                // 下载失败的处理，可以在此处添加适当的提示或错误处理逻辑
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    InputStream inputStream = response.body().byteStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    saveToSystemGallery(bitmap,fileName,0);
                    // 如果需要保存图片到本地文件系统，可以使用类似保存图片的逻辑
                } else {
                    Log.e(WebSocketService.TAG, "Download failed. Response code: " + response.code());
                    // 下载失败的处理，可以在此处添加适当的提示或错误处理逻辑
                }
            }
        });
    }


    //保存图片
    public String saveToSystemGallery(Bitmap bmp,String oldFileName,Integer type) {
        // 首先保存图片
        File appDir = new File(HomeActivity.this.getExternalFilesDir(null), "MyAlbums");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName=oldFileName;
        if(type==1) {
            fileName = System.currentTimeMillis() + oldFileName;
        }
        File file = new File(appDir, fileName);
        file.getPath();
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 其次把文件插入到系统图库

        //sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(file.getAbsolutePath())));
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        sendBroadcast(intent);// 发送广播，通知图库更新
        return file.getPath();
    }

}