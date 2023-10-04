package sdx.talk.component;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import sdx.talk.R;
import sdx.talk.entity.Message;
import sdx.talk.util.DateFormatter;

public class MessageListAdapter extends BaseAdapter {
    private ArrayList<Message> list;
    private Context context;
    private Integer selfId;
    private final DateFormatter formatter = new DateFormatter();

    private static final String TAG = "MessageListAdapter";

    public MessageListAdapter(ArrayList<Message> list, Context context, Integer selfId) {
        this.list = list;
        this.context = context;
        this.selfId = selfId;
    }

    public MessageListAdapter() {}

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
        return list.get(position).getTime();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MessageHolder holder = new MessageHolder();
        Message message = list.get(position);
        Log.i(TAG, "getView()  " + message.toString());
        if (message.getFrom().equals(this.selfId)){
            if(message.getType()==1){
                convertView = LayoutInflater.from(context).inflate(R.layout.message_self_img,parent,false);
                holder.tv_image=convertView.findViewById(R.id.tv_self_message_img);
                holder.tv_time=convertView.findViewById(R.id.tv_self_message_img_time);
                displayImage(message.getText(), holder.tv_image);
            }else{
                convertView = LayoutInflater.from(context).inflate(R.layout.message_self_item,parent,false);
                holder.tv_text = convertView.findViewById(R.id.tv_self_message_text);
                holder.tv_time = convertView.findViewById(R.id.tv_self_message_time);
                holder.tv_text.setText(message.getText());
            }

        }else{
            if(message.getType()==1){
                convertView = LayoutInflater.from(context).inflate(R.layout.message_other_img,parent,false);
                holder.tv_image=convertView.findViewById(R.id.tv_other_message_img);
                holder.tv_time=convertView.findViewById(R.id.tv_other_message_img_time);
                displayImage(message.getText(), holder.tv_image);
            }else {
                convertView = LayoutInflater.from(context).inflate(R.layout.message_other_item, parent, false);
                holder.tv_text = convertView.findViewById(R.id.tv_other_message_text);
                holder.tv_time = convertView.findViewById(R.id.tv_other_message_time);
                holder.tv_text.setText(message.getText());
            }
        }
        if(message.getType()==0) {
            if (position > 0) {
                Message lastMsg = list.get(position - 1);
                if (message.getTime() - lastMsg.getTime() > 60 * 1000) {
                    holder.tv_time.setText(formatter.fromTimeStamp(message.getTime()));
                } else {
                    holder.tv_time.setVisibility(View.GONE);
                }
            } else {
                holder.tv_time.setText(formatter.fromTimeStamp(message.getTime()));
            }
        }
        return convertView;
    }

    private static class MessageHolder{
        private TextView tv_time;
        private TextView tv_text;
        private ImageView tv_image;
    }
    //显示图片
    private void displayImage(String imagePath,ImageView img){
        if(imagePath!=null){
            Bitmap bitmap= BitmapFactory.decodeFile(imagePath);
            img.setImageBitmap(bitmap);//将图片放置在控件上
        }else {
        }
    }
}
