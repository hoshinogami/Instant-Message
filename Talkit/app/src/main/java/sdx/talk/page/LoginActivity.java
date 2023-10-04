package sdx.talk.page;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import sdx.talk.service.LoginService;
import sdx.talk.R;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        EditText name_input = findViewById(R.id.id_input);
        EditText pswd_input = findViewById(R.id.pswd_input);
        findViewById(R.id.login_btn).setOnClickListener(v -> {
            String name = name_input.getText().toString();
            String pswd = pswd_input.getText().toString();
            // 验证登陆程序
            LoginService.login(name,pswd,LoginActivity.this, user -> {
                if (user == null){
                    // 登录失败
                    Toast.makeText(LoginActivity.this,"登录失败",Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(LoginActivity.this,"登录成功",Toast.LENGTH_SHORT).show();
                // 将用户信息持久化保存
                SharedPreferences loginInfo = this.getSharedPreferences("LoginInfo", Context.MODE_PRIVATE);
                loginInfo.edit()
                        .putInt("id",user.getId())
                        .putString("name",name)
                        .putString("pswd",pswd)
                        .apply();

                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("id",user.getId());
                intent.putExtra("name",user.getName());
                intent.putExtra("pswd",pswd);
                startActivity(intent);
            });
        });
        findViewById(R.id.register_btn).setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
        });
    }
}