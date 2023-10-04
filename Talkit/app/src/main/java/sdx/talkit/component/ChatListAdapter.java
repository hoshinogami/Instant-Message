package sdx.talkit.component;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import sdx.talkit.entity.ChatItem;
import sdx.talkit.R;

public class ChatListAdapter extends BaseAdapter {
    private ArrayList<ChatItem> list;
    private Context context;

    public ChatListAdapter(ArrayList<ChatItem> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return list.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ChatItemHolder holder = null;
        if (convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.adapter_chat_item,parent,false);
            holder = new ChatItemHolder();
            holder.tv_userName = convertView.findViewById(R.id.tv_user_name);
            holder.tv_lastMsg = convertView.findViewById(R.id.tv_last_msg_text);
            holder.tv_lastMsgTime = convertView.findViewById(R.id.tv_last_msg_time);
            holder.img_unread = convertView.findViewById(R.id.img_unread);
            convertView.setTag(holder);
        }else {
            holder = (ChatItemHolder) convertView.getTag();
        }
        ChatItem item = list.get(position);
        holder.tv_userName.setText(item.getName());
        holder.tv_lastMsgTime.setText(item.getLastMsgTime());
        holder.tv_lastMsg.setText( item.getMsgList().size() > 0 ?
                item.getMsgList().get(item.getMsgList().size() - 1).getText() : "-");
        holder.img_unread.setVisibility(item.getUnread() ? View.VISIBLE : View.INVISIBLE);
        return convertView;
    }

    private static class ChatItemHolder{
        TextView tv_userName;
        TextView tv_lastMsgTime;
        TextView tv_lastMsg;
        ImageView img_unread;
    }
}
