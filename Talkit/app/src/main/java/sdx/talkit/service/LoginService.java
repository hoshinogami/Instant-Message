package sdx.talkit.service;

import android.app.Activity;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.function.Consumer;

import sdx.talkit.entity.User;
import sdx.talkit.net.Http;

public class LoginService {
    private static final String TAG = "LoginService";
    public static void login(String username, String password, Activity context, Consumer<User> callback){
        Http.get("/login",toLoginValues(username,password),result -> {
            Log.i(TAG, "login: " + result.toString());
            User user = null;
            try {
                if (result.getInt("code") == 0){
                    JSONObject data = result.getJSONObject("data");
                    user = new User();
                    user.setId(data.getInt("id"));
                    user.setName(data.getString("name"));
                    final User finalUser = user;
                    context.runOnUiThread(() -> {
                        callback.accept(finalUser);
                    });
                    return;
                }
            } catch (JSONException e) {
                Log.e(TAG, "login: " + user.toString(),e);
                user = null;
                e.printStackTrace();
            }
            context.runOnUiThread(() -> {
                callback.accept(null);
            });
        });
    }
    private static Http.KV[] toLoginValues(String username, String password){
        return new Http.KV[]{
                Http.KV.pair("name",username),
                Http.KV.pair("pswd",password)
        };
    }
}
