package net.stefanopallicca.android.awsmonitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

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
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class GsnServer implements Parcelable {
	private String url = "";
	private int port = 22001;
	
	private String name = "";
	public ArrayList<VirtualSensor> virtualSensors = new ArrayList<VirtualSensor>();
	
	public static final String TAG = "GsnServer";
	
	public enum Event {ABOVE, BELOW};
	
	public GsnServer(String url, int port){
		this.url = url;
		this.port = port;
	}
	
	public GsnServer(Parcel in) {
		url = in.readString();
		port = in.readInt();
		name = in.readString();
		virtualSensors = new ArrayList<VirtualSensor>();
    in.readList(virtualSensors, getClass().getClassLoader());
	}
	
	/*
	 * Connects to GSN server, reads parameters from its XML representation and populates instance virtual sensors.
	 */
	public void getSummary(){
		try {
			URL url = new URL("http://"+this.url+":"+this.port+"/gsn");
			URLConnection conn = url.openConnection();

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(conn.getInputStream());
			
			Element gsn = (Element) doc.getElementsByTagName("gsn").item(0);
			this.name = gsn.getAttribute("name");

			NodeList nodes = doc.getElementsByTagName("virtual-sensor");
			for (int i = 0; i < nodes.getLength(); i++) {
				this.virtualSensors.add(new VirtualSensor((Element) nodes.item(i), i));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getName(){
		return this.name;
	}
	
	/**
	 * Gets a list of names of the virtual sensors having at least one field defined
	 * 
	 * @return ArrayList<String> containing VS names. Empty array if no VS present 
	 */
	public ArrayList<String> getVirtualSensorWithFieldsName(){
		ArrayList<String> ret = new ArrayList<String>();
		for(int i = 0; i < this.virtualSensors.size(); i++){
			if(this.virtualSensors.get(i).fields.size() > 0)
				ret.add(this.virtualSensors.get(i).getName());
		}
		return ret;
	}
	
	public Map<String, String> getVirtualSensorWithFieldsNameAndDesc(){
		Map<String, String> ret = null;
		for(int i = 0; i < this.virtualSensors.size(); i++){
			if(this.virtualSensors.get(i).fields.size() > 0)
				ret.put(this.virtualSensors.get(i).getName(), this.virtualSensors.get(i).getDescription());
		}
		return ret;
	}
	
	/**
	 * @author Ste
	 *
	 */
	public static class VirtualSensor implements Parcelable{
		private String name = "";
		private String description = "";
		private int index = -1;
		protected ArrayList<VSField> fields = new ArrayList<VSField>();
		
		public VirtualSensor(Element e, int index){
			name = e.getAttribute("name");
			description = e.getAttribute("description");
			this.index = index;
			NodeList nodes = e.getElementsByTagName("field");
			for (int i = 0; i < nodes.getLength(); i++) {
				this.fields.add(new VSField((Element) nodes.item(i), i));
			}
		}
		
		public VirtualSensor(Parcel in) {
			name = in.readString();
			description = in.readString();
			index = in.readInt();
			fields = new ArrayList<VSField>();
			in.readTypedList(fields, VSField.CREATOR);
		}

		public String getDescription() {
			return description;
		}

		public String getName() {
			return name;
		}

		public ArrayList<VSField> getFields() {
			return fields;
		}
		
		public static class VSField implements Parcelable{
			private String name = "";
			private String type = "";
			private String description = "";
			private int index = -1;
			
			public VSField(Element e, int index){
				name = e.getAttribute("name");
				type = e.getAttribute("type");
				description = e.getAttribute("description");
				this.index = index;
			}

			public VSField(Parcel in) {
				name = in.readString();
				type = in.readString();
				index = in.readInt();
				description = in.readString();
			}

			public String getName() {
				return name;
			}

			public String getType() {
				return type;
			}

			public String getDescription() {
				return description;
			}

			@Override
			public int describeContents() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public void writeToParcel(Parcel dest, int flags) {
				dest.writeString(name);
				dest.writeString(type);
				dest.writeInt(index);
				dest.writeString(description);
			}
		
		  // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
		  public static final Parcelable.Creator<VSField> CREATOR = new Parcelable.Creator<VSField>() {
		  	public VSField createFromParcel(Parcel in) {
		  		return new VSField(in);
		  	}

		  	public VSField[] newArray(int size) {
		  		return new VSField[size];
		  	}
		  };

			public int getIndex() {
				return index;
			}
			
		} // End of VSField class

		@Override
		public int describeContents() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeString(name);
			dest.writeString(description);
			dest.writeInt(index);
			dest.writeTypedList(fields);
		}
		
	  // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
	  public static final Parcelable.Creator<VirtualSensor> CREATOR = new Parcelable.Creator<VirtualSensor>() {
	  	public VirtualSensor createFromParcel(Parcel in) {
	  		return new VirtualSensor(in);
	  	}

	  	public VirtualSensor[] newArray(int size) {
	  		return new VirtualSensor[size];
	  	}
	  };

		public int getNumFields() {
			return fields.size();
		}

		public int getIndex() {
			return index;
		}

		public int getFieldIndexByName(String name) {
			VSField field = null;
			for(int i = 0; i < fields.size(); i++){
				field = fields.get(i);
				if(field.getName() == name)
					return field.getIndex(); 
			}
			return -1;
		}
	} // End of VirtualSensor class

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(url);
		dest.writeInt(port);
		dest.writeString(name);
		dest.writeList(virtualSensors);
	}
	
  // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
  public static final Parcelable.Creator<GsnServer> CREATOR = new Parcelable.Creator<GsnServer>() {
  	public GsnServer createFromParcel(Parcel in) {
  		return new GsnServer(in);
  	}

  	public GsnServer[] newArray(int size) {
  		return new GsnServer[size];
  	}
  };

	public int getVSIndexByName(String name) {
		VirtualSensor vs = null;
		for(int i = 0; i < virtualSensors.size(); i++){
			vs = virtualSensors.get(i);
			if(vs.getName() == name)
				return vs.getIndex(); 
		}
		return -1;
	}
	
	/**
	 * Registers device to notification service on GSN server
	 * 
	 * @param regid GCM Registration ID of the device
	 * @param vs_name Virtual Sensor name
	 * @param field_name Field name (must be part of {@code vs_name})
	 * @param threshold The threshold triggering the notification
	 * @param event The event triggering the notification
	 * @return true if registration successful
	 * @throws HttpException
	 */
	public boolean registerToNotification(String regid, String vs_name, String field_name, double threshold, Event event) throws HttpException{
  	HttpParams httpParameters = new BasicHttpParams();
  	HttpConnectionParams.setConnectionTimeout(httpParameters, 5000);
  	HttpConnectionParams.setSoTimeout(httpParameters, 10000);
  	
    // Create a new HttpClient and Post Header
    HttpClient httpclient = new DefaultHttpClient(httpParameters);
    HttpPost httppost = new HttpPost("http://"+url+":"+port+"/gcm/AddVsForDevice");

    try {
        // Add your data
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("regId", regid));
        nameValuePairs.add(new BasicNameValuePair("vs_name", vs_name));
        nameValuePairs.add(new BasicNameValuePair("field", field_name));
        nameValuePairs.add(new BasicNameValuePair("threshold", Double.valueOf(threshold).toString()));
        nameValuePairs.add(new BasicNameValuePair("event", event.toString()));
        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

        // Execute HTTP Post Request
        HttpResponse response = httpclient.execute(httppost);
        int statusCode = response.getStatusLine().getStatusCode();
        if(statusCode >= 400)
        	throw new HttpException(String.valueOf(statusCode));
    } catch (ClientProtocolException e) {
    	 Log.e(TAG, e.toString());
    } catch (ConnectTimeoutException e){
    		throw new HttpException("2200");
    } catch (IOException e) {
    	Log.e(TAG, e.toString());
    	return false;
    }
    return true;
	}

	public boolean unregisterNotification(String regid, String vs_name, String field_name) throws HttpException {
  	HttpParams httpParameters = new BasicHttpParams();
  	HttpConnectionParams.setConnectionTimeout(httpParameters, 5000);
  	HttpConnectionParams.setSoTimeout(httpParameters, 10000);
  	
    // Create a new HttpClient and Post Header
    HttpClient httpclient = new DefaultHttpClient(httpParameters);
    HttpPost httppost = new HttpPost("http://"+url+":"+port+"/gcm/DelVsForDevice");

    try {
        // Add your data
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("regId", regid));
        nameValuePairs.add(new BasicNameValuePair("vs_name", vs_name));
        nameValuePairs.add(new BasicNameValuePair("field_name", field_name));
        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

        // Execute HTTP Post Request
        HttpResponse response = httpclient.execute(httppost);
        int statusCode = response.getStatusLine().getStatusCode();
        if(statusCode >= 400)
        	throw new HttpException(String.valueOf(statusCode));
    } catch (ClientProtocolException e) {
    	Log.e(TAG, e.toString());
    } catch (ConnectTimeoutException e){
    	Log.e(TAG, "unregister: "+e.toString());
    	throw new HttpException("2200");
    } catch (IOException e) {
    	Log.e(TAG, e.toString());
    	return false;
    }
    return true;
	}
	
	
  /**
   * Checks against the GSN server to see whether this device is 
   * already registered or not
   * 
   * @param regid Device Registration id
   * 
   * @return {@code true} if the application {@code regid} has already been registered on the server, {@code false} otherwise
   * @throws ConnectTimeoutException 
   */
  public boolean checkDeviceRegistration(String regid) throws ConnectTimeoutException {
    List<NameValuePair> params = new ArrayList<NameValuePair>();
    params.add(new BasicNameValuePair("regId", regid));
    String paramString = URLEncodedUtils.format(params,"utf-8");
    
  	HttpParams httpParameters = new BasicHttpParams();
  	HttpConnectionParams.setConnectionTimeout(httpParameters, 5000);
  	HttpConnectionParams.setSoTimeout(httpParameters, 10000);
  	
  	try{
	    // Create a new HttpClient and Post Header
	    HttpClient httpclient = new DefaultHttpClient(httpParameters);
      HttpGet checkReg = new HttpGet("http://"+this.url+":"+this.port+"/gcm/checkRegistration?" + paramString);
    
    	HttpResponse response = httpclient.execute(checkReg);
    	if(response.getStatusLine().getStatusCode() == 404)
    		return false;
    	BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
    	StringBuilder builder = new StringBuilder();
    	for (String line = null; (line = reader.readLine()) != null;) {
    	    builder.append(line).append("\n");
    	}
    	JSONTokener tokener = new JSONTokener(builder.toString());
    	JSONObject json_obj = new JSONObject(tokener);
    	if(json_obj.getString("found").equals("true")){
    		return true;
    	}
    	
    } catch (ClientProtocolException e) {
    	Log.e(TAG, e.toString());
    } catch (ConnectTimeoutException e){
			Log.e(TAG, "check "+e.toString());
			throw e;
		} catch (IOException e) {
    	Log.e(TAG, e.toString());
    } catch (JSONException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e){
			Log.e(TAG, e.toString());
		}
		return false;
	}

	public String getURL() {
		return this.url;
	}
	
	public int getPort() {
		return this.port;
	}
	
  /**
   * Sends the registration ID (received from GCM) to our app server, in order to register to
   * the offered services
   * 
   * @param regid
   * @return {@code true} upon successful registration, {@code false} otherwise
   * @throws HttpException 
   */
  public boolean sendRegistrationIdToBackend(String regid) throws HttpException {
  	Log.i(TAG, "Sending regid to GSN server "+this.url);
  	
  	HttpParams httpParameters = new BasicHttpParams();
  	HttpConnectionParams.setConnectionTimeout(httpParameters, 5000);
  	HttpConnectionParams.setSoTimeout(httpParameters, 10000);
  	
    // Create a new HttpClient and Post Header
    HttpClient httpclient = new DefaultHttpClient(httpParameters);
    HttpPost httppost = new HttpPost("http://"+this.url+":"+this.port+"/gcm/RegisterDevice");

    try {
        // Add your data
       	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("regId", regid));
        /*nameValuePairs.add(new BasicNameValuePair("vs_name", "lamare_batt"));
        nameValuePairs.add(new BasicNameValuePair("field", "BATT_VOLT"));
        nameValuePairs.add(new BasicNameValuePair("threshold", "12.65"));
        nameValuePairs.add(new BasicNameValuePair("event", "BELOW"));*/
        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

        // Execute HTTP Post Request
        HttpResponse response = httpclient.execute(httppost);
        int statusCode = response.getStatusLine().getStatusCode();
        if(statusCode >= 400)
        	throw new HttpException(String.valueOf(statusCode));
    } catch (ClientProtocolException e) {
    	 Log.e(TAG, e.toString());
    } catch (ConnectTimeoutException e){
    		throw new HttpException("2200");
    } catch (IOException e) {
    	Log.e(TAG, e.toString());
    	return false;
    }
    return true;
  }

	public void unregisterDevice(String regid) throws HttpException {
  	HttpParams httpParameters = new BasicHttpParams();
  	HttpConnectionParams.setConnectionTimeout(httpParameters, 5000);
  	HttpConnectionParams.setSoTimeout(httpParameters, 10000);
  	
    // Create a new HttpClient and Post Header
    HttpClient httpclient = new DefaultHttpClient(httpParameters);
    HttpPost httppost = new HttpPost("http://"+url+":"+port+"/gcm/unregister");

    try {
        // Add your data
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("regId", regid));
        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

        // Execute HTTP Post Request
        HttpResponse response = httpclient.execute(httppost);
        int statusCode = response.getStatusLine().getStatusCode();
        if(statusCode >= 400)
        	throw new HttpException(String.valueOf(statusCode));
    } catch (ClientProtocolException e) {
    	Log.e(TAG, e.toString());
    } catch (ConnectTimeoutException e){
    	Log.e(TAG, "unregister: "+e.toString());
    	throw new HttpException("2200");
    } catch (IOException e) {
    	Log.e(TAG, e.toString());
    }
	}
	
} // End of GsnServer class
