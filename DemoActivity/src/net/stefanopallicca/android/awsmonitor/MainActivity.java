/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.stefanopallicca.android.awsmonitor;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.R.bool;
import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import net.stefanopallicca.android.awsmonitor.GsnServer.VirtualSensor;
import net.stefanopallicca.android.awsmonitor.R;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Main UI for the demo app.
 */
public class MainActivity extends ListActivity{

    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "0.4";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    protected SharedPreferences _sharedPref;
    //private static final String GSN_URL = "http://stefanopallicca.net:22001";
    
    
    private GsnServer server = null;
    /**
     * Project number in Google console
     */
    String SENDER_ID = "762668984695";

    /**
     * Tag used on log messages.
     */
    static final String TAG = "GCM Demo";

    TextView mDisplay;
    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    static Context context;

    String regid;

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	this._sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
    	  		
    	if(_sharedPref.getString("pref_server_url", "") == ""){
    		Intent intent = new Intent(this, SettingsActivity.class);
				this.startActivityForResult(intent, 1);
    	}
    	else
    		this.launchMainApp();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check device for Play Services APK.
        checkPlayServices();
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Stores the registration ID and the app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGcmPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    /**
     * Gets the current registration ID for application on GCM service, if there is one.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
    	final SharedPreferences prefs = getGcmPreferences(context);
      String registrationId = prefs.getString(PROPERTY_REG_ID, "");
      if (registrationId.isEmpty()) {
      	Log.i(TAG, "Registration not found.");
          return "";
      }
      // Check if app was updated; if so, it must clear the registration ID
      // since the existing regID is not guaranteed to work with the new
      // app version.
      int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
      int currentVersion = getAppVersion(context);
      if (registeredVersion != currentVersion) {
      	Log.i(TAG, "App version changed.");
      	return "";
      }
      return registrationId;
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences.
     * @return 
     */
    private void registerInBackground() {
    	new AsyncTask<Void, Void, String>() {
    		
    		ProgressDialog pd;
    		
    		@Override
    		protected void onPreExecute(){
    			pd = new ProgressDialog(MainActivity.this);
    			pd.setTitle("Processing...");
    			pd.setMessage("Please wait.");
    			pd.setCancelable(false);
    			pd.setIndeterminate(true);
    			pd.show();
    		}
    		
    		@Override
        protected String doInBackground(Void... params) {
    			String msg = "";
          try {
          	if (gcm == null) {
          		gcm = GoogleCloudMessaging.getInstance(context);
            }
            regid = gcm.register(SENDER_ID);
            //msg = "Device registered, registration ID=" + regid;
            
            // Persist the regID - no need to register again.
            
            storeRegistrationId(context, regid);
            /**
             * Send regid to GSN server
             */
            try {
							if(server.sendRegistrationIdToBackend(regid))
								msg = "ok";
							else msg = "ko";
						} catch (HttpException e) {
							msg = "ko";
						}
          } catch (IOException ex) {
          	msg = "ko";
          }
          return msg;
    		}

				@Override
				protected void onPostExecute(String msg) {
					//mDisplay.append(msg + "\n");
					if(msg == "ok"){
  		    	Context context = getApplicationContext();
  		    	CharSequence text = getString(R.string.device_registered) + " " + MainActivity.this._sharedPref.getString("pref_server_url", "")+":"+MainActivity.this._sharedPref.getString("pref_server_port", "");
  		    	int duration = Toast.LENGTH_LONG;

  		    	Toast toast = Toast.makeText(context, text, duration);
  		    	toast.show();
					}
					else{
  		    	Context context = getApplicationContext();
  		    	CharSequence text = getString(R.string.error_connecting) + " " + MainActivity.this._sharedPref.getString("pref_server_url", "")+":"+MainActivity.this._sharedPref.getString("pref_server_port", "");
  		    	int duration = Toast.LENGTH_LONG;

  		    	Toast toast = Toast.makeText(context, text, duration);
  		    	toast.show();
					}
					pd.cancel();
					launchMainApp();
				}
				}.execute(null, null, null);
    }

    // Send an upstream message.
    /*public void onClick(final View view) {

        if (view == findViewById(R.id.send)) {
            new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... params) {
                    String msg = "";
                    try {
                        Bundle data = new Bundle();
                        data.putString("my_message", "Hello World");
                        data.putString("my_action", "com.google.android.gcm.demo.app.ECHO_NOW");
                        String id = Integer.toString(msgId.incrementAndGet());
                        gcm.send(SENDER_ID + "@gcm.googleapis.com", id, data);
                        msg = "Sent message";
                    } catch (IOException ex) {
                        msg = "Error :" + ex.getMessage();
                    }
                    return msg;
                }

                @Override
                protected void onPostExecute(String msg) {
                    mDisplay.append(msg + "\n");
                }
            }.execute(null, null, null);
        } else if (view == findViewById(R.id.clear)) {
            mDisplay.setText("");
        }
    }*/

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGcmPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
 
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
				if (itemId == R.id.menu_settings) {
					Intent i = new Intent(this, SettingsActivity.class);
					startActivityForResult(i, 1);
				}
 
        return true;
    }
    	
    public void launchMainApp(){
      setContentView(R.layout.main);
      //mDisplay = (TextView) findViewById(R.id.display);
      
      context = getApplicationContext();

      // Check device for Play Services APK. If check succeeds, proceed with GCM registration.
      if (checkPlayServices()) {
          gcm = GoogleCloudMessaging.getInstance(this);
          regid = getRegistrationId(context);
          server = new GsnServer( _sharedPref.getString("pref_server_url", ""),  Integer.parseInt(_sharedPref.getString("pref_server_port", "")));

          /*if (regid.isEmpty()) {
              registerInBackground();
          }
          else {
          	Log.i(TAG, "This version has already been registered to GCM "+regid);
          }*/
          boolean registered_to_gsnserver = false;
          try{
          	registered_to_gsnserver = server.checkDeviceRegistration(regid);
          	if(!registered_to_gsnserver)
          		registered_to_gsnserver = server.sendRegistrationIdToBackend(regid);
          } catch (ConnectTimeoutException e){
  		    	int duration = Toast.LENGTH_LONG;
  		    	
  		    	Toast toast = Toast.makeText(context, getString(R.string.connect_timeout)+" "+server.getURL()+":"+server.getPort(), duration);
  		    	toast.show();
          } catch (HttpException e) {
  		    	int duration = Toast.LENGTH_LONG;
  		    	
  		    	Toast toast = Toast.makeText(context, getString(R.string.service_not_found), duration);
  		    	toast.show();
					}
          if(!regid.isEmpty() && registered_to_gsnserver){
          	Log.i(TAG, "This device has been registered to GSN server");
          	//mDisplay.append("Device ready to receive notifications from GSN server " + _sharedPref.getString("pref_server_url", ""));
          	server.getSummary();
            setContentView(R.layout.main);
            ListView listView = getListView();
            mDisplay = (TextView) findViewById(R.id.main_header);
            mDisplay.append(server.getName());
            List<VirtualSensor> list = new LinkedList<VirtualSensor>();
            for(int i = 0; i < server.virtualSensors.size(); i++){
            	if(server.virtualSensors.get(i).getNumFields() > 0)
            		list.add(server.virtualSensors.get(i));
            }
            VirtualSensorListAdapter adapter = new VirtualSensorListAdapter(this, R.layout.vs_row, list);
            listView.setAdapter(adapter);

          }
          else{
          	//mDisplay.append("This device is not ready to receive notifications from a GSN server. Go to settings to configure a GSN server");
          }
      } else {
          Log.i(TAG, "No valid Google Play Services APK found.");
      }
    }
    
    /**
     * Called when {@code SettingsActivity} is closed.
     */
		protected void onActivityResult(int requestCode, int resultCode, Intent data) {
			context = getApplicationContext();
    	if (requestCode == 1) {
    		if(resultCode == RESULT_OK){
    			String regid = getRegistrationId(this);
    			server = new GsnServer(_sharedPref.getString("pref_server_url", ""),  Integer.parseInt(_sharedPref.getString("pref_server_port", "")));
    			if( regid != ""){
    				boolean regOk;
    				Context context = getApplicationContext();
    				CharSequence text = "";
						try {
							regOk = server.sendRegistrationIdToBackend(regid);
							if(regOk){
		  		    	text = getString(R.string.device_registered) + " " + MainActivity.this._sharedPref.getString("pref_server_url", "")+":"+MainActivity.this._sharedPref.getString("pref_server_port", "");
							}
							else{
		  		    	text = getString(R.string.error_connecting) + " " + MainActivity.this._sharedPref.getString("pref_server_url", "")+":"+MainActivity.this._sharedPref.getString("pref_server_port", "");
							}
						} catch (HttpException e) {
								text = getString(R.string.service_not_found);
						}
  		    	int duration = Toast.LENGTH_LONG;
  		    	
  		    	Toast toast = Toast.makeText(context, text, duration);
  		    	toast.show();
  		    	
  		    	launchMainApp();
    			}
    			else{
    				registerInBackground();
    			}
    			
    			
    		}
    		if (resultCode == RESULT_CANCELED) {    
			  }
			}
    }//onActivityResult
		
		@Override  
		protected void onListItemClick(ListView l, View v, int pos, long id) {  
			super.onListItemClick(l, v, pos, id);
			VirtualSensor vs = (VirtualSensor) getListView().getItemAtPosition(pos);
			Intent i = new Intent(this, VirtualSensorActivity.class);
			i.putExtra("ServerParcel", server);
			i.putExtra("vs_index", server.getVSIndexByName(vs.getName()));
			startActivity(i);
		}

		public static Context getContext() {
			return context;
		}  
}
