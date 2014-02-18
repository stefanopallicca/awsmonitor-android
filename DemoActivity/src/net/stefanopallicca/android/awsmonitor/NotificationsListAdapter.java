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

public class NotificationsListAdapter extends ArrayAdapter<String> {
	public NotificationsListAdapter(Context context, int textViewResourceId,
			List<String> list) {
		super(context, textViewResourceId, list);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		convertView = inflater.inflate(R.layout.notification_row, null);
		TextView notification_body_tv = (TextView)convertView.findViewById(R.id.notification_row_body);
		String not_body = getItem(position);
		notification_body_tv.setText(not_body);
		return convertView;
	}
}
