package net.stefanopallicca.android.awsmonitor;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import net.stefanopallicca.android.awsmonitor.GsnServer;
import net.stefanopallicca.android.awsmonitor.GsnServer.VirtualSensor;
import net.stefanopallicca.android.awsmonitor.GsnServer.VirtualSensor.VSField;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

public class VirtualSensorActivity extends ListActivity {
  //private final String TAG = "VirtualSensorActivity";
  private GsnServer server = null;
  private VirtualSensor vs = null;
	
	public void onCreate(Bundle savedInstanceState) {
  	super.onCreate(savedInstanceState);
  	
  	Intent intent = getIntent();
  	server = intent.getParcelableExtra("ServerParcel");
  	int vs_index = intent.getIntExtra("vs_index", -1);
  	vs = server.virtualSensors.get(vs_index);
  	setTitle(server.virtualSensors.get(vs_index).getName());
  	List<VSField> list = new LinkedList<GsnServer.VirtualSensor.VSField>();
  	String[] skipfields = new String[]{"altitude", "geographical", 
  			"latitude", "longitude", "name", "record", "timed"}; // may add more, but values MUST BE in alphabetical order
  	VSField field = null;
  	for(int i = 0; i < vs.getNumFields(); i++){
  		field = vs.fields.get(i);
  		if(Arrays.binarySearch(skipfields, field.getName()) >= 0)
  			continue;
  		list.add(field);
  	}
  	FieldListAdapter fl_adapter = new FieldListAdapter(this, R.layout.field_row, list);
  	ListView listView = getListView();
  	listView.setAdapter(fl_adapter);
	}
	
	@Override  
	protected void onListItemClick(ListView l, View v, int pos, long id) {  
		super.onListItemClick(l, v, pos, id);
		VSField field = (VSField) getListView().getItemAtPosition(pos);
		Intent i = new Intent(this, VSFieldActivity.class);
		i.putExtra("ServerParcel", server);
		i.putExtra("vs_index", vs.getIndex());
		i.putExtra("field_index", vs.getFieldIndexByName(field.getName()));
		startActivity(i);
	}
}
