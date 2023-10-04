package sdx.talkit.entity;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Objects;

public class ChatItem implements Parcelable,Comparable<ChatItem> {
    private Integer id;
    private String name;
    //0是文字，1是图片
    //private Integer type;
    private String lastMsgTime;
    private Boolean unread;
    private ArrayList<Message> msgList;
    //private String imguri;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatItem item = (ChatItem) o;
        return Objects.equals(id, item.id);
    }

    protected ChatItem(Parcel in) {
        id = in.readInt();
        name = in.readString();
        lastMsgTime = in.readString();
        byte tmpUnread = in.readByte();
        unread = tmpUnread == 1;
        msgList = new ArrayList<>();
        in.readTypedList(msgList,Message.CREATOR);
    }

    public ChatItem(Integer id, String name, String lastMsgTime, Boolean unread, ArrayList<Message> msgList) {
        this.id = id;
        this.name = name;
        this.lastMsgTime = lastMsgTime;
        this.unread = unread;
        this.msgList = msgList;
    }

    public ChatItem(){}

    public static final Creator<ChatItem> CREATOR = new Creator<ChatItem>() {
        @Override
        public ChatItem createFromParcel(Parcel in) {
            return new ChatItem(in);
        }
        @Override
        public ChatItem[] newArray(int size) {
            return new ChatItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(lastMsgTime);
        dest.writeBoolean(unread);
        dest.writeTypedList(msgList);
    }

    @Override
    public int compareTo(ChatItem other) {
        return this.getUnread() && !other.getUnread() ? -1 :
                !this.getUnread() && other.getUnread() ? 1 :
                        (int)(other.lastMsgTimeStamp() - this.lastMsgTimeStamp());
    }

    public void alreadyRead(){
        this.unread = false;
    }

    public long lastMsgTimeStamp(){
        return this.msgList == null || this.msgList.size() == 0 ? 0 : msgList.get(msgList.size()-1).getTime();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastMsgTime() {
        return lastMsgTime;
    }

    public void setLastMsgTime(String lastMsgTime) {
        this.lastMsgTime = lastMsgTime;
    }

    public Boolean getUnread() {
        return unread;
    }

    public void setUnread(Boolean unread) {
        this.unread = unread;
    }

    public ArrayList<Message> getMsgList() {
        return msgList;
    }

    public void setMsgList(ArrayList<Message> msgList) {
        this.msgList = msgList;
    }


    @Override
    public String toString() {
        return "ChatItem{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", lastMsgTime='" + lastMsgTime + '\'' +
                ", unread=" + unread +
                ", msgList=" + msgList +
                '}';
    }
}
