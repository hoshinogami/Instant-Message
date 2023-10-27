package sdx.talk.net;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import sdx.talk.entity.Constants;

public class Http {
    public final static String HTTP_URL_PREFIX = Constants.USER_IP;
    public final static String TAG = "Http ";

    public static interface ResponseCallBack{
        void handleResponse(JSONObject result) throws JSONException;
    }

    public static class KV{
        public String key;
        public String val;

        public KV(String key, String val) {
            this.key = key;
            this.val = val;
        }
        public KV(){}
        public static KV pair(String key,String val){
            return new KV(key,val);
        }

        @Override
        public String toString() {
            return "KV{" +
                    "key='" + key + '\'' +
                    ", val='" + val + '\'' +
                    '}';
        }
    }

    public static void get(String url,KV[] kvs,ResponseCallBack callBack){
        OkHttpClient client = new OkHttpClient();
        HttpUrl.Builder urlBuilder = HttpUrl.parse(HTTP_URL_PREFIX + url).newBuilder();
        if (kvs != null){
            for (KV kv : kvs) {
                urlBuilder.addQueryParameter(kv.key,kv.val);
            }
        }
        Request request = new Request
                .Builder()
                .url(urlBuilder.build())
                .get()
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()){
                    try {
                        JSONObject jsonObj = new JSONObject(response.body().string());
                        callBack.handleResponse(jsonObj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });
    }
    public static void post(String url, String filePath){
        // 创建一个 OkHttpClient 实例
        OkHttpClient client = new OkHttpClient();
        HttpUrl.Builder urlBuilder = HttpUrl.parse(HTTP_URL_PREFIX + url).newBuilder();
        // 创建一个 MultipartBody.Builder 对象，用于构建请求体
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM); // 设置请求体类型为表单

        // 添加图片文件到请求体
        File imageFile = new File(filePath);
        if (imageFile.exists()) {
            // 根据实际需求修改表单字段名和文件名
            builder.addFormDataPart("image", imageFile.getName(),
                    RequestBody.create(MediaType.parse("image/*"), imageFile));
        }

        // 构建请求体
        RequestBody requestBody = builder.build();

        // 创建 HTTP 请求对象
        Request request = new Request.Builder()
                .url(urlBuilder.build()) // 替换为实际的上传 URL
                .post(requestBody)
                .build();
        // 发送 HTTP 请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 请求失败处理
                Log.e(TAG, "Upload failed: " + e.getMessage());
                // 可以在这里进行错误提示或处理逻辑
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // 请求成功处理
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    // 在这里处理服务器返回的响应数据
                    Log.d(TAG, "Upload successful. Response: " + responseBody);
                    // 可以根据响应数据执行相应的操作
                } else {
                    // 请求失败处理
                    Log.e(TAG, "Upload failed. Response code: " + response.code());
                    // 可以在这里进行错误提示或处理逻辑
                }
            }
        });

    }
    public static void post(String url,KV[] kvs,String data,ResponseCallBack callBack){
        OkHttpClient client = new OkHttpClient();
        HttpUrl.Builder urlBuilder = HttpUrl.parse(HTTP_URL_PREFIX + url).newBuilder();
        if (kvs != null){
            for (KV kv : kvs) {
                urlBuilder.addQueryParameter(kv.key,kv.val);
            }
        }
        Request request = new Request
                .Builder()
                .url(urlBuilder.build())
                .post(RequestBody.create( MediaType.parse("application/json"),data))
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()){
                    try {
                        JSONObject jsonObj = new JSONObject(response.body().string());
                        callBack.handleResponse(jsonObj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
