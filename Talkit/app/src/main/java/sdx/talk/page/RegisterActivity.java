package sdx.talk.page;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Arrays;

import sdx.talk.R;
import sdx.talk.net.Http;

public class RegisterActivity extends AppCompatActivity {
    private boolean sended = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        EditText nameInput = findViewById(R.id.register_name_input);
        EditText pswdInput = findViewById(R.id.register_pswd_input);
        EditText pswdCheckInput = findViewById(R.id.register_pswd_check_input);

        findViewById(R.id.register_btn).setOnClickListener(v -> {
            if (sended){
                return;
            }
            if (!pswdInput.getText().toString().equals(pswdCheckInput.getText().toString())){
                Toast.makeText(RegisterActivity.this,"密码与验证密码不同",Toast.LENGTH_SHORT).show();
                return;
            }
            Http.KV[] kvs = {
                    Http.KV.pair("name", nameInput.getText().toString()),
                    Http.KV.pair("pswd", pswdInput.getText().toString())
            };
            Log.i("RegisterActivity", "onCreate: " + Arrays.toString(kvs));
            Http.get("/register",kvs, result -> {
                if (result.getInt("code") == 0){
                    RegisterActivity.this.runOnUiThread(() -> {
                        Toast.makeText(RegisterActivity.this,"注册成功",Toast.LENGTH_LONG).show();
                        RegisterActivity.this.finish();
                    });
                }else{
                    String msg = result.getString("msg");
                    RegisterActivity.this.runOnUiThread(() -> {
                        Toast.makeText(RegisterActivity.this,"注册失败 " + msg ,Toast.LENGTH_LONG).show();
                    });
                }
            });
        });
        findViewById(R.id.return_btn).setOnClickListener(v -> {
            this.finish();
        });
    }

}