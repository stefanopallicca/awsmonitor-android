package net.stefanopallicca.android.awsmonitor;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper {

  public static final String TABLE_NOTIFICATIONS = "notifications";
  public static final String COLUMN_ID = "_id";
  public static final String COLUMN_VSNAME = "vs_name";
  public static final String COLUMN_FIELDNAME = "field_name";
  public static final String COLUMN_THRESHOLD = "threshold";
  public static final String COLUMN_EVENT = "event";
  public static final String COLUMN_ACTIVE = "active";

  private static final String DATABASE_NAME = "notifications.db";
  private static final int DATABASE_VERSION = 1;

  // Database creation sql statement
  private static final String DATABASE_CREATE = "create table "
      + TABLE_NOTIFICATIONS + "(" 
  		+ COLUMN_ID + " integer primary key autoincrement, "
      + COLUMN_VSNAME + " text not null, "
      + COLUMN_FIELDNAME + " text not null, "
      + COLUMN_THRESHOLD + " real not null, "
      + COLUMN_EVENT + " text not null, "
      + COLUMN_ACTIVE + " integer not null);";

  public MySQLiteHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase database) {
    database.execSQL(DATABASE_CREATE);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    Log.w(MySQLiteHelper.class.getName(),
        "Upgrading database from version " + oldVersion + " to "
            + newVersion + ", which will destroy all old data");
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATIONS);
    onCreate(db);
  }

} 