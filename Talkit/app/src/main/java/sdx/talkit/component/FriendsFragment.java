package sdx.talkit.component;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import sdx.talkit.R;
import sdx.talkit.entity.ChatItem;
import sdx.talkit.entity.User;
import sdx.talkit.page.HomeActivity;

public class FriendsFragment extends Fragment {
    private ArrayList<User> friendList;
    private SimpleAdapter friendsAdapter;
    private ListView listView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle bundle) {
        return inflater.inflate(R.layout.fragment_friend_list, container, false);
    }

    public static FriendsFragment newInstance(ArrayList<User> friendList){
        FriendsFragment fragment = new FriendsFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("friends",friendList);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.friendList = getArguments().getParcelableArrayList("friends");
        friendsAdapter = new SimpleAdapter(
                this.getContext(),
                toHashMapList(),
                R.layout.adapter_friend_item,
                new String[]{"name","id"},
                new int[]{
                        R.id.tv_friend_item_name,
                        R.id.tv_friend_item_uid
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        this.listView = this.getActivity().findViewById(R.id.fragment_friend_list_view);
        if(listView.getAdapter() == null){
            listView.setAdapter(this.friendsAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    User user = FriendsFragment.this.friendList.get(position);
                    ((HomeActivity)FriendsFragment.this.getActivity()).addChatItem(new ChatItem(
                            user.getId(),
                            user.getName(),
                            "-",
                            false,
                            new ArrayList<>()
                    ));
                }
            });
        }
    }

    private List<HashMap<String,String>> toHashMapList(){
        ArrayList<HashMap<String,String>> list = new ArrayList<>( this.friendList == null ? 8 : this.friendList.size());
        for (User user : friendList) {
            HashMap<String,String> map = new HashMap<>();
            map.put("name",user.getName());
            map.put("id", "UID:" + user.getId());
            list.add(map);
        }
        return list;
    }

    public void update(){
        friendsAdapter = new SimpleAdapter(
                this.getContext(),
                toHashMapList(),
                R.layout.adapter_friend_item,
                new String[]{"name","id"},
                new int[]{
                        R.id.tv_friend_item_name,
                        R.id.tv_friend_item_uid
                });
        listView.setAdapter(friendsAdapter);
    }
}
