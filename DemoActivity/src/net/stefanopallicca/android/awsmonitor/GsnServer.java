package net.stefanopallicca.android.awsmonitor;

import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.os.Parcel;
import android.os.Parcelable;

public class GsnServer implements Parcelable {
	private String url = "";
	private int port = 22001;
	
	private String name = "";
	public ArrayList<VirtualSensor> virtualSensors = new ArrayList<VirtualSensor>();;
	
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
				this.fields.add(new VSField((Element) nodes.item(i)));
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
			
			public VSField(Element e){
				name = e.getAttribute("name");
				type = e.getAttribute("type");
				description = e.getAttribute("description");
			}

			public VSField(Parcel in) {
				name = in.readString();
				type = in.readString();
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
	
	
} // End of GsnServer class
