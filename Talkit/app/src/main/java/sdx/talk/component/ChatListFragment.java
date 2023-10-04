package sdx.talk.component;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import sdx.talk.R;
import sdx.talk.dao.DB;
import sdx.talk.entity.ChatItem;
import sdx.talk.entity.Message;
import sdx.talk.page.TalkActivity;

public class ChatListFragment extends Fragment  {
    private ArrayList<ChatItem> chatItems;
    private Integer selfId;
    private ChatListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle bundle) {
        return inflater.inflate(R.layout.fragment_chat_list, container, false);
    }

    public static ChatListFragment newInstance(ArrayList<ChatItem> chatItems, Integer selfId){
        ChatListFragment fg = new ChatListFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("chatItems",chatItems);
        bundle.putInt("selfId",selfId);
        fg.setArguments(bundle);
        return fg;
    }

    @Override
    public void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);

        this.chatItems = getArguments().getParcelableArrayList("chatItems");
        this.selfId = getArguments().getInt("selfId");
        adapter = new ChatListAdapter(chatItems,this.getActivity());
    }

    @Override
    public void onStart() {
        super.onStart();
        ListView listView = getActivity().findViewById(R.id.chat_list_view);
        if(listView.getAdapter() == null){
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(getActivity(),TalkActivity.class);

                    ChatItem item = chatItems.get(position);
                    ArrayList<Message> msgList = item.getMsgList();
                    ArrayList<Message> dbList = DB.fetchMessage(ChatListFragment.this.selfId, item.getId());
                    for (Message msg : dbList) {
                        boolean exist = false;
                        for (Message addedMsg : msgList) {
                            if (addedMsg.equals(msg)){
                                exist = true;
                                break;
                            }
                        }
                        if (!exist){
                            msgList.add(msg);
                        }
                    }
                    msgList.sort(Message::compareTo);

                    intent.putExtra("item",item);
                    intent.putExtra("selfId",selfId);
                    startActivity(intent);
                    chatItems.get(position).alreadyRead();
                    update();
                }
            });
        }
        update();
    }

    public void update(){
        chatItems.sort(ChatItem::compareTo);
        adapter.notifyDataSetChanged();
    }
}
