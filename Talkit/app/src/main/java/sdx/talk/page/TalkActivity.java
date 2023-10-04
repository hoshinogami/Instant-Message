package sdx.talk.page;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.yalantis.ucrop.UCrop;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.FileProvider;
import com.yanzhenjie.permission.runtime.Permission;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import sdx.talk.component.DialogManager;
import sdx.talk.component.MessageListAdapter;
import sdx.talk.entity.ChatItem;
import sdx.talk.entity.Message;
import sdx.talk.R;
import sdx.talk.dao.DB;
import sdx.talk.net.Http;
import sdx.talk.net.WebSocketService;

public class TalkActivity extends AppCompatActivity {
    private MessageListAdapter messageListAdapter;
    private ChatItem chatItem;
    private ImageView img;

    public static final int REQUEST_CODE_CAMERA = 103; //相机
    public static final int REQUEST_CODE_ALBUM = 102; //相册

    private Uri photoUri;//记录图片地址

    private Context context = TalkActivity.this;
    ChatItem item;
    Integer selfId;
    private String TAG="success";

    private final BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Message msg = Message.fromJSONStr(intent.getStringExtra("message"));
            if (msg.getFrom().equals(TalkActivity.this.chatItem.getId())){
                boolean contains = false;
                for (Message existMsg : TalkActivity.this.chatItem.getMsgList()) {
                    if (msg.equals(existMsg)){
                        contains = true;
                        break;
                    }
                }
                if (!contains){
                    if(msg.getType()==1){
                        Integer index=msg.getText().indexOf("MyAlbums");
                        downloadFile(msg.getText().substring(index+9));
                    }
                    TalkActivity.this.chatItem.getMsgList().add(msg);
                    DB.insertMessage(msg);
                }
                TalkActivity.this.messageListAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_talk);
        // 获取数据
         item = getIntent().getParcelableExtra("item");

         selfId = getIntent().getIntExtra("selfId",-1);
        if (selfId < 0){
            Log.w("TalkActivity","onCreate: getIntent().getIntExtra(\"selfId\") is null");
        }

        this.chatItem = item;
        ((TextView)findViewById(R.id.tv_talk_title)).setText(item.getName());

        // 注册接收消息的 BroadcastReceiver
        this.registerReceiver(this.messageReceiver, new IntentFilter(WebSocketService.RecvMsgAction));

        messageListAdapter = new MessageListAdapter(item.getMsgList(),this,selfId);
        ((ListView)findViewById(R.id.message_list_view)).setAdapter(messageListAdapter);

        ((ImageButton)findViewById(R.id.btn_talk_back)).setOnClickListener(v -> {
            this.finish();
        });
        EditText talkInput = findViewById(R.id.talk_msg_input);
        //获取相册权限
        img = findViewById(R.id.id_more);

        img.setOnClickListener(v -> {
            // 底部dialog
            showBottomDialog();
        });
        ((Button)findViewById(R.id.btn_talk_send)).setOnClickListener(v -> {
            if ("".equals(talkInput.getEditableText().toString())){
                Toast.makeText(TalkActivity.this,"不能发送空文本",Toast.LENGTH_SHORT).show();
                return;
            }
            Message msg = new Message(selfId, item.getId(), talkInput.getEditableText().toString(), System.currentTimeMillis(),0);

            Intent intent = new Intent();
            intent.setAction(WebSocketService.SendMsgAction);
            intent.putExtra("message",msg.toJSONStr());
            sendBroadcast(intent);

            DB.insertMessage(msg);

            item.getMsgList().add(msg);
            talkInput.setText("");
            messageListAdapter.notifyDataSetChanged();
        });
    }
    // 判断是否有相机权限
    private void ifHaveCameraPermission() {
        /**
         * AndPermission.hasPermissions：判断是否有相对应的权限
         *      Permission.Group.CAMERA：摄像权限
         */
        if (!AndPermission.hasPermissions(this, Permission.Group.CAMERA)) {
            /**
             * AndPermission：引用权限相关库
             *      onGranted：允许权限
             *      onDenied：拒绝权限
             */
            // 动态申请权限
            AndPermission.with(this).runtime().permission(Permission.Group.CAMERA)
                    .onGranted(permissions -> {
                        openCamera();
                    })
                    .onDenied(denieds -> {
                        if (denieds != null && denieds.size() > 0) {
                            for (int i = 0; i < denieds.size(); i++) {
                                if (!shouldShowRequestPermissionRationale(denieds.get(i))) {
                                    DialogManager.permissionDialog(this, "没有拍摄和录制权限！");
                                    break;
                                }
                            }
                        }
                    }).start();
        } else {
            // 有权限 打开相机
            openCamera();
        }
    }

    // 打开相机
    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Build.VERSION.SDK_INT：获取当前系统sdk版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // 适配Android 7.0文件权限，通过FileProvider创建一个content类型的Uri
            String fileName = String.format("fr_crop_%s.jpg", System.currentTimeMillis());
            File cropFile = new File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName);
            photoUri = FileProvider.getUriForFile(this, this.getPackageName() + ".FileProvider", cropFile);//7.0
        } else {
            photoUri = getDestinationUri();
        }
        // android11以后强制分区存储，外部资源无法访问，所以添加一个输出保存位置，然后取值操作
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        startActivityForResult(intent, REQUEST_CODE_CAMERA);
    }

    // 判断是否有文件存储权限
    private void ifHaveAlbumPermission(Activity activity) {
        //  Permission.Group.STORAGE：文件存储权限
        if (!AndPermission.hasPermissions(activity, Permission.Group.STORAGE)) {
            // Request permission
            AndPermission.with(activity).runtime().permission(Permission.Group.STORAGE).onGranted(permissions -> {
                openAlbum();
            }).onDenied(denieds -> {
                if (denieds != null && denieds.size() > 0) {
                    for (int i = 0; i < denieds.size(); i++) {
                        if (!activity.shouldShowRequestPermissionRationale(denieds.get(i))) {
                            DialogManager.permissionDialog(activity, "没有访问存储权限！");
                            break;
                        }
                    }
                }
            }).start();
        } else {
            openAlbum();
        }
    }

    // 打开相册
    private void openAlbum() {
        Intent intent = new Intent();
        // intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);//支持多选图片
        intent.setType("image/*");
        intent.setAction("android.intent.action.GET_CONTENT");
        intent.addCategory("android.intent.category.OPENABLE");
        startActivityForResult(intent, REQUEST_CODE_ALBUM);
    }
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_ALBUM:
                    Uri selectedImageUri = data.getData();
                    Cursor cursor = getContentResolver().query(selectedImageUri, null, null, null, null);
                    cursor.moveToFirst();//将光标移到第一行
                    String imgNo = cursor.getString(0); //图片编号
                    String imgPath = cursor.getString(1); //图片文件路径
                    String imgName = cursor.getString(2); //图片大小
                    String imgSize = cursor.getString(3);//图片文件
                    ContentResolver cr = this.getContentResolver();
                    try {
                        //根据Uri获取流文件
                        InputStream is = cr.openInputStream(selectedImageUri);
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize =3;
                        Bitmap bitmap = BitmapFactory.decodeStream(is,null,options);
                        String newImgPath=saveToSystemGallery(bitmap,imgName,1);
                        uploadImg(newImgPath);
                        Message msg = new Message(selfId, item.getId(), newImgPath, System.currentTimeMillis(),1);

                        Intent intent = new Intent();
                        intent.setAction(WebSocketService.SendMsgAction);
                        intent.putExtra("message",msg.toJSONStr());
                        sendBroadcast(intent);

                        DB.insertMessage(msg);

                        item.getMsgList().add(msg);

                        messageListAdapter.notifyDataSetChanged();
                    }
                    catch(Exception e)
                    {
                        Log.i("lyf", e.toString());
                    }

                    //Glide.with(context).load(UCrop.getOutput(data)).apply(RequestOptions.bitmapTransform(new CircleCrop())).into(img);
                    break;
                case REQUEST_CODE_CAMERA:
                    Glide.with(context).load(UCrop.getOutput(data)).apply(RequestOptions.bitmapTransform(new CircleCrop())).into(img);
//                    Glide.with(context).load(UCrop.getOutput(data)).into(img);//方形图像
                    break;
            }
        }
        if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
        }
    }

//    // 裁剪方法
//    private void doCrop(Uri data) {
//        UCrop.of(data, getDestinationUri())//当前资源，保存目标位置
//                .withAspectRatio(1f, 1f)//宽高比
//                .withMaxResultSize(500, 500)//宽高
//                .start(this);
//    }

    private Uri getDestinationUri() {
        String fileName = String.format("fr_crop_%s.jpg", System.currentTimeMillis());
        File cropFile = new File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName);
        return Uri.fromFile(cropFile);
    }

    private void showBottomDialog() {
        // 使用Dialog、设置style
        final Dialog dialog = new Dialog(this, R.style.DialogTheme);
        // 设置布局
        View view = View.inflate(this, R.layout.dialog_bottom_menu, null);
        dialog.setContentView(view);

        Window window = dialog.getWindow();
        // 设置弹出位置
        window.setGravity(Gravity.BOTTOM);
        // 设置弹出动画
        window.setWindowAnimations(R.style.main_menu_animStyle);
        // 设置对话框大小
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show();

        dialog.findViewById(R.id.tv_take_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 判断是否有相机权限
                ifHaveCameraPermission();
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.tv_take_pic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 判断是否有文件存储权限
                ifHaveAlbumPermission((Activity) context);
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

    }
    //上传图片
    private void uploadImg(String imagePath) {
       Http.post("/image/upload",imagePath);
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
                    Log.e(TAG, "Download failed: " + e.getMessage());
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
                        Log.e(TAG, "Download failed. Response code: " + response.code());
                        // 下载失败的处理，可以在此处添加适当的提示或错误处理逻辑
                    }
                }
            });
        }


    //保存图片
    public String saveToSystemGallery(Bitmap bmp,String oldFileName,Integer type) {
        // 首先保存图片
        File appDir = new File(this.context.getExternalFilesDir(null), "MyAlbums");
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