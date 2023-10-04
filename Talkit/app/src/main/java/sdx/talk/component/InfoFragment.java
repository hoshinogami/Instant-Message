package sdx.talk.component;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import sdx.talk.R;
import sdx.talk.page.LoginActivity;

public class InfoFragment extends Fragment {

    boolean needInit = true;

    public static InfoFragment newInstance(Integer id,String name){
        InfoFragment fragment = new InfoFragment();
        Bundle bundle = new Bundle();
        bundle.putString("id",id.toString());
        bundle.putString("name",name);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_info, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (needInit){
            ((TextView)this.getActivity().findViewById(R.id.tv_info_uid))
                    .setText( "UID:" + getArguments().getString("id"));
            ((TextView)this.getActivity().findViewById(R.id.tv_info_name))
                    .setText( "用户名:" + getArguments().getString("name"));
            this.getActivity().findViewById(R.id.btn_logout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences.Editor loginInfo = InfoFragment.this.getActivity()
                            .getSharedPreferences("LoginInfo", Context.MODE_PRIVATE)
                            .edit();
                    loginInfo.clear();
                    loginInfo.apply();

                    Intent intent = new Intent(InfoFragment.this.getActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                    InfoFragment.this.getActivity().startActivity(intent);
                }
            });
            needInit = false;
        }
    }
}
