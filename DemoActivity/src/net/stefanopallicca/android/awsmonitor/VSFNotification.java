package net.stefanopallicca.android.awsmonitor;

import net.stefanopallicca.android.awsmonitor.GsnServer.Event;

public class VSFNotification {
	private String vs_name;
	private String field_name;
	private boolean active;
	private Double threshold;
	private Event event;
	
	public VSFNotification(String vs_name, String field_name, Double threshold, Event event, int active){
		this.vs_name = vs_name;
		this.field_name = field_name;
		this.threshold = threshold;
		this.event = event;
		this.active = (active != 0); // int to boolean conversion
	}

	public Double getThreshold() {
		return threshold;
	}

	public Event getEvent() {
		return event;
	}
	
	public boolean getActive(){
		return active;
	}
}
