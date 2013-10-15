package net.stefanopallicca.android.awsmonitor;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import net.stefanopallicca.android.awsmonitor.R;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener{
	
	private static final String TAG = "Settings Activity";
    @SuppressWarnings("deprecation")
		@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    	if(pref.equals("pref_server_url") || pref.equals("pref_server_port")){
    		String server_url = prefs.getString("pref_server_url", "");
    		String server_port = prefs.getString("pref_server_port", "");
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