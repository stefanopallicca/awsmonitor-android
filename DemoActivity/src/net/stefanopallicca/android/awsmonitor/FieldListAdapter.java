package net.stefanopallicca.android.awsmonitor;

import java.util.List;

import net.stefanopallicca.android.awsmonitor.GsnServer.VirtualSensor;
import net.stefanopallicca.android.awsmonitor.GsnServer.VirtualSensor.VSField;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class FieldListAdapter extends ArrayAdapter<VSField> {
	public FieldListAdapter(Context context, int textViewResourceId,
			List<VSField> list) {
		super(context, textViewResourceId, list);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		convertView = inflater.inflate(R.layout.field_row, null);
    TextView field_name_tv = (TextView)convertView.findViewById(R.id.field_row_name);
    TextView field_desc_tv = (TextView)convertView.findViewById(R.id.field_row_desc);
		VSField field = getItem(position);
		String field_name = field.getName();
		String field_description = field.getDescription();
		field_name_tv.setText(field_description.equals("") ? field_name : field_description);
		field_desc_tv.setText(field_description.equals("") ? "" : field_name);
		return convertView;
	}
}
