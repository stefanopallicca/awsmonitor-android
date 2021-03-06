package net.stefanopallicca.android.awsmonitor;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import net.stefanopallicca.android.awsmonitor.GsnServer.Event;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.provider.SyncStateContract.Columns;
import android.util.Log;

public class NotificationsDatasource {
  // Database fields
  private SQLiteDatabase database;
  private MySQLiteHelper dbHelper;
  
  private String[] allColumns = { 
  		MySQLiteHelper.COLUMN_ID,
  		MySQLiteHelper.COLUMN_URL,
  		MySQLiteHelper.COLUMN_PORT,
      MySQLiteHelper.COLUMN_VSNAME,
      MySQLiteHelper.COLUMN_FIELDNAME,
      MySQLiteHelper.COLUMN_THRESHOLD,
      MySQLiteHelper.COLUMN_EVENT,
      MySQLiteHelper.COLUMN_ACTIVE};

  public NotificationsDatasource(Context context) {
  	// Trick to have only one copy of the DB open at any time.
  	dbHelper = MySQLiteHelper.getInstance(context);
  }

  public synchronized SQLiteDatabase open() {
	  	if(database == null)
	  		database = dbHelper.getWritableDatabase();
	  	return database;
  }

  public synchronized void close() {
    // do nothing
  }
  
  public void addNotification(String server_url, int server_port, String vs_name, String field_name, Double threshold, Event event, boolean active){
  	ContentValues values = new ContentValues();
  	values.put(MySQLiteHelper.COLUMN_URL, server_url);
  	values.put(MySQLiteHelper.COLUMN_PORT, server_port);
  	values.put(MySQLiteHelper.COLUMN_VSNAME, vs_name);
  	values.put(MySQLiteHelper.COLUMN_FIELDNAME, field_name);
  	values.put(MySQLiteHelper.COLUMN_THRESHOLD, threshold);
  	values.put(MySQLiteHelper.COLUMN_EVENT, event.toString());
  	values.put(MySQLiteHelper.COLUMN_ACTIVE, active ? 1 : 0);
  	Cursor findRow = database.query(MySQLiteHelper.TABLE_NOTIFICATIONS, allColumns,
  			MySQLiteHelper.COLUMN_URL + " = '" + server_url + "' AND " +
  			MySQLiteHelper.COLUMN_PORT + " = '" + server_port + "' AND " +
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
  
  public VSFNotification getNotification(String server_url, int server_port, String vs_name, String field_name, Double threshold, Event event){
  	VSFNotification notification = null;
  	Cursor findRow = database.query(MySQLiteHelper.TABLE_NOTIFICATIONS, allColumns,
  			MySQLiteHelper.COLUMN_URL + " = '" + server_url + "' AND " +
  			MySQLiteHelper.COLUMN_PORT + " = '" + server_port + "' AND " +
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
			notif = new VSFNotification(findRow.getString(1), findRow.getInt(2), findRow.getString(3), findRow.getString(4), findRow.getDouble(5), Event.valueOf(findRow.getString(6)), findRow.getInt(7));
		return notif;
	}

	public VSFNotification getNotification(String server_url, int server_port, String vs_name, String field_name) {
  	VSFNotification notification = null;
  	Cursor findRow = database.query(MySQLiteHelper.TABLE_NOTIFICATIONS, allColumns, 
  			MySQLiteHelper.COLUMN_URL + " = '" + server_url + "' AND " +
  			MySQLiteHelper.COLUMN_PORT + " = '" + server_port + "' AND " +
  			MySQLiteHelper.COLUMN_VSNAME + " = '" + vs_name + "' AND " +
  			MySQLiteHelper.COLUMN_FIELDNAME + " = '" + field_name + "'", null, null, null, null
  			);
  	if(findRow != null && findRow.moveToFirst()){
	  	notification = cursorToVSFNotification(findRow);
  	}
  	return notification;
	}
	
	public void RemoveNotificationsForServer(String server_url, int server_port){
		database.delete(MySQLiteHelper.TABLE_NOTIFICATIONS,
				MySQLiteHelper.COLUMN_URL + " = '" + server_url + "' AND " +
				MySQLiteHelper.COLUMN_PORT + " = '" + server_port + "'", null);
	}
	
	/**
	 * Inserts a new notification in the db of received notifications
	 * 
	 * @param body
	 */
	public void dbAddNotification(String body){
		ContentValues values = new ContentValues();
		int now = (int) (new Date().getTime()/1000);
  	values.put(MySQLiteHelper.RECV_COLUMN_RECV_TIME, now);
  	values.put(MySQLiteHelper.RECV_COLUMN_BODY, body);
		long insertId = database.insert(MySQLiteHelper.TABLE_RECEIVED, null, values); 
	}
	
	@SuppressLint("NewApi")
	public LinkedList<String> getDbNotifications(){
		String[] columns = new String[1];
		columns[0] = MySQLiteHelper.RECV_COLUMN_BODY;
		Cursor findRows = database.query(MySQLiteHelper.TABLE_RECEIVED, columns, null, null, null, null, 
				MySQLiteHelper.RECV_COLUMN_RECV_TIME + " DESC");
		LinkedList<String> list = new LinkedList<String>();
		if(findRows != null && findRows.moveToFirst()){
			do{
				list.add(findRows.getString(0));
				findRows.moveToNext();
			}
			while(!findRows.isLast());
		}
		return list;
	}
}
