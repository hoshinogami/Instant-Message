package sdx.talkit;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;

import sdx.talkit.page.HomeActivity;
import sdx.talkit.page.LoginActivity;

public class MainActivity extends AppCompatActivity {
    private CountDownTimer timer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 获取本地ID和密码
        SharedPreferences loginInfo = this.getSharedPreferences("LoginInfo", Context.MODE_PRIVATE);
        int id = loginInfo.getInt("id", -1);
        String name = loginInfo.getString("name","");
        String pswd = loginInfo.getString("pswd", "");
        if (id == -1 || name.isEmpty() || pswd.isEmpty()) {
            // 跳转到登录界面
            timer = new CountDownTimer(1000,1000) {
                @Override
                public void onTick(long millisUntilFinished) {}
                @Override
                public void onFinish() {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            };
            timer.start();
        } else {
            // 跳转到主页面
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("id",id);
            intent.putExtra("name",name);
            intent.putExtra("pswd",pswd);
            startActivity(intent);
        }
    }

    @Override
    protected void onDestroy() {
        if (this.timer != null){
            this.timer.cancel();
        }
        super.onDestroy();
    }
}