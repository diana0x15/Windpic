package com.wind.windpic.objects;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.wind.windpic.MainActivity;

/**
 * Created by dianapislaru on 12/10/15.
 */
public class Database extends SQLiteOpenHelper {

    private static final String TAG = "Database";
    private static final String DATABASE_NAME = "Database";
    private static final String COLUMN_TEXT = "message";
    private static final String COLUMN_CHAT_ID = "chatId";
    private static final String COLUMN_SENDER_ID = "senderId";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_ID = "friendId";
    private static final String COLUMN_FRIEND_REQUEST = "requests"; // type = 0
    private static final String COLUMN_MESSAGES = "messages"; // type = 1
    private static final String COLUMN_PHOTOS = "photos"; // type = 2

    private SQLiteDatabase mDatabase;
    private static int COUNT = 0;

    public Database(Context context) {
        super(context, DATABASE_NAME, null, 1);
        mDatabase = this.getWritableDatabase();

    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        if (MainActivity.CURRENT_USER == null)
            return;
        Log.i(TAG, "new database");
        String table_name = "'" + MainActivity.CURRENT_USER.getObjectId() + "'";
        database.execSQL("CREATE TABLE " + table_name + " (" +
                COLUMN_TEXT + " TEXT, " +
                COLUMN_CHAT_ID + " TEXT, " +
                COLUMN_SENDER_ID + " TEXT, " +
                COLUMN_DATE + " TEXT)");
        table_name = "notifications" + MainActivity.CURRENT_USER.getObjectId();
        database.execSQL("CREATE TABLE " + table_name + " (" +
                COLUMN_FRIEND_REQUEST + " INT, " +
                COLUMN_MESSAGES + " INT, " +
                COLUMN_PHOTOS + " INT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        if (MainActivity.CURRENT_USER == null)
            return;
        Log.i(TAG, "upgrade");
        String table_name = "'" + MainActivity.CURRENT_USER.getObjectId() + "'";
        mDatabase.execSQL("DROP TABLE IF EXISTS " + table_name);
        onCreate(mDatabase);
    }

    public void addMessage(Message message) {
        if (MainActivity.CURRENT_USER == null)
            return;
        String table_name = "'" + MainActivity.CURRENT_USER.getObjectId() + "'";
        ContentValues value = new ContentValues();
        value.put(COLUMN_TEXT, message.getText());
        value.put(COLUMN_CHAT_ID, message.getChatId());
        value.put(COLUMN_SENDER_ID, message.getSenderId());
        value.put(COLUMN_DATE, message.getDate());
        mDatabase.insert(table_name, null, value);
        COUNT++;
    }

    public Cursor getMessages(String conversationId, int messagesToDisplay) {
        if (MainActivity.CURRENT_USER == null)
            return null;
        String table_name = "'" + MainActivity.CURRENT_USER.getObjectId() + "'";
        Cursor result = mDatabase.rawQuery("SELECT * FROM " + table_name + " WHERE " + COLUMN_CHAT_ID + " LIKE " + "'%" + conversationId + "%'", null);
        return result;
    }

    public Message getLastMessage(String id) {
        String id1 = MainActivity.CURRENT_USER.getObjectId();
        if (MainActivity.CURRENT_USER == null)
            return null;
        String table_name = "'" + MainActivity.CURRENT_USER.getObjectId() + "'";
        String conversationId;
        if (id1.compareTo(id) < 0) {
            conversationId = id1 + id;
        } else {
            conversationId = id + id1;
        }

        Message chatMessage = null;
        Cursor messages = mDatabase.rawQuery("SELECT * FROM " + table_name + " WHERE " + COLUMN_CHAT_ID + " LIKE " + "'%" + conversationId + "%'", null);

        if (messages != null && messages.getCount() != 0) {

            chatMessage = new Message();
            messages.moveToLast();

            chatMessage.setText(messages.getString(0));
            chatMessage.setChatId(messages.getString(1));
            chatMessage.setSenderId(messages.getString(2));
            chatMessage.setDate(messages.getString(3));
        }

        return chatMessage;
    }

    public void setChat(String chatId) {
        if (MainActivity.CURRENT_USER == null)
            return;
        String table_name = "'" + MainActivity.CURRENT_USER.getObjectId() + "'";
        COUNT = mDatabase.rawQuery("SELECT * FROM " + table_name + " WHERE " + COLUMN_CHAT_ID + " LIKE " + "'%" + chatId + "%'", null).getCount();
    }

    public int getCount() {
        return COUNT;
    }

}
