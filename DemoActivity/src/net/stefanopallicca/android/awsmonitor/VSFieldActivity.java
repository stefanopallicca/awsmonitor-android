package net.stefanopallicca.android.awsmonitor;

import org.apache.http.HttpException;

import net.stefanopallicca.android.awsmonitor.GsnServer.Event;
import net.stefanopallicca.android.awsmonitor.GsnServer.VirtualSensor;
import net.stefanopallicca.android.awsmonitor.GsnServer.VirtualSensor.VSField;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

public class VSFieldActivity extends Activity {
	private GsnServer server = null;
	private VirtualSensor vs = null;
	private VSField field = null;
	
	private ProgressDialog pd;
	private View v;
	private Context context;
	
	private NotificationsDatasource datasource;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		datasource = new NotificationsDatasource(this);
    datasource.open();
    
		Intent intent = getIntent();
  	server = intent.getParcelableExtra("ServerParcel");
  	int vs_index = intent.getIntExtra("vs_index", -1);
  	int field_index = intent.getIntExtra("field_index", -1);
  	vs = server.virtualSensors.get(vs_index);
  	field = vs.fields.get(field_index);
  	setTitle(vs.getName()+" - "+field.getName());
		setContentView(R.layout.activity_vsfield);
		// Show the Up button in the action bar.
		//setupActionBar();
		Spinner spinner = (Spinner) findViewById(R.id.filter_spinner);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
				R.array.field_filter, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner.setAdapter(adapter);
		onCheckboxClicked(findViewById(R.id.checkbox_notification));
		
		VSFNotification notif = datasource.getNotification(server.getURL(), server.getPort(), vs.getName(), field.getName());
		if(notif != null){
			Log.i("INFO", notif.getThreshold().toString());
			((EditText) findViewById(R.id.text_threshold)).setText(notif.getThreshold().toString());
			Event event = notif.getEvent();
			spinner.setSelection(event.ordinal());
			if(notif.getActive()){
				((CheckBox)findViewById(R.id.checkbox_notification)).setChecked(false);
				findViewById(R.id.checkbox_notification).performClick();
			}
		}
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.vsfield, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void onCheckboxClicked(View view){
    // Is the view now checked?
    boolean checked = ((CheckBox) view).isChecked();
    
		int id = view.getId();
		if (id == R.id.checkbox_notification) {
			findViewById(R.id.filter_spinner).setEnabled(checked);
			findViewById(R.id.text_threshold).setEnabled(checked);
		}
	}
	
	public void saveSetting(View view){
		//view.setEnabled(false);
		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
			@Override
			protected void onPreExecute() {
				
				pd = new ProgressDialog(context);
				pd.setTitle("Processing...");
				pd.setMessage("Please wait.");
				pd.setCancelable(false);
				pd.setIndeterminate(true);
				pd.show();
			}
			
			@Override
			protected Void doInBackground(Void... params) {
				try{
					Double threshold = Double.valueOf(((EditText)findViewById(R.id.text_threshold)).getText().toString());
					Event event = Event.valueOf(((Spinner)findViewById(R.id.filter_spinner)).getSelectedItem().toString().toUpperCase());
					if(((CheckBox)findViewById(R.id.checkbox_notification)).isChecked()){
						boolean register_ok = server.registerToNotification(
								getSharedPreferences(MainActivity.class.getSimpleName(), Context.MODE_PRIVATE).getString(MainActivity.PROPERTY_REG_ID, ""), 
								vs.getName(), field.getName(), 
								threshold, event
								);
						
						if(register_ok)
							datasource.addNotification(server.getURL(), server.getPort(), vs.getName(), field.getName(), threshold, event, true);
					}
					else{
						boolean remove_ok = server.unregisterNotification(
								getSharedPreferences(MainActivity.class.getSimpleName(), Context.MODE_PRIVATE).getString(MainActivity.PROPERTY_REG_ID, ""), 
								vs.getName(), field.getName()
								);
						if(remove_ok)
							datasource.addNotification(server.getURL(), server.getPort(), vs.getName(), field.getName(), threshold, event, false);
					}
				} catch (HttpException e){ }
				return null;
			}
			
			@Override
			protected void onPostExecute(Void result) {
				if (pd!=null) {
					pd.dismiss();
					//view.setEnabled(true);
				}
			}
			
		};
		task.execute((Void[])null);
	}

}
