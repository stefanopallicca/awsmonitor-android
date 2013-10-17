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

public class GsnServer {
	private String url = "";
	private int port = 22001;
	
	private String name = "";
	public ArrayList<VirtualSensor> virtualSensors = new ArrayList<VirtualSensor>();;
	
	public GsnServer(String url, int port){
		this.url = url;
		this.port = port;
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
				this.virtualSensors.add(new VirtualSensor((Element) nodes.item(i)));
				/*Element element = (Element) nodes.item(i);
				NodeList title = element.getElementsByTagName();
				Element line = (Element) title.item(0);
				phoneNumberList.add(line.getTextContent());*/
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
	public class VirtualSensor {
		private String name = "";
		private String description = "";
		public ArrayList<VSField> fields = new ArrayList<VSField>();
		
		public VirtualSensor(Element e){
			this.name = e.getAttribute("name");
			this.description = e.getAttribute("description");
			NodeList nodes = e.getElementsByTagName("field");
			for (int i = 0; i < nodes.getLength(); i++) {
				this.fields.add(new VSField((Element) nodes.item(i)));
			}
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
		
		public class VSField{
			private String name = "";
			private String type = "";
			
			public VSField(Element e){
				this.name = e.getAttribute("name");
				this.type = e.getAttribute("type");
			}

			public String getName() {
				return name;
			}

			public String getType() {
				return type;
			}
			
		}
	}
	
	
}
