package net.stefanopallicca.android.awsmonitor;

import java.util.Map;

import org.apache.http.HttpException;
import org.apache.http.conn.ConnectTimeoutException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import net.stefanopallicca.android.awsmonitor.R;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener{
	
	private SharedPreferences oldPrefs;
	private String oldServerURL;
	private String oldServerPort;
	Context context;
	
	private static final String TAG = "Settings Activity";
    @SuppressWarnings("deprecation")
		@Override
    public void onCreate(Bundle savedInstanceState) {
    	context = this;
      super.onCreate(savedInstanceState);
      oldPrefs = PreferenceManager.getDefaultSharedPreferences(this);
      oldServerURL = oldPrefs.getString("pref_server_url", "");
      oldServerPort = oldPrefs.getString("pref_server_port", "");
      addPreferencesFromResource(R.xml.preferences);
        
    }
    
    /*public void onStop(){
    	Intent resultIntent = new Intent();
    	SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
    	if(sharedPref.getString("pref_server_url", "") != "")
    		setResult(Activity.RESULT_OK, resultIntent);
    	else
    		setResult(Activity.RESULT_CANCELED, resultIntent);
    	finish();
    }*/

		@Override
		public void onSharedPreferenceChanged(SharedPreferences prefs, String pref) {
			Intent resultIntent = new Intent();
			
			String regid = getSharedPreferences(MainActivity.class.getSimpleName(),
            Context.MODE_PRIVATE).getString(MainActivity.PROPERTY_REG_ID, "");
			
  		String server_url = prefs.getString("pref_server_url", "");
  		String server_port = prefs.getString("pref_server_port", "");
  		
  		if(!oldServerURL.equals(server_url) && oldServerPort.length() > 0){
  			
  			GsnServer oldServer = new GsnServer(oldServerURL, Integer.valueOf(oldServerPort));
  			try {
					if(!regid.equals("") && oldServer.checkDeviceRegistration(regid)){
						Log.i("SETTINGS", "Unregistering device");
						oldServer.unregisterDevice(regid);
						Log.i("SETTINGS", "Deleting previous preferences from local DB");
						NotificationsDatasource nd = new NotificationsDatasource(context.getApplicationContext());
						//NotificationsDatasource nd = NotificationsDatasource.getInstance(context);
						nd.open();
						nd.RemoveNotificationsForServer(oldServerURL, Integer.valueOf(oldServerPort));
					}
				} catch (ConnectTimeoutException e) {
					
				} catch (HttpException e) {
					
				}
  		}
    	if(pref.equals("pref_server_url") || pref.equals("pref_server_port")){
    		if(server_port.equals("")){
    			Log.i(TAG, "PORT EMPTY");
    			prefs.edit().putString("pref_server_port", "22001").commit();
    		}
    		if(!server_url.equals("")){
    			Log.i(TAG, "Result OK");
    			setResult(Activity.RESULT_OK, resultIntent);
    		}
    		else{
    			Log.i(TAG, "Result cancelled");
    			setResult(Activity.RESULT_CANCELED, resultIntent);
    		}
    	}
		}
		
		@SuppressWarnings("deprecation")
		@Override
		protected void onResume() {
		    super.onResume();
		    getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		}

		@SuppressWarnings("deprecation")
		@Override
		protected void onPause() {
		    super.onPause();
		    getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		}
}