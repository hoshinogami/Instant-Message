package sdx.talkit.entity;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class Message implements Parcelable,Comparable<Message> {
    private Integer from;
    private Integer to;
    private String text;
    private Long time;
    private int type;

    public Message(Integer from, Integer to, String text, Long time,int type) {
        this.from = from;
        this.to = to;
        this.text = text;
        this.time = time;
        this.type=type;
    }




    public static Message fromJSONObj(JSONObject obj) throws JSONException {
        return new Message(
                obj.getInt("from"),
                obj.getInt("to"),
                obj.getString("text"),
                obj.getLong("time"),
                obj.getInt("type")
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return Objects.equals(from, message.from) &&
                Objects.equals(to, message.to) &&
                Objects.equals(time, message.time);
    }
    @Override
    public int hashCode() {
        return Objects.hash(from, to, text, time);
    }

    public Message() {}

    protected Message(Parcel in) {
        from = in.readInt();
        to = in.readInt();
        text = in.readString();
        time = in.readLong();
        type=in.readInt();
    }

    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getFrom() {
        return from;
    }

    public void setFrom(Integer from) {
        this.from = from;
    }

    public Integer getTo() {
        return to;
    }

    public void setTo(Integer to) {
        this.to = to;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.from);
        dest.writeInt(this.to);
        dest.writeString(this.text);
        dest.writeLong(this.time);
        dest.writeInt(this.type);
    }

    public ContentValues toContentValues(){
        ContentValues values = new ContentValues();
        values.put("src",from);
        values.put("target",to);
        values.put("text",text);
        values.put("time",time);
        values.put("type",type);
        return values;
    }

    public static Message fromJSONStr(String msgStr){
        try {
            JSONObject object = new JSONObject(msgStr);
            Message message = new Message();
            message.from = object.getInt("from");
            message.to = object.getInt("to");
            message.text = object.getString("text");
            message.time = object.getLong("time");
            message.type=object.getInt("type");
            return message;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String toJSONStr(){
        return "{\"from\":" + from +",\"to\":" + to + ",\"text\":\"" + text + "\",\"time\":" + time +",\"type\":" +type+ "}";
    }

    @Override
    public String toString() {
        return "Message{" +
                "from=" + from +
                ", to=" + to +
                ", text='" + text + '\'' +
                ", time=" + time +
                ", type=" + type +
                '}';
    }

    @Override
    public int compareTo(Message msg) {
        return (int) (this.getTime() - msg.getTime());
    }
}
