package sdx.talk.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

import sdx.talk.util.DateFormatter;
import sdx.talk.entity.ChatItem;
import sdx.talk.entity.Message;

public class DB {
    private DB(){}

    public static final String DB_FILE_NAME = "talkit_db.db3";
    public static final String MSG_TABLE_NAME = "tb_msg";
    public static final String CHAT_ITEM_NAME = "tb_chat";

    private static DBOpenHelper dbOpenHelper;
    public static SQLiteDatabase writable_db;
    public static SQLiteDatabase readonly_db;
    public static void init(Context context){
        dbOpenHelper = new DBOpenHelper(context,context.getFilesDir() + "/" + DB_FILE_NAME,null,1);
        writable_db = dbOpenHelper.getWritableDatabase();
        readonly_db = dbOpenHelper.getReadableDatabase();
    }

    public static void insertMessage(Message msg){
        writable_db.insert(MSG_TABLE_NAME,"id",msg.toContentValues());
    }
    public static ArrayList<Message> fetchMessage(Integer id,Integer chatId){
        Cursor cursor = readonly_db.query(MSG_TABLE_NAME,
                new String[]{"src", "target", "text", "time","type"},
                "(target = ? and src = ?) or (src = ? and target = ?)",
                new String[]{
                        id.toString(),
                        chatId.toString(),
                        id.toString(),
                        chatId.toString()
                },
                null, null, null);
        ArrayList<Message> list = new ArrayList<>();
        while (cursor.moveToNext()){
            list.add(new Message(
                    cursor.getInt(cursor.getColumnIndex("src")),
                    cursor.getInt(cursor.getColumnIndex("target")),
                    cursor.getString(cursor.getColumnIndex("text")),
                    cursor.getLong(cursor.getColumnIndex("time")),
                    cursor.getInt(cursor.getColumnIndex("type"))));

        }
        Log.i("DB", "fetchMessage: " + list.toString());
        cursor.close();
        return list;
    }
    public static ArrayList<ChatItem> fetchChatItem(Integer id){
        Cursor cursor = readonly_db.query(CHAT_ITEM_NAME, new String[]{"chatId","chatName"}, "id = ?", new String[]{id.toString()}, null, null, null);
        ArrayList<Integer> idList = new ArrayList<>();
        ArrayList<String> nameList = new ArrayList<>();
        while (cursor.moveToNext()){
            idList.add(cursor.getInt(cursor.getColumnIndex("chatId")));
            nameList.add(cursor.getString(cursor.getColumnIndex("chatName")));
        }
        cursor.close();
        ArrayList<ChatItem> chatItems = new ArrayList<>();
        DateFormatter formatter = new DateFormatter();
        for (int i = 0; i < idList.size(); i++) {
            int chatId = idList.get(i);
            ArrayList<Message> msgList = fetchMessage(id,chatId);
            chatItems.add(new ChatItem(chatId,nameList.get(i),
                    msgList.size() == 0 ? "-" : formatter.fromTimeStamp(msgList.get(msgList.size()-1).getTime()),
                    false,msgList));
        }
        return chatItems;
    }
    public static void insertChatItem(Integer id,Integer chatId,String chatName){
        ContentValues values = new ContentValues();
        values.put("id",id);
        values.put("chatId",chatId);
        values.put("chatName",chatName);
        writable_db.insert(CHAT_ITEM_NAME,"dataId",values);
    }
}
