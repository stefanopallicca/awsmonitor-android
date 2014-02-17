package net.stefanopallicca.android.awsmonitor;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper {

  public static final String TABLE_NOTIFICATIONS = "notifications";
  public static final String COLUMN_ID = "_id";
  public static final String COLUMN_URL = "url";
  public static final String COLUMN_PORT = "port";
  public static final String COLUMN_VSNAME = "vs_name";
  public static final String COLUMN_FIELDNAME = "field_name";
  public static final String COLUMN_THRESHOLD = "threshold";
  public static final String COLUMN_EVENT = "event";
  public static final String COLUMN_ACTIVE = "active";

  public static final String DATABASE_NAME = "notifications.db";
  public static final int DATABASE_VERSION = 2;
  
  
  public static final String TABLE_RECEIVED = "recv_notif"; // received notifications table
  public static final String RECV_COLUMN_ID = "notification_id";
  public static final String RECV_COLUMN_RECV_TIME = "recv_time";
  public static final String RECV_COLUMN_BODY = "body";
  public static final String RECV_CREATE = "create table " + TABLE_RECEIVED + "("
  		+ RECV_COLUMN_ID + " integer primary key autoincrement, "
  		+ RECV_COLUMN_RECV_TIME + " integer not null, "
  		+ RECV_COLUMN_BODY + " text not null);";
  private static MySQLiteHelper sInstance;
  
  // Database creation sql statement
  private static final String DATABASE_CREATE = "create table "
      + TABLE_NOTIFICATIONS + "(" 
  		+ COLUMN_ID + " integer primary key autoincrement, "
  		+ COLUMN_URL + " text not null, "
  		+ COLUMN_PORT + " integer not null, "
      + COLUMN_VSNAME + " text not null, "
      + COLUMN_FIELDNAME + " text not null, "
      + COLUMN_THRESHOLD + " real not null, "
      + COLUMN_EVENT + " text not null, "
      + COLUMN_ACTIVE + " integer not null);";
  
  public static MySQLiteHelper getInstance(Context context) {

    // Use the application context, which will ensure that you 
    // don't accidentally leak an Activity's context.
    // See this article for more information: http://bit.ly/6LRzfx
    if (sInstance == null) {
      sInstance = new MySQLiteHelper(context.getApplicationContext());
    }
    return sInstance;
  }

  /**
   * Constructor should be private to prevent direct instantiation.
   * make call to static factory method "getInstance()" instead.
   */
  private MySQLiteHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }
  
  /*public MySQLiteHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }*/

  @Override
  public void onCreate(SQLiteDatabase database) {
    database.execSQL(DATABASE_CREATE);
    database.execSQL(RECV_CREATE);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    Log.w(MySQLiteHelper.class.getName(),
        "Upgrading database from version " + oldVersion + " to "
            + newVersion + ", which will destroy all old data");
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATIONS);
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECEIVED);
    onCreate(db);
  }

} 