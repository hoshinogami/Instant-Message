package sdx.talk.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBOpenHelper extends SQLiteOpenHelper {
    public DBOpenHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String msgSql = "CREATE TABLE `tb_msg`  (" +
                "  id integer primary key autoincrement," +
                "  src integer NOT NULL," +
                " target integer NOT NULL," +
                "  time integer  NOT NULL," +" type integer NOT NULL," +
                "  text varchar(4096) )";
        db.execSQL(msgSql);
        String chatItemSql = "CREATE TABLE `tb_chat`  (" +
                " dataId integer primary key autoincrement," +
                "  id integer NOT NULL," +
                "  chatId integer NOT NULL," +
                "  chatName varchar(120) NOT NULL)";
        db.execSQL(chatItemSql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
