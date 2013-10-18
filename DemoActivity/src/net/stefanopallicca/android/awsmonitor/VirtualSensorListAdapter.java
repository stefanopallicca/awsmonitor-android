package net.stefanopallicca.android.awsmonitor;

import java.util.List;

import net.stefanopallicca.android.awsmonitor.GsnServer.VirtualSensor;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class VirtualSensorListAdapter extends ArrayAdapter<VirtualSensor> {
	public VirtualSensorListAdapter(Context context, int textViewResourceId,
			List<VirtualSensor> list) {
		super(context, textViewResourceId, list);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		convertView = inflater.inflate(R.layout.vs_row, null);
    TextView vs_name = (TextView)convertView.findViewById(R.id.vs_row_name);
    TextView vs_desc = (TextView)convertView.findViewById(R.id.vs_row_desc);
		VirtualSensor vs = getItem(position);
		vs_name.setText(vs.getName());
		vs_desc.setText(vs.getDescription());
		return convertView;
	}
}
