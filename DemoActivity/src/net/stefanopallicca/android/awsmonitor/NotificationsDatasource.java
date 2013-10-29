package net.stefanopallicca.android.awsmonitor;

import net.stefanopallicca.android.awsmonitor.GsnServer.Event;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class NotificationsDatasource {
  // Database fields
  private SQLiteDatabase database;
  private MySQLiteHelper dbHelper;
  private String[] allColumns = { 
  		MySQLiteHelper.COLUMN_ID,
      MySQLiteHelper.COLUMN_VSNAME,
      MySQLiteHelper.COLUMN_FIELDNAME,
      MySQLiteHelper.COLUMN_THRESHOLD,
      MySQLiteHelper.COLUMN_EVENT,
      MySQLiteHelper.COLUMN_ACTIVE};

  public NotificationsDatasource(Context context) {
    dbHelper = new MySQLiteHelper(context);
  }

  public void open() throws SQLException {
    database = dbHelper.getWritableDatabase();
  }

  public void close() {
    dbHelper.close();
  }
  
  public void addNotification(String vs_name, String field_name, Double threshold, Event event, boolean active){
  	ContentValues values = new ContentValues();
  	values.put(MySQLiteHelper.COLUMN_VSNAME, vs_name);
  	values.put(MySQLiteHelper.COLUMN_FIELDNAME, field_name);
  	values.put(MySQLiteHelper.COLUMN_THRESHOLD, threshold);
  	values.put(MySQLiteHelper.COLUMN_EVENT, event.toString());
  	values.put(MySQLiteHelper.COLUMN_ACTIVE, active ? 1 : 0);
  	Cursor findRow = database.query(MySQLiteHelper.TABLE_NOTIFICATIONS, allColumns, 
  			MySQLiteHelper.COLUMN_VSNAME + " = '" + vs_name + "' AND " +
  			MySQLiteHelper.COLUMN_FIELDNAME + " = '" + field_name + "'", null, null, null, null
  			);
  	if(findRow != null && findRow.moveToFirst()){
	  	int found_id = findRow.getInt(0);
	  	database.update(MySQLiteHelper.TABLE_NOTIFICATIONS, values, MySQLiteHelper.COLUMN_ID + " = " + found_id, null);
	  	Log.i("DATASORUCE", "UPDATE");
  	}
  	else{
	    long insertId = database.insert(MySQLiteHelper.TABLE_NOTIFICATIONS, null, values);
	    /*Cursor cursor = database.query(MySQLiteHelper.TABLE_NOTIFICATIONS,
	        allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
	        null, null, null);*/
  	}
    /*cursor.moveToFirst();
    Comment newComment = cursorToComment(cursor);
    cursor.close();
    return newComment;*/
  }
  
  public VSFNotification getNotification(String vs_name, String field_name, Double threshold, Event event){
  	VSFNotification notification = null;
  	Cursor findRow = database.query(MySQLiteHelper.TABLE_NOTIFICATIONS, allColumns, 
  			MySQLiteHelper.COLUMN_VSNAME + " = '" + vs_name + "' AND " +
  			MySQLiteHelper.COLUMN_FIELDNAME + " = '" + field_name + "' AND " +
  			MySQLiteHelper.COLUMN_THRESHOLD + " = '" + threshold + "' AND " +
  			MySQLiteHelper.COLUMN_EVENT + " = '" + event.toString() + "'", null, null, null, null
  			);
  	if(findRow != null && findRow.moveToFirst()){
	  	notification = cursorToVSFNotification(findRow);
  	}
  	return notification;
  }

	private VSFNotification cursorToVSFNotification(Cursor findRow) {
		VSFNotification notif = null;
		if(findRow.moveToFirst())
			notif = new VSFNotification(findRow.getString(1), findRow.getString(2), findRow.getDouble(3), Event.valueOf(findRow.getString(4)), findRow.getInt(5));
		return notif;
	}

	public VSFNotification getNotification(String vs_name, String field_name) {
  	VSFNotification notification = null;
  	Cursor findRow = database.query(MySQLiteHelper.TABLE_NOTIFICATIONS, allColumns, 
  			MySQLiteHelper.COLUMN_VSNAME + " = '" + vs_name + "' AND " +
  			MySQLiteHelper.COLUMN_FIELDNAME + " = '" + field_name + "'", null, null, null, null
  			);
  	if(findRow != null && findRow.moveToFirst()){
	  	notification = cursorToVSFNotification(findRow);
  	}
  	return notification;
	}
}
