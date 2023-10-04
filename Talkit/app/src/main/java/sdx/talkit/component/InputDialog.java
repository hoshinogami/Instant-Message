package sdx.talkit.component;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import org.json.JSONArray;
import org.json.JSONObject;

import sdx.talkit.R;
import sdx.talkit.entity.User;
import sdx.talkit.net.Http;

public class InputDialog extends DialogFragment {


    protected FragmentActivity mActivity;

    public void setmActivity(FragmentActivity mActivity) {
        this.mActivity = mActivity;
    }

    //自定义样式，注：此处主要设置弹窗的宽高
    @Override
    public int getTheme() {
        return R.style.InputDialogStyle;
    }
    private Button search;
    private TextView findResult;
    private EditText input;
    private LinearLayout friend;
    private TextView friendId;
    private TextView friendName;
    private  Button add;
    private Integer id;
    private  User user;
    private static final String TestApp="TestApp";



    public void show(){
        mActivity.runOnUiThread(() -> {
            if (isActivityAlive()){
                FragmentManager fm = mActivity.getSupportFragmentManager();
                Fragment prev = fm.findFragmentByTag(getClass().getSimpleName());
                if (prev != null) fm.beginTransaction().remove(prev);
                if (!InputDialog.this.isAdded()) {
                    InputDialog.super.show(fm, getClass().getSimpleName());
                }
            }
        });
    }

    @Override
    public void dismiss() {
        mActivity.runOnUiThread(() -> {
            if (isActivityAlive()) {
                InputDialog.super.dismissAllowingStateLoss();
            }
        });
    }

    private boolean isActivityAlive() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return mActivity != null && !mActivity.isFinishing() && !mActivity.isDestroyed();
        }else {
            return mActivity != null && !mActivity.isFinishing();
        }
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_newfriend,container,false);
        search=view.findViewById(R.id.id_confirm);
        input=view.findViewById(R.id.id_inputId);
        findResult=view.findViewById(R.id.id_resultNo);
        friend=view.findViewById(R.id.id_friend);
        friendId=view.findViewById(R.id.id_friendId);
        friendName=view.findViewById(R.id.id_friendName);
        add=view.findViewById(R.id.id_friendAdd);
        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog;
        Bundle getargs = getArguments();
        this.id = getargs.getInt("id");
        dialog = super.onCreateDialog(savedInstanceState);
        Window window = dialog.getWindow();
        window.setGravity(Gravity.BOTTOM);
        return dialog;
    }
    public void onStart(){
        super.onStart();

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TestApp,"HelloWorld");
                FindFriends();
            }
        });
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFriend();
            }
        });
    }
    private void FindFriends() {
        Http.get("/friend/find",new Http.KV[]{Http.KV.pair("friendId", String.valueOf(input.getText()))}, result -> {
            if (result.getInt("code") == 0){
                JSONArray array = result.getJSONArray("data");
                int length = array.length();
                if(length==0){
                   findResult.setText("未找到该用户");
                   return;
                }
                    JSONObject obj = array.getJSONObject(0);
                       user = User.fromJSONObj(obj);
                    mActivity.runOnUiThread(new Runnable() {
//                       user = User.fromJSONObj(obj);
                        @Override
                        public void run() {
                            findResult.setVisibility(View.GONE);
                            friend.setVisibility(View.VISIBLE);
                            friendId.setText("id:"+user.getId());
                            friendName.setText(user.getName());
                        }
                    });

            }
        });
    }
    //添加朋友
    private  void addFriend(){
        Http.get("/friend/add",new Http.KV[]{Http.KV.pair("id",String.valueOf(this.id)),Http.KV.pair("friendId",String.valueOf(user.getId()))},result -> {
            if (result.getInt("code") == 0){

            }
            this.dismiss();
        });

    }

}

